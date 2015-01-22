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

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.DataStructureDescriptor;
import semgen.merging.workbench.DataStructureDescriptor.Descriptor;
import semgen.utilities.uicomponent.SemGenDialog;

public class ConversionFactorDialog extends SemGenDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3182047942231432822L;
	private JOptionPane optionPane;
	private JTextField mantextfield = new JTextField();
	private JComboBox<String> box = new JComboBox<String>(new String[] { "*", "/" });
	private Pair<Double, String> conversionfactor;

	public ConversionFactorDialog(Pair<DataStructureDescriptor, DataStructureDescriptor> descs, 
			boolean keepleft) {
		super("Unit Conflict");
		String cdwd2keep, cdwd2keepunits, cdwd2discard, cdwd2discardunits;
		if (keepleft) {
			cdwd2keep = descs.getLeft().getDescriptorValue(Descriptor.name);
			cdwd2keepunits = descs.getLeft().getDescriptorValue(Descriptor.units);
			cdwd2discard = descs.getRight().getDescriptorValue(Descriptor.name);
			cdwd2discardunits = descs.getRight().getDescriptorValue(Descriptor.units);
		}
		else {
			cdwd2keep = descs.getRight().getDescriptorValue(Descriptor.name);
			cdwd2keepunits = descs.getRight().getDescriptorValue(Descriptor.units);
			cdwd2discard = descs.getLeft().getDescriptorValue(Descriptor.name);
			cdwd2discardunits = descs.getLeft().getDescriptorValue(Descriptor.units);
		}
		
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
			Double conversion = 0.0;
			if (value == "OK") {
				
				boolean proceed = true;
				if(!mantextfield.getText().equals("")) {
					try {
						conversion = Double.valueOf(mantextfield.getText());
						proceed = (!Double.isNaN(conversion) &&  conversion != 0.0);
					}
					catch(NumberFormatException ex){
						proceed = false;
					}
				} 	
				if (!proceed) {
					optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						JOptionPane.showMessageDialog(this, "Please enter a valid nonzero conversion factor");
						return;
				}
			} 
			else if (value == "Cancel") {
				conversion = 0.0;
			}
			conversionfactor = Pair.of(conversion, box.getSelectedItem().toString());
			dispose();
		}
	}
	
	public Pair<Double, String> getConversionFactor() {
		return conversionfactor;
	}
}