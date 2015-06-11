package semgen.annotation.termlibrarydialog;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import semgen.annotation.workbench.AnnotatorWorkbench;

public class ReferenceLibraryDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	AnnotatorWorkbench workbench;
	JTabbedPane mainpane = new JTabbedPane(JTabbedPane.LEFT);
	ReferenceTermPanel reftermpane;
	ImportAnnotationsPanel importpane;
	
	public ReferenceLibraryDialog(AnnotatorWorkbench wb) {
		super("Annotation Reference Library");
		workbench = wb;
		
		makeTabs();
		setContentPane(mainpane);
		validate();
		pack();
		setVisible(true);
	}
	
	private void makeTabs() {
		reftermpane = new ReferenceTermPanel(workbench);
		importpane = new ImportAnnotationsPanel(workbench);
		
		mainpane.addTab("Reference Library", reftermpane);
		mainpane.addTab("Annotation Importer", importpane);
	}
}
