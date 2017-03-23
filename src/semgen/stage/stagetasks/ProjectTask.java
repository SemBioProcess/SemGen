package semgen.stage.stagetasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.JOptionPane;

import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;

import org.apache.commons.io.FilenameUtils;
import semgen.SemGen;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.serialization.ExtractionNode;
import semgen.stage.serialization.Node;
import semgen.stage.serialization.SearchResultSet;
import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.extractor.Extractor;
import semgen.stage.stagetasks.extractor.ExtractorWorkbench;
import semgen.utilities.SemGenError;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SaveSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier.ModelType;

public class ProjectTask extends StageTask<ProjectWebBrowserCommandSender> {
	
	private ArrayList<ExtractionNode> taskextractions = new ArrayList<ExtractionNode>();
	private HashMap<ModelInfo, ExtractorWorkbench> extractnodeworkbenchmap = new HashMap<ModelInfo, ExtractorWorkbench>(); 
	
	public ProjectTask() {
		super(0);
		_commandReceiver = new ProjectCommandReceiver();
		state = new StageState(Task.PROJECT, taskindex);
	}

	/**
	 * Receives commands from javascript
	 * @author Ryan
	 *
	 */
	protected class ProjectCommandReceiver extends CommunicatingWebBrowserCommandReceiver {
		
		public void onInitialized(JSObject jstaskobj) {
			jstask = jstaskobj;
		}
		
		
		/**
		 * Receives the add model command
		 */
		public void onAddModel() {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select models to load", true);
			for (File file : sgc.getSelectedFiles()) {
				boolean alreadyopen = false;
				
				ModelAccessor accessor = new ModelAccessor(file);
				
				for (ModelInfo info : _models) {
					if (info != null) {
						alreadyopen = info.accessor.equals(accessor);
					}
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
				extractnodeworkbenchmap.put(info, new ExtractorWorkbench(info.accessor, info.Model));
				addModeltoTask(info);
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
	
				extractnodeworkbenchmap.put(info, new ExtractorWorkbench(info.accessor, info.Model));
				addModeltoTask(info);
				_commandSender.addModel(info.modelnode);
			}
		}
		
		public void onTaskClicked(Double modelindex, String task) {
			
			// Get the model
			ModelInfo modelInfo = _models.get(modelindex.intValue());
			
			// Execute the proper task
			switch(task) {
				case "annotate":
					SemGen.gacts.NewAnnotatorTab(modelInfo.accessor);
					break;
				case "export":
					String selectedtype = "owl";  // Default extension type
					ModelType modtype = modelInfo.Model.getSourceModelType();
					
					if(modtype==ModelType.MML_MODEL_IN_PROJ || modtype==ModelType.MML_MODEL) selectedtype = "proj";
					else if(modtype==ModelType.CELLML_MODEL) selectedtype = "cellml";
					else if(modtype==ModelType.SBML_MODEL) selectedtype = "sbml";
					
					String suggestedparentfilename = FilenameUtils.removeExtension(modelInfo.accessor.getFileThatContainsModel().getName());
					String modelnameinarchive = modelInfo.accessor.getModelName();
					
					SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "proj", "cellml", "sbml"}, selectedtype, modelnameinarchive, suggestedparentfilename);
					ModelAccessor ma = filec.SaveAsAction(modelInfo.Model);
					
					if (ma != null)				
						SaveSemSimModel.writeToFile(modelInfo.Model, ma, ma.getFileThatContainsModel(), filec.getFileFilter());					
					
					break;
				case "close":
					removeModel(modelindex.intValue());
					_commandSender.removeModel(modelindex.intValue());
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
		
		public void onRequestExtractions() {
				_commandSender.loadExtractions(taskextractions);
		}
		
		public void onNewExtraction(Double sourceindex, JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			createNewExtraction(sourceindex.intValue(), jnodes, extractname);
		}
		
		public void onNewPhysioExtraction(Double sourceindex, JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStagePhysioNodestoJava(nodes);
			createNewExtraction(sourceindex.intValue(), jnodes, extractname);
		}
		
		public void onCreateExtractionExclude(Double sourceindex, JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			createNewExtractionExcluding(sourceindex.intValue(), jnodes, extractname);
		}
		
		public void onCreatePhysioExtractionExclude(Double sourceindex, JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStagePhysioNodestoJava(nodes);
			createNewExtractionExcluding(sourceindex.intValue(), jnodes, extractname);
		}
		
		public void onRemoveExtraction(Double sourceindex, Double extractionindex) {
			removeExtraction(sourceindex, extractionindex);
		}
		
		public void onRemoveNodesFromExtraction(Double sourceindex, Double extraction, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes, extraction);
			removeNodesfromExtraction(sourceindex.intValue(), extraction.intValue(), jnodes);
		}
		
		public void onRemovePhysioNodesFromExtraction(Double sourceindex, Double extraction, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStagePhysioNodestoJava(nodes, extraction);
			removeNodesfromExtraction(sourceindex.intValue(), extraction.intValue(), jnodes);
		}
		
		public void onAddNodestoExtraction(Double sourceindex, Double extraction, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			addNodestoExtraction(sourceindex.intValue(), extraction.intValue(), jnodes);
			
		}
		
		public void onAddPhysioNodestoExtraction(Double sourceindex, Double extraction, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStagePhysioNodestoJava(nodes);
			addNodestoExtraction(sourceindex.intValue(), extraction.intValue(), jnodes);
			
		}
		
		public void onSave(JSArray indicies) {
			ArrayList<Integer> extractstosave = new ArrayList<Integer>();
			if (indicies.length()==0) return;
			for (int i = 0; i < indicies.length(); i++) {
				extractstosave.add((int)(indicies.get(i)).getNumberValue());
			}
			//workbench.saveExtractions(extractstosave);
		}
		
		public void onChangeTask(Double index) {
			switchTask(index.intValue());
		}
		
		public void onConsoleOut(String msg) {
			System.out.println(msg);
		}
		
		public void onConsoleOut(Double msg) {
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


	@Override
	public void closeTask() {
		
	}
	
	
	//EXTRACTION FUNCTIONS
	public void createNewExtraction(Integer infoindex, ArrayList<Node<?>> nodestoextract, String extractname) {
		ExtractorWorkbench workbench = this.extractnodeworkbenchmap.get(_models.get(infoindex));
		Extractor extractor = workbench.makeNewExtraction(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoextract);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, taskextractions.size());
		
		taskextractions.add(extraction);
		_commandSender.newExtraction(infoindex, extraction);
	}	
	
	public void createNewExtractionExcluding(Integer infoindex, ArrayList<Node<?>> nodestoexclude, String extractname) {
		ExtractorWorkbench workbench = this.extractnodeworkbenchmap.get(_models.get(infoindex));
		Extractor extractor = workbench.makeNewExtractionExclude(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoexclude);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, taskextractions.size());
		
		taskextractions.add(extraction);
		_commandSender.newExtraction(infoindex, extraction);
	}
	
	public void addNodestoExtraction(Integer infoindex, Integer extractionindex, ArrayList<Node<?>> nodestoadd) {
		ExtractorWorkbench workbench = this.extractnodeworkbenchmap.get(_models.get(infoindex));
		Extractor extractor = workbench.makeAddExtractor(extractionindex);
		SemSimModel extractedmodel = doExtraction(extractor, nodestoadd);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, extractionindex);
		
		taskextractions.set(extractionindex, extraction);
		_commandSender.modifyExtraction(infoindex, extractionindex, extraction);
	}
	
	public void removeNodesfromExtraction(Integer infoindex, Integer extractionindex, ArrayList<Node<?>> nodestoremove) {
		ExtractorWorkbench workbench = this.extractnodeworkbenchmap.get(_models.get(infoindex));
		Extractor extractor = workbench.makeRemoveExtractor(extractionindex);
		SemSimModel extractedmodel = doExtraction(extractor, nodestoremove);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, extractionindex);
		
		taskextractions.set(extractionindex, extraction);
		_commandSender.modifyExtraction(infoindex, extractionindex, extraction);
	}
	
	
	private SemSimModel doExtraction(Extractor extractor, ArrayList<Node<?>> nodestoextract) {
		for (Node<?> node : nodestoextract) {
			node.collectforExtraction(extractor);
		}
		SemSimModel result = extractor.run();
		return result;
	}
	
	protected void removeExtraction(Double sourceindex, Double index) {		
		ExtractorWorkbench workbench = this.extractnodeworkbenchmap.get(_models.get(sourceindex.intValue()));
		ExtractionNode nodetoremove = taskextractions.set(index.intValue(), null);
		workbench.removeExtraction(nodetoremove.getSourceObject());
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getNodebyHash(int nodehash, String nodeid, int extractionindex) {
		Node<?> returnnode = taskextractions.get(extractionindex).getNodebyHash(nodehash, nodeid);
		if (returnnode!=null) return returnnode; 
		return null;
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getPhysioMapNodebyHash(int nodehash, String nodeid, int extractionindex) {
		Node<?> returnnode = taskextractions.get(extractionindex).getPhysioMapNodebyHash(nodehash, nodeid);
		if (returnnode!=null) return returnnode; 

		return null;
	}
	
	//Convert Javascript Node objects to Java Node objects
	public ArrayList<Node<?>> convertJSStageNodestoJava(JSArray nodearray, Double extractionindex) {
		ArrayList<Node<?>> javanodes = new ArrayList<Node<?>>();
		for (int i = 0; i < nodearray.length(); i++) {
			JSObject val = nodearray.get(i).asObject();
			javanodes.add(getNodebyHash(val.getProperty("hash").asNumber().getInteger(), val.getProperty("id").getStringValue(), extractionindex.intValue()));
		}
		return javanodes;
	}

	//Convert Javascript Node objects to Java Node objects
	public ArrayList<Node<?>> convertJSStagePhysioNodestoJava(JSArray nodearray, Double extractionindex) {
		ArrayList<Node<?>> javanodes = new ArrayList<Node<?>>();
		for (int i = 0; i < nodearray.length(); i++) {
			JSObject val = nodearray.get(i).asObject();
			javanodes.add(getPhysioMapNodebyHash(val.getProperty("hash").asNumber().getInteger(), val.getProperty("id").getStringValue(), extractionindex.intValue()));
		}
		return javanodes;
	}
	
}
