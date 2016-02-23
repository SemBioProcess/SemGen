package semgen.visualizations;

import javax.naming.InvalidNameException;

import semgen.SemGen;
import semgen.stage.stagetasks.ProjectWebBrowserCommandSender;
import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;
import semgen.stage.stagetasks.StageTask.Task;

import com.teamdev.jxbrowser.chromium.LoggerProvider;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class SemGenCommunicatingWebBrowser extends CommunicatingWebBrowser {
	// Stage html in resource dir
	private final static String StageHtml = SemGen.cfgreadpath + "stage/stage.html?testMode="+SemGen.debug;
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException, IOException {
		super(ProjectWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
        
		File stageHtmlFile = new File(StageHtml);
		String stageHtmlPath = stageHtmlFile.getAbsolutePath();
		
		// Paths that start with the seperator need to include
		// the file:// protocol at the beginning
		if (stageHtmlPath.startsWith(File.separator))
			stageHtmlPath = "file://" + stageHtmlPath;
		
		System.out.println("Loading the stage @: " + stageHtmlPath);
		this.loadURL(stageHtmlPath);
	    
	    LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);			// The BrowserLogger is used to log browser messages.
	    LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);				// The IPCLogger is used to log IPC (Inter-Process Communication) messages.
	    LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);	// The ChromiumProcessLogger is used to log messages that are come from Chromium process.
	    
	    System.out.println("SemGen web browser loaded");
	}

	public void changeTask(Class<? extends SemGenWebBrowserCommandSender> commandSenderInterface, 
			CommunicatingWebBrowserCommandReceiver commandReceiver, Task task) throws InvalidNameException {
		
		String javascript = this.setBrowserListeners(commandSenderInterface, commandReceiver);
			javascript += "main.changeTask(\"" + task.jsid + "\");";
		
		executeJavascriptAndHandleErrors(javascript);
	}
	
}
