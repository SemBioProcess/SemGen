package semsim;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class ErrorLog {
	private static Queue<String> errorqueue = new PriorityQueue<String>();
	private static PrintWriter logfilewriter;
	
	public static void addError(String e, boolean addtolog) {
		errorqueue.add(e);
		if (addtolog) logfilewriter.println(e);
	}
	
	public static String getFirstError(String e) {
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
	
	public static ArrayList<String> getAllErrors() {
		return new ArrayList<String>(errorqueue);
	}
	
	public static ArrayList<String> getAllErrorsandFlush() {
		ArrayList<String> errors = getAllErrors();
		flush();
		return errors;
	}
}
