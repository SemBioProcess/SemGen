package semgen;



import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;


public class PaneIcon extends ImageIcon implements MouseListener {

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

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * MouseListener mouseListener = new MouseListener() {
	 * 
	 * public void mouseClicked(MouseEvent arg0) { try { Method method =
	 * component.getClass().getDeclaredMethod("closeAction", (Class[])null);
	 * method.invoke(component, (Class[])null); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 * 
	 * public void mouseEntered(MouseEvent arg0) {
	 * setImage(SemGen.createImageIcon("images/closeicon.gif").getImage());
	 * System.out.println("TEST"); }
	 * 
	 * public void mouseExited(MouseEvent arg0) {
	 * setImage(SemGen.createImageIcon(pathtosymbolicon).getImage());
	 * 
	 * }
	 * 
	 * public void mousePressed(MouseEvent arg0) { // TODO Auto-generated method
	 * stub }
	 * 
	 * public void mouseReleased(MouseEvent arg0) { // TODO Auto-generated
	 * method stub
	 * 
	 * } };
	 */

}
