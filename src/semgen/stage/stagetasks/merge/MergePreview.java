package semgen.stage.stagetasks.merge;

/*
 * A class for storing and determining the consequences of the choices made for a merge.
 */

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.ModelOverlapMap;
import semsim.model.computational.datastructures.DataStructure;

public class MergePreview {
	ModelOverlapMap modelmap;
	 ArrayList<MergeChoice> nodestomerge = new ArrayList<MergeChoice>();
	 
	 public MergePreview(ModelOverlapMap map) {
		 modelmap = map;
		 generatePreview();
	 }
	 
	 private void generatePreview() {
		 for (Pair<DataStructure, DataStructure> overlap : modelmap.getDataStructurePairs()) {
			 nodestomerge.add(new MergeChoice(overlap));
		 }
	 }
}
