package semgen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


public class PreferenceDialog extends JDialog implements PropertyChangeListener, ActionListener {

	private static final long serialVersionUID = 1L;
	protected SemGenSettings settings;
	public JOptionPane optionPane;
	public ArrayList<JCheckBox> checklist = new ArrayList<JCheckBox>();
	JPanel prefpanel = new JPanel();
	
	public PreferenceDialog(SemGenSettings sets) {
		settings = sets;
		setTitle("Set SemGen Default Preferences");
		
		Object[] array = new Object[] { prefpanel };
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		setContentPane(optionPane);
		setModal(true);
		
		this.pack();
		this.setVisible(true);
		setLocationRelativeTo(null);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			if (value == "OK") {
				
			}
			dispose();
		} 
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class SemGenPreference extends Object {
		String caption, prefid;
		boolean isselected;
		
		public SemGenPreference(String capt, String id, boolean selected) {
			caption = capt;
			prefid = id;
			isselected = selected;
		}
		
		public String getCaption() {
			return caption;
		}
		
		public String getID() {
			return prefid;
		}
		
		public boolean isSelected() {
			return isselected;
		}
		
		public void setSelected(boolean selected) {
			isselected = selected;
		}
	}
}
