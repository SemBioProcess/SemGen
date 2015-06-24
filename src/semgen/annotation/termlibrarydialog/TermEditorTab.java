package semgen.annotation.termlibrarydialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.routines.TermCollector;
import semgen.annotation.workbench.routines.TermModifier;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTabToolbar;
import semsim.model.SemSimTypes;

public class TermEditorTab extends JPanel implements ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private AnnotatorWorkbench workbench;
	private JList<String> typechooser = new JList<String>();
	private SemSimTermLibrary library;
	private JList<String> termlist = new JList<String>();
	private ArrayList<Integer> terms;
	
	private TermInformationPanel tip;
	private TermCollector affected;
	private EditorToolbar toolbar= new EditorToolbar();;
	
	private SemSimTypes[] types = new SemSimTypes[]{
			SemSimTypes.PHYSICAL_PROPERTY,
			SemSimTypes.REFERENCE_PHYSICAL_ENTITY,
			SemSimTypes.CUSTOM_PHYSICAL_ENTITY,
			SemSimTypes.COMPOSITE_PHYSICAL_ENTITY,
			SemSimTypes.PHYSICAL_PROCESS
	};
	
	public TermEditorTab(AnnotatorWorkbench wb) {
		workbench = wb;
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS)); 
		library = wb.openTermLibrary();
		setBackground(SemGenSettings.lightblue);
		makePanel();
	}
	
	private void makePanel() {
		String[] names = new String[types.length];
		for (int i = 0; i<types.length; i++) {
			names[i] = types[i].getName();
		}
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		typechooser.setAlignmentX(Box.LEFT_ALIGNMENT);
		typechooser.setAlignmentY(TOP_ALIGNMENT);
		typechooser.setListData(names);
		typechooser.addListSelectionListener(this);
		typechooser.setBorder(BorderFactory.createTitledBorder("Physical Types"));

		termlist.addListSelectionListener(this);
		termlist.setFont(SemGenFont.defaultPlain());
		SemGenScrollPane termscroller = new SemGenScrollPane(termlist);
		
		JPanel typepane = new JPanel();
		typepane.setLayout(new BoxLayout(typepane, BoxLayout.PAGE_AXIS)); 
		typepane.setBackground(SemGenSettings.lightblue);
		typepane.add(typechooser);
		typepane.add(termscroller);
		typepane.setBorder(BorderFactory.createEtchedBorder());
		
		typepane.add(Box.createVerticalGlue());
		add(typepane);
		
		add(toolbar);
	}
	
	private void updateList() {
		termlist.clearSelection();
		terms = library.getRequestedTypes(getTypeSelections());
		termlist.setListData(library.getComponentNames(terms).toArray(new String[]{}));
	}
	
	private SemSimTypes[] getTypeSelections() {
		ArrayList<SemSimTypes> list = new ArrayList<SemSimTypes>();
		
		for (int i : typechooser.getSelectedIndices()) {
			list.add(types[i]);
		}
		
		return list.toArray(new SemSimTypes[]{});
	}

	private void onTermSelection() {
		//Necessary to prevent memmory leaks
		clearPanel();
		if (termlist.isSelectionEmpty()) {
			validate();
			return;
		}
		
		affected = workbench.collectAffiliatedTermsandCodewords(getTermSelection());

		tip = new TermInformationPanel(workbench, affected);
		
		for (ContainerListener listener : getContainerListeners()) {
			tip.addContainerListener(listener);
		}
		for (ComponentListener listener : getComponentListeners()) {
			tip.addComponentListener(listener);
		}
		add(tip);
		validate();
	}
	
	private void clearPanel() {
		//Necessary to prevent memmory leaks
		if (tip!=null) {
			for (ContainerListener listener : getContainerListeners()) {
				tip.removeContainerListener(listener);
			}
			for (ComponentListener listener : getComponentListeners()) {
				tip.removeComponentListener(listener);
			}
			remove(tip);
		}
	}
	
	private Integer getTermSelection() {
		return terms.get(termlist.getSelectedIndex());
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		Object obj = arg0.getSource();
		if (obj.equals(typechooser)) {
			updateList();
			toolbar.toggleButtons();
		}
		if (obj.equals(termlist)) {
			onTermSelection();
			toolbar.toggleButtons();
		}
	}
	
	private void removeComponent() {
		int choice = JOptionPane.showConfirmDialog(this, 
				"Remove selected term and all references from the model and library?",
				"Confirm",
				JOptionPane.YES_NO_OPTION);
		if(JOptionPane.YES_OPTION == choice){
			new TermModifier(workbench, affected).runRemove();
			clearPanel();
			updateList();
			toolbar.toggleButtons();
		}
	}
	
	private void replaceComponent() {
		
	}
	
	private void modifyComponent() {
		
	}
	
	private class EditorToolbar extends SemGenTabToolbar implements ActionListener {
		private static final long serialVersionUID = 1L;
		private SemGenToolbarButton modifybtn = new SemGenToolbarButton(SemGenIcon.modifyicon);
		private SemGenToolbarButton removebtn = new SemGenToolbarButton(SemGenIcon.eraseicon);
		private SemGenToolbarButton replacebtn = new SemGenToolbarButton(SemGenIcon.replaceicon);
		
		public EditorToolbar() {
			super(JToolBar.VERTICAL);
			modifybtn.addActionListener(this);	
			replacebtn.setToolTipText("Replace selected term.");
			add(modifybtn);
			removebtn.addActionListener(this);
			add(removebtn);
			replacebtn.addActionListener(this);
			add(replacebtn);
			toggleButtons();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj.equals(modifybtn)) {
				modifyComponent();
			}
			if (obj.equals(removebtn)) {
				removeComponent();
			}
			if (obj.equals(replacebtn)) {
				replaceComponent();
			}
		}
		
		public void toggleButtons() {
			modifybtn.setToolTipText("Modify selected term.");
			removebtn.setToolTipText("Remove selected term.");
			if (!termlist.isSelectionEmpty()) {
				replacebtn.setEnabled(true);
				if (affected.targetIsReferenceTerm()) {
					modifybtn.setEnabled(false);
					modifybtn.setToolTipText("Reference terms cannot be modified.");
					if (affected.getTargetTermType().equals(SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE)) {
						removebtn.setEnabled(false);
						removebtn.setToolTipText("This property cannot be removed.");
						return;
					}
				}
				else {
					modifybtn.setEnabled(true);
				}
				removebtn.setEnabled(true);	
			}
			else {
				modifybtn.setEnabled(false);
				removebtn.setEnabled(false);		
				replacebtn.setEnabled(false);
			}
		}
	}
}