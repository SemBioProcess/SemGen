package semgen.stage.stagetasks.extractor;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ExtractNew extends Extractor {

	public ExtractNew(SemSimModel source, SemSimModel extractionmodel) {
		super(source, extractionmodel);

	}

	@Override
	public SemSimModel run() {
		collectDataStructureInputs();
		replaceComputations();
		replaceSubmodelDataStructures();
		replaceSubmodels();
		buildExtraction();
		return extraction;
	}

	@Override
	public void addSubmodel(Submodel sourceobj) {
		includeSubModel(sourceobj);
	}

	@Override
	public void addDataStructure(DataStructure sourceobj) {
		includeDependency(sourceobj);
	}

	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(pe)) {
			includeDependency(dstoadd);
		}
	}

	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(proc)) {
			includeDependency(dstoadd);
		}

		for (PhysicalEntity participant : proc.getParticipants()) {
			addEntity(participant);
		}
	}

}
