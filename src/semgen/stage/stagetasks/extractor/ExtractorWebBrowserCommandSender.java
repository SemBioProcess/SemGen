package semgen.stage.stagetasks.extractor;

import semgen.stage.serialization.ExtractionNode;
import semgen.stage.stagetasks.SemGenWebBrowserCommandSender;

public interface ExtractorWebBrowserCommandSender extends SemGenWebBrowserCommandSender {
	public void newExtraction(ExtractionNode newextraction);
	
	public void updateExtraction();
	
	public void removeExtraction();
	
}
