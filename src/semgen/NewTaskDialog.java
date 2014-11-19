package semgen;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.encoding.Encoder;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;

public class NewTaskDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	public JButton annotatebutton = new JButton("Annotate a model",SemGenIcon.annotatoricon);
	public JButton openmenuextractbutton = new JButton("Extract a model", SemGenIcon.extractoricon);
	public JButton openmenumergebutton = new JButton("Merge models", SemGenIcon.mergeicon);
	public JButton encodebutton = new JButton("Encode a model",SemGenIcon.codericon);
	private GlobalActions globalactions;
	
	public NewTaskDialog(GlobalActions gacts) {
		globalactions = gacts;
		setTitle("OPEN: Select task");
		JPanel openpanel = new JPanel();
		openpanel.setLayout(new BoxLayout(openpanel, BoxLayout.Y_AXIS));
		openpanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		
		JButton[] bs = {annotatebutton, openmenuextractbutton, openmenumergebutton, encodebutton};
		for (JButton b : bs) {
			b.setEnabled(true);
			b.setFont(SemGenFont.defaultPlain(1));
			b.addActionListener(this);
			b.setAlignmentX(JButton.CENTER_ALIGNMENT);
			openpanel.add(b);
		}
		openpanel.setPreferredSize(new Dimension(250,135));
		openpanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		
		JOptionPane selectopentype = new JOptionPane(openpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
		selectopentype.setOptions(new Object[]{});
		setContentPane(selectopentype);
		setModal(true);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
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
		dispose();
	}
}
