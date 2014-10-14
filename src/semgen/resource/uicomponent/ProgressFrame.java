package semgen.resource.uicomponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ProgressFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = -408262547613352487L;
	public JLabel msglabel;
	public JProgressBar bar = new JProgressBar();
	public JButton cancelbutton = new JButton("Cancel");
	public SwingWorker<Void,Void> worker;

	public ProgressFrame(String msg, Boolean isindeterminant, SwingWorker<Void,Void> worker) {
		this.worker = worker;
		setPreferredSize(new Dimension(300, 65));
		JPanel progpanel = new JPanel();
		progpanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		progpanel.setLayout(new BorderLayout());
		int bmar = 5;
		bar.setIndeterminate(isindeterminant);
		bar.setStringPainted(!isindeterminant);
		bar.setVisible(true);
		bar.setBorder(BorderFactory.createEmptyBorder(bmar, bmar, bmar, bmar));

		cancelbutton.addActionListener(this);
		
		msglabel = new JLabel(msg);
		msglabel.setBorder(BorderFactory.createEmptyBorder(bmar, bmar, bmar, bmar));
		progpanel.add(msglabel, BorderLayout.NORTH);
		progpanel.add(bar, BorderLayout.SOUTH);
		add(progpanel);
		setUndecorated(true);
		setVisible(true);
		if(isindeterminant) bar.setValue(101);
		requestFocusInWindow();
		pack();
		setLocationRelativeTo(getParent());
	}
	
	public void updateMessage(String message){
		msglabel.setText(message);
	}
	public void updateMessageAndValue(String message, int val){
		msglabel.setText(message);
		bar.setValue(val);
	}

	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==cancelbutton){
			worker.cancel(true);
			if(worker.isDone()){
				
			}
		}
	}
}
