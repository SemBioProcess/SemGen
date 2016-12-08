package semgen.stage.serialization;

import semsim.model.collection.SemSimModel;

public class ExtractionNode extends ParentNode<SemSimModel> { 

	protected ExtractionNode(String name) {
		super(name, EXTRACTION);
		
	}


}
