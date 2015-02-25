package semgen.annotation.dialog.referenceclass.compositedialog;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;

public class CreateCompositeDialog extends SemGenDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private CreateCompositePanel ccpanel;
	
	private JOptionPane optionPane;
	private JTextArea utilarea = new JTextArea();
	private AnnotatorWorkbench workbench; 
	
	public CreateCompositeDialog(AnnotatorWorkbench wb) {
		super("Create Composite");
		workbench = wb;

		utilarea.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		utilarea.setBackground(new Color(0,0,0,0));
		utilarea.setLineWrap(true);
		utilarea.setWrapStyleWord(true);
		utilarea.setEditable(false);
		utilarea.setFont(SemGenFont.defaultBold(-1));
		
		ccpanel = new CreateCompositePanel(workbench);
		
		Object[] options = {"Add Composite", "Close"};
		Object[] array = { utilarea, ccpanel };

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
			workbench.setModelSaved(false);
			
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
	}
}
