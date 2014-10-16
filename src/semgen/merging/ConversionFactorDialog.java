package semgen.merging;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import semgen.SemGenGUI;
import semgen.resource.uicomponent.SemGenTextArea;

public class ConversionFactorDialog extends JDialog implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3182047942231432822L;
	public JOptionPane optionPane;
	public JTextField mantextfield;
	public String cdwd2keep;
	public JComboBox<String> box;
	public Boolean process;
	public String cdwdAndConversionFactor;
	public double conversionfactor;

	public ConversionFactorDialog(String cdwd2keep, String cdwd2discard,
			String cdwd2keepunits, String cdwd2discardunits) {

		process = true;
		this.cdwd2keep = cdwd2keep;
		String[] selections = new String[] { "*", "/" };
		
		SemGenTextArea area = new SemGenTextArea(Color.white);

		area.setText(cdwd2discard + " has units \"" + cdwd2discardunits + "\"" + "\n"
				+ cdwd2keep + " has units \"" + cdwd2keepunits + "\""
				+ "\nPlease enter any necessary conversion factors below");
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		
		JLabel label = new JLabel(cdwd2discard + " (" + cdwd2discardunits + ") = " + cdwd2keep + " (" + cdwd2keepunits + ") ");

		JPanel conpanel = new JPanel();

		box = new JComboBox<String>(selections);
		mantextfield = new JTextField();
		mantextfield.setPreferredSize(new Dimension(250, 30));
		mantextfield.setForeground(Color.blue);

		conpanel.add(label);
		conpanel.add(box);
		conpanel.add(mantextfield);

		setModal(true);
		this.setTitle("Ensuring unitary balances");

		Object[] array = new Object[] { area, conpanel };
		Object[] options = new Object[] { "OK", "Cancel" };
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		pack();
		setLocationRelativeTo(SemGenGUI.desktop);
		setVisible(true);
	}

	public void propertyChange(PropertyChangeEvent e) {
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
				setVisible(false);
			} 
			else {
				optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
				JOptionPane.showMessageDialog(this, "Please enter a valid conversion factor");
			}
		} 
		else if (value == "Cancel") {
			process = false;
			setVisible(false);
		}
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	}
}