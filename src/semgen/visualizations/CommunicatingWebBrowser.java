package semgen.visualizations;

import javax.naming.InvalidNameException;

import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

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
public class CommunicatingWebBrowser extends Browser {
	
	// Name of the variable in javascript that receives commands
	private final String JavascriptCommandReceiverVariableName = "javaCommandReciever";
	
	// Name of the variable in javascript that sends commands
	private final String JavascriptCommandSenderVariableName = "javaCommandSender";
	
	// Javascript needs to listen for this event to learn when the javascript is loaded
	private final String InitializationEventName = "cwb-initialized";
	
	private final String javajsbridgeid = "javaJSBridge";
	
	// This function is called to let javascript developers know when the browser has initialized.
	private final String TriggerInitializationEventScript =
			"function cwb_triggerInitialized(receiver, sender) {" + CommunicationHelpers.NLJS +
				"var event;" + CommunicationHelpers.NLJS +
				"if (document.createEvent) {" + CommunicationHelpers.NLJS +
					"event = document.createEvent('HTMLEvents');" + CommunicationHelpers.NLJS +
					"event.initEvent('" + InitializationEventName + "', true, true);" + CommunicationHelpers.NLJS +
				"}" + CommunicationHelpers.NLJS +
				"else {" + CommunicationHelpers.NLJS +
					"event = document.createEventObject();" + CommunicationHelpers.NLJS +
					"event.eventType = '" + InitializationEventName + "';" + CommunicationHelpers.NLJS +
				"}" + CommunicationHelpers.NLJS +
				
				"event.eventName = '" + InitializationEventName + "';" + CommunicationHelpers.NLJS +
				"event.commandSender = sender;" + CommunicationHelpers.NLJS +
				"event.commandReceiver = receiver;" + CommunicationHelpers.NLJS +
	
				"if (document.createEvent) {" + CommunicationHelpers.NLJS +
					"window.dispatchEvent(event);" + CommunicationHelpers.NLJS +
				"}" + CommunicationHelpers.NLJS +
				"else {" + CommunicationHelpers.NLJS +
					"window.fireEvent('on' + event.eventType, event);" + CommunicationHelpers.NLJS +
				"}" + CommunicationHelpers.NLJS +
			"}";
	
	private final String SetCommunicatorsScript = 
			"function setCommunicators(receivr, sendr) {" + CommunicationHelpers.NLJS +
			"	sender = sendr;" + CommunicationHelpers.NLJS +
			"	receiver = receivr;" + CommunicationHelpers.NLJS +
			"}";
	
	// Executes javascript and handles errors
	private final String ExecuteJavascriptAndHandleErrorsScript = 
			"function executeAndHandleErrors(func) {" + CommunicationHelpers.NLJS +
				"try {" + CommunicationHelpers.NLJS +
					"func();" + CommunicationHelpers.NLJS +
				"} catch (e) { " + CommunicationHelpers.NLJS +
					"alert('Error executing javascript: ' + e.message);" + CommunicationHelpers.NLJS +
				"}" + CommunicationHelpers.NLJS +
			"}";
	
	// Generator for command sender values
	private WebBrowserCommandSenderGenerator<?> _commandSenderGenerator;
	
	// Command receiver
	private CommunicatingWebBrowserCommandReceiver _commandReceiver;
	
	public CommunicatingWebBrowser(Class<? extends SemGenWebBrowserCommandSender> commandSenderInterface, CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException {
		super();
		createBrowserListeners(commandSenderInterface, commandReceiver);

		// Insert the adapter that facilitates communication
		addLoadListener(new CommunicatingWebBrowserLoadAdapter());
	}

	protected String setBrowserListeners(Class<? extends SemGenWebBrowserCommandSender> commandSenderInterface, CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException {
		createBrowserListeners(commandSenderInterface, commandReceiver);
	
		String scriptInnerHtml = generateReceiverandSenderHtml() + 
				SetCommunicatorsScript + CommunicationHelpers.NLJS +
				ExecuteJavascriptAndHandleErrorsScript;
		
		// Insert a script element into the page header that defines an object that receives commands.
		// the page is responsible for registering handlers for those commands
		String changescript = CommunicationHelpers.removeScriptbyID(javajsbridgeid) +
				CommunicationHelpers.appendScript(scriptInnerHtml, javajsbridgeid) +
				"setCommunicators(" + JavascriptCommandReceiverVariableName + ", " + JavascriptCommandSenderVariableName + ");";
		return changescript;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createBrowserListeners(Class<? extends SemGenWebBrowserCommandSender> commandSenderInterface, CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException {
		_commandSenderGenerator = new WebBrowserCommandSenderGenerator(commandSenderInterface,
				this,
				JavascriptCommandReceiverVariableName);

		if(commandReceiver != null){
			_commandReceiver = commandReceiver;
			_commandReceiver.validate();
			listenForCommands();
		}
	}
	
	private void listenForCommands() {
		_commandReceiver.listenForBrowserCommands(this);
	}
	
	public WebBrowserCommandSenderGenerator<?> getCommandSenderGenerator() {
		return _commandSenderGenerator;
	}

	/**
	 * Executes the given javascript and handles errors.
	 * @param javascript Javasript to execute
	 */
	public void executeJavascriptAndHandleErrors(String javascript) {
		// Execute the passed in javascript from within a function that handles errors
		javascript = String.format("executeAndHandleErrors(function () { %s });", javascript);
		executeJavaScript(javascript);
	}
	
	private String generateReceiverandSenderHtml() {
		// Get the script for the command receiver
		String javascriptCommandReceiver = _commandSenderGenerator.generateJavascriptReceiver();
		
		// If there's a command receiver get it's corresponding sender in javascript
		// Otherwise, use a dummy object
		String javascriptCommandSender = "var " + JavascriptCommandSenderVariableName + " = null;";
		if(_commandReceiver != null)
			javascriptCommandSender = _commandReceiver.generateJavascriptSender(JavascriptCommandSenderVariableName);
		
		// Stitch together the script html
		return	javascriptCommandReceiver + CommunicationHelpers.NLJS +
				javascriptCommandSender + CommunicationHelpers.NLJS;
	}
	
	/**
	 * Inserts an initialization script into the page header while the page is loading.
	 * This allows java and javascript to communicate using an agreed upon contract.
	 *
	 * @author Ryan
	 *
	 */
	private class CommunicatingWebBrowserLoadAdapter extends LoadAdapter {
		
		/**
		 * When the page is loaded this function inserts an initialization script
		 * into the page's header
		 */
		@Override
		public void onFinishLoadingFrame(FinishLoadingEvent e) {
			
			// If this is not the main frame return
			if(!e.isMainFrame())
				return;

			// We don't need to listen for anymore events
			e.getBrowser().removeLoadListener(this);
			listenForCommands();
			
			// Stitch together the script html
			String scriptInnerHtml = generateReceiverandSenderHtml() +
					TriggerInitializationEventScript + CommunicationHelpers.NLJS +
					ExecuteJavascriptAndHandleErrorsScript;
			
			// Insert a script element into the page header that defines an object that receives commands.
			// the page is responsible for registering handlers for those commands
			String initializationScript = CommunicationHelpers.appendScript(scriptInnerHtml, javajsbridgeid) +
					"cwb_triggerInitialized(" + JavascriptCommandReceiverVariableName + ", " + JavascriptCommandSenderVariableName + ");";

			// Execute the initialization script
			e.getBrowser().executeJavaScript(initializationScript);
		}
	}
	
}
