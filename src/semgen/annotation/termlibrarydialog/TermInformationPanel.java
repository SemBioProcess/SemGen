package semgen.annotation.termlibrarydialog;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.routines.TermCollector;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.model.SemSimTypes;

public class TermInformationPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private SemSimTermLibrary library;
	private Integer selection;
	private InfoPanel termpane = new InfoPanel("Affiliated Composites");
	private InfoPanel cwpane = new InfoPanel("Affiliated Codewords");
	private JLabel name = new JLabel("No Selection");
	private JEditorPane description = new JEditorPane();
	
	public TermInformationPanel(AnnotatorWorkbench wb) {
		library = wb.openTermLibrary();
		this.setMaximumSize(new Dimension(600, 700));
		createGUI();
	}
	
	private void createGUI() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
		
		makeInformationPanel();
		add(termpane);
		add(cwpane);
		validate();
	}
	
	private void makeInformationPanel() {
		JPanel infopanel = new JPanel();
		infopanel.setLayout(new BoxLayout(infopanel, BoxLayout.PAGE_AXIS));
		infopanel.setBackground(SemGenSettings.lightblue);
		
		name.setFont(SemGenFont.Bold("Arial", 3));
		name.setAlignmentX(Box.LEFT_ALIGNMENT);
		name.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		
		description = new JEditorPane("text/html", "");
		description.setAlignmentX(Box.LEFT_ALIGNMENT);
		description.setEditable(false);
		description.setOpaque(false);
		description.setBackground(new Color(0,0,0,0));
		description.setFont(SemGenFont.defaultPlain(-1));
		description.setBorder(BorderFactory.createEmptyBorder(3, 30, 3, 0));
		
		infopanel.add(name);
		infopanel.add(description);
		infopanel.setAlignmentX(Box.LEFT_ALIGNMENT);
		add(infopanel);
	}
	
	public void updateInformation(TermCollector collection) {
		if (collection==null) {
			termpane.clearList();
			cwpane.clearList();
			selection = -1;
		}
		else {
			selection = collection.getTermLibraryIndex();
			termpane.setListData(collection.getCompositeNames());
			cwpane.setListData(collection.getCodewordNames());
			name.setText("<html>" + library.getComponentName(collection.getTermLibraryIndex()) + "</html>");
			describeComponent();
			termpane.setVisible(showCompositePane());
		}
		validate();
	}
		
	private boolean showCompositePane() {
		if (selection==-1) return true; 
		return !(library.getSemSimType(selection).equals(SemSimTypes.REFERENCE_PHYSICAL_PROCESS) || 
				library.getSemSimType(selection).equals(SemSimTypes.CUSTOM_PHYSICAL_PROCESS) ||
				library.getSemSimType(selection).equals(SemSimTypes.PHYSICAL_PROPERTY));
	}
	
	private void describeComponent() {
		String desc = "<html><body>";
		switch (library.getSemSimType(selection)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			desc = describeCompositeEntity();
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			desc = describeCustomTerm();
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			desc = describeCustomTerm() + "<br><br>" + describeProcess();
			break;
		case PHYSICAL_PROPERTY:
			desc = describeRefTerm();
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			desc = describeRefTerm();
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			desc = describeRefTerm();
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			desc = describeRefTerm() + "<br><br>" + describeProcess();
			break;
		default:
			break;
		
		}
		description.setText("<html><body>" + desc  + "</body></html>");
	}
	
	private String describeRefTerm() {
		return "<b>Ontology:</b> " + library.getOntologyName(selection) + "<br><b>ID:</b> " + library.getReferenceID(selection);
	}
	
	private String describeCustomTerm() {
		String desc = library.getComponentDescription(selection);
		if (desc.isEmpty()) desc = "No Description";
		return desc;
	}
	
	private String describeProcess() {
		return library.listParticipants(selection);
	}
	
	private String describeCompositeEntity() {
		ArrayList<Integer> ents = library.getCompositeEntityIndicies(selection);
		String description = "";
		for (int i = 0; i < (ents.size()-1); i++) {
			description = description + "<b>" + library.getComponentName(ents.get(i)) + "</b> part of ";
		}
		description = description + "<b>" + library.getComponentName(ents.get(ents.size()-1)) + "</b>";
		
		return description;
	}
	
	private class InfoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Dimension dim = new Dimension(500,300);
		private JList<String> list = new JList<String>();
		
		public InfoPanel(String name) {
			setPreferredSize(dim);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
			setAlignmentX(Box.LEFT_ALIGNMENT);
			Border title = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), name, TitledBorder.CENTER, 
					TitledBorder.DEFAULT_POSITION ,SemGenFont.defaultBold());
			
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0), title));
			setBackground(SemGenSettings.lightblue);
			list.setFont(SemGenFont.defaultPlain());
			list.setSelectionModel(new DisabledItemSelectionModel());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setForeground(Color.black);
			add(new SemGenScrollPane(list));
			validate();
		}
		
		public void setListData(ArrayList<String> listdata) {
			list.setListData(listdata.toArray(new String[]{}));
		}
		public void clearList() {
			list.clearSelection();
			list.setListData(new String[]{});
		}
		
		class DisabledItemSelectionModel extends DefaultListSelectionModel {
			private static final long serialVersionUID = 1L;

			@Override
		    public void setSelectionInterval(int index0, int index1) {
		        super.setSelectionInterval(-1, -1);
		    }
		}
	}
}
