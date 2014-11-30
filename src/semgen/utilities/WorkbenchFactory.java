/** 
 * Abstract class for defining a workbench factory. Extending classes are required to
 * specify a class extending workbench.
 * */


package semgen.utilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

public abstract class WorkbenchFactory<T extends Workbench>  implements Runnable {

	protected File sourcefile;
	protected boolean cont = true;
	protected String status;
	protected T workbench;
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public WorkbenchFactory() {}

	public WorkbenchFactory(String initialstatus) {
		status = initialstatus;
		}
	
	protected abstract boolean makeWorkbench();
	
	public T getWorkbench() {
		return workbench;
	}
	
	public void run() {
		if (!makeWorkbench()) abort();
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
	
}
