package semgen.utilities.uicomponent;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class SemGenScrollPane extends JScrollPane implements MouseWheelListener {
	private static final long serialVersionUID = 2703381146331909737L;
	private DragListener drag = new DragListener();
	
	public SemGenScrollPane(Component view) {
		super(view);
		setScrollBehavior();
		
		view.addMouseListener(drag);
		view.addMouseMotionListener(drag);
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
	
	public void scrollToBottom(){
		SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
		       getVerticalScrollBar().setValue(getVerticalScrollBar().getMaximum());
		   }
		});
	}
	
	@Override
	public void setViewportView(Component view) {
		super.setViewportView(view);
		view.addMouseListener(drag);
		view.addMouseMotionListener(drag);
	}
	
	public void scrollToLeft(){
		SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
			       getHorizontalScrollBar().setValue(0);
			   }
			});
	}
	
	public void scrollToComponent(final JComponent cp){
		Rectangle rc = cp.getVisibleRect();
		cp.scrollRectToVisible(rc);
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

		   int iScrollAmount = evt.getScrollAmount();
           int valMoved = selectedScrollBar.getBlockIncrement() * iScrollAmount * Math.abs(evt.getWheelRotation());
           
		   if (evt.getWheelRotation() >= 1)//mouse wheel was rotated down/ towards the user
           {  
               int iNewValue = selectedScrollBar.getValue() + valMoved;
               if (iNewValue <= selectedScrollBar.getMaximum())
               {
            	   selectedScrollBar.setValue(iNewValue);
               }
               else 
            	   selectedScrollBar.setValue(selectedScrollBar.getMaximum());
           }
           else if (evt.getWheelRotation() <= -1)//mouse wheel was rotated up/away from the user
           {
               int iNewValue = selectedScrollBar.getValue() - valMoved;
               if (iNewValue >= selectedScrollBar.getMinimum())
               {
            	   selectedScrollBar.setValue(iNewValue);
               }
               else
            	   selectedScrollBar.setValue(selectedScrollBar.getMinimum());
           }
	    }
	}
	
	//Allows scrolling via mouse or touch dragging events in the main viewport
	private class DragListener extends MouseAdapter {
		private Point origin;
		
        @Override
        public void mousePressed(MouseEvent e) {
        	if (getVerticalScrollBar().isVisible()) {
	            origin = e.getPoint();
	            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	}
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseDragged(MouseEvent e) {       	
            if (origin != null) {
                if (viewport != null && getVerticalScrollBar().isVisible()) {
                    //int deltaX = origin.x - e.getX();
                    int deltaY = origin.y - e.getY();

                    Point view = viewport.getViewPosition();
                    //view.x += deltaX;
                    view.y += deltaY;
                    
                    if (view.y >= 0 && view.y < viewport.getView().getHeight()) {
                    	viewport.setViewPosition(view);
                    }
                    
                }
            }
        }
	}
}
