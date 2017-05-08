package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import semgen.GlobalActions;
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
import semgen.utilities.file.SaveSemSimModel;
import semgen.utilities.file.SemGenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelAccessor;
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
		SMLISTCHANGED, FREE_TEXT_CHANGED, SMNAMECHANGED, CWLIST_CHANGED }
	
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
		return (semsimmodel.getSourceModelType()==ModelType.SEMSIM_MODEL || 
				semsimmodel.getSourceModelType()==ModelType.SEMSIM_MODEL ||
				semsimmodel.getSourceModelType()==ModelType.CELLML_MODEL ||
				semsimmodel.getSourceModelType()==ModelType.MML_MODEL_IN_PROJ);
	}

	@Override
	public void setModelSaved(boolean val){
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
	
	private void validateModelComposites() {
		new ModelComponentValidator(this, semsimmodel);
	}

	@Override
	public ModelAccessor saveModel(Integer index) {
		
		URI fileuri = modelaccessor.getFileThatContainsModelAsURI();
		
		if(fileuri != null){
			File file = new File(fileuri);
			validateModelComposites();
			SaveSemSimModel.writeToFile(semsimmodel, modelaccessor, file, lastsavedas);			
			setModelSaved(true);
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
		
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "cellml", "sbml","proj"}, selectedtype, semsimmodel.getName());
		
		ModelAccessor newaccessor = filec.SaveAsAction(semsimmodel);

		if (newaccessor != null) {
			
			modelaccessor = newaccessor;
			
			if(filec.getFileFilter() == SemGenFileChooser.owlfilter)
				lastsavedas = ModelType.SEMSIM_MODEL;
			else if(filec.getFileFilter() == SemGenFileChooser.sbmlfilter)
				lastsavedas = ModelType.SBML_MODEL;
			else if(filec.getFileFilter() == SemGenFileChooser.projfilter)
				lastsavedas = ModelType.MML_MODEL_IN_PROJ;
			else if(filec.getFileFilter() == SemGenFileChooser.cellmlfilter)
				lastsavedas = ModelType.CELLML_MODEL;
			else if(filec.getFileFilter() == SemGenFileChooser.mmlfilter)
				lastsavedas = ModelType.MML_MODEL;
				
			saveModel(0);
			semsimmodel.setName(modelaccessor.getModelName());
			
			return modelaccessor;
		}
		
		return null;
	}
	
	public void exportModel(Integer index){
		
		String suggestedfilename = FilenameUtils.removeExtension(modelaccessor.getFileThatContainsModel().getName());
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser("owl", semsimmodel.getName(),suggestedfilename);
		
		ModelAccessor ma = filec.SaveAsAction(semsimmodel);

		if (ma != null) {
			validateModelComposites();
			SaveSemSimModel.writeToFile(semsimmodel, ma, ma.getFileThatContainsModel(), filec.getFileFilter());			
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
			
			else if( ! modelaccessor.modelIsPartOfArchive())
				msg = msg + modelaccessor.getFileThatContainsModel().getName() + "?";
			else msg = msg + modelaccessor.getModelName() + " in " + modelaccessor.getFileThatContainsModel().getName() + "?";
			
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
		File returnfile = new File(getModelAccessor().getFileThatContainsModel().getParent() + "/" + smdrawer.getHrefValue());
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
		if ((arg1==WBEvent.SMSELECTION) || (arg1==WBEvent.CWSELECTION) || arg1==ModelChangeEnum.METADATASELECTED){
			setChanged();
			notifyObservers(arg1);
		}
		if (arg1.equals(ModelEdit.SMNAMECHANGED)) {
			cwdrawer.reloadCodewords();

		}
		if (arg1==ModelEdit.FREE_TEXT_CHANGED || arg1==ModelEdit.CODEWORD_CHANGED || arg1==ModelEdit.SUBMODEL_CHANGED
				|| arg1==LibraryEvent.SINGULAR_TERM_CHANGE || arg1.equals(LibraryEvent.COMPOSITE_ENTITY_CHANGE) 
				|| arg1.equals(LibraryEvent.PROCESS_CHANGE) || arg1.equals(ModelChangeEnum.METADATACHANGED) || arg1.equals(ModelChangeEnum.METADATAIMPORTED)
				|| arg1.equals(ModelChangeEnum.SOURCECHANGED) || arg1.equals(ModelEdit.SMNAMECHANGED)) {
			this.setModelSaved(false);
		}
	}
}
