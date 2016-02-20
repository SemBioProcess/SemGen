package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.semanticweb.owlapi.model.OWLException;

import JSim.aserver.ASModel;
import JSim.aserver.ASServer;
import JSim.data.NamedVal;
import JSim.util.UtilIO;
import JSim.util.Xcept;
import semsim.utilities.ErrorLog;

public class MMLParser {
	public int srcType = -1;

	protected Document doc;

	public MMLParser(){}
	
	public Document readFromFile(File file) throws JDOMException, IOException, Xcept, InterruptedException, OWLException {
		String fname = file.getName();
		if (fname.endsWith(".mod")) srcType = ASModel.TEXT_MML;
	    if (fname.endsWith(".xml")) srcType = ASModel.TEXT_XML;
	    if (fname.endsWith(".sbml")) srcType = ASModel.TEXT_SBML;
	    if (fname.endsWith(".cellml")) srcType = ASModel.TEXT_CELLML;
	    if(fname.endsWith(".xml.origin")) srcType = ASModel.TEXT_SBML;
	    if (srcType < 0) {
	    	ErrorLog.addError("Unknown file suffix: " + fname, true, true);
	    	return null;
	    }
		
	    String srcText = UtilIO.readText(file);
	    
	    // create server
	    NamedVal.NList soptions = new NamedVal.NList();
	    ASServer server = ASServer.create(soptions, null, null);	
	    String options = "sbml";
	
	    // translate
	    String xmlstring = null;
	    try {xmlstring = server.translateModelText(srcType, ASModel.TEXT_XMML, srcText, options);} 
	    catch (Exception e) {
	    	ErrorLog.addError("XMML parsing error - could not compile " + file.getName(), true, true);
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
		return doc;
	}
}