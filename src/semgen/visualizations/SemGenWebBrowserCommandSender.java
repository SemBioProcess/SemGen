package semgen.visualizations;

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
}
