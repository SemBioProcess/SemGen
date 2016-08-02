package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import semgen.SemGen;
import semsim.model.collection.SemSimCollection;
import semsim.model.computational.datastructures.DataStructure;

/**
 * Represents a dependency node in the d3 graph
 * 
 * @author Ryan
 *
 */
public class DependencyNode extends LinkableNode<DataStructure> {	
	
	@Expose public String submodelId = "";
	@Expose public boolean issubmodelinput;
	
	public DependencyNode(DataStructure dataStructure, Node<? extends SemSimCollection> parent) {
		super(dataStructure, parent);
		String nodeType = dataStructure.getPropertyType(SemGen.semsimlib).toString();
		this.typeIndex = nodetypes.indexOf(nodeType);
		issubmodelinput = dataStructure.isFunctionalSubmodelInput();
	}
	
	//Copy with same parent
	DependencyNode(DependencyNode original) {
		super(original);
		submodelId = new String(submodelId);
		issubmodelinput = original.issubmodelinput;
	}
	//Copy with new parent
	public DependencyNode(DependencyNode original, Node<? extends SemSimCollection> parent) {
		super(original, parent);
		submodelId = new String(submodelId);
		issubmodelinput = original.issubmodelinput;
	}
	
	public void setInputs(HashMap<DataStructure, DependencyNode> dsnodemap) {
		inputs = new ArrayList<Link>();
		if (sourceobj.hasComputation()) {
			for (DataStructure input : sourceobj.getComputationInputs()) {
				if (sourceobj!=input) {
					inputs.add(new Link(this, dsnodemap.get(input)));
				}
			}
		}
	}
}