package semgen.stage.serialization;

import java.util.ArrayList;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;

/**
 * Represents a submodel node in a d3 graph
 * 
 * @author Ryan
 *
 */
public class SubModelNode extends Node {
	public ArrayList<DependencyNode> dependencies;
	
	public SubModelNode(Submodel subModel) {
		super(subModel.getName());

		dependencies = new ArrayList<DependencyNode>();
		for(DataStructure dependency : subModel.getAssociatedDataStructures()) {
			dependencies.add(new DependencyNode(dependency));
		}
	}

}
