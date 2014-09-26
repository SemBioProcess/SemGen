package semgen.resource;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class NewTaskDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	public JButton annotatebutton = new JButton("Annotate a model",SemGenIcon.annotatoricon);
	public JButton openmenuextractbutton = new JButton("Extract a model", SemGenIcon.extractoricon);
	public JButton openmenumergebutton = new JButton("Merge models", SemGenIcon.mergeicon);
	public JButton encodebutton = new JButton("Encode a model",SemGenIcon.codericon);
	private int choice = -1;
	
	public NewTaskDialog() {
		setTitle("OPEN: Select task");
		setLocationRelativeTo(getParent());
		
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
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == annotatebutton) {
			choice = 0;
		}
		else if (o == openmenuextractbutton) {
			choice = 1;
		}
		else if (o == openmenumergebutton){
			choice = 2;
		}
		
		else if (o == encodebutton) {
			choice = 3;
		}
		dispose();
	}
	
	public int getChoice() {
		return choice;
	}
	
}
