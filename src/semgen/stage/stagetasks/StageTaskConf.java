package semgen.stage.stagetasks;

import java.util.ArrayList;

import semgen.stage.stagetasks.StageTask.Task;
import semgen.stage.stagetasks.merge.StageMergerTask;

public class StageTaskConf {
	private ArrayList<StageRootInfo<?>> taskmodels;
	private Task tasktype;

	public StageTaskConf(Task type, ArrayList<StageRootInfo<?>> models) {
		tasktype = type;
		taskmodels = models;
	}
	
	public StageTask<? extends SemGenWebBrowserCommandSender> createTask(int taskindex) {
		switch (tasktype) {
		case MERGER: 
			return new StageMergerTask(taskmodels, taskindex);
		case EDITOR:
			return null;
		default:
			return null;
		}
	}
}
