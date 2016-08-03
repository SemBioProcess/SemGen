package semgen.stage.stagetasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observable;

import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.serialization.SearchResultSet;
import semgen.stage.serialization.StageState;
import semgen.utilities.SemGenError;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public class ProjectTask extends StageTask<ProjectWebBrowserCommandSender> {
	
	public ProjectTask() {
		_commandReceiver = new ProjectCommandReceiver();
		state = new StageState(Task.PROJECT);
	}
	
	
	protected void removeModel(Integer index) {
		_models.set(index, null);
	}
	/**
	 * Receives commands from javascript
	 * @author Ryan
	 *
	 */
	protected class ProjectCommandReceiver extends CommunicatingWebBrowserCommandReceiver {

		/**
		 * Receives the add model command
		 */
		public void onAddModel() {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select models to load", true);
			for (File file : sgc.getSelectedFiles()) {
				boolean alreadyopen = false;
				
				ModelAccessor accessor = new ModelAccessor(file);
				
				for (ModelInfo info : _models) {
					alreadyopen = info.accessor.equals(accessor);
					if (alreadyopen) break;
				}
				if (alreadyopen) continue;
				
				LoadSemSimModel loader = new LoadSemSimModel(accessor, false);
				loader.run();
				SemSimModel semsimmodel = loader.getLoadedModel();
				if (SemGenError.showSemSimErrors()) {
					continue;
				}
				
				
				ModelInfo info = new ModelInfo(semsimmodel, accessor, _models.size());
				_models.add(info);
				
				// Tell the view to add a model
				_commandSender.addModel(info.modelnode);
			}
		}
		
		public void onAddModelByName(String source, String modelName) throws FileNotFoundException {
			if(source.equals(CompositeAnnotationSearch.SourceName)) {
				ModelAccessor file = new ModelAccessor(SemGen.examplespath + "AnnotatedModels/" + modelName + ".owl");
				LoadSemSimModel loader = new LoadSemSimModel(file, false);
				loader.run();
				SemSimModel semsimmodel = loader.getLoadedModel();
				if (SemGenError.showSemSimErrors()) {
					return;
				}
				ModelInfo info = new ModelInfo(semsimmodel, file, _models.size());
				_models.add(info);

				_commandSender.addModel(info.modelnode);
			}
		}
		
		public void onTaskClicked(Integer modelindex, String task) {
			
			// Get the model
			ModelInfo modelInfo = _models.get(modelindex);
			
			// Execute the proper task
			switch(task) {
				case "annotate":
					SemGen.gacts.NewAnnotatorTab(modelInfo.accessor);
					break;
				case "dependencies":
				//	modelInfo.modelnode.requestAllChildDependencies());
					break;
				case "extract":
					SemGen.gacts.NewExtractorTab(modelInfo.accessor);
					break;
				case "merge":
					break;
				case "close":
					removeModel(modelindex);
					_commandSender.removeModel(modelindex);
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
		
		public void onMerge(Double model1, Double model2) {
			createMerger(model1.intValue(), model2.intValue());
		}
		
		public void onQueryModel(Integer modelindex, String query) {
			ModelInfo modelInfo = _models.get(modelindex);
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
		
		public void onChangeTask(Number index) {
			
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
	public void update(Observable o, Object arg) {
		
	}

	@Override
	public Task getTaskType() {
		return Task.PROJECT;
	}

	@Override
	public Class<ProjectWebBrowserCommandSender> getSenderInterface() {
		return ProjectWebBrowserCommandSender.class;
	}
	
}
