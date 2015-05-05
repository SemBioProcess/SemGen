package semgen.annotation.dialog;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import semgen.annotation.common.SelectionPanel;
import semgen.utilities.uicomponent.SemGenDialog;
import semgen.utilities.uicomponent.SemGenScrollPane;

public class SemSimComponentSelectionDialog extends SemGenDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	protected JOptionPane optionPane;
	protected Object[] options = new Object[]{"OK","Cancel"};

	protected SelectionPanel panel;
	protected boolean result = false;
	
	public SemSimComponentSelectionDialog(String title, ArrayList<String> termlist, ArrayList<Boolean> preselectlist, boolean multiselect) {
		super(title);
		setPreferredSize(new Dimension(500, 600));
		panel = new SelectionPanel(multiselect);
		panel.setChecklist(termlist, preselectlist);
		
		drawUI(termlist);
		showDialog();
	}
	
	public void drawUI(ArrayList<String> objectlist) {	
		SemGenScrollPane scroller = new SemGenScrollPane(panel);
		
		Object[] array = {scroller};

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
	}
	
	public Integer[] getSelections() {
		return panel.getSelection();
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
