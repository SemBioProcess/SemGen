package semgen.utilities.file;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;

import semsim.model.collection.SemSimModel;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier;

public class SemGenSaveFileChooser extends SemGenFileChooser {
	private static final long serialVersionUID = 1L;
	private String modelInArchiveName;
	
	public SemGenSaveFileChooser() {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		setPreferredSize(filechooserdims);
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedext) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
		setFileFilter(getFilter(selectedext));
		setPreferredSize(filechooserdims);
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedext, String modelinarchivename) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
		setFileFilter(getFilter(selectedext));
		setPreferredSize(filechooserdims);
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
			if (! file.getName().toLowerCase().endsWith("." + extension.toLowerCase())) {
					setSelectedFile(new File(file.getAbsolutePath() + "." + extension));
			} 
		}
	}
	
	public ModelAccessor SaveAsAction(SemSimModel semsimmodel){
		
		ModelAccessor ma = null;
		
		while(true) {
			int returnVal = showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				setFileExtension();
				File filetosave = getSelectedFile();
				
				// If we're attempting to write a CellML model with discrete events, show error
				if( ! semsimmodel.getEvents().isEmpty() && getFileFilter()==SemGenFileChooser.cellmlfilter){
					JOptionPane.showMessageDialog(this, 
							"Cannot save as CellML because model contains discrete events", 
							"Cannot write to CellML", JOptionPane.WARNING_MESSAGE);
					continue;
				}
				
				boolean overwriting = false;

				// If we're saving to a JSim project file
				if(getFileFilter()==SemGenFileChooser.projfilter){
					
					// If we don't have a name for the model we're writing out yet
					// like during extraction and merging...
					if(modelInArchiveName == null){
						
						String modelname = null;
						
						// If the output file already exists
						if(filetosave.exists()){
							Document projdoc = JSimProjectFileReader.getDocument(filetosave);
							ArrayList<String> modelnames = JSimProjectFileReader.getNamesOfModelsInProject(projdoc);
							
							JSimModelSelectorDialogForWriting jms = 
									new JSimModelSelectorDialogForWriting(modelnames);
							
							modelname = jms.getSelectedModelName();

							if(modelname == null) return null;

							overwriting = modelnames.contains(modelname);
						}
						
						// Otherwise we're creating a new project with one as-yet-unnamed model
						else{
							JSimModelSelectorDialogForWriting jms = 
									new JSimModelSelectorDialogForWriting(new ArrayList<String>());
							
							modelname = jms.getSelectedModelName();
							
							if(modelname == null) return null;
						}
						ma = new ModelAccessor(filetosave, modelname);
					}
					
					// Otherwise we already have a name, like when we're saving from
					// the Annotator
					else{
						ma = new ModelAccessor(getSelectedFile(), modelInArchiveName);

						if(getSelectedFile().exists()){
							
							// If we're saving the model to a JSim project file, check if we'll be overwriting
							// a model with the same name
							Document projdoc = JSimProjectFileReader.getDocument(ma.getFileThatContainsModel());
							overwriting = (JSimProjectFileReader.getModelElement(projdoc, ma.getModelName()) != null);
						}
					}		
				}
				
				// Otherwise we're saving to a standalone file
				else{
					ma = new ModelAccessor(getSelectedFile());
					overwriting = ma.getFileThatContainsModel().exists();
				}
				
				// If we're overwriting a model...
				if (overwriting) {
					String overwritemsg = "Overwrite " + ma.getFileThatContainsModel().getName() + "?";
					
					if(ma.modelIsPartOfArchive()) 
						overwritemsg = "Overwrite model " + ma.getModelName() + " in " + ma.getFileThatContainsModel().getName() + "?";
					
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
		return ma;
	}
	
}
