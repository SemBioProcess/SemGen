package semgen;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenDialog;

public class NewTaskDialog extends SemGenDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	public JButton annotatebutton = new JButton("Annotate a model",SemGenIcon.annotatoricon);
	public JButton openmenuextractbutton = new JButton("Extract a model", SemGenIcon.extractoricon);
	public JButton stagebutton = new JButton("Open the stage", SemGenIcon.stageicon);
	private GlobalActions globalactions;
	
	public NewTaskDialog(GlobalActions gacts) {
		super("Select task");
		globalactions = gacts;
		JPanel openpanel = new JPanel();
		
		JButton[] buttons = {
			stagebutton,
			annotatebutton,
			openmenuextractbutton,
			
		};
		for (JButton button : buttons) {
			button.setEnabled(true);
			button.setFont(SemGenFont.defaultPlain(1));
			button.addActionListener(this);
			openpanel.add(button);
		}
		
		setPreferredSize(new Dimension(500,110));
		
		JOptionPane selectopentype = new JOptionPane(openpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
		selectopentype.setOptions(new Object[]{});
		setContentPane(selectopentype);
		showDialog();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == annotatebutton) {
			globalactions.NewAnnotatorTab();
		}
		else if (o == openmenuextractbutton) {
			globalactions.NewExtractorTab();
		}
		else if (o == stagebutton) {
			globalactions.NewStageTab();
		}
		dispose();
	}
}
