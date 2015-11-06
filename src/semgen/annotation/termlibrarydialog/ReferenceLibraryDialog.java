package semgen.annotation.termlibrarydialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.LibraryRequest;
import semgen.utilities.SemGenFont;

public class ReferenceLibraryDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	AnnotatorWorkbench workbench;
	JTabbedPane mainpane = new JTabbedPane(JTabbedPane.TOP);
	TermEditorTab reftermpane;
	ImportAnnotationsPanel importpane;
	AddCreateTermPanel createaddpane;
	SemGenSettings settings;
	int prefwidth = 1100;
	int prefheight = 850;
	
	public ReferenceLibraryDialog(SemGenSettings sets, AnnotatorWorkbench wb) {
		super("Annotation Reference Library");
		workbench = wb;
		settings = sets;
		// Set closing behavior
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				workbench.sendTermLibraryEvent(LibraryRequest.CLOSE_LIBRARY);
			}
		});
		//setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		makeTabs();

		setContentPane(new JScrollPane(mainpane));
		
		float fracheight = 0.85F;
		float fracwidth = 0.8F;
		
		boolean tootall = settings.getScreenHeight() < prefheight;
		boolean toowide = settings.getScreenWidth() < prefwidth;
		
		int height = tootall ? Math.round(fracheight * settings.getAppHeight()) : prefheight;
		int width = toowide ? Math.round(fracwidth * settings.getAppWidth()) : prefwidth;
		
		setPreferredSize(new Dimension(width, height));
		validate();
		pack();
		
		setLocation(settings.centerDialogOverApplication(getSize()));
		
		setVisible(true);
	}
	
	private void makeTabs() {
		reftermpane = new TermEditorTab(workbench);
		importpane = new ImportAnnotationsPanel(workbench, this);
		createaddpane = new AddCreateTermPanel(workbench.openTermLibrary());
		
		mainpane.addTab("Add/Create Term", createaddpane);
		mainpane.addTab("Edit Term", reftermpane);
		mainpane.addTab("Annotation Importer", importpane);
		for (Component comp : mainpane.getComponents()) {
			comp.setFont(SemGenFont.defaultPlain(1));
		}
	}
	
	public void openCreatorTab() {
		mainpane.setSelectedComponent(mainpane.getComponent(0));
	}
		
	public void openReferenceTab() {
		mainpane.setSelectedComponent(mainpane.getComponent(1));
	}
	
	public void openImportTab() {
		mainpane.setSelectedComponent(mainpane.getComponent(2));
	}
	
}
