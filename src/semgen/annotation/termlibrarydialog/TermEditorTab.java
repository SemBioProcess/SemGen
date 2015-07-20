package semgen.annotation.termlibrarydialog;

import java.awt.Color;
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
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
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
	private boolean repbtnpressed = false;
	
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
		typechooser.setAlignmentY(TOP_ALIGNMENT);
		typechooser.setListData(names);
		typechooser.addListSelectionListener(this);
		typechooser.setBorder(BorderFactory.createTitledBorder("Physical Types"));
		
		termlist.addListSelectionListener(this);
		termlist.setFont(SemGenFont.defaultPlain());
		SemGenScrollPane termscroller = new SemGenScrollPane(termlist);
		termscroller.setPreferredSize(new Dimension(300,300));
		termscroller.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		JPanel typepane = new JPanel();
		typepane.setLayout(new BoxLayout(typepane, BoxLayout.PAGE_AXIS)); 
		typepane.setBackground(SemGenSettings.lightblue);
		typepane.setBorder(BorderFactory.createEtchedBorder());
		
		typepane.add(typechooser);
		typepane.add(termscroller);
		typepane.add(Box.createVerticalGlue());
		
		tip = new TermInformationPanel(workbench);
		
		add(typepane);
		add(toolbar);
		add(tip);
		add(Box.createHorizontalGlue());
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
		closeReplacer();
		validate();
		repaint();
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
	
	private void replaceComponent() {
		if (replacer==null) {
			replacer = new ReplaceTermPane(workbench);
			for (ContainerListener listener : getContainerListeners()) {
				replacer.addContainerListener(listener);
			}
			for (ComponentListener listener : getComponentListeners()) {
				replacer.addComponentListener(listener);
			}
		}
		
		if (repbtnpressed && affected.isUsed()) {
			replacer.showReplacementPanel(affected);
			add(replacer);
		}
		else closeReplacer();
		validate();
		repaint();
	}
	
	private void closeReplacer() {
			remove(replacer);
	}
	
	private void modifyComponent() {
		
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1==modeledit.codewordchanged || arg1==modeledit.propertychanged || arg1==modeledit.compositechanged) {
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
		if (replacer!=null) {
			for (ContainerListener listener : getContainerListeners()) {
				replacer.removeContainerListener(listener);
			}
			for (ComponentListener listener : getComponentListeners()) {
				replacer.removeComponentListener(listener);
			}
		}
		workbench.deleteObserver(this);	
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
		
		private void paintReplaceButton() {
			if (repbtnpressed) replacebtn.setBorder(BorderFactory.createLineBorder(Color.green));
			else replacebtn.setBorder(BorderFactory.createLineBorder(Color.red));
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
				repbtnpressed = !repbtnpressed;
				paintReplaceButton();
				replaceComponent();
			}
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
