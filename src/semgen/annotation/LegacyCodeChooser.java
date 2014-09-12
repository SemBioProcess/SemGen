package semgen.annotation;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import semgen.SemGenGUI;
import semsim.SemSimConstants;
import semsim.model.annotation.Annotation;


public class LegacyCodeChooser extends JDialog implements ActionListener,
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5097390254331353085L;
	public JOptionPane optionPane;
	public JTextField txtfld;
	public Annotator ann;
	public JButton locbutton;
	public Annotation existingann;

	public LegacyCodeChooser(Annotator ann) {
		this.ann = ann;
		
		for(Annotation a : ann.semsimmodel.getAnnotations()){
			if(a.getRelation()==SemSimConstants.LEGACY_CODE_LOCATION_RELATION){
				existingann = a;
			}
		}

		JPanel srcmodpanel = new JPanel();
		txtfld = new JTextField();
		txtfld.setPreferredSize(new Dimension(250, 25));
		locbutton = new JButton("or choose local file");
		locbutton.addActionListener(this);
		srcmodpanel.add(txtfld);
		srcmodpanel.add(locbutton);
		Object[] array = new Object[] { srcmodpanel };
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		this.setTitle("Enter URL of legacy code or choose a local file");
		this.pack();
		this.setLocationRelativeTo(SemGenGUI.desktop);
		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void propertyChange(PropertyChangeEvent e) {
		String value = optionPane.getValue().toString();
		if (value == "OK") {
			String urltoadd = txtfld.getText();
			if (urltoadd != null && ann.semsimmodel != null && !urltoadd.equals("")) {
				if(this.existingann!=null){
					this.existingann.setValue(urltoadd);
				}
				else{
					ann.semsimmodel.addAnnotation(new Annotation(SemSimConstants.LEGACY_CODE_LOCATION_RELATION, urltoadd));
				}
				try {
					ann.setCodeViewer(urltoadd, false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ann.setModelSaved(false);
			}
			setVisible(false);
		} else if (value == "Cancel") {
			setVisible(false);
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		Object o = arg0.getSource();
		if (o == locbutton) {
			int val = SemGenGUI.showSemGenFileChooser(SemGenGUI.currentdirectory, null, "Select legacy model code", 0, false);
			if (val == JFileChooser.APPROVE_OPTION) {
				File file = SemGenGUI.fc.getSelectedFile();
				txtfld.setText(file.getAbsolutePath());
			}
		}
		setVisible(true);
	}
}
