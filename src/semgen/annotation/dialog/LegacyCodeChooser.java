package semgen.annotation.dialog;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import semgen.resource.file.SemGenOpenFileChooser;

public class LegacyCodeChooser extends JDialog implements ActionListener,
		PropertyChangeListener {

	private static final long serialVersionUID = 5097390254331353085L;
	public JOptionPane optionPane;
	public JTextField txtfld = new JTextField();
	public JButton locbutton = new JButton("or choose local file");
	private String urltoadd = "";
	
	public LegacyCodeChooser() {
		JPanel srcmodpanel = new JPanel();
		txtfld.setPreferredSize(new Dimension(250, 25));
		locbutton.addActionListener(this);
		srcmodpanel.add(txtfld);
		srcmodpanel.add(locbutton);
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(srcmodpanel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		setTitle("Enter URL of legacy code or choose a local file");
		pack();
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void propertyChange(PropertyChangeEvent e) {
		String value = optionPane.getValue().toString();
		if (value == "OK") {
			urltoadd = txtfld.getText();
		} 
		dispose();
	}

	public String getCodeLocation() {
		return urltoadd;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if (o == locbutton) {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy model code"); //null
			File file = sgc.getSelectedFile();
			if (file!=null) txtfld.setText(file.getAbsolutePath());
		}
	}
}
