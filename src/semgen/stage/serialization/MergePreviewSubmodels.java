package semgen.stage.serialization;

import com.google.gson.annotations.Expose;

import semgen.stage.stagetasks.merge.MergeChoice;

public class MergePreviewSubmodels {
	@Expose public SubModelNode left;
	@Expose public SubModelNode middle;
	@Expose public SubModelNode right;
	
	public MergePreviewSubmodels(MergeChoice choices) {
		left = new SubModelNode(choices.getChoice(0));
		middle = new SubModelNode(choices.getChoice(2));
		right = new SubModelNode(choices.getChoice(1));
	}
}
