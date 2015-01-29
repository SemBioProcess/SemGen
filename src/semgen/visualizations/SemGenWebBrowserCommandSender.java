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
	 * Sends a test string to the browser. This will eventually be removed.
	 * 
	 * @param testString
	 */
	void test(String testString);
}
