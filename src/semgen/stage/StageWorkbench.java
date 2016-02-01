package semgen.stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.serialization.PhysioMapNode;
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
	public String getModelSourceFile() {
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

		/**
		 * Receives the add model command
		 */
		public void onAddModel() {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select models to load", true);
			for (File file : sgc.getSelectedFiles()) {
				LoadSemSimModel loader = new LoadSemSimModel(file, false);
				loader.run();
				SemSimModel semsimmodel = loader.getLoadedModel();
				if (SemGenError.showSemSimErrors()) {
					continue;
				}
				_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, file));
				
				// Tell the view to add a model
				_commandSender.addModel(semsimmodel.getName());
			}
		}
		
		public void onAddModelByName(String source, String modelName) throws FileNotFoundException {
			if(source.equals(CompositeAnnotationSearch.SourceName)) {
				File file = new File(SemGen.examplespath + "AnnotatedModels/" + modelName + ".owl");
				LoadSemSimModel loader = new LoadSemSimModel(file, false);
				loader.run();
				SemSimModel semsimmodel = loader.getLoadedModel();
				if (SemGenError.showSemSimErrors()) {
					return;
				}
				_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, file));

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
				case "physiomap":
					PhysioMapNode[] physiomapNetwork = SemSimModelSerializer.getPhysioMapNetwork(model);
					if(physiomapNetwork.length <= 0)
						JOptionPane.showMessageDialog(null,  "'" + model.getName() + "' does not have a PhysioMap");
					else
						_commandSender.showPhysioMapNetwork(model.getName(), physiomapNetwork);
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
		
		public void onQueryModel(String modelName, String query) {
			ModelInfo modelInfo = _models.get(modelName);
			switch (query) {
			case "hassubmodels":
				Boolean hassubmodels = !modelInfo.Model.getSubmodels().isEmpty();
				_commandSender.receiveReply(hassubmodels.toString());
				break;
			case "hasdependencies":
				Boolean hasdependencies = !modelInfo.Model.getAssociatedDataStructures().isEmpty();
				_commandSender.receiveReply(hasdependencies.toString());
				break;
			}
		}
		
		public void onConsoleOut(String msg) {
			System.out.println(msg);
		}
		
		public void onConsoleOut(Number msg) {
			System.out.println(msg.toString());
		}
		
		public void onConsoleOut(boolean msg) {
			System.out.println(msg);
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
}
