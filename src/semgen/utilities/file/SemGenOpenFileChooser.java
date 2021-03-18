package semgen.utilities.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelReader;
import semsim.reading.OMEXmanifestReader;

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
		if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
			modelaccessors.addAll(getSelectedFilesAsModelAccessors());
		}
		else {
			setSelectedFiles(null);
			setSelectedFile(null);
		}
	}
	
	private void openFile() {	
		int choice = showOpenDialog(null);
		if (choice == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
		}
		else {
			setSelectedFiles(null);
			setSelectedFile(null);	}
	}

	
	// This returns a list of accessors because if a user selects an OMEX files or JSim
	// project file there may be multiple models within the file they 
	// want to open
	public ArrayList<ModelAccessor> convertFileToModelAccessorList(File file) throws ParserConfigurationException, SAXException, URISyntaxException, JDOMException{
		ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();
		
		
		
		if (file.exists() && file.getName().toLowerCase().endsWith(".omex")) {
			
			try {
				ZipFile zfile = new ZipFile(file);

				ArrayList<ModelAccessor> models = OMEXmanifestReader.getModelsInArchive(zfile, file);
				
				if (models.size()==1) {
					modelaccessors.add(models.get(0));
					return modelaccessors;
				}
				
				ArrayList<String> modelnames = new ArrayList<String>();
				for (ModelAccessor omexmodel : models) {
					modelnames.add(omexmodel.getFileName());
				}
				
				
				JSimModelSelectorDialogForReading pfmsd = 
						new JSimModelSelectorDialogForReading("Select model(s) in " + file.getName(), modelnames);
	
				for(Integer index : pfmsd.getSelectedModelNames()){
					modelaccessors.add(models.get(index));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(file.exists() && file.getName().toLowerCase().endsWith(".proj")){
			
			Document projdoc = ModelReader.getJDOMdocumentFromFile(file);
			ArrayList<String> modelnames = JSimProjectFileReader.getNamesOfModelsInProject(projdoc);
			
			if(modelnames.size()==1)  modelaccessors.add(FileAccessorFactory.getJSIMprojectAccessor(file, modelnames.get(0)));
			
			else{
				JSimModelSelectorDialogForReading pfmsd = 
						new JSimModelSelectorDialogForReading("Select model(s) in " + file.getName(), modelnames);
	
				for(Integer index : pfmsd.getSelectedModelNames()){
					modelaccessors.add(FileAccessorFactory.getJSIMprojectAccessor(file, modelnames.get(index)));
				}
			}
		}
		else modelaccessors.add(FileAccessorFactory.getModelAccessor(file));
		
		return modelaccessors;
	}
	
	
	// NEED TO SEE HOW SEMGENOPENFILECHOOSER gets selected files set
	public ArrayList<ModelAccessor> getSelectedFilesAsModelAccessors(){
		
		ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();

		try {

			if(isMultiSelectionEnabled())
				for (File file : getSelectedFiles()) {
					modelaccessors.addAll(convertFileToModelAccessorList(file));
				}
			else
				modelaccessors.addAll(convertFileToModelAccessorList(getSelectedFile()));
			} catch (ParserConfigurationException | SAXException | URISyntaxException | JDOMException e) {
				e.printStackTrace();
			}
		
		return modelaccessors;
	}
	
}
