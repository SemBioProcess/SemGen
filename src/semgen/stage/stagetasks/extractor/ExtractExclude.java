package semgen.stage.stagetasks.extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ExtractExclude extends Extractor {
	protected Set<DataStructure> dsstoexclude = new HashSet<DataStructure>();
	protected Set<Submodel> smstoexclude = new HashSet<Submodel>();

	public ExtractExclude(SemSimModel source, SemSimModel extractionmodel) {
		super(source, extractionmodel);
	}
	
	@Override
	public SemSimModel run() {
		collectElementstoKeep();
		collectDataStructureInputs();
		replaceComputations();
		replaceSubmodelDataStructures();
		replaceSubmodels();
		buildExtraction();
		
		return extraction;
	}
	
	
	protected void collectElementstoKeep() {
		Set<Submodel> smstokeep = new HashSet<Submodel>(sourcemodel.getSubmodels());
		
		smstokeep = getSubmodelsToKeep(smstoexclude, smstokeep);
		
		// If no submodels were explicitly selected for inclusion or exclusion, exclude them all. 
		// Otherwise we end up keeping a bunch of data structures we don't need because of their
		// association with submodels. If some submodels were explicitly excluded, retain the 
		// correct ones.
		if(smstoexclude.size() > 0){ 
			for (Submodel smtokeep : smstokeep) includeSubModel(smtokeep, smstoexclude);
		}
		
		ArrayList<DataStructure> dsstokeep = sourcemodel.getAssociatedDataStructures();	
		for (DataStructure dstoexclude : dsstoexclude) {
			
			dsstokeep.remove(dstoexclude);
		
			for (DataStructure dstokeep : dsstokeep)
				dstokeep.removeOutput(dstoexclude);
		}
		
		for (DataStructure dstokeep : dsstokeep){
		
			// This statement excludes codewords that aren't declared as well as 
			// orphaned codewords that are not solution domains
			// and are not CellML-type MappableVariables. Not sure if this exclusion approach
			// will work for MappableVariables, too.
			if( ! dstokeep.isDeclared() || (dstokeep.getComputationInputs().isEmpty() 
					&& dstokeep.getUsedToCompute().isEmpty() 
					&& ! (dstokeep instanceof MappableVariable)
					&& ! dstokeep.isSolutionDomain()))
				continue; 
			
			includeDependency(dstokeep);
		}
	}
	
	
	// Iteratively determines which submodels should be included in extraction based on which have
	// been explicitly excluded
	private Set<Submodel> getSubmodelsToKeep(Set<Submodel> submodelstoexclude, Set<Submodel> runningsmstokeep){
	
		for (Submodel smtoexclude : submodelstoexclude) {
		
			runningsmstokeep.remove(smtoexclude);
			
			for (DataStructure smds : smtoexclude.getAssociatedDataStructures()) dsstoexclude.add(smds);
			
			runningsmstokeep = getSubmodelsToKeep(new HashSet<Submodel>(smtoexclude.getSubmodels()),runningsmstokeep); // iterate through sub-submodels
		}
		return runningsmstokeep;
	}
	

	@Override
	public void addSubmodel(Submodel sourceobj) {
		smstoexclude.add(sourceobj);
	}

	@Override
	public void addDataStructure(DataStructure sourceobj) {
		
		System.out.println("HERE in addDataStructure of ExtractExclude" + sourceobj.getName());
		if (sourceobj instanceof MappableVariable) {
			if (((MappableVariable)sourceobj).getMappedFrom()!=null) {
				sourceobj = ((MappableVariable)sourceobj).getMappedFrom();
			}
		}
		dsstoexclude.add(sourceobj);
	}

	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : sourcemodel.gatherDatastructuresWithPhysicalComponent(pe)) {
			addDataStructure(dstoadd);
		}
	}

	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : sourcemodel.gatherDatastructuresWithPhysicalComponent(proc)) {
			addDataStructure(dstoadd);
		}

//		for (PhysicalEntity participant : proc.getParticipants()) {
//			addEntity(participant);
//		}
	}

}
