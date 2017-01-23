package semgen.stage.stagetasks.extractor;

import semsim.model.collection.SemSimModel;

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

}
