package semgen.stage;

import java.awt.BorderLayout;
import java.util.Observer;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.visualizations.SemGenCommunicatingWebBrowser;

public class StageTab extends SemGenTab {

	public StageTab(SemGenSettings sets, GlobalActions globalacts, StageWorkbench bench) {
		super("Stage", SemGenIcon.annotatemodelicon, "Stage for facilitating SemGen tasks", sets, globalacts);
	}
	
	/**
	 * Can we create the SemGenTab on this platform?
	 * @return null if we can create the tab. Return an error message if we can't.
	 */
	public static String canCreate() {
		try {
			new SemGenCommunicatingWebBrowser();
			return null;
		} catch (NoClassDefFoundError e) {
			return "Unable to show the stage. This may be because swt.jar is not loading properly. Exception: " + e.getMessage();
		}
	}

	@Override
	public void loadTab() {
		setOpaque(false);
		setLayout(new BorderLayout());
		
		// Prepare to create the browser
		NativeInterface.open();

		// Create the browser
		SemGenCommunicatingWebBrowser browser = new SemGenCommunicatingWebBrowser();
		this.add(browser);
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
