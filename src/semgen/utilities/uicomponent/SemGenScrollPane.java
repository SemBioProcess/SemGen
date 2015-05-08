package semgen.utilities.uicomponent;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class SemGenScrollPane extends JScrollPane implements MouseWheelListener{
	private static final long serialVersionUID = 2703381146331909737L;
	
	public SemGenScrollPane(Component view) {
		super(view);
		setScrollBehavior();
	}

	public SemGenScrollPane() {
		setScrollBehavior();
	}
	
	private void setScrollBehavior() {
		
		setWheelScrollingEnabled(false);
        addMouseWheelListener(this);
        
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

	@Override
	public void mouseWheelMoved(MouseWheelEvent evt) {
			       
		int mod = evt.getModifiers();
				
		// I've been able to get SG to distinguish between vertical and
		// horizontal trackpad scrolling by collecting the modifiers
		// on the MouseWheelEvent. For vertical scrolling the modifier
		// is 0, for horizontal it is 1. When using the InputEvent.getModifiersExText()
		// function to determine the InputEvent constants that 0 and 1 correspond to,
		// they come out to an empty string and ⇧, respectively. 
		// So I'm currently hard-coding the 0 and 1 values into this custom
		// MouseWheelMoved function.
		
		if (evt.getWheelRotation() != 0){
			
		   JScrollBar selectedScrollBar;
		   
		   // Hard-coding the modifier on the MouseWheelEvent to distinguish
		   // vertical and horizontal trackpad scrolling
		   if(mod==0){
			   selectedScrollBar = getVerticalScrollBar();
		   }
		   else if(mod==1){
			   selectedScrollBar = getHorizontalScrollBar();
		   }
		   else return;

		   if (evt.getWheelRotation() >= 1)//mouse wheel was rotated down/ towards the user
           {
               int iScrollAmount = evt.getScrollAmount();
               int iNewValue = selectedScrollBar.getValue() + selectedScrollBar.getBlockIncrement() * iScrollAmount * Math.abs(evt.getWheelRotation());
               if (iNewValue <= selectedScrollBar.getMaximum())
               {
            	   selectedScrollBar.setValue(iNewValue);
               }
           }
           else if (evt.getWheelRotation() <= -1)//mouse wheel was rotated up/away from the user
           {
               int iScrollAmount = evt.getScrollAmount();
               int iNewValue = selectedScrollBar.getValue() - selectedScrollBar.getBlockIncrement() * iScrollAmount * Math.abs(evt.getWheelRotation());
               if (iNewValue >= 0)
               {
            	   selectedScrollBar.setValue(iNewValue);
               }
           }
	       
	    }
	}
}
