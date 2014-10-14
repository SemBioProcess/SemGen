package semgen.resource.uicomponent;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class SemGenScrollPane extends JScrollPane {
	private static final long serialVersionUID = 2703381146331909737L;

	public SemGenScrollPane(Component view) {
		super(view);
		setUnitIncrements();
	}

	public SemGenScrollPane() {
		setUnitIncrements();
	}
	
	private void setUnitIncrements() {
		this.getVerticalScrollBar().setUnitIncrement(12);
		this.getHorizontalScrollBar().setUnitIncrement(12);
	}
	
	public void scrollToTopLeft() {
		scrollToTop();
		scrollToLeft();
	}
	
	public void scrollToTop(){
		SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
		       getVerticalScrollBar().setValue(0);
		   }
		});
	}
	
	public void scrollToLeft(){
		SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
			       getHorizontalScrollBar().setValue(0);
			   }
			});
	}
	
	public void scrollToComponent(final Component cp){
		SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
			       Rectangle rc = cp.getBounds();
				   scrollRectToVisible(rc);
			   }
			});
	}
}
