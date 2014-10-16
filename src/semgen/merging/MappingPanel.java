package semgen.merging;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class MappingPanel extends JPanel {
	private static final long serialVersionUID = 2776275290257952399L;
	public JLabel title;
	public String filename;
	public JList<String> scrollercontent = new JList<String>();
	public JScrollPane scroller;

	public MappingPanel(String filename) {
		this.filename = filename;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		title = new JLabel(filename);
		title.setAlignmentX(LEFT_ALIGNMENT);
		title.setFont(new Font("SansSerif", Font.BOLD, 12));

		scrollercontent.setBackground(Color.white);
		scrollercontent.setFont(new Font("SansSerif", Font.PLAIN, 10));
		scrollercontent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroller = new JScrollPane(scrollercontent);
		scroller.setAlignmentX(LEFT_ALIGNMENT);
		this.add(title);
		this.add(scroller);
		this.setBackground(Color.white);
	}
}
