package semgen.stage.stagetasks;

import java.util.ArrayList;

import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.StageTask.Task;
import semgen.stage.stagetasks.merge.MergerTask;

public class StageTaskConf {
	private ArrayList<ModelInfo> taskmodels;
	private StageState state = null;
	private Task tasktype;

	public StageTaskConf(Task type, ArrayList<ModelInfo> models) {
		tasktype = type;
		taskmodels = models;
		state = new StageState(type, models);
	}
	
	public StageTask<? extends SemGenWebBrowserCommandSender> createTask() {
		switch (tasktype) {
		case MERGER: 
			return new MergerTask(taskmodels, state);
		case EXTRACTOR:
			return null;
		case EDITOR:
			return null;
		default:
			return null;
		}
	}
	
	public StageState getState() {
		return state;
	}
}
