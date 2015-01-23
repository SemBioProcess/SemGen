package semgen.visualizations;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

/**
 * A browser that enables typed communication between java and javascript
 * 
 * How it works:
 * 1) TSender is an interface that defines the contract for communication from java to javascript.
 * 2) An instance of TSender is dynamically created.
 * 3) A script defining an object to facilitate handling commands in javascript is generated and inserted into the browser's DOM
 * 
 * - When functions on the TSender instance are executed corresponding handlers are executed in javascript.
 * - Javascript must register handlers to handle the commands from java.
 * 
 * 
 * As an example of the above let's take the following interface...
 * 
 * public interface CommandSender {
 * 		public void sendCommand( arg1 );
 * }
 * 
 * When we create the following communicating web browser...
 * 
 * browser = new CommunicatingWebBrowser<CommandSender>(CommandSender.class);
 * 
 * the following javascript is inserted into the <head> of the browser's DOM..
 * 
 * var javaCommandReciever = {
 * 		eventHandlers: {},
 *		registerEventHandler: function ( functionName, handler ) { this.eventHandlers[functionName] = handler; },
 *		executeHandler: function ( functionName ) { this.eventHandlers[functionName].apply(null, Array.prototype.slice.call(arguments, 1)); },
 *		onSendCommand: function (handler) { this.registerEventHandler('onLoadGraph', handler); },
 * };
 * 
 * Now  javascript can register handlers like this...
 * 
 * javaCommandReciever.onSendCommand( function( arg1 ) { ... } );
 * 
 * and java can send commands like this...
 * 
 * browser.getCommandSender().sendCommand( "some data" );
 * 
 * @author Ryan
 *
 * @param <TSender> - Type of interface used as a contract for communication between java and javascript
 */
@SuppressWarnings("serial")
public class CommunicatingWebBrowser<TSender> extends JWebBrowser {
	
	// Generator for command sender values
	private WebBrowserCommandSenderGenerator<TSender> _commandSenderGenerator;
	
	public CommunicatingWebBrowser(Class<TSender> senderInterface) {
		super();
		
		_commandSenderGenerator = new WebBrowserCommandSenderGenerator<TSender>(senderInterface, this, "javaCommandReciever");
		
		// Insert the initialization script into the header while the page is loading
		addWebBrowserListener(new InitializationWebBrowserAdapter());
	}
	
	public TSender getCommandSender() {
		return _commandSenderGenerator.getSender();
	}
	
	/**
	 * Inserts an initialization script into the page header while the page is loading
	 * @author Ryan
	 *
	 */
	private class InitializationWebBrowserAdapter extends WebBrowserAdapter {
		
		/**
		 * When the page is loading this function inserts an initialization script
		 * into the page's header
		 * 
		 * NOTE: We should fire an event after this script is inserted that javascript can listen for.
		 * Until then javascript can listen for the window.onload event.
		 * However, I'm not sure if we'll run into timing issues with the current approach.
		 */
		@Override
		public void loadingProgressChanged(WebBrowserEvent e) {
			// We don't need to listen for anymore events
			e.getWebBrowser().removeWebBrowserListener(this);
			
			// Insert a script element into the page header that defines an object that receives commands.
			// the page is responsible for registering handlers for those commands
			String initializationScript = 
					"var head = document.getElementsByTagName('head')[0];" + CommunicationHelpers.NL +
					"var script = document.createElement('script');" + CommunicationHelpers.NL +
					"script.type = 'text/javascript';" + CommunicationHelpers.NL +
					"script.innerHTML = \"" + _commandSenderGenerator.generateJavascript() + "\";" + CommunicationHelpers.NL +
					"head.appendChild(script);";
			
			// Execute the initialization script
			CommunicatingWebBrowser.this.executeJavascript(initializationScript);
		}
	}
}
