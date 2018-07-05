package semsim.utilities;

/**
 * An entry in a SemSim {@link ErrorLog}
 * @author mneal
 *
 */
public class ErrorEntry implements Comparable<ErrorEntry>{
	public String errmsg;
	public Boolean isfatal;
	
	public ErrorEntry(String message, Boolean fatal) {
		errmsg = message;
		isfatal = fatal;
	}

	@Override
	public int compareTo(ErrorEntry arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
