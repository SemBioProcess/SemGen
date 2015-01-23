package semgen.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingWorker;

import semgen.utilities.uicomponent.SemGenProgressBar;

public abstract class SemGenTask extends SwingWorker<Void, Void> implements PropertyChangeListener {
	protected SemGenProgressBar progframe = null;
	
	@Override
	protected abstract Void doInBackground() throws Exception;
	
    @Override
    public void done() {
    	if (progframe!=null) progframe.dispose();
    	if (isCancelled()) return;
    	endTask();
    }

    public void endTask() {}

    public void progressUpdated(String update) {
    	firePropertyChange("status", new String(update), null);
    }
    
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName() == "status") {
			progframe.updateMessage(evt.getNewValue().toString());
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
