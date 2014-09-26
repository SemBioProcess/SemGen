package semgen.merging;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;

public class FileToMergeLabel extends JLabel implements ActionListener,
		MouseListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9210035304014498845L;
	public Boolean selected;
	public String filepath;

	public FileToMergeLabel(String filepath) {
		this.filepath = filepath;
		selected = false;
		setOpaque(true);
		setBackground(Color.white);
		this.setText(filepath);
	}

	public void setSelected() {
		this.selected = true;
		setBackground(new Color(207, 215, 252));
		validate();
	}

	public void setUnselected() {
		selected = false;
		setBackground(Color.white);
		validate();
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
