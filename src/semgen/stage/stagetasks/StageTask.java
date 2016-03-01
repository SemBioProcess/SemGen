package semgen.stage.stagetasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;

public abstract class StageTask<TSender extends SemGenWebBrowserCommandSender> extends Observable implements Observer {
	// Maps semsim model name to a semsim model
	protected TSender _commandSender;
	protected CommunicatingWebBrowserCommandReceiver _commandReceiver;
	protected Map<String, ModelInfo> _models  = new HashMap<String, ModelInfo>();
	protected StageTaskConf newtaskconf = null;
	private int existingtaskindex = 0;

	public enum Task {
		PROJECT("proj"), 
		MERGER("merge"), 
		EXTRACTOR("extr"), 
		EDITOR("edit");
		
		public String jsid;
		Task(String id) {
			jsid = id;
		}
	};
	
	public enum StageTaskEvent {SWITCHTASK, NEWTASK};
	
	public StageTask() {}
	
	public CommunicatingWebBrowserCommandReceiver getCommandReceiver() {
		return _commandReceiver;
	}
	
	public void setCommandSender(TSender sender) {
		_commandSender = sender;
	}
	
	public ModelInfo getModel(String name) {
		return _models.get(name);
	}
	
	public StageTaskConf getNewTaskConfiguration() {
		return newtaskconf;
	}
	
	public void clearNewTaskConfiguration() {
		newtaskconf = null;
	} 
	
	protected void configureTask(Task task, ArrayList<ModelInfo> info) {
		newtaskconf = new StageTaskConf(task, info);
		this.setChanged();
		this.notifyObservers(StageTaskEvent.NEWTASK);
	}

	protected void createMerger(String modelnames) {
		ArrayList<ModelInfo> mods = new ArrayList<ModelInfo>();
		
		for (String name : modelnames.split(",")) {
		// If the models don't exist throw an exception
			if(!_models.containsKey(name))
				throw new IllegalArgumentException(name);
			
			mods.add(_models.get(name));
		}
		configureTask(Task.MERGER, mods);
	}
	
	protected void switchTask(int task) {
		existingtaskindex = task;
		this.setChanged();
		this.notifyObservers(StageTaskEvent.SWITCHTASK);
	}
	
	public abstract Task getTaskType();
	public abstract Class<TSender> getSenderInterface();
	
	public int getIndexofTasktoLoad() {
		return existingtaskindex;
	}
}
