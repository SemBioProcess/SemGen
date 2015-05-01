package semgen.stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.serialization.PhysioMapNode;
import semgen.stage.serialization.SemSimModelSerializer;
import semgen.stage.serialization.SubModelNode;
import semgen.utilities.Workbench;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semgen.visualizations.JsonString;
import semgen.visualizations.SemGenWebBrowserCommandSender;
import semsim.model.SemSimModel;

public class StageWorkbench extends Workbench {

	// Maps semsim model name to a semsim model
	private Map<String, ModelInfo> _models;
	
	// Used to send commands to the view
	private SemGenWebBrowserCommandSender _commandSender;
	
	public StageWorkbench() {
		_models = new HashMap<String, ModelInfo>();
	}
	
	/**
	 * Get an object that listens for javascript commands
	 * @return
	 */
	public CommunicatingWebBrowserCommandReceiver getCommandReceiver() {
		return new StageCommandReceiver();
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
		// TODO Auto-generated method stub

	}

	@Override
	public File saveModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveModelAs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setModelSaved(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCurrentModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModelSourceFile() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class ModelInfo {
		public SemSimModel Model;
		public File Path;
		
		public ModelInfo(SemSimModel model, File path) {
			Model = model;
			Path = path;
		}
	}

	/**
	 * Receives commands from javascript
	 * @author Ryan
	 *
	 */
	public class StageCommandReceiver extends CommunicatingWebBrowserCommandReceiver {

		public static final boolean ShowJavascriptLogs = false;
		
		/**
		 * Receives the add model command
		 */
		public void onAddModel() {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select models to load", true);
			for (File file : sgc.getSelectedFiles()) {
				SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
				_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, file));
				
				// Tell the view to add a model
				_commandSender.addModel(semsimmodel.getName());
			}
		}
		
		public void onAddModelByName(String modelName) throws FileNotFoundException {
			File file = new File("examples/AnnotatedModels/" + modelName + ".owl");
			SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
			_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, file));
			
			_commandSender.addModel(semsimmodel.getName());
		}
		
		public void onTaskClicked(String modelName, String task) {
			// If the model doesn't exist throw an exception
			if(!_models.containsKey(modelName))
				throw new IllegalArgumentException(modelName);
			
			// Get the model
			ModelInfo modelInfo = _models.get(modelName);
			SemSimModel model = modelInfo.Model;
			
			// Execute the proper task
			switch(task) {
				case "annotate":
					SemGen.gacts.NewAnnotatorTab(modelInfo.Path);
					break;
				case "dependencies":
					_commandSender.showDependencyNetwork(model.getName(),
							SemSimModelSerializer.toJsonString(SemSimModelSerializer.getDependencyNetwork(model)));
					break;
				case "extract":
					SemGen.gacts.NewExtractorTab(modelInfo.Path);
					break;
				case "merge":
					SemGen.gacts.NewMergerTab(modelInfo.Path, null);
					break;
				case "close":
					_models.remove(model);
					_commandSender.removeModel(modelName);
					break;
				case "submodels":
					ArrayList<SubModelNode> submodelNetwork = SemSimModelSerializer.getSubmodelNetwork(model);
					if(submodelNetwork.isEmpty())
						JOptionPane.showMessageDialog(null, "'" + model.getName() + "' does not have any submodels");
					else
						_commandSender.showSubmodelNetwork(model.getName(), SemSimModelSerializer.toJsonString(submodelNetwork));
					break;
				case "physiomap":
					ArrayList<PhysioMapNode> physiomapNetwork = SemSimModelSerializer.getPhysioMapNetwork(model);
					if(physiomapNetwork.isEmpty())
						JOptionPane.showMessageDialog(null,  "'" + model.getName() + "' does not have a PhysioMap");
					else
						_commandSender.showPhysioMapNetwork(model.getName(),
							SemSimModelSerializer.toJsonString(SemSimModelSerializer.getPhysioMapNetwork(model)));
					break;
				default:
					JOptionPane.showMessageDialog(null, "Task: '" + task +"', coming soon :)");
					break;
			}
		}
		
		public void onSearch(String searchString) throws FileNotFoundException {
			JsonString searchResults = CompositeAnnotationSearch.compositeAnnotationSearch(searchString);
			_commandSender.search(searchResults);
		}
		
		public void onMerge(String modelName1, String modelName2) {
			// If the models don't exist throw an exception
			if(!_models.containsKey(modelName1))
				throw new IllegalArgumentException(modelName1);
			
			if(!_models.containsKey(modelName2))
				throw new IllegalArgumentException(modelName2);
			
			ModelInfo model1Info = _models.get(modelName1);
			ModelInfo model2Info = _models.get(modelName2);
			
			SemGen.gacts.NewMergerTab(model1Info.Path, model2Info.Path);
		}
		
		/**
		 * Print Javascript logs in Java
		 * @param log
		 */
		public void onLog(String log) {
			if(ShowJavascriptLogs)
				System.out.println(log);
		}
	}
}
