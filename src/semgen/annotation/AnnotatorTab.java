package semgen.annotation;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.SemGen;
import semgen.SemGenGUI;
import semgen.SemGenSettings;
import semgen.resource.ComparatorByName;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenScrollPane;
import semgen.resource.uicomponent.SemGenTab;
import semsim.SemSimConstants;
import semsim.model.Importable;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.Submodel;

import java.net.HttpURLConnection;
import java.net.URI;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Hashtable;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotatorTab extends SemGenTab implements ActionListener, MouseListener,
		KeyListener {

	private static final long serialVersionUID = -5360722647774877228L;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File sourcefile; //File originally loaded at start of Annotation session (could be in SBML, MML, CellML or SemSim format)
	public URI fileURI;
	
	public SingularAnnotationEditor noncompeditor;
	public HumanDefEditor humdefeditor;

	private JSplitPane splitpane;
	private JSplitPane eastsplitpane;
	public JSplitPane westsplitpane;
	public SemGenScrollPane submodelscrollpane;
	public SemGenScrollPane codewordscrollpane;
	public SemGenScrollPane dialogscrollpane = new SemGenScrollPane();
	private SemGenScrollPane treeviewscrollpane = new SemGenScrollPane();
	
	public AnnotatorButtonTree tree;

	public AnnotationDialog anndialog;
	public AnnotationObjectButton focusbutton;
	public Hashtable<String, CodewordButton> codewordbuttontable = new Hashtable<String, CodewordButton>();
	public Hashtable<String, SubmodelButton> submodelbuttontable = new Hashtable<String, SubmodelButton>();

	public JPanel codewordpanel = new JPanel();
	public JPanel submodelpanel = new JPanel();
	public JTextArea codearea = new JTextArea("");
	private JPanel genmodinfo;
	public JTextPane annotationpane = new JTextPane();
	private String nextline;
	public JButton addsubmodelbutton = new JButton(SemGenIcon.plusicon);
	public JButton removesubmodelbutton = new JButton(SemGenIcon.minusicon);
	public CustomPhysicalComponentEditor customtermeditor;

	public Highlighter hilit = new DefaultHighlighter();
	public Highlighter.HighlightPainter allpainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
	public Highlighter.HighlightPainter onepainter = new DefaultHighlighter.DefaultHighlightPainter(SemGenGUI.lightblue);

	public int numcomponents;
	public static int initwidth;
	public static int initheight;
	public String ontspref;
	public JButton extractorbutton = new JButton(SemGenIcon.extractoricon);
	public JButton coderbutton;
	private JCheckBox sortbycompletenessbutton = new JCheckBox("Sort by Composite Completeness");
	public TextMinerDialog tmd;
	public ArrayList<Integer> indexesOfHighlightedTerms;
	public int currentindexforcaret;
	public int lastSavedAs = -1;
	
	private boolean modelsaved = false;
	
	public SemSimModel semsimmodel;

	public AnnotatorTab(File srcfile, SemGenSettings sets) {
		super(srcfile.getName(), SemGenIcon.annotatoricon, "Annotating " + srcfile.getName(), sets);
		this.sourcefile = srcfile;
		fileURI = srcfile.toURI();
		initwidth = settings.getAppWidth();
		initheight = settings.getAppHeight();
		setOpaque(false);
		setLayout(new BorderLayout());

		// Create the scroll pane for the imported model code
		codearea.setEditable(false);
		codearea.setBackground(new Color(250, 250, 227));
		codearea.setMargin(new Insets(5, 15, 5, 5));
		codearea.setForeground(Color.black);
		codearea.setFont(SemGenFont.Plain("Monospaced"));
		codearea.setHighlighter(hilit);

		codewordpanel.setBackground(Color.white);
		codewordpanel.setLayout(new BoxLayout(codewordpanel, BoxLayout.Y_AXIS));
		
		submodelpanel.setBackground(Color.white);
		submodelpanel.setLayout(new BoxLayout(submodelpanel, BoxLayout.Y_AXIS));
		submodelscrollpane = new SemGenScrollPane(submodelpanel);
		addsubmodelbutton.addActionListener(this);
		removesubmodelbutton.addActionListener(this);
		
		SemGenScrollPane legacycodescrollpane = new SemGenScrollPane();
		
		legacycodescrollpane.getViewport().add(codearea);
		dialogscrollpane.setBackground(SemGenGUI.lightblue);
		dialogscrollpane.getViewport().setBackground(SemGenGUI.lightblue);
		
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

		// Create the general model information panel
		extractorbutton.setToolTipText("Open this model in Extractor");
		extractorbutton.setSize(new Dimension(10, 10));
		extractorbutton.setRolloverEnabled(true);
		extractorbutton.addActionListener(this);
		extractorbutton.setPreferredSize(new Dimension(20, 20));
		extractorbutton.setAlignmentY(JButton.TOP_ALIGNMENT);

		coderbutton = new JButton(SemGenIcon.codericon);
		coderbutton.setToolTipText("Encode this model for simulation");
		coderbutton.setRolloverEnabled(true);
		coderbutton.addActionListener(this);
		coderbutton.setAlignmentY(JButton.TOP_ALIGNMENT);
		coderbutton.setPreferredSize(new Dimension(20, 20));

		sortbycompletenessbutton.setFont(SemGenFont.defaultPlain(-1));
		sortbycompletenessbutton.addActionListener(this);
		
		JPanel toolpanel = new JPanel();
		toolpanel.add(extractorbutton);
		toolpanel.add(coderbutton);
		toolpanel.add(sortbycompletenessbutton);
		toolpanel.setAlignmentY(JPanel.TOP_ALIGNMENT);

		genmodinfo = new JPanel(new BorderLayout());
		genmodinfo.setOpaque(true);
		genmodinfo.add(toolpanel, BorderLayout.WEST);
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
		if (o == extractorbutton) {
			try {
				if(unsavedChanges()){
					SemGenGUI.NewExtractorTask task = new SemGenGUI.NewExtractorTask(sourcefile);
					task.execute();
				}
			} catch (Exception e1) {
				e1.printStackTrace();}
		}
		if (o == sortbycompletenessbutton) {
			AlphabetizeAndSetCodewords();
		}
		
		if (o == coderbutton) {
			String filenamesuggestion = null;
			if(sourcefile!=null) filenamesuggestion = sourcefile.getName().substring(0, sourcefile.getName().lastIndexOf("."));
			try {
				if (!getModelSaved()) {
					int savefilechoice = JOptionPane.showConfirmDialog(this,
							"Save changes before encoding model?",
							"There are unsaved changes",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (savefilechoice == JOptionPane.YES_OPTION) {
						File tempfile = new File(SemGen.tempdir.getAbsoluteFile() + "/" + SemGenSettings.sdf.format(SemGen.datenow) + ".owl");

						manager.saveOntology(semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(tempfile));
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
	
	public SemSimModel NewAnnotatorAction(){
		SemGen.logfilewriter.println("Started new annotater");
		annotationpane.setText(null);
		annotationpane.setCaretPosition(0);
		
		try {
			setCodeViewer(semsimmodel.getLegacyCodeLocation(), true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		SemGenProgressBar progframe = new SemGenProgressBar("Sorting codewords...", true);
		
		refreshAnnotatableElements();
		showAnnotaterPanes(this);

		if(SemGenGUI.desktop.getComponentCount()>0){
			SemGenGUI.desktop.setSelectedComponent(SemGenGUI.desktop.getComponentAt(SemGenGUI.numtabs - 1));
		}
		
		codewordscrollpane.scrollToTop();
		submodelscrollpane.scrollToTop();
		
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

	public AnnotationDialog annotationObjectAction(AnnotationObjectButton aob) throws BadLocationException, IOException {
		if(focusbutton!=null){
			focusbutton.setBackground(Color.white);
		}
		aob.setBackground(SemGenGUI.lightblue);
		focusbutton = aob;

		dialogscrollpane.getViewport().removeAll();
		anndialog = new AnnotationDialog(this, aob);
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
		dialogscrollpane.scrollToTop();

		return anndialog;
	}
	
	// Refresh the display of codewords and submodels based on the view options selected in the Annotate menu
	public void refreshAnnotatableElements(){
		int divLoc = splitpane.getDividerLocation();
		if(divLoc==-1)
			divLoc = (int)(settings.getAppWidth())/3;
		
		// If the "Tree view" menu item is selected...
		if(SemGenGUI.annotateitemtreeview.isSelected()){
			sortbycompletenessbutton.setEnabled(false);
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
			treeviewscrollpane.scrollToLeft();
		}
		else{
			sortbycompletenessbutton.setEnabled(true);
			splitpane.setTopComponent(westsplitpane);
			AlphabetizeAndSetCodewords();
			AlphabetizeAndSetSubmodels();
			if(focusbutton!=null){
				if(!SemGenGUI.annotateitemtreeview.isSelected()){
					if(focusbutton instanceof CodewordButton)
						codewordscrollpane.scrollToComponent(focusbutton);
					else if(focusbutton instanceof SubmodelButton)
						submodelscrollpane.scrollToComponent(focusbutton);
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
		if(sortbycompletenessbutton.isSelected()) setCodewordsbyAnnCompleteness(aoblist);
		if(SemGenGUI.annotateitemsortbytype.isSelected()) setCodewordsbyMarker(aoblist);
		
		for (AnnotationObjectButton aob : aoblist) {
			aob.refreshAllCodes();

			CodewordButton cb = (CodewordButton) aob;
			
			// Set visibility
			aob.setVisible(getCodewordButtonVisibility(cb));
			
			// Set name
			if(!SemGenGUI.annotateitemtreeview.isSelected()) cb.namelabel.setText(cb.ds.getName());
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
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Select a codeword or submodel to view annotations");
		label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		panel.add(label, BorderLayout.CENTER);
		dialogscrollpane.getViewport().add(panel);
		dialogscrollpane.scrollToTop();
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
			SemGenProgressBar progframe = null;
			// If the legacy model code is on the web
			if (loc.startsWith("http://")) {
				progframe = new SemGenProgressBar("Retrieving legacy code...", false);
				
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
					progframe.setProgressValue(0);
					
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
							progframe.setProgressValue(x);
						}
						d.close();
						codearea.setCaretPosition(0);
					}
					else cont = false;
				} 
				else cont = false;
				
				progframe.dispose();
			}
			// Otherwise it's a local file
			else {
				modelfile = new File(loc);
				if (modelfile.exists()) {
					// Read in the model code and append it to the codearea
					// JTextArea in the top pane
					String filename = modelfile.getAbsolutePath();
					Scanner importfilescanner = new Scanner(new File(filename));
	
					while (importfilescanner.hasNextLine()) {
						nextline = importfilescanner.nextLine();
						codearea.append(nextline);
						codearea.append("\n");
						codearea.setCaretPosition(0);
					}
					importfilescanner.close();
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
	
	public static void showAnnotaterPanes(final AnnotatorTab ann){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
			   ann.add(ann.genmodinfo, BorderLayout.NORTH);
				ann.add(ann.splitpane, BorderLayout.CENTER);
				ann.setVisible(true);
				ann.eastsplitpane.setDividerLocation((int)(initheight-150)/2);
				ann.westsplitpane.setDividerLocation((int)(initheight-150)/2);
		   }
		});
	}

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
		while(match){
			if(m.find()){
				int index = m.start();
				indexesOfHighlightedTerms.add(index);
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
		return !(cb.ds.isImportedViaSubmodel() && !SemGenGUI.annotateitemshowimports.isSelected());
	}
	
	public boolean getSubmodelButtonVisibility(SubmodelButton sb){
		if(sb.sub instanceof Importable){
			if(((Importable)sb.sub).getParentImport()!=null && !SemGenGUI.annotateitemshowimports.isSelected()){
				return false;
			}
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
	
	public boolean checkFile(URI uri) {
		return (uri.toString().equals(fileURI.toString()));
	}
	
	public boolean unsavedChanges() {
		if (!getModelSaved()) {
			String title = "[unsaved file]";
			if(fileURI!=null){
				title =  new File(fileURI).getName();
			}
			int returnval= JOptionPane.showConfirmDialog(getParent(),
					"Save changes?", title + " has unsaved changes",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (returnval == JOptionPane.CANCEL_OPTION)
				return false;
			if (returnval == JOptionPane.YES_OPTION) {
				if(!SemGenGUI.SaveAction(this, lastSavedAs)){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean closeTab() {
		return unsavedChanges();
}
}