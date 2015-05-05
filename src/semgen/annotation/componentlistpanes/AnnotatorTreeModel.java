package semgen.annotation.componentlistpanes;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import semgen.SemGenSettings;
import semgen.annotation.componentlistpanes.buttons.AnnotatorTreeNode;
import semgen.annotation.workbench.AnnotatorTreeMap;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public class AnnotatorTreeModel implements TreeModel, Observer {
	private AnnotatorWorkbench workbench;
	private CodewordToolDrawer cwdrawer;
	private SubModelToolDrawer smdrawer;
	
	private AnnotatorTreeNode focus;
	private RootButton root = new RootButton();
	
	private HashMap<CodewordTreeButton, Integer> cwmap = new HashMap<CodewordTreeButton, Integer>();
	private HashMap<Integer, CodewordTreeButton> cwmapinv = new HashMap<Integer, CodewordTreeButton>();
	private HashMap<SubModelTreeButton, Integer> smmap = new HashMap<SubModelTreeButton, Integer>();
	private HashMap<Integer, SubModelTreeButton> smmapinv = new HashMap<Integer, SubModelTreeButton>();
	
	private ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	protected SemGenSettings settings;
	
	public AnnotatorTreeModel(AnnotatorWorkbench wb, SemGenSettings sets) {
		workbench = wb;
		settings = sets;
		cwdrawer = wb.openCodewordDrawer();
		smdrawer = wb.openSubmodelDrawer();
		
		wb.addObserver(this);
		cwdrawer.addObserver(this);
		smdrawer.addObserver(this);
		sets.addObserver(this);
		
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
	
	private void addSubModelNode(Integer index, ArrayList<Integer> dsindicies, AnnotatorTreeNode parent) {
		SubModelTreeButton newnode = new SubModelTreeButton();
		
		for (Integer dsi : dsindicies) {
			addCodewordNode(dsi, newnode);
		}
		
		smmap.put(newnode, index);
		smmapinv.put(index, newnode);
		parent.add(newnode);
	}
		
	private void addCodewordNode(Integer index, AnnotatorTreeNode parent) {
		CodewordTreeButton newnode = new CodewordTreeButton();
		
		cwmap.put(newnode, index);
		cwmapinv.put(index, newnode);
		parent.add(newnode);
	}
	
	public void reload() {
		loadModel();
		
		fireTreeStructureChanged();
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
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
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		
	}
	
	protected void fireTreeStructureChanged() {
        fireNodeChanged(root);
    }
	
	protected void fireNodeChanged(DefaultMutableTreeNode node) {
        TreeModelEvent e = new TreeModelEvent(this, new Object[] {node});
        for (TreeModelListener tml : listeners) {
            tml.treeStructureChanged(e);
        }
    }
	
	private void changeButtonFocus(AnnotatorTreeNode focusbutton) {
		focus = focusbutton;
	}
	
	public void destroy() {
		focus = null;
		root.removeAllChildren();
		cwmap.clear(); cwmapinv.clear();
		smmap.clear(); smmapinv.clear();
		fireTreeStructureChanged();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (settings.useTreeView()) {
			if (arg1==modeledit.freetextchange) {
				if (focus!=null) {
					fireNodeChanged(focus);
				}
			}
			if (arg0==smdrawer) {
				if (arg1 == modeledit.smnamechange) {
					fireTreeStructureChanged();
				}
				if	(arg1==modeledit.submodelchanged) {
					for (Integer i : smdrawer.getChangedComponents()) {
						fireNodeChanged(smmapinv.get(i));
					}
				}
			}
			if (arg0==cwdrawer) {
				if	(arg1==modeledit.compositechanged) {
					for (Integer i : cwdrawer.getChangedComponents()) {
						fireNodeChanged(cwmapinv.get(i));
					}
				}
			}
		}
	}
	
	protected class RootButton extends AnnotatorTreeNode {
		private static final long serialVersionUID = 1L;
		
		public RootButton() {}
		
		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			JLabel label = new JLabel(workbench.getCurrentModelName());
			label.setIcon(SemGenIcon.homeiconsmall);
			label.setFont(SemGenFont.defaultBold(1));
			return label;
		}

		@Override
		public void onSelection() {
			changeButtonFocus(this);
			workbench.getModelAnnotationsWorkbench().notifyOberserversofMetadataSelection(0);
		}
	}
	
	class SubModelTreeButton extends AnnotatorTreeNode {
		private static final long serialVersionUID = 1L;

		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			int index = smmap.get(this);
			
			SubModelTreeButton btn = new SubModelTreeButton(smdrawer.getCodewordName(index), smdrawer.isEditable(index));
			btn.toggleHumanDefinition(smdrawer.hasHumanReadableDef(index));
			btn.toggleSingleAnnotation(smdrawer.hasSingularAnnotation(index));
			return btn;
		}

		@Override
		public void onSelection() {
			changeButtonFocus(this);
			smdrawer.setSelectedIndex(smmap.get(this));
		}
	}
	
	class CodewordTreeButton extends AnnotatorTreeNode {
		private static final long serialVersionUID = 1L;

		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			int index = cwmap.get(this);
			
			CodewordTreeButton btn = new CodewordTreeButton(cwdrawer.getLookupName(index), cwdrawer.isEditable(index), settings.useDisplayMarkers());
			
			btn.toggleHumanDefinition(cwdrawer.hasHumanReadableDef(index));
			btn.toggleSingleAnnotation(cwdrawer.hasSingularAnnotation(index));
			btn.refreshCompositeAnnotationCode(cwdrawer.getAnnotationStatus(index));
			btn.refreshPropertyOfMarker(cwdrawer.getPropertyType(index));
			return btn;
		}
		
		@Override
		public void onSelection() {
			changeButtonFocus(this);
			cwdrawer.setSelectedIndex(cwmap.get(this));
		}
	}
	

}
