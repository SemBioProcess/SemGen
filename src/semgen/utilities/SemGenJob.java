package semgen.utilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class SemGenJob {
	protected final PropertyChangeSupport pcs;
	protected String status;
	protected boolean cont = true;
	
	public SemGenJob() {
		pcs = new PropertyChangeSupport(this);
	}
	
	public SemGenJob(SemGenJob sga) {
		pcs = sga.pcs;
	}
	
	public abstract void run();
	
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
