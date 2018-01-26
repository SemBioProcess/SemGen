package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import semsim.fileaccessors.OMEXAccessor;
import semsim.reading.ModelClassifier.ModelType;

public class OMEXArchiveWriter {
	private ModelWriter writer;
	private CASAwriter casawriter;
	
	public OMEXArchiveWriter(ModelWriter writer) {
		
		this.writer = writer;
	}
	
	public OMEXArchiveWriter(ModelWriter writer, CASAwriter casawriter) {
		
		this.writer = writer;
		this.casawriter = casawriter;
	}


	public void appendOMEXArchive(OMEXAccessor archive) {
      //  String inputFileName = archive.getFilePath();
        try {

	        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "false");
	        Path path = Paths.get(archive.getDirectoryPath());
	        URI uri = URI.create("jar:" + path.toUri());
	        
	        FileSystem fs = FileSystems.newFileSystem(uri, env);
	        Path nf = null;
	        if (Files.exists(fs.getPath("model\\"))) {
	        	nf = fs.getPath("model\\" + archive.getFileName());
	        }
	        else {
	        	nf = fs.getPath(archive.getFileName());
	        }
	        
	        boolean fileexists = Files.exists(nf);
	        
	        Writer zwriter = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	        String model = writer.encodeModel();
	        zwriter.write(model);
	        zwriter.close(); 	
	        if (!fileexists) {
	        	createManifestEntry(fs, nf, archive.getModelType());
	        }
	        
	        if (archive.hasCASAFile()) {
	        //	createCASA(fs, archive);
	        }
	        archive.closeStream();
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
    
    private void createCASA(FileSystem fs, OMEXAccessor archive) throws IOException, JDOMException {
        Path nf = null;
        if (Files.exists(fs.getPath("model\\"))) {
        	nf = fs.getPath("model\\" + archive.getCASAFileName());
        }
        else {
        	nf = fs.getPath(archive.getCASAFileName());
        }
        boolean fileexists = Files.exists(nf);
        
        Writer omexwriter = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        String model = casawriter.getObjectRDFmodelAsString();
        omexwriter.write(model);
        omexwriter.close(); 
        
        if (!fileexists) {
        	createManifestEntry(fs, nf, ModelType.CASA_FILE);
        }
    }
    
	private void createManifestEntry(FileSystem fs, Path modelfile, ModelType type) throws ZipException, IOException, JDOMException {
		Path manifestpath = fs.getPath("manifest.xml");        
		
		Reader zreader = Files.newBufferedReader(manifestpath, StandardCharsets.UTF_8);
		
		StringBuilder buf = new StringBuilder();
		CharBuffer cbuff = CharBuffer.allocate(2048);
		
	    while(zreader.read(cbuff) != -1){
	    	cbuff.flip();
	        buf.append(cbuff);
	        cbuff.clear();
	    }
		InputStream targetStream = new ByteArrayInputStream(
			      buf.toString().getBytes(StandardCharsets.UTF_8));
		zreader.close();
        Document doc = new SAXBuilder().build(targetStream);
        targetStream.close();
        
        Element root = doc.getRootElement();//.getChild("omexManifest");
        
        Element newentry = new Element("content", "http://identifiers.org/combine.specifications/omex-manifest");
        newentry.setAttribute("location", "./" + modelfile.toString());
        newentry.setAttribute("format", type.getFormat());
        
        root.addContent(newentry);
        Writer zwriter = Files.newBufferedWriter(manifestpath, StandardCharsets.UTF_8, StandardOpenOption.CREATE);  
        
        zwriter.write(new XMLOutputter().outputString(doc));
        zwriter.close();

	}


}
