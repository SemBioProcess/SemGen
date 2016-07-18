package semgen.stage.serialization;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semgen.stage.stagetasks.ModelInfo;
import semgen.stage.stagetasks.StageTask.Task;

public class StageState {
	@Expose public String tasktype;
	@Expose public ArrayList<ModelNode> models = new ArrayList<ModelNode>();;
	
	public StageState(Task type) {
		tasktype = type.jsid;

	}
	
	public StageState(Task task, ArrayList<ModelInfo> modelstruct) {
		this.tasktype = task.jsid;
		for (ModelInfo info : modelstruct) {
			models.add(info.modelnode);
		}
	}

}
