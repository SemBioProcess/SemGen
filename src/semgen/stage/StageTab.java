package semgen.stage;

import java.awt.BorderLayout;
import java.util.Observer;

import javax.naming.InvalidNameException;
import javax.swing.JOptionPane;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.visualizations.SemGenCommunicatingWebBrowser;

public class StageTab extends SemGenTab {

	// Stage workbench
	private StageWorkbench _workbench;
	
	public StageTab(SemGenSettings sets, GlobalActions globalacts, StageWorkbench bench) {
		super("Stage", SemGenIcon.annotatemodelicon, "Stage for facilitating SemGen tasks", sets, globalacts);
		
		_workbench = bench;
	}
	
	/**
	 * Can we create the SemGenTab on this platform?
	 * @return null if we can create the tab. Return an error message if we can't.
	 */
	public static String canCreate() {
		try {
			NativeInterface.open();
			new SemGenCommunicatingWebBrowser(null);
			return null;
		} catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
			e.printStackTrace();
			return "Unable to show the stage. This may be because swt.jar is not loading properly. Exception: " + e.getMessage();
		} catch (InvalidNameException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@Override
	public void loadTab() {
		setOpaque(false);
		setLayout(new BorderLayout());

		// Prepare to create the browser
		NativeInterface.open();
		
		// Create the browser
		try {
			SemGenCommunicatingWebBrowser browser = new SemGenCommunicatingWebBrowser(_workbench.getCommandReceiver());
			_workbench.setCommandSender(browser.getCommandSender());
			this.add(browser);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isSaved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void requestSave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addObservertoWorkbench(Observer obs) {
		// TODO Auto-generated method stub

	}
}
