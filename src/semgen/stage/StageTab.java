package semgen.stage;

import java.awt.BorderLayout;
import java.util.Observer;

import javax.naming.InvalidNameException;

import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.visualizations.SemGenCommunicatingWebBrowser;

public class StageTab extends SemGenTab {
	private static final long serialVersionUID = 1L;
	// Stage workbench
	private StageWorkbench _workbench;
	
	public StageTab(SemGenSettings sets, GlobalActions globalacts, StageWorkbench bench) {
		super("Stage", SemGenIcon.stageicon, "Stage for facilitating SemGen tasks", sets, globalacts);
		
		_workbench = bench;
	}

	@Override
	public void loadTab() {
		setOpaque(false);
		setLayout(new BorderLayout());
		
		// Create the browser
		try {
			SemGenCommunicatingWebBrowser browser = new SemGenCommunicatingWebBrowser(_workbench.getCommandReceiver());
			_workbench.setCommandSender(browser.getCommandSender());
			BrowserView browserView = new BrowserView(browser);
			this.add(browserView, BorderLayout.CENTER);
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
