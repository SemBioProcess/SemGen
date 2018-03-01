package semsim.writing;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import semsim.fileaccessors.OMEXAccessor;

//class for generating a new OMEX archive on the file system

public class OMEXArchiveBuilder {
	private OMEXAccessor archive;

	public OMEXArchiveBuilder(OMEXAccessor archive) {
		this.archive = archive;
	}
	
	//Construct an archive with the resources required by the standard.
	public void build() throws IOException {
        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "true");
	        Path path = Paths.get(archive.getDirectoryPath());
	        URI uri = URI.create("jar:" + path.toUri());
	        
	        FileSystem fs = FileSystems.newFileSystem(uri, env);
  	        Document manifest = buildManifest();
  	        
  			Path manifestpath = fs.getPath("manifest.xml");
  			Writer zwriter = Files.newBufferedWriter(manifestpath, StandardCharsets.UTF_8, StandardOpenOption.CREATE);  
  	        
  			Files.createDirectory(fs.getPath("model"));
  			
  			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			
  	        zwriter.write(outputter.outputString(manifest));
  	        zwriter.close();
  	        
  	        //Path nf = fs.getPath("model\\" + archive.getFileName());
  	        fs.close(); //close and write archive to the filesystem
	}
	
	//Create a basic OMEX manifest
	private Document buildManifest() {
		Element root = new Element("omexManifest", "http://identifiers.org/combine.specifications/omex-manifest");
		
		addManifestElement(root, ".", "http://identifiers.org/combine.specifications/omex");
		addManifestElement(root, "./manifest.xml", "http://identifiers.org/combine.specifications/omex-manifest");
				
		Document manifest = new Document(root);
		
		return manifest;
	}
	
	private void addManifestElement(Element root, String location, String format) {
		Element element = new Element("content");
		element.setAttribute("location", location);
		element.setAttribute("format", format);
		root.addContent(element);
	}
	
}
