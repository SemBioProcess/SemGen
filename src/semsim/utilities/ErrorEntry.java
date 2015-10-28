package semsim.utilities;

public class ErrorEntry {
	public String errmsg;
	public Boolean isfatal;
	
	public ErrorEntry(String msg, Boolean fatal) {
		errmsg = msg;
		isfatal = fatal;
	}
}
