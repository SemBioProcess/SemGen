package semgen.visualizations;

import javax.naming.InvalidNameException;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import java.util.logging.*;

public class SemGenCommunicatingWebBrowser extends CommunicatingWebBrowser<SemGenWebBrowserCommandSender> {
	private static final long serialVersionUID = 1L;
	// Stage html
	private final static String StageHtml = "/resources/stage.html";
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException {
		super(SemGenWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
        
        // Load the stage
	    this.loadURL(this.getClass().getResource(StageHtml).toString() + "?testMode=false");
	    
	    System.out.println("SemGen web browser loaded");
	    
	    LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);			// The BrowserLogger is used to log browser messages.
	    LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);				// The IPCLogger is used to log IPC (Inter-Process Communication) messages.
	    LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);	// The ChromiumProcessLogger is used to log messages that are come from Chromium process.
	}

}
