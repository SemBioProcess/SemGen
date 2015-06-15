package semgen.annotation.termlibrarydialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.file.SemGenOpenFileChooser;

public class ImportAnnotationsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	AnnotatorWorkbench workbench;
	JButton loadbtn = new JButton("Load Model");
	JLabel modellabel = new JLabel("<No model loaded>");
	ArrayList<JCheckBox> checklist = new ArrayList<JCheckBox>();
	JButton importbtn = new JButton("Import");
	File file = null;
	
	public ImportAnnotationsPanel(AnnotatorWorkbench wb) {
		workbench = wb;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
		makePanel();
	}
	
	private void makePanel() {
		JPanel loadpane = new JPanel();
		loadpane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		loadpane.setAlignmentX(LEFT_ALIGNMENT);
		loadpane.setBackground(SemGenSettings.lightblue);
		loadpane.setLayout(new BoxLayout(loadpane, BoxLayout.LINE_AXIS)); 
		loadpane.add(loadbtn);
		modellabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		modellabel.setFont(SemGenFont.defaultPlain());
		modellabel.setForeground(Color.red);
		loadpane.add(modellabel);
		loadbtn.addActionListener(this);
		
		add(loadpane);
		
		checklist.add(new JCheckBox("Import annotations for codewords with the same name"));
		checklist.add(new JCheckBox("Import annotations for submodels with the same name"));
		
		JPanel optionpane = new JPanel();
		optionpane.setBackground(SemGenSettings.lightblue);
		optionpane.setAlignmentX(LEFT_ALIGNMENT);
		optionpane.setLayout(new BoxLayout(optionpane, BoxLayout.PAGE_AXIS));
		JLabel inslbl = new JLabel("Import Options");
		inslbl.setFont(SemGenFont.defaultBold(1));
		optionpane.add(inslbl);
		for (JCheckBox checkbox : checklist) {
			checkbox.setFont(SemGenFont.defaultPlain());
			checkbox.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));
			optionpane.add(checkbox);
		}
		
		importbtn.setEnabled(false);
		optionpane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		add(optionpane);
		optionpane.add(importbtn);

		importbtn.addActionListener(this);
	}

	private void selectModelFile() {
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model containing annotations", new String[]{"owl"}, false);
		File sourcefile = sgc.getSelectedFile();
		if (sourcefile != null) {
			file = sourcefile;
			modellabel.setText(file.getName());
			modellabel.setForeground(Color.green);
			importbtn.setEnabled(true);
		}
	}
	
	private void doCopy() {
		Boolean[] options = new Boolean[checklist.size()];
		for (int i = 0; i < checklist.size(); i++) {
			options[i] = checklist.get(i).isSelected();
		}
		if (!workbench.importModelAnnotations(file, options)) {
			SemGenError.showError("There were errors associated with the selected model. Not copying.", "Copy Model Failed");
		}
		importbtn.setEnabled(false);
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
