package semgen.annotation.termlibrarydialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.LibraryEvent;

public class ReferenceLibraryDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	AnnotatorWorkbench workbench;
	JTabbedPane mainpane = new JTabbedPane(JTabbedPane.LEFT);
	ReferenceTermPanel reftermpane;
	ImportAnnotationsPanel importpane;
	
	public ReferenceLibraryDialog(AnnotatorWorkbench wb) {
		super("Annotation Reference Library");
		workbench = wb;
		
		// Set closing behavior
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					workbench.sendTermLibraryEvent(LibraryEvent.closelibrary);
				}
			});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
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
	
	public void openReferenceTab() {
		mainpane.setSelectedComponent(mainpane.getComponent(0));
	}
	
	public void openImportTab() {
		mainpane.setSelectedComponent(mainpane.getComponent(1));
	}
}
