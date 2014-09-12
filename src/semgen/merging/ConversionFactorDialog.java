package semgen.merging;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import semgen.SemGenGUI;
import semgen.SemGenTextArea;



public class ConversionFactorDialog extends JDialog implements
		PropertyChangeListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3182047942231432822L;
	public JOptionPane optionPane;
	public Merger merger;
	public JTextField mantextfield;
	public SemGenTextArea area;
	public JPanel conpanel;
	public JLabel label;
	public String cdwd2keep;
	public String cdwd2keepunits;
	public String cdwd2discard;
	public String cdwd2discardunits;
	public String newcdwd;
	public File file1;
	public File file2;
	public JComboBox box;
	public String[] selections;
	public Boolean process;
	public String cdwdAndConversionFactor;
	public double conversionfactor;

	public ConversionFactorDialog(Merger merger, String cdwd2keep, String cdwd2discard,
			String cdwd2keepunits, String cdwd2discardunits) {

		this.merger = merger;
		this.cdwd2keepunits = cdwd2keepunits;
		this.cdwd2discardunits = cdwd2discardunits;

		process = true;
		this.cdwd2keep = cdwd2keep;
		this.cdwd2discard = cdwd2discard;
		selections = new String[] { "*", "/" };
		
		area = new SemGenTextArea(Color.white);

		area.setText(cdwd2discard + " has units \"" + cdwd2discardunits + "\"" + "\n"
				+ cdwd2keep + " has units \"" + cdwd2keepunits + "\""
				+ "\nPlease enter any necessary conversion factors below");
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		
		label = new JLabel(cdwd2discard + " (" + cdwd2discardunits + ") = " + cdwd2keep + " (" + cdwd2keepunits + ") ");

		conpanel = new JPanel();

		box = new JComboBox(selections);
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

	public void actionPerformed(ActionEvent e) {
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