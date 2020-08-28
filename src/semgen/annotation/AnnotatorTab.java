package semgen.annotation;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.SemGenSettings.SettingChange;
import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.annotatorpane.CodewordAnnotationPanel;
import semgen.annotation.annotatorpane.SubmodelAnnotationPanel;
import semgen.annotation.componentlistpanes.AnnotatorButtonTree;
import semgen.annotation.componentlistpanes.CodewordListPane;
import semgen.annotation.componentlistpanes.SubmodelListPane;
import semgen.annotation.dialog.modelanns.ModelLevelMetadataDialog;
import semgen.annotation.termlibrarydialog.ReferenceLibraryDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.LibraryRequest;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTab;
import semsim.SemSimObject;
import semsim.fileaccessors.ModelAccessor;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;

import java.util.Observable;
import java.util.Observer;
import java.math.BigDecimal;

public class AnnotatorTab extends SemGenTab implements Observer {

	private static final long serialVersionUID = -5360722647774877228L;
	public ModelAccessor modelaccessor; //Model location originally loaded at start of Annotation session (could be in SBML, MML, CellML, PROJ or SemSim format)
	private AnnotatorWorkbench workbench;
	
	public static int initwidth;
	public static int initheight;

	private AnnotatorToolBar toolbar;
	private JSplitPane splitpane;
	private JSplitPane eastsplitpane;
	private JSplitPane westsplitpane;
	private SemGenScrollPane annotatorscrollpane = new SemGenScrollPane();
	
	private SemGenScrollPane treeviewscrollpane;
	
	private AnnotatorButtonTree tree;
	private SubmodelListPane smpane;
	private CodewordListPane cwpane;
	
	private AnnotationPanel<? extends AnnotatorDrawer<? extends SemSimObject>> annotatorpane;
	
	private AnnotatorTabCodePanel codearea;
	private ReferenceLibraryDialog libdialog;

	public AnnotatorTab(SemGenSettings sets, GlobalActions gacts, AnnotatorWorkbench bench) {
		super(bench.getCurrentModelName(), SemGenIcon.annotatoricon, "Annotating " + bench.getCurrentModelName(), sets, gacts);
		workbench = bench;
		modelaccessor = workbench.getModelAccessor();
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
		
		SemGenScrollPane legacycodescrollpane = new SemGenScrollPane(codearea);
		legacycodescrollpane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), 
				"Model source code", TitledBorder.CENTER, TitledBorder.TOP, SemGenFont.defaultBold(2)));

		annotatorscrollpane.setBackground(SemGenSettings.lightblue);
		annotatorscrollpane.getViewport().setBackground(SemGenSettings.lightblue);

		westsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, null); 
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
		
		tree = new AnnotatorButtonTree(workbench, settings);
		treeviewscrollpane = new SemGenScrollPane(tree);
		
		cwpane = new CodewordListPane(workbench, settings);
		smpane = new SubmodelListPane(workbench, settings);
		
		splitpane.setDividerLocation(iniwloc);
		
		int horseppos = getHorizontalSeparatorPosition();
		eastsplitpane.setDividerLocation(horseppos);	
		westsplitpane.setDividerLocation(horseppos);
		
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

	
	private void changeComponentView() {
		if (settings.useTreeView()) {
			toolbar.enableSort(false);
			westsplitpane.setTopComponent(treeviewscrollpane);
			westsplitpane.setBottomComponent(null);
		}
		else {
			cwpane.update();
			smpane.update();
			toolbar.enableSort(true);
			westsplitpane.setTopComponent(cwpane);
			westsplitpane.setBottomComponent(smpane);
			int loc = getHorizontalSeparatorPosition();
			westsplitpane.setDividerLocation(loc);
		}
	}
	
	private void annotationObjectAction() {
		annotatorscrollpane.setViewportView(annotatorpane);
		annotatorscrollpane.scrollToTop();
		annotatorscrollpane.scrollToLeft();
	}
	
	// Fires when the Auto-copy mapped annotations button is pressed
	private void changeAutoCopyMappedAnnotations() {
		if(annotatorpane.getDrawer() instanceof CodewordToolDrawer){
			CodewordToolDrawer cd = workbench.openCodewordDrawer();
			if(cd.getSelectedIndex() != -1)
				cd.setSelectedIndex(cd.getSelectedIndex()); // refresh the currently selected codeword so the copy button appears
		}
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
	
	
	public void openReferenceLibrary() {
		if (libdialog==null) {
			libdialog = new ReferenceLibraryDialog(settings, workbench);
		}
		libdialog.toFront();
	}
	
	public boolean modelAccessorMatches(ModelAccessor accessor) {
		return accessor.getFullPathToModel().equals(modelaccessor.getFullPathToModel());
	}

	public boolean closeTab() {
		if (libdialog!=null) {
			libdialog.dispose();
		}
		return workbench.unsavedChanges();
	}

	@Override
	public boolean isSaved() {
		return workbench.getModelSaved();
	}

	@Override
	public void requestSave() {
		workbench.saveModel(0);
	}
	
	@Override
	public void requestSaveAs() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		if (workbench.saveModelAs(0)!=null) {
			setTabName(workbench.getCurrentModelName());
			setToolTipText("Annotating " + workbench.getCurrentModelName());
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	@Override
	public void requestExport(){
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		workbench.exportModel(0);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void requestEditModelLevelMetadata() {
		new ModelLevelMetadataDialog(workbench);
	}
	
	
	public int getHorizontalSeparatorPosition(){
		initheight = settings.getAppHeight();
		int inihloc = settings.scaleHeightforScreen(initheight-150);
		return new BigDecimal(inihloc/1.75).intValue();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0==workbench) {
			
			if (arg1 == WBEvent.IMPORT_FREETEXT) {
				if (annotatorpane!=null) {
					annotatorpane.setFreeText(codearea.getHighlightedText());
				}
			}
			if (arg1==WBEvent.CWSELECTION) {
				try {
					codewordSelected();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				annotationObjectAction() ;
			}
			if (arg1==WBEvent.SMSELECTION) {
				this.subModelSelected();
				annotationObjectAction();
			}
			if (arg1==LibraryRequest.REQUEST_LIBRARY) {
				openReferenceLibrary();
				libdialog.openReferenceTab();
			}
			if (arg1==LibraryRequest.REQUEST_CREATOR) {
				openReferenceLibrary();
				libdialog.openCreatorTab();
			}
			if (arg1==LibraryRequest.REQUEST_IMPORT) {
				openReferenceLibrary();
				libdialog.openImportTab();
			}
			if (arg1==LibraryRequest.CLOSE_LIBRARY) {
				libdialog=null;
			}
		}
		
		if (arg0==settings) {
			if (arg1==SettingChange.TOGGLETREE) {
				changeComponentView();
			}
		}
		
		if (arg0==settings) {
			if( arg1 == SettingChange.AUTOANNOTATEMAPPED){
				changeAutoCopyMappedAnnotations();
			}
		}

	}
	
}
