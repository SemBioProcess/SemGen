package semgen.annotation.dialog;


import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;

import semgen.annotation.annotationpane.AnnotationPanel;
import semgen.resource.SemGenFont;


public class AnnotationDialogClickableTextPane extends JTextPane{
	private static final long serialVersionUID = -1862678829844737844L;
	
	public AnnotationDialogClickableTextPane(String text, AnnotationPanel dialog, int indent, boolean addMouseListener){
		setEditable(false);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(0, indent, 7, 15));
		setBackground(new Color(0,0,0,0));
		
		// If need mouse listener, then it's clickable, if not, customize for computational code field
		if(addMouseListener){  
			addMouseListener(dialog);
			setFont(SemGenFont.defaultPlain());
		}
		else{
			setContentType("text/html");
			setFont(SemGenFont.defaultBold(-1));
		}
		setCustomText(text);
	}
	
	public void setCustomText(String text){
		setText(text);
	}
}
