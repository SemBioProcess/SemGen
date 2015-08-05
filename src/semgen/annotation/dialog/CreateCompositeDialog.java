package semgen.annotation.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.uicomponent.SemGenDialog;

public class CreateCompositeDialog extends SemGenDialog implements ActionListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	protected JButton createbtn = new JButton("Create");
	protected JButton cancelbtn = new JButton("Cancel");
	protected JLabel msgbox = new JLabel("Select a Physical Entity");
	private CompositeBuilder esg;
	private Integer composite = -1;
	
	public CreateCompositeDialog(SemSimTermLibrary lib) {
		super("Create Composite Physical Entity");
		setBackground(SemGenSettings.lightblue);
		esg = new CompositeBuilder(lib);
		makeGUI();
	}
	
	private void makeGUI() {
		JPanel mainpane = new JPanel(new BorderLayout());
		mainpane.add(esg, BorderLayout.NORTH);
		esg.addComponentListener(this);
		createbtn.setEnabled(false);
		createbtn.addActionListener(this);
		cancelbtn.addActionListener(this);
		
		JPanel confirmpan = new JPanel();
		
		confirmpan.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
		confirmpan.setLayout(new BoxLayout(confirmpan, BoxLayout.X_AXIS));
		confirmpan.add(msgbox);
		confirmpan.add(createbtn);
		confirmpan.add(cancelbtn);
		
		mainpane.add(confirmpan, BorderLayout.SOUTH);
		mainpane.add(Box.createVerticalGlue());
		
		this.setContentPane(mainpane);
		showDialog();
	}

	public int getComposite() {
		return composite;
	}
	
	class CompositeBuilder extends EntitySelectorGroup {
		private static final long serialVersionUID = 1L;

		public CompositeBuilder(SemSimTermLibrary lib) {
			super(lib);
		}

		@Override
		public void onChange() {
			ArrayList<Integer> selections = pollSelectors();
			if (selections.contains(-1)) {
				createbtn.setEnabled(false);
				msgbox.setText("Composite components cannot be unspecified");
			}
			else {
				createbtn.setEnabled(true);
				msgbox.setText("");
			}
		}
		
		protected int createComposite() {
			return termlib.createCompositePhysicalEntity(pollSelectors());
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object obj = arg0.getSource();
		if (obj.equals(cancelbtn)) {
			dispose();
		}
		if (obj.equals(createbtn)) {
			composite = esg.createComposite();
			dispose();
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		validate();
		pack();
	}

	@Override
	public void componentShown(ComponentEvent e) {}


	
}
