package semgen.stage;

import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.DialogParams;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultDialogHandler;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.utilities.BrowserLauncher;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.visualizations.SemGenCommunicatingWebBrowser;

import javax.naming.InvalidNameException;
import javax.swing.*;

import java.awt.*;
import java.io.IOException;

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
			if (SemGen.debug) {
				BrowserPreferences.setChromiumSwitches("--remote-debugging-port=9222"); // Uncomment to debug JS
			}
			SemGenCommunicatingWebBrowser browser = new SemGenCommunicatingWebBrowser(_workbench.getCommandReceiver());
			_workbench.setCommandSender(browser.getCommandSender());
			
			if (SemGen.debug) {
				String remoteDebuggingURL = browser.getRemoteDebuggingURL(); // Uncomment to debug JS
				System.out.println(remoteDebuggingURL); // Uncomment to debug JS. Past this url in chrome to begin debugging JS
				BrowserLauncher.openURL(remoteDebuggingURL);
			}
			
			final BrowserView browserView = new BrowserView(browser);
			
			// Show JS alerts in java dialogs
			browser.setDialogHandler(new DefaultDialogHandler(browserView) {
			    @Override
			    public void onAlert(DialogParams params) {
			        String title = "SemGen Browser Alert";
			        String message = params.getMessage();
			        JOptionPane.showMessageDialog(browserView, message, title,
			                JOptionPane.PLAIN_MESSAGE);
			    }
			});
			this.add(browserView, BorderLayout.CENTER);
		} catch (InvalidNameException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isSaved() {
		return false;
	}

	@Override
	public void requestSave() {

	}

	@Override
	public void requestSaveAs() {

	}
}
