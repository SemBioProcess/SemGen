package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

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
	@Expose public ArrayList<SubModelNode> childsubmodels = new ArrayList<SubModelNode>();
	@Expose public ArrayList<DependencyNode> dependencies = new ArrayList<DependencyNode>();
	//Count of how many dependencies of each type are childrent of this submodel.
	@Expose public int[] deptypecounts = {0, 0 ,0};
	
	public SubModelNode(Submodel subModel, Node<? extends SemSimCollection> parent) {
		super(subModel, parent);
		

	}
	
	public SubModelNode(Submodel subModel) {
		super(subModel);

		
			loadDataStructuresfromCenteredSubmodel(subModel);

	}
	
	public HashMap<DataStructure, DependencyNode> serialize() {
		HashMap<DataStructure, DependencyNode> depmap = new HashMap<DataStructure, DependencyNode>();
		if (!sourceobj.getSubmodels().isEmpty()) {
			for (Submodel sm : sourceobj.getSubmodels()) {
				SubModelNode smn = new SubModelNode(sm, this);
				depmap.putAll(smn.serialize());
				childsubmodels.add(smn);
			}
		}
		for(DataStructure dependency : sourceobj.getAssociatedDataStructures()) {
			DependencyNode sdn = new DependencyNode(dependency, this);
			depmap.put(dependency, sdn);
			dependencies.add(sdn);
			incrementType(sdn.typeIndex);
		}
		
		return depmap;
	}
	

	
	private void loadDataStructuresfromCenteredSubmodel(Submodel subModel) {
		for(DataStructure dependency : subModel.getAssociatedDataStructures()) {
			   
		}
	}
	
	private void incrementType(Number type) {
		deptypecounts[(int) type]++;
	}

}
