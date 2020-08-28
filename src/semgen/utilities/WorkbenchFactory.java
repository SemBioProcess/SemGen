/** 
 * Abstract class for defining a workbench factory. Extending classes are required to
 * specify a class extending workbench.
 * */


package semgen.utilities;

import java.util.ArrayList;
import java.util.HashSet;

import semgen.menu.FileMenu;
import semsim.fileaccessors.ModelAccessor;

public abstract class WorkbenchFactory<T extends Workbench> extends SemGenJob  implements Runnable {
	protected ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();

	protected HashSet<T> workbenches = new HashSet<T>();
	public WorkbenchFactory() {}

	public WorkbenchFactory(String initialstatus) {
		status = initialstatus;
	}
	
	protected boolean makeWorkbenches() {
		for (ModelAccessor modelaccessor : modelaccessors) {
			makeWorkbench(modelaccessor);
		}
		return (workbenches.size() > 0);
	}
	
	abstract protected void makeWorkbench(ModelAccessor modelaccessor);
	
	public HashSet<T> getWorkbenches() {
		return workbenches;
	}
	
	public void run() {
		if ( ! makeWorkbenches()) abort();
		setStatus("Loading Tab");
	}
	
	public String getStatus() {
		return status;
	}
	
	public ArrayList<ModelAccessor> getModelAccessors(){
		return modelaccessors;
	}
	

	public void addFileMenuasBenchObserver(FileMenu menu) {
		for (T wb : workbenches) {
			if (wb!=null) {
				wb.addObserver(menu);
			}
		}
	}
}
