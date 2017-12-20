package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;
import semsim.reading.ModelClassifier;
import semsim.reading.OMEXManifestreader;

public class OMEXArchiveWriter {
	private ModelWriter writer;
	private FileSystem fs;
	
	public OMEXArchiveWriter(ModelWriter writer) {
		
		this.writer = writer;
	}

	public void appendOMEXArchive(OMEXAccessor archive) {
      //  String inputFileName = archive.getFilePath();
        try {

	        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "false");
	        Path path = Paths.get(archive.getDirectoryPath());
	        URI uri = URI.create("jar:" + path.toUri());
	        
	        fs = FileSystems.newFileSystem(uri, env);
	        Path nf = fs.getPath("model\\" + archive.getFileName());
	        boolean fileexists = Files.exists(nf);
	        
	        Writer zwriter = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	        String model = writer.encodeModel();
	        zwriter.write(model);
	        zwriter.close(); 	
	        if (!fileexists) {
	        	createManifestEntry(fs, nf);
	        }
	        
	        fs.close();
	        
	        
        }
		catch (IOException | JDOMException e1) {
				e1.printStackTrace();
		}
	        
	}
	

    private void createArchive(OMEXAccessor archive) {
        try {

  	        Map<String, String> env = new HashMap<>(); 
  	        env.put("create", "true");
  	        Path path = Paths.get(archive.getDirectoryPath());
  	        URI uri = URI.create("jar:" + path.toUri());
  	        
  	        FileSystem fs = FileSystems.newFileSystem(uri, env);
  	        
  	        Path nf = fs.getPath("model\\" + archive.getFileName());
  	        Writer zwriter = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
  	        String model = writer.encodeModel();
  	        zwriter.write(model);
  	        zwriter.close();
  	        fs.close();
  	        
          }
  		catch (IOException e1) {
  				e1.printStackTrace();
  		}
    }
    
	private void createManifestEntry(FileSystem fs, Path modelfile) throws ZipException, IOException, JDOMException {
		Path manifestpath = fs.getPath("manifest.xml");        
		
		Reader zreader = Files.newBufferedReader(manifestpath, StandardCharsets.UTF_8);
		
		StringBuilder buf = new StringBuilder();
		CharBuffer cbuff = CharBuffer.allocate(1024);
		
	    while(zreader.read(cbuff) != -1){
	        buf.append(cbuff);
	        cbuff.clear();
	    }
		InputStream targetStream = new ByteArrayInputStream(
			      buf.toString().getBytes(StandardCharsets.UTF_8));
        Document doc = new SAXBuilder().build(targetStream);
        Element child = doc.getRootElement();//.getChild("omexManifest");
        //ArrayList<Element> children = new ArrayList<Element>(child.getChildren());
        
	}


}
