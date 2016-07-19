package semgen.stage.stagetasks.merge;

import semgen.stage.serialization.MergePreviewSubmodels;
import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;
import semgen.stage.stagetasks.merge.MergerTask.Overlap;

public interface MergerWebBrowserCommandSender extends SemGenWebBrowserCommandSender{
	
	public void showOverlaps(Overlap[] overlaps);
	public void showPreview(MergePreviewSubmodels preview);
	
	/**
	 * Tell the browser the merge is completed
	 * 
	 * @param modelName Name of model
	 */
	public void mergeCompleted(String mergedName);
}
