package semgen.visualizations;

import javax.naming.InvalidNameException;

public class SemGenCommunicatingWebBrowser extends
		CommunicatingWebBrowser<SemGenWebBrowserCommandSender> {
	private static final long serialVersionUID = 1L;
	// Stage html
	private final static String StageHtml = "/resources/stage.html";
	
	public SemGenCommunicatingWebBrowser(CommunicatingWebBrowserCommandReceiver commandReceiver) throws InvalidNameException {
		super(SemGenWebBrowserCommandSender.class, commandReceiver);

		System.out.println("Loading SemGen web browser");
        
        // Load the stage
	    this.loadURL(this.getClass().getResource(StageHtml).toString() + "?testMode=false");
	    
	    System.out.println("SemGen web browser loaded");
	}

}
