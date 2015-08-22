package semgen.stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.janet.janet_calls;
import semgen.stage.janet.janet_main;
import semgen.stage.janet.parseSearchResults;
import semgen.stage.janet.waitOnKeyStrokes;
import semgen.stage.serialization.SearchResultSet;
import semgen.stage.serialization.SemSimModelSerializer;
import semgen.stage.serialization.SubModelNode;
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
		
		public void onAddModelByName(String source, String modelName) throws FileNotFoundException {
			if(source.equals(CompositeAnnotationSearch.SourceName)) {
				File file = new File("examples/AnnotatedModels/" + modelName + ".owl");
				
				//System.out.println("file == " + file);
				
				SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
				_models.put(semsimmodel.getName(), new ModelInfo(semsimmodel, file));

				_commandSender.addModel(semsimmodel.getName());
			}
			//PMR
			if(source.equals("PMR")) {
				String filepath =parseSearchResults.modelnameToFilePath(modelName);
				File file = new File("examples/JanetModels/" +filepath);
				SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
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
					_models.remove(model);
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
	 		// Janet code goes here.
	 		resultSets = janet_main.processSearch(resultSets,searchString);
	 		
	 	
	 		
	 		
	 		//resultSets = t1.getR
	 				
	 						
	 		_commandSender.search(resultSets);
	    	//resultSets = janet_main.processSearch(resultSets,searchString);
	    	
			
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
}
