package semgen.stage.stagetasks.extractor;

import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ExtractExclude extends Extractor {
	protected Set<DataStructure> dsstoremove = new HashSet<DataStructure>();
	protected Set<Submodel> smstoremove = new HashSet<Submodel>();
	protected boolean physioextraction = false;
	
	public ExtractExclude(SemSimModel source, SemSimModel extractionmodel) {
		super(source, extractionmodel);
	}
	

	public void addDataStructuretoRemove(DataStructure dstoremove) {
		dsstoremove.add(dstoremove);
	}
	
	public void addSubmodeltoRemove(Submodel smtoremove) {
		smstoremove.add(smtoremove);
		for (DataStructure smds : smtoremove.getAssociatedDataStructures()) {
			dsstoremove.add(smds);
		}
	}
	
	private void collectElementstoKeep() {
		Set<Submodel> smstokeep = new HashSet<Submodel>(sourcemodel.getSubmodels());
		
		for (Submodel smtoremove : smstoremove) {
			smstokeep.remove(smtoremove);
			for (DataStructure smds : smtoremove.getAssociatedDataStructures()) {
				dsstoremove.add(smds);
			}
		}
		
		for (Submodel smtokeep : smstokeep) {
			this.includeSubModel(smtokeep);
		}
		
		Set<DataStructure> dsstokeep = new HashSet<DataStructure>();
		
		for (DataStructure dstocheck : sourcemodel.getAssociatedDataStructures()) {
			if (dstocheck.hasAssociatedPhysicalComponent() || !physioextraction) {
				if (!dsstoremove.contains(dstocheck)) {
					dsstokeep.add(dstocheck);
				}
			}
			
		}
		
		for (DataStructure dstokeep : dsstokeep) {
			includeDependency(dstokeep);
		}		
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

	@Override
	public void addSubmodel(Submodel sourceobj) {
		smstoremove.add(sourceobj);
	}

	@Override
	public void addDataStructure(DataStructure sourceobj) {
			if (sourceobj instanceof MappableVariable) {
				if (((MappableVariable)sourceobj).getMappedFrom()!=null) {
					sourceobj = ((MappableVariable)sourceobj).getMappedFrom();
				}
			}
		dsstoremove.add(sourceobj);
	}

	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(pe)) {
			addDataStructure(dstoadd);
		}
		physioextraction = true;
	}

	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(proc)) {
			addDataStructure(dstoadd);
		}

//		for (PhysicalEntity participant : proc.getParticipants()) {
//			addEntity(participant);
//		}
		physioextraction = true;
	}

}
