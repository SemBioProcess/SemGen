package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

import semgen.SemGenSettings;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;

public class EntitySelectorGroup extends Box {
	private static final long serialVersionUID = 1L;
	private ArrayList<SelectorPanel> selectors = new ArrayList<SelectorPanel>();
	private ArrayList<StructuralRelationPanel> relations = new ArrayList<StructuralRelationPanel>();
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary termlib;
	
	public EntitySelectorGroup(CodewordToolDrawer bench, SemSimTermLibrary lib) {
		super(BoxLayout.PAGE_AXIS);
		termlib = lib;
		drawer = bench;
		setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
		
		drawBox();
	}
	
	public void drawBox() {
		for (int i = 0; i < drawer.countEntitiesinCompositeEntity(); i++) {
			addEntitySelector();
		}
		alignAndPaint(15);
		refreshLists();
	}
	
	public void addEntitySelector() {
		if (selectors.size()!=0) {
			StructuralRelationPanel lbl = new StructuralRelationPanel();
			relations.add(lbl); 
			add(lbl);
		}
		SelectorPanel esp = new SelectorPanel(!drawer.isEditable());
		selectors.add(esp);
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
	
	public void refreshLists() {
		ArrayList<Integer> choices = termlib.getSortedSingularPhysicalEntityIndicies();
		ArrayList<Integer> composite = drawer.getCompositeEntityIndicies();
		for (int i=0; i < selectors.size(); i++) {
			selectors.get(i).setComboList(choices, composite.get(i));
		}
	}
	
	private class SelectorPanel extends AnnotationChooserPanel {
		private static final long serialVersionUID = 1L;

		protected SelectorPanel(boolean isstatic) {
			super(termlib);
			if (isstatic) {
				makeStaticPanel(drawer.getIndexofModelComponent());
			}
			else {
				makeEntitySelector();
			}
			constructSelector();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
		}
		
		@Override
		public void searchButtonClicked() {
			
		}

		@Override
		public void createButtonClicked() {
			
		}

		@Override
		public void modifyButtonClicked() {
			
		}
	}
	
	
}
