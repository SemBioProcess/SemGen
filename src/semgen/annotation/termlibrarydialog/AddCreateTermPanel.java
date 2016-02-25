package semgen.annotation.termlibrarydialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.definitions.ReferenceOntologies.OntologyDomain;
import semsim.definitions.SemSimTypes;

public class AddCreateTermPanel extends JPanel implements ListSelectionListener, ActionListener {
	private static final long serialVersionUID = 1L;

	private SemSimTermLibrary library;
	
	private JList<String> typechooser = new JList<String>();
	private JPanel creatorpane;
	private JLabel header = new JLabel();
	private JButton makebtn = new JButton("Add Component");
	private JButton clearbtn = new JButton("Clear");
	private JPanel rightpane = new JPanel();
	protected JTextArea msgbox = new JTextArea(3, 2);
	
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
		setBackground(SemGenSettings.lightblue);
	}
	
	private void makePanel() {
		String[] names = new String[types.length];
		for (int i = 0; i<types.length; i++) {
			names[i] = types[i].getName();
		}

		SemGenScrollPane typechoosepane = new SemGenScrollPane(typechooser);
		typechoosepane.setBorder(BorderFactory.createTitledBorder("Physical Types"));
		typechoosepane.setAlignmentX(Component.LEFT_ALIGNMENT);
		typechoosepane.setAlignmentY(Component.TOP_ALIGNMENT);
		typechoosepane.setBackground(SemGenSettings.lightblue);
		Dimension dim = new Dimension(360,140);
		typechoosepane.setMinimumSize(dim);
		typechoosepane.setMaximumSize(dim);
		
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		typechooser.setAlignmentX(Box.LEFT_ALIGNMENT);
		typechooser.setAlignmentY(TOP_ALIGNMENT);
		typechooser.setListData(names);
		typechooser.addListSelectionListener(this);
		//Just set the #%#$^ size!
		typechooser.setMinimumSize(dim);
		typechooser.setMaximumSize(dim);
		typechooser.setPreferredSize(dim);

		JPanel optionpane = new JPanel();
		optionpane.setLayout(new BoxLayout(optionpane, BoxLayout.LINE_AXIS));
		optionpane.setAlignmentX(Box.LEFT_ALIGNMENT);
		optionpane.setBackground(SemGenSettings.lightblue);
		optionpane.add(makebtn);
		optionpane.add(clearbtn);
		
		makebtn.setAlignmentX(Box.LEFT_ALIGNMENT);
		clearbtn.setAlignmentX(Box.LEFT_ALIGNMENT);
		makebtn.addActionListener(this);
		clearbtn.addActionListener(this);
		makebtn.setEnabled(false);
		clearbtn.setEnabled(false);
		
		
		optionpane.setAlignmentX(Box.LEFT_ALIGNMENT);
		msgbox.setAlignmentX(Box.LEFT_ALIGNMENT);
		msgbox.setEditable(false);
		msgbox.setOpaque(false);
		msgbox.setText("Select a SemSim Type");
		msgbox.setLineWrap(true);
		msgbox.setWrapStyleWord(true);
		msgbox.setBackground(SemGenSettings.lightblue);
		msgbox.setMaximumSize(dim);

		JPanel typepane = new JPanel();
		typepane.setLayout(new BoxLayout(typepane, BoxLayout.PAGE_AXIS)); 
		typepane.setBackground(SemGenSettings.lightblue);
		typepane.setBorder(BorderFactory.createEtchedBorder());
		typepane.add(Box.createVerticalStrut(12));
		typepane.add(typechoosepane);
		typepane.add(optionpane);
		typepane.add(msgbox);
		typepane.add(Box.createVerticalGlue());
		typepane.setAlignmentY(Box.TOP_ALIGNMENT);
		typepane.setMaximumSize(new Dimension(360,9999));
		add(typepane);
		
		header.setFont(SemGenFont.Bold("Arial", 3));
		header.setAlignmentX(Box.LEFT_ALIGNMENT);
		header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
		
		rightpane.setBackground(SemGenSettings.lightblue);
		rightpane.setAlignmentY(Box.TOP_ALIGNMENT);
		rightpane.setAlignmentX(Box.LEFT_ALIGNMENT);
		rightpane.setLayout(new BoxLayout(rightpane, BoxLayout.PAGE_AXIS)); 
		
		rightpane.add(header);
		add(rightpane);
	}
	
	private void showCreator() {
		if (creatorpane!=null) {
			rightpane.remove(creatorpane);
		}
		toggleOptionVisibility(true);
		Integer sel = typechooser.getSelectedIndex();
		String title = "";
		switch (types[sel]) {
		case PHYSICAL_PROPERTY:
			title = "Import Singular Physical Property";
			creatorpane = new SearchPane(library, OntologyDomain.PhysicalProperty);
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			title = "Import Composite Linkable Physical Property";
			creatorpane = new SearchPane(library, OntologyDomain.AssociatePhysicalProperty);
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			title = "Create Custom Physical Enity";
			creatorpane = new CustomEntityPane(library);
			toggleOptionVisibility(false);
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			title = "Import Reference Physical Enity";
			creatorpane = new SearchPane(library, OntologyDomain.PhysicalEntity);
			break;
		case COMPOSITE_PHYSICAL_ENTITY:
			title = "Create Composite Physical Entity";
			creatorpane = new CPEPanel();
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			title = "Create Custom Physical Process";
			creatorpane = new CustomProcessPane(library);
			toggleOptionVisibility(false);
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			title = "Create Referance Physical Process";
			creatorpane = new SearchPane(library, OntologyDomain.PhysicalProcess);
			break;
		default:
			break;
		
		}
		header.setText(title);
		rightpane.add(creatorpane);
		creatorpane.setAlignmentX(Box.LEFT_ALIGNMENT);
		validate();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if (arg0.getSource().equals(typechooser)) {
			showCreator();
			msgbox.setText(types[typechooser.getSelectedIndex()].getDescription());
			makebtn.setEnabled(false);
			clearbtn.setEnabled(true);
		}
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
		makebtn.setEnabled(false);
		clearbtn.setEnabled(true);
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
			setBackground(SemGenSettings.lightblue);
			for (Component c : getComponents()) {
				c.setBackground(SemGenSettings.lightblue);
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = arg0.getSource();
			if (obj==this.createbtn) {
				createTerm();
				termindex=-1;
			}
			if (obj==this.cancelbtn) {
				clear();
			}
		}

		@Override
		public void makeTerm() {}

		@Override
		public void clearForm() {
			clear();
		}
		
	}
	
	private class CustomProcessPane extends CustomPhysicalProcessPanel implements TermMaker{
		private static final long serialVersionUID = 1L;

		public CustomProcessPane(SemSimTermLibrary lib) {
			super(lib);
			cancelbtn.setText("Clear");
			setBackground(SemGenSettings.lightblue);
			for (Component c : getComponents()) {
				c.setBackground(SemGenSettings.lightblue);
			}
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
			clear();
		}
	}
	
	private class CPEPanel extends JPanel implements TermMaker {
		private static final long serialVersionUID = 1L;
		private CompositeCreator cpec;
		
		public CPEPanel() {
			cpec = new CompositeCreator(library);
			setBackground(SemGenSettings.lightblue);
			add(cpec);
		}
		@Override
		public void makeTerm() {
			cpec.makeTerm();
		}

		@Override
		public void clearForm() {
			cpec.reset();
			revalidate();
			msgbox.setText("Composite components cannot be unspecified");
		}
	}
	
	private class CompositeCreator extends EntitySelectorGroup  {
		private static final long serialVersionUID = 1L;

		public CompositeCreator(SemSimTermLibrary lib) {
			super(lib);
			msgbox.setText("Composite components cannot be unspecified");
		}

		@Override
		public void onChange() {
			pollSelectors();
			if (selections.contains(-1)) {
				makebtn.setEnabled(false);
				msgbox.setText("Composite components cannot be unspecified");
			}
			else {
				makebtn.setEnabled(true);
				msgbox.setText("");
			}
		}

		public void makeTerm() {
			int i = termlib.createCompositePhysicalEntity(pollSelectors());
			msgbox.setText(library.getComponentName(i) + " added as Composite Physical Entity.");
		}		
	}
	
	public class SearchPane extends ReferenceClassFinderPanel implements ListSelectionListener, TermMaker {
		private static final long serialVersionUID = 1L;

		public SearchPane(SemSimTermLibrary lib, OntologyDomain dom) {
			super(lib, dom);
			setBackground(SemGenSettings.lightblue);
			for (Component c : getComponents()) {
				c.setBackground(SemGenSettings.lightblue);
			}
		}
		
		public void valueChanged(ListSelectionEvent arg0) {
	        boolean adjust = arg0.getValueIsAdjusting();
	        if (!adjust) {
		        if(!resultslistright.isSelectionEmpty()){
		        	externalURLbutton.setEnabled(true);
		        	makebtn.setEnabled(true);
		    		clearbtn.setEnabled(true);
		        }
	        }
		}
		
		public void performSearch() {
			super.performSearch();
		}

		@Override
		public void makeTerm() {
			addTermtoLibrary();
			msgbox.setText(library.getComponentName(getSelectedTermIndex()) + " added as " + 
			library.getSemSimType(getSelectedTermIndex()).getName() + ".");
		}

		@Override
		public void clearForm() {
			clear();
		}
	}
}
