package semgen.stage.stagetasks;

import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JOptionPane;

import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;

import org.apache.commons.io.FilenameUtils;
import semgen.SemGen;
import semgen.SemGenGUI;
import semgen.SemGenGUI.loadTask;
import semgen.SemGenGUI.saveTask;
import semgen.search.BioModelsSearch;
import semgen.search.CompositeAnnotationSearch;
import semgen.stage.serialization.ExtractionNode;
import semgen.stage.serialization.Node;
import semgen.stage.serialization.SearchResultSet;
import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.extractor.ExtractionInfo;
import semgen.stage.stagetasks.extractor.ModelExtractionGroup;
import semgen.utilities.SemGenError;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.reading.ModelClassifier.ModelType;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;

import java.io.*;

public class ProjectTask extends StageTask<ProjectWebBrowserCommandSender> {
	
	public ArrayList<ModelExtractionGroup> extractnodeworkbenchmap = new ArrayList<ModelExtractionGroup>(); 
	
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
	public class ProjectCommandReceiver extends CommunicatingWebBrowserCommandReceiver {

		public void onInitialized(JSObject jstaskobj) {
			jstask = jstaskobj;
		}


		/**
		 * Receives the add model command
		 */
		public void onAddModel() {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select models to load", true);

			for (ModelAccessor accessor : sgc.getSelectedFilesAsModelAccessors()) {
				boolean alreadyopen = false;

				for (ModelInfo info : _modelinfos) {
					if (info != null) {
						if (info.accessor != null) {
							alreadyopen = info.accessor.equals(accessor);
						}
					}
					if (alreadyopen) break;
				}
				if (alreadyopen) continue;

				loadTask loadtask = new loadTask(accessor, ProjectTask.this, true);
				loadtask.execute();
			}
		}

		public void onAddModelByName(String source, String modelName) throws IOException, BioModelsWSException {
			ModelAccessor accessor = null;
			if (source.equals(CompositeAnnotationSearch.SourceName)) {
				accessor = FileAccessorFactory.getModelAccessor(SemGen.examplespath + "AnnotatedModels/" + modelName + ".owl");
			}
			else if (source.equals("BioModels")) {
				System.out.println("Retrieving SBML file from BioModels...");

				String tempPath = System.getProperty("java.io.tmpdir") + File.separator + modelName + ".xml";
				File tempBioModelFile = new File(tempPath);
				BufferedWriter bw = new BufferedWriter(new FileWriter(tempBioModelFile));
				String bioModelString = BioModelsSearch.getModelSBMLById(modelName);

				bw.write(bioModelString);
				bw.close();
				accessor = FileAccessorFactory.getModelAccessor(tempBioModelFile.getPath());
				tempBioModelFile.deleteOnExit();
			}
			if (accessor != null) {
				loadTask loadtask = new loadTask(accessor, ProjectTask.this);
				loadtask.execute();
			}
		}
		
		// For loading models from within the Annotator
		public void onAddModelFromAnnotator(ModelAccessor accessor){
			loadTask loadtask = new loadTask(accessor, ProjectTask.this);
			loadtask.execute();
		}
		
		public void onTaskClicked(JSArray modelindex, String task) {
			// Execute the proper task
			switch(task) {
				case "annotate":
					annotateModels(modelindex);
					break;
				case "save":
					saveModels(modelindex);
					break;
				case "export":
					exportModels(modelindex);
					break;
				case "close":
					closeModels(modelindex);					
					break;
				default:
					JOptionPane.showMessageDialog(SemGen.getSemGenGUI(), "Task: '" + task +"', coming soon :)");
					break;
			}

		}



		public void onCloseModels(JSArray modelindex) {
			closeModels(modelindex);
		}

		public void onSearch(String searchString) throws Exception {
			SearchResultSet[] resultSets = {
					CompositeAnnotationSearch.compositeAnnotationSearch(searchString)
			};
			_commandSender.search(resultSets);
		}

		public void onBioModelsSearch(String searchString) throws Exception {
			SearchResultSet[] resultSets = {
					BioModelsSearch.bioModelsSearch(searchString)
			};
			_commandSender.search(resultSets);
		}

		public void onGetModelAbstract(String modelName) throws BioModelsWSException {
			String bioModelAbstract = BioModelsSearch.findPubmedAbstract(modelName);
			_commandSender.showModelAbstract(bioModelAbstract);
		}

		public void onMerge(JSArray model1, JSArray model2) {
			createMerger(model1, model2);
		}


		public void onQueryModel(Integer modelindex, String query) {
			ModelInfo modelInfo = _modelinfos.get(modelindex);
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
			ArrayList<ArrayList<ExtractionNode>> extractions = new ArrayList<ArrayList<ExtractionNode>>();
			for (ModelExtractionGroup meg : extractnodeworkbenchmap) {
				if (meg!=null) {
					extractions.add(meg.getExtractionArray());
				}
				else {
					extractions.add(null);
				}

			}
			_commandSender.loadExtractions(extractions);
		}

		//Each possible extraction task needs two methods, one for submodels and data structures and another
		//for physiomap based extractions 

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

		public void onRemoveNodesFromExtraction(Double sourceindex, Double extraction, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes, sourceindex, extraction);
			removeNodesfromExtraction(sourceindex.intValue(), extraction.intValue(), jnodes);
		}

		public void onRemovePhysioNodesFromExtraction(Double sourceindex, Double extraction, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStagePhysioNodestoJava(nodes,sourceindex, extraction);
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
			saveModels(indicies);
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

	protected void annotateModels(JSArray indicies) {
		for (int i=0; i < indicies.length(); i++) {
			JSArray address = indicies.get(i).asArray();
			int indexedtomodel = address.get(0).asNumber().getInteger();
			int modelindex = address.get(1).asNumber().getInteger();


			ModelAccessor accessor = null;
			if (indexedtomodel==-1) {
				accessor = _modelinfos.get(modelindex).accessor;
			}
			else {
				ModelExtractionGroup meg = this.extractnodeworkbenchmap.get(indexedtomodel);
				if (meg!=null) {
					accessor = meg.getAccessorbyIndexAlways(modelindex);
				}
			}
			if (accessor == null) continue;
			SemGen.gacts.NewAnnotatorTab(accessor);
		}
	}

	protected void exportModels(JSArray indicies) {

		for (int i=0; i < indicies.length(); i++) {
			StageRootInfo<?> modelinfo = null;
			JSArray address = indicies.get(i).asArray();
			int indexedtomodel = address.get(0).asNumber().getInteger();
			int modelindex = address.get(1).asNumber().getInteger();

			if (indexedtomodel==-1) {
				modelinfo = this.getModel(modelindex);
				String selectedtype = "owl";  // Default extension type
				ModelType modtype = modelinfo.Model.getSourceModelType();

				if(modtype==ModelType.MML_MODEL_IN_PROJ || modtype==ModelType.MML_MODEL) selectedtype = "proj";
				else if(modtype==ModelType.CELLML_MODEL) selectedtype = "cellml";
				else if(modtype==ModelType.SBML_MODEL) selectedtype = "sbml";

				String suggestedparentfilename = FilenameUtils.removeExtension(modelinfo.accessor.getFileName());
				String modelnameinarchive = modelinfo.accessor.getFileName();

				SemGenSaveFileChooser filec = new SemGenSaveFileChooser(SemGenSaveFileChooser.ALL_WRITABLE_TYPES, selectedtype, modelnameinarchive, suggestedparentfilename);
				ModelAccessor ma = filec.SaveAsAction(modelinfo.Model);
			
				if (ma != null)	{
					saveTask savetask = new SemGenGUI.saveTask(ma, modelinfo.Model, this);	
					savetask.execute();
				}
			}
			else {
				if (this.extractnodeworkbenchmap.get(indexedtomodel)!=null) {
					modelinfo =  this.extractnodeworkbenchmap.get(indexedtomodel).getExtractionInfo(modelindex);
					this.extractnodeworkbenchmap.get(indexedtomodel).exportExtraction(modelindex);
				}
			}
		}			
	}

	protected void saveModels(JSArray indicies) {
		for (int i = 0; i < indicies.length(); i++) {
			JSArray saveset = indicies.get(i).asArray();
			Integer basemodelindex = saveset.get(0).asNumber().getInteger();
			Integer targetindex = saveset.get(1).asNumber().getInteger();
			if (basemodelindex==-1) {

			}
			else {
				if (this.extractnodeworkbenchmap.get(basemodelindex)!=null) {
					extractnodeworkbenchmap.get(basemodelindex).saveExtraction(targetindex);
				}
			}
		}
	}
	
	
	// For closing from the Javascript side
	public void closeModels(JSArray indicies) {
		
		for (int i=0; i < indicies.length(); i++) {
			JSArray address = indicies.get(i).asArray();
			int parentmodelindex = address.get(0).asNumber().getInteger();
			int modelindex = address.get(1).asNumber().getInteger();
			closeModels(parentmodelindex, modelindex);
		}
	}
	
	// Overloaded method so that we can call the close command from the Java side and the Javascript side
	// Returns whether the close was cancelled
	public boolean closeModels(int parentmodelindex, int modelindex) {
		
		if (parentmodelindex==-1) { // if it's not an extraction
			removeModel(modelindex);
			//Only remove the extraction group if it doesn't contain any extractions
			if (this.extractnodeworkbenchmap.get(modelindex).isEmpty()) {
				this.extractnodeworkbenchmap.set(modelindex, null);
			}
		}
		else {
			ModelExtractionGroup meg = this.extractnodeworkbenchmap.get(parentmodelindex);
			ExtractionInfo exinfo = meg.getExtractionInfo(modelindex);
			if(exinfo==null) return false; // Skips extractions that were already created, saved and closed
			
			String name = exinfo.getModelName();
			
			if( ! exinfo.getChangesSaved()){
				int returnval = JOptionPane.showConfirmDialog(SemGen.getSemGenGUI(), 
						"Save extraction " + name + "?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);
				if(returnval==JOptionPane.YES_OPTION){
					ModelAccessor returnma = meg.saveExtraction(modelindex);
					if(returnma==null) return true; // Return true to indicate the save operation, and thus the close operation, was cancelled
				}
				else if(returnval==JOptionPane.NO_OPTION){
				}
				else if(returnval==JOptionPane.CANCEL_OPTION){
					return true;
				}
			}
			
			boolean empty = meg.removeExtraction(modelindex);
			//If the parent model has been removed and the extraction group is empty, remove the extraction group
			if (empty && this._modelinfos==null) {
				this.extractnodeworkbenchmap.set(modelindex, null);
			}
		}
		_commandSender.removeModel(new Integer[]{parentmodelindex, modelindex});
		return false;
		
	}

	@Override
	public void addModeltoTask(ModelInfo info, boolean updatestage) {
		extractnodeworkbenchmap.add(new ModelExtractionGroup(info));
		super.addModeltoTask(info, updatestage);
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
		ModelExtractionGroup group = this.extractnodeworkbenchmap.get(infoindex);
		ExtractionNode extraction = group.createExtraction(extractname, nodestoextract);
		if (extraction !=null) {
			_commandSender.newExtraction(infoindex, extraction);
		}
		else SemGenError.showError("Empty Model", "Attempted extraction results in an empty model");
	}

	//Create an extraction including all nodes except those selected
	public void createNewExtractionExcluding(Integer infoindex, ArrayList<Node<?>> nodestoexclude, String extractname) {
		ModelExtractionGroup group = this.extractnodeworkbenchmap.get(infoindex);
		ExtractionNode extraction = group.createExtractionExcluding(extractname, nodestoexclude);
		_commandSender.newExtraction(infoindex, extraction);
	}

	//Add nodes to an existing extraction
	public void addNodestoExtraction(Integer infoindex, Integer extractionindex, ArrayList<Node<?>> nodestoadd) {
		ModelExtractionGroup group = this.extractnodeworkbenchmap.get(infoindex);
		ExtractionNode extraction = group.addNodestoExtraction(extractionindex, nodestoadd);

		_commandSender.modifyExtraction(infoindex, extractionindex, extraction);
	}

	//Remove nodes from existing extraction
	public void removeNodesfromExtraction(Integer infoindex, Integer extractionindex, ArrayList<Node<?>> nodestoremove) {
		ModelExtractionGroup group = this.extractnodeworkbenchmap.get(infoindex);
		ExtractionNode extraction = group.removeNodesfromExtraction(extractionindex, nodestoremove);
		_commandSender.modifyExtraction(infoindex, extractionindex, extraction);
	}

	//Close an extraction on the stage
	protected void removeExtraction(Double sourceindex, Double indextoremove) {
		this.extractnodeworkbenchmap.get(sourceindex.intValue()).removeExtraction(indextoremove.intValue());
	}

	//Convert Javascript Node objects to Java Node objects
	public ArrayList<Node<?>> convertJSStageNodestoJava(JSArray nodearray, Double modelindex, Double extractionindex) {
		ModelExtractionGroup group = this.extractnodeworkbenchmap.get(modelindex.intValue());
		ArrayList<Node<?>> javanodes = new ArrayList<Node<?>>();
		for (int i = 0; i < nodearray.length(); i++) {
			JSObject val = nodearray.get(i).asObject();
			javanodes.add(group.getNodebyHash(val.getProperty("hash").asNumber().getInteger(), val.getProperty("id").getStringValue(), extractionindex.intValue()));
		}
		return javanodes;
	}

	//Convert Javascript Node objects to Java Node objects
	public ArrayList<Node<?>> convertJSStagePhysioNodestoJava(JSArray nodearray, Double modelindex, Double extractionindex) {
		ModelExtractionGroup group = this.extractnodeworkbenchmap.get(modelindex.intValue());
		ArrayList<Node<?>> javanodes = new ArrayList<Node<?>>();
		for (int i = 0; i < nodearray.length(); i++) {
			JSObject val = nodearray.get(i).asObject();
			javanodes.add(group.getPhysioMapNodebyHash(val.getProperty("hash").asNumber().getInteger(), val.getProperty("id").getStringValue(), extractionindex.intValue()));
		}
		return javanodes;
	}

	//Get a node by its model index and hash number
	protected StageRootInfo<?> getInfobyAddress(JSArray address) {
		if (address.get(0).asNumber().getInteger()==-1) {
			return _modelinfos.get(address.get(1).asNumber().getInteger());
		}
		else {
			return this.extractnodeworkbenchmap.get(address.get(0).asNumber().getInteger()).getExtractionInfo(address.get(1).asNumber().getInteger());
		}
	}
}
