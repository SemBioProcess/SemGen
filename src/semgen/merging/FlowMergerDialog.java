package semgen.merging;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenScrollPane;
import semsim.SemSimConstants;
import semsim.owl.SemSimOWLFactory;

public class FlowMergerDialog extends JDialog implements
		PropertyChangeListener, ActionListener{

	private static final long serialVersionUID = 7845342502406090947L;
	public JOptionPane optionPane;
	public JPanel mainpanel = new JPanel();
	public String disp;
	public OWLOntology keptont;
	public SemGenScrollPane scrollpane;
	public JButton questionbutton = new JButton(SemGenIcon.questionicon);

	public FlowMergerDialog(String disp, Set<String> flowdepsfromdiscarded,
			OWLOntology discardedont, OWLOntology keptont) {
		this.disp = disp;
		this.keptont = keptont;

		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		questionbutton.addActionListener(this);
		questionbutton.setBorderPainted(false);
		questionbutton.setContentAreaFilled(false);
		questionbutton.setMaximumSize(new Dimension(30, 30));

		for (String flowdep : flowdepsfromdiscarded) {

			try {
				JButton flowbutton = new JButton(SemSimOWLFactory
						.getIRIfragment(flowdep).replace(
								"_dependency", ""));
				flowbutton.setName(flowdep.replace("_dependency", ""));
				flowbutton.setRolloverEnabled(true);
				flowbutton.setForeground(Color.blue);
				flowbutton.addActionListener(this);
				JPanel choicepanel = new JPanel();
				String comp = SemSimOWLFactory.getFunctionalIndObjectProperty(
						discardedont, flowdep, SemSimConstants.SEMSIM_NAMESPACE
								+ "hasComputationalComponent");
				String eq = SemSimOWLFactory.getFunctionalIndDatatypeProperty(
						discardedont, comp, SemSimConstants.SEMSIM_NAMESPACE
								+ "hasComputationalCode");

				JRadioButton addButton = new JRadioButton("+");
				addButton.setActionCommand("+");
				addButton.setToolTipText("Add " + flowbutton.getText()
						+ " to conservation equation for "
						+ SemSimOWLFactory.getIRIfragment(disp));
				JRadioButton subButton = new JRadioButton("-");
				subButton.setActionCommand("-");
				subButton.setToolTipText("Subtract " + flowbutton.getText()
						+ " to conservation equation for "
						+ SemSimOWLFactory.getIRIfragment(disp));
				JRadioButton ignoreButton = new JRadioButton("Ignore");
				ignoreButton.setActionCommand("Ignore");
				ignoreButton.setToolTipText("Ignore the influence of "
						+ flowbutton.getText()
						+ " on conservation equation for "
						+ SemSimOWLFactory.getIRIfragment(disp));

				// Group the radio buttons.
				ButtonGroup group = new ButtonGroup();
				group.add(addButton);
				group.add(subButton);
				group.add(ignoreButton);

				choicepanel.add(flowbutton);
				choicepanel.add(addButton);
				choicepanel.add(subButton);
				choicepanel.add(ignoreButton);
				JTextArea eqarea = new JTextArea();
				eqarea.setEditable(false);
				eqarea.setMaximumSize(new Dimension(300, 300));
				eqarea.setLineWrap(true);
				eqarea.setText(eq);
				mainpanel.add(choicepanel);
				mainpanel.add(eqarea);
				mainpanel.add(Box.createGlue());
				scrollpane = new SemGenScrollPane(mainpanel);
			} catch (OWLException x) {
				x.printStackTrace();
			}
		}
		setModal(true);
		this.setPreferredSize(new Dimension(550, 600));
		this.setTitle("Possible altertions to "
				+ SemSimOWLFactory.getIRIfragment(disp)
				+ " equation identified");

		Object[] array = new Object[] { scrollpane, questionbutton };
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		this.pack();
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o instanceof JButton && o != questionbutton) {
			JButton button = (JButton) o;
			// FIX ME!
			String[] anns = { "" };
			Object[] panestuff = new Object[] {
					new JLabel("Composite annotation: " + anns[0]),
					new JLabel("Singular annotation: " + anns[1]),
					new JLabel("Human readable definition: " + anns[2]) };
			JOptionPane.showMessageDialog(this, panestuff, "Annotations for " + button.getText(), JOptionPane.PLAIN_MESSAGE);
		}

		if (o == questionbutton) {
			try {
				// FIX ME!
				String[] anns = null;
				JOptionPane.showMessageDialog(this,
								"SemGen uses the semantics of model codewords to identify\n"
										+ "conservation equations that may need to be extended to account for\n"
										+ "energetic flows introduced during merging.\n\n"
										+ "Use this dialog to add or subtract the term(s) in blue from the\n"
										+ "original conservation equation for "
										+ SemSimOWLFactory.getIRIfragment(disp) + ":\n"
										+ SemSimOWLFactory.getFunctionalIndDatatypeProperty(keptont,disp + "_computation",SemSimConstants.SEMSIM_NAMESPACE + "hasComputationalCode")
										+ "\n\n"
										+ "If you do not want to alter the conservation equation, select \"Ignore\" for all terms.\n\n"
										+ "Semantic information for "
										+ SemSimOWLFactory.getIRIfragment(disp)
										+ ":\n" + "  Composite annotation: "
										+ anns[0] + "\n"
										+ "  Singular annotation: " + anns[1]
										+ "\n"
										+ "  Human readable definition: "
										+ anns[2] + "\n\n");

			} catch (HeadlessException  | OWLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		Set<String> setofadds = new HashSet<String>();
		String value = optionPane.getValue().toString();
		Set<Boolean> choicesmade = new HashSet<Boolean>();
		if (value == "OK") {
			Component[] components = mainpanel.getComponents();
			for (int x = 0; x < components.length; x++) {
				if (components[x] instanceof JPanel) {
					JPanel cpanel = (JPanel) components[x];
					Component[] innercomp = cpanel.getComponents();
					String flowtomerge = "";
					Boolean somethingselected = false;
					for (int y = 0; y < innercomp.length; y++) {
						if (innercomp[y] instanceof JButton) {
							JButton cbutton = (JButton) innercomp[y];
							flowtomerge = cbutton.getText();
						}
						if (innercomp[y] instanceof JRadioButton) {
							JRadioButton button = (JRadioButton) innercomp[y];
							if (button.isSelected()) {
								somethingselected = true;
								if (!button.getText().equals("Ignore")) {
									setofadds.add(" " + button.getText() + " "
											+ flowtomerge);
								}
							}
						}
					}
					choicesmade.add(somethingselected);
				}
			}
			if (!choicesmade.contains(false)) {
				dispose();
			} else {
				optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			}
		}
		if (value == "Cancel") {
			dispose();
		}
	}
}
