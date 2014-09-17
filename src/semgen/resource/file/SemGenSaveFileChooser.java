package semgen.resource.file;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import semsim.reading.ModelClassifier;

public class SemGenSaveFileChooser extends SemGenFileChooser {
	private static final long serialVersionUID = 1L;
	
	public SemGenSaveFileChooser(String title) {
		super(title);
		setAcceptAllFileFilterUsed(false);
	}
	
	public SemGenSaveFileChooser(String title, String[] exts) {
		super(title);
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
	}
	
	public void setFileExtension() {
			String extension = null;
			File file =getSelectedFile();
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

			// If there's an extension for the file type, make sure the filename ends in it
			if(extension!=null){
				if (!file.getAbsolutePath().endsWith("." + extension.toLowerCase())
						&& !file.getAbsolutePath().endsWith("." + extension.toUpperCase())) {
						setSelectedFile(new File(file.getAbsolutePath() + "." + extension));
				} 
			}
	}
	
		public File SaveAsAction(){
			File file = null;
			while(true) {
				int returnVal = showSaveDialog(this);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					setFileExtension();
					file = getSelectedFile();
					
					if (file.exists()) {
						int overwriteval = JOptionPane.showConfirmDialog(this,
								"Overwrite existing file?", file.getName() + " already exists",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (overwriteval == JOptionPane.OK_OPTION) break;
						else {
							file = null;
						}
					}
				}
				else if (returnVal == JFileChooser.CANCEL_OPTION) {
					break;
				}
			}
			return file;
		}

}
