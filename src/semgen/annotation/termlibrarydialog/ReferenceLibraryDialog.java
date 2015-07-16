package semgen.annotation.termlibrarydialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.LibraryRequest;
import semgen.utilities.SemGenFont;

public class ReferenceLibraryDialog extends JFrame implements ComponentListener, ContainerListener {
	private static final long serialVersionUID = 1L;
	AnnotatorWorkbench workbench;
	JTabbedPane mainpane = new JTabbedPane(JTabbedPane.TOP);
	TermEditorTab reftermpane;
	ImportAnnotationsPanel importpane;
	AddCreateTermPanel createaddpane;
	SemGenSettings settings;
	
	public ReferenceLibraryDialog(SemGenSettings sets, AnnotatorWorkbench wb) {
		super("Annotation Reference Library");
		workbench = wb;
		settings = sets;
		// Set closing behavior
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				workbench.sendTermLibraryEvent(LibraryRequest.closelibrary);
			}
		});
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		mainpane.addComponentListener(this);
		
		makeTabs();
		setContentPane(mainpane);
		
		validate();
		pack();
		
		setLocation(settings.centerDialogOverApplication(getSize()));
		setVisible(true);
	}
	
	private void makeTabs() {
		reftermpane = new TermEditorTab(workbench);
		importpane = new ImportAnnotationsPanel(workbench);
		createaddpane = new AddCreateTermPanel(workbench.openTermLibrary());
		createaddpane.setMinimumSize(new Dimension(settings.getAppWidth()/4, settings.getAppHeight()/4));
		
		mainpane.addTab("Add/Create Term", createaddpane);
		mainpane.addTab("Edit Term", reftermpane);
		mainpane.addTab("Annotation Importer", importpane);
		createaddpane.addContainerListener(this);
		for (Component comp : mainpane.getComponents()) {
			comp.addComponentListener(this);
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
	
	@Override
	public void componentHidden(ComponentEvent e) {
		mainpane.validate();
		validate();
		pack();
	}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		mainpane.validate();
		validate();
		pack();
	}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentAdded(ContainerEvent e) {
		validate();
		pack();
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		validate();
		pack();
	}
}
