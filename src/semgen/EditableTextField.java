package semgen;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import semgen.annotation.AnnotationObjectButton;
import semgen.annotation.CodewordButton;
import semgen.annotation.SubmodelButton;


public class EditableTextField extends JTextField implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AnnotationObjectButton aob;
	public EditableTextField(CodewordButton cb, String displayname){
		setOpaque(false);
		setText(displayname);
		setEditable(false);
		addMouseListener(this);
		this.aob = cb;
	}

	public void mouseClicked(MouseEvent arg0) {
		AnnotationObjectButton focuscodeword = aob.annotater.focusbutton;
		aob.annotater.changeButtonFocus(focuscodeword, aob, null);
		aob.annotater.focusbutton = aob;
		
		if(arg0.getClickCount()>=2 && aob instanceof SubmodelButton){
			setEditable(true);
			setOpaque(true);
			setForeground(Color.blue);
			setBackground(Color.white);
			setEditable(true);
		}
	}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) {}
}
