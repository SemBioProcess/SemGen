package semgen.annotation.termlibrarydialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.AnnotatorWorkbench.ModelEdit;
import semgen.annotation.workbench.routines.TermCollector;
import semgen.annotation.workbench.routines.TermModifier;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTabToolbar;
import semsim.model.SemSimTypes;

public class TermEditorTab extends JPanel implements ListSelectionListener, AncestorListener, Observer {
	private static final long serialVersionUID = 1L;
	private AnnotatorWorkbench workbench;
	private JList<String> typechooser = new JList<String>();
	private SemSimTermLibrary library;
	private JList<String> termlist = new JList<String>();
	private ArrayList<Integer> terms;
	
	private TermInformationPanel tip;
	private TermCollector affected;
	private EditorToolbar toolbar= new EditorToolbar();
	private ReplaceTermPane replacer;
	private JPanel editpane = new JPanel();
	private JPanel editspacer = new JPanel();
	private JPanel curpane;
	private TermModifyPanel modifier;
	
	private SemSimTypes[] types = new SemSimTypes[]{
			SemSimTypes.PHYSICAL_PROPERTY,
			SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE,
			SemSimTypes.REFERENCE_PHYSICAL_ENTITY,
			SemSimTypes.CUSTOM_PHYSICAL_ENTITY,
			SemSimTypes.COMPOSITE_PHYSICAL_ENTITY,
			SemSimTypes.PHYSICAL_PROCESS
	};
	
	public TermEditorTab(AnnotatorWorkbench wb) {
		workbench = wb;
		workbench.addObserver(this);
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS)); 
		library = wb.openTermLibrary();
		this.addAncestorListener(this);
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
		Dimension dim = new Dimension(300,160);
		typechooser.setMinimumSize(dim);
		typechooser.setMaximumSize(dim);
		typechooser.setAlignmentY(TOP_ALIGNMENT);
		typechooser.setListData(names);
		typechooser.addListSelectionListener(this);
		typechooser.setBorder(BorderFactory.createTitledBorder("Physical Types"));
		
		termlist.addListSelectionListener(this);
		termlist.setFont(SemGenFont.defaultPlain());
		SemGenScrollPane termscroller = new SemGenScrollPane(termlist);
		Dimension tsdim = new Dimension(300,300);
		termscroller.setPreferredSize(tsdim);
		termscroller.setMaximumSize(tsdim);
		termscroller.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		JPanel typepane = new JPanel();
		typepane.setLayout(new BoxLayout(typepane, BoxLayout.PAGE_AXIS)); 
		typepane.setBackground(SemGenSettings.lightblue);
		typepane.setBorder(BorderFactory.createEtchedBorder());
		
		typepane.add(typechooser);
		typepane.add(termscroller);
		typepane.add(Box.createVerticalGlue());
		
		tip = new TermInformationPanel(workbench);
		replacer = new ReplaceTermPane(workbench);
		modifier = new TermModifyPanel(workbench);
		
		for (ContainerListener l : this.getContainerListeners()) {
			tip.addContainerListener(l);
			replacer.addContainerListener(l);
			modifier.addContainerListener(l);
		}
		for (ComponentListener l : getComponentListeners()) {
			tip.addComponentListener(l);
			replacer.addComponentListener(l);
			modifier.addComponentListener(l);
		}
		
		curpane = tip;
		add(typepane);
		editpane.setLayout(new BoxLayout(editpane, BoxLayout.PAGE_AXIS)); 
		editpane.setBackground(SemGenSettings.lightblue);
		editpane.add(toolbar);
		editpane.add(curpane);
		editspacer.add(editpane);
		add(editspacer);
		add(Box.createVerticalGlue());
		validate();
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
		if (termlist.isSelectionEmpty()) {
			validate();
			return;
		}
		
		affected = workbench.collectAffiliatedTermsandCodewords(getTermSelection());
		tip.updateInformation(affected);
		replaceComponent();
	}
	
	private void clearPanel() {
		tip.updateInformation(null);
		swapEditor(tip);
	}
	
	private Integer getTermSelection() {
		return terms.get(termlist.getSelectedIndex());
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		Object obj = arg0.getSource();
		if (obj.equals(typechooser)) {
			updateList();
		}
		if (obj.equals(termlist)) {
			onTermSelection();
		}
		toolbar.toggleButtons();
	}
	
	private void removeComponent() {
		int choice = JOptionPane.showConfirmDialog(this, 
				"Remove selected term and all references from the model and library?",
				"Confirm",
				JOptionPane.YES_NO_OPTION);
		if(JOptionPane.YES_OPTION == choice){
			new TermModifier(workbench, affected).runRemove();
		}
	}
	
	private void swapEditor(JPanel pan) {
		editpane.remove(curpane);
		curpane = pan;
		editpane.add(pan);
		validate();
		repaint();
	}
	
	private void replaceComponent() {
		if (affected.isUsed()) {
			replacer.showReplacementPanel(affected);
			swapEditor(replacer);
		}
		
	}
	
	private void modifyComponent() {
		modifier.showModifier(affected.getTermLibraryIndex());
		swapEditor(modifier);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1==ModelEdit.CODEWORD_CHANGED || arg1==ModelEdit.PROPERTY_CHANGED || arg1==ModelEdit.COMPOSITE_CHANGED) {
			if (affected==null) return;
			int i = affected.getTermLibraryIndex();
			if (!library.isTerm(i)) {
				affected = null;
				updateList();
				clearPanel();
			}
			else {
				termlist.setSelectedIndex(terms.indexOf(i));
				affected = new TermCollector(workbench, i);
			}
			toolbar.toggleButtons();	
			tip.updateInformation(affected);
		}
	}

	@Override
	public void ancestorAdded(AncestorEvent arg0) {
		
	}

	@Override
	public void ancestorMoved(AncestorEvent arg0) {
		
	}

	@Override
	public void ancestorRemoved(AncestorEvent arg0) {
			for (ContainerListener listener : getContainerListeners()) {
				replacer.removeContainerListener(listener);
				modifier.removeContainerListener(listener);
				tip.removeContainerListener(listener);
			}
			for (ComponentListener listener : getComponentListeners()) {
				replacer.removeComponentListener(listener);
				modifier.removeComponentListener(listener);
				tip.removeComponentListener(listener);
			}
		workbench.deleteObserver(this);	
	}

	private class EditorToolbar extends SemGenTabToolbar implements ActionListener {
		private static final long serialVersionUID = 1L;
		private SemGenToolbarRadioButton infobtn = new SemGenToolbarRadioButton(SemGenIcon.moreinfoicon);
		private SemGenToolbarRadioButton modifybtn = new SemGenToolbarRadioButton(SemGenIcon.modifyicon);
		private SemGenToolbarButton removebtn = new SemGenToolbarButton(SemGenIcon.eraseicon);
		private SemGenToolbarRadioButton replacebtn = new SemGenToolbarRadioButton(SemGenIcon.replaceicon);
		
		public EditorToolbar() {
			super(JToolBar.HORIZONTAL);
			infobtn.addActionListener(this);
			add(infobtn);
			infobtn.setSelected(true);
			infobtn.toggleSelectionGraphic();
			modifybtn.addActionListener(this);	
			add(modifybtn);
			replacebtn.setToolTipText("Replace selected term.");
			replacebtn.addActionListener(this);
			add(replacebtn);
			removebtn.addActionListener(this);
			add(removebtn);
			
			ButtonGroup sels = new ButtonGroup();
			sels.add(infobtn);
			sels.add(modifybtn);
			sels.add(replacebtn);
			
			toggleButtons();
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj.equals(infobtn)) {
				swapEditor(tip);
			}
			else if (obj.equals(modifybtn)) {
				modifyComponent();
			}
			
			else if (obj.equals(replacebtn)) {
				replaceComponent();
			}
			else if (obj.equals(removebtn)) {
				removeComponent();
				
			}
			infobtn.toggleSelectionGraphic();
			modifybtn.toggleSelectionGraphic();
			replacebtn.toggleSelectionGraphic();
		}
		
		public void toggleButtons() {
			modifybtn.setToolTipText("Modify selected term.");
			removebtn.setToolTipText("Remove selected term.");
			if (!termlist.isSelectionEmpty() && affected.isUsed()) {
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
