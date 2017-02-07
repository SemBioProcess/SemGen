package semgen.stage.stagetasks.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public abstract class Extractor {
	protected SemSimModel sourcemodel;
	protected SemSimModel extraction;
	protected HashMap<Submodel,Submodel> submodels = new HashMap<Submodel, Submodel>();
	protected HashMap<DataStructure,DataStructure> datastructures = new HashMap<DataStructure,DataStructure>();

	public Extractor(SemSimModel source, SemSimModel extractionmodel) {
		sourcemodel = source;
		extraction = extractionmodel;
	}
	
	public abstract SemSimModel run();
	
	protected void collectDataStructureInputs() {
		Set<DataStructure> smdatastructures = new HashSet<DataStructure>(this.datastructures.keySet());
		smdatastructures.addAll(sourcemodel.getSolutionDomains());
		for (DataStructure smds : smdatastructures) {
			for (DataStructure input : smds.getComputationInputs()) {
				if (!smdatastructures.contains(input)) {
					DataStructure newinput = input.copy();
					newinput.clearInputs();
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
		extraction.addSubmodels(submodels.values());
		
	}
	
	public void addSubModel(Submodel sourceobj) {
		submodels.put(sourceobj, new Submodel(sourceobj));
		for (Submodel submodel : sourceobj.getSubmodels()) {
			this.addSubModel(submodel);
		}
		for (DataStructure ds : sourceobj.getAssociatedDataStructures()) {
			addDependency(ds);
		}
	}

	public void addDependency(DataStructure sourceobj) {
		datastructures.put(sourceobj, sourceobj.copy());
	}
}
