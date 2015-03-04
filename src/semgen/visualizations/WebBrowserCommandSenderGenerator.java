package semgen.visualizations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * Generates a command sender and its associated javascript
 * @author Ryan
 *
 * @param <T> - Type of command sender to generate
 */
public class WebBrowserCommandSenderGenerator<T> {
		
	// Interface to generate from
	private Class<T> _senderInterface;
	
	// Name of variable in javascript
	private String _javascriptCommandReceiverVariableName;
	
	// Instance of command sender used to send commands to javascript
	private T _sender;
	
	@SuppressWarnings("unchecked")
	public WebBrowserCommandSenderGenerator(Class<T> senderInterface, CommunicatingWebBrowser<T> browser, String javascriptCommandReceiverVariableName) {
		if(senderInterface == null)
			throw new NullPointerException("senderInterface");
		
		if(browser == null)
			throw new NullPointerException("browser");
		
		if(javascriptCommandReceiverVariableName == null ||
				javascriptCommandReceiverVariableName.isEmpty())
		{
			throw new NullPointerException("javascriptCommandReceiverVariableName");
		}
		
		_senderInterface = senderInterface;
		_javascriptCommandReceiverVariableName = javascriptCommandReceiverVariableName;
		
		// Create a sender object
		InvocationHandler handler = new WebBrowserCommandSenderInvocationHandler(browser);
		_sender = (T) Proxy.newProxyInstance(_senderInterface.getClassLoader(),
						new Class[] { _senderInterface },
						handler);
	}
	
	/**
	 * Gets an object used to send information to javascript
	 * @return sender
	 */
	public T getSender() {
		return _sender;
	}
	
	/**
	 * Generates a definition for the object in javascript that will be used to register
	 * handlers for events executed in java
	 * @return String representation of javascript object
	 */
	public String generateJavascriptReceiver() {
		// Use reflection to create methods in javascript that listen for commands from java
		String javascriptReceiverMethods = "";
		Method[] javaCommandSenderMethods = _senderInterface.getDeclaredMethods();
		for(int i = 0; i < javaCommandSenderMethods.length; i++) {
			Method method = javaCommandSenderMethods[i];
			String senderMethodName = method.getName();
			
			// The method name for the javascript function that receives the java function call
			String javascriptMethodName = CommunicationHelpers.getEventHandlerMethodName(senderMethodName);
					
			javascriptReceiverMethods += String.format(
					"%s: function (handler) { this.registerEventHandler('%s', handler); }," + CommunicationHelpers.NLJS,
					javascriptMethodName, 
					javascriptMethodName);
		}
		
		// Return a script that defines the recceiver in javascript.
		// Javascript will use this object to receive messages from java in a type safe manner.
		return 
			"var " + _javascriptCommandReceiverVariableName + " = {" + CommunicationHelpers.NLJS +
				"eventHandlers: {}," + CommunicationHelpers.NLJS +
				"registerEventHandler: function ( functionName, handler ) { this.eventHandlers[functionName] = handler; }," + CommunicationHelpers.NLJS +
				"executeHandler: function ( functionName ) { this.eventHandlers[functionName].apply(null, Array.prototype.slice.call(arguments, 1)); }," + CommunicationHelpers.NLJS +
				javascriptReceiverMethods +
			"};";
	}
	
	/**
	 * Creates an instance of a class that executes javascript handlers each time a method is called
	 * @author Ryan
	 *
	 */
	private class WebBrowserCommandSenderInvocationHandler implements InvocationHandler {
		
		private CommunicatingWebBrowser<T> _browser;
		
		public WebBrowserCommandSenderInvocationHandler(CommunicatingWebBrowser<T> browser) {
			if(browser == null)
				throw new NullPointerException("browser");
			
			_browser = browser;
		}
		
		/**
		 * When a method is invoked execute it's javascript handler
		 */
		public Object invoke(Object proxy, Method method, Object[] args)  {
			// Javascript event handler to invoke
			String methodToInvoke = CommunicationHelpers.getEventHandlerMethodName(method.getName());
			
			// Create a string of args to call the javascript method with
			String argsString = "";
			if(args != null) {
				for(int i = 0; i < args.length; i++) {
					if(i != 0)
						argsString += ", ";
					
					// Add single quotes around non-json strings so they're handled
					// as strings in javascript. Do not add quotes around json
					Object arg = args[i];
					String argStringValue;
					if(arg instanceof JsonString)
						argStringValue = arg.toString();
					else
						argStringValue = String.format("'%s'", arg.toString());
					
					argsString += argStringValue;
				}
			}
			
			// Execute the browser function
			// For example is the method called is "loadGraph" with "abcd" as the argument, the following statement will be executed
			// <CommandReceiverVarName>.onLoadGraph('abcd');
			String executionScript = String.format("%s.executeHandler('%s', %s);", _javascriptCommandReceiverVariableName, methodToInvoke, argsString);
			_browser.executeJavascriptAndHandleErrors(executionScript);
			
			return null;
		}
	}
}
