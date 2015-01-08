package semgen.merging.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import semgen.utilities.uicomponent.SemGenDialog;

public class ConversionFactorDialog extends SemGenDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3182047942231432822L;
	private JOptionPane optionPane;
	private JTextField mantextfield = new JTextField();
	private String cdwd2keep;
	private JComboBox<String> box = new JComboBox<String>(new String[] { "*", "/" });;
	public Boolean process = true;
	public String cdwdAndConversionFactor;
	public double conversionfactor;

	public ConversionFactorDialog(String cdwd2keep, String cdwd2discard,
			String cdwd2keepunits, String cdwd2discardunits) {
		super("Ensuring unitary balances");
		this.cdwd2keep = cdwd2keep;
		
		JTextArea area = new JTextArea();
		area.setOpaque(false);
		area.setEditable(false);
		area.setBackground(Color.white);

		area.setText(cdwd2discard + " has units \"" + cdwd2discardunits + "\"" + "\n"
				+ cdwd2keep + " has units \"" + cdwd2keepunits + "\""
				+ "\nPlease enter any necessary conversion factors below");
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		
		JLabel label = new JLabel(cdwd2discard + " (" + cdwd2discardunits + ") = " + cdwd2keep + " (" + cdwd2keepunits + ") ");

		mantextfield.setPreferredSize(new Dimension(250, 30));
		mantextfield.setForeground(Color.blue);
		
		JPanel conpanel = new JPanel();
		conpanel.add(label);
		conpanel.add(box);
		conpanel.add(mantextfield);

		Object[] array = new Object[] { area, conpanel };
		Object[] options = new Object[] { "OK", "Cancel" };
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
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
				Boolean oktoproceed = true;
				try{
					conversionfactor = Double.parseDouble(mantextfield.getText());
				}
				catch(NumberFormatException ex){oktoproceed = false;}
				if (!mantextfield.getText().equals("")
						&& mantextfield.getText() != null
						&& oktoproceed) {
					cdwdAndConversionFactor = "(" + cdwd2keep + box.getSelectedItem() + mantextfield.getText() + ")";
					if (mantextfield.getText().equals("1")) {
						cdwdAndConversionFactor = cdwd2keep;
						process = true;
					}
				} 
				else {
					optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					JOptionPane.showMessageDialog(this, "Please enter a valid conversion factor");
					return;
				}
			} 
			else if (value == "Cancel") {
				process = false;;
			}
			dispose();
		}
		
	}
}