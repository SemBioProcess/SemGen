package semgen.stage.stagetasks.merge;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.annotations.Expose;
import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.DataStructureDescriptor.Descriptor;
import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.MergerWorkbench.MergeEvent;
import semgen.stage.serialization.DependencyNode;
import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.ModelInfo;
import semgen.stage.stagetasks.StageTask;
import semgen.utilities.SemGenError;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelAccessor;

public class MergerTask extends StageTask<MergerWebBrowserCommandSender> implements Observer {
	private MergerWorkbench workbench = new MergerWorkbench();
	private MergeConflictResolvers resolvers;
	private ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>> dsdescriptors;
	private ArrayList<Pair<DependencyNode, DependencyNode>> overlaps = new ArrayList<Pair<DependencyNode, DependencyNode>>();
	
	public MergerTask(ArrayList<ModelInfo> modelinfo) {
		workbench.addObserver(this);
		_commandReceiver = new MergerCommandReceiver();
		ArrayList<ModelAccessor> files = new ArrayList<ModelAccessor>();
		ArrayList<SemSimModel> models = new ArrayList<SemSimModel>();
		
		for (ModelInfo model : modelinfo) {
			ModelInfo info = new ModelInfo(model, _models.size());
			_models.add(info);
			files.add(info.accessor);
			models.add(info.Model);
		}
		workbench.addModels(files, models, true);
		state = new StageState(Task.MERGER, modelinfo);
		
		primeForMerging();

		resolvers = new MergeConflictResolvers(workbench);
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
		generateOverlapDescriptors();
		getOverlappingNodes();
	}

	private void generateOverlapDescriptors() {
		int n = workbench.getSolutionDomainCount();
		ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>> descriptors = new ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>>();
		
		for (int i = n; i < (workbench.getMappingCount()); i++) {
			descriptors.add(workbench.getDSDescriptors(i));
		}
		dsdescriptors = descriptors;
	}
	
	private void getOverlappingNodes() {
		overlaps.clear();
		ArrayList<Pair<DataStructure,DataStructure>> dsoverlaps = workbench.getOverlapPairs();
		
		for (Pair<DataStructure,DataStructure> overlap : dsoverlaps) {
			DependencyNode left = _models.get(0).modelnode.getDependencyNode(overlap.getLeft());
			DependencyNode right = _models.get(1).modelnode.getDependencyNode(overlap.getRight());
			overlaps.add(Pair.of(left, right));
		}
	}
	
	public ModelAccessor saveMerge() {
		return workbench.saveModelAs();
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
			ModelAccessor modelfile = saveMerge();
			String mergedname = workbench.getMergedModelName();
			_models.add(new ModelInfo(workbench.getMergedModel(), modelfile, _models.size()));
			_commandSender.mergeCompleted(mergedname);
		}
		if (arg == MergeEvent.mappingadded) {	
			generateOverlapDescriptors();
			getOverlappingNodes();
		}
	}
	

	protected class MergerCommandReceiver extends CommunicatingWebBrowserCommandReceiver {

		public void onRequestOverlaps() {
			ArrayList<Overlap> overlaps = new ArrayList<Overlap>();
			for (Pair<DataStructureDescriptor, DataStructureDescriptor> dsd : dsdescriptors) {
				overlaps.add(new Overlap(dsd));
			}
			_commandSender.showOverlaps(overlaps.toArray(new Overlap[]{}));
		}
		public void onMinimizeTask() {
			switchTask(0);
		}
		
		public void onRequestPreview(Double index) {
			MergeChoice choices = new MergeChoice(overlaps.get(index.intValue()), getModelNodes());
			_commandSender.showPreview(choices);
		}

		public void onCreateCustomOverlap(String nodes, Double nodemodelindex) {
			String[] nodestolink = nodes.split(",");
			if (nodemodelindex.intValue()==0) {
				workbench.addManualCodewordMapping(nodestolink[0], nodestolink[1]);
			}
			else {
				workbench.addManualCodewordMapping(nodestolink[1], nodestolink[0]);
			}
		}

		public void onExecuteMerge(JSArray choicesmade) {
			ArrayList<ResolutionChoice> choicelist = new ArrayList<ResolutionChoice>();

			int ndomains = workbench.getSolutionDomainCount();
			for (int i = 0; i < ndomains; i++) {
				choicelist.add(ResolutionChoice.first);
			}
			
			for (int i=0; i<choicesmade.length(); i++) {
				int choice = choicesmade.get(i).asNumber().getInteger();
				switch(choice) {
				case 0:
					choicelist.add(ResolutionChoice.first);
					break;
				case 1:
					choicelist.add(ResolutionChoice.second);
					break;
				case 2:
					choicelist.add(ResolutionChoice.ignore);
					break;
				}
			}
			resolvers.mergeButtonAction(choicelist);

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
		public void onTaskClicked(String modelName, String task) {
			onTaskClicked(modelName, task, null);
		}

		public void onTaskClicked(String modelName, String task, JSObject snapshot) {
			// If the model doesn't exist throw an exception

			// Execute the proper task
			switch(task) {
				default:
					JOptionPane.showMessageDialog(null, "Task: '" + task +"', coming soon :)");
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
	public Task getTaskType() {
		return Task.MERGER;
	}
	
	public Class<MergerWebBrowserCommandSender> getSenderInterface() {
		return MergerWebBrowserCommandSender.class;
	}
	
	//Classes for passing information to the stage
	public class Overlap {
		@Expose public StageDSDescriptor dsleft;
		@Expose public StageDSDescriptor dsright; 
		
		protected Overlap(Pair<DataStructureDescriptor, DataStructureDescriptor> dsdesc) {
			dsleft = new StageDSDescriptor(dsdesc.getLeft());
			dsright = new StageDSDescriptor(dsdesc.getRight());
		}
	}
	
	public class StageDSDescriptor {
		@Expose public String name;
		@Expose public String type;
		@Expose public String description;
		@Expose public String annotation;
		@Expose public String equation;
		@Expose public String unit;

		protected StageDSDescriptor(DataStructureDescriptor dsdesc) {
			name = dsdesc.getDescriptorValue(Descriptor.name);
			type = dsdesc.getDescriptorValue(Descriptor.type);
			description = dsdesc.getDescriptorValue(Descriptor.description);
			annotation = dsdesc.getDescriptorValue(Descriptor.annotation);
			equation = dsdesc.getDescriptorValue(Descriptor.computationalcode);
			unit = dsdesc.getDescriptorValue(Descriptor.units);
		}
	}
}
