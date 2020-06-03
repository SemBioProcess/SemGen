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
import semsim.reading.OMEXmetadataReader;
import semsim.reading.OMEXmanifestReader;
import semsim.reading.SemSimRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.OMEXmetadataWriter;
import semsim.writing.ModelWriter;
import semsim.writing.OMEXArchiveWriter;

/**
 * Specialized ModelAccessor for working with models in OMEX archives
 * @author mneal
 */
public class OMEXAccessor extends ModelAccessor {
	
	protected ModelAccessor archivedfile;
	protected OMEXAccessor casaaccessor = null;
	protected ZipFile archive;
	
	/**
	 * Constructor for pointing to a CellML or SBML model in an archive
	 * @param omexarchive A File pointing to the OMEX archive
	 * @param file A File pointing to a model within the OMEX archive
	 * @param type The type of the model within the OMEX archive
	 */
	public OMEXAccessor(File omexarchive, File file, ModelType type) {
		super(omexarchive, ModelType.OMEX_ARCHIVE);
		this.file = omexarchive;
		archivedfile = new ModelAccessor(file, type);
		fragment = file.getPath();
	}
	
	/**
	 * Constructor for pointing to a model within a JSim project file within an archive
	 * @param omexarchive A File pointing to the OMEX archive
	 * @param file A File pointing to a JSim project file within the OMEX archive
	 * @param fragment The name of the model within the JSim project file within the OMEX archive
	 */
	public OMEXAccessor(File omexarchive, File file, String fragment) {
		super(omexarchive, ModelType.OMEX_ARCHIVE);
		archivedfile = new JSimProjectAccessor(file, fragment);
		
	}

	/**
	 * Copy constructor
	 * @param matocopy the OMEXAccessor to copy
	 */
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
	
	@Override
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
	
	/** Close stream to archive */
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
				return new OMEXmetadataReader(this, thesemsimmodel, sslib, casardfstring);
			}
		}	
		return new SemSimRDFreader(archivedfile, thesemsimmodel, curationalrdf, sslib);

	}
	
	
	/** If the model is a SBML or CellML model, check for a CASA file and read it in if one exists */
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
	
	@Override
	public String getShortLocation(){
		return getFileName() + '>'  + getModelName();
	}

	@Override
	public String getDirectoryPath() {
		return file.getPath();
	}
	
	@Override
	public String getFileName() {
		return this.archivedfile.getFileName();
	}
	
	/** @return The name of the CASA file in the archive, if present */
	public String getCASAFileName() {
		if (hasCASAFile()) {
			return casaaccessor.getFileName();
		}
		return getModelName() + ".rdf";
	}
	
	@Override
	public String getModelName() {
		return archivedfile.getModelName();
	}
	
	/** @return The model location within the archive as a ZipEntry */
	public ZipEntry getEntry() {
		return new ZipEntry(file.getPath());
	}
	
	@Override
	public void writetoFile(SemSimModel model) {
		ModelWriter writer = makeWriter(model);
		OMEXArchiveWriter omexwriter;
		
		if (this.getModelType()==ModelType.SBML_MODEL || this.getModelType()==ModelType.CELLML_MODEL) {
			OMEXmetadataWriter casawriter = new OMEXmetadataWriter(model);
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
	
	@Override
	public ModelType getModelType() {
		return archivedfile.getModelType();
	}
	
	@Override
	protected ModelWriter makeWriter(SemSimModel semsimmodel) {
		 return archivedfile.makeWriter(semsimmodel);
	}
	
	/** @return Whether a CASA file has been associated with this OMEXAccessor */
	public boolean hasCASAFile() {
		return casaaccessor != null;
	}
}
