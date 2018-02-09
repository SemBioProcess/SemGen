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
import semsim.model.collection.SemSimModel;

public abstract class ModelWriter {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel;
	protected File srcfile;
	public ModelAccessor writelocation;
	
	ModelWriter(SemSimModel model) {
		semsimmodel = model;
	}
	
	public static void pointToSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	public abstract AbstractRDFwriter getRDFwriter();

	//Return whether write succeeded
	
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
	
	public abstract String encodeModel();
	
	public boolean writeToStream(OutputStream stream) {
		String outputstring = encodeModel();
		if (outputstring == null) {
			return false;
		}
		commitStringtoStream(stream, outputstring);
		return true;
	}

	public static Content makeXMLContentFromString(String xml){
		try {
			InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Document aDoc = new SAXBuilder().build(stream);
			return aDoc.getRootElement().detach();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public void setWriteLocation(ModelAccessor ma){
		writelocation = ma;
	}
	
	public ModelAccessor getWriteLocation(){
		return writelocation;
	}
	
}
