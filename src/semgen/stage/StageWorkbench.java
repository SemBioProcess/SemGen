package semgen.stage;

import java.util.ArrayList;
import java.util.Observable;

import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.ProjectTask;
import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;
import semgen.stage.stagetasks.StageTask;
import semgen.stage.stagetasks.StageTask.StageTaskEvent;
import semgen.stage.stagetasks.StageTask.Task;
import semgen.utilities.Workbench;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semgen.visualizations.WebBrowserCommandSenderGenerator;
import semsim.reading.ModelAccessor;

public class StageWorkbench extends Workbench {
	public enum StageEvent {CHANGETASK}
	
	private ArrayList<StageTask<? extends SemGenWebBrowserCommandSender>> tasks = new ArrayList<StageTask<? extends SemGenWebBrowserCommandSender>>();
	private StageTask<? extends SemGenWebBrowserCommandSender> activetask;	
	
	public StageWorkbench() {}
	
	@Override
	public void initialize() {
		ProjectTask projtask = new ProjectTask();
		projtask.addObserver(this);
		tasks.add(projtask);
		setActiveTask(0);
	}

	/**
	 * Get an object that listens for javascript commands
	 * @return
	 */
	public CommunicatingWebBrowserCommandReceiver getCommandReceiver() {
		return activetask.getCommandReceiver();
	}

	/**
	 * Get an object that sends javascript commands
	 * @return
	 */
	public Class<? extends SemGenWebBrowserCommandSender> getCommandSenderInterface() {
		return activetask.getSenderInterface();
	}

	/**
	 * Sets the object used to send commands to the view
	 * @param commandSender Object used to send commands to the view
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setCommandSender(WebBrowserCommandSenderGenerator<?> commandSender) {
		new CommandInterfaceSetter(commandSender, activetask);
	}
	
	private void setActiveTask(int task) {
		activetask = tasks.get(task);
		if (task==0) {
			for (StageTask<?> stagetask : tasks) {
				if (stagetask!= null) {
					activetask.addModelstoTask(stagetask.getQueuedModels());
				}
			}
		}
		this.setChanged();
		this.notifyObservers(StageEvent.CHANGETASK);
	}

	private void createTask() {
		StageTask<?> newtask = activetask.getNewTaskConfiguration().createTask(tasks.size());
		newtask.addObserver(this);
		tasks.add(newtask);
		activetask.clearNewTaskConfiguration();	
		
		this.setActiveTask(tasks.size()-1);

	}

	@Override
	public ModelAccessor saveModel() {
		return null;
	}

	@Override
	public ModelAccessor saveModelAs() {
		return null;
	}

	@Override
	public void setModelSaved(boolean val) {
		
	}

	@Override
	public String getCurrentModelName() {
		return null;
	}

	@Override
	public ModelAccessor getModelSourceLocation() {
		return null;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == StageTaskEvent.NEWTASK) {
			createTask();
		}
		if (arg1 == StageTaskEvent.SWITCHTASK) {
			setActiveTask(activetask.getIndexofTasktoLoad());
		}
		if (arg1 == StageTaskEvent.CLOSETASK) {
			closeTask();
		}
	}
	
	private class CommandInterfaceSetter<T extends SemGenWebBrowserCommandSender> {
		public CommandInterfaceSetter(WebBrowserCommandSenderGenerator<T> setter, StageTask<T> task) {
			task.setCommandSender(setter.getSender());
		}
	}
	
	public Task getTaskType() {
		return activetask.getTaskType();
	}
	
	public StageState getActiveStageState() {		
		return activetask.getStageState();		
	}
	
	public void closeTask() {
		tasks.set(activetask.getTaskIndex(), null);
		
		//Remove task icon
		setChanged();
		this.notifyObservers(StageTaskEvent.CLOSETASK);
		activetask.deleteObservers();
		//set active task
		this.setActiveTask(0);
	}

	public int getActiveTaskIndex() {
		return activetask.getTaskIndex();
	}
}
