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
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import semsim.fileaccessors.OMEXAccessor;
import semsim.reading.ModelClassifier.ModelType;

/**
 * Class for writing models within OMEX archives
 * @author mneal
 *
 */
public class OMEXArchiveWriter {
	private ModelWriter writer;
	
	public OMEXArchiveWriter(ModelWriter writer) {
		this.writer = writer;
	}
	
	public OMEXArchiveWriter(ModelWriter writer, OMEXmetadataWriter metadatawriter) {		
		this.writer = writer;
	}


	/**
	 * Add a new file within an omex archive or modify an existing one
	 * @param archiveaccessor Location of the archive
	 */
	public void appendOMEXArchive(OMEXAccessor archiveaccessor) {
        

        	if (!archiveaccessor.isLocalFile()) {
        		 try{
        	        OMEXArchiveBuilder builder = new OMEXArchiveBuilder(archiveaccessor);
        	        builder.build();
        		 }
    	      	catch (IOException e1) {
	  				e1.printStackTrace();
    	  		}
        	}
        	
	        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "false");
	        Path path = Paths.get(archiveaccessor.getDirectoryPath());
	        URI uri = URI.create("jar:" + path.toUri());
	        
	     //Create the omex file system
	     try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
	        Path modelpath = null;
	        
	        if (Files.exists(fs.getPath("model\\"))) modelpath = fs.getPath("model\\" + archiveaccessor.getFileName());
	        else modelpath = fs.getPath(archiveaccessor.getFileName());
	        
	        boolean fileexists = Files.exists(modelpath);
	        
	        //Write out the model
	        Writer zwriter = Files.newBufferedWriter(modelpath, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	        writer.setWriteLocation(archiveaccessor);
	        
	        // Have to encodeModel() whether we're generating the code anew or copying it in from a legacy location
	        // because encodeModel() populates the RDF with the annotations
	        String modelcode = writer.encodeModel(); 

	        // For now we always generate the code anew. This is because it's possible that the user has
	        // added an annotation on a model element that doesn't have a metadata id attribute set
	        // and we will have to generate new code that adds the metadata id
	        zwriter.write(modelcode);
	        
	        // TODO: If the modeling format we're writing out is the same as the legacy code format
	        // and we don't need to add metadta id's to the original code, we should
	        // just copy the legacy code and put it in the archive
//	        if( writer.getWriteLocation().getModelType() == writer.semsimmodel.getSourceModelType()) {
//		        ModelAccessor legacyaccessor = writer.semsimmodel.getLegacyCodeLocation();
//	        	zwriter.write(legacyaccessor.getModelAsString());
//	        }
	        // Otherwise we generate the model code anew
//	        else 
	        
	        zwriter.close(); 	
	        
	        //Add an entry if the file doesn't already exist
	        if ( ! fileexists) {
	        	createManifestEntry(fs, modelpath, archiveaccessor.getModelType());
	        }
	        //Write out the OMEX metadata file if the model should have one
	        if (archiveaccessor.hasOMEXmetadataFile())
	        	createOMEXmetadata(fs, archiveaccessor);
	        
	        archiveaccessor.closeStream();	        
	        
        }
		catch (IOException | JDOMException e1) {
				e1.printStackTrace();
		}
	}
    
	/**
	 * Create an OMEX annotation file for a {@link semsim.model.collection.SemSimModel}
	 * @param fs A FileSystem object
	 * @param archive Location of the OMEX archive
	 * @throws IOException
	 * @throws JDOMException
	 */
    private void createOMEXmetadata(FileSystem fs, OMEXAccessor archive) throws IOException, JDOMException {
    	
        Path nf = null;
        if (Files.exists(fs.getPath("model\\"))) {
        	nf = fs.getPath("model\\" + archive.getOMEXmetadataFileName());
        }
        else {
        	nf = fs.getPath(archive.getOMEXmetadataFileName());
        }
        boolean fileexists = Files.exists(nf);
        
        Writer omexwriter = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        String model = AbstractRDFwriter.getRDFmodelAsString(writer.getRDFwriter().rdf, "RDF/XML");
        omexwriter.write(model);
        omexwriter.close(); 
        
        if (!fileexists) {
        	createManifestEntry(fs, nf, ModelType.OMEX_METADATA_FILE);
        }
    }
    
    /**
     * Create an entry in the OMEX manifest for a file
     * @param fs A FileSystem object
     * @param modelfile Path to model in archive
     * @param type The type of file described by the entry
     * @throws ZipException
     * @throws IOException
     * @throws JDOMException
     */
	private void createManifestEntry(FileSystem fs, Path modelfile, ModelType type) throws ZipException, IOException, JDOMException {
		Path manifestpath = fs.getPath("manifest.xml");        
		
		Reader zreader = Files.newBufferedReader(manifestpath, StandardCharsets.UTF_8);
		
		StringBuilder buf = new StringBuilder();
		CharBuffer cbuff = CharBuffer.allocate(2048);
		//Read in manifest
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
        
        Element newentry = new Element("content", "https://identifiers.org/combine.specifications/omex-manifest");
        newentry.setAttribute("location", "./" + modelfile.toString());
        newentry.setAttribute("format", type.getFormat());
        
        root.addContent(newentry);
        Writer zwriter = Files.newBufferedWriter(manifestpath, StandardCharsets.UTF_8, StandardOpenOption.CREATE);  
        
        XMLOutputter xmloutputter = new XMLOutputter();
        xmloutputter.setFormat(Format.getPrettyFormat());;
        zwriter.write(xmloutputter.outputString(doc));
        zwriter.close();

	}

}
