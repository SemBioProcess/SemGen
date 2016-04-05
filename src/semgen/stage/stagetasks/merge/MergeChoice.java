package semgen.stage.stagetasks.merge;

import org.apache.commons.lang3.tuple.Pair;

import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class MergeChoice {
	private Submodel[] choices = new Submodel[]{null, null, null};

	
	public MergeChoice(Pair<DataStructure, DataStructure> dsoverlap) {
		CenteredSubmodel left = new CenteredSubmodel(dsoverlap.getLeft());
		CenteredSubmodel right = new CenteredSubmodel(dsoverlap.getRight());
		
		choices[0] = generateChoice(left, right.getFocusDataStructure());
		choices[1] = generateChoice(right, left.getFocusDataStructure());
		
		Submodel notrelated = new Submodel(left);
		for (DataStructure dstoadd : right.getAssociatedDataStructures()) {
			notrelated.addDataStructure(dstoadd);
		}
		choices[2] = notrelated;
	}
	

	private Submodel generateChoice(CenteredSubmodel base, DataStructure replacee) {
		CenteredSubmodel choice = new CenteredSubmodel(base);
		choice.getFocusDataStructure().addUsedToCompute(replacee.getUsedToCompute());
		
		for (DataStructure dstoadd : replacee.getUsedToCompute()) {
			choice.addDataStructure(dstoadd);
		}
		return choice;
	}
}
