package semgen.stage.stagetasks.merge;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import org.apache.commons.lang3.tuple.Pair;

import com.teamdev.jxbrowser.chromium.JSObject;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.MergerWorkbench.MergeEvent;
import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.ModelInfo;
import semgen.stage.stagetasks.StageTask;
import semgen.utilities.SemGenError;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.model.collection.SemSimModel;

public class MergerTask extends StageTask<MergerWebBrowserCommandSender> implements Observer {
	private MergerWorkbench workbench = new MergerWorkbench();
	private ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>> dsdescriptors;
	
	public MergerTask(ArrayList<ModelInfo> modelinfo, StageState state) {
		_commandReceiver = new MergerCommandReceiver();
		ArrayList<File> files = new ArrayList<File>();
		ArrayList<SemSimModel> models = new ArrayList<SemSimModel>();
		
		for (ModelInfo model : modelinfo) {
			_models.put(model.getModelName(), model);
			files.add(model.Path);
			models.add(model.Model);
		}
		this.state = state;
		workbench.addModels(files, models, true);
	}

	public void primeForMerging() {
		if (workbench.getNumberofStagedModels() == 0) return;
		if(workbench.hasMultipleModels()) {

			SemGenProgressBar progframe = new SemGenProgressBar("Comparing models...", true);
			workbench.mapModels();
			progframe.dispose();
		}
		//Check if two models have semantic overlap
		if (!workbench.hasSemanticOverlap()) {
			SemGenError.showError("SemGen did not find any semantic equivalencies between the models", "Merger message");
			return;
		}
		int n = workbench.getSolutionDomainCount();
		ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>> descriptors = new ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>>();
		
		for (int i = n; i < (workbench.getMappingCount()); i++) {
			descriptors.add(workbench.getDSDescriptors(i));
		}
		dsdescriptors = descriptors;
	}
	
	public File saveMerge() {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser("Choose location to save file", 
				new String[]{"owl"});
		
		if (filec.SaveAsAction()!=null) {
			return filec.getSelectedFile();
		}
		return null;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg==MergeEvent.modellistupdated) {
			primeForMerging();
		}
		if (arg == MergeEvent.threemodelerror) {
			SemGenError.showError("SemGen can only merge two models at a time.", "Too many models");
		}	
		if (arg == MergeEvent.modelerrors) {
			JOptionPane.showMessageDialog(null, "Model " + ((MergeEvent)arg).getMessage() + " has errors.",
					"Failed to analyze.", JOptionPane.ERROR_MESSAGE);
		}
		if (arg == MergeEvent.mergecompleted) {
			File file = saveMerge();	
			if (file==null) return;
		}
	}
	
	protected class MergerCommandReceiver extends CommunicatingWebBrowserCommandReceiver {
		public void onMinimizeTask(JSObject snapshot) {
			createStageState(snapshot);
			switchTask(0);
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
	public Task getTaskType() {
		return Task.MERGER;
	}
	
	public Class<MergerWebBrowserCommandSender> getSenderInterface() {
		return MergerWebBrowserCommandSender.class;
	}
}
