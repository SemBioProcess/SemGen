package semgen.merging.workbench;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.encoding.Encoder;
import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.merging.workbench.ModelOverlapMap.maptype;
import semgen.utilities.Workbench;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelClassifier;

public class MergerWorkbench extends Workbench {
	private int modelselection = -1;
	private ModelOverlapMap overlapmap = null;
	private ArrayList<SemSimModel> loadedmodels = new ArrayList<SemSimModel>();
	private SemSimModel mergedmodel;
	private ArrayList<File> filepathlist = new ArrayList<File>();
	private ArrayList<ArrayList<DataStructure>> dsnamelist = new ArrayList<ArrayList<DataStructure>>();
	
	public enum MergeEvent {
		functionalsubmodelerr, threemodelerror, modellistupdated, modelerrors,
		mapfocuschanged, mappingadded, mergecompleted;
		
		String message = null;
		
		private MergeEvent() {}
		
		public boolean hasMessage() {
			return message != null;
		}
		
		public String getMessage() {
			String msg = message;
			message = null;
			return msg;
		}
		
		public void setMessage(String msg) {
			message = msg;
		}
	}

	@Override
	public void initialize() {}
	
	private SemSimModel loadModel(File file, boolean autoannotate) {
		SemSimModel modeltoload = LoadSemSimModel.loadSemSimModelFromFile(file, autoannotate);
		
		if(modeltoload.getFunctionalSubmodels().size()>0) {
			CellMLModelError(file.getName());
		}
		
		return modeltoload;
	}
	public int getNumberofStagedModels() {
		return loadedmodels.size();
	}
	
	public boolean addModels(Set<File> files, boolean autoannotate) {
		if (loadedmodels.size() == 2) {
			setChanged();
			notifyObservers(MergeEvent.threemodelerror);
			return false;
		}
		
		SemSimModel model;
		for (File file : files) {
			if (ModelClassifier.classify(file)==ModelClassifier.CELLML_MODEL) {
				CellMLModelError(file.getName());
				continue;
			}
			model = loadModel(file, autoannotate);
			loadedmodels.add(model);
			filepathlist.add(file);
			addDSNameList(model.getDataStructures());
		}

		notifyModelListUpdated();
		return true;
	}
	
	private void addDSNameList(Collection<DataStructure> dslist) {
		dsnamelist.add(SemSimUtil.alphebetizeSemSimObjects(dslist));
	}
	
	public Pair<DataStructureDescriptor,DataStructureDescriptor> getDSDescriptors(int index) {
		return overlapmap.getDSPairDescriptors(index);
	}
	
	public void reloadModel(int index, boolean autoannotate) {
		File path = filepathlist.get(index);
		loadedmodels.set(index, loadModel(path, autoannotate));
	}
	
	public void reloadAllModels(boolean autoannotate) {
		for (int i=0; i<loadedmodels.size(); i++) {
			reloadModel(i, autoannotate);
		}
		notifyModelListUpdated();
	}
	
	public void removeSelectedModel() {
		if (modelselection == -1) return;
		loadedmodels.remove(modelselection);
		filepathlist.remove(modelselection);
		notifyModelListUpdated();
	}
	
	//Temporary
	public SemSimModel getModel(int index) {
		return loadedmodels.get(index);
	}
	
	public boolean hasMultipleModels() {
		return (loadedmodels.size() > 1);
	}
	
	public boolean validateModels() {
		for (SemSimModel model : loadedmodels) {
			if (!model.getErrors().isEmpty()) {
				MergeEvent.functionalsubmodelerr.setMessage(model.getName());
				setChanged();
				notifyObservers(MergeEvent.functionalsubmodelerr);
				return false;
			}
		}
		
		return true;
	}
	
	public void mapModels() {
		SemanticComparator comparator = new SemanticComparator(loadedmodels.get(0), loadedmodels.get(1));
		overlapmap = new ModelOverlapMap(0, 1, comparator);
		setChanged();
		notifyObservers(MergeEvent.mapfocuschanged);
	}
	
	public HashMap<String, String> createIdenticalNameMap(ArrayList<ResolutionChoice> choicelist) {
		HashMap<String, String> identicalmap = new HashMap<String,String>();
		Set<String> identolnames = new HashSet<String>();
		for (int i=0; i<choicelist.size(); i++) {	
			if (!choicelist.get(i).equals(ResolutionChoice.ignore)) {
				identolnames.add(overlapmap.getDataStructurePairNames(i).getLeft());
			}
		}
		for (String name : overlapmap.getIdenticalNames()) {
			if (!identolnames.contains(name)) {
				identicalmap.put(name, "");
			}
		}
		return identicalmap;
	}
	
	public Pair<String, String> getMapPairNames(int index) {
		return overlapmap.getDataStructurePairNames(index);
	}
	
	public String getMapPairType(int index) {
		return overlapmap.getMappingType(index);
	}
	
	public Pair<String, String> getOverlapMapModelNames() {
		Pair<Integer, Integer> indicies = overlapmap.getModelIndicies();
		return Pair.of(loadedmodels.get(indicies.getLeft()).getName(), 
				loadedmodels.get(indicies.getRight()).getName());
	}
	
	public ArrayList<String> getModelNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (SemSimModel model : loadedmodels) {
			names.add(model.getName());
		}
		return names;
	}
	
	public void addSemanticCodewordMapping(String cdwd1, String cdwd2) {
		addCodewordMapping(cdwd1, cdwd2, maptype.exactsemaoverlap);
	}
	
	public Pair<String,String> addManualCodewordMapping(int cdwd1, int cdwd2) {
		Pair<Integer, Integer> minds = overlapmap.getModelIndicies();
		DataStructure ds1 = dsnamelist.get(minds.getLeft()).get(cdwd1);
		DataStructure ds2 = dsnamelist.get(minds.getRight()).get(cdwd2);
				
		if (codewordMappingExists(ds1, ds2)) return Pair.of(ds1.getName(),ds2.getName());
		addCodewordMapping(ds1, ds2
				, maptype.manualmapping);
		setChanged();
		notifyObservers(MergeEvent.mappingadded);
		return null;
	}
	
	public int getMappingCount() {
		return overlapmap.getMappingCount();
	}
	
	public boolean hasSemanticOverlap() {
		return (overlapmap.getMappingCount()>0);
	}
	
	private void addCodewordMapping(String cdwd1, String cdwd2, maptype maptype) {
		Pair<Integer, Integer> minds = overlapmap.getModelIndicies();
		DataStructure ds1 = loadedmodels.get(minds.getLeft()).getDataStructure(cdwd1);
		DataStructure ds2 = loadedmodels.get(minds.getRight()).getDataStructure(cdwd2);
		
		overlapmap.addDataStructureMapping(ds1, ds2, maptype);
	}
	
	private void addCodewordMapping(DataStructure ds1, DataStructure ds2, maptype maptype) {
		overlapmap.addDataStructureMapping(ds1, ds2, maptype);
	}
	
	private boolean codewordMappingExists(DataStructure ds1, DataStructure ds2) {
		return overlapmap.dataStructuresAlreadyMapped(ds1, ds2);
	}
	
	public boolean codewordMappingExists(String cdwd1uri, String cdwd2uri) {
		return overlapmap.codewordsAlreadyMapped(cdwd1uri, cdwd2uri);
	}
	
	public void setSelection(int index) {
		modelselection = index;
	}
	
	private Pair<SemSimModel, SemSimModel> getModelOverlapMapModels(ModelOverlapMap map) {
		Pair<Integer, Integer> indexpair = map.getModelIndicies();
		return Pair.of(loadedmodels.get(indexpair.getLeft()),loadedmodels.get(indexpair.getRight()));
	}
	
	public ArrayList<Boolean> getUnitOverlaps() {
		return overlapmap.compareDataStructureUnits();
	}
	
	public String executeMerge(HashMap<String,String> namemap, ArrayList<ResolutionChoice> choices, 
			ArrayList<Pair<Double,String>> conversions, SemGenProgressBar bar) {
		Pair<SemSimModel, SemSimModel> models = getModelOverlapMapModels(overlapmap);

		if(models.getLeft().getSolutionDomains().size()>1 || models.getRight().getSolutionDomains().size()>1){
			return "One of the models to be merged has multiple solution domains.";
		}
		
		MergerTask task = new MergerTask(models, overlapmap, namemap, choices, conversions, bar) {
			public void endTask() {
				mergedmodel = getMergedModel();
				setChanged();
				notifyObservers(MergeEvent.mergecompleted);
			}
		};
		task.execute();
		
		return null;
	}
	
	public void saveMergedModel(File file) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.saveOntology(mergedmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(file.toURI()));
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean getModelSaved() {
		return false;
	}

	@Override
	public void setModelSaved(boolean val) {
		
	}

	@Override
	public String getCurrentModelName() {
		if (modelselection == -1) return null;
		return loadedmodels.get(modelselection).getName();
	}

	@Override
	public String getModelSourceFile() {
		if (modelselection == -1) return null;
		return loadedmodels.get(modelselection).getLegacyCodeLocation();
	}

	@Override
	public File saveModel() {
		return null;

	}

	@Override
	public File saveModelAs() {
		return null;
	}
	
	private void CellMLModelError(String name) {
		MergeEvent.functionalsubmodelerr.setMessage(name);
		setChanged();
		notifyObservers(MergeEvent.functionalsubmodelerr);
		notifyModelListUpdated();
	}
	
	private void notifyModelListUpdated() {
		modelselection = -1;
		setChanged();
		notifyObservers(MergeEvent.modellistupdated);
	}
	
	public String getMergedModelName() {
		return mergedmodel.getName();
	}
	
	public void encodeMergedModel(String filepath) {
		new Encoder(mergedmodel, filepath.substring(0, filepath.lastIndexOf(".")));
	}
	
	public String getModelName(int index) {
		return loadedmodels.get(index).getName();
	}
	
	//Get all Data Structure names and add descriptions if available.
	public ArrayList<String> getDSNamesandDescriptions(int index) {
		ArrayList<String> namelist = new ArrayList<String>();
		for (DataStructure ds : dsnamelist.get(index)) {
			String desc = "(" + ds.getName() + ")";
			if(ds.getDescription()!=null){
				desc = ds.getDescription() + " " + desc;
			}
			namelist.add(desc);
		}
		return namelist;
	}
}
