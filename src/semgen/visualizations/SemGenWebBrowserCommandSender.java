package semgen.visualizations;

import semgen.stage.serialization.DependencyNode;
import semgen.stage.serialization.PhysioMapNode;
import semgen.stage.serialization.SearchResultSet;
import semgen.stage.serialization.SubModelNode;

/**
 * Contract for sending commands between java and javascript
 * 
 * Note: This interface should not be implemented.
 * An instance of this interface will be dynamically implemented by CommunicatingWebBrowser.
 * 
 * @author Ryan
 *
 */
public interface SemGenWebBrowserCommandSender {
	
	/**
	 * Sends the add model command
	 * 
	 * @param testString
	 */
	void addModel(String modelName);
	
	
	void removeModel(String modelName);
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

	/**
	 * Sends search results from multiple sources to JavaScript
	 * @param resultSets Array containing search results from multiple sources
	 */
	void search(SearchResultSet[] resultSets);

	/**
	 * Tell the browser to render the PhysioMap
	 * 
	 * @param modelName name of model
	 * @param jsonPhysioMap PhysioMap
	 */
	void showPhysioMapNetwork(String modelName, PhysioMapNode[] jsonPhysioMap);

	/**
	 * 
	 * @param Answer query
	 */
	void receiveReply(String reply);
}
