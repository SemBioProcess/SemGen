/** 
 * Abstract class for defining a workbench factory. Extending classes are required to
 * specify a class extending workbench.
 * */


package semgen.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import semgen.menu.FileMenu;

public abstract class WorkbenchFactory<T extends Workbench> extends SemGenJob  implements Runnable {
	protected ArrayList<File> sourcefile = new ArrayList<File>();

	protected HashSet<T> workbenches = new HashSet<T>();
	public WorkbenchFactory() {}

	public WorkbenchFactory(String initialstatus) {
		status = initialstatus;
	}
	
	protected boolean makeWorkbenches() {
		for (File file : sourcefile) {
			makeWorkbench(file);
		}
		return (workbenches.size() > 0);
	}
	
	abstract protected void makeWorkbench(File file);
	
	public HashSet<T> getWorkbenches() {
		return workbenches;
	}
	
	public void run() {
		if (!makeWorkbenches()) abort();
	}
	
	public String getStatus() {
		return status;
	}
	

	public void addFileMenuasBenchObserver(FileMenu menu) {
		for (T wb : workbenches) {
			if (wb!=null) {
				wb.addObserver(menu);
			}
		}
	}
}
