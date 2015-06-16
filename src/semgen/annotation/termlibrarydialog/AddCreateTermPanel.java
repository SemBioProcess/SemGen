package semgen.annotation.termlibrarydialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import semgen.SemGenSettings;
import semgen.annotation.common.CustomTermOptionPane;
import semgen.annotation.common.EntitySelectorGroup;
import semgen.annotation.dialog.termlibrary.CustomPhysicalProcessPanel;
import semgen.annotation.dialog.termlibrary.ReferenceClassFinderPanel;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenFont;
import semsim.model.SemSimTypes;
import semsim.utilities.ReferenceOntologies.OntologyDomain;

public class AddCreateTermPanel extends JPanel implements ListSelectionListener, ActionListener {
	private static final long serialVersionUID = 1L;

	private SemSimTermLibrary library;
	
	private JList<String> typechooser = new JList<String>();
	private JPanel creatorpane;
	private JButton makebtn = new JButton("Add Component");
	private JButton clearbtn = new JButton("Clear");
	protected JLabel msgbox = new JLabel("Select a SemSim Type");
	
	private SemSimTypes[] types = new SemSimTypes[]{
			SemSimTypes.PHYSICAL_PROPERTY,
			SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE,
			SemSimTypes.REFERENCE_PHYSICAL_ENTITY,
			SemSimTypes.CUSTOM_PHYSICAL_ENTITY,
			SemSimTypes.COMPOSITE_PHYSICAL_ENTITY,
			SemSimTypes.CUSTOM_PHYSICAL_PROCESS,
			SemSimTypes.REFERENCE_PHYSICAL_PROCESS
	};
	
	public AddCreateTermPanel(SemSimTermLibrary lib) {
		library = lib;
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS)); 
		makePanel();
	}
	
	private void makePanel() {
		String[] names = new String[types.length];
		for (int i = 0; i<types.length; i++) {
			names[i] = types[i].getName();
		}
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		typechooser.setListData(names);
		typechooser.addListSelectionListener(this);
		typechooser.setBorder(BorderFactory.createTitledBorder("Physical Types"));
		
		JPanel optionpane = new JPanel();
		optionpane.setBackground(SemGenSettings.lightblue);
		optionpane.add(makebtn);
		optionpane.add(clearbtn);
		makebtn.addActionListener(this);
		clearbtn.addActionListener(this);
		toggleOptions(false);
		
		JPanel typepane = new JPanel();
		typepane.setLayout(new BoxLayout(typepane, BoxLayout.PAGE_AXIS)); 
		typepane.setBackground(SemGenSettings.lightblue);
		typepane.add(typechooser);
		typepane.add(optionpane);
		typepane.setAlignmentX(LEFT_ALIGNMENT);
		typepane.add(msgbox);
		add(typepane);
	}
	
	private void showCreator() {
		if (creatorpane!=null) remove(creatorpane);
		toggleOptionVisibility(true);
		Integer sel = typechooser.getSelectedIndex();
		switch (types[sel]) {
		case PHYSICAL_PROPERTY:
			creatorpane = new SearchPane(library, OntologyDomain.PhysicalProperty);
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			creatorpane = new SearchPane(library, OntologyDomain.AssociatePhysicalProperty);
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			creatorpane = new CustomEntityPane(library);
			toggleOptionVisibility(false);
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			creatorpane = new SearchPane(library, OntologyDomain.PhysicalEntity);
			break;
		case COMPOSITE_PHYSICAL_ENTITY:
			JPanel cpane = new JPanel();
			cpane.add(new CompositeCreator(library));
			creatorpane = cpane;
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			creatorpane = new CustomProcessPane(library);
			toggleOptionVisibility(false);
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			creatorpane = new SearchPane(library, OntologyDomain.PhysicalProcess);
			break;
		default:
			break;
		
		}
		add(creatorpane);
		validate();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if (arg0.getSource().equals(typechooser)) {
			showCreator();
			msgbox.setText("");
			toggleOptions(false);
		}
	}
		
	private void toggleOptions(boolean toggle) {
		makebtn.setEnabled(toggle);
		clearbtn.setEnabled(toggle);
	}
	
	private void toggleOptionVisibility(boolean toggle) {
		makebtn.setVisible(toggle);
		clearbtn.setVisible(toggle);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o==makebtn) {
			((TermMaker)creatorpane).makeTerm();
		}
		if (o==clearbtn) {
			((TermMaker)creatorpane).clearForm();
		}
		toggleOptions(false);
	}
	
	private interface TermMaker {
		public void makeTerm();
		public void clearForm();
	}
	
	private class CustomEntityPane extends CustomTermOptionPane implements TermMaker {
		private static final long serialVersionUID = 1L;

		public CustomEntityPane(SemSimTermLibrary lib) {
			super(lib);
			cancelbtn.setText("Clear");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj==this.createbtn) {
				createTerm();
				termindex=-1;
			}
			if (obj==this.cancelbtn) {
				
			}
		}

		@Override
		public void makeTerm() {}

		@Override
		public void clearForm() {
			
		}
		
	}
	
	private class CustomProcessPane extends CustomPhysicalProcessPanel implements TermMaker{
		private static final long serialVersionUID = 1L;

		public CustomProcessPane(SemSimTermLibrary lib) {
			super(lib);
			cancelbtn.setText("Clear");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj==this.createbtn) {
				createTerm();
				termindex=-1;
			}
			if (obj==this.cancelbtn) {
				
			}
		}

		@Override
		public void makeTerm() {}

		@Override
		public void clearForm() {
			
		}
	}
	
	private class CompositeCreator extends EntitySelectorGroup implements TermMaker {
		private static final long serialVersionUID = 1L;

		public CompositeCreator(SemSimTermLibrary lib) {
			super(lib);
			
		}

		@Override
		public void onChange() {
			if (selections.contains(-1)) {
				makebtn.setEnabled(false);
				msgbox.setText("Composite components cannot be unspecified");
			}
			else {
				makebtn.setEnabled(true);
				msgbox.setText("");
			}
		}

		@Override
		public void makeTerm() {
			termlib.createCompositePhysicalEntity(pollSelectors());
		}

		@Override
		public void clearForm() {
			clearGroup();
		}
		
	}
	
	public class SearchPane extends ReferenceClassFinderPanel implements ListSelectionListener, TermMaker {
		private static final long serialVersionUID = 1L;

		public SearchPane(SemSimTermLibrary lib, OntologyDomain dom) {
			super(lib, dom);
		}
		
		public void valueChanged(ListSelectionEvent arg0) {
	        boolean adjust = arg0.getValueIsAdjusting();
	        if (!adjust) {
		        if(!resultslistright.isSelectionEmpty()){
		        	externalURLbutton.setEnabled(true);
		        	toggleOptions(true);
		        }
	        }
		}
		
		public void performSearch() {
			super.performSearch();
		}

		@Override
		public void makeTerm() {
			addTermtoLibrary();
		}

		@Override
		public void clearForm() {
			
		}
	}
}
