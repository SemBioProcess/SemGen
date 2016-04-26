package semgen.stage.stagetasks.merge;

import org.apache.commons.lang3.tuple.Pair;

import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class MergeChoice {
	private Submodel[] choices = new Submodel[]{null, null, null};

	public MergeChoice(Pair<DataStructure, DataStructure> dsoverlap, Pair<String, String> modelnames) {
		CenteredSubmodel left = new CenteredSubmodel(dsoverlap.getLeft(), modelnames.getLeft());
		CenteredSubmodel right = new CenteredSubmodel(dsoverlap.getRight(), modelnames.getRight());
		
		Submodel notrelated = new Submodel(left);
		for (DataStructure dstoadd : right.getAssociatedDataStructures()) {
			notrelated.addDataStructure(dstoadd);
		}
		
		DataStructure templeft = left.getFocusDataStructure().copy();
		choices[0] = generateChoice(left, right.getFocusDataStructure());
		choices[1] = generateChoice(right, templeft);
		choices[2] = notrelated;

	}
	

	private Submodel generateChoice(CenteredSubmodel base, DataStructure replacee) {
		CenteredSubmodel choice = new CenteredSubmodel(base);
		//choice.addUsedtoComputetoFocus(replacee);
		
		return choice;
	}
	
	public Submodel getChoice(int choice) {
		return choices[choice];
	}
}
