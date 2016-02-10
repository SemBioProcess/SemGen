package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import JSim.aserver.ASModel;
import JSim.aserver.ASServer;
import JSim.data.NamedVal;
import JSim.util.Xcept;
import semsim.utilities.ErrorLog;

public class MMLParser {
	public int srcType = -1;

	protected Document doc;
	public String jsimBuildDir;

	public MMLParser(String jsimBuildDir){
		this.jsimBuildDir = jsimBuildDir;
		System.setProperty("jsim.home", jsimBuildDir);
	}
	
	public Document readFromMMLstring(String srcText, String modelname) {

		try{
		    // create server
		    NamedVal.NList soptions = new NamedVal.NList();
		    if (jsimBuildDir != null) soptions.setVal("buildDir", jsimBuildDir);
		    ASServer server = ASServer.create(soptions, null, null);	
		    String options = "sbml";
		
		    // translate
		    String xmlstring = null;
		    try {xmlstring = server.translateModelText(ASModel.TEXT_MML, ASModel.TEXT_XMML, srcText, options);} 
		    catch (Exception e) {
		    	ErrorLog.addError("XMML parsing error - could not compile " + modelname, true, true);
		    	e.printStackTrace();
		    	return null;
		    }
		    
			InputStream is = new ByteArrayInputStream(xmlstring.getBytes("UTF-8"));
			doc = new SAXBuilder().build(is);
			
			if(doc.hasRootElement()){
				// If it's not XMML version 2
				if(doc.getRootElement().getChild("model")==null){
					ErrorLog.addError("XMML did not have a root element named 'model' - please use JSim version 2.05 or higher", true, true);
				}
			}
			// Otherwise the model didn't compile
			else ErrorLog.addError("Conversion to SemSim failed because model did not compile", true, true);
		}
		catch(Xcept | JDOMException | IOException e){
			e.printStackTrace();
		}
		return doc;
	}
	
}