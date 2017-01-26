package semsim.reading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ModelAccessor {

	private URI modelURI;
	public static final String separator = "#";

	// Copy constructor
	public ModelAccessor(ModelAccessor matocopy) {
		modelURI = URI.create(matocopy.modelURI.toString());
	}
		
	// Use this constructor for models that are stored as standalone files
	public ModelAccessor(File standAloneFile){
		modelURI = standAloneFile.toURI();
	}
	
	// Use this constructor for models that are stored within archive files
	public ModelAccessor(File archiveFile, String modelNameInArchive){
		modelURI = URI.create(archiveFile.toURI().toString() + separator + modelNameInArchive);
	}
	
	// This constructor parses a string input and assigns values to the object's fields
	public ModelAccessor(String location){
				
		if(location.startsWith("http") || location.startsWith("file")) modelURI = URI.create(location);
				
		else if(location.contains(separator)){ // Account for locations formatted like C:\whatever\junk.owl#model1
			String path = location.substring(0, location.indexOf(separator));
			String frag = location.substring(location.indexOf(separator) + 1, location.length());

			URI pathuri = new File(path).toURI();
			modelURI = URI.create(pathuri.toString() + separator + frag);
		}
		
		else modelURI = new File(location).toURI();
	}

	public URI getModelURI(){
		return modelURI;
	}
	
	public void setModelURI(URI uri){
		modelURI = uri;
	}
	

	// This returns the archive uri if the model is part of an archive, 
	// or returns the standalone uri if the model is in a standalone file.
	public URI getFileThatContainsModelAsURI(){
		
		if( ! modelIsPartOfArchive()) return modelURI;
		else{
			String uri = modelURI.toString();
			return URI.create(uri.substring(0, uri.indexOf(separator)));
		}
	}
	
	public File getFileThatContainsModel(){
		if(modelIsOnline()) return null;
		return new File(getFileThatContainsModelAsURI());
	}
	
	public boolean modelIsPartOfArchive(){
		return getModelURI().getFragment() != null;
	}
	
	public boolean modelIsOnline(){
		return ! getFileThatContainsModelAsURI().toString().startsWith("file");
	}
	
	// Retrieve model text as a string (only for locally-stored models)
	public String getLocalModelTextAsString(){
		String returnstring = "";
		
		if(modelIsOnline()) return null;
		else if(modelIsPartOfArchive()){
			
			if(modelIsPartOfJSimProjectFile()){
				Document projdoc = JSimProjectFileReader.getDocument(getFileThatContainsModel());
				returnstring = JSimProjectFileReader.getModelSourceCode(projdoc, getModelName());
			}
		}
		else if(getFileThatContainsModel().exists()){
			Scanner scanner = null;

			try {
				scanner = new Scanner(getFileThatContainsModel());
				
				while(scanner.hasNextLine()){
					String nextline = scanner.nextLine();
					returnstring = returnstring + nextline + "\n";
				}
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			finally{
				scanner.close();
			}
		}

		else if(modelIsPartOfArchive()){
			
			if(modelIsPartOfJSimProjectFile()){
				Document projdoc = ModelReader.getJDOMdocumentFromFile(getFileThatContainsModel());
				returnstring = JSimProjectFileReader.getModelSourceCode(projdoc, getModelName());
			}
		}
			
		return returnstring;
	}
	
	
	public Boolean modelIsPartOfJSimProjectFile(){
		
		if(modelIsPartOfArchive()){
			SAXBuilder builder = new SAXBuilder();
			
			try{
				Document doc = builder.build(getFileThatContainsModel());
				String rootname = doc.getRootElement().getName();
				
				if(rootname.equals("JSim")) return true;
			}
			catch(JDOMException | IOException e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	public String getModelName(){
		
		if(modelIsPartOfArchive())
			return getModelURI().getFragment();
		else{
			String filename = getFileThatContainsModel().getName();
			return filename.substring(0, filename.indexOf("."));
		}
	}
	
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		
		if( ! modelIsPartOfArchive()) return getFileThatContainsModel().getName();
		else{
			String archiveloc = modelURI.toString();
			return archiveloc.substring(archiveloc.lastIndexOf(File.separator)+1, archiveloc.length());
		}
	}
		
	
	public boolean equals(ModelAccessor ma){
		return modelURI.toString().equals(ma.getModelURI().toString());
	}
}
