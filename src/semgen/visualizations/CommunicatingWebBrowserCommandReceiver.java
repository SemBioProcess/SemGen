package semgen.visualizations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.naming.InvalidNameException;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.text.WordUtils;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

/**
 * Contract for receiving commands from Javascript
 * 
 * Note: All methods must start with "on" to indicate that the function is an event handler
 * 
 * @author Ryan
 *
 */
public abstract class CommunicatingWebBrowserCommandReceiver {

	/**
	 * Ensure the class is setup properly
	 * @throws InvalidNameException If any method in this class does not follow the naming convention
	 */
	public void validate() throws InvalidNameException {
		Method[] methods = this.getClass().getDeclaredMethods();
		for(int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String name = method.getName();
			
			// If the method name doesn't start with "on"
			// or it is "on", throw an exception
			if(!name.startsWith("on") ||
					name.equals("on"))
			{
				String exceptionMessage = String.format("%s in class %s must start with 'on' and must not be named 'on'",
						name,
						this.getClass().getName());
				throw new InvalidNameException(exceptionMessage);
			}
		}
	}
	
	/**
	 * Generates a definition for the object in javascript that will be used to register
	 * handlers for events executed in java
	 * @return String representation of javascript object
	 */
	public String generateJavascriptSender(String javascriptCommandSenderVariableName ) {
		// Use reflection to create methods in javascript that listen for commands from java
		String javascriptSenderMethods = "";
		Method[] javaReceiverMethods = this.getClass().getDeclaredMethods();
		for(int i = 0; i < javaReceiverMethods.length; i++) {
			Method method = javaReceiverMethods[i];
			String javaMethodName = method.getName();
			
			// The method name for the javascript function that sends the command to java
			// Removes 'on' from the beginning of the string and lowercases the first character.
			// For example, 'onAddModel' -> 'addModel'
			String javascriptMethodName = javaMethodName.substring(2);
			javascriptMethodName = WordUtils.uncapitalize(javascriptMethodName.substring(0,1)) + 	// capitalize the first letter
					javascriptMethodName.substring(1); 												// get the rest of method name
				
			// Each javascript method will send a command to java with arguments
			javascriptSenderMethods += String.format(
					"%s: function () {" + CommunicationHelpers.NLJS +
						"var argumentsArray = Array.prototype.slice.call(arguments);" + CommunicationHelpers.NLJS +		// Create an array object from the arguments array
						"argumentsArray.unshift('%s');" + CommunicationHelpers.NLJS +									// Add the name of the function to call to the beginning of the array
						"sendNSCommand.apply(this, argumentsArray);" + CommunicationHelpers.NLJS +						// Send the command
					"}," + CommunicationHelpers.NLJS,
					javascriptMethodName, 
					javaMethodName);
		}
		
		// Return a script that defines the sender object.
		// Javascript will use this object to send commands to java in a type safe manner.
		return 
			"var " + javascriptCommandSenderVariableName + " = {" + CommunicationHelpers.NLJS +
				javascriptSenderMethods +
			"};";
	}
	
	/**
	 * Creates a web browser adapter that listens for javascript commands
	 * and adds it to the browser
	 */
	public void listenForBrowserCommands(JWebBrowser browser) {
		browser.addWebBrowserListener(new WebBrowserAdapter()
		{
			/**
			 * Receives a command from javascript and calls the receiving function
			 */
			@Override
			public void commandReceived(WebBrowserCommandEvent event) {
				String methodName = event.getCommand();
				Object[] parameters = event.getParameters();
				
				// Create and array of parameter types
				Class<?>[] parameterTypes = new Class<?>[parameters.length];
				for(int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
					parameterTypes[paramIndex] = parameters[paramIndex].getClass();
				}
				
				// Get the java method to call
				Method receivingMethod;
				try {
					receivingMethod = CommunicatingWebBrowserCommandReceiver.this.getClass().getMethod(methodName, parameterTypes);
				} catch (SecurityException | NoSuchMethodException getMethodException) {
					getMethodException.printStackTrace();
					return;
				}
				
				// Invoke the java method with parameters from javascript
				try {
					receivingMethod.invoke(CommunicatingWebBrowserCommandReceiver.this, parameters);
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException invokeMethodException) {
					invokeMethodException.printStackTrace();
					return;
				}
			}
		});
	}
}
