package semgen.stage.serialization;

import semgen.stage.stagetasks.extractor.Extractor;
import semsim.model.collection.SemSimCollection;
import semsim.model.collection.Submodel;

/**
 * Represents a submodel node in a d3 graph
 * 
 * @author Ryan
 *
 */
public class SubModelNode extends ParentNode<Submodel> {
	//Count of how many dependencies of each type are childrent of this submodel.
	
	public SubModelNode(Submodel subModel, Node<? extends SemSimCollection> parent) {
		super(subModel, parent);
		
		typeIndex=SUBMODEL;
	}
	
	
	public SubModelNode(String name) {
		super(name, SUBMODEL);
	}


	@Override
	public void collectforExtraction(Extractor extractor) {
		extractor.addSubmodel(sourceobj);
	}

}
