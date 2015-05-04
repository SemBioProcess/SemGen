package semgen.annotation;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.SemGenSettings.SettingChange;
import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.annotatorpane.CodewordAnnotationPanel;
import semgen.annotation.annotatorpane.ModelAnnotationEditor;
import semgen.annotation.annotatorpane.SubmodelAnnotationPanel;
import semgen.annotation.componentlistpanes.AnnotatorButtonTree;
import semgen.annotation.componentlistpanes.CodewordListPane;
import semgen.annotation.componentlistpanes.ModelAnnotationsListPane;
import semgen.annotation.componentlistpanes.SubmodelListPane;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTab;
import semsim.SemSimObject;

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
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	private AnnotatorWorkbench workbench;
	
	public static int initwidth;
	public static int initheight;

	private AnnotatorToolBar toolbar;
	private JSplitPane splitpane;
	private JSplitPane eastsplitpane;
	private JSplitPane westsplitpane;
	private SemGenScrollPane annotatorscrollpane = new SemGenScrollPane();
	
	private JSplitPane swsplitpane;
	private SemGenScrollPane treeviewscrollpane;
	
	private AnnotatorButtonTree tree;
	private SubmodelListPane smpane;
	private CodewordListPane cwpane;
	
	private AnnotationPanel<? extends AnnotatorDrawer<? extends SemSimObject>> annotatorpane;

	private ModelAnnotationsListPane modelannspane;
	
	private AnnotatorTabCodePanel codearea;

	public AnnotatorTab(SemGenSettings sets, GlobalActions gacts, AnnotatorWorkbench bench) {
		super(bench.getCurrentModelName(), SemGenIcon.annotatoricon, "Annotating " + bench.getCurrentModelName(), sets, gacts);
		workbench = bench;
		sourcefile = workbench.getFile();
		workbench.addObserver(this);
		settings.addObserver(this);
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

		westsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, modelannspane, null); 
		westsplitpane.setOneTouchExpandable(true);

		eastsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, annotatorscrollpane, legacycodescrollpane);
		eastsplitpane.setOneTouchExpandable(true);

		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westsplitpane, eastsplitpane);
		splitpane.setOneTouchExpandable(true);
		
		NewAnnotatorAction();
	}
	
	// --------------------------------//
	// METHODS
	// --------------------------------//
	
	public void NewAnnotatorAction(){
		SemGen.logfilewriter.println("Started new annotater");
		
		add(toolbar, BorderLayout.NORTH);
		add(splitpane, BorderLayout.CENTER);
		setVisible(true);
		
		int iniwloc = settings.scaleWidthforScreen(360);
		int inihloc = settings.scaleWidthforScreen(initheight-150);
		
		tree = new AnnotatorButtonTree(workbench, settings);
		treeviewscrollpane = new SemGenScrollPane(tree);
		
		cwpane = new CodewordListPane(workbench, settings);
		smpane = new SubmodelListPane(workbench, settings);
		
		swsplitpane  = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cwpane, smpane);
		swsplitpane.setDividerLocation((int)(inihloc)/2);
		swsplitpane.setOneTouchExpandable(true);
		
		splitpane.setDividerLocation(iniwloc);
		eastsplitpane.setDividerLocation((int)(inihloc)/2);	
		westsplitpane.setDividerLocation((int)(inihloc)/6);
		
		// If we are hiding the imported codewords, select the first one that is editable
		changeComponentView();
		showSelectAnnotationObjectMessage();
	}

	private void subModelSelected() {
		if (annotatorpane!= null) annotatorpane.destroy();
		annotatorpane = new SubmodelAnnotationPanel(workbench, settings, globalactions);
	}
	
	private void codewordSelected() throws BadLocationException {
		if (annotatorpane!= null) annotatorpane.destroy();
		annotatorpane = new CodewordAnnotationPanel(workbench, settings, globalactions);
		// Highlight occurrences of codeword in legacy code
		codearea.setCodeword(workbench.openCodewordDrawer().getFocusLookupName());
		codearea.HighlightOccurances(true);
	}

	public void showModelAnnotator() {
		ModelAnnotationEditor modelmetadataeditor = new ModelAnnotationEditor(workbench);
		annotatorscrollpane.setViewportView(modelmetadataeditor);
	}
	
	private void changeComponentView() {
		if (settings.useTreeView()) {
			toolbar.enableSort(false);
			westsplitpane.setBottomComponent(treeviewscrollpane);
		}
		else {
			cwpane.update();
			smpane.update();
			toolbar.enableSort(true);
			westsplitpane.setBottomComponent(swsplitpane);
		}
	}
	
	private void annotationObjectAction() {
		annotatorscrollpane.setViewportView(annotatorpane);
		annotatorscrollpane.scrollToTop();
		annotatorscrollpane.scrollToLeft();
	}
	
	// Refresh the display of codewords and submodels based on the view options selected in the Annotate menu
	public void refreshAnnotatableElements(){
		int divLoc = splitpane.getDividerLocation();
		if(divLoc==-1)
			divLoc = (int)(settings.getAppWidth())/4;

		splitpane.setDividerLocation(divLoc);
	}

	private void showSelectAnnotationObjectMessage(){
		annotatorscrollpane.getViewport().removeAll();
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Select a codeword or submodel to view annotations");
		label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		panel.add(label, BorderLayout.CENTER);
		annotatorscrollpane.getViewport().add(panel);
		annotatorscrollpane.scrollToTop();
		codearea.removeAllHighlights();
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
		return workbench.getModelSaved();
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
		if (arg0==workbench) {
			if (arg1 == ModelAnnotationsBench.ModelChangeEnum.METADATASELECTED) {
				showModelAnnotator();
				return;
			}
			if (arg1==WBEvent.cwselection) {
				try {
					codewordSelected();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				annotationObjectAction() ;
			}
			if (arg1==WBEvent.smselection) {
				this.subModelSelected();
				annotationObjectAction();
			}
		}
		if (arg0==settings) {
			if (arg1==SettingChange.toggletree) {
				changeComponentView();
			}
		}

	}
}
