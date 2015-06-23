package semgen.annotation.termlibrarydialog;

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
	private TermCollector affiliates;
	private SemSimTermLibrary library;
	private Integer selection;
			
	public TermInformationPanel(AnnotatorWorkbench wb, TermCollector collection) {
		library = wb.openTermLibrary();
		affiliates = collection;
		selection = collection.getTermLibraryIndex();
		
		createGUI();
	}
	
	private void createGUI() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		setBackground(SemGenSettings.lightblue);
		
		makeInformationPanel();
		if (!isProcess()) {
			makeTermAffiliatePanel();
		}
		makeCodewordAffiliatePanel();
		validate();
	}
	
	private void makeInformationPanel() {
		JPanel infopanel = new JPanel();
		infopanel.setBackground(SemGenSettings.lightblue);
		JLabel name = new JLabel(library.getComponentName(selection));
		name.setFont(SemGenFont.Bold("Arial", 3));
		infopanel.add(name);
		add(infopanel);
	}
	
	private void makeTermAffiliatePanel() {
		InfoPanel termpane = new InfoPanel("Affiliated Composites", affiliates.getCompositeNames());
		add(termpane);
	}
	
	private void makeCodewordAffiliatePanel() {
		InfoPanel cwpane = new InfoPanel("Affiliated Codewords", affiliates.getCodewordNames());
		add(cwpane);
	}
	
	private boolean isProcess() {
		return library.getSemSimType(selection).equals(SemSimTypes.REFERENCE_PHYSICAL_PROCESS) || 
				library.getSemSimType(selection).equals(SemSimTypes.CUSTOM_PHYSICAL_PROCESS);
	}
	
	private class InfoPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public InfoPanel(String name, ArrayList<String> listdata) {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
			setBorder(BorderFactory.createEtchedBorder());
			setBackground(SemGenSettings.lightblue);
			JLabel namelbl = new JLabel(name);
			namelbl.setFont(SemGenFont.defaultBold(2));
			add(namelbl);
			
			JList<String> list = new JList<String>(listdata.toArray(new String[]{}));
			list.setFont(SemGenFont.defaultPlain());
			list.setEnabled(false);
			add(new SemGenScrollPane(list));
			validate();
		}
	}
}
