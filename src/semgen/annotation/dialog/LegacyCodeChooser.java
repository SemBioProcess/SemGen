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

import semgen.annotation.AnnotatorTab;
import semgen.resource.file.SemGenOpenFileChooser;
import semsim.SemSimConstants;
import semsim.model.annotation.Annotation;

public class LegacyCodeChooser extends JDialog implements ActionListener,
		PropertyChangeListener {

	private static final long serialVersionUID = 5097390254331353085L;
	public JOptionPane optionPane;
	public JTextField txtfld = new JTextField();
	private boolean changed = false;
	public AnnotatorTab ann;
	public JButton locbutton = new JButton("or choose local file");
	public Annotation existingann;

	public LegacyCodeChooser(AnnotatorTab ann) {
		this.ann = ann;
		
		for(Annotation a : ann.semsimmodel.getAnnotations()){
			if(a.getRelation()==SemSimConstants.LEGACY_CODE_LOCATION_RELATION){
				existingann = a;
			}
		}

		JPanel srcmodpanel = new JPanel();
		txtfld.setPreferredSize(new Dimension(250, 25));
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
		setModal(true);
		this.setTitle("Enter URL of legacy code or choose a local file");
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
				String urltoadd = txtfld.getText();
				if (urltoadd != null && ann.semsimmodel != null && !urltoadd.equals("")) {
					changed=true;
					if(this.existingann!=null){
						existingann.setValue(urltoadd);
					}
					else{
						existingann = new Annotation(SemSimConstants.LEGACY_CODE_LOCATION_RELATION, urltoadd);
					}
					ann.setModelSaved(false);
				}
			}
			dispose();
		} 
	}

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if (o == locbutton) {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy model code"); //null
			File file = sgc.getSelectedFile();
			if (file!=null) txtfld.setText(file.getAbsolutePath());
		}
	}
	
	public boolean wasChanged() {
		return changed;
	}
}
