package semgen.stage.stagetasks.extractor;

import java.util.ArrayList;
import java.util.Observable;
import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;
import semgen.stage.serialization.ExtractionNode;
import semgen.stage.serialization.Node;
import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.ModelInfo;
import semgen.stage.stagetasks.StageTask;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.model.collection.SemSimModel;

public class ExtractorTask extends StageTask<ExtractorWebBrowserCommandSender> {
	private ExtractorWorkbench workbench;
	private ArrayList<ExtractionNode> taskextractions = new ArrayList<ExtractionNode>();
	
	public ExtractorTask(ModelInfo taskmodel, int index) {
		super(index);
		_models.add(taskmodel);
		_commandReceiver = new ExtractorCommandReceiver();
		workbench = new ExtractorWorkbench(taskmodel.accessor, taskmodel.Model);
	
		state = new StageState(Task.EXTRACTOR, _models, index);
	}

	public void createNewExtraction(ArrayList<Node<?>> nodestoextract, String extractname) {
		Extractor extractor = workbench.makeNewExtraction(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoextract);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, taskextractions.size());
		
		taskextractions.add(extraction);
		_commandSender.newExtraction(extraction);
	}	
	
	public void createNewExtractionExcluding(ArrayList<Node<?>> nodestoexclude, String extractname) {
		Extractor extractor = workbench.makeNewExtractionExclude(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoexclude);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, taskextractions.size());
		
		taskextractions.add(extraction);
		_commandSender.newExtraction(extraction);
	}
	
	private SemSimModel doExtraction(Extractor extractor, ArrayList<Node<?>> nodestoextract) {
		for (Node<?> node : nodestoextract) {
			node.collectforExtraction(extractor);
		}
		SemSimModel result = extractor.run();
		return result;
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		
	}

	@Override
	public semgen.stage.stagetasks.StageTask.Task getTaskType() {
		return Task.EXTRACTOR;
	}

	@Override
	public Class<ExtractorWebBrowserCommandSender> getSenderInterface() {
		return ExtractorWebBrowserCommandSender.class;
	}

	protected class ExtractorCommandReceiver extends CommunicatingWebBrowserCommandReceiver {
		public void onInitialized(JSObject jstaskobj) {
			jstask = jstaskobj;
			//jstask.setProperty("nodetreejs", new NodeTreeBridge());
			jstask.setProperty("extractionjs", new ExtractorBridge());
			
		}
		
		public void onRequestExtractions() {
				_commandSender.loadExtractions(taskextractions);
		}
		
		public void onNewExtraction(JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			createNewExtraction(jnodes, extractname);
		}
		
		public void onNewPhysioExtraction(JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStagePhysioNodestoJava(nodes);
			createNewExtraction(jnodes, extractname);
		}
		
		public void onCreateExtractionExclude(JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			createNewExtractionExcluding(jnodes, extractname);
		}
		
		public void onCreatePhysioExtractionExclude(JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			createNewExtractionExcluding(jnodes, extractname);
		}
		
		public void onRemoveExtraction(Double extraction) {
			
		}
		
		public void onRemoveNodesFromExtraction(Double extraction, JSArray nodes) {
			
		}
		
		public void onAddNodestoExtraction(Double extraction, JSArray nodes) {
			
		}
		
		public void onSendModeltoStage(JSArray indicies) {
			for (int i = 0; i < indicies.length(); i++) {
				int modelindex = (int)indicies.get(i).getNumberValue();
				SemSimModel newmodel = workbench.getExtractedModelbyIndex(modelindex).clone();
				queueModel(new ModelInfo(newmodel, workbench.getAccessorbyIndex(modelindex), modelindex+1));
			}
		}
		
		public void onClose() {
			closeTask();
		}
		
		public void onSave(JSArray indicies) {
			ArrayList<Integer> extractstosave = new ArrayList<Integer>();
			if (indicies.length()==0) return;
			for (int i = 0; i < indicies.length(); i++) {
				extractstosave.add((int)(indicies.get(i)).getNumberValue());
			}
			workbench.saveExtractions(extractstosave);
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
	
	public class ExtractorBridge {

	}
	
	
}
