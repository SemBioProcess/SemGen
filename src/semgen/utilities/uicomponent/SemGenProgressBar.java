package semgen.utilities.uicomponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class SemGenProgressBar extends SemGenDialog implements ActionListener {

	private static final long serialVersionUID = -408262547613352487L;
	private JLabel msglabel;
	public JProgressBar bar = new JProgressBar();
	private JButton cancelbutton = new JButton("Cancel");
	private CancelEvent onCancel = null;
	
	public SemGenProgressBar(String msg, Boolean isindeterminant) {
		super("");
		setUndecorated(true);
		createBar(msg, isindeterminant);
	}
	
	public SemGenProgressBar(String msg, Boolean isindeterminant, Observer obs) {
		super("");
		onCancel = new CancelEvent(obs);
		createBar(msg, isindeterminant);
	}
	
	private void createBar(String msg, Boolean isindeterminant) {			
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

		if(isindeterminant) bar.setValue(101);
		this.setModalityType(ModalityType.MODELESS);
		setAlwaysOnTop(true);
		showDialog();
	}
	
	public void updateMessage(final String message){
		        msglabel.setText(message);		
		        revalidate();
		        repaint();
	}
	public void updateMessageAndValue(final String message, final int val){
		        msglabel.setText(message);
		        bar.setValue(val);
		        revalidate();
		        repaint();
	}

	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==cancelbutton){
			onCancel.cancel();
		}
	}
	
	public void setProgressValue(int i) {
		bar.setValue(i);
	}
	
	public class CancelEvent extends Observable{
		public CancelEvent(Observer obs) {
			addObserver(obs);
		}
		public void cancel() {
			setChanged();
			notifyObservers();
		}
	}
}
