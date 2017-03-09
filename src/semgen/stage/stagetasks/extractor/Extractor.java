package semgen.stage.stagetasks.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;

public abstract class Extractor {
	protected SemSimModel sourcemodel;
	protected SemSimModel extraction;
	protected HashMap<Submodel,Submodel> submodels = new HashMap<Submodel, Submodel>();
	protected HashMap<DataStructure,DataStructure> datastructures = new HashMap<DataStructure,DataStructure>();

	public Extractor(SemSimModel source, SemSimModel extractionmodel) {
		sourcemodel = source;
		extraction = extractionmodel;
		includeSolutionDomains();
	}
	
	public Extractor(SemSimModel source) {
		sourcemodel = source.clone();
		extraction = new SemSimModel();
		includeSolutionDomains();
	}
	
	private void includeSolutionDomains() {
		for (DataStructure sdom : sourcemodel.getSolutionDomains()) {
			this.includeDependency(sdom);
		}
	}
	
	public abstract SemSimModel run();
	
	protected void collectDataStructureInputs() {
		Set<DataStructure> smdatastructures = new HashSet<DataStructure>(this.datastructures.keySet());
		for (DataStructure smds : smdatastructures) {
			for (DataStructure input : smds.getComputationInputs()) {
				if (!datastructures.keySet().contains(input)) {	
//					DataStructure existing = null;
//					for (DataStructure key : datastructures.keySet()) {
//						if (key.getName().equalsIgnoreCase(input.getName())) {
//							existing = datastructures.get(key);
//							break;
//						}
//					}
//					if (existing != null) {
//						datastructures.put(input, existing);
//						return;
//					}
					DataStructure newinput = input.copy();
					//Retain inputs which are constants
					if (!newinput.getComputationInputs().isEmpty()) {
						newinput.clearInputs();
						newinput.setExternal(true);
					}
					datastructures.put(input, newinput);
				}
			}	
		}
	}
	
	protected void replaceComputations() {
		for (DataStructure dscopy : datastructures.values()) {
			dscopy.replaceAllDataStructures(datastructures);
		}
	}
	
	protected void replaceSubmodelDataStructures() {
		for (Submodel smcopy : submodels.values()) {
			smcopy.replaceDataStructures(datastructures);
		}
	}
	
	protected void replaceSubmodels() {
		for (Submodel smcopy : submodels.values()) {
			smcopy.replaceSubmodels(submodels);
		}
	}
	
	protected void buildExtraction() {
		for (DataStructure dstoadd : datastructures.values()) {
			dstoadd.addToModel(extraction);			
		}
		extraction.setSubmodels(submodels.values());
	}
	
	protected void includeSubModel(Submodel sourceobj) {
		submodels.put(sourceobj, sourceobj.clone());
		for (Submodel submodel : sourceobj.getSubmodels()) {
			this.includeSubModel(submodel);
		}
		for (DataStructure ds : sourceobj.getAssociatedDataStructures()) {
			if (!ds.isExternal()) {
				includeDependency(ds);
			}
			
		}
	}
	
	protected HashSet<DataStructure> gatherDatastructureswithPhysicalComponent(PhysicalModelComponent pmc) {
		HashSet<DataStructure> dsswithpmc = new HashSet<DataStructure>();
		
		for (DataStructure ds : sourcemodel.getAssociatedDataStructures()) {
			if (ds.getAssociatedPhysicalModelComponent()==null) continue;
			if (ds.getAssociatedPhysicalModelComponent().equals(pmc)) {
				dsswithpmc.add(ds);
			}
		}
		
		return dsswithpmc;
	}
	
	protected void includeDependency(DataStructure sourceobj) {
		if (!this.datastructures.containsKey(sourceobj)) {
//			DataStructure existing = null;
//			for (DataStructure key : datastructures.keySet()) {
//				if (key.getName().equalsIgnoreCase(sourceobj.getName())) {
//					existing = datastructures.get(key);
//					break;
//				}
//			}
//			if (existing != null) {
//				datastructures.put(sourceobj, existing);
//				return;
//			}
			datastructures.put(sourceobj, sourceobj.copy());
		}
	}
	
	public abstract void addEntity(PhysicalEntity pe);

	public abstract void addProcess(PhysicalProcess proc);
	
	public abstract void addSubmodel(Submodel sourceobj);
	public abstract void addDataStructure(DataStructure sourceobj);
		

}
