package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;

public class OMEXManifestreader {

	
	@SuppressWarnings("unchecked")
	public static ArrayList<Element> getManifestEntries(ZipFile zarchive, File arcfile) throws JDOMException, IOException {
			
	    Enumeration<? extends ZipEntry> entries = zarchive.entries();
			    ZipEntry entry = null;
			    while(entries.hasMoreElements()){
			        entry = entries.nextElement();
			        if (entry.getName().contains("manifest.xml")) break;
			    }
			    if (entry==null) throw new InvalidObjectException("Missing Manifest");
			   
			    InputStream stream = zarchive.getInputStream(entry);
		        Document doc = new SAXBuilder().build(stream);
		        Element child = doc.getRootElement();//.getChild("omexManifest");
		        ArrayList<Element> children = new ArrayList<Element>(child.getChildren());
		        
		        ArrayList<ModelAccessor> accessors = new ArrayList<ModelAccessor>();
		        for (Element content : children) {
		        	Attribute location = content.getAttribute("location");
		        	Attribute format = content.getAttribute("format");
		        	if (ModelClassifier.hasValidFileExtension(location.getValue(), format.getValue())) {
		   
		        		accessors.add(new OMEXAccessor(arcfile, new File(location.getValue()), ModelClassifier.getTypebyFormat(format.getValue())));
		        	}
		        	
		        }

        
        children = new ArrayList<Element>(child.getChildren());
        
        return children;
	}
	
	// Return all valid model files in archive (SBML and CellML for now)
	public static ArrayList<ModelAccessor> getModelsInArchive(ZipFile archive, File omexfile) throws JDOMException, IOException{
		ArrayList<Element> manifestelements = getManifestEntries(archive, omexfile);
		
		 ArrayList<ModelAccessor> accessors = new ArrayList<ModelAccessor>();
	        for (Element content : manifestelements) {
	        	Attribute format = content.getAttribute("format");
	        	String formvalue = format.getValue().toLowerCase();
	        	
	        	// Only return files with valid SBML or CellML extensions for now
	        	if (ModelClassifier.hasValidOMEXmodelFileFormat(formvalue)) {
	        		
	        		Attribute location = content.getAttribute("location");
	        		accessors.add(new OMEXAccessor(omexfile, new File(location.getValue()), ModelClassifier.getTypebyFormat(formvalue)));
	        	}
	        }

		return accessors;
	}
	
	// Return all valid annotation files in archive, including CASA files
	public static ArrayList<ModelAccessor> getAnnotationFilesInArchive(ZipFile archive, File omexfile) throws JDOMException, IOException{
		ArrayList<Element> manifestelements = getManifestEntries(archive, omexfile);
		
		 ArrayList<ModelAccessor> accessors = new ArrayList<ModelAccessor>();
	        for (Element content : manifestelements) {
	        	Attribute format = content.getAttribute("format");
	        	String formvalue = format.getValue().toLowerCase();
	        	
	        	// Assume that all valid annotaion files end in "rdf"
	        	if (ModelClassifier.hasValidOMEXannotationFileFormat(formvalue)){
	        		Attribute location = content.getAttribute("location");
	        		accessors.add(new OMEXAccessor(omexfile, new File(location.getValue()), ModelClassifier.getTypebyFormat(formvalue)));
	        	}
	        }

		return accessors;
	}
	
	public static boolean archiveContainsModelFile(ZipFile archive, File omexfile, File modelfile) throws JDOMException, IOException {
		ArrayList<Element> manifestelements = getManifestEntries(archive, omexfile);
        for (Element content : manifestelements) {
        	Attribute format = content.getAttribute("format");
        	String formvalue = format.getValue().toLowerCase();
        	
        	// Only return files with valid SBML or CellML extensions for now
        	if (ModelClassifier.hasValidOMEXmodelFileFormat(formvalue)) {
        		
        		Attribute location = content.getAttribute("location");
        		if (location.getValue().matches(omexfile.getPath())) return true;
        	}
        }

		return false;
	}

}
