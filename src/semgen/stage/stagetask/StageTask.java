package semgen.stage.stagetask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semgen.visualizations.SemGenWebBrowserCommandSender;
import semsim.model.collection.SemSimModel;

public abstract class StageTask {
	// Maps semsim model name to a semsim model
	protected SemGenWebBrowserCommandSender _commandSender;
	protected CommunicatingWebBrowserCommandReceiver _commandReceiver;
	protected Map<String, ModelInfo> _models  = new HashMap<String, ModelInfo>();

	public StageTask(SemGenWebBrowserCommandSender sender) {
		_commandSender = sender;
	}
	
	public CommunicatingWebBrowserCommandReceiver getCommandReceiver() {
		return _commandReceiver;
	}
	
	protected class ModelInfo {
		public SemSimModel Model;
		public File Path;
		
		public ModelInfo(SemSimModel model, File path) {
			Model = model;
			Path = path;
		}
	}
}
