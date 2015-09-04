package semgen.annotation.dialog.termlibrary;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.annotation.ReferenceOntologies.OntologyDomain;

public class AddReferenceClassDialog extends SemGenDialog implements
		PropertyChangeListener {

	private static final long serialVersionUID = -3830623199860161812L;
	protected ReferenceClassFinderPanel refclasspanel;
	protected JOptionPane optionPane;
	protected JTextArea utilarea = new JTextArea();

	public AddReferenceClassDialog(SemSimTermLibrary lib, OntologyDomain dom) {
		super("Select reference concept");
		
		refclasspanel = new ReferenceClassFinderPanel(lib, dom);

		utilarea.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		utilarea.setBackground(new Color(0,0,0,0));
		utilarea.setLineWrap(true);
		utilarea.setWrapStyleWord(true);
		utilarea.setEditable(false);
		utilarea.setFont(SemGenFont.defaultBold(-1));

		Object[] array = { utilarea, refclasspanel};
		Object[] options = new Object[]{"Annotate","Close"};
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		setContentPane(optionPane);
		showDialog();
		refclasspanel.align();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName()=="value") {
			Object value = optionPane.getValue();
			if (value == JOptionPane.UNINITIALIZED_VALUE) return;
			if (value.toString() == "Close") {
				refclasspanel.clearSelection();
				dispose();
				return;
			}
			refclasspanel.addTermtoLibrary();
			dispose();
		}
	}
	
	public int getIndexofSelection() {
		return refclasspanel.getSelectedTermIndex();
	}
}
