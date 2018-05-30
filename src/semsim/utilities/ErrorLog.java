package semsim.utilities;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Class for storing errors that may occur when processing models and associated data
 * @author mneal
 *
 */
public class ErrorLog {
	private static Queue<ErrorEntry> errorqueue = new PriorityQueue<ErrorEntry>();
	private static PrintWriter logfilewriter;
	
	/**
	 * Add an error to the error log
	 * @param e An error message
	 * @param isfatal Whether the error is fatal
	 * @param addtolog Whether to add to this object's PrintWriter 
	 */
	public static void addError(String e, Boolean isfatal, boolean addtolog) {
		errorqueue.add(new ErrorEntry(e, isfatal));
		if (addtolog) logfilewriter.println(e);
	}
	
	/** @return The first error in the log */
	public static ErrorEntry getFirstError() {
		return errorqueue.poll();
	}
	
	/** @return Whether any errors have been added to the log */
	public static boolean hasErrors() {
		return !errorqueue.isEmpty();
	}
	
	/**
	 * Set the PrintWriter used to output the log
	 * @param logger PrintWriter used for output
	 */
	public static void setLogFile(PrintWriter logger) {
		logfilewriter = logger;
	}
	
	/** Clear the error log */
	public static void flush() {
		errorqueue.clear();
	}
	
	/** @return List of all errors in the log */
	public static ArrayList<ErrorEntry> getAllErrors() {
		return new ArrayList<ErrorEntry>(errorqueue);
	}
	
	/** @return Whether any errors in the log are fatal */
	public static boolean errorsAreFatal() {
		for (ErrorEntry e : errorqueue) {
			if (e.isfatal) return true;
		}
		return false;
	}
	
	/** @return List of all errors in the log (function also clears the error log) */
	public static ArrayList<ErrorEntry> getAllErrorsandFlush() {
		ArrayList<ErrorEntry> errors = getAllErrors();
		flush();
		return errors;
	}

}
