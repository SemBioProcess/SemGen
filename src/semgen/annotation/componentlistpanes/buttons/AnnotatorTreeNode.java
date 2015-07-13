package semgen.annotation.componentlistpanes.buttons;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.tree.DefaultMutableTreeNode;


public abstract class AnnotatorTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	
	protected boolean isexpanded = false;
	
	public abstract void updateButton(int index);
	public abstract Component getButton();
	public abstract void onSelection();
	
	protected class CodewordTreeButton extends CodewordButton {
		private static final long serialVersionUID = 1L;

		public CodewordTreeButton(String name, boolean canedit, boolean usemarkers) {
			super(name, canedit, usemarkers);
		}
	
		@Override
		public void mouseClicked(MouseEvent arg0) {}
			
	}
	
	protected class SubModelTreeButton extends SubmodelButton {
		private static final long serialVersionUID = 1L;

		public SubModelTreeButton(String name, boolean canedit) {
			super(name, canedit);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {				
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			
		}
	}
	
	public void setExpanded(boolean expand) {
		isexpanded = expand;
	}
	
	public boolean isExpanded() {
		return isexpanded;
	}
}
