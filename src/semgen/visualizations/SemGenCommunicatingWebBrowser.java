package semgen.visualizations;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

public class SemGenCommunicatingWebBrowser extends
		CommunicatingWebBrowser<SemGenWebBrowserCommandSender> {

	// Stage html
	private final static String StageHtml = "/resources/stage.html";
	
	public SemGenCommunicatingWebBrowser() {
		super(SemGenWebBrowserCommandSender.class);

		System.out.println("Loading SemGen web browser");
		
		// Setup the borwser's look and feel
		this.setMenuBarVisible(false);
        this.setBarsVisible(false);
        this.setFocusable(false);
        
        // Load the stage
	    this.navigate(WebServer.getDefaultWebServer().getClassPathResourceURL(getClass().getName(), StageHtml));
	    
	    // THIS IS A TEST. THIS WILL BE REMOVED.
	    // When the page is loaded send the graph json to the browser
	    this.addWebBrowserListener(new WebBrowserAdapter() {

	    	@Override
			public void loadingProgressChanged(WebBrowserEvent e) {
	    		if(e.getWebBrowser().getLoadingProgress() == 100)
	    		{
	    			// Send the json to javascript
	    			SemGenCommunicatingWebBrowser.this.getCommandSender().test("'This message was sent fromm Java'");
	    			e.getWebBrowser().removeWebBrowserListener(this);
	    		}
	    	}
		});
	    // JPanel webBrowserPanel = new JPanel(new BorderLayout());
	    // webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
	    
	    System.out.println("SemGen web browser loaded");
	}

}
