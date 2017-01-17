package semgen.stage.stagetasks;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;
import semgen.stage.serialization.ModelNode;
import semgen.stage.serialization.Node;
import semgen.stage.serialization.StageState;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;

public abstract class StageTask<TSender extends SemGenWebBrowserCommandSender> extends Observable implements Observer {
	// Maps semsim model name to a semsim model
	protected TSender _commandSender;
	protected CommunicatingWebBrowserCommandReceiver _commandReceiver;
	
	protected StageState state;
	protected ArrayList<ModelInfo> _models  = new ArrayList<ModelInfo>();
	protected ArrayList<ModelInfo> stagemodelqueue = new ArrayList<ModelInfo>();
	protected JSObject jstask;
	protected StageTaskConf newtaskconf = null;
	protected int taskindex;
	private int existingtaskindex = 0;

	public enum Task {
		PROJECT("proj"), 
		MERGER("merge"), 
		EXTRACTOR("extract"), 
		EDITOR("edit");
		
		public String jsid;
		Task(String id) {
			jsid = id;
		}
	};
	
	public enum StageTaskEvent {SWITCHTASK, NEWTASK, CLOSETASK, CLOSEPROJECT};
	
	public StageTask(int index) {
		taskindex = index;
	}
	
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
	public ArrayList<ModelInfo> getQueuedModels() {
		ArrayList<ModelInfo> queue = new ArrayList<ModelInfo>(stagemodelqueue);
		stagemodelqueue.clear();
		
		return queue;
	}
	
	public void addModeltoTask(ModelInfo newmodel) {
		_models.add(newmodel);
		state.updateModelNodes(_models);
	}
	
	public void addModelstoTask(ArrayList<ModelInfo> newmodels) {
		_models.addAll(newmodels);
		state.updateModelNodes(_models);
	}
	
	protected void removeModel(Integer index) {
		_models.set(index, null);
		state.updateModelNodes(_models);
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
	
	protected void createExtractor(Integer modindex) {
		ArrayList<ModelInfo> mods = new ArrayList<ModelInfo>();
		
		mods.add(_models.get(modindex));
		configureTask(Task.EXTRACTOR, mods);
	}
	
	protected void switchTask(int task) {
		existingtaskindex = task;
		this.setChanged();
		this.notifyObservers(StageTaskEvent.SWITCHTASK);
	}
	
	public abstract Task getTaskType();
	public abstract Class<TSender> getSenderInterface();
	
	public void closeTask() {
		setChanged();
		this.notifyObservers(StageTaskEvent.CLOSETASK);
		
	}
	
	public int getIndexofTasktoLoad() {
		return existingtaskindex;
	}

	public StageState getStageState() {
		return state;
	}
	
	public int getTaskIndex() {
		return taskindex;
	}
	
	public void setTaskIndex(int newindex) {
		taskindex = newindex;
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getNodebyHash(int nodehash, String nodeid) {
		for (ModelInfo mni : _models) {
			Node<?> returnnode = mni.modelnode.getNodebyHash(nodehash, nodeid);
			if (returnnode!=null) return returnnode; 
		}
		return null;
	}
	
	//Convert Javascript Node objects to Java Node objects
	public ArrayList<Node<?>> convertJSStageNodestoJava(JSArray nodearray) {
		ArrayList<Node<?>> javanodes = new ArrayList<Node<?>>();
		for (int i = 0; i < nodearray.length(); i++) {
			JSObject val = nodearray.get(i).asObject();
			javanodes.add(getNodebyHash(val.getProperty("hash").asNumber().getInteger(), val.getProperty("id").getStringValue()));
		}
		return javanodes;
	}
	
	public class NodeTreeBridge {
		void setNodeLocation(Object jsobject, Integer xloc, Integer yloc) {
			Node<?> node = (Node<?>)jsobject;
			node.xpos = xloc;
		}
	}
}
