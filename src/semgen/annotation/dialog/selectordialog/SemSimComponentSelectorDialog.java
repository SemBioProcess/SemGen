package semgen.annotation.dialog.selectordialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.resource.SemGenFont;
import semgen.resource.uicomponents.SemGenScrollPane;

public class SemSimComponentSelectorDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -1776906245210358895L;
	public JPanel panel = new JPanel();
	private JCheckBox markallbox = new JCheckBox("Mark all/none");
	public JOptionPane optionPane;
	protected ArrayList<JCheckBox> compboxes = new ArrayList<JCheckBox>();

	public ArrayList<String> selectableset; 
	public ArrayList<Integer> preselectedset, sscstodisable;
	public String ssctoignore;
	
	public SemSimComponentSelectorDialog(
			ArrayList<String> settolist,
			ArrayList<Integer> preselset,
			ArrayList<Integer> sscstodisable,
			String title) {

		selectableset = settolist;
		preselectedset = preselset;
		this.sscstodisable = sscstodisable;
		setTitle(title);
	}
	
	public void setUpUI(Object[] options) {
		setPreferredSize(new Dimension(500, 600));

		markallbox.setFont(SemGenFont.defaultItalic(-1));
		markallbox.setForeground(Color.blue);
		markallbox.setSelected(false);
		markallbox.addActionListener(this);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JCheckBox checkbox;
		for (String name : selectableset) {
			checkbox = new JCheckBox(name);
			checkbox.setName(name);
			compboxes.add(checkbox);
			panel.add(checkbox);
		}
		for (Integer i : preselectedset) {
			compboxes.get(i).setSelected(false);
		}
		for (Integer i : sscstodisable) {
			compboxes.get(i).setSelected(false);
		}
				
		optionPane = new JOptionPane(new SemGenScrollPane(panel), JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		// Need an argument to this function for the parent of the addPropertyChangeListener so can use ObjectPropertyEditor function
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		setModalityType(ModalityType.APPLICATION_MODAL);  // If put before pack() get null pointer error when hit OK, if here doesn't act modal
		pack();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == markallbox) {
			for (JCheckBox checkbox : compboxes) {
				checkbox.setSelected(markallbox.isSelected());
			}
		}
	}
	
	public ArrayList<Integer> getSelected() {
		ArrayList<Integer> selected = new ArrayList<Integer>();
		for (JCheckBox checkbox : compboxes) {
			if (checkbox.isSelected()) {
				selected.add(compboxes.indexOf(checkbox));
			}
		}
		return selected;
	}
}
