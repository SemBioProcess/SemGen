package semgen.annotation.termlibrarydialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.model.SemSimTypes;

public class TermEditorPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private AnnotatorWorkbench workbench;
	private JList<String> typechooser = new JList<String>();
	private SemSimTermLibrary library;
	private JList<String> termlist = new JList<String>();
	
	private SemSimTypes[] types = new SemSimTypes[]{
			SemSimTypes.PHYSICAL_PROPERTY,
			SemSimTypes.REFERENCE_PHYSICAL_ENTITY,
			SemSimTypes.CUSTOM_PHYSICAL_ENTITY,
			SemSimTypes.COMPOSITE_PHYSICAL_ENTITY,
			SemSimTypes.PHYSICAL_PROCESS
	};
	
	public TermEditorPanel(AnnotatorWorkbench wb) {
		workbench = wb;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
		library = wb.openTermLibrary();
		setBackground(SemGenSettings.lightblue);
		makePanel();
	}
	
	private void makePanel() {
		String[] names = new String[types.length];
		for (int i = 0; i<types.length; i++) {
			names[i] = types[i].getName();
		}
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		typechooser.setListData(names);
		add(typechooser);
		
		SemGenScrollPane termscroller = new SemGenScrollPane(termlist);
		add(termscroller);
	}

	private void updateList() {

	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
	}
}
