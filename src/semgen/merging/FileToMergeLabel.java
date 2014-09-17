package semgen.merging;

import java.awt.Color;
import javax.swing.JLabel;

public class FileToMergeLabel extends JLabel {

	private static final long serialVersionUID = -9210035304014498845L;
	private Boolean selected;
	private String filepath;

	public FileToMergeLabel(String path) {
		this.filepath = path;
		setOpaque(true);
		this.setText(filepath);
		setSelected(false);
	}

	public void setSelected(Boolean sel) {
		this.selected = sel;
		if (sel) setBackground(new Color(207, 215, 252));
			else setBackground(Color.white);
		validate();
	}
	
	public String getFilePath() {
		return filepath;
	}
	
	public Boolean isSelected() {
		return selected;
	}
}
