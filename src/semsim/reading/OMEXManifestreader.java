package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class OMEXManifestreader {

	ZipFile archive;
	File omexfile;
	
	
	public OMEXManifestreader(File file) throws ZipException, IOException {
		omexfile = file;
		archive = new ZipFile(file);
		
	}
	
	public ArrayList<ModelAccessor> collectModels() throws JDOMException, IOException {
			
			    Enumeration<? extends ZipEntry> entries = archive.entries();

			    ZipEntry entry = null;
			    while(entries.hasMoreElements()){
			        entry = entries.nextElement();
			        if (entry.getName().contains("manifest.xml")) break;
			    }
			    if (entry==null) throw new InvalidObjectException("Missing Manifest");
			   
			    InputStream stream = archive.getInputStream(entry);
		        Document doc = new SAXBuilder().build(stream);
		        Element child = doc.getRootElement();//.getChild("omexManifest");
		        @SuppressWarnings("unchecked")
				ArrayList<Element> children = new ArrayList<Element>(child.getChildren());
		        
		        ArrayList<ModelAccessor> accessors = new ArrayList<ModelAccessor>();
		        for (Element content : children) {
		        	Attribute format = content.getAttribute("format");
		        	String formvalue = format.getValue();
		        	if (ModelClassifier.hasValidFileExtension(formvalue)) {
		        		Attribute location = content.getAttribute("location");
		        		accessors.add(new ModelAccessor(omexfile, new File(location.getValue())));
		        	}
		        	
		        }

			return accessors;
	}
	
	public void close() {
		try {
			archive.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
