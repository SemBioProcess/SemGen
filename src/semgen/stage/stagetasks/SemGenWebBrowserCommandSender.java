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
	 * Answer query
	 * @param reply 
	 */
	void receiveReply(Object reply);
	
	/**
	 * Change task
	 * @param taskname Name of task to change to 
	 */
	void changeTask(String taskname);
	
}
