package semgen.stage.stagetasks.merge;

import java.util.ArrayList;

import semgen.stage.serialization.ModelNode;
import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;
import semgen.stage.stagetasks.merge.MergerTask.MappingCandidate;
import semgen.stage.stagetasks.merge.MergerTask.MergeConflicts;
import semgen.stage.stagetasks.merge.MergerTask.Overlap;

public interface MergerWebBrowserCommandSender extends SemGenWebBrowserCommandSender{
	
	public void showConflicts(MergeConflicts conflicts);
	public void showOverlaps(Overlap[] overlaps);
	public void showPreview(MergeChoice preview);
	public void saved(Boolean wassaved);
	/**
	 * Tell the browser the merge is completed
	 * 
	 * @param modelName Name of model
	 */
	public void mergeCompleted(ModelNode modelnode, Boolean wassaved);
	public void clearLink(Overlap[] overlaps, String leftid, String rightid);
	public void showMappingCandidates(ArrayList<ArrayList<MappingCandidate>> candidates);
}
