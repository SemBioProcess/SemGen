package semgen.stage.serialization;

import java.util.ArrayList;

import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

/**
 * Represents a submodel node in a d3 graph
 * 
 * @author Ryan
 *
 */
public class SubModelNode extends Node {
	public ArrayList<SubModelNode> childsubmodels = new ArrayList<SubModelNode>();
	public ArrayList<DependencyNode> dependencies;
	//Count of how many dependencies of each type are childrent of this submodel.
	public int[] deptypecounts = {0, 0 ,0};
	
	public SubModelNode(Submodel subModel, String parentModelName) {
		super(subModel.getName(), parentModelName);

		dependencies = new ArrayList<DependencyNode>();
		
		// SemSimModelSerializer.getDependencyNetwork(subModel);
		for(DataStructure dependency : subModel.getAssociatedDataStructures()) {
			SubModelDependencyNode sdn = new SubModelDependencyNode(dependency, this);
			dependencies.add(sdn);
			incrementType(sdn.typeIndex);		
		}
	}
	
	public SubModelNode(Submodel subModel) {
		super(subModel.getName());

		dependencies = new ArrayList<DependencyNode>();
		if (!subModel.getSubmodels().isEmpty()) {
			for (Submodel sm : subModel.getSubmodels()) {
				childsubmodels.add(new SubModelNode(sm));
			}
		}
		else {
			loadDataStructuresfromCenteredSubmodel(subModel);
		}

	}

	
	private void loadDataStructuresfromCenteredSubmodel(Submodel subModel) {
		for(DataStructure dependency : subModel.getAssociatedDataStructures()) {
			SubModelDependencyNode sdn = new SubModelDependencyNode(dependency);
			dependencies.add(sdn);
			incrementType(sdn.typeIndex);		
		}
	}
	
	private void incrementType(Number type) {
		deptypecounts[(int) type]++;
	}

}
