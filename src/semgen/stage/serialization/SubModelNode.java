package semgen.stage.serialization;

import semsim.model.collection.SemSimCollection;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

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
	
	public SubModelNode(Submodel subModel) {
		super(subModel);

			loadDataStructuresfromCenteredSubmodel(subModel);
			typeIndex=SUBMODEL;
	}
	

	

	
	private void loadDataStructuresfromCenteredSubmodel(Submodel subModel) {
		for(DataStructure dependency : subModel.getAssociatedDataStructures()) {
			   
		}
	}


}
