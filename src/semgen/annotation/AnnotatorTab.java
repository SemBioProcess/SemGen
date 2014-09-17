package semgen.annotation;

import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.annotation.annotationpane.AnnotationPanel;
import semgen.annotation.annotationtree.AnnotationTreePanel;
import semgen.annotation.codewordpane.CodewordButton;
import semgen.annotation.codewordpane.CodewordPanel;
import semgen.annotation.submodelpane.SubModelPanel;
import semgen.annotation.submodelpane.SubmodelButton;
import semgen.annotation.uicomponents.ComponentPane;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ReferenceTermToolbox;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.uicomponents.ProgressBar;
import semgen.resource.uicomponents.SemGenScrollPane;
import semgen.resource.uicomponents.SemGenTab;

import java.net.URI;
import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.awt.BorderLayout;
import java.io.IOException;

public class AnnotatorTab extends SemGenTab implements Observer {

	private static final long serialVersionUID = -5360722647774877228L;
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	public URI fileURI;

	private JSplitPane splitpane, eastsplitpane, westsplitpane;
	private ButtonScrollPane submodelscrollpane, codewordscrollpane, treeviewscrollpane, focusPane;
	private SemGenScrollPane dialogscrollpane;

	public AnnotationPanel anndialog;
	public AnnotationTreePanel treepanel;

	public CodewordPanel codewordpanel;
	private SubModelPanel submodelpanel;
	private AnnotatorTabCodePanel codearea;

	JPanel genmodinfo = new JPanel(new BorderLayout());
	
	private AnnotatorWorkbench canvas;
	private ReferenceTermToolbox toolbox;

	public AnnotatorTab(File srcfile, SemGenSettings sets, UniversalActions ua) {
		super(srcfile.getName(), SemGenIcon.annotatoricon, "Annotating " + srcfile.getName(), sets, ua);
		sourcefile = srcfile;
		fileURI = srcfile.toURI();
		toolbox = new ReferenceTermToolbox();

		setOpaque(false);
		setLayout(new BorderLayout());
	}
	
	public Boolean Initialize(){
		SemGen.logfilewriter.println("Started new annotater");
		
		canvas = new AnnotatorWorkbench(sourcefile, settings.doAutoAnnotate());
		codearea = new AnnotatorTabCodePanel(canvas);
		
		submodelpanel = new SubModelPanel(canvas.sma, settings);
		submodelscrollpane = new ButtonScrollPane(submodelpanel);
		
		dialogscrollpane = new SemGenScrollPane();
		dialogscrollpane.setBackground(SemGenSettings.lightblue);
		
		codewordpanel = new CodewordPanel(canvas.cwa, settings);
		codewordscrollpane = new ButtonScrollPane(codewordpanel);
		InputMap im = codewordscrollpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// Override up and down key functions so user can use arrows to move between codewords
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
		
		treeviewscrollpane = new ButtonScrollPane(treepanel);
		
		westsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codewordscrollpane, submodelscrollpane);
		westsplitpane.setOneTouchExpandable(true);
		
		eastsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dialogscrollpane, new SemGenScrollPane(codearea));
		eastsplitpane.setOneTouchExpandable(true);

		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westsplitpane, eastsplitpane);
		splitpane.setOneTouchExpandable(true);
		
		ProgressBar progframe = new ProgressBar("Sorting codewords...", true);
		refreshAnnotatableElements();
		
		genmodinfo.add(new AnnotatorToolbar(uacts, settings, canvas, toolbox), BorderLayout.WEST);
		genmodinfo.setOpaque(true);
		
		showModelSourceCode();
		showAnnotaterPanes();
		
		codewordscrollpane.scrollToTop();
		submodelscrollpane.scrollToTop();
								
		if (progframe!=null) progframe.dispose();
		return true;
	}
	

	
	// --------------------------------//
	// METHODS
	// --------------------------------//
	
	public void showAnnotaterPanes(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
			   add(genmodinfo, BorderLayout.NORTH);
				add(splitpane, BorderLayout.CENTER);
				setVisible(true);
				eastsplitpane.setDividerLocation((int)(settings.getAppHeight()-150)/2);
				westsplitpane.setDividerLocation((int)(settings.getAppWidth()-150)/2);
		   }
		});
	}
	
	public void addPanelTitle(SemGenScrollPane scrollpane) {
		scrollpane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), 
			codewordpanel.getName(), 
			TitledBorder.CENTER, TitledBorder.TOP, SemGenFont.defaultBold(2)));
	}
	
	public void annotationObjectAction() throws BadLocationException, IOException {
		dialogscrollpane.getViewport().removeAll();
		codearea.setCodeword(canvas.getFocusName());
		codearea.HighlightOccurances(true);

		dialogscrollpane.scrollToTop();
	}
	
	public void showModelSourceCode() {
		try {
			codearea.setCodeViewer();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	// Refresh the display of codewords and submodels based on the view options selected in the Annotate menu
	public void refreshAnnotatableElements(){
		int divLoc = splitpane.getDividerLocation();
		if(divLoc==-1)
			divLoc = (settings.getAppWidth())/3;
		
		// If the "Tree view" menu item is selected...
		if(settings.useTreeView()){
			splitpane.setTopComponent(treeviewscrollpane);
			treepanel.setFocusNode();
			treeviewscrollpane.scrollToLeft();
		}
		else{
			splitpane.setTopComponent(westsplitpane);
						
			focusPane.scrolltoFocusButton();
		}
		splitpane.setDividerLocation(divLoc);
	}
					
	public int closeTab() {
		if (!canvas.getModelSaved()) {
			String title = "[unsaved file]";
			if(fileURI!=null){
				title =  new File(fileURI).getName();
			}
			return JOptionPane.showConfirmDialog(getParent(),
					"Save changes before closing?", title,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}  
		System.gc();		
		return -1;
}
	
	public void scrolltoButton(CodewordButton aob) {
		codewordscrollpane.scrollToComponent(aob);
	}
	public void scrolltoButton(SubmodelButton aob) {
		submodelscrollpane.scrollToComponent(aob);
	}

	@Override
	public int getTabType() {
		return AnnotatorTab;
	}
	
	@Override
	public void requestSaveAs() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		setTitle(canvas.getCurrentModelName());

		setToolTipText("Annotating " + canvas.getCurrentModelName());
		
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void requestSave() {
		canvas.saveModel();	
	}
	
	public boolean checkFile(URI uri) {
			return (uri.toString().equals(fileURI.toString()));
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		//Show code from new file
		if (arg0.equals(300)) {
			showModelSourceCode();
		}
		refreshAnnotatableElements();
	}

	@Override
	public boolean isSaved() {
		return canvas.getModelSaved();
	}
	
	class ButtonScrollPane extends SemGenScrollPane {
		private static final long serialVersionUID = 1L;
		ComponentPane buttonpane;
		ButtonScrollPane(ComponentPane view) {
			super(view);
			buttonpane = view;
		}
		
		public void scrolltoFocusButton() {
			this.scrollToComponent(buttonpane.getFocusButton());
		}
	}
}