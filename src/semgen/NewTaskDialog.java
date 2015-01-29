package semgen;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.encoding.Encoder;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenDialog;

public class NewTaskDialog extends SemGenDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	public JButton annotatebutton = new JButton("Annotate a model",SemGenIcon.annotatoricon);
	public JButton openmenuextractbutton = new JButton("Extract a model", SemGenIcon.extractoricon);
	public JButton openmenumergebutton = new JButton("Merge models", SemGenIcon.mergeicon);
	public JButton encodebutton = new JButton("Encode a model",SemGenIcon.codericon);
	public JButton stagebutton = new JButton("Open the stage", SemGenIcon.annotatemodelicon);
	private GlobalActions globalactions;
	
	public NewTaskDialog(GlobalActions gacts) {
		super("OPEN: Select task");
		globalactions = gacts;
		JPanel openpanel = new JPanel();
		openpanel.setLayout(new BoxLayout(openpanel, BoxLayout.Y_AXIS));
		openpanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		
		JButton[] buttons = {
			annotatebutton,
			openmenuextractbutton,
			openmenumergebutton,
			encodebutton,
			stagebutton
		};
		for (JButton button : buttons) {
			button.setEnabled(true);
			button.setFont(SemGenFont.defaultPlain(1));
			button.addActionListener(this);
			button.setAlignmentX(JButton.CENTER_ALIGNMENT);
			openpanel.add(button);
		}
		openpanel.setPreferredSize(new Dimension(250,165));
		openpanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		
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
		else if (o == openmenumergebutton){
			globalactions.NewMergerTab();
		}
		else if (o == encodebutton) {
			new Encoder();
		}
		else if (o == stagebutton) {
			globalactions.NewStageTab();
		}
		dispose();
	}
}
