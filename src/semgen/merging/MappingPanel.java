package semgen.merging;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import semgen.merging.workbench.MergerWorkbench;
import semgen.utilities.SemGenFont;

public class MappingPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 2776275290257952399L;
	private JLabel title = new JLabel("[ ]");
	private MergerWorkbench workbench;
	public JList<String> scrollercontent = new JList<String>();
	
	public MappingPanel(MergerWorkbench bench) {
		workbench = bench;
		workbench.addObserver(this);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
	
	public void populatePanel(int index) {
		title.setText(workbench.getModelName(index));
		ArrayList<String> list = workbench.getDSNamesandDescriptions(index);
		String[] array = new String[list.size()];
		list.toArray(array);
		scrollercontent.setListData(array);
		
		if (index == 0) {
			scrollercontent.setForeground(Color.blue);
		}
		else {
			scrollercontent.setForeground(Color.red);
		}
	}

	public void clearPanel() {
		scrollercontent.clearSelection();
		scrollercontent.setListData(new String[0]);
		scrollercontent.repaint();
		title.setText("[ ]");
	}
	
	public int getSelectionIndex() {
		return scrollercontent.getSelectedIndex();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {

	}
	
}
