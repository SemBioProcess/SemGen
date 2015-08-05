package semgen.annotation.termlibrarydialog;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

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
	
	public TermInformationPanel(AnnotatorWorkbench wb) {
		library = wb.openTermLibrary();		
		
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
		infopanel.setBackground(SemGenSettings.lightblue);
		
		name.setFont(SemGenFont.Bold("Arial", 3));
		infopanel.add(name);
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
			name.setText(library.getComponentName(collection.getTermLibraryIndex()));
			termpane.setVisible(showCompositePane());
		}
	}
		
	private boolean showCompositePane() {
		if (selection==-1) return true; 
		return !(library.getSemSimType(selection).equals(SemSimTypes.REFERENCE_PHYSICAL_PROCESS) || 
				library.getSemSimType(selection).equals(SemSimTypes.CUSTOM_PHYSICAL_PROCESS) ||
				library.getSemSimType(selection).equals(SemSimTypes.PHYSICAL_PROPERTY));
	}
	
	private class InfoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Dimension dim = new Dimension(400,300);
		private JList<String> list = new JList<String>();
		
		public InfoPanel(String name) {
			setPreferredSize(dim);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
			setBorder(BorderFactory.createEtchedBorder());
			setBackground(SemGenSettings.lightblue);
			JLabel namelbl = new JLabel(name);
			namelbl.setFont(SemGenFont.defaultBold(2));
			add(namelbl);
			
			list.setFont(SemGenFont.defaultPlain());
			list.setEnabled(false);
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
	}
}
