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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class SemGenProgressBar extends JFrame implements ActionListener {

	private static final long serialVersionUID = -408262547613352487L;
	private JLabel msglabel;
	public JProgressBar bar = new JProgressBar();
	private JButton cancelbutton = new JButton("Cancel");
	private CancelEvent onCancel = null;
	protected static JFrame location = null;
	
	public SemGenProgressBar(String msg, Boolean isindeterminant) {
		setUndecorated(true);
		createBar(msg, isindeterminant);
		setLocationRelativeTo(location);
		setAlwaysOnTop(true);
		
		requestFocusInWindow();
		setVisible(true);		
	}
	
	public SemGenProgressBar(String msg, Boolean isindeterminant, Observer obs) {
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
		pack();	
	}
	
	public void updateMessage(final String message){
		SwingUtilities.invokeLater(new Runnable() {
		     public void run() {
		        msglabel.setText(message);
		     }
		  });
		
	}
	public void updateMessageAndValue(final String message, final int val){
		SwingUtilities.invokeLater(new Runnable() {
		     public void run() {
		        msglabel.setText(message);
		        bar.setValue(val);
		     }
		  });
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
	
	public static void setLocation(JFrame frame) {
		location = frame;
	}
}
