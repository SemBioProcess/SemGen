package semgen.annotation.termlibrarydialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenError;
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
		makePanel();
	}
	
	private void makePanel() {
		JPanel loadpane = new JPanel();
		loadpane.setLayout(new BoxLayout(loadpane, BoxLayout.LINE_AXIS)); 
		loadpane.add(loadbtn);
		loadpane.add(modellabel);
		loadbtn.addActionListener(this);
		
		add(loadpane);
		
		checklist.add(new JCheckBox("Import annotations for codewords with the same name"));
		checklist.add(new JCheckBox("Import annotations for submodels with the same name"));
		
		JPanel optionpane = new JPanel();
		optionpane.setLayout(new BoxLayout(optionpane, BoxLayout.PAGE_AXIS));
		optionpane.add(new JLabel("Import Options"));
		for (JCheckBox checkbox : checklist) {
			optionpane.add(checkbox);
		}
		
		importbtn.setEnabled(false);
		add(optionpane);
		add(importbtn);
	}

	private void selectModelFile() {
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model containing annotations", false);
		File sourcefile = sgc.getSelectedFile();
		if (sourcefile != null) {
			file = sourcefile;
			modellabel.setText(file.getName());
			importbtn.setEnabled(false);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(loadbtn)) {
			selectModelFile();
		}
		if (obj.equals(importbtn)) {
			SemGenError.showError("There were errors associated with the selected model. Not copying.", "Copy Model Failed");
		}
	}
}
