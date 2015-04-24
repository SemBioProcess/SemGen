package semgen.annotation.componentlistpanes.buttons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class PropertyMarker extends JLabel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8788987331968930201L;
	public Color color;
	private int radius = 3;
	public PropertyMarker(Color color, String tooltip){
		this.color = color;
		this.setToolTipText(tooltip);
		this.setPreferredSize(new Dimension(radius*3,(radius*3)+2));
		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color);
		g.drawOval(0, 6, radius*2, radius*2);
		g.fillOval(0, 6, radius*2, radius*2);
	}
}
