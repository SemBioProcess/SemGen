package semsim.reading;

import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;

/** 
 * Base class for readers of all file types
 * **/
public abstract class ModelReader {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel = new SemSimModel();
	protected ModelAccessor modelaccessor;
	
	ModelReader(ModelAccessor accessor){
		this.modelaccessor = accessor;
		if(sslib==null) sslib = new SemSimLibrary();
	}
	
	/**
	 * Give the ModelReader class and all subclasses a {@link SemSimLibrary}
	 * instance to use when reading in models
	 * @param lib A {@link SemSimLibrary} instance
	 */
	public static void pointToSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	/**
	 * Build document from an XML-encoded file
	 * @param file An XML-encoded File
	 * @return JDOM Document representing the file
	 */
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
	
	/**
	 * Convert the file referred to by this class's ModelAccessor into
	 * a {@link SemSimModel}
	 * @return The {@link SemSimModel} representation
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OWLException
	 * @throws CloneNotSupportedException
	 * @throws XMLStreamException
	 * @throws JDOMException
	 */
	public abstract SemSimModel read() throws IOException, InterruptedException, OWLException, CloneNotSupportedException, XMLStreamException, JDOMException;
}
