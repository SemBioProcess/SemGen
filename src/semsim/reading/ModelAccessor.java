package semsim.reading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;

import semsim.reading.ModelClassifier.ModelType;

public class ModelAccessor {

	protected String filepath;
	protected File file;
	protected String fragment = "";
	public static final String separator = "#";
	protected boolean isarchive = false;
	protected ModelType modeltype;	

	protected ModelAccessor() {}
		
	// Use this constructor for models that are stored as standalone files
	public ModelAccessor(File standAloneFile){
			try {
				modeltype = ModelClassifier.classify(standAloneFile);
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
			filepath = standAloneFile.getPath();
			file = new File(filepath);
	}
	
	// Use this constructor for models that are stored as standalone files
	public ModelAccessor(File standAloneFile, ModelType type){
			modeltype = type;
			filepath = standAloneFile.getPath();
			file = new File(filepath);
	}
	
	// Use this constructor for models that are stored within JSim Project files
	public ModelAccessor(File archiveFile, String modelNameInArchive){
			filepath = archiveFile.getPath();
			fragment = modelNameInArchive;
			file = new File(filepath + separator + fragment);
			isarchive = true;
			
			modeltype = ModelType.MML_MODEL_IN_PROJ;
	}
	

	
	// This constructor parses a string input and assigns values to the object's fields
	public ModelAccessor(String location){
				
		
		if(location.startsWith("http") || location.startsWith("file")) filepath = location;
				
		else if(location.contains(separator)){ // Account for locations formatted like C:\whatever\junk.owl#model1
			String path = location.substring(0, location.indexOf(separator));
			String frag = location.substring(location.indexOf(separator) + 1, location.length());

			filepath = path;
			fragment = frag;
			file = new File(filepath + frag);
		}
		
		else filepath = location;
			try {
				modeltype = ModelClassifier.classify(location);
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
		}
	
	// Copy constructor
	public ModelAccessor(ModelAccessor matocopy) {
		filepath = matocopy.filepath.toString();
		isarchive = matocopy.isarchive;
		modeltype = matocopy.modeltype;
	}

	public String getFilePath(){
		return new String(filepath);
	}
	
	public String getFullPath() {
		return new String(filepath + separator + fragment);
	}

	// This returns the archive uri if the model is part of an archive, 
	// or returns the standalone uri if the model is in a standalone file.
	public URI getFileThatContainsModelAsURI(){
		
		return file.toURI();
	}
	
	protected boolean isLocalFile(File file){
		if(modelIsOnline()) return false;
		return file.exists();
	}
	
	public File getFile() {
		return new File(filepath);
	}
	
	public boolean modelIsPartOfArchive(){
		return isarchive;
	}
	
	public boolean modelIsOnline(){
		return filepath.startsWith("http");
	}
	
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(modelInStream(), writer, Charset.defaultCharset());
		return writer.toString();
	}

	// Retrieve model text as a string (only for locally-stored models)
	public InputStream modelInStream() throws IOException{
		InputStream returnstring = null;
		
		if(modelIsOnline()) return null;

				if(modelIsPartOfJSimProjectFile()){
	
					Document projdoc = JSimProjectFileReader.getDocument(new FileInputStream(filepath));
					returnstring = JSimProjectFileReader.getModelSourceCode(projdoc, getFileName());
				}

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
	
	
	public Boolean modelIsPartOfJSimProjectFile(){
		return this.modeltype.equals(ModelType.MML_MODEL_IN_PROJ);
	}
	
	
	public String getFileName(){
		if(modelIsPartOfJSimProjectFile())
			return new String(fragment);
		else{
			return file.getName();
		}
	}
	
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		
		if( ! modelIsPartOfArchive()) return new File(this.filepath).getName();
		else{
			return filepath;
		}
	}
		
	//Overridden for OMEX archives
	public ModelType getFileType() {
		return modeltype;
	}
	
	public ModelType getModelType() {
		return modeltype;
	}
	
	public boolean equals(ModelAccessor ma){
		return getFile().getPath().matches(ma.getFile().getPath());
	}
	
	public boolean isArchive() {
		return isarchive;
	}

	public String getModelName() {
		//String filename =  
		return null;
	}
}
