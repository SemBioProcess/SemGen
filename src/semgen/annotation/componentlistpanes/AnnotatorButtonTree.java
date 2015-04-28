package semgen.annotation.componentlistpanes;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.Painter;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;

public class AnnotatorButtonTree extends JTree implements TreeSelectionListener, MouseListener, Observer{
	private static final long serialVersionUID = 7868541704010347520L;
	private AnnotatorTreeModel model;
	
	public AnnotatorButtonTree(AnnotatorWorkbench wb, SemGenSettings sets){
		wb.addObserver(this);

		model = new AnnotatorTreeModel(wb, sets);
		
		setModel(model);
		UIDefaults dialogTheme = new UIDefaults();
		dialogTheme.put("Tree:TreeCell[Focused+Selected].backgroundPainter", new MyPainter());
		dialogTheme.put("Tree:TreeCell[Enabled+Selected].backgroundPainter", new MyPainter());
		putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		putClientProperty("Nimbus.Overrides", dialogTheme);
		
		setLargeModel(true);
		this.setCellRenderer(new Renderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setVisible(true);
	}
	
	class MyPainter implements Painter<Object>{
		public void paint(Graphics2D arg0, Object arg1, int arg2, int arg3) {
			arg0.setColor(SemGenSettings.lightblue);
			arg0.fillRect( 0, 0, arg2, arg3 );
		}
	}
	
	class Renderer implements TreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree arg0, Object node,
				boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			
			return model.getButton((DefaultMutableTreeNode)node).getButton();
		}	
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
	}

	@Override
	public void update(Observable arg0, Object arg1) {

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		TreePath tp = this.getPathForLocation(arg0.getX(), arg0.getY());
	
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
