package semgen.visualizations;

/**
 * Contact for sending commands between java and javascript
 * 
 * Note: This interface should not be implemented.
 * An instance of this interface will be dynamically implemented by CommunicatingWebBrowser.
 * 
 * @author Ryan
 *
 */
public interface D3WebBrowserCommandSender {
	
	/**
	 * Command javascript to load the d3 graph with the given data
	 * @param graphData
	 */
	void loadGraph(String graphData);
}
