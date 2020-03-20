package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import semsim.SemSimLibrary;
import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;
import semsim.model.collection.SemSimModel;

/**
 * Class for translating SemSim models into different modeling formats
 * @author mneal
 *
 */
public abstract class ModelWriter {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel;
	protected File srcfile;
	public ModelAccessor writelocation;
	// casa flag, set to true automatically when output
	// is an omex or can be manually overridden
	protected boolean useCASA = false;
	
	ModelWriter(SemSimModel model) {
		semsimmodel = model;
		if(sslib==null) sslib = new SemSimLibrary();
		useCASA = false;
	}

	/**
	 * Override the value of useCASA, to allow outputing CASA
	 * even in the absence of an omex archive.
	 */
	ModelWriter(SemSimModel model, boolean CASA) {
		semsimmodel = model;
		if(sslib==null) sslib = new SemSimLibrary();
		useCASA = CASA;
	}
	
	/**
	 * Sets the {@link SemSimLibrary} instance used when writing out models
	 * @param lib The {@link SemSimLibrary} instance to use
	 */
	public static void pointToSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	/** @return The RDF writer used to output RDF-formatted SemSim annotations.
	 * RDF writers are only used when writing out standalone SBML or CellML models
	 * and when writing CASA files in OMEX archives.
	 */
	public abstract AbstractRDFwriter getRDFwriter();

	
	/**
	 * Commit a model encoded as a String to an OutputStream
	 * @param stream An OutputStream
	 * @param outputstring Model code
	 * @return Return whether writing succeeded
	 */
	protected boolean commitStringtoStream(OutputStream stream, String outputstring) {
		OutputStreamWriter writer = new OutputStreamWriter(stream, Charset.defaultCharset());
		
		try {
			writer.write(outputstring);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/** @return The translated model code */
	public abstract String encodeModel();
	
	
	/**
	 * Write translated model code to an OutputStream
	 * @param stream An OutputStream
	 * @return False if the model translation process returned null, otherwise true
	 */
	public boolean writeToStream(OutputStream stream) {
		String outputstring = encodeModel();
		if (outputstring == null) {
			return false;
		}
		commitStringtoStream(stream, outputstring);
		return true;
	}

	
	/**
	 * Convert a String to an JDOM XML Content object 
	 * @param xml XML code
	 * @return JDOM Content object representation of the XML code
	 */
	public static Content makeXMLContentFromString(String xml){
		try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
			Document aDoc = new SAXBuilder().build(stream);
			return aDoc.getRootElement().detach();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	
	/**
	 * Set the location for writing the translated code.
	 * If the location is an omex archive, enable CASA annotations.
	 * @param ma Location to store the code
	 */
	public void setWriteLocation(ModelAccessor ma){
		writelocation = ma;
		if (ma instanceof OMEXAccessor) {
			useCASA = true;
		} else {
			useCASA = false;
		}
	}
	
	
	/** @return Location where the translated code will be stored */
	public ModelAccessor getWriteLocation(){
		return writelocation;
	}

	/** Returns true if the casa flag is enabled, false otherwise. */
	public boolean OMEXmetadataEnabled() {
		return useCASA;
	}
	
}
