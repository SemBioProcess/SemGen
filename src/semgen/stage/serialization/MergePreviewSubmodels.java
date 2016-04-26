package semgen.stage.serialization;

import semgen.stage.stagetasks.merge.MergeChoice;

public class MergePreviewSubmodels {
	public SubModelNode left;
	public SubModelNode middle;
	public SubModelNode right;
	
	public MergePreviewSubmodels(MergeChoice choices) {
		left = new SubModelNode(choices.getChoice(0));
		middle = new SubModelNode(choices.getChoice(2));
		right = new SubModelNode(choices.getChoice(1));
	}
}
