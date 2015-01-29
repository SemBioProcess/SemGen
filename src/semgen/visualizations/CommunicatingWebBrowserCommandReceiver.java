package semgen.visualizations;

import java.lang.reflect.Method;

import javax.naming.InvalidNameException;

import org.apache.commons.lang3.text.WordUtils;

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
			String receiverMethodName = method.getName();
			
			// The method name for the javascript function that sends the command to java
			// Removes 'on' from the beginning of the string and lowercases the first character.
			// For example, 'onAddModel' -> 'addModel'
			String javascriptMethodName = receiverMethodName.substring(2);
			javascriptMethodName = WordUtils.uncapitalize(javascriptMethodName.substring(0,1)) + 	// capitalize the first letter
					javascriptMethodName.substring(1); 												// get the rest of method name
				
			// Each javascript method will send a command to java with arguments
			javascriptSenderMethods += String.format(
					"%s: function () { sendNSCommand('%s', arguments); }," + CommunicationHelpers.NLJS,
					javascriptMethodName, 
					javascriptMethodName);
		}
		
		// Return a script that defines the sender object.
		// Javascript will use this object to send commands to java in a type safe manner.
		return 
			"var " + javascriptCommandSenderVariableName + " = {" + CommunicationHelpers.NLJS +
				javascriptSenderMethods +
			"};";
	}
}
