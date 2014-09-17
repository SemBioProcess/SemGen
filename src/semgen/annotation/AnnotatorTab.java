package semgen.annotation;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGen;
import semgen.SemGenGUI;
import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.annotation.annotationpane.AnnotationPanel;
import semgen.annotation.annotationtree.AnnotatorButtonTree;
import semgen.annotation.codewordpane.CodewordButton;
import semgen.annotation.codewordpane.CodewordPanel;
import semgen.annotation.dialog.HumanDefEditor;
import semgen.annotation.dialog.textminer.TextMinerDialog;
import semgen.annotation.submodelpane.SubModelPanel;
import semgen.annotation.submodelpane.SubmodelButton;
import semgen.annotation.uicomponents.AnnotationObjectButton;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ReferenceTermToolbox;
import semgen.resource.ComparatorByName;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenResource;
import semgen.resource.SemGenTask;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.uicomponents.ProgressBar;
import semgen.resource.uicomponents.SemGenScrollPane;
import semgen.resource.uicomponents.SemGenTab;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.Submodel;
import semsim.reading.ModelClassifier;

import java.net.URI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Hashtable;
import java.util.HashSet;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Comparator;
import java.util.Arrays;

public class AnnotatorTab extends SemGenTab implements Observer {

	private static final long serialVersionUID = -5360722647774877228L;
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	public URI fileURI;
	
	public HumanDefEditor humdefeditor;

	private JSplitPane splitpane, eastsplitpane, westsplitpane;
	public SemGenScrollPane submodelscrollpane, codewordscrollpane, treeviewscrollpane;
	public SemGenScrollPane dialogscrollpane;
	
	public AnnotationPanel anndialog;
	public AnnotationObjectButton focusbutton;

	public CodewordPanel codewordpanel;
	private JPanel submodelpanel;
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
		submodelscrollpane = new SemGenScrollPane(submodelpanel);
		
		dialogscrollpane = new SemGenScrollPane();
		dialogscrollpane.setBackground(SemGenResource.lightblue);
		
		codewordpanel = new CodewordPanel(canvas.cwa, settings);
		codewordscrollpane = new SemGenScrollPane(codewordpanel);
		InputMap im = codewordscrollpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// Override up and down key functions so user can use arrows to move between codewords
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
		
		treeviewscrollpane = new SemGenScrollPane();
		
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
			treeviewscrollpane.getViewport().removeAll();
			treeviewscrollpane.getViewport().add(tree);
			
			// If focusbutton in Annotator associated with focusnode here, set the selected node
			if(tree.focusnode!=null){
				tree.setSelectionPath(new TreePath(tree.focusnode.getPath()));
				tree.scrollPathToVisible(new TreePath(tree.focusnode.getPath()));
			}
			treeviewscrollpane.scrollToLeft();
		}
		else{
			splitpane.setTopComponent(westsplitpane);
						
			if(focusbutton!=null){
				if(focusbutton instanceof CodewordButton)
					codewordscrollpane.scrollToComponent(focusbutton);
				else if(focusbutton instanceof SubmodelButton)
					submodelscrollpane.scrollToComponent(focusbutton);
			}
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
		if (arg0.equals(300)) {
			showModelSourceCode();
		}
		refreshAnnotatableElements();
	}

	@Override
	public boolean isSaved() {
		return canvas.getModelSaved();
	}
		
}