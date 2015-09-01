/** 
 * Abstract class for defining a workbench factory. Extending classes are required to
 * specify a class extending workbench.
 * */


package semgen.utilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import semgen.menu.FileMenu;

public abstract class WorkbenchFactory<T extends Workbench>  implements Runnable {
	protected ArrayList<File> sourcefile = new ArrayList<File>();
	protected boolean cont = true;
	protected String status;
	protected HashSet<T> workbenches = new HashSet<T>();
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected String errors = new String();
	
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
	
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
       pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void setStatus(String newValue) {
        String oldValue = status;
        status = newValue;
        pcs.firePropertyChange("status", oldValue, newValue);
    }
    
    public boolean isValid() {
		return cont;
	}
	
	/** 
	 * Stop signal for while loop
	 */
	protected void abort() {
		cont = false;
	}
	
	public void addFileMenuasBenchObserver(FileMenu menu) {
		for (T wb : workbenches) {
			if (wb!=null) {
				wb.addObserver(menu);
			}
		}
	}
	
	public String getErrors() {
		return errors;
	}
}
