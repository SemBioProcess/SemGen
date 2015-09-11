package semgen.annotation.dialog;

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
import javax.swing.JSeparator;

import semgen.SemGenSettings;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;

public class CreateCompositeDialog extends SemGenDialog implements ActionListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	protected JButton createbtn = new JButton("Create");
	protected JButton cancelbtn = new JButton("Cancel");
	private CompositeBuilder esg;
	private Integer composite = -1;
	
	public CreateCompositeDialog(SemSimTermLibrary lib) {
		super("Create Composite Physical Entity");
		setBackground(SemGenSettings.lightblue);
		esg = new CompositeBuilder(lib);
		makeGUI();
	}
	
	private void makeGUI() {
		JPanel mainpane = new JPanel();
		mainpane.setBackground(SemGenSettings.lightblue);
		mainpane.setLayout(new BoxLayout(mainpane, BoxLayout.PAGE_AXIS));
		JLabel header = new JLabel("Create Composite Physical Entity");
		header.setAlignmentY(TOP_ALIGNMENT);
		header.setFont(SemGenFont.Bold("Arial", 3));
		header.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
		mainpane.add(header);
		mainpane.add(esg);
		mainpane.setAlignmentX(LEFT_ALIGNMENT);
		esg.addComponentListener(this);
		esg.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		createbtn.setEnabled(false);
		createbtn.addActionListener(this);
		cancelbtn.addActionListener(this);
		
		JPanel confirmpan = new JPanel();
		
		confirmpan.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
		confirmpan.setOpaque(false);
		confirmpan.setLayout(new BoxLayout(confirmpan, BoxLayout.X_AXIS));
		confirmpan.add(Box.createGlue());
		confirmpan.add(createbtn);
		confirmpan.add(cancelbtn);
		confirmpan.setAlignmentX(LEFT_ALIGNMENT);
		
		mainpane.add(new JSeparator());
		mainpane.add(confirmpan);
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
			setAlignmentY(TOP_ALIGNMENT);
			setAlignmentX(LEFT_ALIGNMENT);
		}

		@Override
		public void onChange() {
			ArrayList<Integer> selections = pollSelectors();
			if (selections.contains(-1)) {
				createbtn.setEnabled(false);
			}
			else createbtn.setEnabled(true);
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
