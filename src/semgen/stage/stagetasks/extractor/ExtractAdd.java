package semgen.stage.stagetasks.extractor;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ExtractAdd extends Extractor {
	private SemSimModel receivermodel;
	
	public ExtractAdd(SemSimModel source, SemSimModel target) {
		super(source);
		receivermodel = target.clone();
		extraction.setName(receivermodel.getName());
	}

	public void addDataStructure(DataStructure dstoimport) {
		if (dstoimport instanceof MappableVariable) {
			if (((MappableVariable)dstoimport).getMappedFrom()!=null) {
				dstoimport = ((MappableVariable)dstoimport).getMappedFrom();
			}
		}
		includeDependency(dstoimport);
	}
	
	@Override
	public void addSubmodel(Submodel sourceobj) {
		includeSubModel(sourceobj);
	}
	
	private void collectStructures() {
		for (Submodel sm : receivermodel.getSubmodels()) {
			this.includeSubModel(sourcemodel.getSubmodel(sm.getName()));
		}
		for (DataStructure ds : receivermodel.getAssociatedDataStructures()) {
			if (!ds.isExternal()) {
				DataStructure existingds = sourcemodel.getAssociatedDataStructure(ds.getName());
				this.includeDependency(existingds);
			}
		}
	}
	
	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(pe)) {
			addDataStructure(dstoadd);
		}
	}
	
	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(proc)) {
			addDataStructure(dstoadd);
		}

		for (PhysicalEntity participant : proc.getParticipants()) {
			addEntity(participant);
		}
	}
	
	@Override
	public SemSimModel run() {
		collectStructures();
		collectDataStructureInputs();
		replaceComputations();
		replaceSubmodelDataStructures();
		replaceSubmodels();
		buildExtraction();
		
		return extraction;
	}


}
