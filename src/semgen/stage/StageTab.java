package semgen.stage;

import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.DialogParams;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultDialogHandler;
import com.teamdev.jxbrowser.chromium.DefaultLoadHandler;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.stage.StageWorkbench.StageEvent;
import semgen.utilities.BrowserLauncher;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.visualizations.SemGenCommunicatingWebBrowser;

import javax.naming.InvalidNameException;
import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class StageTab extends SemGenTab implements Observer {
	private static final long serialVersionUID = 1L;
	// Stage workbench
	private StageWorkbench _workbench;
	private SemGenCommunicatingWebBrowser browser;

	public StageTab(SemGenSettings sets, GlobalActions globalacts, StageWorkbench bench) {
		super("Stage", SemGenIcon.stageicon, "Stage for facilitating SemGen tasks", sets, globalacts);

		_workbench = bench;
		bench.addObserver(this);
		
	}

	@Override
	public void loadTab() {
		setOpaque(false);
		setLayout(new BorderLayout());
		// Create the browser
		
		try {
			//Allows local files to be accessed - security vulnerability if not running locally
			ArrayList<String> browserprefs = new ArrayList<String>();
			if (SemGen.debug) {
				browserprefs.add("--remote-debugging-port=9222"); // Uncomment to debug JS
			}

			BrowserPreferences.setChromiumSwitches(browserprefs.toArray(new String[]{}));
			browser = new SemGenCommunicatingWebBrowser();
			
			if (SemGen.debug) {
				String remoteDebuggingURL = browser.getRemoteDebuggingURL(); // Uncomment to debug JS
				System.out.println(remoteDebuggingURL); // Uncomment to debug JS. Past this url in chrome to begin debugging JS
				BrowserLauncher.openURL(remoteDebuggingURL);
			}
			
			browser.setPostloadinstructions(new WorkbenchInitializer());
			
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

			// Disable Backspace and Shift+Backspace navigation
			browser.setLoadHandler(new DefaultLoadHandler() {
				@Override
				public boolean canNavigateOnBackspace() {
					return false;
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
		_workbench.saveModel();
	}

	@Override
	public void requestSaveAs() {
		_workbench.saveModelAs();
	}

	public boolean closeTab() {
		browser.dispose();
		return true;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg == StageEvent.CHANGETASK) {
			try {
				browser.changeTask(_workbench.getCommandSenderInterface(), _workbench.getCommandReceiver(), _workbench.getActiveStageState());
				_workbench.setCommandSender(browser.getCommandSenderGenerator());
			} catch (InvalidNameException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class WorkbenchInitializer implements Runnable {
		public void run() {
			_workbench.initialize();
		}
	}
}
