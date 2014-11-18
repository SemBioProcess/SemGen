package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.resource.CSVExporter;
import semgen.resource.SemGenError;
import semgen.resource.SemGenTask;
import semgen.resource.Workbench;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.file.SemGenSaveFileChooser;
import semgen.resource.uicomponent.SemGenProgressBar;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.SemSimRelation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelClassifier;
import semsim.writing.CellMLwriter;

public class AnnotatorWorkbench extends Observable implements Workbench, Observer {
	private SemSimModel semsimmodel;
	private File sourcefile; //File originally loaded at start of Annotation session (could be 
							//in SBML, MML, CellML or SemSim format)
	private ModelAnnotations modanns;
	private boolean modelsaved = true;
	private int lastsavedas = -1;
	
	public AnnotatorWorkbench() {}
	
	public boolean initialize(boolean autoannotate) {
		NewAnnotatorTask task = new NewAnnotatorTask(autoannotate);
		return runTask(task);
	}
	
	public boolean initialize(File file, boolean autoannotate) {
		NewAnnotatorTask task = new NewAnnotatorTask(file, autoannotate);
		return runTask(task);
	}
	
	private boolean runTask(NewAnnotatorTask task) {
		task.execute();
		while (!task.isDone()) task.showProgressBar(); //Pause
		if (sourcefile == null) {
			return false;
		}
		modanns = new ModelAnnotations(semsimmodel);
		modanns.addObserver(this);
		return true;
	}
	
	@Override
	public boolean loadModel(boolean autoannotate) {
		semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(sourcefile, autoannotate);
		lastsavedas = semsimmodel.getSourceModelType();
		
		if(semsimmodel.getErrors().isEmpty()){
			setModelSaved(isSemSimorCellMLModel());
			
			// Add unspecified physical model components for use during annotation
			semsimmodel.addCustomPhysicalEntity(SemSimModel.unspecifiedName, "Non-specific entity for use as a placeholder during annotation");
			semsimmodel.addCustomPhysicalProcess(SemSimModel.unspecifiedName, "Non-specific process for use as a placeholder during annotation");
			return true;
		}
		return false; //Model has errors, abort
	}
	
	public void addObservertoModelAnnotator(Observer obs) {
		modanns.addObserver(obs);
	}
	
	//Temporary
	public SemSimModel getSemSimModel() {
		return semsimmodel;
	}
	
	public boolean isSemSimorCellMLModel() {
		return (semsimmodel.getSourceModelType()==ModelClassifier.SEMSIM_MODEL || 
				semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL);
	}
	
	public boolean getModelSaved(){
		return modelsaved;
	}
	
	public void setModelSaved(boolean val){
		modelsaved = val;
		setChanged();
		notifyObservers(GlobalActions.appactions.SAVED);
	}
	
	public int getLastSavedAs() {
		return lastsavedas;
	}

	@Override
	public String getCurrentModelName() {
		return semsimmodel.getName();
	}

	public String getModelSourceFile() {
		return semsimmodel.getLegacyCodeLocation();
	}

	@Override
	public File saveModel() {
		java.net.URI fileURI = sourcefile.toURI();
		if(fileURI!=null){
			Set<DataStructure> unspecds = new HashSet<DataStructure>();
			unspecds.addAll(semsimmodel.getDataStructuresWithUnspecifiedAnnotations());
			if(unspecds.isEmpty()){
				try {
					if(lastsavedas==ModelClassifier.SEMSIM_MODEL) {
						OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
						manager.saveOntology(semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(fileURI));
					}
					else if(lastsavedas==ModelClassifier.CELLML_MODEL){
						File outputfile =  new File(fileURI);
						String content = new CellMLwriter().writeToString(semsimmodel);
						SemSimUtil.writeStringToFile(content, outputfile);
					}
				} catch (Exception e) {e.printStackTrace();}		
				SemGen.logfilewriter.println(sourcefile.getName() + " was saved");
			}
			else{
				SemGenError.showUnspecifiedAnnotationError(unspecds);
			}
		}
		else{
			return saveModelAs();
		}
		setModelSaved(true);
		
		return sourcefile;
	}

	@Override
	public File saveModelAs() {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser("Choose location to save file", new String[]{"cellml","owl"});
		if (filec.SaveAsAction()!=null) {
			sourcefile = filec.getSelectedFile();
			lastsavedas = filec.getFileType();
			saveModel();
			semsimmodel.setName(sourcefile.getName().substring(0, sourcefile.getName().lastIndexOf(".")));
			return sourcefile;
		}
		return null;
	}

	public void exportCSV() {
		try {
			new CSVExporter(semsimmodel).exportCodewords();
		} catch (Exception e1) {e1.printStackTrace();} 
	}
	
	public boolean unsavedChanges() {
		if (!getModelSaved()) {
			String title = "[unsaved file]";
			URI fileURI = sourcefile.toURI();
			if(fileURI!=null){
				title =  new File(fileURI).getName();
			}
			int returnval= JOptionPane.showConfirmDialog(null,
					"Save changes?", title + " has unsaved changes",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (returnval == JOptionPane.CANCEL_OPTION)
				return false;
			if (returnval == JOptionPane.YES_OPTION) {
				if(saveModel()==null) {
					return false;
				}
			}
		}
		return true;
	}
	public File getFile() {
		return sourcefile;
	}
	
	public String getSourceModelLocation() {
		return semsimmodel.getLegacyCodeLocation();
	}

	public void changeModelSourceFile() {
		modanns.changeModelSourceFile();
	}
	
	public void addModelAnnotation(SemSimRelation rel, String ann) {
		modanns.addModelAnnotation(rel, ann);
		setModelSaved(false);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		setModelSaved(false);
	}

	public class NewAnnotatorTask extends SemGenTask {
		public boolean autoannotate;
        public NewAnnotatorTask(boolean autoann){
        	SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate");
        	sourcefile = sgc.getSelectedFile();
        	autoannotate = autoann;
        	if (sourcefile==null) endTask(); //If no file was selected, abort
        	progframe = new SemGenProgressBar("Loading " + sourcefile.getName() + "...", true);
        }
        
        public NewAnnotatorTask(File f, boolean autoann){
        	sourcefile=f;
        	autoannotate = autoann;
        }
        @Override
        public Void doInBackground(){
        	System.out.println("Loading " + sourcefile.getName());
    			progframe.updateMessage("Loading " + sourcefile.getName() + "...");
    			try{
    				if (!loadModel(autoannotate)) {
    					sourcefile=null;
    					endTask();
    				}
    			}
    			catch(Exception e){e.printStackTrace();}
            return null;
        }
    }
}
