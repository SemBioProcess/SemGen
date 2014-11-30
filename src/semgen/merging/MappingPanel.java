package semgen.merging;

import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import semgen.utilities.SemGenFont;

public class MappingPanel extends JPanel {
	private static final long serialVersionUID = 2776275290257952399L;
	private JLabel title;
	public JList<String> scrollercontent = new JList<String>();
	
	public MappingPanel(String filename) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		title = new JLabel(filename);
		title.setAlignmentX(LEFT_ALIGNMENT);
		title.setFont(SemGenFont.defaultBold());

		scrollercontent.setBackground(Color.white);
		scrollercontent.setFont(SemGenFont.defaultPlain(-2));
		scrollercontent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroller = new JScrollPane(scrollercontent);
		scroller.setAlignmentX(LEFT_ALIGNMENT);
		this.add(title);
		this.add(scroller);
		this.setBackground(Color.white);
	}
	
	public void setTitle(String name) {
		title.setText(name);
	}
}
