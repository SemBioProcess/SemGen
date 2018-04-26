package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.reading.AbstractRDFreader;
import semsim.reading.CASAreader;
import semsim.reading.OMEXmanifestReader;
import semsim.reading.SemSimRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CASAwriter;
import semsim.writing.ModelWriter;
import semsim.writing.OMEXArchiveWriter;

public class OMEXAccessor extends ModelAccessor {
	
	protected ModelAccessor archivedfile;
	protected OMEXAccessor casaaccessor = null;
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
		
		if (matocopy.casaaccessor != null)
			casaaccessor = new OMEXAccessor(matocopy.casaaccessor);
		
	}
	
	@Override
	public InputStream modelInStream() throws IOException {
		
		archive = new ZipFile(filepath.replace('\\', '/'));
		String path = archivedfile.getFilePath().replace('\\', '/');		
		
		ZipEntry entry = archive.getEntry(path);
		
		if (entry==null) {
			path = path.substring(2, path.length());
			entry = archive.getEntry(path);
		}
				
		return archive.getInputStream(entry);
		
	}
	
	@Override
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		
		IOUtils.copy(modelInStream(), writer, StandardCharsets.UTF_8);
		archive.close();
		return writer.toString();
	}
	
	public Document getJDOMDocument() {		
		Document doc = null;
		try{ 
			InputStream instream = modelInStream();
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(instream);
			instream.close();
			archive.close();
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
	public AbstractRDFreader createRDFreaderForModel(SemSimModel thesemsimmodel, String curationalrdf, SemSimLibrary sslib) 
			throws ZipException, IOException, JDOMException{
		
		
		if (getModelType() == ModelType.SBML_MODEL || getModelType() == ModelType.CELLML_MODEL) {
			
			if(casaaccessor == null){ // If the CASA file is not set for this archive, find the CASA file
				getCASAaccessor();
			}
						
			if (casaaccessor != null){
				String casardfstring = casaaccessor.getModelasString();
				return new CASAreader(this, thesemsimmodel, sslib, casardfstring);
			}
		}	
		return new SemSimRDFreader(archivedfile, thesemsimmodel, curationalrdf, sslib);

	}
	
	//If the model is a SBML or CellML model, check for a CASA file and read it if one exists
	protected void getCASAaccessor() {

		try {
				ZipFile archive = new ZipFile(file);
				
				ArrayList<OMEXAccessor> accs = OMEXmanifestReader.getAnnotationFilesInArchive(archive, file);

				for(OMEXAccessor acc : accs){
					//			String rdfincellml = "";
					if(acc.getModelType()==ModelType.CASA_FILE){	
					    this.casaaccessor = acc;
					    break;
					}
				}
				archive.close();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
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
			return casaaccessor.getFileName();
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
		
		if (this.getModelType()==ModelType.SBML_MODEL || this.getModelType()==ModelType.CELLML_MODEL) {
			CASAwriter casawriter = new CASAwriter(model);
			if (!this.hasCASAFile()) {

				casaaccessor = new OMEXAccessor(file, new File("model/" + getModelName() + ".rdf"), ModelType.CASA_FILE);
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
		return casaaccessor != null;
	}
}
