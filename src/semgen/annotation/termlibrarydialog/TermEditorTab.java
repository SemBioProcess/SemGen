package semgen.annotation.termlibrarydialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.ModelEdit;
import semgen.annotation.workbench.routines.TermCollector;
import semgen.annotation.workbench.routines.TermModifier;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTabToolbar;
import semsim.annotation.SemSimTermLibrary;
import semsim.annotation.SemSimTermLibrary.LibraryEvent;
import semsim.definitions.SemSimTypes;

public class TermEditorTab extends JPanel implements ListSelectionListener, Observer {
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
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setAlignmentY(Component.TOP_ALIGNMENT);
		library = wb.openTermLibrary();
		library.addObserver(this);
		setBackground(SemGenSettings.lightblue);
		makePanel();
	}
	
	private void makePanel() {
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		Dimension dim = new Dimension(360,140);
		typechooser.addListSelectionListener(this);
		
		SemGenScrollPane typechoosepane = new SemGenScrollPane(typechooser);
		typechoosepane.setBorder(BorderFactory.createTitledBorder("Physical Types"));
		typechoosepane.setAlignmentX(Component.LEFT_ALIGNMENT);
		typechoosepane.setAlignmentY(Component.TOP_ALIGNMENT);
		typechoosepane.setBackground(SemGenSettings.lightblue);
		typechoosepane.setMinimumSize(dim);
		typechoosepane.setMaximumSize(dim);
		
		termlist.addListSelectionListener(this);
		termlist.addMouseMotionListener(new ListTooltip(termlist));
		termlist.setFont(SemGenFont.defaultPlain());
		termlist.setForeground(Color.blue);
		
		SemGenScrollPane termscroller = new SemGenScrollPane(termlist);
		Dimension tsdim = new Dimension(360,300);
		termscroller.setPreferredSize(tsdim);
		termscroller.setMaximumSize(tsdim);
		termscroller.setAlignmentX(Box.LEFT_ALIGNMENT);
		termscroller.setBackground(SemGenSettings.lightblue);
		termscroller.setBorder(BorderFactory.createTitledBorder("Instances"));
		
		JPanel typepane = new JPanel();
		typepane.setLayout(new BoxLayout(typepane, BoxLayout.PAGE_AXIS)); 
		typepane.setBackground(SemGenSettings.lightblue);
		typepane.setBorder(BorderFactory.createEtchedBorder());
	
		typepane.add(Box.createVerticalStrut(12));
		typepane.add(typechoosepane);
		typepane.add(termscroller);
		typepane.add(Box.createVerticalGlue());
		
		typepane.setAlignmentY(Component.TOP_ALIGNMENT);
		typepane.setAlignmentX(Component.LEFT_ALIGNMENT);
		typepane.setMaximumSize(new Dimension(360,9999));
		
		tip = new TermInformationPanel(workbench);
		replacer = new ReplaceTermPane(workbench);
		modifier = new TermModifyPanel(workbench);
		
		curpane = tip;

		editpane.setLayout(new BoxLayout(editpane, BoxLayout.LINE_AXIS)); 
		editpane.setAlignmentY(Component.TOP_ALIGNMENT);
		editpane.setAlignmentX(Component.LEFT_ALIGNMENT);
		editpane.setBackground(SemGenSettings.lightblue);
		editpane.add(curpane);
		
		JPanel editspacer = new JPanel();
		editspacer.setLayout(new BoxLayout(editspacer, BoxLayout.PAGE_AXIS)); 
		editspacer.setAlignmentY(Component.TOP_ALIGNMENT);
		editspacer.setAlignmentX(Component.LEFT_ALIGNMENT);
		editspacer.setBackground(SemGenSettings.lightblue);
		editspacer.add(toolbar);
		editspacer.add(editpane);
		editspacer.add(Box.createVerticalGlue());

		add(typepane);
		add(editspacer);
		add(Box.createHorizontalGlue());
		setTypeList();
		validate();
	}
	
	private void setTypeList() {
		int seli = typechooser.getSelectedIndex();
		
		String[] names = new String[types.length];
		for (int i = 0; i<types.length; i++) {
			names[i] = types[i].getName() + " (" + library.countObjectofType(types[i]) + ")";
		}
		typechooser.setListData(names);
		if (seli!=-1) typechooser.setSelectedIndex(seli);
		clearPanel();
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
		toolbar.selectInfoButton();
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
				"Remove selected term(s) and all references from the model and library?",
				"Confirm",
				JOptionPane.YES_NO_OPTION);
		if(JOptionPane.YES_OPTION == choice){
			ArrayList<Integer> termindicies = new ArrayList<Integer>();
			for (int index : termlist.getSelectedIndices()) {
				termindicies.add(terms.get(index));
			}
			
			for (int index : termindicies) {
				affected = workbench.collectAffiliatedTermsandCodewords(index);
				new TermModifier(workbench, affected).runRemove();
			}
			
			setTypeList();
			updateList();
		}
	}
	
	private void swapEditor(JPanel pan) {
		editpane.remove(curpane);
		curpane = pan;
		editpane.add(pan);
		repaint();
		validate();
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
		if (arg1==ModelEdit.CODEWORD_CHANGED || arg1==ModelEdit.PROPERTY_CHANGED || arg1==ModelEdit.COMPOSITE_CHANGED ||
				arg1==LibraryEvent.TERM_CHANGE) {
			if (affected==null) return;
			int i = affected.getTermLibraryIndex();
			updateList();
			if (!library.isTerm(i)) {
				affected = null;
				clearPanel();
			}
			else {
				termlist.setSelectedIndex(terms.indexOf(i));
				affected = new TermCollector(workbench, i);
			}
			toolbar.toggleButtons();	
			tip.updateInformation(affected);
			setTypeList();
		}
		if (arg1==LibraryEvent.SINGULAR_TERM_CREATED || arg1==LibraryEvent.SINGULAR_TERM_REMOVED || arg1==LibraryEvent.PROCESS_CHANGE || 
				arg1==ModelEdit.COMPOSITE_CHANGED) {
			setTypeList();
		}
	}

	private class EditorToolbar extends SemGenTabToolbar implements ActionListener {
		private static final long serialVersionUID = 1L;
		private SemGenToolbarRadioButton infobtn = new SemGenToolbarRadioButton(SemGenIcon.moreinfoicon);
		private SemGenToolbarRadioButton modifybtn = new SemGenToolbarRadioButton(SemGenIcon.modifyicon);
		private SemGenToolbarButton removebtn = new SemGenToolbarButton(SemGenIcon.eraseicon);
		private SemGenToolbarRadioButton replacebtn = new SemGenToolbarRadioButton(SemGenIcon.replaceicon);
		
		public EditorToolbar() {
			super(JToolBar.HORIZONTAL);
			this.setMaximumSize(new Dimension(9999, 34));
			setAlignmentX(LEFT_ALIGNMENT);
			infobtn.addActionListener(this);
			add(infobtn);
			infobtn.setSelected(true);
			infobtn.toggleSelectionGraphic();
			modifybtn.addActionListener(this);	
			add(modifybtn);
			replacebtn.setToolTipText("Replace selected term");
			replacebtn.addActionListener(this);
			add(replacebtn);
			add(Box.createHorizontalStrut(470));
			removebtn.addActionListener(this);
			add(removebtn);
			this.setMargin(new Insets(0,0,1,0));
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
			infobtn.setToolTipText("Term Usage");
			modifybtn.setToolTipText("Modify selected term");
			removebtn.setToolTipText("Remove selected term");
			if (!termlist.isSelectionEmpty()) {
				replacebtn.setEnabled(affected.isUsed());
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
		
		public void selectInfoButton() {
			infobtn.doClick();
		}
	}
	
	private class ListTooltip extends MouseMotionAdapter {
		JList<String> l;
		ListTooltip(JList<String> list) {
			l = list;
		}
		
        @Override
        public void mouseMoved(MouseEvent e) {
            ListModel<String> m = l.getModel();
            int index = l.locationToIndex(e.getPoint());
            if( index>-1 ) {
                l.setToolTipText(m.getElementAt(index).toString());
            }
        }
    };
}
