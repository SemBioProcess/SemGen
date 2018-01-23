package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.reading.AbstractRDFreader;
import semsim.reading.CASAreader;
import semsim.reading.OMEXManifestreader;
import semsim.reading.SemSimRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CASAwriter;
import semsim.writing.ModelWriter;
import semsim.writing.OMEXArchiveWriter;
import semsim.writing.SemSimRDFwriter;

public class OMEXAccessor extends ModelAccessor {
	
	protected ModelAccessor archivedfile;
	protected ModelAccessor casafile = null;
	protected ZipFile archive;
	
	public OMEXAccessor(File omexarchive, File file, ModelType type) {
		super(omexarchive, ModelType.OMEX_ARCHIVE);
		this.file = omexarchive;
		archivedfile = new ModelAccessor(file, type);
	}
	
	public OMEXAccessor(File omexarchive, File file, String fragment) {
		super(omexarchive, ModelType.OMEX_ARCHIVE);
		archivedfile = new JSIMProjectAccessor(file, fragment);
		
	}

	// Copy constructor
	public OMEXAccessor(OMEXAccessor matocopy) {
		super(matocopy);

		archivedfile = new ModelAccessor(matocopy.archivedfile);
		if (matocopy.casafile != null) {
			casafile = new ModelAccessor(matocopy.casafile);
		}
		

	}
	
	@Override
	public InputStream modelInStream() throws IOException {
		archive = new ZipFile(filepath);
		String path = archivedfile.getFilePath().replace('\\', '/');
		Enumeration<? extends ZipEntry> entries = archive.entries();
		while (entries.hasMoreElements()) {
			ZipEntry current = entries.nextElement();
			System.out.println(current.getName());
		}
		
		path = path.substring(2, path.length());
		ZipEntry entry = archive.getEntry(path);
		return archive.getInputStream(entry);
		
	}
	
	@Override
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(modelInStream(), writer, Charset.defaultCharset());
		return writer.toString();
	}
	
	public Document getJDOMDocument() {		
		Document doc = null;
		try{ 
			InputStream instream = modelInStream();
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(instream);
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public void closeStream() {		
		try {
			if (archive != null) {
				archive.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AbstractRDFreader getRDFreaderForModel(SemSimModel thesemsimmodel, String curationalrdf, SemSimLibrary sslib) 
			throws ZipException, IOException, JDOMException{
		
		AbstractRDFreader reader = getAssociatedCASAFile(thesemsimmodel, sslib);
		if(reader!=null) return reader;
		else return new SemSimRDFreader(this, thesemsimmodel, curationalrdf, sslib);

	}
	
	protected void getCASAFile() {

		try {
				ZipFile archive = new ZipFile(file);
				
				ArrayList<ModelAccessor> accs = OMEXManifestreader.getAnnotationFilesInArchive(archive, file);

				for(ModelAccessor acc : accs){
					
					//			String rdfincellml = "";
					
					if(acc.getModelType()==ModelType.CASA_FILE){
						
						    this.casafile = acc;
						    break;
					}
				}
				archive.close();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}


	}
	
	public CASAreader getAssociatedCASAFile(SemSimModel semsimmodel, SemSimLibrary sslib) throws ZipException, IOException, JDOMException {
		//Get the CASA file if this is an SBML model and it hasn't been retrieved yet.
		if (getModelType() == ModelType.SBML_MODEL && this.casafile==null) {
			this.getCASAFile();
		}	
		if (this.casafile==null) return null;	
		
			ZipFile archive = new ZipFile(file);
			ZipEntry entry = archive.getEntry(casafile.getFileName());
	        InputStream stream = archive.getInputStream(entry);
	        
	        Model casardf = ModelFactory.createDefaultModel();
	        Model cellmlrdf = ModelFactory.createDefaultModel();
	        
	        RDFReader casardfreader = casardf.getReader();
			casardfreader.setProperty("relativeURIs","same-document,relative");
			casardfreader.read(casardf, stream, "");
							
//			        if(rdfincellml.isEmpty()){
//			        	RDFReader cellmlrdfreader = cellmlrdf.getReader();
//			        	cellmlrdfreader.setProperty("relativeURIs","same-document,relative");
//			        	InputStream cellmlstream = new ByteArrayInputStream(rdfincellml.getBytes());
//			        	cellmlrdfreader.read(casardf, cellmlstream, AbstractRDFreader.TEMP_NAMESPACE);
//			        }
	        
    
	        // If we find a reference to the model in the RDF resources, we use the file as the source of the model's annotations
	        for(Resource subj : casardf.listSubjects().toSet()){
	        			        	
	        	if (subj.getNameSpace().equals(semsimmodel.getLegacyCodeLocation().getFileName() + "#")){
	        		AbstractRDFreader.stripSemSimRelatedContentFromRDFblock(cellmlrdf, semsimmodel); // when read in rdfreader in CellML file there may be annotations that we want to ignore
					casardf.add(cellmlrdf.listStatements()); // Add curatorial statements to rdf model. When instantiate CASA reader, need to provide all RDF statements as string.
					
					String combinedrdf = SemSimRDFwriter.getRDFmodelAsString(casardf);
					stream.close();
					archive.close();
					return new CASAreader(casafile, semsimmodel, sslib, combinedrdf);
	        	}
	        }		
	        stream.close();
	       archive.close();
		return null;
	}
	


	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		
		return getFileName() + '>'  + getModelName();

	}

	public String getDirectoryPath() {
		return file.getPath();
	}
	
	public String getFileName() {
		return this.archivedfile.getFileName();
	}
	
	public String getCASAFileName() {
		if (hasCASAFile()) {
			return casafile.getFileName();
		}
		return getModelName() + ".rdf";
	}
	
	public String getModelName() {
		return archivedfile.getModelName();
	}
	
	public ZipEntry getEntry() {
		return new ZipEntry(file.getPath());
	}
	
	@Override
	public void writetoFile(SemSimModel model) {
		ModelWriter writer = makeWriter(model);
		OMEXArchiveWriter omexwriter;
		
		if (this.getModelType()==ModelType.SBML_MODEL) {
			CASAwriter casawriter = new CASAwriter(model);
			if (!this.hasCASAFile()) {
				casafile = new ModelAccessor(new File("model/" + getModelName() + ".rdf"), ModelType.CASA_FILE);
			}
			omexwriter = new OMEXArchiveWriter(writer, casawriter);
		}
		else {
			omexwriter = new OMEXArchiveWriter(writer);
		}
		
		omexwriter.appendOMEXArchive(this);
	 }
	
	public ModelType getModelType() {
		return archivedfile.getModelType();
	}
	
	protected ModelWriter makeWriter(SemSimModel semsimmodel) {
		 return archivedfile.makeWriter(semsimmodel);
	}
	
	public boolean hasCASAFile() {
		return casafile != null;
	}
}
