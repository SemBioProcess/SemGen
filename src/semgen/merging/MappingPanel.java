package semgen.merging;

import java.awt.Color;
<<<<<<< HEAD

=======
import java.awt.Font;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

<<<<<<< HEAD
import semgen.resource.SemGenFont;

public class MappingPanel extends JPanel {

	private static final long serialVersionUID = 2776275290257952399L;
	private JLabel title;
	public JList<String> scrollercontent = new JList<String>();

	public MappingPanel(String filename) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		title = new JLabel(filename);
		title.setAlignmentX(LEFT_ALIGNMENT);
		title.setFont(SemGenFont.defaultPlain());

		scrollercontent.setBackground(Color.white);
		scrollercontent.setFont(SemGenFont.defaultPlain(-2));
		scrollercontent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroller = new JScrollPane(scrollercontent);
		scroller.setAlignmentX(LEFT_ALIGNMENT);
		
		add(title);
		add(scroller);
		setBackground(Color.white);
	}
	
	public void setTitle(String text) {
		title.setText(text);
=======
public class MappingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2776275290257952399L;
	public JLabel title;
	public String filename;
	public JList scrollercontent;
	public JScrollPane scroller;

	public MappingPanel(String filename) {
		this.filename = filename;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		title = new JLabel(filename);
		title.setAlignmentX(LEFT_ALIGNMENT);
		title.setFont(new Font("SansSerif", Font.BOLD, 12));

		scrollercontent = new JList();
		scrollercontent.setBackground(Color.white);
		scrollercontent.setFont(new Font("SansSerif", Font.PLAIN, 10));
		scrollercontent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroller = new JScrollPane(scrollercontent);
		scroller.setAlignmentX(LEFT_ALIGNMENT);
		this.add(title);
		this.add(scroller);
		this.setBackground(Color.white);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	}
}
