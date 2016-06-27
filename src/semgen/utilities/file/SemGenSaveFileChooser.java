package semgen.utilities.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdom.Document;

import semsim.model.collection.SemSimModel;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier;

public class SemGenSaveFileChooser extends SemGenFileChooser implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private String modelInArchiveName;
	private static final JLabel warningMsg = new JLabel("Warning! Annotations are not preserved in the standalone MML format.");
	private JPanel warningMsgPanel;
	
	public SemGenSaveFileChooser() {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		setPreferredSize(filechooserdims);
		initializeWarningMsg();
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedext) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
		setFileFilter(getFilter(selectedext));
		setPreferredSize(filechooserdims);
		initializeWarningMsg();
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedtype, String modelinarchivename) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(getFilter(exts));
		setFileFilter(getFilter(selectedtype));
		setPreferredSize(filechooserdims);
		initializeWarningMsg();
		modelInArchiveName = modelinarchivename;
	}
	
	public void setFileExtension() {
		String type = null;
		File file = getSelectedFile();
		
		if(getFileFilter()==owlfilter){
			type = "owl";
			modeltype = ModelClassifier.SEMSIM_MODEL;
		}
		else if(getFileFilter()==sbmlfilter){
			type = "xml";
			modeltype = ModelClassifier.SBML_MODEL;
		}
		else if(getFileFilter()==cellmlfilter){
			type = "cellml";
			modeltype = ModelClassifier.CELLML_MODEL;
		}
		else if(getFileFilter()==mmlfilter){
			type = "mod";
			modeltype = ModelClassifier.MML_MODEL;
		}
		else if(getFileFilter()==projfilter){
			type = "proj";
			modeltype = ModelClassifier.MML_MODEL_IN_PROJ;
		}
		else if(getFileFilter()==csvfilter){
			type = "csv";
		}

		// If there's an extension for the file type, make sure the filename ends in it
		if(type!=null){
			if (! file.getName().toLowerCase().endsWith("." + type.toLowerCase())) {
					setSelectedFile(new File(file.getAbsolutePath() + "." + type));
			} 
		}
	}
	
	
	private void initializeWarningMsg(){
		addPropertyChangeListener(this);
		
		warningMsg.setForeground(Color.RED);
		warningMsg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		warningMsg.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
		warningMsg.setVisible(getFileFilter()==mmlfilter);
		
		warningMsgPanel = new JPanel();
		warningMsgPanel.add(warningMsg);
		warningMsgPanel.add(Box.createVerticalStrut(18));
		
		Component c = getComponent(getComponentCount()-1); // This is the bottom-most component
		
		if(c instanceof JPanel)	((JPanel)c).add(warningMsgPanel);
	}
	
	
	
	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		
		if (propertyfired.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)){
			warningMsg.setVisible(getFileFilter()==mmlfilter);
			validate();
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
					
					String modelname = null;
					ArrayList<String> existingmodelnames = new ArrayList<String>();
					
					// If the output file already exists
					if(filetosave.exists()){
						Document projdoc = JSimProjectFileReader.getDocument(filetosave);
						existingmodelnames = JSimProjectFileReader.getNamesOfModelsInProject(projdoc);	
					}
					
					JSimModelSelectorDialogForWriting jms = 
							new JSimModelSelectorDialogForWriting(existingmodelnames, modelInArchiveName);
					
					modelname = jms.getSelectedModelName();

					if(modelname == null) return null;

					overwriting = existingmodelnames.contains(modelname);
					ma = new ModelAccessor(filetosave, modelname);
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
					else return null;
				}
				break;
			}
			else if (returnVal == JFileChooser.CANCEL_OPTION) {
				return null;
			}
		}

		currentdirectory = getCurrentDirectory();

		return ma;
	}
	
}
