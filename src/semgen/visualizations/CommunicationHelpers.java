package semgen.visualizations;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Helpers to facilitate implementing communication between java and javascript
 * @author Ryan
 *
 */
public class CommunicationHelpers {
	
	// New line
	public final static String NL = System.getProperty("line.separator");
	
	// New lines in the middle of a javascript string need to end with a '\'
	// to indicate that the next line is part of the same string
	//
	// For example:
	// var s = "this is all \
	//		the same string.";
	public final static String NLJS = "\\" + NL;
		
	/**
	 * Gets the name of an event handler function for the given method.
	 * 
	 * For example, given a method named "loadData", this function will return "onLoadData"
	 * @param methodName - use to create event handler method name
	 * @return Event handler method name
	 */
	public static String getEventHandlerMethodName(String methodName) {
		if(methodName == null)
			throw new NullPointerException(methodName);
		
		if(methodName.isEmpty())
			return "";
		
		// Prepend 'on' and camel case the rest of the name
		String capitalizedFirstLetter = WordUtils.capitalize(methodName.substring(0,1));
		String restOfMethodName = methodName.substring(1);
		return "on" +
				capitalizedFirstLetter +
				restOfMethodName;
	}
}
