package semgen.annotation.common;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import semgen.utilities.SemGenFont;

public class SelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected ArrayList<JCheckBox> checklist = new ArrayList<JCheckBox>();
	
	public SelectionPanel() {
		setOpaque(false);
	}

	public void setChecklist(ArrayList<String> termlist) {
		layoutChecklist(termlist);
		validate();
	}
	
	public void setChecklist(ArrayList<String> termlist, ArrayList<Integer> preselectlist) {
		layoutChecklist(termlist);
		doPreselection(preselectlist);
		validate();
	}
	
	public void setChecklist(ArrayList<String> termlist, ArrayList<Integer> preselectlist, ArrayList<Integer> disablelist) {
		layoutChecklist(termlist);
		doPreselection(preselectlist);
		disableEntries(disablelist);
		validate();
	}
	
	private void layoutChecklist(ArrayList<String> objectlist) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (String s : objectlist) {
			JCheckBox cb = new JCheckBox(s); 
			cb.setFont(SemGenFont.defaultPlain());
			checklist.add(cb);
			add(cb);
		}
	}
	
	private void doPreselection(ArrayList<Integer> preselectlist) {
		for (int i : preselectlist) {
			checklist.get(i).setSelected(true);
		}
	}
	
	public void disableEntries(ArrayList<Integer> disablelist) {
		for (int i : disablelist) {
			checklist.get(i).setEnabled(false);
		}
	}
	
	public ArrayList<Integer> getSelection() {
		ArrayList<Integer> selections = new ArrayList<Integer>();
		for (JCheckBox cb : checklist) {
			if (cb.isSelected()) selections.add(checklist.indexOf(cb));
		}
		return selections;
	}
}
