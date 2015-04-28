package semgen.annotation.componentlistpanes;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import semgen.SemGenSettings;
import semgen.annotation.componentlistpanes.buttons.CodewordButton;
import semgen.annotation.componentlistpanes.buttons.SubmodelButton;
import semgen.annotation.workbench.AnnotatorTreeMap;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public class AnnotatorTreeModel implements TreeModel {
	private AnnotatorWorkbench workbench;
	private CodewordToolDrawer cwdrawer;
	private SubModelToolDrawer smdrawer;
	
	private Component focus;
	private DefaultMutableTreeNode root;
	private HashMap<DefaultMutableTreeNode, TreeButton> nodemap = new HashMap<DefaultMutableTreeNode, TreeButton>();
	
	private HashMap<CodewordTreeButton, Integer> cwmap = new HashMap<CodewordTreeButton, Integer>();
	private HashMap<SubModelTreeButton, Integer> smmap = new HashMap<SubModelTreeButton, Integer>();
	
	protected SemGenSettings settings;
	
	AnnotatorTreeModel(AnnotatorWorkbench wb, SemGenSettings sets) {
		workbench = wb;
		settings = sets;
		cwdrawer = wb.openCodewordDrawer();
		smdrawer = wb.openSubmodelDrawer();
		
		root = new DefaultMutableTreeNode(wb.getCurrentModelName());
		nodemap.put(root, new RootButton());
		
		loadModel();
	}
	
	private void loadModel() {
		AnnotatorTreeMap treemap = workbench.makeTreeMap(settings.showImports());
		
		if (!treemap.getSubmodelList().isEmpty()) {
			for (Integer smi : treemap.getSubmodelList()) {
				addSubModelNode(smi, treemap.getSubmodelDSIndicies(smi), root);
			}
		}
		for (Integer dsi : treemap.getOrphanDS()) {
			addCodewordNode(dsi, root);
		}
		
	}
	private void addSubModelNode(Integer index, ArrayList<Integer> dsindicies, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode newnode = new DefaultMutableTreeNode("sm" + index);
		
		for (Integer dsi : dsindicies) {
			addCodewordNode(dsi, newnode);
		}
		
		SubModelTreeButton renderer = new SubModelTreeButton(smdrawer.getCodewordName(index), smdrawer.isEditable(index));
		renderer.updateButton(index);
		nodemap.put(newnode, renderer);
		
		smmap.put(renderer, index);
		parent.add(newnode);
	}
	
	public void buttonSelected(DefaultMutableTreeNode node) {
		nodemap.get(node).onSelection();
	}
	
	private void addCodewordNode(Integer index, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode newnode = new DefaultMutableTreeNode("cw" + index);
		
		CodewordTreeButton renderer = new CodewordTreeButton(cwdrawer.getCodewordName(index), cwdrawer.isEditable(index));
		renderer.updateButton(index);
		nodemap.put(newnode, renderer);
		
		cwmap.put(renderer, index);
		parent.add(newnode);
	}

	public TreeButton getButton(DefaultMutableTreeNode node) {
		return nodemap.get(node);
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		
	}
	
	@Override
	public Object getChild(Object node, int arg1) {
		if (getChildCount(node)!=0)
			return ((DefaultMutableTreeNode)node).getChildAt(arg1);
		return null;
	}

	@Override
	public int getChildCount(Object node) {
		return ((DefaultMutableTreeNode)node).getChildCount();
	}

	@Override
	public int getIndexOfChild(Object node, Object child) {
		return ((DefaultMutableTreeNode)node).getIndex((DefaultMutableTreeNode)child);
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object node) {
		return ((DefaultMutableTreeNode)node).isLeaf();
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
		
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		
	}
	
	private void changeButtonFocus(Component focusbutton) {
		focus = focusbutton;
	}
	
	protected interface TreeButton {
		public void updateButton(int index);
		public Component getButton();
		public void onSelection();
	}
	
	protected class RootButton extends JLabel implements TreeButton {
		private static final long serialVersionUID = 1L;

		public RootButton() {
			setIcon(SemGenIcon.homeiconsmall);
			setFont(SemGenFont.defaultBold(1));
			setText(workbench.getCurrentModelName());
		}
		
		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			return this;
		}


		@Override
		public void onSelection() {
			workbench.getModelAnnotationsWorkbench().notifyOberserversofMetadataSelection(0);
		}
	}
	
	class SubModelTreeButton extends SubmodelButton implements TreeButton {
		private static final long serialVersionUID = 1L;

		public SubModelTreeButton(String name, boolean canedit) {
			super(name, canedit);
			addMouseListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if (arg0.getSource()==humdeflabel) {
				workbench.requestFreetextChange();
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {

		}

		@Override
		public void updateButton(int index) {
			toggleHumanDefinition(cwdrawer.hasHumanReadableDef(index));
			toggleSingleAnnotation(cwdrawer.hasSingularAnnotation(index));
		}

		@Override
		public Component getButton() {
			return this;
		}

		@Override
		public void onSelection() {
			changeButtonFocus(this);
			smdrawer.setSelectedIndex(smmap.get(this));
		}
	}
	
	class CodewordTreeButton extends CodewordButton implements TreeButton {
		private static final long serialVersionUID = 1L;

		public CodewordTreeButton(String name, boolean canedit) {
			super(name, canedit, settings.useDisplayMarkers());
			addMouseListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {

		}

		@Override
		public void updateButton(int index) {
			toggleHumanDefinition(cwdrawer.hasHumanReadableDef(index));
			toggleSingleAnnotation(cwdrawer.hasSingularAnnotation(index));
			refreshCompositeAnnotationCode(cwdrawer.getAnnotationStatus(index));
			refreshPropertyOfMarker(cwdrawer.getPropertyType(index));
		}

		@Override
		public Component getButton() {
			return this;
		}
		
		@Override
		public void onSelection() {
			changeButtonFocus(this);
			cwdrawer.setSelectedIndex(cwmap.get(this));
		}
	}
	
}
