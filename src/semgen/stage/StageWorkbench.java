package semgen.stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.serialization.SearchResultSet;
import semgen.stage.serialization.SemSimModelSerializer;
import semgen.stage.serialization.SubModelNode;
import semgen.utilities.SemGenError;
import semgen.utilities.Workbench;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semgen.visualizations.SemGenWebBrowserCommandSender;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

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
	public ModelAccessor getModelSourceLocation() {
		return null;
	}
	
	private class ModelInfo {
		public SemSimModel Model;
		public ModelAccessor Path;
		
		public ModelInfo(SemSimModel model, ModelAccessor path) {
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

		/**
		 * Receives the add model command
		 */
		public void onAddModel() {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select models to load", true);
			
			for (ModelAccessor ma : sgc.getSelectedFilesAsModelAccessors()) {
				LoadSemSimModel loader = new LoadSemSimModel(ma, false);
				loader.run();
				SemSimModel semsimmodel = loader.getLoadedModel();
				if (SemGenError.showSemSimErrors()) {
					continue;
				}
				_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, ma));
				
				// Tell the view to add a model
				_commandSender.addModel(semsimmodel.getName());
			}
		}
		
		public void onAddModelByName(String source, String modelName) throws FileNotFoundException {
			
			if(source.equals(CompositeAnnotationSearch.SourceName)) {
				
				File file = new File(SemGen.examplespath + "AnnotatedModels/" + modelName + ".owl");
				ModelAccessor ma = new ModelAccessor(file);
				LoadSemSimModel loader = new LoadSemSimModel(ma, false);
				loader.run();
				SemSimModel semsimmodel = loader.getLoadedModel();
				
				if (SemGenError.showSemSimErrors()) return;
				
				_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, ma));

				_commandSender.addModel(semsimmodel.getName());
			}
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
							SemSimModelSerializer.getDependencyNetwork(model));
					break;
				case "extract":
					SemGen.gacts.NewExtractorTab(modelInfo.Path);
					break;
				case "merge":
					SemGen.gacts.NewMergerTab(modelInfo.Path, null);
					break;
				case "close":
					_models.remove(modelName);
					_commandSender.removeModel(modelName);
					break;
				case "submodels":
					SubModelNode[] submodelNetwork = SemSimModelSerializer.getSubmodelNetwork(model);
					if(submodelNetwork.length <= 0)
						JOptionPane.showMessageDialog(null, "'" + model.getName() + "' does not have any submodels");
					else
						_commandSender.showSubmodelNetwork(model.getName(), submodelNetwork);
					break;
				default:
					JOptionPane.showMessageDialog(null, "Task: '" + task +"', coming soon :)");
					break;
			}
		}

		public void onSearch(String searchString) throws FileNotFoundException {
			SearchResultSet[] resultSets = {
					CompositeAnnotationSearch.compositeAnnotationSearch(searchString),
					// PMR results here
			};

			_commandSender.search(resultSets);
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
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
}
