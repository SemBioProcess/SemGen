package semgen.resource;

import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import semgen.resource.uicomponent.SemGenProgressBar;

public abstract class SemGenTask extends SwingWorker<Void, Void> implements Observer {
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

    public void showProgressBar() {
		 SwingUtilities.invokeLater(new Runnable() {
		     public void run() {
		        progframe.setVisible(true);
		     }
		  });
    }
	@Override
	public void update(Observable arg0, Object arg1) {
		cancel(true);
	}
}
