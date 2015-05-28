package semgen.visualizations;

import javax.naming.InvalidNameException;

import com.teamdev.jxbrowser.chromium.LoggerProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

public class SemGenCommunicatingWebBrowser extends CommunicatingWebBrowser<SemGenWebBrowserCommandSender> {
	private static final long serialVersionUID = 1L;
	
	// Resource dir
	private final static String ResourcesDir = "/resources";
	
	// Stage html in resource dir
	private final static String StageHtml = ResourcesDir + "/stage.html?testMode=false";
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException, IOException {
		super(SemGenWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
        
		// Copy files to temp dir
		Path tempResourcesDirPath = Files.createTempDirectory("SemGen-" + Long.toString(System.nanoTime()));
		File tempResourcesDir = tempResourcesDirPath.toFile();
		if(!FileUtils.copyResourcesRecursively(this.getClass().getResource(ResourcesDir), tempResourcesDir)) {
			System.out.println("Failed to load the browser. Unable to copy resources to " + tempResourcesDirPath.toString());
			return;
		}
		else
			System.out.println("Browser files copied sucessfully to " + tempResourcesDirPath.toString());
		
        // Load the stage from the temp dir
		String stageDir = tempResourcesDir.getAbsolutePath() + StageHtml;
		System.out.println("Loading the stage @: " + stageDir);
		this.loadURL(stageDir);
	    
	    LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);			// The BrowserLogger is used to log browser messages.
	    LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);				// The IPCLogger is used to log IPC (Inter-Process Communication) messages.
	    LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);	// The ChromiumProcessLogger is used to log messages that are come from Chromium process.
	    
	    System.out.println("SemGen web browser loaded");
	}

}
