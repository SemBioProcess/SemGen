package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
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
import semsim.reading.OMEXManifestreader;
import semsim.reading.SemSimRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CASAwriter;
import semsim.writing.ModelWriter;
import semsim.writing.OMEXArchiveWriter;

public class OMEXAccessor extends ModelAccessor {
	
	protected ModelAccessor archivedfile;
	protected ModelAccessor casaaccessor = null;
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
		if (matocopy.casaaccessor != null) {
			casaaccessor = new ModelAccessor(matocopy.casaaccessor);
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
		
		if (getModelType() == ModelType.SBML_MODEL || getModelType() == ModelType.CELLML_MODEL && this.casaaccessor==null) {
			
			getCASAaccessor();
			
			if (casaaccessor != null){
				String casardfstring = casaaccessor.getModelasString();
				return new CASAreader(this, thesemsimmodel, null, casardfstring);
			}
		}	
		return new SemSimRDFreader(archivedfile, thesemsimmodel, curationalrdf, sslib);

	}
	
	protected void getCASAaccessor() {

		try {
				ZipFile archive = new ZipFile(file);
				
				ArrayList<ModelAccessor> accs = OMEXManifestreader.getAnnotationFilesInArchive(archive, file);

				for(ModelAccessor acc : accs){
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
				casaaccessor = new ModelAccessor(new File("model/" + getModelName() + ".rdf"), ModelType.CASA_FILE);
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
