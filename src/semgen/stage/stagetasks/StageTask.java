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

	public enum Task {PROJECT, MERGER, EXTRACTOR, EDITOR};
	
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

	protected void switchTask() {
		this.setChanged();
		this.notifyObservers(StageTaskEvent.SWITCHTASK);
	}
	
	public abstract Task getTaskType();
	public abstract Class<TSender> getSenderInterface();
}
