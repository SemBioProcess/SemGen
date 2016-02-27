package semgen.utilities.file;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;

import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier;

public class SemGenSaveFileChooser extends SemGenFileChooser {
	private static final long serialVersionUID = 1L;
	private String modelInArchiveName;
	
	public SemGenSaveFileChooser(String title) {
		super(title);
		setAcceptAllFileFilterUsed(false);
	}
	
	public SemGenSaveFileChooser(String title, String[] exts, String selectedext) {
		super(title);
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
		setFileFilter(getFilter(selectedext));
	}
	
	public SemGenSaveFileChooser(String title, String[] exts, String selectedext, String modelinarchivename) {
		super(title);
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
		setFileFilter(getFilter(selectedext));
		modelInArchiveName = modelinarchivename;
	}
	
	public void setFileExtension() {
		String extension = null;
		File file = getSelectedFile();
		
		if(getFileFilter()==owlfilter){
			extension = "owl";
			modeltype = ModelClassifier.SEMSIM_MODEL;
		}
		else if(getFileFilter()==cellmlfilter){
			extension = "cellml";
			modeltype = ModelClassifier.CELLML_MODEL;
		}
		else if(getFileFilter()==mmlfilter){
			extension = "mod";
			modeltype = ModelClassifier.MML_MODEL;
		}
		else if(getFileFilter()==projfilter){
			extension = "proj";
			modeltype = ModelClassifier.MML_MODEL_IN_PROJ;
		}
		else if(getFileFilter()==csvfilter){
			extension = "csv";
		}

		// If there's an extension for the file type, make sure the filename ends in it
		if(extension!=null){
			if (! file.getAbsolutePath().toLowerCase().endsWith("." + extension.toLowerCase())) {
					setSelectedFile(new File(file.getAbsolutePath() + "." + extension));
			} 
		}
	}
	
		public File SaveAsAction(){
			
			ModelAccessor ma = null;
			
			while(true) {
				int returnVal = showSaveDialog(this);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					setFileExtension();
					
					boolean overwriting = false;

					// If we're saving to a JSim project file
					if(modelInArchiveName != null){
						ma = new ModelAccessor(getSelectedFile(), modelInArchiveName);
						
						if(getSelectedFile().exists()){
						
							// If we're saving the model to a JSim project file, check if we'll be overwriting
							// a model with the same name
							Document projdoc = JSimProjectFileReader.getDocument(ma.getFileThatContainsModel());
							overwriting = (JSimProjectFileReader.getModelElement(projdoc, ma.getModelName()) != null);
						}
					}
					else{
						ma = new ModelAccessor(getSelectedFile());
						overwriting = ma.getFileThatContainsModel().exists();
					}
					
					if (overwriting) {
						String overwritemsg = "Overwrite " + ma.getFileThatContainsModel().getName() + "?";
						
						if(ma.modelIsPartOfArchive()) 
							overwritemsg = "Overwrite model " + modelInArchiveName + " in " + ma.getFileThatContainsModel().getName() + "?";
						
						int overwriteval = JOptionPane.showConfirmDialog(this,
								overwritemsg, "Confirm overwrite",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (overwriteval == JOptionPane.OK_OPTION) break;
						else {
							return null;
						}
					}
					break;
				}
				else if (returnVal == JFileChooser.CANCEL_OPTION) {
					return null;
				}
			}
			return ma.getFileThatContainsModel();
		}

}
