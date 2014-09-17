package semgen.annotation.uicomponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import semgen.SemGenSettings;

public abstract class ComponentPane extends JPanel implements Observer, KeyListener {
	private static final long serialVersionUID = 1L;
	protected AnnotationObjectButton focusbutton;
	protected ArrayList<AnnotationObjectButton> aoblist = new ArrayList<AnnotationObjectButton>();
	protected DualHashBidiMap<Integer, AnnotationObjectButton> aobmap = new DualHashBidiMap<Integer, AnnotationObjectButton>();
	protected SemGenSettings settings;
	
	public ComponentPane(SemGenSettings sets)  {
		settings = sets;
		setBackground(Color.white);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	public AnnotationObjectButton getFocusButton() {
		return focusbutton;
	}	
	
	public void keyPressed(KeyEvent e) {
		int id = e.getKeyCode();
		
		// Up arrow key
		if (id == 38) {
			int index = -1;
			for (int x = 0; x < getComponentCount(); x++) {
				Component c = getComponent(x);
				if (c == focusbutton) {
					index = x;
					break;
				}
			}
			if(index!=-1){
				for(int y=(index-1); y>=0; y--){
					if(getComponent(y).isVisible() && getComponent(y) instanceof AnnotationObjectButton){
						changeButtonFocus((AnnotationObjectButton) getComponent(y), null);
						break;
					}
				}
			}
		}
		// Down arrow key
		if (id == 40) {
			int index = -1;
			for (int x = 0; x < getComponentCount(); x++) {
				Component c = getComponent(x);
				if (c == focusbutton) {
					index = x;
					break;
				}
			}
			if(index!=-1){
				for(int y=(index+1); y<getComponentCount(); y++){
					if(getComponent(y).isVisible() && getComponent(y) instanceof AnnotationObjectButton){
						changeButtonFocus((AnnotationObjectButton) getComponent(y), null);
						break;
					}
				}
			}
		}
	}
	
	public void changeButtonFocus(AnnotationObjectButton aob, JLabel whichann) {
		focusbutton = aob;
		Integer index = aobmap.getKey(aob);
		if (whichann == aob.humdeflabel) {
			freeTextRequest(index);
		}
		if(whichann == aob.singularannlabel){
			singAnnRequest(index);
		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	
	public abstract void makeButtons();
	
	@Override
	public abstract void update(Observable o, Object arg);
	
	public abstract void refreshAnnotatableElements();
	
	public abstract void freeTextRequest(int index);
	
	public abstract void singAnnRequest(int index);
}
