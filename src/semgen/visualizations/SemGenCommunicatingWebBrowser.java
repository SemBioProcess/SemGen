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
	private final static String StageHtml = "/stage.html?testMode=false";
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException, IOException {
		super(SemGenWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
        
		// Copy files to temp dir
		Path tempDirPath = Files.createTempDirectory("SemGen-" + Long.toString(System.nanoTime()));
		File tempDir = tempDirPath.toFile();
		if(!FileUtils.copyResourcesRecursively(this.getClass().getResource(ResourcesDir), tempDir)) {
			System.out.println("Failed to load the browser. Unable to copy resources to " + tempDir.toString());
			return;
		}
		else
			System.out.println("Browser files copied sucessfully to " + tempDir.toString());
		
		String tempDirAbsolutePath = tempDir.getAbsolutePath();
		
		// Paths that start with the seperator need to include
		// the file:// protocol at the beginning of the path
		if(tempDirAbsolutePath.startsWith(File.separator))
			tempDirAbsolutePath = "file://" + tempDirAbsolutePath;
		
        // Load the stage from the temp dir
		String stageDir = tempDirAbsolutePath + ResourcesDir + StageHtml;
		System.out.println("Loading the stage @: " + stageDir);
		this.loadURL(stageDir);
	    
	    LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);			// The BrowserLogger is used to log browser messages.
	    LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);				// The IPCLogger is used to log IPC (Inter-Process Communication) messages.
	    LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);	// The ChromiumProcessLogger is used to log messages that are come from Chromium process.
	    
	    System.out.println("SemGen web browser loaded");
	}

}
