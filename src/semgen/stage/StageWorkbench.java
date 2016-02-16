package semgen.stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;

import semgen.stage.stagetask.ProjectTask;
import semgen.stage.stagetask.StageTask;
import semgen.utilities.Workbench;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semgen.visualizations.SemGenWebBrowserCommandSender;

public class StageWorkbench extends Workbench {
	private ProjectTask projtask;
	private ArrayList<StageTask> tasks = new ArrayList<StageTask>();
	
	// Used to send commands to the view
	private SemGenWebBrowserCommandSender _commandSender = null;
	private CommunicatingWebBrowserCommandReceiver _commandReceiver;
	
	public StageWorkbench() {}
	
	/**
	 * Get an object that listens for javascript commands
	 * @return
	 */
	public CommunicatingWebBrowserCommandReceiver getCommandReceiver() {
		return _commandReceiver;
	}
	
	/**
	 * Sets the object used to send commands to the view
	 * @param commandSender Object used to send commands to the view
	 */
	public void setCommandSender(SemGenWebBrowserCommandSender commandSender) {
		_commandSender = commandSender;
	}
	
	@Override
	public void initialize() {
		projtask = new ProjectTask(_commandSender);
		_commandReceiver = projtask.getCommandReceiver();
	}

	@Override
	public File saveModel() {
		return null;
	}

	@Override
	public File saveModelAs() {
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
	public String getModelSourceFile() {
		return null;
	}


	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
}
