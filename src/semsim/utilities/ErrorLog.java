package semsim.utilities;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class ErrorLog {
	private static Queue<ErrorEntry> errorqueue = new PriorityQueue<ErrorEntry>();
	private static PrintWriter logfilewriter;
	
	public static void addError(String e, Boolean isfatal, boolean addtolog) {
		errorqueue.add(new ErrorEntry(e, isfatal));
		if (addtolog) logfilewriter.println(e);
	}
	
	public static ErrorEntry getFirstError() {
		return errorqueue.poll();
	}
	
	public static boolean hasErrors() {
		return !errorqueue.isEmpty();
	}
	
	public static void setLogFile(PrintWriter logger) {
		logfilewriter = logger;
	}
	
	public static void flush() {
		errorqueue.clear();
	}
	
	public static ArrayList<ErrorEntry> getAllErrors() {
		return new ArrayList<ErrorEntry>(errorqueue);
	}
	
	public static boolean errorsAreFatal() {
		for (ErrorEntry e : errorqueue) {
			if (e.isfatal) return true;
		}
		return false;
	}
	
	public static ArrayList<ErrorEntry> getAllErrorsandFlush() {
		ArrayList<ErrorEntry> errors = getAllErrors();
		flush();
		return errors;
	}

}
