package semgen.annotation;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.resource.SemGenFont;
import semsim.Annotatable;

public class AddReferenceClassDialog extends JDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3830623199860161812L;
	public ReferenceClassFinderPanel refclasspanel;
	public AnnotatorTab annotator;
	public JOptionPane optionPane;
	public JTextArea utilarea = new JTextArea();

	public AddReferenceClassDialog(AnnotatorTab ann, String[] ontList, Object[] options, Annotatable annotatable) {
		this.annotator = ann;
		setTitle("Select reference concept");
		refclasspanel = new ReferenceClassFinderPanel(ann, annotatable, ontList);

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
		setVisible(true);
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String value = optionPane.getValue().toString();
		String selectedname = (String) refclasspanel.resultslistright.getSelectedValue();
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		if (value == "Add as entity" && this.getFocusOwner() != refclasspanel.findbox) {
			annotator.semsimmodel.addReferencePhysicalEntity(URI.create(refclasspanel.resultsanduris.get(selectedname)), selectedname);
			JOptionPane.showMessageDialog(this,"Added " + (String) refclasspanel.resultslistright.getSelectedValue() + " as reference physical enitity", "",
					JOptionPane.PLAIN_MESSAGE);
			annotator.setModelSaved(false);
		}
		else if(value == "Add as process" && this.getFocusOwner() != refclasspanel.findbox){
			annotator.semsimmodel.addReferencePhysicalProcess(URI.create(refclasspanel.resultsanduris.get(selectedname)), selectedname);
			JOptionPane.showMessageDialog(this,"Added " + (String) refclasspanel.resultslistright.getSelectedValue() + " as reference physical process", "",
					JOptionPane.PLAIN_MESSAGE);
			annotator.setModelSaved(false);
		}
		else if (value == "Close") {
			this.dispose();
		}
		if(annotator.focusbutton instanceof CodewordButton) annotator.anndialog.compositepanel.refreshUI();
	}
}
