package semgen.visualizations;

/**
 * Contract for receiving commands from Javascript
 * 
 * Note: All methods must start with "on" to indicate that the function is an event handler
 * 
 * @author Ryan
 *
 */
public abstract class SemGenWebBrowserCommandReceiver extends CommunicatingWebBrowserCommandReceiver {

	/**
	 * Receives the add model command
	 */
	public abstract void onAddModel();
}
