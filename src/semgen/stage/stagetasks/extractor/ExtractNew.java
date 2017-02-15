package semgen.stage.stagetasks.extractor;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

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
		addSubmodel(sourceobj);
	}

	@Override
	public void addDataStructure(DataStructure sourceobj) {
		includeDependency(sourceobj);
	}

}
