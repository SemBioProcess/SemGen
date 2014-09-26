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

//    @Override
//    protected void paintComponent(Graphics g) {
//        g.setColor(new Color(0,255,255,255));
//        Insets insets = getInsets();
//        int x = insets.left;
//        int y = insets.top;
//        int width = getWidth() - (insets.left + insets.right);
//        int height = getHeight() - (insets.top + insets.bottom);
//        g.fillRect(x, y, width, height);
//        super.paintComponent(g);
//    }
}
