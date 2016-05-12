package semgen.stage.stagetasks.merge;

import semgen.stage.serialization.DependencyNode;
import semgen.stage.serialization.MergePreviewSubmodels;
import semgen.stage.serialization.SubModelNode;
import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;
import semgen.stage.stagetasks.merge.MergerTask.Overlap;

public interface MergerWebBrowserCommandSender extends SemGenWebBrowserCommandSender{
	
	public void showOverlaps(Overlap[] overlaps);
	public void showPreview(MergePreviewSubmodels preview);
	
	/**
	 * Tell the browser to render the dependencies
	 * 
	 * @param modelName Name of model
	 * @param jsonDependencies Dependencies
	 */
	void showDependencyNetwork(String modelName, DependencyNode[] jsonDependencies);
	
	/**
	 * Tell the browser to render the submodel dependencies
	 * 
	 * @param modelName name of parent model
	 * @param jsonSubmodelNetwork submodel network
	 */
	void showSubmodelNetwork(String modelName, SubModelNode[] jsonSubmodelNetwork);

}
