package semgen.annotation.dialog.termlibrary;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;

public class AddReferenceClassDialog extends SemGenDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3830623199860161812L;
	//public ReferenceClassFinderPanel refclasspanel;
	public JOptionPane optionPane;
	public JTextArea utilarea = new JTextArea();
	private AnnotatorWorkbench workbench; 

	public AddReferenceClassDialog(AnnotatorWorkbench wb, String[] ontList, Object[] options) {
		super("Select reference concept");
		workbench = wb;
		
		//refclasspanel = new ReferenceClassFinderPanel(workbench, annotatable, ontList);

		utilarea.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		utilarea.setBackground(new Color(0,0,0,0));
		utilarea.setLineWrap(true);
		utilarea.setWrapStyleWord(true);
		utilarea.setEditable(false);
		utilarea.setFont(SemGenFont.defaultBold(-1));

		Object[] array = { utilarea };//, refclasspanel

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		setContentPane(optionPane);
		showDialog();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName()=="value") {
			if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) return;
			String value = optionPane.getValue().toString();
			if (value == "Close") {
				dispose();
				return;
			}
			
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
	}
}
