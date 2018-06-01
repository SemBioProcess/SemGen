package semsim.utilities;

/**
 * An entry in a SemSim {@link ErrorLog}
 * @author mneal
 *
 */
public class ErrorEntry {
	public String errmsg;
	public Boolean isfatal;
	
	public ErrorEntry(String message, Boolean fatal) {
		errmsg = message;
		isfatal = fatal;
	}
}
