//Falilitator object for saving the stage state in Java.

package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.Set;

import semgen.stage.stagetasks.ModelInfo;
import semgen.stage.stagetasks.StageTask.Task;

import com.teamdev.jxbrowser.chromium.JSObject;

public class StageState {
	public String tasktype;
	public ArrayList<NodeBranch> nodetree;
	
	public StageState(Task type) {
		tasktype = type.jsid;

	}
	
	public StageState(Task task, ArrayList<ModelInfo> models) {
		this.tasktype = task.jsid;
		ArrayList<NodeBranch> root = new ArrayList<NodeBranch>();
		for (ModelInfo info : models) {
			root.add(new NodeBranch(new ModelNode(info.getModelName())));
		}
		nodetree = root;
	}
	
	public StageState(JSObject state, Set<String> modelnames) {

		
	}
	
	private class NodeBranch {
		public Node branchroot;
		public NodeBranch[] children = {};
		
		public NodeBranch(Node root) {
			branchroot = root;
		}
	}
	
	private class ModelNode extends Node {
		public ModelNode(String name) {
			super(name);
		}
		
	}
}
