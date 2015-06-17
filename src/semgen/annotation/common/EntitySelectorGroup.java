package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import semgen.SemGenSettings;
import semgen.annotation.dialog.termlibrary.AddReferenceClassDialog;
import semgen.annotation.dialog.termlibrary.CustomTermDialog;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenIcon;
import semsim.utilities.ReferenceOntologies.OntologyDomain;

public abstract class EntitySelectorGroup extends Box implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ArrayList<SelectorPanel> selectors = new ArrayList<SelectorPanel>();
	private ArrayList<StructuralRelationPanel> relations = new ArrayList<StructuralRelationPanel>();
	protected JButton addButton;
	protected SemSimTermLibrary termlib;
	protected ArrayList<Integer> selections;
	
	public EntitySelectorGroup(SemSimTermLibrary lib) {
		super(BoxLayout.Y_AXIS);
		termlib = lib;
		selections = new ArrayList<Integer>();
		selections.add(-1);
		setBackground(SemGenSettings.lightblue);
		
		drawBox(true);
	}
	
	public EntitySelectorGroup(SemSimTermLibrary lib, ArrayList<Integer> sels, boolean editable) {
		super(BoxLayout.PAGE_AXIS);
		termlib = lib;
		selections = sels;
		setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
		
		drawBox(editable);
	}
	
	public void drawBox(boolean editable) {
		clearGroup();
		for (int i=0; i < selections.size(); i++) {
			addEntitySelector(editable, i);
		}
		if (editable) {
			addButton = new JButton("Add Entity");
			addButton.addActionListener(this);
			add(addButton);
			
		}
		alignAndPaint(15);
		refreshLists();
		setEraseable();
	}
	
	public void addEntitySelector(boolean editable, int selectionindex) {
		if (selectors.size()!=0) {
			StructuralRelationPanel lbl = new StructuralRelationPanel();
			relations.add(lbl); 
			lbl.setAlignmentX(Box.LEFT_ALIGNMENT);
			add(lbl);
		}
		SelectorPanel esp = new SelectorPanel(editable, selections.get(selectionindex));
		selectors.add(esp);
		esp.setAlignmentX(Box.LEFT_ALIGNMENT);
		add(esp, BorderLayout.NORTH);
	}	
	
	private void alignAndPaint(int indent){
		int x = indent;
		int i = 0;
		for(SelectorPanel p : selectors){
			p.setBorder(BorderFactory.createEmptyBorder(0, x, 5, 0));
			if (i < relations.size()) {
				relations.get(i).setBorder(BorderFactory.createEmptyBorder(0, x+15, 5, 0));
			}
			x = x + 15;
			i++;
		}
		validate();
	}
	
	public void clearGroup() {
		selectors.clear();
		relations.clear();
		removeAll();
	}
	
	public void reset() {
		selections.clear();
		selections.add(-1);
		drawBox(true);
		validate();
		repaint();
	}
	
	public void refreshLists() {
		ArrayList<Integer> choices = termlib.getSortedSingularPhysicalEntityIndicies();
		for (int i=0; i < selectors.size(); i++) {
			selectors.get(i).setComboList(choices, selections.get(i));
		}
	}
	
	public void removeEntity(SelectorPanel pan) {
		int i = selectors.indexOf(pan);
		selectors.remove(pan);
		if (i!=0) { 
			StructuralRelationPanel srp = relations.get(i-1);
			relations.remove(srp);
		}
		onChange();
		drawBox(true);
	}

	protected ArrayList<Integer> pollSelectors() {
		selections.clear();
		for (SelectorPanel sel : selectors) {
			selections.add(sel.getSelection());
		}
		return selections;
	}
	
	protected void insertEntity(int loc) {
		selections.add(loc, -1);
		drawBox(true);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(addButton)) {
			selections.add(-1);
			drawBox(true);
		}
	}
	
	protected void setEraseable() {
		boolean enabled = true;
		if (selectors.size()==1 && selectors.get(0).getSelection()==-1) {
			enabled = false;
		}
		for (SelectorPanel sp : selectors) {
			sp.enableEraseButton(enabled);
		}
	}
	
	public abstract void onChange();
	
	private class SelectorPanel extends AnnotationChooserPanel {
		private static final long serialVersionUID = 1L;
		protected ComponentPanelLabel insertbtn = new ComponentPanelLabel(SemGenIcon.createicon, "Insert Physical Entity");
		
		protected SelectorPanel(boolean editable, int entityindex) {
			super(termlib);
			if (editable) {
				makeEntitySelector();
				lbllist.add(insertbtn);
				insertbtn.addMouseListener(new AddEntityClick(this));
			}
			else {
				makeStaticPanel(entityindex);
			}
			constructSelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(combobox)) {
				onChange();
				toggleCustom(!termlib.isReferenceTerm(getSelection()));
				setEraseable();
			}
		}
		
		@Override
		public void searchButtonClicked() {
			AddReferenceClassDialog rcd = new AddReferenceClassDialog(termlib, OntologyDomain.PhysicalEntity);
			onNewTerm(rcd.getIndexofSelection());
		}

		@Override
		public void createButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			ctd.makeEntityTerm(termlib);
			onNewTerm(ctd.getSelection());
		}

		@Override
		public void modifyButtonClicked() {
			CustomTermDialog ctd = new CustomTermDialog();
			ctd.makeEntityTerm(termlib, getSelection());
			onNewTerm(ctd.getSelection());
		}
		
		protected void onNewTerm(Integer selection) {
			if (selection!=-1) {
				setComboList(termlib.getSortedSingularPhysicalEntityIndicies(), selection);
				onChange();
			}
		}
		
		@Override
		protected void onEraseButtonClick() {
			if (selectors.size()>1) {
				removeEntity(this);
			}
			else {
				setSelection(-1);
			}
		}
		
		private class AddEntityClick extends MouseAdapter {
			SelectorPanel selector;
			
			public AddEntityClick(SelectorPanel sel) {
				selector = sel;
			}
			
			public void mouseClicked(MouseEvent arg0) {
				insertEntity(selectors.indexOf(selector));
			}
		}
		
	}
	
	
}
