package semgen.stage.stagetasks;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import semgen.stage.serialization.ModelNode;
import semgen.stage.serialization.StageState;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;

public abstract class StageTask<TSender extends SemGenWebBrowserCommandSender> extends Observable implements Observer {
	// Maps semsim model name to a semsim model
	protected TSender _commandSender;
	protected CommunicatingWebBrowserCommandReceiver _commandReceiver;
	
	protected StageState state;
	protected ArrayList<ModelInfo> _models  = new ArrayList<ModelInfo>();
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
	
	public ModelInfo getModel(int index) {
		return _models.get(index);
	}
	
	public StageTaskConf getNewTaskConfiguration() {
		return newtaskconf;
	}
	
	public void clearNewTaskConfiguration() {
		newtaskconf = null;
	} 
	
	public ArrayList<ModelNode> getModelNodes() {
		ArrayList<ModelNode> modelnodes = new ArrayList<ModelNode>();
		
		for (ModelInfo info : _models) {
			modelnodes.add(info.modelnode);
		}
		return modelnodes;
	}
	
	protected void configureTask(Task task, ArrayList<ModelInfo> info) {
		newtaskconf = new StageTaskConf(task, info);
		this.setChanged();
		this.notifyObservers(StageTaskEvent.NEWTASK);
	}

	protected void createMerger(Integer modind1, Integer modind2) {
		ArrayList<ModelInfo> mods = new ArrayList<ModelInfo>();
		
		mods.add(_models.get(modind1));
		mods.add(_models.get(modind2));
		
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

	public StageState getStageState() {
		return state;
	}
}
