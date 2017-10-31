package semgen.utilities.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import semgen.SemGen;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelReader;
import semsim.reading.OMEXManifestreader;
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
	
	
	// This returns a list of accessors because if a user selects an OMEX files or JSim
	// project file there may be multiple models within the file they 
	// want to open
	public ArrayList<ModelAccessor> convertFileToModelAccessorList(File file) throws ParserConfigurationException, SAXException, URISyntaxException, JDOMException{
		ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();
		
		if (file.exists() && file.getName().toLowerCase().endsWith(".omex")) {
			try {
				OMEXManifestreader omexreader = new OMEXManifestreader(file);
				ArrayList<ModelAccessor> models = omexreader.getModelsInArchive();
				if (models.size()==1) {
					modelaccessors.add(models.get(0));
					return modelaccessors;
				}
				
				ArrayList<String> modelnames = new ArrayList<String>();
				for (ModelAccessor omexmodel : models) {
					modelnames.add(omexmodel.getModelName());
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
			
			if(modelnames.size()==1)  modelaccessors.add(new ModelAccessor(file, modelnames.get(0)));
			
			else{
				JSimModelSelectorDialogForReading pfmsd = 
						new JSimModelSelectorDialogForReading("Select model(s) in " + file.getName(), modelnames);
	
				for(Integer index : pfmsd.getSelectedModelNames()){
					modelaccessors.add(new ModelAccessor(file, modelnames.get(index)));
				}
			}
		}
		else modelaccessors.add(new ModelAccessor(file));
		
		return modelaccessors;
	}
	
	
	public ArrayList<ModelAccessor> getSelectedFilesAsModelAccessors(){
		ArrayList<ModelAccessor> modelaccessors = new ArrayList<ModelAccessor>();
		try {
		for (File file : getSelectedFiles())
			
				modelaccessors.addAll(convertFileToModelAccessorList(file));
			} catch (ParserConfigurationException | SAXException | URISyntaxException | JDOMException e) {
				e.printStackTrace();
			}
		
		return modelaccessors;
	}
	
}
