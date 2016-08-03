package semgen.stage.stagetasks.merge;

import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;
import semgen.stage.stagetasks.merge.MergerTask.MergeConflicts;
import semgen.stage.stagetasks.merge.MergerTask.Overlap;

public interface MergerWebBrowserCommandSender extends SemGenWebBrowserCommandSender{
	
	public void showConflicts(MergeConflicts conflicts);
	public void showOverlaps(Overlap[] overlaps);
	public void showPreview(MergeChoice preview);
	
	/**
	 * Tell the browser the merge is completed
	 * 
	 * @param modelName Name of model
	 */
	public void mergeCompleted(String mergedName);
}
