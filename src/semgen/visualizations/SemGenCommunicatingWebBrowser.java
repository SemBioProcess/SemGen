package semgen.visualizations;

import javax.naming.InvalidNameException;

import chrriis.common.WebServer;

public class SemGenCommunicatingWebBrowser extends
		CommunicatingWebBrowser<SemGenWebBrowserCommandSender> {
	private static final long serialVersionUID = 1L;
	// Stage html
	private final static String StageHtml = "/resources/stage.html";
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException {
		super(SemGenWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
		
		// Setup the borwser's look and feel
		this.setMenuBarVisible(false);
        this.setBarsVisible(false);
        this.setFocusable(false);
        
        // Load the stage
	    this.navigate(WebServer.getDefaultWebServer().getClassPathResourceURL(getClass().getName(), StageHtml));
	    
	    System.out.println("SemGen web browser loaded");
	}

}
