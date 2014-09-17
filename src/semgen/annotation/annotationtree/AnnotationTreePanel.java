package semgen.annotation.annotationtree;

import java.util.Observable;

import javax.swing.tree.DefaultMutableTreeNode;

import semgen.SemGenSettings;
import semgen.annotation.uicomponents.ComponentPane;
import semgen.annotation.workbench.AnnotatorWorkbench;

public class AnnotationTreePanel extends ComponentPane {
	private static final long serialVersionUID = 1L;
	
	private AnnotatorButtonTree tree;
	public AnnotationTreePanel(AnnotatorWorkbench canvas, SemGenSettings settings) {
		super(settings);

		tree = new AnnotatorButtonTree(new DefaultMutableTreeNode(canvas.getCurrentModelName()), canvas);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		repaint();
		validate();
	}
	
	public void updateTreeNode(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) (tree.getSelectionPath().getLastPathComponent()); 
		tree.update(node, tree);
	}
	
	@Override
	public void refreshAnnotatableElements() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void makeButtons() {
		// TODO Auto-generated method stub
		
	}

}
