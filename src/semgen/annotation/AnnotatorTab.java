package semgen.annotation;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.GlobalActions;
import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.componentdisplays.AnnotationObjectButton;
import semgen.annotation.componentdisplays.buttontree.AnnotatorButtonTree;
import semgen.annotation.componentdisplays.codewords.CodewordButton;
import semgen.annotation.componentdisplays.submodels.SubmodelButton;
import semgen.annotation.dialog.HumanDefEditor;
import semgen.annotation.dialog.LegacyCodeChooser;
import semgen.annotation.dialog.textminer.TextMinerDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.resource.ComparatorByName;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenScrollPane;
import semgen.resource.uicomponent.SemGenTab;
import semsim.SemSimConstants;
import semsim.model.Importable;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.Submodel;

import java.net.URI;
import java.awt.*;
import java.awt.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.awt.BorderLayout;
import java.io.IOException;

public class AnnotatorTab extends SemGenTab implements ActionListener, MouseListener,
		KeyListener, Observer {

	private static final long serialVersionUID = -5360722647774877228L;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	public URI fileURI;
	
	private JSplitPane splitpane;
	private JSplitPane eastsplitpane;
	public JSplitPane westsplitpane;
	public SemGenScrollPane submodelscrollpane;
	public SemGenScrollPane codewordscrollpane;
	public SemGenScrollPane dialogscrollpane = new SemGenScrollPane();
	private SemGenScrollPane treeviewscrollpane = new SemGenScrollPane();
	
	public AnnotatorButtonTree tree;

	public AnnotationPanel anndialog;
	public AnnotationObjectButton focusbutton;
	public Hashtable<String, CodewordButton> codewordbuttontable = new Hashtable<String, CodewordButton>();
	public Hashtable<String, SubmodelButton> submodelbuttontable = new Hashtable<String, SubmodelButton>();

	public JPanel codewordpanel = new JPanel();
	public JPanel submodelpanel = new JPanel();
	public AnnotatorTabCodePanel codearea = new AnnotatorTabCodePanel();
	public JButton addsubmodelbutton = new JButton(SemGenIcon.plusicon);
	public JButton removesubmodelbutton = new JButton(SemGenIcon.minusicon);
	AnnotatorToolBar toolbar;
	
	public int numcomponents;
	public static int initwidth;
	public static int initheight;
	public String ontspref;

	public TextMinerDialog tmd;
	
	AnnotatorWorkbench workbench;

	public SemSimModel semsimmodel;

	public AnnotatorTab(File srcfile, SemGenSettings sets, GlobalActions gacts) {
		super(srcfile.getName(), SemGenIcon.annotatoricon, "Annotating " + srcfile.getName(), sets, gacts);
		sourcefile = srcfile;
		
		workbench = new AnnotatorWorkbench(sourcefile, settings.doAutoAnnotate());		
		semsimmodel = workbench.getSemSimModel();
		toolbar = new AnnotatorToolBar(this, workbench, settings );
		
		fileURI = srcfile.toURI();
		initwidth = settings.getAppWidth();
		initheight = settings.getAppHeight();
		setOpaque(false);
		setLayout(new BorderLayout());

		codewordpanel.setBackground(Color.white);
		codewordpanel.setLayout(new BoxLayout(codewordpanel, BoxLayout.Y_AXIS));
		
		submodelpanel.setBackground(Color.white);
		submodelpanel.setLayout(new BoxLayout(submodelpanel, BoxLayout.Y_AXIS));
		submodelscrollpane = new SemGenScrollPane(submodelpanel);
		addsubmodelbutton.addActionListener(this);
		removesubmodelbutton.addActionListener(this);
		
		SemGenScrollPane legacycodescrollpane = new SemGenScrollPane();
		legacycodescrollpane.getViewport().add(codearea);
		
		dialogscrollpane.setBackground(SemGenSettings.lightblue);
		dialogscrollpane.getViewport().setBackground(SemGenSettings.lightblue);
		
		codewordscrollpane = new SemGenScrollPane(codewordpanel);
		InputMap im = codewordscrollpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// Override up and down key functions so user can use arrows to move between codewords
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");

		westsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codewordscrollpane, submodelscrollpane);
		westsplitpane.setOneTouchExpandable(true);
		
		eastsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dialogscrollpane, legacycodescrollpane);
		eastsplitpane.setOneTouchExpandable(true);

		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westsplitpane, eastsplitpane);
		splitpane.setOneTouchExpandable(true);
	}

	public void addObservertoWorkbench(Observer obs) {
		workbench.addObserver(obs);
	}
	
	// --------------------------------//
	// METHODS
	// --------------------------------//
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == addsubmodelbutton){
			try {addNewSubmodelButton();
			} catch (OWLException e1) {e1.printStackTrace();}
		}
		if(o == removesubmodelbutton){
			if(focusbutton instanceof SubmodelButton){
				int choice = JOptionPane.showConfirmDialog(this, 
						"Are you sure you want to remove component " + focusbutton.namelabel.getText() + "?", "Confirm removal", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION){
					try {
						removeSubmodel((SubmodelButton) focusbutton);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	public SemSimModel NewAnnotatorAction(){
		SemGen.logfilewriter.println("Started new annotater");
		
		try {
			codearea.setCodeView(semsimmodel.getLegacyCodeLocation());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		SemGenProgressBar progframe = new SemGenProgressBar("Sorting codewords...", true);
		
		refreshAnnotatableElements();
		showAnnotaterPanes();
		
		codewordscrollpane.scrollToTop();
		submodelscrollpane.scrollToTop();
		
		AnnotationObjectButton initialaob = null; 
		// If tree view is selected, select first child of the root node
		if(settings.useTreeView()){
			TreeNode firstChild = ((DefaultMutableTreeNode)tree.getModel().getRoot()).getChildAt(0);
			tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode)firstChild).getPath()));
		}
		
		// If we are hiding the imported codewords, select the first one that is editable
		else if(!settings.useTreeView()){
			for(int i=0; i<codewordpanel.getComponents().length; i++){
				if(codewordpanel.getComponent(i) instanceof CodewordButton){
					CodewordButton tempaob = (CodewordButton)codewordpanel.getComponent(i);
					if(getCodewordButtonVisibility(tempaob)){
						initialaob = tempaob;
						break;
					}
				}
			}
		}
				
		if(initialaob!=null)
			try {
				annotationObjectAction(initialaob);
			} catch (BadLocationException | IOException e) {
				e.printStackTrace();
			} 
		if (progframe!=null) progframe.dispose();
		return semsimmodel;
	}

	public void addPanelTitle(String type, int totalcount, int displayedcount, SemGenScrollPane scrollpane, String zerocountmsg) {
		scrollpane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), 
				type + "(" + totalcount + " total, " + displayedcount + " editable)", 
				TitledBorder.CENTER, TitledBorder.TOP, SemGenFont.defaultBold(2)));
		if (totalcount == 0 && type.equals("Codeword ")) {
			scrollpane.getViewport().add(new JLabel(zerocountmsg));
		} 
	}

	public AnnotationPanel annotationObjectAction(AnnotationObjectButton aob) throws BadLocationException, IOException {
		if(focusbutton!=null){
			focusbutton.setBackground(Color.white);
		}
		aob.setBackground(SemGenSettings.lightblue);
		focusbutton = aob;

		dialogscrollpane.getViewport().removeAll();
		anndialog = new AnnotationPanel(this, settings, aob);
		dialogscrollpane.getViewport().add(anndialog);
		
		// Highlight occurrences of codeword in legacy code
		
		codearea.setCodeword(getLookupNameForAnnotationObjectButton(aob));
		codearea.HighlightOccurances(true);

		dialogscrollpane.scrollToTop();

		return anndialog;
	}
	
	// Refresh the display of codewords and submodels based on the view options selected in the Annotate menu
	public void refreshAnnotatableElements(){
		int divLoc = splitpane.getDividerLocation();
		if(divLoc==-1)
			divLoc = (int)(settings.getAppWidth())/3;
		
		// If the "Tree view" menu item is selected...
		if(settings.useTreeView()){
			toolbar.enableSort(false);
			updateCodewordButtonTable();
			updateSubmodelButtonTable();
			splitpane.setTopComponent(treeviewscrollpane);
			tree = new AnnotatorButtonTree(this, settings, new DefaultMutableTreeNode(semsimmodel));
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
			toolbar.enableSort(true);
			splitpane.setTopComponent(westsplitpane);
			AlphabetizeAndSetCodewords();
			AlphabetizeAndSetSubmodels();
			if(focusbutton!=null){
				if(!settings.useTreeView()){
					if(focusbutton instanceof CodewordButton)
						codewordscrollpane.scrollToComponent(focusbutton);
					else if(focusbutton instanceof SubmodelButton)
						submodelscrollpane.scrollToComponent(focusbutton);
					codewordpanel.validate();
					codewordpanel.repaint();
				}
			}
		}

		splitpane.setDividerLocation(divLoc);
	}
	
	public void AlphabetizeAndSetCodewords(){
		codewordpanel.removeAll();
		int numcdwdsshowing = updateCodewordButtonTable();
		ArrayList<AnnotationObjectButton> aoblist = getAnnotationObjectButtonArray(codewordbuttontable);
		
		addPanelTitle("Codewords ", aoblist.size(), numcdwdsshowing, codewordscrollpane, "No codewords or dependencies found");
		if(settings.organizeByCompositeCompleteness()) setCodewordsbyAnnCompleteness(aoblist);
		if(settings.organizeByPropertyType()) setCodewordsbyMarker(aoblist);
		
		for (AnnotationObjectButton aob : aoblist) {
			aob.refreshAllCodes();

			CodewordButton cb = (CodewordButton) aob;
			
			// Set visibility
			aob.setVisible(getCodewordButtonVisibility(cb));
			
			// Set name
			if(!settings.useTreeView()) cb.namelabel.setText(cb.ds.getName());
			codewordpanel.add(aob);
		}

		codewordpanel.add(Box.createGlue());
	}

	public void setCodewordsbyMarker(ArrayList<AnnotationObjectButton> aoblist){
		ArrayList<CodewordButton> entset = new ArrayList<CodewordButton>();
		ArrayList<CodewordButton> procset = new ArrayList<CodewordButton>();
		ArrayList<CodewordButton> depset = new ArrayList<CodewordButton>();

		for (AnnotationObjectButton aob : aoblist) {
			CodewordButton cb = (CodewordButton)aob;
			DataStructure ds = cb.ds;

			int type = ds.getPropertyType(SemGen.semsimlib);
			
			// Group according to physical property type
			if(type==SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY)
				entset.add(cb);
			else if(type==SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS)
				procset.add(cb);
			else depset.add(cb);
		}
		
		aoblist.clear();
		aoblist.addAll(entset);
		aoblist.addAll(procset);
		aoblist.addAll(depset);
	}
	
	public void setCodewordsbyAnnCompleteness(ArrayList<AnnotationObjectButton> aoblist) {
		ArrayList<CodewordButton> nonelist = new ArrayList<CodewordButton>();
		ArrayList<CodewordButton> physproplist = new ArrayList<CodewordButton>();
		ArrayList<CodewordButton> physentlist = new ArrayList<CodewordButton>();
		ArrayList<CodewordButton> alllist = new ArrayList<CodewordButton>();
		
		for (AnnotationObjectButton aob : aoblist) {
			CodewordButton cb = (CodewordButton)aob;

			switch (cb.getCompositeAnnotationCodeForButton()) {
			
			// Group according to physical property type
			case noAnnotations:
				nonelist.add(cb);
				break;
			case hasPhysProp:
				physproplist.add(cb);
				break;
			case hasPhysEnt:
				physentlist.add(cb);
				break;
			default:
				alllist.add(cb);
				break;
			}
		}
		
		aoblist.clear();
		aoblist.addAll(nonelist);
		aoblist.addAll(physproplist);
		aoblist.addAll(physentlist);
		aoblist.addAll(alllist);
	}
	
	public void AlphabetizeAndSetSubmodels(){
		int numsubshowing = updateSubmodelButtonTable();
		submodelpanel.removeAll();
		numcomponents = semsimmodel.getSubmodels().size();
		
		// First put all the codeword buttons in an array so they can be sorted
		addPanelTitle("Sub-models ", numcomponents, numsubshowing, submodelscrollpane, "No sub-models found");
		JPanel componentaddremovepanel = new JPanel();
		componentaddremovepanel.setOpaque(false);
		componentaddremovepanel.setLayout(new BoxLayout(componentaddremovepanel, BoxLayout.X_AXIS));
		componentaddremovepanel.add(addsubmodelbutton);
		componentaddremovepanel.add(removesubmodelbutton);
		submodelpanel.add(componentaddremovepanel);
		
		ArrayList<AnnotationObjectButton> aobarray = getAnnotationObjectButtonArray(submodelbuttontable);
		
		// Sort the array alphabetically using the custom Comparator
		for (AnnotationObjectButton aob : aobarray) {
			aob.setVisible(getSubmodelButtonVisibility((SubmodelButton)aob));
			submodelpanel.add(aob);
		}
		submodelpanel.add(Box.createGlue());
	}

	public ArrayList<AnnotationObjectButton> getAnnotationObjectButtonArray(Hashtable<String, ? extends AnnotationObjectButton> table){
		ArrayList<AnnotationObjectButton> bset = new ArrayList<AnnotationObjectButton>();
		for (String key : table.keySet())
			bset.add(table.get(key));
		Collections.sort(bset, new ComparatorByName());
		return bset;
	}
	
	public int updateCodewordButtonTable(){
		int numdisplayed = semsimmodel.getDataStructures().size();
		// Associate codeword names with their buttons
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(!codewordbuttontable.containsKey(ds.getName())){
				boolean hashumreadtext = ds.getDescription()!=null;
				CodewordButton cbutton = new CodewordButton(this, settings, ds, false, "", ds.hasRefersToAnnotation(), hashumreadtext, !ds.isImportedViaSubmodel());
				if(ds.isImportedViaSubmodel() && !settings.showImports()){
					cbutton.setVisible(false);
					numdisplayed--;
				}
				cbutton.addMouseListener(this);
				codewordbuttontable.put(ds.getName(), cbutton);
			}
			else if(!codewordbuttontable.get(ds.getName()).editable){
				numdisplayed--;
			}
		}
		return numdisplayed;
	}
	
	public int updateSubmodelButtonTable(){
		int numdisplayed = semsimmodel.getSubmodels().size();
		// Associate submodel names with their buttons
		for(Submodel sub : semsimmodel.getSubmodels()){
			if(!submodelbuttontable.containsKey(sub.getName())){
				boolean editable = true;
				if(sub instanceof FunctionalSubmodel){
					editable = ((FunctionalSubmodel)sub).getParentImport()==null;
					if(!editable) numdisplayed--;
				}
				SubmodelButton sb = new SubmodelButton(this, settings, sub,
					false, null, sub.hasRefersToAnnotation(), (sub.getDescription()!=null), editable);
				submodelbuttontable.put(sub.getName(), sb);
			}
			else if(!submodelbuttontable.get(sub.getName()).editable){
				numdisplayed--;
			}
		}
		return numdisplayed;
	}
	
	public void showSelectAnnotationObjectMessage(){
		dialogscrollpane.getViewport().removeAll();
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Select a codeword or submodel to view annotations");
		label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		panel.add(label, BorderLayout.CENTER);
		dialogscrollpane.getViewport().add(panel);
		dialogscrollpane.scrollToTop();
		if(focusbutton!=null){
			focusbutton.setBackground(Color.white);
		}
		codearea.removeAllHighlights();
	}

	public void showAnnotaterPanes(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
			  add(toolbar, BorderLayout.NORTH);
			  add(splitpane, BorderLayout.CENTER);
			  setVisible(true);
			  eastsplitpane.setDividerLocation((int)(initheight-150)/2);
			  westsplitpane.setDividerLocation((int)(initheight-150)/2);
		   }
		});
	}

	public void addNewSubmodelButton() throws OWLException {
		String newname = JOptionPane.showInputDialog(this,"Enter a name for the new sub-model");
		if(newname !=null && !newname.equals("")){
			Submodel newsub = semsimmodel.addSubmodel(new Submodel(newname));
			setModelSaved(false);
			AlphabetizeAndSetSubmodels();
			submodelscrollpane.validate();
			submodelscrollpane.repaint();
			
			// Find the component we just added and select it
			for(Component c : submodelpanel.getComponents()){
				if(c instanceof SubmodelButton){
					SubmodelButton cb = (SubmodelButton)c;
					if(cb.namelabel.getText().equals(newsub.getName())){
						changeButtonFocus(cb, null);
					}
				}
			}
		}
	}
	
	public void removeSubmodel(SubmodelButton cb) throws IOException{
		semsimmodel.removeSubmodel(cb.sub);
		setModelSaved(false);
		try {
			AlphabetizeAndSetSubmodels();
			if(submodelpanel.getComponent(1) instanceof AnnotationObjectButton) // Account for +/- panel
				annotationObjectAction((AnnotationObjectButton) submodelpanel.getComponent(1));
			else if(codewordpanel.getComponent(0) instanceof CodewordButton)
				annotationObjectAction((AnnotationObjectButton) codewordpanel.getComponent(0));
			else
				dialogscrollpane.getViewport().add(new JLabel("No codewords or sub-models to annotate"));
		} 
		catch (BadLocationException e) {
			e.printStackTrace();}
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

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() instanceof AnnotationObjectButton) {
			try {
				annotationObjectAction((AnnotationObjectButton) e.getSource());
			} catch (BadLocationException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void keyPressed(KeyEvent e) {
		int id = e.getKeyCode();
		JPanel panel = codewordpanel;
		if(focusbutton instanceof SubmodelButton){
			panel = submodelpanel;
			}
		
		// Up arrow key
		if (id == 38) {
			int index = -1;
			for (int x = 0; x < panel.getComponentCount(); x++) {
				Component c = panel.getComponent(x);
				if (c == focusbutton) {
					index = x;
					break;
				}
			}
			if(index!=-1){
				for(int y=(index-1); y>=0; y--){
					if(panel.getComponent(y).isVisible() && panel.getComponent(y) instanceof AnnotationObjectButton){
						changeButtonFocus((AnnotationObjectButton) panel.getComponent(y), null);
						break;
					}
				}
			}
		}
		// Down arrow key
		if (id == 40) {
			int index = -1;
			for (int x = 0; x < panel.getComponentCount(); x++) {
				Component c = panel.getComponent(x);
				if (c == focusbutton) {
					index = x;
					break;
				}
			}
			if(index!=-1){
				for(int y=(index+1); y<panel.getComponentCount(); y++){
					if(panel.getComponent(y).isVisible() && panel.getComponent(y) instanceof AnnotationObjectButton){
						changeButtonFocus((AnnotationObjectButton) panel.getComponent(y), null);
						break;
					}
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public void changeButtonFocus(AnnotationObjectButton aob, JLabel whichann) {
		SemGenScrollPane pane = codewordscrollpane;
		if(aob instanceof SubmodelButton){
			pane = submodelscrollpane;
		}
		pane.scrollToComponent(aob);
		try {
			annotationObjectAction(aob);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
		
		focusbutton = aob;
		if (whichann == aob.humdeflabel) {
			new HumanDefEditor(aob.ssc, anndialog, true);
		}
		if(whichann == aob.singularannlabel){
			anndialog.showSingularAnnotationEditor();
		}
	}

	public boolean getCodewordButtonVisibility(CodewordButton cb){
		return !(cb.ds.isImportedViaSubmodel() && !settings.showImports());
	}
	
	public boolean getSubmodelButtonVisibility(SubmodelButton sb){
		if(sb.sub instanceof Importable){
			if(((Importable)sb.sub).getParentImport()!=null && !settings.showImports()){
				return false;
			}
		}
		return true;
	}
	
	public String getLookupNameForAnnotationObjectButton(AnnotationObjectButton aob){
		return (aob.ssc instanceof MappableVariable) ? aob.ssc.getName().substring(aob.ssc.getName().lastIndexOf(".")+1) : aob.ssc.getName();
	}
	
	public void updateTreeNode(){
		if(tree!=null && settings.useTreeView()){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) (tree.getSelectionPath().getLastPathComponent()); 
			tree.update(node, tree);
		}
	}
	
	public boolean checkFile(URI uri) {
		return (uri.toString().equals(fileURI.toString()));
	}
	
	public void changeLegacyLocation() {
		LegacyCodeChooser lcc = new LegacyCodeChooser(this);
		try {
			if (lcc.wasChanged()) 
				codearea.setCodeView(semsimmodel.getLegacyCodeLocation());
			} catch (IOException e1) {
				e1.printStackTrace();
		}
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
		workbench.saveModelAs();
		setTabName(workbench.getCurrentModelName());
		setToolTipText("Annotating " + workbench.getCurrentModelName());
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		refreshAnnotatableElements();
	}
}