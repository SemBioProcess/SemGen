package semgen.stage;

import java.io.File;

import javax.swing.JOptionPane;

import semgen.utilities.Workbench;
import semgen.visualizations.CommunicatingWebBrowserCommandReceiver;
import semgen.visualizations.SemGenWebBrowserCommandReceiver;

public class StageWorkbench extends Workbench {

	/**
	 * Get an object that listens for javascript commands
	 * @return
	 */
	public CommunicatingWebBrowserCommandReceiver getCommandReceiver() {
		return new StageCommandReceiver();
	}
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public File saveModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveModelAs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setModelSaved(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCurrentModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModelSourceFile() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Receives commands from javascript
	 * @author Ryan
	 *
	 */
	public class StageCommandReceiver extends SemGenWebBrowserCommandReceiver {

		/**
		 * Receives the add model command
		 */
		@Override
		public void onAddModel() {
			JOptionPane.showMessageDialog(null, "Add model from stage coming soon");
		}
	}
}
