package semgen.stage.stagetasks.merge;

/*
 * A class for storing and determining the consequences of the choices made for a merge.
 */

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.ModelOverlapMap;
import semgen.stage.serialization.MergePreviewSubmodels;
import semsim.model.computational.datastructures.DataStructure;

public class MergePreview {
	private ModelOverlapMap modelmap;
	private ArrayList<MergeChoice> nodestomerge = new ArrayList<MergeChoice>();
	 
	 public MergePreview(ModelOverlapMap map, Pair<String, String> names) {
		 modelmap = map;
		 generatePreview(names);
	 }
	 
	 private void generatePreview(Pair<String, String> names) {
		 for (Pair<DataStructure, DataStructure> overlap : modelmap.getDataStructurePairs()) {
			 nodestomerge.add(new MergeChoice(overlap, names));
		 }
	 }
	 
	 public MergePreviewSubmodels getPreviewSerializationforSelection(Number index) {
		 return new MergePreviewSubmodels(nodestomerge.get(index.intValue()));
	 }
}
