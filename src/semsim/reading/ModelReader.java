package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;

public abstract class ModelReader {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel = new SemSimModel();
	protected ModelAccessor modelaccessor;
	
	ModelReader(ModelAccessor accessor){
		this.modelaccessor = accessor;
	}
	
	public static void pointtoSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	public static Document getJDOMdocumentFromFile(File file){
		Document doc = null;
		SAXBuilder builder = new SAXBuilder();
		
		try{ 
			doc = builder.build(file);
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public Document getJDOMdocumentFromStream(InputStream stream){
		Document doc = null;
		SAXBuilder builder = new SAXBuilder();
		
		try {
			doc = builder.build(stream);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			semsimmodel.addError(e.getLocalizedMessage());
		}	
		return doc;
	}
	
	public abstract SemSimModel read() throws IOException, InterruptedException, OWLException, CloneNotSupportedException, XMLStreamException, JDOMException;
}
