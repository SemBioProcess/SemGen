package semsim.reading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jdom.Document;
import org.jdom.JDOMException;
import semsim.reading.ModelClassifier.ModelType;

public class ModelAccessor {

	private String baseURI;
	private String modelURI = "";
	public static final String separator = "#";
	private boolean isarchive = false;
	private ModelType basetype;
	private ModelType modeltype;
	
	// Copy constructor
	public ModelAccessor(ModelAccessor matocopy) {
		baseURI = matocopy.baseURI.toString();
		isarchive = matocopy.isarchive;
		basetype = matocopy.basetype;
		modeltype = matocopy.modeltype;
	}
		
	// Use this constructor for models that are stored as standalone files
	public ModelAccessor(File standAloneFile){
		try {
			basetype = ModelClassifier.classify(standAloneFile);
			modeltype = basetype;
			baseURI = standAloneFile.getPath();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}

	}
	
	// Use this constructor for models that are stored within JSim Project files
	public ModelAccessor(File archiveFile, String modelNameInArchive){
		try {
			baseURI = archiveFile.getPath().toString();
			modelURI = separator + modelNameInArchive;
			isarchive = true;
			
			basetype = ModelClassifier.classify(archiveFile);
			modeltype = basetype;
			
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}
	
	// This constructor parses a string input and assigns values to the object's fields
	public ModelAccessor(String location){
				
		if(location.startsWith("http") || location.startsWith("file")) baseURI = location;
				
		else if(location.contains(separator)){ // Account for locations formatted like C:\whatever\junk.owl#model1
			String path = location.substring(0, location.indexOf(separator));
			String frag = location.substring(location.indexOf(separator) + 1, location.length());

			baseURI = path;
			modelURI = separator + frag;
		}
		
		else baseURI = location;
		try {
			basetype = ModelClassifier.classify(new File(baseURI));
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	//OMEX Constructor
	public ModelAccessor(File omexarchive, File file) {
		try {
			baseURI = omexarchive.getPath().toString();
			modelURI = file.getPath();
			isarchive = true;
			
			basetype = ModelClassifier.classify(omexarchive);
			modeltype = ModelClassifier.classify(file);
			
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	public URI getBaseURI(){
		return new File(baseURI).toURI();
	}
	
	public void setModelURI(URI uri){
		baseURI = uri.toString();
	}
	

	// This returns the archive uri if the model is part of an archive, 
	// or returns the standalone uri if the model is in a standalone file.
	public URI getFileThatContainsModelAsURI(){
		
		return new File(baseURI).toURI();
	}
	
	public File getModelwithBaseFile(){
		if(modelIsOnline()) return null;
		return new File(baseURI + modelURI);
	}
	
	public File getModelFile() {
		return new File(modelURI);
	}
	
	public File getBaseFile() {
		return new File(baseURI);
	}
	
	public boolean modelIsPartOfArchive(){
		return isarchive;
	}
	
	public boolean modelIsOnline(){
		return baseURI.startsWith("http");
	}

	// Retrieve model text as a string (only for locally-stored models)
	public InputStream getLocalModelStream() throws ZipException, IOException{
		InputStream returnstring = null;
		
		if(modelIsOnline()) return null;
		else {
				if (this.modelIsPartofOMEXArchive()) {
						ZipFile archive = new ZipFile(getBaseFile());
						String path = getModelFile().getPath().substring(2).replace('\\', '/');
						
						ZipEntry entry = archive.getEntry(path);
						returnstring = archive.getInputStream(entry);
						
						//archive.close();
				}
				
				else if(modelIsPartOfJSimProjectFile()){
	
					Document projdoc = JSimProjectFileReader.getDocument(new FileInputStream(baseURI));
					returnstring = JSimProjectFileReader.getModelSourceCode(projdoc, getModelName());
				}
			
			else if(getModelwithBaseFile().exists()){
				try {
					returnstring = new FileInputStream(getModelwithBaseFile());
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
			
		return returnstring;
	}
	
	public Boolean modelIsPartofOMEXArchive() {
		
		return this.basetype.equals(ModelType.OMEX_ARCHIVE);
	}
	
	public Boolean modelIsPartOfJSimProjectFile(){
		return this.basetype.equals(ModelType.MML_MODEL_IN_PROJ);
	}
	
	
	public String getModelName(){
		
		if(modelIsPartOfJSimProjectFile())
			return modelURI.substring(1, modelURI.length());
		else{
			return getModelwithBaseFile().getName();
		}
	}
	
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		
		if( ! modelIsPartOfArchive()) return new File(this.baseURI).getName();
		else{
			return modelURI;
		}
	}
		
	public ModelType getFileType() {
		return modeltype;
	}
	
	public boolean equals(ModelAccessor ma){
		return getModelwithBaseFile().getPath().contentEquals(ma.getModelwithBaseFile().getPath());
	}
	
	public boolean isArchive() {
		return isarchive;
	}
}
