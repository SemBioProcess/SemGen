package semgen;

import java.awt.Color;

import javax.swing.JTextArea;

public class SemGenTextArea extends JTextArea{

    /**
	 * 
	 */
	private static final long serialVersionUID = -9202322761777280913L;

	public Color bgcolor;
	public SemGenTextArea(Color bgcolor) {
        setOpaque(false);
		setEditable(false);
		this.bgcolor = bgcolor;
    }
}
