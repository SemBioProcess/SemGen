package semsim.fileaccessors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.jdom.Document;
import org.jdom.JDOMException;

import semgen.SemGen;
import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.reading.AbstractRDFreader;
import semsim.reading.ModelClassifier;
import semsim.reading.ModelReader;
import semsim.reading.SemSimRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CellMLwriter;
import semsim.writing.MMLwriter;
import semsim.writing.ModelWriter;
import semsim.writing.SBMLwriter;
import semsim.writing.SemSimOWLwriter;

/**
 * A class for accessing the contents of a model, whether stored in a standalone file,
 * within an archive, or online
 * @author mneal
 *
 */
public class ModelAccessor {

	protected String filepath;
	protected File file;
	protected String fragment = "";

	public static final String separator = "#";
	protected ModelType modeltype;	

	/**
	 * Constructor for models that are stored as standalone files (not in archives)
	 * @param standAloneFile The model file
	 */
	protected ModelAccessor(File standAloneFile){
			try {
				modeltype = ModelClassifier.classify(standAloneFile);
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
			filepath = standAloneFile.getPath();
			file = new File(filepath);
	}
	
	/** Constructor for models that are stored as standalone files where the ModelType is specified
	 * @param standAloneFile The model file
	 * @param type The model's {@link ModelType}
	 */
	protected ModelAccessor(File standAloneFile, ModelType type){
			modeltype = type;
			filepath = standAloneFile.getPath();
			file = standAloneFile;
	}
	
	/**
	 * Constructor for models JSim project files
	 * @param standAloneFile The JSim project file
	 * @param frag The name of the model in the project file
	 * @param type The model type
	 */
	protected ModelAccessor(File standAloneFile, String frag, ModelType type){
			modeltype = type;
			fragment = frag;
			filepath = standAloneFile.getPath();
			file = standAloneFile;
	}
	
	/**
	 *  Constructor for models that are stored online, not locally
	 * @param uri URI for the model location
	 * @param type The model's {@link ModelType}
	 */
	protected ModelAccessor(URI uri, ModelType type){
		filepath = uri.toString();
		modeltype = type;
	}
	
	/** 
	 * Copy constructor
	 * @param matocopy The ModelAccessor to copy
	 */
	public ModelAccessor(ModelAccessor matocopy) {
		filepath = matocopy.filepath;
		
		if(file != null){
			file = new File(matocopy.file.getPath());
		}
		
		modeltype = matocopy.modeltype;
	}

	/** @return The path to the file containing the model */
	public String getFilePath(){
		return new String(filepath);
	}
	
	/** @return The path to the model file. If the model is in an archive or
	 * a JSim project file, the path takes the form [path to archive/.proj file]#[name of model].
	 * Otherwise, this returns the same path as getFilePath().
	 */
	public String getFullPath() {
		if (!fragment.isEmpty()) return new String(filepath + separator + fragment);
		return new String(filepath);
	}

	/** @return The archive URI if the model is part of an archive, or returns the standalone URI if the model is in a standalone file. */
	public URI getFileThatContainsModelAsURI(){
		if(modelIsOnline()) return URI.create(filepath);
		else return file.toURI();
	}
	
	/** @return Get the parent directory of the file containing the model  */
	public String getDirectoryPath() {
		return file.getParent();
	}
	
	/** @return Whether the model is stored locally */
	public boolean isLocalFile(){
		if(modelIsOnline()) return false;
		return existingModel();
	}
	
	/** @return The File containing the model */
	public File getFile() {
		return new File(filepath);
	}

	/** @return Whether the model is online or not */
	public boolean modelIsOnline(){
		return filepath.startsWith("http");
	}
	
	/** @return Whether the file containing the model exists locally */
	public boolean existingModel() {
		return file.exists();
	}
	
	
	/** @return The model as a String
	 * @throws IOException
	 */
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		InputStream instream = modelInStream();
		
		if(instream != null){
			IOUtils.copy(instream, writer, StandardCharsets.UTF_8);
			instream.close();
		}
		
		return writer.toString();
	}

	/**
	 * @return The model text as an InputStream (only for locally-stored models)
	 * @throws IOException
	 */
	public InputStream modelInStream() throws IOException{
		InputStream returnstring = null;
		
		if(modelIsOnline()) return null;
		else {
			try {
				returnstring = new FileInputStream(file);
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
			
		return returnstring;
	}
	
	/**
	 * Get an appropriate ModelWriter given the type of model specified by the modeltype parameter
	 * and the {@link SemSimModel} to write out
	 * @param semsimmodel The SemSimModel that will be written out
	 * @return A {@link ModelWriter} that will output according to the model type specified by the modeltype parameter
	 */
	protected ModelWriter makeWriter(SemSimModel semsimmodel) {
		
		ModelWriter writer = null;
		try {
			if(modeltype==ModelType.SEMSIM_MODEL) {
				writer = new SemSimOWLwriter(semsimmodel);
			}
			else if(modeltype==ModelType.SBML_MODEL){
				writer = new SBMLwriter(semsimmodel);
			}
			else if(modeltype==ModelType.CELLML_MODEL){
				writer = new CellMLwriter(semsimmodel);
			}
			else if(modeltype==ModelType.MML_MODEL){
				writer = new MMLwriter(semsimmodel);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return writer;
		
}

	/**
	 * Write out a {@link SemSimModel} to the ModelAccessor's file location
	 * @param model The {@link SemSimModel} to write out
	 */
	public void writetoFile(SemSimModel model) {
		ModelWriter writer = makeWriter(model);
		try {
			String modelstring = writer.encodeModel();
			PrintWriter pwriter = new PrintWriter(new FileWriterWithEncoding(getFile(),"UTF-8"), true);
			pwriter.print(modelstring);
			pwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			SemGen.logfilewriter.println(getShortLocation() + " write failed.");
		}
		
		SemGen.logfilewriter.println(getShortLocation() + " was saved");
		
	}


	/** @return The name of the file that contains the model */
	public String getFileName(){
			return file.getName();
	}
	
	/** @return A JDOM Document object read in from a modeling file */
	public Document getJDOMDocument() {
		return ModelReader.getJDOMdocumentFromFile(file);
	}
	
	
	/** @return A truncated path to the model */
	public String getShortLocation(){		
		return new File(this.filepath).getName();
	}
	
	/** @return The type of model that the ModelAccessor accesses */
	public ModelType getModelType() {
		return modeltype;
	}
	
	/**
	 * @param name The name of a file
	 * @return Whether a file with the input name is contained within the same directory as the 
	 * model that the ModelAccessor accesses
	 */
	public boolean sharesDirectory(String name) {
		File samedirfile = new File (
				getDirectoryPath() + 
				"/" + name);
		
		return samedirfile.exists();
	}
	
	/**
	 * @param ma A ModelAccessor
	 * @return Whether this ModelAccessor is equivalent to the input ModelAccessor
	 */
	public boolean equals(ModelAccessor ma){
		return getFile().getPath().contentEquals(ma.getFile().getPath());
	}

	/** @return The name of the model being accessed by this ModelAccessor */
	public String getModelName() {
		String filename = file.getName();
		return filename.substring(0, filename.indexOf('.'));
	}

	/**
	 * Create an appropriate {@link AbstractRDFreader} to read in RDF-based annotations for a model
	 * @param thesemsimmodel The {@link SemSimModel} to be annotated
	 * @param curationalrdf Existing RDF-based curational metadata for the model
	 * @param sslib A {@link SemSimLibrary} instance
	 * @return An appropriate {@link AbstractRDFreader} to read in RDF-based annotations for a model
	 * @throws ZipException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public AbstractRDFreader createRDFreaderForModel(SemSimModel thesemsimmodel, String curationalrdf, SemSimLibrary sslib) 
			throws ZipException, IOException, JDOMException{
				
		return new SemSimRDFreader(this, thesemsimmodel, curationalrdf, sslib);
		
	}
}
