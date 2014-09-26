package semgen.annotation.dialog.referencedialog;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.annotation.AnnotatorTab;
import semgen.annotation.codewordpane.CodewordButton;
import semgen.resource.SemGenFont;

public class AddReferenceClassDialog extends JDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3830623199860161812L;
	public ReferenceClassFinderPanel refclasspanel;
	public JOptionPane optionPane;
	public JTextArea utilarea = new JTextArea();

	public AddReferenceClassDialog(String[] ontList, Object[] options) {
		setTitle("Select reference concept");
		refclasspanel = new ReferenceClassFinderPanel(ontList);
		
		utilarea.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		utilarea.setBackground(new Color(0,0,0,0));
		utilarea.setLineWrap(true);
		utilarea.setWrapStyleWord(true);
		utilarea.setEditable(false);
		utilarea.setFont(SemGenFont.defaultBold(-1));

		Object[] array = { utilarea, refclasspanel };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		setContentPane(optionPane);
	}
	
	public void packAndSetModality(){
		setModalityType(ModalityType.APPLICATION_MODAL);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String value = optionPane.getValue().toString();
		String selectedname = refclasspanel.resultslistright.getSelectedValue();

		if (value == "Add as entity" && this.getFocusOwner() != refclasspanel.findbox) {
			annotator.semsimmodel.addReferencePhysicalEntity(URI.create(refclasspanel.resultsanduris.get(selectedname)), selectedname);
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			JOptionPane.showMessageDialog(this,"Added " + (String) refclasspanel.resultslistright.getSelectedValue() + " as reference physical enitity", "",
					JOptionPane.PLAIN_MESSAGE);
		}
		else if(value == "Add as process" && this.getFocusOwner() != refclasspanel.findbox){
			annotator.semsimmodel.addReferencePhysicalProcess(URI.create(refclasspanel.resultsanduris.get(selectedname)), selectedname);
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			JOptionPane.showMessageDialog(this,"Added " + (String) refclasspanel.resultslistright.getSelectedValue() + " as reference physical process", "",
					JOptionPane.PLAIN_MESSAGE);
		}
		else if (value == "Close") {
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			dispose();
		}
	}
}
