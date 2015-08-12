package semgen.annotation.termlibrarydialog;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.dialog.termlibrary.CustomPhysicalProcessPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;

public class TermModifyPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;
	private SemSimTermLibrary library;
	private JPanel modpane;
	private Integer compindex;
	
	public TermModifyPanel(AnnotatorWorkbench wb) {
		library = wb.openTermLibrary();
		wb.addObserver(this);
		createGUI();
	}
	
	private void createGUI() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
	}
	
	public void showModifier(int index) {
		compindex = index;
		if (modpane!=null) remove(modpane);
		
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
		validate();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
	
	private class CustomProcessPane extends CustomPhysicalProcessPanel {
		private static final long serialVersionUID = 1L;

		public CustomProcessPane(SemSimTermLibrary lib) {
			super(lib, compindex);

		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			
		}
	}
	
	private class CustomEntityPane extends CustomTermOptionPane  {
		private static final long serialVersionUID = 1L;

		public CustomEntityPane(SemSimTermLibrary lib) {
			super(lib, compindex);
			remove(cancelbtn);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

		}
		
	}
	
	private class CPEPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private CompositeCreator cpec;
		
		
		public CPEPanel() {
			
			cpec = new CompositeCreator(library);
			setBackground(SemGenSettings.lightblue);
			add(cpec);
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

			}
			else {

			}
		}
	
	}
}
