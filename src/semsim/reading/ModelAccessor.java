package semsim.reading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ModelAccessor {

	private File archiveFile;
	private String modelNameInArchive; 
	private File standAloneFile;
	private static final String separator = "#";

	// Copy constructor
	public ModelAccessor(ModelAccessor matocopy) {
		
		if(matocopy.modelIsInStandAloneFile()) this.standAloneFile = matocopy.getStandAloneFile();
		else if(matocopy.modelIsPartOfArchive()){
			this.archiveFile = matocopy.archiveFile;
			this.modelNameInArchive = matocopy.modelNameInArchive;
		}
	}
		
	// Use this constructor for models that are stored as standalone files
	public ModelAccessor(File standAloneFile){
		this.standAloneFile = standAloneFile;
	}
	
	// Use this constructor for models that are stored within archive files
	public ModelAccessor(File archiveFile, String modelNameInArchive){
		this.archiveFile = archiveFile;
		this.modelNameInArchive = modelNameInArchive;
	}
	
	// This constructor parses a string input and assigns values to the object's fields
	public ModelAccessor(String location){
		
		if(location.toLowerCase().contains(".proj" + separator)){
			String archiveloc = location.substring(0, location.indexOf(separator));
		
			this.archiveFile = new File(archiveloc);
			this.modelNameInArchive = location.substring(location.indexOf(separator)+1, location.length());
		}
		else this.standAloneFile = new File(location);
	}

	private File getArchiveFile(){
		return archiveFile;
	}
	
	private String getModelNameInArchive(){
		return modelNameInArchive;
	}
	
	private File getStandAloneFile(){
		return standAloneFile;
	}

	// This returns the archive file if the model is part of an archive, 
	// or returns the standalone file if the model is in a standalone file.
	public File getFileThatContainsModel(){
		
		if(modelIsInStandAloneFile()) return getStandAloneFile();
		else if(modelIsPartOfArchive()) return getArchiveFile();
		
		return null;
	}
	
	public void setFileThatContainsModel(File file){
		if(modelIsInStandAloneFile()) standAloneFile = file;
		else if(modelIsPartOfArchive()) archiveFile = file;
	}
	
	public boolean modelIsInStandAloneFile(){
		return (standAloneFile!=null && archiveFile==null && modelNameInArchive==null);
	}
	
	public boolean modelIsPartOfArchive(){
		return (archiveFile!=null && modelNameInArchive!=null && standAloneFile==null);
	}
	
	// Retrieve model text as a string
	public String getModelTextAsString(){
		String returnstring = "";
		
		if(modelIsInStandAloneFile()){
			Scanner scanner = null;

			try {
				scanner = new Scanner(getStandAloneFile());
				while(scanner.hasNextLine()){
					String nextline = scanner.nextLine();
					returnstring = returnstring + nextline + "\n";
				}
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			finally{scanner.close();}
		}
		else if(modelIsPartOfArchive()){
			
			if(modelIsPartOfJSimProjectFile()){
				returnstring = JSimProjectFileReader.getModelCode(getArchiveFile(), getModelNameInArchive());
			}
		}
			
		return returnstring;
	}
	
	
	public Boolean modelIsPartOfJSimProjectFile(){
		
		if(modelIsPartOfArchive()){
			SAXBuilder builder = new SAXBuilder();
			try{
				Document doc = builder.build(archiveFile);
				String rootname = doc.getRootElement().getName();
				if(rootname.equals("JSim")){
					return true;
				}
			}
			catch(JDOMException | IOException e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	public String getModelName(){
		
		if(modelIsInStandAloneFile()){
			String filename = getStandAloneFile().getName();
			return filename.substring(0, filename.lastIndexOf("."));
		}
		else return getModelNameInArchive();
	}
	
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		
		if(modelIsInStandAloneFile()) return getStandAloneFile().getName();
		else if(modelIsPartOfArchive()) return getArchiveFile().getName() + separator + getModelNameInArchive();
		
		return null;
	}
	
	// If the model is in a standalone file, the full path to the file is returned
    // otherwise a string with format [path to archive] > [name of model] is returned
	public String getFullLocation(){
		
		if(modelIsInStandAloneFile()) return getStandAloneFile().getAbsolutePath();
		else if(modelIsPartOfArchive()) return getArchiveFile().getAbsolutePath() + separator + getModelNameInArchive();
		
		return null;
	}

}
