/** 
 * Extends SwingWorker by automatically providing a SemGen Progress Bar
 * and adding some methods for using it.
 */

package semgen.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.SwingWorker;

import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.utilities.ErrorLog;

public abstract class SemGenTask extends SwingWorker<Void, String> implements PropertyChangeListener {
	protected SemGenProgressBar progframe = null;
	
    @Override
    public void done() {
    	if (progframe!=null) progframe.dispose();
    	boolean fatalerror = ErrorLog.errorsAreFatal();
		if (SemGenError.showSemSimErrors()) { 
			onError();
		}
    	if (isCancelled() || fatalerror) {
    		return;
    	}
    	endTask();
    }

    public void onError() {}
    
    public void endTask() {}

    public void progressUpdated(String update) {    	
    	firePropertyChange("status", new String(update), null);
    }
    
    @Override
    protected void process(List<String> chunks) {
		progframe.updateMessage(chunks.get(0));
    }
    
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!isDone()) {
			publish(evt.getNewValue().toString());
		}
	}
}
