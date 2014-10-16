package semgen.merging;

import java.awt.Color;
import javax.swing.JLabel;

public class FileToMergeLabel extends JLabel {

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

}
