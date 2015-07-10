package semgen.visualizations;

import javax.naming.InvalidNameException;

import semgen.SemGen;

import com.teamdev.jxbrowser.chromium.LoggerProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

public class SemGenCommunicatingWebBrowser extends CommunicatingWebBrowser<SemGenWebBrowserCommandSender> {
	private static final long serialVersionUID = 1L;
	
	// Stage html in resource dir
	private final static String StageHtml = "stage/stage.html?testMode=false";
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException, IOException {
		super(SemGenWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
        
		File stageFile = new File(StageHtml);
		System.out.println("Loading the stage @: " + stageFile.getAbsolutePath());
		this.loadURL(stageFile.getAbsolutePath());
	    
	    LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);			// The BrowserLogger is used to log browser messages.
	    LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);				// The IPCLogger is used to log IPC (Inter-Process Communication) messages.
	    LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);	// The ChromiumProcessLogger is used to log messages that are come from Chromium process.
	    
	    System.out.println("SemGen web browser loaded");
	}
}
