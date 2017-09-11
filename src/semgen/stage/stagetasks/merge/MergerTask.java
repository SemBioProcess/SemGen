package semgen.stage.stagetasks.merge;

import com.google.gson.annotations.Expose;
import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;
import org.apache.commons.lang3.tuple.Pair;
import semgen.merging.DataStructureDescriptor;
import semgen.merging.DataStructureDescriptor.Descriptor;
import semgen.merging.Merger.ResolutionChoice;
import semgen.merging.MergerWorkbench;
import semgen.merging.MergerWorkbench.MergeEvent;
import semgen.merging.ModelOverlapMap.MapType;
import semgen.stage.serialization.DependencyNode;
import semgen.stage.serialization.StageState;
import semgen.stage.stagetasks.ModelInfo;
import semgen.stage.stagetasks.StageRootInfo;
import semgen.stage.stagetasks.StageTask;
import semgen.utilities.SemGenError;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelAccessor;

import javax.swing.*;
import java.util.*;

public class MergerTask extends StageTask<MergerWebBrowserCommandSender> implements Observer {
	private MergerWorkbench workbench = new MergerWorkbench();
	private ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>> jsoverlaps;
	
	private ArrayList<Pair<DependencyNode, DependencyNode>> overlaps = new ArrayList<Pair<DependencyNode, DependencyNode>>();
	private MergeConflicts conflicts = new MergeConflicts();
	private ArrayList<UnitConflict> unitpairs = new ArrayList<UnitConflict>();
	private ArrayList<Integer> choices = new ArrayList<Integer>();
	
	public MergerTask(ArrayList<StageRootInfo<?>> modelinfo, int index) {
		super(index);
		workbench.addObserver(this);
		_commandReceiver = new MergerCommandReceiver();
		ArrayList<ModelAccessor> files = new ArrayList<ModelAccessor>();
		ArrayList<SemSimModel> models = new ArrayList<SemSimModel>();
		
		for (StageRootInfo<?> model : modelinfo) {
			ModelInfo info = new ModelInfo(model, _models.size());
			_models.add(info);
			files.add(info.accessor);
			models.add(info.Model);
		}
		workbench.addModels(files, models, true);
		primeForMerging();
		for (int i=0; i< workbench.getMappingCount(); i++) {
			choices.add(-1);
		}
		state = new StageState(Task.MERGER, _models, index);

	}

	private void primeForMerging() {
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
		
		getOverlappingNodes();
		generateOverlapDescriptors();
		collectConflicts();
	}

	private void generateOverlapDescriptors() {
		int n = workbench.getSolutionDomainCount();
		ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>> descriptors = new ArrayList<Pair<DataStructureDescriptor, DataStructureDescriptor>>();
		
		for (int i = n; i < (workbench.getMappingCount()); i++) {
			descriptors.add(workbench.getDSDescriptors(i));
		}
		jsoverlaps = descriptors;
	}
	
	private void getOverlappingNodes() {
		overlaps.clear();
		ArrayList<Pair<DataStructure,DataStructure>> dsoverlaps = workbench.getOverlapPairs();
		
		int n = workbench.getSolutionDomainCount();
		for (int i = n; i < (workbench.getMappingCount()); i++) {
			Pair<DataStructure,DataStructure> overlap = dsoverlaps.get(i);
			DependencyNode left = _models.get(0).modelnode.getDependencyNode(overlap.getLeft());
			DependencyNode right = _models.get(1).modelnode.getDependencyNode(overlap.getRight());
			
			overlaps.add(Pair.of(left, right));
			//Add a link to the graph
			left.addLink(right);
		}
	}
			
	private void collectConflicts() {
		conflicts.clearConflicts();
		ArrayList<Boolean> units = workbench.getUnitOverlaps();
		for (int i=0; i<units.size(); i++) {
			checkUnitConflict(i, units.get(i));
			
		}
		HashMap<String, String> smoverlaps = workbench.createIdenticalSubmodelNameMap();
		
		for (String overlap : smoverlaps.keySet()) {
			conflicts.dupesubmodels.add(new SyntacticDuplicate(overlap));
		}
		
		HashMap<String, String> cwoverlaps = workbench.createIdenticalNameMap();
		
		for (String overlap : cwoverlaps.keySet()) {
			
			//If the codeword name uses the submodel.codeword convention, 
			// don't include if renaming the submodel would resolve the codeword name overlap.
			if(overlap.contains(".")){
				
				String submodelname = overlap.substring(0, overlap.lastIndexOf("."));
				boolean smresolves = false;
				for (String smoverlap : smoverlaps.keySet()) {
					if (smoverlap.equals(submodelname)) {
						smresolves = true;
						break;
					}
				}
				if (smresolves) continue;
			}
			
			conflicts.dupecodewords.add(new SyntacticDuplicate(overlap));
		}
	}
	
	private void checkUnitConflict(int i, boolean doesconflict) {
		UnitConflict conflict = null;
		if (!doesconflict) {
			Pair<DataStructureDescriptor, DataStructureDescriptor> descs = workbench.getDSDescriptors(i);
			String leftunit = descs.getLeft().getDescriptorValue(Descriptor.units);
			String rightunit = descs.getRight().getDescriptorValue(Descriptor.units);
			for (UnitConflict con : conflicts.unitconflicts) {
				if (leftunit.equalsIgnoreCase(con.unitleft) && rightunit.equalsIgnoreCase(con.unitright)) {
					con.incrementPairCount();
					unitpairs.add(con);
					return;
				}
			}
			conflict = new UnitConflict(descs);
			conflicts.unitconflicts.add(conflict);
		}
		unitpairs.add(conflict);
	}
	
	private ModelAccessor saveMerge() {
		return workbench.saveModelAs(0);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg == MergeEvent.threemodelerror) {
			SemGenError.showError("SemGen can only merge two models at a time.", "Too many models");
		}	
		if (arg == MergeEvent.modelerrors) {
			JOptionPane.showMessageDialog(null, "Model " + ((MergeEvent)arg).getMessage() + " has errors.",
					"Failed to analyze.", JOptionPane.ERROR_MESSAGE);
		}
		if (arg == MergeEvent.mergecompleted) {
			ModelAccessor modelfile = saveMerge();
			
			ModelInfo mergedmodel = new ModelInfo(workbench.getMergedModel(), modelfile, _models.size());
			_models.add(mergedmodel);
			_commandSender.mergeCompleted(mergedmodel.modelnode, workbench.getModelSaved());
		}
		if (arg == MergeEvent.mappingevent) {
			generateOverlapDescriptors();
			getOverlappingNodes();
			_commandSender.showOverlaps(updateOverlaps().toArray(new Overlap[]{}));
		}
	}
	
	private ArrayList<Overlap> updateOverlaps() {
		ArrayList<Overlap> overlaps = new ArrayList<Overlap>();
		int i = workbench.getSolutionDomainCount();
		for (Pair<DataStructureDescriptor, DataStructureDescriptor> dsd : jsoverlaps) {
			Overlap overlap = new Overlap(dsd);
			overlap.similar = workbench.getMapPairType(i).equals(MapType.SEMANTICALLY_SIMILAR);
			overlap.custom = workbench.getMapPairType(i).equals(MapType.MANUAL_MAPPING);
			overlap.choice = choices.get(i);
			overlaps.add(overlap);
			i++;
		}
		return overlaps;
	}
	
	private ArrayList<MappingCandidate> getMappingCandidates(int modelindex) {
		HashSet<DependencyNode> depnodes = this._models.get(modelindex).modelnode.requestAllChildDependencies();
		
		ArrayList<MappingCandidate> candidates = new ArrayList<MappingCandidate>();
		for (DependencyNode dnode : depnodes) {
			if (dnode.issubmodelinput) continue;
			boolean mapped = false;
			int mappedhash = -1;
			int i = workbench.getSolutionDomainCount();
			for (Pair<DependencyNode, DependencyNode> overlap : this.overlaps) {
				DependencyNode mappeddepnode = modelindex == 0 ? overlap.getLeft() : overlap.getRight();
				DependencyNode othermappeddepnode = modelindex == 0 ? overlap.getRight() : overlap.getLeft();
				if (mappeddepnode==dnode) {
					if (workbench.getMapPairType(i) == MapType.MANUAL_MAPPING) {
						mappedhash = othermappeddepnode.hash;
					}
					else mapped = true;
					break;
				}
				i++;
			}
			if (!mapped) {
				MappingCandidate candidate = new MappingCandidate(dnode);
				candidate.linkedhash = mappedhash;
				candidates.add(candidate);
			}
			
		}

		Collections.sort(candidates, new MappingComparator());

		return candidates;
	}


	public class MergerCommandReceiver extends CommunicatingWebBrowserCommandReceiver {

		public void onInitialized(JSObject jstaskobj) {
			jstask = jstaskobj;
			jstask.setProperty("conflictsj", new MergerBridge());
		}
		
		public void onRequestConflicts() {
			_commandSender.showConflicts(conflicts);
		}
		
		public void onRequestOverlaps() {
			_commandSender.showOverlaps(updateOverlaps().toArray(new Overlap[]{}));
			
		}
		
		public void onChangeTask(Double index) {
			switchTask(index.intValue());
		}
		
		public void onRequestPreview(Double index) {
			MergeChoice choices = new MergeChoice(overlaps.get(index.intValue()), getModelNodes());
			_commandSender.showPreview(choices);
		}
		
		//Model id has to be included because we don't know the which model's node is being passed in
		public void onCreateCustomOverlap(String nodes) {
			String[] nodestolink = nodes.split(",");
			String firstnodeid = _models.get(0).modelnode.getNodebyId(nodestolink[0]).getSourceObjectName();
			String secondnodeid = _models.get(1).modelnode.getNodebyId(nodestolink[1]).getSourceObjectName();
			
			choices.add(-1);
			workbench.addManualCodewordMapping(firstnodeid, secondnodeid);
			ArrayList<Boolean> units = workbench.getUnitOverlaps();
			
			int i = units.size()-1;
			checkUnitConflict(i, units.get(i));
		}
		
		public void onRemoveCustomOverlap(Double customindex) {
			int intval = customindex.intValue();
			Pair<DependencyNode, DependencyNode> ol = overlaps.get(intval);
			workbench.removeManualCodewordMapping(intval+workbench.getSolutionDomainCount());
			generateOverlapDescriptors();
			getOverlappingNodes();
			conflicts.decrementUnitConflict(unitpairs.get(intval));
			unitpairs.remove(intval);
			choices.remove(customindex);
			_commandSender.clearLink(updateOverlaps().toArray(new Overlap[]{}), ol.getLeft().id, ol.getRight().id);
			
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
			
			SemGenProgressBar progframe = new SemGenProgressBar("Merging...", true);
			String error = workbench.executeMerge(conflicts.buildCodewordNameMap(), 
					conflicts.buildSubmodelNameMap(), 
					choicelist, 
					conflicts.buildConversionList(), 
					progframe);
			
			if (error!=null){
				SemGenError.showError(
						"ERROR: " + error, "Merge Failed");
			}

		}
		
		public void onGetManualMappingCandidates() {
			ArrayList<ArrayList<MappingCandidate>> candidates = new ArrayList<ArrayList<MappingCandidate>>();
			
			candidates.add(getMappingCandidates(0));
			candidates.add(getMappingCandidates(1));
			_commandSender.showMappingCandidates(candidates);
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

		public void onSendModeltoStage() {
			queueModel(_models.get(2));
		}
		
		public void onSave() {
			if (workbench.saveModel(0) != null) _commandSender.saved(true);
		}
		
		public void onSaveandClose() {
			if (workbench.saveModel(0) != null) {
				closeTask();
			}
		}
		
		public void onClose() {
				closeTask();
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
	public Task getTaskType() {
		return Task.MERGER;
	}
	
	public Class<MergerWebBrowserCommandSender> getSenderInterface() {
		return MergerWebBrowserCommandSender.class;
	}
	
	//Classes for passing information to the stage
	class Overlap {
		@Expose public StageDSDescriptor dsleft;
		@Expose public StageDSDescriptor dsright;
		@Expose public Boolean similar = false;
		@Expose public Boolean custom = false;
		@Expose public int choice = -1;
		
		protected Overlap(Pair<DataStructureDescriptor, DataStructureDescriptor> dsdesc) {
			dsleft = new StageDSDescriptor(dsdesc.getLeft());
			dsright = new StageDSDescriptor(dsdesc.getRight());
		}
	}
	
	private class StageDSDescriptor {
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
	
	class MergeConflicts {
		@Expose public ArrayList<SyntacticDuplicate> dupecodewords = new ArrayList<SyntacticDuplicate>();
		@Expose public ArrayList<SyntacticDuplicate> dupesubmodels = new ArrayList<SyntacticDuplicate>();
		@Expose public ArrayList<UnitConflict> unitconflicts = new ArrayList<UnitConflict>();
		
		public HashMap<String,String> buildSubmodelNameMap() {
			HashMap<String,String> dupemap = new HashMap<String, String>();
			for (SyntacticDuplicate dsm : dupesubmodels) {
				dupemap.put(dsm.duplicate, dsm.replacement);
			}
			
			return dupemap;
		}
		
		public HashMap<String,String> buildCodewordNameMap() {
			HashMap<String,String> dupemap = new HashMap<String, String>();
			for (SyntacticDuplicate dcw : dupecodewords) {
				dupemap.put(dcw.duplicate, dcw.replacement);
			}
			
			return dupemap;
		}

		public ArrayList<Pair<Double,String>> buildConversionList() {
			ArrayList<Pair<Double,String>> conversions = new ArrayList<Pair<Double,String>>();
			for (UnitConflict uc : unitpairs) {
				if (uc==null) {
					conversions.add(Pair.of(1.0, "*"));
				}
				else {
					conversions.add(uc.getConversion());
				}
			}
			
			return conversions;
		}
		
		public void clearConflicts() {
			dupesubmodels.clear();
			dupecodewords.clear();
			unitconflicts.clear();
		}
		
		public void decrementUnitConflict(UnitConflict conflict) {
			if (conflict.decrementPairCount()) {
				unitconflicts.remove(conflict);
			}
		}
	}
	
	private class SyntacticDuplicate {
		@Expose public String duplicate;
		@Expose public String replacement = "";
		@Expose public String modelname;
		@Expose public boolean userightmodel = true;
		
		protected SyntacticDuplicate(String dupe) {
			duplicate = dupe;
			modelname = workbench.getModelName(1);
		}
		
		public void setReplacementName(boolean rightmodel, String rep) {
			replacement = rep;
			userightmodel = rightmodel;
			
		}
	}
	
	private class UnitConflict {
		@Expose public String unitleft;
		@Expose public String unitright;
		@Expose public boolean multiply = true;
		@Expose public Float conversion = 1.0f;
		private int numpairs = 1;
		
		protected UnitConflict(Pair<DataStructureDescriptor, DataStructureDescriptor> descs) {
			unitleft = descs.getLeft().getDescriptorValue(Descriptor.units);
			unitright = descs.getRight().getDescriptorValue(Descriptor.units);

		}
		
		public void setConversion(Float val, boolean mult) {
			conversion = val;
			multiply = mult;
		}
		
		public Pair<Double, String> getConversion() {
			String operator = "*";
			if (!multiply) operator = "/"; 
			return Pair.of(conversion.doubleValue(), operator);
		}
		
		public void incrementPairCount() {
			numpairs++;
		}
		
		public boolean decrementPairCount() {
			numpairs--;
			return numpairs == 0;
		}
	}
	
	public class MergerBridge {
		public void setResolutionChoice(Integer index, Integer choice) {
			
			choices.set(index + workbench.getSolutionDomainCount(), choice);
		}
		public void setUnitConversion(Integer index, boolean multiply, String conversion) {
			conflicts.unitconflicts.get(index).setConversion(Float.valueOf(conversion), multiply);
		}
		
		public void setSubmodelName(Integer index, boolean rightmodel, String name) {
			conflicts.dupesubmodels.get(index).setReplacementName(rightmodel, name);
		}
		
		public void setCodewordName(Integer index, boolean rightmodel, String name) {
			conflicts.dupecodewords.get(index).setReplacementName(rightmodel, name);
		}
		
	} 
	
	public class MappingCandidate {
		@Expose String name;
		@Expose int hash;
		@Expose int linkedhash = -1;
		@Expose String id;
		@Expose String modelname;
		
		public MappingCandidate(DependencyNode depnode) {
			this.name = depnode.name;
			this.hash = depnode.hash;
			this.id = depnode.id;
			this.modelname = depnode.parentModelId;
		}
		
	}
	public class MappingComparator implements Comparator<MappingCandidate> {
		public int compare(MappingCandidate A, MappingCandidate B) {
		    return A.name.compareToIgnoreCase(B.name);
		  }
	}

}
