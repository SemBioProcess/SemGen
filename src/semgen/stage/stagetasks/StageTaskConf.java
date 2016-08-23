package semgen.stage.stagetasks;

import java.util.ArrayList;

import semgen.stage.stagetasks.StageTask.Task;
import semgen.stage.stagetasks.merge.MergerTask;

public class StageTaskConf {
	private ArrayList<ModelInfo> taskmodels;
	private Task tasktype;

	public StageTaskConf(Task type, ArrayList<ModelInfo> models) {
		tasktype = type;
		taskmodels = models;
	}
	
	public StageTask<? extends SemGenWebBrowserCommandSender> createTask(int taskindex) {
		switch (tasktype) {
		case MERGER: 
			return new MergerTask(taskmodels, taskindex);
		case EXTRACTOR:
			return null;
		case EDITOR:
			return null;
		default:
			return null;
		}
	}
}
