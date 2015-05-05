package semgen.annotation.common;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class SelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected ArrayList<JCheckBox> checklist = new ArrayList<JCheckBox>();
	protected boolean multiselect;
	
	public SelectionPanel(boolean multi) {
		multiselect = multi;
	}

	public void setChecklist(ArrayList<String> termlist, ArrayList<Boolean> preselectlist) {
		layoutChecklist(termlist);
		doPreselection(preselectlist);
	}
	
	private void layoutChecklist(ArrayList<String> objectlist) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (String s : objectlist) {
			JCheckBox cb = new JCheckBox(s); 
			checklist.add(cb);
			add(cb);
		}
	}
	
	private void doPreselection(ArrayList<Boolean> preselectlist) {
		int i =0;
		for (JCheckBox cb : checklist) {
			cb.setSelected(preselectlist.get(i));
			i++;
		}
	}
	
	public void disableEntries(ArrayList<Integer> disablelist) {
		for (int i : disablelist) {
			checklist.get(i).setEnabled(false);
		}
	}
	
	public Integer[] getSelection() {
		Integer[] selections = new Integer[]{};
		int i = 0;
		for (JCheckBox cb : checklist) {
			if (cb.isSelected()) selections[i] = checklist.indexOf(cb);
		}
		return selections;
	}
}
