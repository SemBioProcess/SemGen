package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.annotation.workbench.routines.AnnotationCopier;
import semgen.utilities.CSVExporter;
import semgen.utilities.SemGenError;
import semgen.utilities.Workbench;
import semgen.utilities.file.SemGenSaveFileChooser;
import semsim.annotation.SemSimRelation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.reading.ModelClassifier;
import semsim.utilities.SemSimUtil;
import semsim.writing.CellMLwriter;

public class AnnotatorWorkbench extends Workbench implements Observer {
	private SemSimModel semsimmodel;
	protected File sourcefile; //File originally loaded at start of Annotation session (could be 
							//in SBML, MML, CellML or SemSim format)
	private ModelAnnotationsBench modanns;
	private SemSimTermLibrary termlib;
	private CodewordToolDrawer cwdrawer;
	private SubModelToolDrawer smdrawer;
	private boolean modelsaved = true;
	private int lastsavedas = -1;
	private String ontspref;
	public static enum WBEvent {freetextrequest, smselection, cwselection }
	public static enum modeledit {compositechanged, submodelchanged, modelimport, smlistchanged, freetextchange, smnamechange }
	
	public AnnotatorWorkbench(File file, SemSimModel model) {
		semsimmodel = model;
		sourcefile = file;
		lastsavedas = semsimmodel.getSourceModelType();	
	}
	
	public void initialize() {
		modanns = new ModelAnnotationsBench(semsimmodel);
		modanns.addObserver(this);
		termlib = new SemSimTermLibrary(semsimmodel);
		cwdrawer = new CodewordToolDrawer(semsimmodel.getDataStructures());
		cwdrawer.addObserver(this);
		smdrawer = new SubModelToolDrawer(semsimmodel.getSubmodels());
		smdrawer.addObserver(this);

		// Add unspecified physical model components for use during annotation
		semsimmodel.addCustomPhysicalEntity(SemSimModel.unspecifiedName, "Non-specific entity for use as a placeholder during annotation");
		semsimmodel.addCustomPhysicalProcess(SemSimModel.unspecifiedName, "Non-specific process for use as a placeholder during annotation");

		setModelSaved(isSemSimorCellMLModel());
	}
	
	public CodewordToolDrawer openCodewordDrawer() {
		return cwdrawer;
	}
	
	public SubModelToolDrawer openSubmodelDrawer() {
		return smdrawer;
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
		Set<DataStructure> unspecds = semsimmodel.getDataStructuresWithUnspecifiedAnnotations();
		if(unspecds.isEmpty()){
			URI fileURI = sourcefile.toURI();
			if(fileURI!=null){
				try {
					if(lastsavedas==ModelClassifier.SEMSIM_MODEL) {
						OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
						manager.saveOntology(semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(fileURI));
					}
					else if(lastsavedas==ModelClassifier.CELLML_MODEL){
						File outputfile =  new File(fileURI);
						String content = new CellMLwriter(semsimmodel).writeToString();
						SemSimUtil.writeStringToFile(content, outputfile);
					}
				} catch (Exception e) {e.printStackTrace();}		
				SemGen.logfilewriter.println(sourcefile.getName() + " was saved");
				setModelSaved(true);
			}
			else{
				return saveModelAs();
			}			
		}
		else{
			SemGenError.showUnspecifiedAnnotationError(unspecds);
			return null;
		}

		return sourcefile;
	}

	@Override
	public File saveModelAs() {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser("Choose location to save file", new String[]{"owl"});
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
	
	public void importModelAnnotations() {
		AnnotationCopier copier = new AnnotationCopier(semsimmodel);
		if (copier.doCopy()) {
			setModelSaved(false);
			setChanged();
			notifyObservers();
		}
	}
	
	public ModelAnnotationsBench getModelAnnotationsWorkbench() {
		return modanns;
	}
	
	public TreeMap<String,PhysicalEntity> getPhysicalEntityIDList() {
		return termlib.getPhysEntIDMap();
	}
		
	public void compositeChanged() {
		setModelSaved(false);
		setChanged();
		notifyObservers(modeledit.compositechanged);
	}

	public String getLastOntology() {
		return ontspref;
	}
	
	public void setLastOntology(String ont) {
		ontspref = ont;
	}
	
	private void submodelListChanged() {
		smdrawer.refreshSubModels();
		setModelSaved(false);
		setChanged();
		notifyObservers(modeledit.smlistchanged);
	}
	
	public ArrayList<Integer> getSelectedSubmodelDSIndicies() {
		ArrayList<DataStructure> dslist = smdrawer.getSelectionDataStructures();
		ArrayList<Integer> dsindicies = new ArrayList<Integer>();
		
		for (DataStructure ds : dslist) {
			dsindicies.add(cwdrawer.getIndexofComponent(ds));
		}
		
		return dsindicies;
	}
	
	public void addDataStructurestoSubmodel(ArrayList<Integer> dsindicies) {
		smdrawer.setDataStructures(cwdrawer.getComponentsfromIndicies(dsindicies));
		setChanged();
		notifyObservers(modeledit.smlistchanged);
	}
	
	public void addSubmodeltoModel(String name) {
		semsimmodel.addSubmodel(smdrawer.addSubmodel(name));
		submodelListChanged();
	}
	
	public void removeSubmodelfromModel() {
		semsimmodel.removeSubmodel(smdrawer.removeSubmodel());
		submodelListChanged();
	}
	
	public Boolean submitSubmodelName(String newname) {
		if (!newname.equals("") && !cwdrawer.containsComponentwithName(newname) &&
			!smdrawer.containsComponentwithName(newname) && !newname.contains("--")) {
			return true;
		}
		
		return false;
	}
	
	public void requestFreetextChange() {
		setChanged();
		notifyObservers(WBEvent.freetextrequest);
	}
	
	public AnnotatorTreeMap makeTreeMap(boolean useimports) {
		return new AnnotatorTreeMap(useimports, smdrawer, cwdrawer);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		//Event forwarding
		if ((arg1==WBEvent.smselection) || (arg1==WBEvent.cwselection)){
			setChanged();
			notifyObservers(arg1);
		}
		if (arg1==modeledit.freetextchange) {
			this.setModelSaved(false);
		}
	}
	
	public File getSourceSubmodelFile() {
		return new File(getFile().getParent() + "/" + smdrawer.getHrefValue());
	}
}
