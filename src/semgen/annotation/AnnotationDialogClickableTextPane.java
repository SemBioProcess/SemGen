package semgen.annotation;


import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;

import semgen.SemGenGUI;


public class AnnotationDialogClickableTextPane extends JTextPane {
	private static final long serialVersionUID = -1862678829844737844L;

	public AnnotationDialogClickableTextPane(String text, AnnotationDialog dialog, int indent, boolean addMouseListener){

		setEditable(false);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(0, indent, 7, 15));
		setBackground(new Color(0,0,0,0));

		if(addMouseListener){  // If need mouse listener, then it's clickable, if not, customize for computational code field
			addMouseListener(dialog);
			setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		}
		else{
			setContentType("text/html");
			setFont(new Font("SansSerif", Font.ITALIC, SemGenGUI.defaultfontsize-1));
		}
		setCustomText(text);

	}
	
	public void setCustomText(String text){
		setText(text);
	}
}
