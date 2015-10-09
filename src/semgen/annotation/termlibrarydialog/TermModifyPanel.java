package semgen.annotation.termlibrarydialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.dialog.termlibrary.CustomPhysicalProcessPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenFont;

public class TermModifyPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private SemSimTermLibrary library;
	private JPanel modpane;
	private Integer compindex;
	private JLabel header = new JLabel();
	private JPanel modcntrls = new JPanel();
	private JLabel msglbl = new JLabel();
	private JButton confirmbtn = new JButton("Modify");
	
	public TermModifyPanel(AnnotatorWorkbench wb) {
		library = wb.openTermLibrary();
		createGUI();
	}
	
	private void createGUI() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
		
		header.setFont(SemGenFont.Bold("Arial", 3));
		header.setAlignmentX(Box.LEFT_ALIGNMENT);
		header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
		header.setAlignmentY(Box.TOP_ALIGNMENT);
		add(header);
		
		modcntrls.setLayout(new BoxLayout(modcntrls, BoxLayout.LINE_AXIS));
		modcntrls.setBackground(SemGenSettings.lightblue);
		modcntrls.setAlignmentX(LEFT_ALIGNMENT);
		modcntrls.setAlignmentY(Box.BOTTOM_ALIGNMENT);
		modcntrls.add(confirmbtn);
		confirmbtn.addActionListener(this);
		confirmbtn.setEnabled(false);
		modcntrls.add(msglbl);
	}
	
	public void showModifier(int index) {
		compindex = index;
		String name = library.getComponentName(compindex);
		header.setText("<html>Editing " + name + "</html>");
		if (modpane!=null) {
			remove(modpane);
			remove(modcntrls);
		}
		
		switch (library.getSemSimType(compindex)) {
		case CUSTOM_PHYSICAL_ENTITY:
			modpane = new CustomEntityPane(library);
			break;
		case COMPOSITE_PHYSICAL_ENTITY:
			modpane = new CPEPanel();
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			modpane = new CustomProcessPane(library);
			break;
		default:
			break;
		}
		
		add(modpane);
		add(modcntrls);
		validate();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		((TermEditor)modpane).editTerm();
	}
	
	private interface TermEditor {
		public void editTerm();
	}
	
	private class CustomProcessPane extends CustomPhysicalProcessPanel implements TermEditor {
		private static final long serialVersionUID = 1L;

		public CustomProcessPane(SemSimTermLibrary lib) {
			super(lib, compindex);
			remove(confirmpan);
			createbtn = confirmbtn;
			descriptionarea.addKeyListener(this);
			msgbox = msglbl;
			setAlignmentX(Box.LEFT_ALIGNMENT);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {}

		@Override
		public void editTerm() {
			modifyTerm();
			confirmbtn.setEnabled(false);
			msglbl.setText("Custom process modified.");
		}
	}
	
	private class CustomEntityPane extends CustomTermOptionPane implements TermEditor {
		private static final long serialVersionUID = 1L;

		public CustomEntityPane(SemSimTermLibrary lib) {
			super(lib, compindex);
			remove(confirmpan);
			createbtn = confirmbtn;
			setAlignmentX(Box.LEFT_ALIGNMENT);
			descriptionarea.addKeyListener(this);
			
			msgbox = msglbl;
			
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {}

		@Override
		public void editTerm() {
			modifyTerm();
			msglbl.setText("Custom entity modified.");
		}
		
	}
	
	private class CPEPanel extends JPanel implements TermEditor{
		private static final long serialVersionUID = 1L;
		private CompositeCreator cpec;
		
		
		public CPEPanel() {
			setAlignmentX(Box.LEFT_ALIGNMENT);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
			cpec = new CompositeCreator(library);
			setBackground(SemGenSettings.lightblue);
			add(cpec);
			add(Box.createVerticalGlue());
		}
		
		@Override
		public void editTerm() {
			cpec.editTerm();
			confirmbtn.setEnabled(false);
			msglbl.setText("Composite entity modified.");
		}
	}
	
	private class CompositeCreator extends EntitySelectorGroup  {
		private static final long serialVersionUID = 1L;
				
		public CompositeCreator(SemSimTermLibrary lib) {
			super(lib,library.getCompositeEntityIndicies(compindex), true);
			
		}

		@Override
		public void onChange() {
			pollSelectors();
			if (selections.contains(-1)) {
				confirmbtn.setEnabled(false);
				msglbl.setText("Composite entities cannot contain unspecified terms.");
			}
			else if (library.containsCompositeEntitywithTerms(selections)) {
				msglbl.setText("Composite physical entity with those terms already exists.");
				confirmbtn.setEnabled(false);
			}
			else {
				confirmbtn.setEnabled(true);
				msglbl.setText("Valid composite physical entity");
			}
		}
		
		public void editTerm() {
			library.setCompositeEntityComponents(compindex, selections);
		}
		
	
	}


}
