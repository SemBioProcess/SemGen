package semgen.merging;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.Merger.ResolutionChoice;
import semgen.merging.ModelOverlapMap.maptype;
import semgen.utilities.SemGenError;
import semgen.utilities.Workbench;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SaveSemSimModel;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.SemSimUtil;

public class MergerWorkbench extends Workbench {
	private int modelselection = -1;
	private ModelOverlapMap overlapmap = null;
	private ArrayList<SemSimModel> loadedmodels = new ArrayList<SemSimModel>();
	public SemSimModel mergedmodel;
	private ArrayList<ModelAccessor> modelaccessorlist = new ArrayList<ModelAccessor>();
	private ArrayList<ArrayList<DataStructure>> alldslist = new ArrayList<ArrayList<DataStructure>>();
	private ArrayList<ArrayList<DataStructure>> exposeddslist = new ArrayList<ArrayList<DataStructure>>();
	
	public enum MergeEvent {
		threemodelerror, modellistupdated, modelerrors,	mapfocuschanged, mappingevent, MAPPINGREMOVED, mergecompleted;
		
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
	
	private SemSimModel loadModel(ModelAccessor modelaccessor, boolean autoannotate) {
		LoadSemSimModel loader = new LoadSemSimModel(modelaccessor, autoannotate);
		loader.run();
		SemSimModel modeltoload = loader.getLoadedModel();
		return modeltoload;
	}
	public int getNumberofStagedModels() {
		return loadedmodels.size();
	}
	
	public boolean addModels(ArrayList<ModelAccessor> modelaccessors, boolean autoannotate) {
		if (loadedmodels.size() == 2) {
			setChanged();
			notifyObservers(MergeEvent.threemodelerror);
			return false;
		}
		
		SemSimModel model;
		for (ModelAccessor modelaccessor : modelaccessors) {
			model = loadModel(modelaccessor, autoannotate);
			if (SemGenError.showSemSimErrors()) continue;
			loadedmodels.add(model);
			modelaccessorlist.add(modelaccessor);
			addDSNameList(model.getAssociatedDataStructures());
		}

		notifyModelListUpdated();
		return true;
	}
	
	public boolean addModels(ArrayList<ModelAccessor> files, ArrayList<SemSimModel> models, boolean autoannotate) {
		if (loadedmodels.size() == 2) {
			setChanged();
			notifyObservers(MergeEvent.threemodelerror);
			return false;
		}
		
		SemSimModel model;
		ModelAccessor file;
		for (int i = 0; i < files.size(); i++) {
			file = files.get(i);
			model = models.get(i);
			if (model == null) {
				model = loadModel(file, autoannotate);
				if (SemGenError.showSemSimErrors()) continue;
			}
			
			loadedmodels.add(model);
			modelaccessorlist.add(file);
			addDSNameList(model.getAssociatedDataStructures());
		}
		notifyModelListUpdated();
		
		return true;
	}
	private void addDSNameList(Collection<DataStructure> dslist) {
		alldslist.add(SemSimUtil.alphebetizeSemSimObjects(dslist));
		ArrayList<DataStructure> tempdslist = new ArrayList<DataStructure>();
		
		// Iterate through the DataStructures just added and weed out CellML-style inputs
		for(DataStructure ds : alldslist.get(alldslist.size()-1)){
			if(! ds.isFunctionalSubmodelInput()){
				tempdslist.add(ds);
			}
		}
		exposeddslist.add(tempdslist);
	}
	
	//Get the objects describing a pair of semantically matched data strutures
	public Pair<DataStructureDescriptor,DataStructureDescriptor> getDSDescriptors(int index) {
		return overlapmap.getDSPairDescriptors(index);
	}
	
	public void removeSelectedModel() {
		if (modelselection == -1) return;
		loadedmodels.remove(modelselection);
		modelaccessorlist.remove(modelselection);
		overlapmap = null;
		alldslist.clear();
		exposeddslist.clear();
		if (!loadedmodels.isEmpty()) {
			addDSNameList(loadedmodels.get(0).getAssociatedDataStructures());
		}
		
		notifyModelListUpdated();
	}

	public boolean hasMultipleModels() {
		return (loadedmodels.size() > 1);
	}
	
	//Compare two models and find all semantically identical codewords and datastructures using the same name
	public void mapModels() {
		SemanticComparator comparator = new SemanticComparator(loadedmodels.get(0), loadedmodels.get(1));
		overlapmap = new ModelOverlapMap(0, 1, comparator);
		setChanged();
		notifyObservers(MergeEvent.mapfocuschanged);
	}
	
	//Create a map of identical submodel names with an empty string for the value. The empty string will
	//be replaced by the users replacement name
	public HashMap<String, String> createIdenticalSubmodelNameMap() {
		HashMap<String, String> namemap = new HashMap<String, String>();
		for (String name : overlapmap.getIdenticalSubmodelNames()) {
			namemap.put(new String(name), "");
		}
		return namemap;
	}
	
	public HashMap<String, String> createIdenticalNameMap() {
		HashMap<String, String> namemap = new HashMap<String, String>();
		for (String name : overlapmap.getIdenticalNames()) {
			namemap.put(new String(name), "");
		}
		return namemap;
	}
	
	public HashMap<String, String> createIdenticalNameMap(ArrayList<ResolutionChoice> choicelist, Set<String> submodelnamemap) {
		HashMap<String, String> identicalmap = new HashMap<String,String>();
		Set<String> identolnames = new HashSet<String>();
		for (int i=getSolutionDomainCount(); i<choicelist.size(); i++) {	
			if (!choicelist.get(i).equals(ResolutionChoice.ignore)) {
				identolnames.add(overlapmap.getDataStructurePairNames(i).getLeft());
			}
		}
		for (String name : overlapmap.getIdenticalNames()) {
			if(name.contains(".")) {
				if (submodelnamemap.contains(name.substring(0, name.lastIndexOf("."))))
					continue;
			}
				
			// If an identical codeword mapping will be resolved by a semantic resolution step or a renaming of identically-named submodels, 
		    // don't include in idneticalmap	
			if (!identolnames.contains(name)) {
				identicalmap.put(new String(name), "");
			}
		}
		return identicalmap;
	}
	
	//Get the names of the two data structures at the given index
	public Pair<String, String> getMapPairNames(int index) {
		return overlapmap.getDataStructurePairNames(index);
	}
	
	public String getMapPairType(int index) {
		return overlapmap.getMappingType(index);
	}
	
	//Get the names of the two models being merged
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

	public void removeManualCodewordMapping(int overlapindex) {
		overlapmap.removeOverlap(overlapindex);
		setChanged();
		notifyObservers(MergeEvent.MAPPINGREMOVED);
	}
	
	//Add manual codeword mapping to the list of equivalent terms
	public Pair<String,String> addManualCodewordMapping(String cdwd1, String cdwd2) {
		int index1 = getExposedCodewordIndexbyName(0, cdwd1);
		int index2 =getExposedCodewordIndexbyName(1, cdwd2);

		return addManualCodewordMapping(index1,index2);
	}
	
	
	//Add manual codeword mapping to the list of equivalent terms
	public Pair<String,String> addManualCodewordMapping(int cdwd1, int cdwd2) {
		Pair<Integer, Integer> minds = overlapmap.getModelIndicies();
		DataStructure ds1 = exposeddslist.get(minds.getLeft()).get(cdwd1);
		DataStructure ds2 = exposeddslist.get(minds.getRight()).get(cdwd2);
				
		if (codewordMappingExists(ds1, ds2)) return Pair.of(ds1.getName(),ds2.getName());
		addCodewordMapping(ds1, ds2, maptype.manualmapping);
		setChanged();
		notifyObservers(MergeEvent.mappingevent);
		return null;
	}
	
	private int getExposedCodewordIndexbyName(int modind, String cdwd) {
		for (DataStructure ds : exposeddslist.get(modind)) {
			if (ds.getName().equals(cdwd)) {
				return exposeddslist.get(modind).indexOf(ds);
			}
		}
		//If a datastructure with that name cannot be found, try searching with just the DS name
		String[] names = cdwd.split("\\.");
		String dsname = names[names.length-1];
		for (DataStructure ds : exposeddslist.get(modind)) {
			if (ds.getName().equals(dsname)) {
				return exposeddslist.get(modind).indexOf(ds);
			}
		}
		return -1;
	}

	public ArrayList<Pair<DataStructure,DataStructure>> getOverlapPairs() {
		return overlapmap.getDataStructurePairs();
	}
	
	public int getMappingCount() {
		return overlapmap.getMappingCount();
	}
	
	public int getSolutionDomainCount() {
		return overlapmap.getSolutionDomainCount();
	}
	
	public boolean hasSemanticOverlap() {
		return (overlapmap.getMappingCount()>0);
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
	
	//Get the models a modeloverlapmap describes
	private Pair<SemSimModel, SemSimModel> getModelOverlapMapModels(ModelOverlapMap map) {
		Pair<Integer, Integer> indexpair = map.getModelIndicies();
		return Pair.of(loadedmodels.get(indexpair.getLeft()),loadedmodels.get(indexpair.getRight()));
	}
	
	public ArrayList<Boolean> getUnitOverlaps() {
		return overlapmap.compareDataStructureUnits();
	}
	
	public String executeMerge(HashMap<String,String> dsnamemap, HashMap<String,String> smnamemap, ArrayList<ResolutionChoice> choices, 
			ArrayList<Pair<Double,String>> conversions, SemGenProgressBar bar) {
		Pair<SemSimModel, SemSimModel> models = getModelOverlapMapModels(overlapmap);

		if(models.getLeft().getSolutionDomains().size()>1 || models.getRight().getSolutionDomains().size()>1){
			return "One of the models to be merged has multiple solution domains.";
		}
		
		MergerTask task = new MergerTask(models, overlapmap, dsnamemap, smnamemap, choices, conversions, bar) {
			public void endTask() {
				mergedmodel = getMergedModel();
				setChanged();
				notifyObservers(MergeEvent.mergecompleted);
			}
		};
		task.execute();
		
		return null;
	}

	@Override
	public void setModelSaved(boolean val) {
		modelsaved = val;
	}

	@Override
	public String getCurrentModelName() {
		if (modelselection == -1) return null;
		return loadedmodels.get(modelselection).getName();
	}

	@Override
	public ModelAccessor getModelSourceLocation() {
		if (modelselection == -1) return null;
		return loadedmodels.get(modelselection).getLegacyCodeLocation();
	}

	@Override
	public ModelAccessor saveModel(Integer index) {
		if (modelaccessorlist.size() >= 3) {
			URI fileuri = modelaccessorlist.get(2).getFileThatContainsModelAsURI();
			
			if(fileuri != null){
				File file = new File(fileuri);
				SaveSemSimModel.writeToFile(mergedmodel,  modelaccessorlist.get(2), file, ModelType.SEMSIM_MODEL);
				setModelSaved(true);
				return  modelaccessorlist.get(2);
			}
		}
		return saveModelAs(index);		
	}

	@Override
	public ModelAccessor saveModelAs(Integer index) {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "proj", "cellml", "sbml"}, "owl");
		ModelAccessor ma = filec.SaveAsAction(mergedmodel);
		
		if (ma != null) {
			mergedmodel.setName(ma.getModelName());
			
			SaveSemSimModel.writeToFile(mergedmodel, ma, ma.getFileThatContainsModel(), filec.getFileFilter());
			if (modelaccessorlist.size() != 3) {
				modelaccessorlist.add(null);
			}
			modelaccessorlist.set(2, ma);
		}
		this.modelsaved = (ma!=null);
		return ma;
	}
	
	private void notifyModelListUpdated() {
		modelselection = -1;
		setChanged();
		notifyObservers(MergeEvent.modellistupdated);
	}
	
	public String getMergedModelName() {
		return mergedmodel.getName();
	}
	
	public String getModelName(int index) {
		return loadedmodels.get(index).getName();
	}
	
	public SemSimModel getMergedModel() {
		return mergedmodel;
	}
	

	//For populating the manual mapping panel, get all Data Structure names and add descriptions if available.
	public ArrayList<String> getExposedDSNamesandDescriptions(int index){
		ArrayList<String> namelist = new ArrayList<String>();
		for (DataStructure ds : exposeddslist.get(index)) {
			String desc = "(" + ds.getName() + ")";
			if(ds.getDescription()!=null) desc = ds.getDescription() + " " + desc;
			namelist.add(desc);
		}
		return namelist;
	}
	
	@Override
	public void update(Observable o, Object arg) {
	}
}
