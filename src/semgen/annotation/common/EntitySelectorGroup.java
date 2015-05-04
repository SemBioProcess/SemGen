package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;

import semgen.SemGenSettings;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;

public class EntitySelectorGroup extends Box {
	private static final long serialVersionUID = 1L;
	private ArrayList<SelectorPanel> selectors = new ArrayList<SelectorPanel>();
	private ArrayList<StructuralRelationPanel> relations = new ArrayList<StructuralRelationPanel>();
	private CodewordToolDrawer drawer;
	
	public EntitySelectorGroup(CodewordToolDrawer bench, int orientation) {
		super(orientation);
		drawer = bench;
		this.setBackground(SemGenSettings.lightblue);
		setAlignmentX(Box.LEFT_ALIGNMENT);
	}
	
	public void drawBox(boolean isprocess) {
		if (isprocess) {
			addProcessSelector();
		}
		else {
		//	for (int i = 0; i < numboxes; i++) {
				
		//	}
		}
		alignAndPaint(15);
	}
	
	public void addEntitySelector() {
		if (selectors.size()!=0) {
			StructuralRelationPanel lbl = new StructuralRelationPanel();
			relations.add(lbl); 
			add(lbl);
		}
		SelectorPanel esp = new SelectorPanel();
		selectors.add(esp);
		add(esp, BorderLayout.NORTH);
	}
	
	public void addProcessSelector() {
		SelectorPanel p = new SelectorPanel();
		p.makeEntitySelector();
		selectors.add(p);
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
	
	public void refreshLists(ArrayList<String> peidlist) {
		for (SelectorPanel p : selectors) {
			p.setComboList(peidlist);
		}
	}
	
	private class SelectorPanel extends AnnotationSelectorPanel {
		protected SelectorPanel() {
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
		}

		@Override
		public void webButtonClicked() {
			
		}

		@Override
		public void eraseButtonClicked() {
			
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
