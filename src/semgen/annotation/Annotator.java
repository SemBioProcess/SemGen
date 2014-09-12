package semgen.annotation;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.ComparatorByName;
import semgen.ProgressFrame;
import semgen.SemGenGUI;
import semgen.SemGenScrollPane;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.Importable;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.Submodel;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.Hashtable;
import java.util.HashSet;
import java.io.PrintWriter;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Comparator;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Annotator extends JPanel implements ActionListener, MouseListener,
		KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5360722647774877228L;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	public String base;
	public URI tempURI;
	public URI fileURI;
	public IRI tempIRI;
	public Set<String> recententitiesset = new HashSet<String>();
	public Set<String> recentdependenciesset = new HashSet<String>();
	public String[] entityKBlist = { "-none selected-" };
	public String[] allKBlist = { "-none selected-" };
	public String[] propertyKBlist = { "-none selected-" };
	public String[] depKBlist = { "-none selected-" };
	public File ontfile;
	public PrintWriter localontwriter;

	public SingularAnnotationEditor noncompeditor;
	public HumanDefEditor humdefeditor;

	private JSplitPane splitpane;
	private JSplitPane eastsplitpane;
	public JSplitPane westsplitpane;
	public SemGenScrollPane submodelscrollpane;
	public SemGenScrollPane legacycodescrollpane;
	public SemGenScrollPane codewordscrollpane;
	public SemGenScrollPane dialogscrollpane;
	public SemGenScrollPane treeviewscrollpane;
	
	public AnnotatorButtonTree tree;

	public AnnotationDialog anndialog;
	public AnnotationObjectButton focusbutton;
	public Hashtable<String, CodewordButton> codewordbuttontable = new Hashtable<String, CodewordButton>();
	public Hashtable<String, SubmodelButton> submodelbuttontable = new Hashtable<String, SubmodelButton>();
	private JButton closebutton;

	public JPanel codewordpanel;
	public JPanel submodelpanel;
	public JTextArea codearea;
	private JPanel genmodinfo;
	private JLabel modname;
	private JTextField modnamefield;
	private JLabel editmeta;
	private JButton editmetabutton;
	public JTextPane annotationpane;
	private Scanner importfilescanner;
	private String nextline;
	public JButton addsubmodelbutton = new JButton(SemGenGUI.plusicon);
	public JButton removesubmodelbutton = new JButton(SemGenGUI.minusicon);
	public CustomPhysicalComponentEditor customtermeditor;

	public Highlighter hilit;
	public Highlighter.HighlightPainter allpainter;
	public Highlighter.HighlightPainter onepainter;

	public SimpleAttributeSet attrs;
	//public int numcdwds;
	public int numcomponents;
	public static int initwidth = 900;
	public static int initheight = 700;
	public static int leftsidewidth = 275;
	public float percentProps;
	public float numprops = 0;
	public int compositeannpref = 0;
	public int depannpref = 0;
	public String ontspref;
	public JButton extractorbutton;
	public JButton coderbutton;
	public int numID;
	public JOptionPane optionpane = new JOptionPane();
	public TextMinerDialog tmd;
	public ProgressFrame progframe;
	public ArrayList<Integer> indexesOfHighlightedTerms;
	public int currentindexforcaret;
	public int lastSavedAs = -1;
	
	private boolean modelsaved = false;
	
	public SemSimModel semsimmodel;

	public Annotator(File sourcefile, URI temploc, URI fileloc, String[] entityKBs, String[] propKBs, String[] depKBs,
			String[] allKBs) {
		
		this.sourcefile = sourcefile;
		ontfile = new File("cfg/allontologies.txt");

		tempURI = temploc;
		tempIRI = IRI.create(tempURI);
		fileURI = fileloc;
		entityKBlist = entityKBs;
		propertyKBlist = propKBs;
		depKBlist = depKBs;
		allKBlist = allKBs;

		setOpaque(false);
		setLayout(new BorderLayout());

		// Create the scroll pane for the imported model code
		codearea = new JTextArea();
		codearea.setEditable(false);
		codearea.setBackground(new Color(250, 250, 227));
		codearea.setText(null);
		codearea.setMargin(new Insets(5, 15, 5, 5));
		codearea.setForeground(Color.black);
		codearea.setFont(new Font("Monospaced", Font.PLAIN, SemGenGUI.defaultfontsize));

		hilit = new DefaultHighlighter();
		onepainter = new DefaultHighlighter.DefaultHighlightPainter(SemGenGUI.lightblue);
		allpainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
		
		codearea.setHighlighter(hilit);

		annotationpane = new JTextPane();

		codewordpanel = new JPanel();
		codewordpanel.setBackground(Color.white);
		codewordpanel.setLayout(new BoxLayout(codewordpanel, BoxLayout.Y_AXIS));
		
		submodelpanel = new JPanel();
		submodelpanel.setBackground(Color.white);
		submodelpanel.setLayout(new BoxLayout(submodelpanel, BoxLayout.Y_AXIS));
		submodelscrollpane = new SemGenScrollPane(submodelpanel);
		addsubmodelbutton.addActionListener(this);
		removesubmodelbutton.addActionListener(this);
		
		legacycodescrollpane = new SemGenScrollPane();
		
		legacycodescrollpane.getViewport().add(codearea);
		//simcodescrollpane.setPreferredSize(new Dimension(SemGen.initwidth, Math
		//		.round(SemGen.initheight / 26) * 10));
		dialogscrollpane = new SemGenScrollPane();
		dialogscrollpane.setBackground(SemGenGUI.lightblue);
		dialogscrollpane.getViewport().setBackground(SemGenGUI.lightblue);
		
		codewordscrollpane = new SemGenScrollPane(codewordpanel);
		//codewordscrollpane.setPreferredSize(new Dimension(Math.round(SemGen.initwidth / 33) * 10, Math.round(SemGen.initheight / 80) * 10)); // Math.round(SemGen.initwidth/3),
														// Math.round(SemGen.initheight/25)/10));
		InputMap im = codewordscrollpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// Override up and down key functions so user can use arrows to move between codewords
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
		
		treeviewscrollpane = new SemGenScrollPane();
		
		westsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codewordscrollpane, submodelscrollpane);
		westsplitpane.setOneTouchExpandable(true);
		
		eastsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dialogscrollpane, legacycodescrollpane);
		eastsplitpane.setOneTouchExpandable(true);

		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westsplitpane, eastsplitpane);
		splitpane.setOneTouchExpandable(true);

		// Create the general model information panel
		modname = new JLabel("Model name");
		modname.setFont(new Font("SansSerif", Font.ITALIC, 12));
		modnamefield = new JTextField(10);
		modnamefield.setFont(new Font("SansSerif", Font.PLAIN, 12));
		modnamefield.setForeground(Color.blue);

		editmeta = new JLabel("Curation data:");
		editmeta.setFont(new Font("SansSerif", Font.ITALIC, 12));
		editmetabutton = new JButton("Curation data");
		// editmetabutton.setPreferredSize(new Dimension(100, 40));
		editmetabutton.setFont(new Font("SansSerif", Font.PLAIN, 11));
		editmetabutton.setOpaque(false);
		editmetabutton.setEnabled(false);
		editmetabutton.addActionListener(this);
		editmetabutton.setVisible(false);

		extractorbutton = new JButton();
		extractorbutton.setToolTipText("Open this model in Extractor");
		extractorbutton.setIcon(SemGenGUI.extractoricon);
		extractorbutton.setSize(new Dimension(10, 10));
		extractorbutton.setRolloverEnabled(true);
		extractorbutton.addActionListener(this);
		extractorbutton.setPreferredSize(new Dimension(20, 20));
		extractorbutton.setAlignmentY(JButton.TOP_ALIGNMENT);

		coderbutton = new JButton();
		coderbutton.setToolTipText("Encode this model for simulation");
		coderbutton.setIcon(SemGenGUI.codericon);
		coderbutton.setRolloverEnabled(true);
		coderbutton.addActionListener(this);
		coderbutton.setAlignmentY(JButton.TOP_ALIGNMENT);
		coderbutton.setPreferredSize(new Dimension(20, 20));

		JPanel toolpanel = new JPanel();
		toolpanel.add(extractorbutton);
		toolpanel.add(coderbutton);
		toolpanel.setAlignmentY(JPanel.TOP_ALIGNMENT);

		closebutton = new JButton("Close tab");
		closebutton.setContentAreaFilled(false);
		closebutton.setForeground(Color.blue);
		closebutton.setFont(new Font("SansSerif", Font.ITALIC, 11));
		closebutton.setBorderPainted(false);
		closebutton.setOpaque(false);
		closebutton.addActionListener(this);
		closebutton.addMouseListener(this);
		closebutton.setRolloverEnabled(true);
		closebutton.setAlignmentY(JButton.TOP_ALIGNMENT);

		genmodinfo = new JPanel(new BorderLayout());
		// genmodinfo.setPreferredSize(new Dimension(initwidth, 32));
		genmodinfo.setOpaque(true);
		genmodinfo.add(toolpanel, BorderLayout.WEST);
		// genmodinfo.add(editmetabutton, BorderLayout.WEST);
		// genmodinfo.add(graphs, BorderLayout.CENTER);
		//genmodinfo.add(closebutton, BorderLayout.EAST);

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
				int choice = JOptionPane.showConfirmDialog(SemGenGUI.desktop, 
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
		if (o == editmetabutton) {MetadataAction();}
		if (o == closebutton) {closeAction();}
		if (o == extractorbutton) {
			try {
				Boolean open = true;
				if (!getModelSaved()) {
					int savefilechoice = JOptionPane.showConfirmDialog(SemGenGUI.desktop,
							"Save changes before opening Extractor?",
							"There are unsaved changes",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if(savefilechoice == JOptionPane.CANCEL_OPTION){open = false;}
					else {
						if (savefilechoice == JOptionPane.YES_OPTION) {
							open = SemGenGUI.SaveAction(this, lastSavedAs);
						}
					}
				}
				if(open){
					SemGenGUI.NewExtractorTask task = new SemGenGUI.NewExtractorTask(sourcefile);
					task.execute();
				}
			} catch (Exception e1) {e1.printStackTrace();}
		}

		if (o == coderbutton) {
			String filenamesuggestion = null;
			if(sourcefile!=null) filenamesuggestion = sourcefile.getName().substring(0, sourcefile.getName().lastIndexOf("."));
			try {
				if (!getModelSaved()) {
					int savefilechoice = JOptionPane.showConfirmDialog(SemGenGUI.desktop,
							"Save changes before encoding model?",
							"There are unsaved changes",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (savefilechoice == JOptionPane.YES_OPTION) {
						manager.saveOntology(semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), tempIRI);
						if(SemGenGUI.SaveAction(this, lastSavedAs)) SemGenGUI.startEncoding(semsimmodel, filenamesuggestion);
					}
					else if(savefilechoice == JOptionPane.NO_OPTION)
						SemGenGUI.startEncoding(semsimmodel, filenamesuggestion);
				}
				else SemGenGUI.startEncoding(semsimmodel, filenamesuggestion); 
			} 
			catch (OWLException e3) {
				e3.printStackTrace();
			} 
		}
	}

	public void closeAction() {
		try {
			SemGenGUI.closeTabAction(this);
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
	}

	
	
	public SemSimModel NewAnnotatorAction(){
		
		SemGenGUI.logfilewriter.println("Started new annotater");
		annotationpane.setText(null);
		annotationpane.setCaretPosition(0);
		
		try {
			setCodeViewer(semsimmodel.getLegacyCodeLocation(), true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		SemGenGUI.progframe.updateMessage("Sorting codewords...");
		
		refreshAnnotatableElements();
		showAnnotaterPanes(this);

		if(SemGenGUI.desktop.getComponentCount()>0){
			SemGenGUI.desktop.setSelectedComponent(SemGenGUI.desktop.getComponentAt(SemGenGUI.numtabs - 1));
		}
		
		SemGenGUI.scrollToTop(codewordscrollpane);
		SemGenGUI.scrollToTop(submodelscrollpane);
		
		AnnotationObjectButton initialaob = null; 
		// If tree view is selected, select first child of the root node
		if(SemGenGUI.annotateitemtreeview.isSelected()){
			TreeNode firstChild = ((DefaultMutableTreeNode)tree.getModel().getRoot()).getChildAt(0);
			tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode)firstChild).getPath()));
		}
		
		// If we are hiding the imported codewords, select the first one that is editable
		else if(!SemGenGUI.annotateitemtreeview.isSelected()){
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
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return semsimmodel;
	}
	

	
	public void addPanelTitle(String type, int totalcount, int displayedcount, SemGenScrollPane scrollpane, String zerocountmsg) {
		scrollpane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), 
				type + "(" + totalcount + " total, " + displayedcount + " editable)", 
				TitledBorder.CENTER, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, SemGenGUI.defaultfontsize + 2)));
		if (totalcount == 0 && type.equals("Codeword ")) {
			scrollpane.getViewport().add(new JLabel(zerocountmsg));
		} 
//		else {
//			// Put label into panel
//			JLabel label = new JLabel(type +  "(" + count + ")");
//			label.setFont(new Font("SansSerif", Font.BOLD, SemGenGUI.defaultfontsize + 1));
//			label.setAlignmentX(Component.CENTER_ALIGNMENT);
//			label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
//			panel.add(label);
//		}
	}

	public AnnotationDialog annotationObjectAction(AnnotationObjectButton aob) throws BadLocationException, IOException {
		if(focusbutton!=null){
			focusbutton.setBackground(Color.white);
		}
		aob.setBackground(SemGenGUI.lightblue);
		focusbutton = aob;

		dialogscrollpane.getViewport().removeAll();
		anndialog = new AnnotationDialog(this, aob, fileURI, entityKBlist, propertyKBlist, depKBlist);
		dialogscrollpane.getViewport().add(anndialog);
		
		// Highlight occurrences of codeword in legacy code
		hilit.removeAllHighlights();
		String name = getLookupNameForAnnotationObjectButton(aob);
		getIndexesForCodewordOccurrences(name, false);
		
		if(indexesOfHighlightedTerms.size()>0){
			hilit.addHighlight(indexesOfHighlightedTerms.get(0).intValue()+1, 
				(indexesOfHighlightedTerms.get(0).intValue()+1+name.length()), onepainter);
			
			highlightAllOccurrencesOfString(name);
			codearea.setCaretPosition(indexesOfHighlightedTerms.get(0));
		}
		currentindexforcaret = 0;
		SemGenGUI.scrollToTop(dialogscrollpane);

		return anndialog;
	}
	
	// Refresh the display of codewords and submodels based on the view options selected in the Annotate menu
	public void refreshAnnotatableElements(){
		
		int divLoc = splitpane.getDividerLocation();
		if(divLoc==-1)
			divLoc = (int)(SemGenGUI.initwidth)/3;
		
		// If the "Tree view" menu item is selected...
		if(SemGenGUI.annotateitemtreeview.isSelected()){
			updateCodewordButtonTable();
			updateSubmodelButtonTable();
			splitpane.setTopComponent(treeviewscrollpane);
			tree = new AnnotatorButtonTree(this, new DefaultMutableTreeNode(semsimmodel));
			treeviewscrollpane.getViewport().removeAll();
			treeviewscrollpane.getViewport().add(tree);
			
			// If focusbutton in Annotator associated with focusnode here, set the selected node
			if(tree.focusnode!=null){
				tree.setSelectionPath(new TreePath(tree.focusnode.getPath()));
				tree.scrollPathToVisible(new TreePath(tree.focusnode.getPath()));
			}
			SemGenGUI.scrollToLeft(treeviewscrollpane);
		}
		else{
			splitpane.setTopComponent(westsplitpane);
			if(SemGenGUI.annotateitemsortbytype.isSelected()) AlphabetizeAndSetCodewordsbyMarker();
			else AlphabetizeAndSetCodewords();
			AlphabetizeAndSetSubmodels();
			if(focusbutton!=null){
				if(!SemGenGUI.annotateitemtreeview.isSelected()){
					if(focusbutton instanceof CodewordButton)
						SemGenGUI.scrollToComponent(codewordpanel, focusbutton);
					else if(focusbutton instanceof SubmodelButton)
						SemGenGUI.scrollToComponent(submodelpanel, focusbutton);
				}
			}
		}
		splitpane.setDividerLocation(divLoc);
	}

	
	public void AlphabetizeAndSetCodewords(){
		
		codewordpanel.removeAll();
		int numcdwdsshowing = updateCodewordButtonTable();
		AnnotationObjectButton[] aobarray = getAnnotationObjectButtonArray(codewordbuttontable);
		
		// Sort the array alphabetically using the custom Comparator
		Comparator<Component> byVarName = new ComparatorByName();
		Arrays.sort(aobarray, byVarName);
		addPanelTitle("Codewords ", aobarray.length, numcdwdsshowing, codewordscrollpane, "No codewords or dependencies found");
		
		for (int x = 0; x < aobarray.length; x++) {
			aobarray[x].refreshAllCodes();
			codewordpanel.add(aobarray[x]);
			
			CodewordButton cb = (CodewordButton) aobarray[x];
			
			// Set visibility
			aobarray[x].setVisible(getCodewordButtonVisibility(cb));
			
			// Set name
			if(!SemGenGUI.annotateitemtreeview.isSelected()) cb.namelabel.setText(cb.ds.getName());
		}
		codewordpanel.add(Box.createGlue());
	}
	
	
	public void AlphabetizeAndSetCodewordsbyMarker(){
		
		// First put all the codeword buttons in an array so they can be sorted
		codewordpanel.removeAll();
		int numcdwdsshowing = updateCodewordButtonTable();
		AnnotationObjectButton[] aobarray = getAnnotationObjectButtonArray(codewordbuttontable);
		
		Set<CodewordButton> entset = new HashSet<CodewordButton>();
		Set<CodewordButton> procset = new HashSet<CodewordButton>();
		Set<CodewordButton> depset = new HashSet<CodewordButton>();

		for (int i = 0; i < aobarray.length; i++) {
			CodewordButton cb = (CodewordButton) aobarray[i];
			DataStructure ds = cb.ds;
			aobarray[i].namelabel.setText(ds.getName());  // Need to do this when switching from tree view to flat list
			aobarray[i].refreshAllCodes();

			int type = SemGenGUI.getPropertyType(ds);
			
			// Group according to physical property type
			if(type==SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY)
				entset.add(cb);
			else if(type==SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS)
				procset.add(cb);
			else depset.add(cb);
			
			// Set visibility
			cb.setVisible(getCodewordButtonVisibility(cb));
			
			// Set name
			if(!SemGenGUI.annotateitemtreeview.isSelected()) cb.namelabel.setText(ds.getName());
		}
		CodewordButton[] entbuttonarray = entset.toArray(new CodewordButton[] {});
		CodewordButton[] procbuttonarray = procset.toArray(new CodewordButton[] {});
		CodewordButton[] depbuttonarray = depset.toArray(new CodewordButton[] {});
		
		// Sort the arrays alphabetically using the custom Comparator
		Comparator<Component> byVarName = new ComparatorByName();
		Arrays.sort(entbuttonarray, byVarName);
		Arrays.sort(procbuttonarray, byVarName);
		Arrays.sort(depbuttonarray, byVarName);
		
		addPanelTitle("Codewords ", (entset.size()+procset.size()+depset.size()), numcdwdsshowing, codewordscrollpane, "No codewords or dependencies found");
		for (int x = 0; x < entbuttonarray.length; x++) {
			codewordpanel.add(entbuttonarray[x]);
		}
		for (int x = 0; x < procbuttonarray.length; x++) {
			codewordpanel.add(procbuttonarray[x]);
		}
		for (int x = 0; x < depbuttonarray.length; x++) {
			codewordpanel.add(depbuttonarray[x]);
		}
		codewordpanel.add(Box.createGlue());
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
		
		AnnotationObjectButton[] aobarray = getAnnotationObjectButtonArray(submodelbuttontable);
		
		// Sort the array alphabetically using the custom Comparator
		Comparator<Component> byVarName = new ComparatorByName();
		Arrays.sort(aobarray, byVarName);
		for (int x = 0; x < aobarray.length; x++) {
			aobarray[x].setVisible(getSubmodelButtonVisibility((SubmodelButton)aobarray[x]));
			submodelpanel.add(aobarray[x]);
		}
		submodelpanel.add(Box.createGlue());
	}
	
	
	public AnnotationObjectButton[] getAnnotationObjectButtonArray(Hashtable<String, ? extends AnnotationObjectButton> table){
		Set<AnnotationObjectButton> bset = new HashSet<AnnotationObjectButton>();
		for (String key : table.keySet())
			bset.add(table.get(key));
		return bset.toArray(new AnnotationObjectButton[]{});
	}
	
	
	public int updateCodewordButtonTable(){
		
		int numdisplayed = semsimmodel.getDataStructures().size();
		// Associate codeword names with their buttons
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(!codewordbuttontable.containsKey(ds.getName())){
				boolean hashumreadtext = ds.getDescription()!=null;
				CodewordButton cbutton = new CodewordButton(this, ds, false, "", ds.hasRefersToAnnotation(), hashumreadtext, false, !ds.isImportedViaSubmodel());
				if(ds.isImportedViaSubmodel() && !SemGenGUI.annotateitemshowimports.isSelected()){
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
				SubmodelButton sb = new SubmodelButton(this, sub,
					false, null, sub.hasRefersToAnnotation(), (sub.getDescription()!=null), false, editable);
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
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel("Select a codeword or submodel to view annotations");
		label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		panel.add(label, BorderLayout.CENTER);
		dialogscrollpane.getViewport().add(panel);
		SemGenGUI.scrollToTop(dialogscrollpane);
		if(focusbutton!=null){
			focusbutton.setBackground(Color.white);
		}
		hilit.removeAllHighlights();
	}
	
	

	public void setCodeViewer(String loc, Boolean startann) throws IOException {
		codearea.setText(null);
		String modelloc = loc; 
		File modelfile = null;
		Boolean cont = true;
		if(loc!=null && !loc.equals("")){
			// If the legacy model code is on the web
			if (loc.startsWith("http://")) {
				SemGenGUI.progframe.updateMessage("Retrieving legacy code...");
				
				Boolean online = true;
				modelfile = new File(loc);
	
				URL url = new URL(loc);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
				httpcon.setReadTimeout(60000);
				httpcon.setRequestMethod("HEAD");
				try {
					httpcon.getResponseCode();
				} catch (Exception e) {e.printStackTrace(); online = false;}
				if (online) {
					SemGenGUI.progframe.bar.setIndeterminate(false);
					SemGenGUI.progframe.bar.setValue(0);
					
					URLConnection urlcon = url.openConnection();
					urlcon.setDoInput(true);
					urlcon.setUseCaches(false);
					urlcon.setReadTimeout(60000);
					// If there's no file at the URL
					if(urlcon.getContentLength()>0){
						BufferedReader d = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
						String s;
						float charsremaining = urlcon.getContentLength();
						float totallength = charsremaining;
						while ((s = d.readLine()) != null) {
							codearea.append(s);
							codearea.append("\n");
							charsremaining = charsremaining - s.length();
							int x = Math.round(100*(totallength-charsremaining)/totallength);
							SemGenGUI.progframe.bar.setValue(x);
						}
						d.close();
						codearea.setCaretPosition(0);
					}
					else cont = false;
				} 
				else cont = false;
				
				SemGenGUI.progframe.bar.setIndeterminate(true);
				SemGenGUI.progframe.bar.setValue(101);
			}
			// Otherwise it's a local file
			else {
				modelfile = new File(loc);
				if (modelfile.exists()) {
					// Read in the model code and append it to the codearea
					// JTextArea in the top pane
					String filename = modelfile.getAbsolutePath();
					importfilescanner = new Scanner(new File(filename));
	
					while (importfilescanner.hasNextLine()) {
						nextline = importfilescanner.nextLine();
						codearea.append(nextline);
						codearea.append("\n");
						codearea.setCaretPosition(0);
					}
				} else cont = false;
			}
		}
		else{ cont = false; modelloc = "<file location not specified>";}

		if (!cont) {
			modelloc = " at " + modelloc;
			codearea.setText("ERROR: Could not access model code"
					+ modelloc
					+ "\n\nIf the file is on the web, make sure you are connected to the internet."
					+ "\nIf the file is local, make sure it exists at the location above."
					+ "\n\nUse the 'Change Associated Legacy Code' menu item to specify this SemSim model's legacy code.");
		}
	}
	
	
	public static void showAnnotaterPanes(final Annotator ann){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
			   ann.add(ann.genmodinfo, BorderLayout.NORTH);
				ann.add(ann.splitpane, BorderLayout.CENTER);
				ann.setVisible(true);
				ann.eastsplitpane.setDividerLocation((int)(SemGenGUI.initheight-150)/2);
				ann.westsplitpane.setDividerLocation((int)(SemGenGUI.initheight-150)/2);
		   }
		});
	}
	
	
	
	public String getCompositeAnnotationCodeForButton(CodewordButton cb){
		if(cb.ds.getPhysicalProperty()!=null){
			if(cb.ds.getPhysicalProperty().hasRefersToAnnotation()){
				if(cb.ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) return "P+X";
				else return "P+_";
			}
			else{
				if(cb.ds.getPhysicalProperty().getPhysicalPropertyOf()!=null) return "_+X";
				else return "_";
			}
		}
		else return "_";
	}
	
	
	public String getNonCompositeAnnotationCodeForButton(AnnotationObjectButton aob) {
		if(aob.ssc instanceof Annotatable){
			if(((Annotatable)aob.ssc).hasRefersToAnnotation()){
				return "S";
			}
			else return "_";
		}
		return "_";
	}
	
	
	private void MetadataAction() {new ModelLevelMetadataEditor(this);}
	
	
	public void addNewSubmodelButton() throws OWLException {
		String newname = JOptionPane.showInputDialog(SemGenGUI.desktop,"Enter a name for the new sub-model");
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
						changeButtonFocus(focusbutton, cb, null);
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
		catch (BadLocationException e) {e.printStackTrace();}
	}
	
	
	public void getIndexesForCodewordOccurrences(String origword, Boolean caseinsensitive) {
		indexesOfHighlightedTerms = new ArrayList<Integer>();
		String word = origword;
		hilit.removeAllHighlights();
		
		Pattern pattern = Pattern.compile("[\\W_]" + Pattern.quote(word) + "[\\W_]");
		if(caseinsensitive){
			pattern = Pattern.compile("[\\W_]" + Pattern.quote(word) + "[\\W_]", Pattern.CASE_INSENSITIVE);
		}
		Boolean match = true;
		Matcher m = pattern.matcher(codearea.getText());
		int nummatches = 0;
		while(match){
			if(m.find()){
				int index = m.start();
				indexesOfHighlightedTerms.add(index);
				nummatches++;
			}
			else{
				match = false;
			}
		}
	}
	
	public ArrayList<Integer> highlightAll(String origword, Highlighter lighter, HighlightPainter pntr, JTextArea area, Boolean forcdwd, Boolean caseinsensitive) {
		ArrayList<Integer> listofindexes = new ArrayList<Integer>();
		String word = origword;
		Pattern pattern = Pattern.compile("[\\W_]" + Pattern.quote(word) + "[\\W_]");
		if(caseinsensitive){
			pattern = Pattern.compile("[\\W_]" + Pattern.quote(word) + "[\\W_]", Pattern.CASE_INSENSITIVE);
		}
		String content = area.getText();
		Boolean match = true;
		Matcher m = pattern.matcher(content);
		int nummatches = 0;
		while(match){
			if(m.find()){
				int index = m.start();
				int end = m.end();
				try{
					lighter.addHighlight(index+1, end-1, pntr);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				listofindexes.add(index);
				nummatches++;
			}
			else{
				match = false;
			}
		}
		if(!forcdwd && listofindexes.size()>0){area.setCaretPosition(0);}
		return listofindexes;
	}
	
	public void findNextStringInText(String string){
		if(!indexesOfHighlightedTerms.isEmpty()){
			hilit.removeAllHighlights();
			if(indexesOfHighlightedTerms.size()-1>currentindexforcaret){
				currentindexforcaret++;
				codearea.setCaretPosition(indexesOfHighlightedTerms.get(currentindexforcaret));
			}
			else if(indexesOfHighlightedTerms.size()-1==currentindexforcaret){
				// Wrap and beep
				Toolkit.getDefaultToolkit().beep();
				codearea.setCaretPosition(indexesOfHighlightedTerms.get(0));
				currentindexforcaret = 0;
			}
			try {
				hilit.addHighlight(codearea.getCaretPosition()+1, codearea.getCaretPosition() + 1 + string.length(), onepainter);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			highlightAllOccurrencesOfString(string);
		}
	}
	
	public void highlightAllOccurrencesOfString(String string){
		if(focusbutton!=null)
			indexesOfHighlightedTerms = highlightAll(string, hilit, allpainter, codearea, true, false);
	}
	
	public boolean getModelSaved(){
		return modelsaved;
	}
	
	public void setModelSaved(boolean val){
		modelsaved = val;
	}

	public void appendOntFile(String name, String path) throws IOException {
//		PrintWriter ontwriter = new PrintWriter(new FileWriter(ontfile));
//		Set<String> keyset = SemGenGUI.RefOntologiesAndURIsTable_all.keySet();
//		String[] valarray = {};
//		for (String str : keyset) {
//			valarray = (String[]) SemGenGUI.RefOntologiesAndURIsTable_all.get(str);
//			ontwriter.println(str + "; " + valarray[0]);
//		}
//		ontwriter.println(name + "; " + path);
//		ontwriter.flush();
//		ontwriter.close();
	}

	public void removeOntFromFile(String name) throws IOException {
//		PrintWriter ontwriter = new PrintWriter(new FileWriter(ontfile));
//		Set<String> keyset = SemGenGUI.RefOntologiesAndURIsTable_all.keySet();
//		String[] valarray = {};
//		for (String str : keyset) {
//			valarray = (String[]) SemGenGUI.RefOntologiesAndURIsTable_all.get(str);
//			if (!str.equals(name)) {
//				ontwriter.println(str + "; " + valarray[0]);
//			}
//		}
//		ontwriter.flush();
//		ontwriter.close();
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
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void keyPressed(KeyEvent e) {
		int id = e.getKeyCode();
		JPanel panel = codewordpanel;
		if(focusbutton instanceof SubmodelButton){panel = submodelpanel;}
		
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
						changeButtonFocus(focusbutton, (AnnotationObjectButton) panel.getComponent(y), null);
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
						changeButtonFocus(focusbutton, (AnnotationObjectButton) panel.getComponent(y), null);
						break;
					}
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public void changeButtonFocus(AnnotationObjectButton thebutton, AnnotationObjectButton aob, JLabel whichann) {
		JPanel panel = codewordpanel;
		if(aob instanceof SubmodelButton){
			panel = submodelpanel;
		}
		SemGenGUI.scrollToComponent(panel, aob);
		try {
			annotationObjectAction(aob);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		focusbutton = aob;
		if (whichann == aob.humdeflabel) {
			if (humdefeditor != null)
				humdefeditor.dispose();
			humdefeditor = new HumanDefEditor(aob.ssc, anndialog, true);
			humdefeditor.setVisible(true);
		}
		if(whichann == aob.singularannlabel){
			anndialog.showSingularAnnotationEditor();
		}
	}
	
	public boolean getCodewordButtonVisibility(CodewordButton cb){
		if(cb.ds.isImportedViaSubmodel() && !SemGenGUI.annotateitemshowimports.isSelected())
			return false;
		else return true;
	}
	
	public boolean getSubmodelButtonVisibility(SubmodelButton sb){
		if(sb.sub instanceof Importable){
			if(((Importable)sb.sub).getParentImport()!=null && !SemGenGUI.annotateitemshowimports.isSelected()){
				return false;
			}
//			if(((Importable)sb.sub).getParentImport()!=null && !SemGenGUI.annotateitemshowimports.isSelected()){
//				return false;
//			}
		}
		return true;
	}
	
	public String getLookupNameForAnnotationObjectButton(AnnotationObjectButton aob){
		return (aob.ssc instanceof MappableVariable) ? aob.ssc.getName().substring(aob.ssc.getName().lastIndexOf(".")+1) : aob.ssc.getName();
	}
	
	protected void updateTreeNode(){
		if(tree!=null && SemGenGUI.annotateitemtreeview.isSelected()){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) (tree.getSelectionPath().getLastPathComponent()); 
			tree.update(node, tree);
		}
	}
}