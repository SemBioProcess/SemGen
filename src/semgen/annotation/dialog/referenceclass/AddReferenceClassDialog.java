package semgen.annotation.dialog.referenceclass;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.annotation.AnnotatorTab;
import semgen.annotation.componentdisplays.codewords.CodewordButton;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.Annotatable;

public class AddReferenceClassDialog extends SemGenDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3830623199860161812L;
	public ReferenceClassFinderPanel refclasspanel;
	public AnnotatorTab annotator;
	public JOptionPane optionPane;
	public JTextArea utilarea = new JTextArea();

	public AddReferenceClassDialog(AnnotatorTab ann, String[] ontList, Object[] options, Annotatable annotatable) {
		super("Select reference concept");
		this.annotator = ann;
		
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
		showDialog();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName()=="value") {
			String value = optionPane.getValue().toString();
			if (value == "Close") {
				this.dispose();
				return;
			}
			
			String selectedname = (String) refclasspanel.resultslistright.getSelectedValue();
			String type = "";
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			if (value == "Add as entity" && this.getFocusOwner() != refclasspanel.findbox) {
				type = " physical enitity";
			}
			else if(value == "Add as process" && this.getFocusOwner() != refclasspanel.findbox){
				type =  " physical process";
			}

			annotator.semsimmodel.addReferencePhysicalEntity(URI.create(refclasspanel.resultsanduris.get(selectedname)), selectedname);
				JOptionPane.showMessageDialog(this,
						"Added " + (String) refclasspanel.resultslistright.getSelectedValue() + " as reference" + type,
						"", JOptionPane.PLAIN_MESSAGE);
			annotator.setModelSaved(false);
			if(annotator.focusbutton instanceof CodewordButton) annotator.annotatorpane.compositepanel.refreshUI();
		}
	}
}
