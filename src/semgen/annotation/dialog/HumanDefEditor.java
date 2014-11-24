package semgen.annotation.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.resource.uicomponent.SemGenDialog;
import semsim.model.SemSimComponent;

public class HumanDefEditor extends SemGenDialog implements PropertyChangeListener {

	private static final long serialVersionUID = -4040704987589247388L;
	private JOptionPane optionPane;
	private JTextArea defarea = new JTextArea();
	public SemSimComponent ssc;
	public AnnotationPanel anndialog;

	public HumanDefEditor(SemSimComponent ssc, AnnotationPanel dialog) {
		super("Enter free-text description");
		this.ssc = ssc;
		anndialog = dialog;

		setPreferredSize(new Dimension(430, 250));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		setResizable(false);

		JLabel codewordlabel = new JLabel(ssc.getName());
		codewordlabel.setFont(new Font("SansSerif", Font.BOLD, 12));

		defarea.setForeground(Color.blue);
		defarea.setLineWrap(true);
		defarea.setWrapStyleWord(true);

		JScrollPane areascroller = new JScrollPane(defarea);

		Object[] array = { codewordlabel, areascroller };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		Object[] options = new Object[] { "OK", "Cancel" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		String presentval = "";
		if(ssc.getDescription()!=null) presentval = ssc.getDescription();
		defarea.setText(presentval);
		defarea.requestFocusInWindow();
		showDialog();
	}
	
	public final void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			if (value.equals("OK")) {
				ssc.setDescription(defarea.getText());
				anndialog.annotator.setModelSaved(false);
				anndialog.humremovebutton.setEnabled(true);
				anndialog.refreshHumanReadableDefinition();
				remove(this);
			}
			dispose();
		}
		
	}
}