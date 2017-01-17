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
		ExtractionNode extraction = new ExtractionNode(extractname);
		taskextractions.add(extraction);
		_commandSender.newExtraction(extraction);
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
		public void onClose() {
			closeTask();
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
		public void createExtraction(JSArray nodes, String extractname) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
			createNewExtraction(jnodes, extractname);
			
		}
		
		public void createExtractionExclude(String extractname, JSObject model, JSArray nodes) {
			ArrayList<Node<?>> jnodes = convertJSStageNodestoJava(nodes);
		}
		
		public void removeExtraction() {
			
		}
		
		public void removeNodesFromExtraction() {
			
		}
		
		public void addNodestoExtraction() {
			
		}
	}
	
}
