package semgen.annotation.termlibrarydialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.GenericWorker;
import semgen.utilities.SemGenJob;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.fileaccessors.ModelAccessor;

/**
 * Allows the importation of annotations from another model. The user can choose to only import the annotations into the library 
 * all the way to a full copy of the model, submodel and datastructure annotations.
 */
public class ImportAnnotationsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	AnnotatorWorkbench workbench;
	JButton loadbtn = new JButton("Load Model");
	JLabel modellabel = new JLabel("<No model loaded>");
	ArrayList<JCheckBox> checklist = new ArrayList<JCheckBox>();
	JButton importbtn = new JButton("Import");
	File file = null;
	JFrame parent;
	ModelAccessor sourcemodelaccessor = null;
	
	public ImportAnnotationsPanel(AnnotatorWorkbench wb, JFrame par) {
		workbench = wb;
		parent = par;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
		makePanel();
	}
	
	private void makePanel() {
		JPanel loadpane = new JPanel();
		loadpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		loadpane.setAlignmentX(LEFT_ALIGNMENT);
		loadpane.setBackground(SemGenSettings.lightblue);
		loadpane.setLayout(new BoxLayout(loadpane, BoxLayout.LINE_AXIS)); 
		loadpane.add(loadbtn);
		modellabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		modellabel.setFont(SemGenFont.defaultPlain());
		modellabel.setForeground(Color.gray);
		loadpane.add(modellabel);
		loadbtn.addActionListener(this);
		
		add(loadpane);
		
		checklist.add(new JCheckBox("Import model level annotations", true));
		checklist.add(new JCheckBox("Import annotations for codewords with the same name", true));
		checklist.add(new JCheckBox("Import annotations for submodels with the same name", true));
		
		JPanel optionpane = new JPanel();
		optionpane.setBackground(SemGenSettings.lightblue);
		optionpane.setAlignmentX(LEFT_ALIGNMENT);
		optionpane.setLayout(new BoxLayout(optionpane, BoxLayout.PAGE_AXIS));
		optionpane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));

		JLabel inslbl = new JLabel("Import Options");
		inslbl.setFont(SemGenFont.defaultBold(1));
		optionpane.add(inslbl);
		for (JCheckBox checkbox : checklist) {
			checkbox.setFont(SemGenFont.defaultPlain());
			checkbox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			optionpane.add(checkbox);
		}
		
		//importbtn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		importbtn.setEnabled(false);
		importbtn.addActionListener(this);
		optionpane.add(add(Box.createVerticalStrut(15)));
		optionpane.add(importbtn);
		add(optionpane);

	}

	
	private void selectModelFile() {
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model containing annotations", new String[]{"owl"}, false);
		
		ArrayList<ModelAccessor> sourceaccs = sgc.getSelectedFilesAsModelAccessors();
		
		if (sourceaccs.size() == 1 ) {
			sourcemodelaccessor = sourceaccs.get(0);
			File thefile = sourcemodelaccessor.getFile();
			modellabel.setText(thefile.getName());
			modellabel.setForeground(Color.blue);
			importbtn.setEnabled(true);
		}
	}
	
	
	private void doCopy() {
		
		Boolean[] options = new Boolean[checklist.size()];
		for (int i = 0; i < checklist.size(); i++) {
			options[i] = checklist.get(i).isSelected();
		}
		SemGenJob sgt = workbench.importModelAnnotations(sourcemodelaccessor, options);
		if (sgt == null) {
			SemGenError.showError("There were errors associated with the selected model. Not copying.", "Copy Model Failed");
		}
		GenericWorker worker = new GenericWorker(sgt, parent) {
			@Override
			public void endTask() {
				workbench.updateAllListeners();
				importbtn.setEnabled(false);
			}
		};
		
		worker.execute();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(loadbtn)) {
			selectModelFile();
		}
		
		if (obj.equals(importbtn)) {
			doCopy();
		}
	}
}
