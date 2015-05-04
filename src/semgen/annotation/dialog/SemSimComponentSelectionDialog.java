package semgen.annotation.dialog;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.utilities.uicomponent.SemGenDialog;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class SemSimComponentSelectionDialog extends SemGenDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	protected JOptionPane optionPane;
	protected Object[] options = new Object[]{"OK","Cancel"};

	protected ArrayList<JCheckBox> checklist = new ArrayList<JCheckBox>();
	protected JPanel panel = new JPanel();
	protected boolean result = false;
	
	public SemSimComponentSelectionDialog(String title, ArrayList<String> termlist, ArrayList<Boolean> preselectlist, boolean multiselect) {
		super(title);
		setPreferredSize(new Dimension(500, 600));
		
		drawUI(termlist);
		showDialog();
	}
	
	public void drawUI(ArrayList<String> objectlist) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (String s : objectlist) {
			JCheckBox cb = new JCheckBox(s); 
			checklist.add(cb);
			panel.add(cb);
		}
		
		SemGenScrollPane scroller = new SemGenScrollPane(panel);
		
		Object[] array = {scroller};

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
	}
	
	public Integer[] getSelection() {
		Integer[] selections = new Integer[]{};
		int i = 0;
		for (JCheckBox cb : checklist) {
			if (cb.isSelected()) selections[i] = checklist.indexOf(cb);
		}
		return selections;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
				String value = optionPane.getValue().toString();
				if (value == "OK") {
					result = true;
				}
				dispose();
		}
	}
}
