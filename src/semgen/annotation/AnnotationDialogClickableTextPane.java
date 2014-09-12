package semgen.annotation;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;

import semgen.SemGenGUI;


public class AnnotationDialogClickableTextPane extends JTextPane implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1862678829844737844L;
	private AnnotationDialog dialog;

	public AnnotationDialogClickableTextPane(String text, AnnotationDialog dialog, int indent, boolean addMouseListener){
		
		this.dialog = dialog;
		setEditable(false);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(0, indent, 7, 15));
		setBackground(new Color(0,0,0,0));
//		setBackground(SemGenGUI.lightblue);  // Doeesn't work due to original problem in Swing? 
											//http://stackoverflow.com/questions/613603/java-nimbus-laf-with-transparent-text-fields

		if(addMouseListener){  // If need mouse listener, then it's clickable, if not, customize for computational code field
			addMouseListener(dialog);
			setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		}
		else{
			setContentType("text/html");
			setFont(new Font("SansSerif", Font.ITALIC, SemGenGUI.defaultfontsize-1));
			//setForeground(Color.gray);
		}
		setCustomText(text);
		
//		this.setMinimumSize(new Dimension(150, 35));
		//this.setMaximumSize(new Dimension(500, 999999));
	}
	
	public void setCustomText(String text){
		//setText("<html><body style='width:500px'>" + text);
		//setText("<html>" + text);
		setText(text);
	}
	
	public void mouseClicked(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
