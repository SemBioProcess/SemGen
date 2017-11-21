package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public abstract class ModelWriter {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel;
	protected File srcfile;
	
	ModelWriter(SemSimModel model) {
		semsimmodel = model;
	}
	
	public static void pointToSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	public void writeToFile(ModelAccessor destination) {
		FileOutputStream outstream;
		try {
			outstream = new FileOutputStream(destination.getFile());
			writeToStream(outstream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
		
	//Return whether write succeeded
	protected abstract boolean writeToStream(OutputStream stream);
	
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
	
	public void writeToArchive(ModelAccessor archive) {
		
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
	
//	/**
//	 * Write a string to a file
//	 *  @param content The string to write out
//	 *  @param outputfile The file to which the string will be written
//	 */
//	public static void writeStringToFile(OutputStream content, File outputfile){
//		if(content!=null && (content!=null) && outputfile!=null){
//			try {
//				PrintWriter pwriter = new PrintWriter(new FileWriter(outputfile));
//				pwriter.print(content);
//				pwriter.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
