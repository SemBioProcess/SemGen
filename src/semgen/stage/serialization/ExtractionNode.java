package semgen.stage.serialization;

import semsim.model.collection.SemSimModel;

public class ExtractionNode extends ModelNode { 

	public ExtractionNode(SemSimModel srcextract, int extractionindex) {
		super(srcextract, extractionindex);
		typeIndex = EXTRACTION;
	}

	
}
