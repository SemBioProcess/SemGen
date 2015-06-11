package semgen.annotation.termlibrarydialog;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.SemGenFont;
import semsim.model.SemSimTypes;

public class ReferenceTermPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private AnnotatorWorkbench workbench;
	private JList<String> typechooser = new JList<String>();
	private SemSimTermLibrary library;
	
	private SemSimTypes[] types = new SemSimTypes[]{
			SemSimTypes.PHYSICAL_PROPERTY,
			SemSimTypes.REFERENCE_PHYSICAL_ENTITY,
			SemSimTypes.CUSTOM_PHYSICAL_ENTITY,
			SemSimTypes.COMPOSITE_PHYSICAL_ENTITY,
			SemSimTypes.PHYSICAL_PROCESS
	};
	
	public ReferenceTermPanel(AnnotatorWorkbench wb) {
		workbench = wb;
		library = wb.openTermLibrary();
		
		makePanel();
	}
	
	public void makePanel() {
		String[] names = new String[types.length];
		for (int i = 0; i<types.length; i++) {
			names[i] = types[i].getName();
		}
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		typechooser.setListData(names);
		add(typechooser);
	}
}
