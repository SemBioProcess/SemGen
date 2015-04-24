package semgen.annotation;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.annotatorpane.CodewordAnnotationPanel;
import semgen.annotation.annotatorpane.ModelAnnotationEditor;
import semgen.annotation.annotatorpane.SubmodelAnnotationPanel;
import semgen.annotation.componentlistpanes.CodewordListPane;
import semgen.annotation.componentlistpanes.ModelAnnotationsListPane;
import semgen.annotation.componentlistpanes.SubmodelListPane;
import semgen.annotation.workbench.AnnotatorDrawer;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ModelAnnotationsBench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTab;
import semsim.model.SemSimModel;

import java.net.URI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.awt.BorderLayout;

public class AnnotatorTab extends SemGenTab implements MouseListener, Observer {

	private static final long serialVersionUID = -5360722647774877228L;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	private AnnotatorWorkbench workbench;
	
	public static int initwidth;
	public static int initheight;

	public SemSimModel semsimmodel;

	private AnnotatorToolBar toolbar;
	private JSplitPane splitpane;
	private JSplitPane eastsplitpane;
	public JSplitPane westsplitpane;
	public JSplitPane swsplitpane;
	public SemGenScrollPane annotatorscrollpane = new SemGenScrollPane();
	private SemGenScrollPane treeviewscrollpane = new SemGenScrollPane();

	public AnnotationPanel<? extends AnnotatorDrawer> annotatorpane;

	private CodewordListPane cwpane;
	private SubmodelListPane smpane;	
	public ModelAnnotationsListPane modelannspane;
	
	public AnnotatorTabCodePanel codearea;

	public AnnotatorTab(SemGenSettings sets, GlobalActions gacts, AnnotatorWorkbench bench) {
		super(bench.getCurrentModelName(), SemGenIcon.annotatoricon, "Annotating " + bench.getCurrentModelName(), sets, gacts);
		workbench = bench;
		sourcefile = workbench.getFile();
		workbench.addObserver(this);
		workbench.addObservertoModelAnnotator(this);
	}
	
	public void loadTab() {
		toolbar = new AnnotatorToolBar(globalactions, workbench, settings );
		
		initwidth = settings.getAppWidth();
		initheight = settings.getAppHeight();
		setOpaque(false);
		setLayout(new BorderLayout());
		
		codearea = new AnnotatorTabCodePanel(workbench);
		modelannspane = new ModelAnnotationsListPane(workbench, settings);
		
		SemGenScrollPane legacycodescrollpane = new SemGenScrollPane(codearea);

		annotatorscrollpane.setBackground(SemGenSettings.lightblue);
		annotatorscrollpane.getViewport().setBackground(SemGenSettings.lightblue);

		cwpane = new CodewordListPane(workbench, settings);
		
		swsplitpane  = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cwpane, smpane);
		swsplitpane.setOneTouchExpandable(true);
		
		westsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, modelannspane, swsplitpane); 
		westsplitpane.setOneTouchExpandable(true);

		eastsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, annotatorscrollpane, legacycodescrollpane);
		eastsplitpane.setOneTouchExpandable(true);

		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westsplitpane, eastsplitpane);
		splitpane.setOneTouchExpandable(true);
		
		NewAnnotatorAction();
	}
	//Required to add the application menu as an observer
	public void addObservertoWorkbench(Observer obs) {
		workbench.addObserver(obs);
	}
	
	// --------------------------------//
	// METHODS
	// --------------------------------//
	
	public void NewAnnotatorAction(){
		SemGen.logfilewriter.println("Started new annotater");
		refreshAnnotatableElements();

		add(toolbar, BorderLayout.NORTH);
		add(splitpane, BorderLayout.CENTER);
		setVisible(true);
		
		int iniwloc = settings.scaleWidthforScreen(360);
		int inihloc = settings.scaleWidthforScreen(initheight-150);
		splitpane.setDividerLocation(iniwloc);
		eastsplitpane.setDividerLocation((int)(inihloc)/2);
		swsplitpane.setDividerLocation((int)(inihloc)/2);
		westsplitpane.setDividerLocation((int)(inihloc)/6);
		
		//Dimension topmaxsize = westsplitpane.getTopComponent().getMaximumSize();
		//westsplitpane.getBottomComponent().setMinimumSize(new Dimension(0, inihloc-topmaxsize.height));
		//splitpane.getRightComponent().setMinimumSize(new Dimension(initwidth-iniwloc,0));
		
		cwpane.scrollToTop();
		smpane.scrollToTop();
		
		// If we are hiding the imported codewords, select the first one that is editable

	}

	private void subModelSelected() {
		annotatorpane = new SubmodelAnnotationPanel(workbench, settings, globalactions);
	}
	
	private void codewordSelected() throws BadLocationException {
		annotatorpane = new CodewordAnnotationPanel(workbench, settings, globalactions);
		// Highlight occurrences of codeword in legacy code
		codearea.setCodeword(workbench.openCodewordDrawer().getFocusLookupName());
		codearea.HighlightOccurances(true);
	}

	public void showModelAnnotator() {
		ModelAnnotationEditor modelmetadataeditor = new ModelAnnotationEditor(workbench);
		annotatorscrollpane.setViewportView(modelmetadataeditor);
	}
	
	public void annotationObjectAction() {
		annotatorscrollpane.setViewportView(annotatorpane);
		annotatorscrollpane.scrollToTop();
	}
	
	// Refresh the display of codewords and submodels based on the view options selected in the Annotate menu
	public void refreshAnnotatableElements(){
		int divLoc = splitpane.getDividerLocation();
		if(divLoc==-1)
			divLoc = (int)(settings.getAppWidth())/4;
		
			toolbar.enableSort(true);
			westsplitpane.setBottomComponent(swsplitpane);
		

		splitpane.setDividerLocation(divLoc);
	}

	public void showSelectAnnotationObjectMessage(){
		annotatorscrollpane.getViewport().removeAll();
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Select a codeword or submodel to view annotations");
		label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		panel.add(label, BorderLayout.CENTER);
		annotatorscrollpane.getViewport().add(panel);
		annotatorscrollpane.scrollToTop();
		codearea.removeAllHighlights();
	}
	
	public boolean getModelSaved(){
		return workbench.getModelSaved();
	}
	
	public void setModelSaved(boolean val){
		workbench.setModelSaved(val);
	}

	public void mouseEntered(MouseEvent e) {
		Component component = e.getComponent();
		if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			button.setBorderPainted(true);
			button.setContentAreaFilled(true);
			button.setOpaque(true);
		}
	}

	public void mouseExited(MouseEvent e) {
		Component component = e.getComponent();
		if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			button.setBorderPainted(false);
			button.setContentAreaFilled(false);
			button.setOpaque(false);
		}
	}

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	
	public boolean fileURIMatches(URI uri) {
		return (uri.toString().equals(sourcefile.toURI().toString()));
	}

	public boolean closeTab() {
		return workbench.unsavedChanges();
	}

	@Override
	public boolean isSaved() {
		return getModelSaved();
	}

	@Override
	public void requestSave() {
		workbench.saveModel();
	}
	
	@Override
	public void requestSaveAs() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		if (workbench.saveModelAs()!=null) {
			setTabName(workbench.getCurrentModelName());
			setToolTipText("Annotating " + workbench.getCurrentModelName());
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == ModelAnnotationsBench.ModelChangeEnum.METADATASELECTED) {
			showModelAnnotator();
			return;
		}
		if (arg1==modeledit.cwselection) {
			try {
				codewordSelected();
				annotationObjectAction() ;
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		if (arg1==modeledit.smselection) {
			this.subModelSelected();
			annotationObjectAction();
		}

	}
}
