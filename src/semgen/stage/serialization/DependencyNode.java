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
public class DependencyNode extends Node<DataStructure> {	
	
	@Expose public String submodelId = "";
	@Expose public ArrayList<Link> inputs = new ArrayList<Link>();
	@Expose public boolean issubmodelinput;
	
	public DependencyNode(DataStructure dataStructure, Node<? extends SemSimCollection> parent) {
		super(dataStructure, parent);

		String nodeType = dataStructure.getPropertyType(SemGen.semsimlib).toString();
		this.typeIndex = nodetypes.indexOf(nodeType);
		issubmodelinput = dataStructure.isFunctionalSubmodelInput();
	}
	
	public void setInputs(HashMap<DataStructure, DependencyNode> dsnodemap) {
		if (sourceobj.hasComputation()) {
			for (DataStructure input : sourceobj.getComputationInputs()) {
				if (sourceobj!=input) {
					inputs.add(new Link(this, dsnodemap.get(input)));
				}
			}
		}
	}
	
	public void showParentSubModelName(boolean show) {
		if (this.parent!= this.getFirstAncestor()) {
			if (show) {
				name = parent.name + '.'  + sourceobj.getName();
			}
			else {
				name = sourceobj.getName();
			}
		}
	}
}