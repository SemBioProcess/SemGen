package semgen.stage.stagetasks.merge;

import org.apache.commons.lang3.tuple.Pair;

import semgen.stage.serialization.SubModelNode;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class MergeChoice {
	private SubModelNode[] choices = new SubModelNode[]{null, null, null};

	public MergeChoice(Pair<DataStructure, DataStructure> dsoverlap, Pair<String, String> modelnames) {
		//Four models have to be generated per choice. One to be used as the base of a choice and the other
		//to provide the objects for the other choice.
		CenteredSubmodel left = new CenteredSubmodel(new CenteredSubmodel(dsoverlap.getLeft(), modelnames.getLeft()), modelnames.getLeft());
		CenteredSubmodel lefttodiscard = new CenteredSubmodel(dsoverlap.getLeft(), modelnames.getLeft());
		CenteredSubmodel right = new CenteredSubmodel(new CenteredSubmodel(dsoverlap.getRight(), modelnames.getRight()), modelnames.getRight());
		CenteredSubmodel righttodiscard = new CenteredSubmodel(dsoverlap.getRight(), modelnames.getRight());
		
		CenteredSubmodel centerleft = new CenteredSubmodel(dsoverlap.getLeft(), modelnames.getLeft());
		CenteredSubmodel centerright = new CenteredSubmodel(dsoverlap.getRight(), modelnames.getRight());
		//The submodel for the ignore option
		CenteredSubmodel notrelated = new CenteredSubmodel(centerleft, "center");
		
		notrelated.addSubmodel(new CenteredSubmodel(centerright));
		


	}
	

	private Submodel generateChoice(CenteredSubmodel base, CenteredSubmodel replacee) {
		base.addUsedtoComputetoFocus(replacee.getFocusDataStructure(), replacee.getName());
		
		return base;
	}
	
	public Submodel getChoice(int choice) {
		return null;
	}
}
