package semgen;

import java.awt.Component;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;


public class PaneIcon extends ImageIcon {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4558619973467192784L;
	public String pathtosymbolicon;
	public Component component;
	public JTabbedPane pane;
	public URI saveuri;
	public URI tempuri;

	public PaneIcon(String pathtosymbolicon, Component component) {
		this.pathtosymbolicon = pathtosymbolicon;
		this.component = component;
		setImage(SemGenGUI.createImageIcon(pathtosymbolicon).getImage());

	}

}
