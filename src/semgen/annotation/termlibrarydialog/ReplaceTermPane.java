package semgen.annotation.termlibrarydialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.SemGenSettings;
import semgen.annotation.dialog.termlibrary.ReferenceClassFinderPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.routines.TermCollector;
import semsim.PropertyType;
import semsim.model.SemSimTypes;
import semsim.utilities.ReferenceOntologies.OntologyDomain;

public class ReplaceTermPane extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private AnnotatorWorkbench workbench;
	private TermCollector affected;
	private SemSimTermLibrary library;
	private ExistingTermPane existpane;
	private JPanel replacepan;
	private JRadioButton existbtn = new JRadioButton("Existing Term");
	private JRadioButton importbtn = new JRadioButton("Imported Term");
	private JButton replacebtn = new JButton("Replace");
	private JButton replaceremovebtn = new JButton("Replace and Remove");
	
	public ReplaceTermPane(AnnotatorWorkbench wb, TermCollector aff) {
		workbench = wb;
		affected = aff;
		library = wb.openTermLibrary();
		
		drawPanel();
	}
	
	private void drawPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
		JPanel toppane = new JPanel();
		toppane.setBackground(SemGenSettings.lightblue);
		JLabel desc = new JLabel("Replace with:");
		desc.setBackground(SemGenSettings.lightblue);
		
		existbtn.setSelected(true);
		ButtonGroup btngroup = new ButtonGroup();
		btngroup.add(existbtn);
		btngroup.add(importbtn);
		existbtn.addActionListener(this);
		importbtn.addActionListener(this);
		replacebtn.addActionListener(this);
		replaceremovebtn.addActionListener(this);
		
		toppane.add(desc);
		toppane.add(existbtn);
		toppane.add(importbtn);
		add(toppane);

		showReplacementPanel();
	}
	
	private void showReplacementPanel() {
		if (replacepan != null) remove(replacepan); 
		ArrayList<Integer> terms = null;
		OntologyDomain domain = null;
		importbtn.setEnabled(true);
		replaceremovebtn.setEnabled(true);
		
		switch (affected.getTargetTermType()) {
		case PHYSICAL_PROPERTY:
			terms = library.getSortedPhysicalPropertyIndicies();
			domain = OntologyDomain.PhysicalProperty;
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			PropertyType pt = library.getPropertyinCompositeType(affected.getTermLibraryIndex());
			terms = library.getSortedAssociatePhysicalPropertyIndiciesbyPropertyType(pt);
			domain = OntologyDomain.AssociatePhysicalProperty;
			replaceremovebtn.setEnabled(false);
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
		validate();
	}
	
	private void showExisting(ArrayList<Integer> terms) {
		existpane = new ExistingTermPane();
		existpane.updateTermPanel(terms);
		replacepan = existpane;
		add(replacepan);
	}
	
	private void showImporter(OntologyDomain domain) {
		SearchPane importpane = new SearchPane(library, domain);
		replacepan = importpane;
		add(replacepan);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		
		if (obj.equals(existbtn) || obj.equals(importbtn)) {
			showReplacementPanel();
		}
		if (obj.equals(replacebtn)) {
			Integer repindex = ((GetSelection)replacepan).getSelection();
			workbench.replaceTerm(affected, repindex, false);
		}
		if (obj.equals(replaceremovebtn)) {
			Integer repindex = ((GetSelection)replacepan).getSelection();
			workbench.replaceTerm(affected, repindex, true);
		}
	}
	
	private JPanel addButtonPane() {
		JPanel btnpane = new JPanel();
		btnpane.setLayout(new BoxLayout(btnpane, BoxLayout.LINE_AXIS));
		btnpane.add(replaceremovebtn);
		btnpane.add(replacebtn);
		btnpane.setBackground(SemGenSettings.lightblue);
		return btnpane;
	}
	
	private interface GetSelection {
		public int getSelection();
	}
	
	private class ExistingTermPane extends JPanel implements ListSelectionListener, GetSelection {
		private static final long serialVersionUID = 1L;
		
		ArrayList<Integer> options;
		JList<String> list = new JList<String>();
		
		public ExistingTermPane() {
			setAlignmentY(TOP_ALIGNMENT);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
			setBackground(SemGenSettings.lightblue);
			replacebtn.setEnabled(false);
			replaceremovebtn.setEnabled(false);
			list.setPreferredSize(new Dimension(300,300));
			list.addListSelectionListener(this);
			add(list);
			add(addButtonPane());
		}
		
		public void updateTermPanel(ArrayList<Integer> terms) {
			list.clearSelection();
			replacebtn.setEnabled(false);
			options = terms;
			
			//Remove the term being replaced as an option
			options.remove(affected.getTermLibraryIndex());
			list.setListData(library.getComponentNames(options).toArray(new String[]{}));
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			replacebtn.setEnabled(!list.isSelectionEmpty());
			replaceremovebtn.setEnabled(!list.isSelectionEmpty());
		}

		@Override
		public int getSelection() {
			return options.get(list.getSelectedIndex());
		}
	}
	
	public class SearchPane extends ReferenceClassFinderPanel implements ListSelectionListener, GetSelection {
		private static final long serialVersionUID = 1L;
		private JButton clearbtn = new JButton("Clear");
		
		public SearchPane(SemSimTermLibrary lib, OntologyDomain dom) {
			super(lib, dom);
			JPanel bp = addButtonPane();
			bp.add(clearbtn);
			clearbtn.addActionListener(new ClearAction());
			replacebtn.setEnabled(false);
			replaceremovebtn.setEnabled(false);
			add(bp);
		}
		
		public void valueChanged(ListSelectionEvent arg0) {
	        boolean adjust = arg0.getValueIsAdjusting();
	        if (!adjust) {
	        	replacebtn.setEnabled(!resultslistright.isSelectionEmpty());
				replaceremovebtn.setEnabled(!resultslistright.isSelectionEmpty());
		        externalURLbutton.setEnabled(!resultslistright.isSelectionEmpty());
	        }
		}
		
		public void performSearch() {
			super.performSearch();
			if (domain==OntologyDomain.AssociatePhysicalProperty) {
				
			}
		}

		public void makeTerm() {
			addTermtoLibrary();
		}

		public void clearForm() {
			clear();
		}
		
		public class ClearAction implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearForm();
			}			
		}

		@Override
		public int getSelection() {
			addTermtoLibrary();
			return getSelectedTermIndex();
		}
	}



}
