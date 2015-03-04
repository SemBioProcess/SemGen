package semgen.visualizations;

/**
 * Used in WebBrowserCommandSenderGenerator to execute javascript with the appropriate type of string
 * 
 * @author Ryan
 *
 */
public class JsonString {

	// Value
	private String _jsonString;
	
	public JsonString(String jsonString) {
		_jsonString = jsonString;
	}
	
	@Override
	public String toString() {
		return _jsonString;
	}
}
