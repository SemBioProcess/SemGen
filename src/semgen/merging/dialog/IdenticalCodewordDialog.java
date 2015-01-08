package semgen.merging.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import semgen.utilities.uicomponent.SemGenDialog;

public class IdenticalCodewordDialog extends SemGenDialog implements PropertyChangeListener, ItemListener {
	private static final long serialVersionUID = 7836229234967611648L;
	private JOptionPane optionPane;
	private JTextField mantextfield = new JTextField();
	private JTextArea area = new JTextArea();
	private JComboBox<String> box;

	public IdenticalCodewordDialog(String cdwd, String file1, String file2) {
		super("");
		String[] selections = new String[] { "Rename codeword in " + file2,
				"Only keep " + cdwd + " from " + file1,
				"Only keep " + cdwd + " from " + file2};
		area.setEditable(false);

		area.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		area.setText("Duplicate codeword: " + cdwd
				+ "\nChoose a resolution option:"
				+ "\n(This change will only appear in the merged model)");
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);

		box = new JComboBox<String>(selections);
		box.addItemListener(this);
		mantextfield.setForeground(Color.blue);
		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(area, BorderLayout.NORTH);
		mainpanel.add(box, BorderLayout.CENTER);
		mainpanel.add(mantextfield, BorderLayout.SOUTH);

		this.setTitle("Duplicate codeword: " + cdwd);

		Object[] array = new Object[] { mainpanel };
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		showDialog();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName()=="value") {
			String value = optionPane.getValue().toString();
			if (value == "OK") {
				if (box.getSelectedIndex() == 0) {	
					if (!mantextfield.getText().equals("")
							&& mantextfield.getText() != null) {
						System.out.println(mantextfield.getText());
					} else {
						System.out.println("Please enter a new codeword name");
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						return;
					}
				} 
			}
			dispose();
		}
	}

	public void itemStateChanged(ItemEvent arg0) {
		mantextfield.setEnabled(!(box.getSelectedIndex() > 0));
	}
}
