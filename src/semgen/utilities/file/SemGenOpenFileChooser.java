package semgen.utilities.file;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JFileChooser;

import org.jdom.Document;

import semgen.SemGen;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.utilities.SemSimUtil;

public class SemGenOpenFileChooser extends SemGenFileChooser {
	
	private static final long serialVersionUID = -9040553448654731532L;
		
	public SemGenOpenFileChooser(String title, Boolean multi){
		super(title);
		setMultiSelectionEnabled(multi);
		initialize();
		openFile();
	}
	
	public SemGenOpenFileChooser(String title, String[] filters, Boolean multi){
		super(title, filters);
		setMultiSelectionEnabled(multi);
		initialize();
		openFile();
	}
	
	public SemGenOpenFileChooser(Set<ModelAccessor> modelaccessors, String title, String[] filters){
		super(title, filters);
		setMultiSelectionEnabled(true);
		initialize();
		openFile(modelaccessors);
	}
	
	private void initialize(){
		setPreferredSize(filechooserdims);

		addChoosableFileFilter(fileextensions);
		setFileFilter(fileextensions);

	}
		
	private void openFile(Set<ModelAccessor> modelaccessors) {
		if (showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
			modelaccessors.addAll(getSelectedFilesAsModelAccessors());
		}
		else {
			setSelectedFiles(null);
			setSelectedFile(null);
		}
	}
	
	private void openFile() {	
		int choice = showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
		}
		else {
			setSelectedFiles(null);
			setSelectedFile(null);	}
	}
	
	public void closeAndWriteStringAsModelContent(URL url, String content){
		cancelSelection();
		String urlstring = url.toString();
		String name = urlstring.substring(urlstring.lastIndexOf("/"));
		
		File tempfile = new File(SemGen.tempdir.getAbsoluteFile() + "/" + name);
		SemSimUtil.writeStringToFile(content, tempfile);
	}
	
	
	// This returns a list of accessors because if a user selects a JSim
	// project file there may be multiple models within the file they 
	// want to open
	public ArrayList<ModelAccessor> convertFileToModelAccessorList(File file){
		ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();
		
		if(file.exists() && file.getName().toLowerCase().endsWith(".proj")){
			
			Document projdoc = JSimProjectFileReader.getDocument(file);
			ArrayList<String> modelnames = JSimProjectFileReader.getNamesOfModelsInProject(projdoc);
			
			if(modelnames.size()==1)  modelaccessors.add(new ModelAccessor(file, modelnames.get(0)));
			
			else{
				JSimModelSelectorDialogForReading pfmsd = 
						new JSimModelSelectorDialogForReading("Select model(s) in " + file.getName(), modelnames);
	
				for(String modelname : pfmsd.getSelectedModelNames()){
					modelaccessors.add(new ModelAccessor(file, modelname));
				}
			}
		}
		else modelaccessors.add(new ModelAccessor(file));
		
		return modelaccessors;
	}
	
	
	public ArrayList<ModelAccessor> getSelectedFilesAsModelAccessors(){
		ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();
		
		for (File file : getSelectedFiles())
			modelaccessors.addAll(convertFileToModelAccessorList(file));
		
		return modelaccessors;
	}
	
}
