package semgen.utilities.file;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import semgen.utilities.uicomponent.SemGenDialog;

public class JSimModelSelectorDialogForReading extends SemGenDialog implements PropertyChangeListener{

	private static final long serialVersionUID = -6899404099942989139L;
	private ArrayList<String> selectedModelNames = new ArrayList<String>();
	private JOptionPane optionPane;
	private JPanel panel = new JPanel();
	private ArrayList<JCheckBox> modelstoread = new ArrayList<JCheckBox>();

	public JSimModelSelectorDialogForReading(String title, ArrayList<String> modelnames) {
		super(title);
		
		setPreferredSize(new Dimension(430, 250));
		setResizable(false);
		
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		for(String modname : modelnames) {
			JCheckBox cb = new JCheckBox(modname);
			modelstoread.add(cb);
			panel.add(cb);
		}
		
		JScrollPane scroller = new JScrollPane(panel);

		Object[] array = { scroller };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		Object[] options = new Object[] { "OK", "Cancel" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		showDialog();
		
	}
	
	public ArrayList<String> getSelectedModelNames(){
		return selectedModelNames;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals("value")) {
			String value = optionPane.getValue().toString();
			
			if (value.equals("OK")) {				
				for(JCheckBox checkbox : modelstoread){
					if(checkbox.isSelected()){
						selectedModelNames.add(checkbox.getText());
					}
				}
			}
			dispose();
		}			
	}
	
}