package semgen.visualizations;

import java.util.Set;

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
	
	/**
	 * Tell the browser to render the dependencies
	 * 
	 * @param modelName Name of model
	 * @param jsonDependencies Dependencies
	 */
	void showDependencyNetwork(String modelName, JsonString jsonDependencies);
	
	/**
	 * Tell the browser to render the submodel dependencies
	 * 
	 * @param modelName name of parent model
	 * @param jsonSubmodelNetwork submodel network
	 */
	void showSubmodelNetwork(String modelName, JsonString jsonSubmodelNetwork);

	void search(Set<String> searchResults);
}
