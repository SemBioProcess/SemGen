package semgen.annotation.termlibrarydialog;

import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.routines.TermCollector;
import semsim.model.SemSimTypes;
import semsim.utilities.ReferenceOntologies.OntologyDomain;

public class ReplaceTermPane extends JPanel{
	private static final long serialVersionUID = 1L;
	private AnnotatorWorkbench workbench;
	private TermCollector affected;
	private SemSimTermLibrary library;
	private ExistingTermPane existpane = new ExistingTermPane();
	private JPanel replacepan;
	private JRadioButton existbtn = new JRadioButton("Existing Term");
	private JRadioButton importbtn = new JRadioButton("Imported Term");
	
	public ReplaceTermPane(AnnotatorWorkbench wb, TermCollector aff) {
		workbench = wb;
		affected = aff;
		library = wb.openTermLibrary();
		
		drawPanel();
	}
	
	public void redraw() {
		
	}
	
	private void drawPanel() {
		setBackground(SemGenSettings.lightblue);
		JPanel toppane = new JPanel();
		toppane.setBackground(SemGenSettings.lightblue);
		JLabel desc = new JLabel("Replace with:");
		desc.setBackground(SemGenSettings.lightblue);
		
		existbtn.setSelected(true);
		ButtonGroup btngroup = new ButtonGroup();
		btngroup.add(existbtn);
		btngroup.add(importbtn);
		
		toppane.add(desc);
		toppane.add(existbtn);
		toppane.add(importbtn);
		add(toppane);
		
		showReplacementPanel();
	}
	
	private void showReplacementPanel() {
		ArrayList<Integer> terms = null;
		OntologyDomain domain = null;
		importbtn.setEnabled(true);
		switch (affected.getTargetTermType()) {
		case PHYSICAL_PROPERTY:
			terms = library.getSortedPhysicalPropertyIndicies();
			domain = OntologyDomain.PhysicalProperty;
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			terms = library.getSortedAssociatePhysicalPropertyIndicies();
			domain = OntologyDomain.PhysicalProperty;
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			terms = library.getRequestedTypes(new SemSimTypes[]{SemSimTypes.CUSTOM_PHYSICAL_ENTITY, SemSimTypes.REFERENCE_PHYSICAL_ENTITY});
			domain = OntologyDomain.PhysicalEntity;
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			terms = library.getRequestedTypes(new SemSimTypes[]{SemSimTypes.CUSTOM_PHYSICAL_ENTITY, SemSimTypes.REFERENCE_PHYSICAL_ENTITY});
			domain = OntologyDomain.PhysicalEntity;
			break;
		case COMPOSITE_PHYSICAL_ENTITY:
			terms = library.getSortedCompositePhysicalEntityIndicies();
			existbtn.setSelected(true);
			importbtn.setEnabled(false);
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			terms = library.getRequestedTypes(new SemSimTypes[]{SemSimTypes.CUSTOM_PHYSICAL_PROCESS, SemSimTypes.REFERENCE_PHYSICAL_PROCESS});
			domain = OntologyDomain.PhysicalProcess;
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			terms = library.getRequestedTypes(new SemSimTypes[]{SemSimTypes.CUSTOM_PHYSICAL_PROCESS, SemSimTypes.REFERENCE_PHYSICAL_PROCESS});
			domain = OntologyDomain.PhysicalProcess;
			break;
		default:
			break;
		}
		if (existbtn.isSelected()) {
			showExisting(terms);
		}
		else {
			showImporter(domain);
		}
	}
	
	private void showExisting(ArrayList<Integer> terms) {
		existpane.updateTermPanel(terms);
		replacepan = existpane;
	}
	
	private void showImporter(OntologyDomain domain) {
		
	}
	
	private class ExistingTermPane extends JPanel {
		private static final long serialVersionUID = 1L;
		ArrayList<Integer> options;
		JList<String> list = new JList<String>();
		JButton replacebtn = new JButton("Replace");
		
		public ExistingTermPane() {
			replacebtn.setEnabled(false);
			add(list);
			add(replacebtn);
		}
		
		public void updateTermPanel(ArrayList<Integer> terms) {
			list.clearSelection();
			replacebtn.setEnabled(false);
			options = terms;
			list.setListData(library.getComponentNames(terms).toArray(new String[]{}));
		}
		
	}
}
