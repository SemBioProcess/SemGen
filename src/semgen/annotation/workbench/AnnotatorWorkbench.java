package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import semgen.GlobalActions;
import semgen.SemGenGUI;
import semgen.SemGenGUI.saveTask;
import semgen.annotation.workbench.SemSimTermLibrary.LibraryEvent;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench.ModelChangeEnum;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.annotation.workbench.routines.AnnotationImporter;
import semgen.annotation.workbench.routines.ModelComponentValidator;
import semgen.annotation.workbench.routines.TermCollector;
import semgen.annotation.workbench.routines.TermModifier;
import semgen.utilities.CSVExporter;
import semgen.utilities.Workbench;
import semgen.utilities.file.SemGenSaveFileChooser;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalModelComponent;
import semsim.reading.ModelClassifier.ModelType;

public class AnnotatorWorkbench extends Workbench implements Observer {
	private SemSimModel semsimmodel;
	protected ModelAccessor modelaccessor; //File originally loaded at start of Annotation session (could be 
							//in SBML, MML, CellML or SemSim format)
	private ModelAnnotationsBench modanns;
	private SemSimTermLibrary termlib;
	private CodewordToolDrawer cwdrawer;
	private SubModelToolDrawer smdrawer;
	private ModelType lastsavedas = ModelType.UNKNOWN;
	public static enum WBEvent {FREETEXT_REQUEST, IMPORT_FREETEXT, SMSELECTION, CWSELECTION}
	public static enum LibraryRequest {REQUEST_IMPORT, REQUEST_LIBRARY, REQUEST_CREATOR, CLOSE_LIBRARY }
	public static enum ModelEdit {PROPERTY_CHANGED, COMPOSITE_CHANGED, CODEWORD_CHANGED, SUBMODEL_CHANGED, MODEL_IMPORT, 
		SMLISTCHANGED, FREE_TEXT_CHANGED, SMNAMECHANGED, SM_DATASTRUCTURES_CHANGED, SM_SUBMODELS_CHANGED, 
		CWLIST_CHANGED }
	
	public AnnotatorWorkbench(ModelAccessor accessor, SemSimModel model) {
		semsimmodel = model;
		this.modelaccessor = accessor;
		lastsavedas = semsimmodel.getSourceModelType();	
	}
	
	@Override
	public void initialize() {
		termlib = new SemSimTermLibrary(semsimmodel);
		termlib.addObserver(this);
		modanns = new ModelAnnotationsBench(semsimmodel);
		modanns.addObserver(this);
		cwdrawer = new CodewordToolDrawer(termlib, semsimmodel.getAssociatedDataStructures());
		cwdrawer.addObserver(this);
		smdrawer = new SubModelToolDrawer(termlib, semsimmodel.getSubmodels());
		smdrawer.addObserver(this);

		setModelSaved(sourceModelTypeCanStoreSemSimAnnotations());
	}
	
	public CodewordToolDrawer openCodewordDrawer() {
		return cwdrawer;
	}
	
	public SubModelToolDrawer openSubmodelDrawer() {
		return smdrawer;
	}
	
	public ModelAnnotationsBench openModelAnnotationsWorkbench() {
		return modanns;
	}
	
	public SemSimTermLibrary openTermLibrary() {
		return termlib;
	}
	
	public void addObservertoModelAnnotator(Observer obs) {
		modanns.addObserver(obs);
	}
	
	//Temporary
	public SemSimModel getSemSimModel() {
		return semsimmodel;
	}
	
	public boolean sourceModelTypeCanStoreSemSimAnnotations() { 
		// This is distinct from lastSavedAsTypeCanStoreSemSimAnnotations. This refers to original source code read into SemGen.
		return (semsimmodel.getSourceModelType()==ModelType.SEMSIM_MODEL || 
				semsimmodel.getSourceModelType()==ModelType.SBML_MODEL ||
				semsimmodel.getSourceModelType()==ModelType.CELLML_MODEL ||
				semsimmodel.getSourceModelType()==ModelType.MML_MODEL_IN_PROJ);
	}
	
	public boolean lastSavedAsTypeCanStoreSemSimAnnotations() {
		// This is distinct from sourceModelTypeCanStoreSemSimAnnotations. This refers to the type of code that the model
		// was last saved out as.
		return (getLastSavedAs()==ModelType.SEMSIM_MODEL || 
				getLastSavedAs()==ModelType.SBML_MODEL ||
				getLastSavedAs()==ModelType.CELLML_MODEL ||
				getLastSavedAs()==ModelType.MML_MODEL_IN_PROJ);
	}

	@Override
	public void setModelSaved(boolean val){
		this.modelsaved = val;
		setChanged();
		notifyObservers(GlobalActions.appactions.SAVED);
	}
	
	public ModelType getLastSavedAs() {
		return lastsavedas;
	}

	@Override
	public String getCurrentModelName() {
		return semsimmodel.getName();
	}

	@Override
	public ModelAccessor getModelSourceLocation() {
		return semsimmodel.getLegacyCodeLocation();
	}
	
	public void validateModelComposites() {
		new ModelComponentValidator(this, semsimmodel);
	}

	@Override
	public ModelAccessor saveModel(Integer index) {
				
		if(modelaccessor != null && lastSavedAsTypeCanStoreSemSimAnnotations()){
			saveTask savetask = new SemGenGUI.saveTask(modelaccessor, semsimmodel, this, true);	
			savetask.execute();
			return modelaccessor;
		}
		return saveModelAs(index);			
	}

	@Override
	public ModelAccessor saveModelAs(Integer index) {
				
		String selectedtype = "owl";  // Default extension type
		ModelType modtype = semsimmodel.getSourceModelType();
		
		if(modtype==ModelType.MML_MODEL_IN_PROJ || modtype==ModelType.MML_MODEL) selectedtype = "proj";
		else if(modtype==ModelType.CELLML_MODEL) selectedtype = "cellml";
		else if(modtype==ModelType.SBML_MODEL) selectedtype = "sbml";
		
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "cellml", "sbml","proj", "omex"}, selectedtype, semsimmodel.getName());
		
		ModelAccessor newaccessor = filec.SaveAsAction(semsimmodel);

		if (newaccessor != null) {
			
			modelaccessor = newaccessor;
			FileFilter filter = filec.getFileFilter();
						
			if(ModelType.SEMSIM_MODEL.fileFilterMatches(filter))
				lastsavedas = ModelType.SEMSIM_MODEL;
			else if(ModelType.SBML_MODEL.fileFilterMatches(filter))
				lastsavedas = ModelType.SBML_MODEL;
			else if(ModelType.MML_MODEL_IN_PROJ.fileFilterMatches(filter))
				lastsavedas = ModelType.MML_MODEL_IN_PROJ;
			else if(ModelType.CELLML_MODEL.fileFilterMatches(filter))
				lastsavedas = ModelType.CELLML_MODEL;
				
			saveModel(0);
			semsimmodel.setName(modelaccessor.getModelName());
			
			return modelaccessor;
		}
		
		return null;
	}
	
	public void exportModel(Integer index){
		
		String suggestedfilename = FilenameUtils.removeExtension(modelaccessor.getFullPath());
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(SemGenSaveFileChooser.ALL_WRITABLE_TYPES, "owl", semsimmodel.getName(), suggestedfilename);
		
		ModelAccessor ma = filec.SaveAsAction(semsimmodel);

		if (ma != null) {
			saveTask savetask = new SemGenGUI.saveTask(ma, semsimmodel, this, false);	
			savetask.execute();
		}
	}
	

	public void exportCSV() {
		try {
			validateModelComposites();
			new CSVExporter(semsimmodel).exportCodewords();
		} catch (Exception e1) {e1.printStackTrace();} 
	}
	
	public boolean unsavedChanges() {
		if ( ! getModelSaved()) {
			String title = "Unsaved changes in model";
			String msg = "Save changes to ";
			
			URI fileURI = modelaccessor.getFileThatContainsModelAsURI();
			
			if(fileURI == null)
				msg = msg + "[unsaved file]?";
			
			else msg = msg + modelaccessor.getShortLocation() + "?";
			
			int returnval= JOptionPane.showConfirmDialog(null,
					msg, title,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			
			if (returnval == JOptionPane.CANCEL_OPTION)
				return false;
			
			if (returnval == JOptionPane.YES_OPTION) {
				
				if(saveModel(0)==null)
					return false;
				
			}
			if (returnval == JOptionPane.CLOSED_OPTION)
				return false;
		}
		return true;
	}
	public ModelAccessor getModelAccessor() {
		return modelaccessor;
	}
	
	public void setModelAccessor(ModelAccessor accessor) {
		modelaccessor = accessor;
	}

	public void changeModelSourceLocation() {
		modanns.changeModelSourceLocation();
	}
	
	public void addModelAnnotation(SemSimRelation rel, String ann) {
		modanns.addModelAnnotation(rel, ann);
		setModelSaved(false);
	}
	
	public AnnotationImporter importModelAnnotations(File file, Boolean[] options) {
		validateModelComposites();
		AnnotationImporter copier = new AnnotationImporter(termlib, semsimmodel, file, options);
	
		return copier;
	}
		
	public void compositeChanged() {
		setChanged();
		notifyObservers(ModelEdit.COMPOSITE_CHANGED);
	}
	
	private void submodelListChanged() {
		smdrawer.refreshSubModels();
		setModelSaved(false);
		setChanged();
		notifyObservers(ModelEdit.SMLISTCHANGED);
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
		notifyObservers(ModelEdit.SMLISTCHANGED);
	}
	
	public void addSubmodeltoModel(String name) {
		semsimmodel.addSubmodel(smdrawer.addSubmodel(name));
		submodelListChanged();
	}
	
	public void removeSubmodelfromModel() {
		semsimmodel.removeSubmodel(smdrawer.removeSubmodel());
		submodelListChanged();
	}
	
	public void removePhysicalComponentfromModel(PhysicalModelComponent pmc) {
		pmc.removeFromModel(semsimmodel);
		setModelSaved(false);
		compositeChanged();
	}
	
	public Boolean submitSubmodelName(String newname) {
		if (!newname.equals("") && !cwdrawer.containsComponentwithName(newname) &&
			!smdrawer.containsComponentwithName(newname) && !newname.contains("--")) {
			return true;
		}
		
		return false;
	}
	
	public AnnotatorTreeMap makeTreeMap(boolean useimports) {
		return new AnnotatorTreeMap(useimports, smdrawer, cwdrawer);
	}
	
	public void sendTermLibraryEvent(LibraryRequest evt) {
		setChanged();
		notifyObservers(evt);
	}
	
	public File getSourceSubmodelFile() {
		File returnfile = new File(getModelAccessor().getFile().getParent() + "/" + smdrawer.getHrefValue());
		return returnfile;
	}
	
	public TermCollector collectAffiliatedTermsandCodewords(Integer index) {
		return new TermCollector(this, index);
	}
	
	public void replaceTerm(TermCollector affected, Integer repindex, boolean remove) {
		new TermModifier(this, affected).runReplace(repindex, remove);
		setChanged();
		notifyObservers(ModelEdit.CODEWORD_CHANGED);
	}
	
	public void requestFreetextChange() {
		setChanged();
		notifyObservers(WBEvent.FREETEXT_REQUEST);
	}
		
	public void useCodeWindowFreetext() {
		setChanged();
		notifyObservers(WBEvent.IMPORT_FREETEXT);
	}
	
	public void updateAllListeners() {
		setChanged();
		notifyObservers(LibraryEvent.SINGULAR_TERM_CREATED ); 
		setChanged();
		notifyObservers(ModelEdit.CWLIST_CHANGED);
		setChanged();
		notifyObservers(ModelEdit.SUBMODEL_CHANGED);
		openModelAnnotationsWorkbench().notifyMetaDataImported();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		//Event forwarding
		if ((arg1==WBEvent.SMSELECTION) || (arg1==WBEvent.CWSELECTION)){
			setChanged();
			notifyObservers(arg1);
		}
		if (arg1.equals(ModelEdit.SMNAMECHANGED)) {
			cwdrawer.reloadCodewords();
		}
		if (arg1==ModelEdit.FREE_TEXT_CHANGED || arg1==ModelEdit.CODEWORD_CHANGED || arg1==ModelEdit.SUBMODEL_CHANGED
				|| arg1==LibraryEvent.SINGULAR_TERM_CHANGE || arg1.equals(LibraryEvent.COMPOSITE_ENTITY_CHANGE) 
				|| arg1.equals(LibraryEvent.PROCESS_CHANGE) || arg1.equals(LibraryEvent.FORCE_CHANGE) 
				|| arg1.equals(ModelChangeEnum.METADATACHANGED)	|| arg1.equals(ModelChangeEnum.METADATAIMPORTED) 
				|| arg1.equals(ModelChangeEnum.SOURCECHANGED)
				|| arg1.equals(ModelEdit.SMNAMECHANGED) || arg1.equals(ModelEdit.SM_DATASTRUCTURES_CHANGED) || arg1.equals(ModelEdit.SM_SUBMODELS_CHANGED)) {
			setModelSaved(false);
		}
	}
}
