package semgen.stage.stagetasks.extractor;

import java.util.HashSet;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
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
		includeSubModel(sourceobj, new HashSet<Submodel>());
	}

	//Stage a datastructure for inclusion. If an CellML input node is passed get it's output node instead.
	@Override
	public void addDataStructure(DataStructure sourceobj) {
		if (sourceobj instanceof MappableVariable) {
			if (((MappableVariable)sourceobj).getMappedFrom()!=null) {
				sourceobj = ((MappableVariable)sourceobj).getMappedFrom();
			}
		}
		
		includeDependency(sourceobj);
	}

	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : sourcemodel.gatherDatastructuresWithPhysicalComponent(pe)) {
			includeDependency(dstoadd);
		}
	}

	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : sourcemodel.gatherDatastructuresWithPhysicalComponent(proc)) {
			includeDependency(dstoadd);
		}

		for (PhysicalEntity participant : proc.getParticipants()) {
			participant.addToModel(extraction);
		}
	}
}
