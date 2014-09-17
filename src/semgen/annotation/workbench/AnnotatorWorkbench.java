package semgen.annotation.workbench;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.SemGen;
import semgen.annotation.AnnotationCopier;
import semgen.annotation.BatchCellML;
import semgen.annotation.dialog.SemanticSummaryDialog;
import semgen.annotation.dialog.selectordialog.AnnotationComponentReplacer;
import semgen.annotation.dialog.selectordialog.RemovePhysicalComponentDialog;
import semgen.encoding.Encoder;
import semgen.resource.CSVExporter;
import semgen.resource.SemGenError;
import semgen.resource.SemGenTask;
import semgen.resource.Workbench;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.file.SemGenSaveFileChooser;
import semgen.resource.uicomponents.ProgressBar;
//import semsim.SemSim;
//import semsim.SemSimLibrary;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.SemSimRelation;
import semsim.model.computational.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.reading.ModelClassifier;
import semsim.writing.CellMLwriter;

public class AnnotatorWorkbench implements Workbench, Observer {
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)

	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	protected AnnotatorObservable aofocus;
	public CodewordAnnotations cwa;
	public SubModelAnnotations sma;
	public ModelAnnotations mla;
	private SemSimModel semsimmodel;
	//private SemSimLibrary semsimlib = SemSim.semsimlib;
	private boolean modelsaved = true;
	private int lastsavedas = -1;
	
	public AnnotatorWorkbench(File file, Boolean autoann) {
		sourcefile = file;
		loadModel();
		if (autoann) {
			try {
				autoAnnotate();
			}
			catch (IOException | OWLException | JDOMException | ServiceException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void loadModel() {
		NewAnnotatorTask task = new NewAnnotatorTask(false);
		task.execute();
		mla = new ModelAnnotations(semsimmodel);
		mla.addObserver(this);
		cwa = new CodewordAnnotations(semsimmodel);
		cwa.addObserver(this);
		sma = new SubModelAnnotations(semsimmodel);
		sma.addObserver(this);		
	}	
	
	public void addModelAnnotation(SemSimRelation rel, String ann) {
		mla.addModelAnnotation(rel, ann);
		setModelSaved(false);
	}
	
	public void autoAnnotate() throws OWLException, IOException, JDOMException, ServiceException {
		AutoAnnotate aa = new AutoAnnotate(sourcefile, semsimmodel);
		aa.doAutoAnnotate(true);
	}
	
	public void addObservertoSubModels(Observer obs) {
		sma.addObserver(obs);
	}
	
	public void addObservertoCodewords(Observer obs) {
		cwa.addObserver(obs);
	}
	
	public void addObservertoModelAnnotator(Observer obs) {
		mla.addObserver(obs);
	}
	
	public boolean getModelSaved(){
		return modelsaved;
	}
	
	public void setModelSaved(boolean val){
		modelsaved = val;
	}
	
	public int getLastSavedAs() {
		return lastsavedas;
	}
	
	public String getCurrentModelName() {
		return semsimmodel.getName();
	}
	
	public String getModelSourceFile() {
		return semsimmodel.getLegacyCodeLocation();
	}
	
	public void changeModelSourceFile() {
		mla.changeModelSourceFile();
	}

	public File saveModel() {
		java.net.URI fileURI = sourcefile.toURI();
		if(fileURI!=null){
			Set<DataStructure> unspecds = new HashSet<DataStructure>();
			unspecds.addAll(semsimmodel.getDataStructuresWithUnspecifiedAnnotations());
			if(unspecds.isEmpty()){
				try {
					if(lastsavedas==ModelClassifier.SEMSIM_MODEL)
						manager.saveOntology(semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(fileURI));
					else if(lastsavedas==ModelClassifier.CELLML_MODEL){
						File outputfile =  new File(fileURI);
						String content = new CellMLwriter().writeToString(semsimmodel);
						SemSimUtil.writeStringToFile(content, outputfile);
					}
				} catch (Exception e) {e.printStackTrace();}		
				SemGen.logfilewriter.println(sourcefile.getName() + " was saved");
			}
			else{
				SemGenError.showUnspecifiedAnnotationError(null, unspecds);
			}
		}
		else{
			return saveModelAs();
		}
		setModelSaved(true);
		
		return sourcefile;
	}
	
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
	
	public void encodeModel() {
		new Encoder(semsimmodel, sourcefile.getName());
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		setModelSaved(false);
	}
	
	public void AnnotateAction(File file, Boolean autosave) {	
		semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file);
		
		if(semsimmodel!=null){			
			if(semsimmodel.getErrors().isEmpty()){
				setModelSaved(semsimmodel.getSourceModelType()==ModelClassifier.SEMSIM_MODEL ||
						semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL);
					
			if(getModelSaved()) lastsavedas = semsimmodel.getSourceModelType();
					
			// Add unspecified physical model components for use during annotation
			semsimmodel.addCustomPhysicalEntity(semsimmodel.unspecifiedName, "Non-specific entity for use as a placeholder during annotation");
			semsimmodel.addCustomPhysicalProcess(semsimmodel.unspecifiedName, "Non-specific process for use as a placeholder during annotation");

			}
		}
	}

	public class NewAnnotatorTask extends SemGenTask {
		public File file;
		public boolean autosave;
        public NewAnnotatorTask(boolean autosave){
        	SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate");
        	file = sgc.getSelectedFile();
        	this.autosave = autosave;
        }
        
        public NewAnnotatorTask(File f, boolean autosave){
        	file=f;
        }
        @Override
        public Void doInBackground(){
        	if (file==null) endTask(); //If no file was selected, abort
        	progframe = new ProgressBar("Loading " + file.getName() + "...", true);
    			System.out.println("Loading " + file.getName());
    			progframe.updateMessage("Loading " + file.getName() + "...");
    			try{
    				AnnotateAction(file, autosave);
    			}
    			catch(Exception e){e.printStackTrace();}
            return null;
        }
    }
	
	public void changeFocus(AnnotatorObservable ao) {
		aofocus = ao;
		aofocus.notifyObservers();
	}
	
	public String getFocusName() {
		return aofocus.getName();
	}
	
	public void summarizeSemantics() {
		new SemanticSummaryDialog(semsimmodel);
	}
	
	public void removeRefTermfromModel() {
		ArrayList<PhysicalModelComponent> pmcs = new ArrayList<PhysicalModelComponent>();
		for(PhysicalModelComponent pmc : semsimmodel.getPhysicalModelComponents()){
			if(!(pmc instanceof CompositePhysicalEntity))
				pmcs.add(pmc);
		}
		ArrayList<String> names = SemSimUtil.getNamesfromComponentSet(pmcs, true);
		new RemovePhysicalComponentDialog(names);
	}
	
	public void replaceTerminModel() {
		try {
			new AnnotationComponentReplacer();
		} catch (OWLException e1) {
			e1.printStackTrace();
		}
	}
	
	public void doBatchCellML() {
		try {new BatchCellML(semsimmodel);}
		catch (Exception e1) {e1.printStackTrace();}
	}
	
	public void exportCSV() {
		try {
			new CSVExporter(semsimmodel).exportCodewords();
		} catch (Exception e1) {e1.printStackTrace();} 
	}
	
	public void requestModelLevelAnnotator() {
		mla.editModelAnnotations();
	}
	
	public void importandAnnotate() {
		try {
			new AnnotationCopier();
		} catch (OWLException | CloneNotSupportedException e1) {
			e1.printStackTrace();
		} 
	}
	
	public void assignCodewordstoSubmodel(Integer index) {
		Submodel target = sma.getSubModel(index);
		
		ArrayList<DataStructure> selection = cwa.codewordSelectionDialog(
				"Select codewords for " + target.getName(), (ArrayList<DataStructure>) target.getAssociatedDataStructures());
		if (selection != null) {
			
			target.setAssociatedDataStructures((Set<DataStructure>) selection);
		}
	}
	
	// This replaces the physical component with the *unspecified* component place-holder
		public void removeComponentFromModel(PhysicalModelComponent removedComp) {
			for(PhysicalProperty prop: semsimmodel.getPhysicalProperties()){
				// Remove the component's use from non-composite physical entities
				if(prop.getPhysicalPropertyOf()==removedComp){

					if(prop.getPhysicalPropertyOf() instanceof PhysicalEntity)
						prop.setPhysicalPropertyOf(semsimmodel.getCustomPhysicalEntityByName(semsimmodel.unspecifiedName));
					if(prop.getPhysicalPropertyOf() instanceof PhysicalProcess){
						// Need to do sources, sinks, meds here?
						prop.setPhysicalPropertyOf(semsimmodel.getCustomPhysicalProcessByName(semsimmodel.unspecifiedName));
					}

				}
				// Remove the component's use from composite physical entities
				else if(prop.getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
					CompositePhysicalEntity cpe = (CompositePhysicalEntity)prop.getPhysicalPropertyOf();

					for(int k=0; k<cpe.getArrayListOfEntities().size(); k++){
						if(cpe.getArrayListOfEntities().get(k)==removedComp)
							cpe.getArrayListOfEntities().set(k, semsimmodel.getCustomPhysicalEntityByName(semsimmodel.unspecifiedName));
					}
				}
			}
		}
}
