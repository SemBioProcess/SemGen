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
	
	@SuppressWarnings("unchecked")
	public ArrayList<Element> getManifestEntries() throws JDOMException, IOException {
			
		ArrayList<Element> children = new ArrayList<Element>();

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
        
        children = new ArrayList<Element>(child.getChildren());
        
        return children;
	}
	
	// Return all valid model files in archive (SBML and CellML for now)
	public ArrayList<ModelAccessor> getModelsInArchive() throws JDOMException, IOException{
		ArrayList<Element> manifestelements = getManifestEntries();
		
		 ArrayList<ModelAccessor> accessors = new ArrayList<ModelAccessor>();
	        for (Element content : manifestelements) {
	        	Attribute format = content.getAttribute("format");
	        	String formvalue = format.getValue().toLowerCase();
	        	
	        	// Only return files with valid SBML or CellML extensions for now
	        	if (ModelClassifier.hasValidOMEXmodelFileFormat(formvalue)) {
	        		
	        		Attribute location = content.getAttribute("location");
	        		accessors.add(new ModelAccessor(omexfile, new File(location.getValue())));
	        	}
	        }

		return accessors;
	}
	
	// Return all valid annotation files in archive, including CASA files
	public ArrayList<ModelAccessor> getAnnotationFilesInArchive() throws JDOMException, IOException{
		ArrayList<Element> manifestelements = getManifestEntries();
		
		 ArrayList<ModelAccessor> accessors = new ArrayList<ModelAccessor>();
	        for (Element content : manifestelements) {
	        	Attribute format = content.getAttribute("format");
	        	String formvalue = format.getValue().toLowerCase();
	        	
	        	// Assume that all valid annotaion files end in "rdf"
	        	if (ModelClassifier.hasValidOMEXannotationFileFormat(formvalue)){
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
