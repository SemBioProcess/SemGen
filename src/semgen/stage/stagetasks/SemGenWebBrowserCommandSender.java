package semgen.stage.stagetasks;

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
	 * 
	 * @param Answer query
	 */
	void receiveReply(Object reply);
	
	/**
	 * 
	 * @param Change task
	 */
	void changeTask(String taskname);
	
}
