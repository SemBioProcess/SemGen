package semgen.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import semgen.ExternalURLButton;
import semgen.FileFilter;
import semgen.GenericThread;
import semgen.MoreInfoButton;
import semgen.SemGenGUI;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.physical.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;
import semsim.webservices.BioPortalConstants;
import semsim.webservices.BioPortalSearcher;
import semsim.webservices.UniProtSearcher;

public class ReferenceClassFinderPanel extends JPanel implements
		ActionListener, PropertyChangeListener, MouseListener, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7884648622981159203L;
	public JTable table;
	public Dimension scrollerdim;
	public JTabbedPane panelrighttabbed;
	public JSplitPane splitpane;
	public JScrollPane scrollpaneleft;
	public JScrollPane scrollpaneright;
	public JPanel panelright;
	public Annotator annotator;
	public String codeword;
	public URI tempURI;
	public AnnotationDialog anndialog;
	private JPanel selectKBsourcepanel;
	private JLabel selectKBsource;
	public String choosertype;

	public JComboBox ontologychooser;
	public Map<String,String> ontologySelectionsAndBioPortalIDmap = new HashMap<String,String>();
	private JComboBox findchooser;
	public JComboBox displaychooserright;
	public JComboBox displaychooserleft;
	public String[] existingresultskeysetarray;
	private JPanel querypanel;
	private JButton findbutton;
	private ExternalURLButton externalURLbutton;
	public JButton loadingbutton;
	private JButton querybutton;
	private JButton browsebutton;

	private JPanel leftscrollerbuttonpanel;
	private JPanel rightscrollerbuttonpanel;
	private JButton leftscrollerinfobutton;
	public JButton rightscrollerloadingbutton;
	public JButton leftscrollerapplybutton;
	//public MoreInfoButton rightscrollerinfobutton;
	public JButton rightscrollerapplybutton;

	public JButton addontsbutton;
	public JButton removeontsbutton;
	private JLabel findtext;
	private JPanel findpanel;
	public JTextField findbox;
	private JPanel resultspanel;
	private JPanel resultspanelleft;
	private JPanel resultspanelleftheader;
	private JPanel resultspanelrightheader;
	private JPanel resultspanelright;
	private JLabel resultslabelright;
	private JLabel resultslabelleft;
	public JList resultslistright = new JList();
	private JScrollPane resultsscrollerright;
	private JScrollPane resultsscrollerleft;
	//public URI currentonturi;
	public String currentonturistring = "";
	public String[] colnames = new String[] { "ID", "RDF:LABEL" };
	public Hashtable<String,String> resultsanduris = new Hashtable<String,String>();
//	public Hashtable<String,String> classnamesanduris = new Hashtable<String,String>();
	public Hashtable<String,String> rdflabelsanduris = new Hashtable<String,String>();
	public Hashtable<String, String> classnamesandshortconceptids = new Hashtable<String, String>();
	public JDialog moreinfodialog;
	public JOptionPane optionPane;
	public JDialog weborlocaldia;
	public JButton fromwebbutton;
	public JButton fromlocalbutton;
	public String bioportalID = null;
	public GenericThread querythread = new GenericThread(this, "performSearch");
	public Annotatable annotatable;
	public String[] ontList;

	
	public ReferenceClassFinderPanel(Annotator ann, Annotatable annotatable, String[] ontList) {
		annotator = ann;
		this.ontList = ontList;
		this.annotatable = annotatable;
		setUpUI();
	}
	
	public ReferenceClassFinderPanel(Annotator ann, String[] ontList) {
		annotator = ann;
		this.ontList = ontList;
		setUpUI();
	}
	
	// Set up the interface
	public void setUpUI(){

		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Create item list for ontology selector box
		for(String ontfullname : ontList){
			String itemtext = ontfullname;
			if(SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.containsKey(ontfullname)){
				itemtext = itemtext + " (" + SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(ontfullname) + ")";
			}
			if(SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.containsKey(ontfullname)
					&& BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.containsKey(ontfullname)){
				ontologySelectionsAndBioPortalIDmap.put(itemtext, SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(ontfullname));
			}
			else ontologySelectionsAndBioPortalIDmap.put(itemtext, null);
		}
		
		String[] ontologyboxitems = ontologySelectionsAndBioPortalIDmap.keySet().toArray(new String[]{});
		Arrays.sort(ontologyboxitems);

		selectKBsourcepanel = new JPanel();
		selectKBsource = new JLabel("Select ontology: ");
		selectKBsource.setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));

		ontologychooser = new JComboBox(ontologyboxitems);
		ontologychooser.setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		
		// Set ontology chooser to recently used ontology
		ontologychooser.setSelectedIndex(0);
		if(annotator.ontspref!=null){
			for(String ont : ontologyboxitems){
				if(ont.equals(annotator.ontspref)){
					ontologychooser.setSelectedItem(annotator.ontspref);
				}
			}
		}
		ontologychooser.addActionListener(this);

		addontsbutton = new JButton();
		addontsbutton.setIcon(SemGenGUI.plusicon);
		addontsbutton.addActionListener(this);
		addontsbutton.setToolTipText("Add an online or local OWL ontology for querying");

		removeontsbutton = new JButton();
		removeontsbutton.setIcon(SemGenGUI.minusicon);
		removeontsbutton.addActionListener(this);
		removeontsbutton.setToolTipText("Remove the selected ontology from the list");

		selectKBsourcepanel.add(selectKBsource);
		selectKBsourcepanel.add(ontologychooser);
		//selectKBsourcepanel.add(addontsbutton);
		//selectKBsourcepanel.add(removeontsbutton);
		selectKBsourcepanel.setMaximumSize(new Dimension(900, 40));

		querybutton = new JButton("Query");
		querybutton.setEnabled(false);

		browsebutton = new JButton("Browse");
		browsebutton.addActionListener(this);
		browsebutton.setEnabled(false);

		querypanel = new JPanel();
		querypanel.setLayout(new BoxLayout(querypanel, BoxLayout.X_AXIS));
		// querypanel.add(browsebutton);
		// querypanel.add(querybutton);

		findtext = new JLabel("Term search:  ");
		findtext.setFont(new Font("SansSerif", Font.PLAIN,SemGenGUI.defaultfontsize));

		findchooser = new JComboBox();
		findchooser.setFont(new Font("SansSerif", Font.ITALIC, SemGenGUI.defaultfontsize - 1));
		findchooser.addItem("contains");
		// findchooser.addItem("starts with");
		findchooser.addItem("exact match");
		findchooser.setMaximumSize(new Dimension(125, 25));

		findbox = new JTextField();
		findbox.setForeground(Color.blue);
		findbox.setBorder(BorderFactory.createBevelBorder(1));
		findbox.setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		findbox.setMaximumSize(new Dimension(300, 25));
		findbox.addActionListener(this);

		findbutton = new JButton("Go");
		findbutton.setVisible(true);
		findbutton.addActionListener(this);

		findpanel = new JPanel();
		findpanel.setLayout(new BoxLayout(findpanel, BoxLayout.X_AXIS));
		findpanel.add(findtext);
		findpanel.add(findchooser);
		findpanel.add(findbox);
		findpanel.add(findbutton);

		loadingbutton = new JButton();
		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);
		loadingbutton.setIcon(SemGenGUI.blankloadingicon);
		findpanel.add(loadingbutton);

		resultspanel = new JPanel();
		resultspanelleft = new JPanel(); // intended to contain tree view of ontology terms within their source ontology
		
		resultspanelleft.setLayout(new BoxLayout(resultspanelleft,BoxLayout.Y_AXIS));
		resultspanelright = new JPanel();
		resultspanelright.setLayout(new BoxLayout(resultspanelright,BoxLayout.Y_AXIS));

		resultspanelleftheader = new JPanel();
		resultspanelleftheader.setLayout(new BorderLayout());
		resultspanelleftheader.setOpaque(false);

		resultspanelrightheader = new JPanel();
		resultspanelrightheader.setLayout(new BorderLayout());
		resultspanelrightheader.setOpaque(false);

		resultslabelright = new JLabel("Search results");
		resultslabelright.setFont(new Font("SansSerif", Font.PLAIN,SemGenGUI.defaultfontsize));
		resultslabelleft = new JLabel("Browsing results");
		resultslabelright.setEnabled(true);
		resultslabelleft.setEnabled(true);

		displaychooserright = new JComboBox(new String[] { "Show class names","Show RDF labels" });
		displaychooserright.setFont(new Font("SansSerif", Font.ITALIC,SemGenGUI.defaultfontsize - 2));

		displaychooserleft = new JComboBox(new String[] { "Show class names","Show RDF labels" });
		displaychooserleft.setFont(new Font("SansSerif", Font.ITALIC,SemGenGUI.defaultfontsize - 2));

		resultslistright = new JList();
		resultslistright.addListSelectionListener(this);
		//resultslistright.setOpaque(false);
		resultslistright.setBackground(Color.white);
		resultslistright.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultslistright.setBorder(BorderFactory.createBevelBorder(1));
		resultslistright.setEnabled(true);

		BrowseTree browsetree = new BrowseTree();
		browsetree.tree.setEnabled(false);

		scrollerdim = new Dimension(650, 400);
		resultsscrollerright = new JScrollPane(resultslistright);
		resultsscrollerright.setBorder(BorderFactory.createTitledBorder("Search results"));
		resultsscrollerright.setPreferredSize(scrollerdim);
		resultsscrollerleft = new JScrollPane(browsetree.tree);
		//resultsscrollerleft.setPreferredSize(scrollerdim);
		resultsscrollerleft.setEnabled(false);

		leftscrollerinfobutton = new JButton("more info");
		leftscrollerinfobutton.setOpaque(false);
		leftscrollerapplybutton = new JButton("Apply");

		//rightscrollerinfobutton = new MoreInfoButton();
		//rightscrollerinfobutton.addMouseListener(this);
		rightscrollerloadingbutton = new JButton();
		rightscrollerloadingbutton.setIcon(SemGenGUI.blankloadingiconsmall);
		rightscrollerloadingbutton.setBorderPainted(false);
		rightscrollerloadingbutton.setContentAreaFilled(false);
		rightscrollerapplybutton = new JButton("Apples");
		// rightscrollerapplybutton.addActionListener(this);
		
		externalURLbutton = new ExternalURLButton();
		
		leftscrollerbuttonpanel = new JPanel();
		leftscrollerbuttonpanel.setOpaque(false);
		leftscrollerbuttonpanel.setLayout(new BorderLayout());
		// leftscrollerbuttonpanel.setMaximumSize(new Dimension(900,40));
		leftscrollerbuttonpanel.add(leftscrollerinfobutton, BorderLayout.WEST);
		leftscrollerbuttonpanel.add(leftscrollerapplybutton, BorderLayout.EAST);

		rightscrollerbuttonpanel = new JPanel();
		rightscrollerbuttonpanel.setLayout(new BorderLayout());
		rightscrollerbuttonpanel.setOpaque(false);
		// rightscrollerbuttonpanel.setMaximumSize(new Dimension(900,40));
		JPanel rightscrollerinfobuttonpanel = new JPanel();
		//rightscrollerinfobuttonpanel.add(rightscrollerinfobutton);
		rightscrollerinfobuttonpanel.add(externalURLbutton);
		rightscrollerbuttonpanel.add(rightscrollerinfobuttonpanel, BorderLayout.WEST);
		rightscrollerbuttonpanel.add(Box.createGlue(), BorderLayout.EAST);
		//rightscrollerbuttonpanel.add(rightscrollerloadingbutton,BorderLayout.CENTER);
		//rightscrollerbuttonpanel.add(rightscrollerapplybutton,BorderLayout.EAST);

		resultspanelrightheader.add(resultslabelright, BorderLayout.WEST);
		//resultspanelrightheader.add(displaychooserright, BorderLayout.EAST);
		resultspanelrightheader.add(Box.createGlue(), BorderLayout.EAST);

		resultspanelleftheader.add(resultslabelleft, BorderLayout.WEST);
		resultspanelleftheader.add(displaychooserleft, BorderLayout.EAST);

		resultspanelleft.add(resultspanelleftheader);
		resultspanelleft.add(resultsscrollerleft);
		resultspanelleft.add(leftscrollerbuttonpanel);

//		resultspanelright.add(resultspanelrightheader);
//		resultspanelright.add(resultsscrollerright);
//		resultspanelright.add(rightscrollerbuttonpanel);

		resultspanelleft.setOpaque(false);
		resultspanelright.setOpaque(false);
		resultspanel.setOpaque(false);
		//resultspanel.add(resultspanelleft);
		//resultspanel.add(resultspanelright);

		resultspanelleft.setVisible(false);

		JComponent[] arrayright = { selectKBsourcepanel, querypanel, findpanel, resultsscrollerright, rightscrollerbuttonpanel};

		for (int i = 0; i < arrayright.length; i++) {
			//arrayright[i].setOpaque(false);
			this.add(arrayright[i]);
		}
		findbox.requestFocusInWindow();
	}


	// Show the RDF labels for the classes in the results list instead of the class names
	public void showRDFlabels() {
		resultsanduris = rdflabelsanduris;
		String[] resultsarray = (String[]) rdflabelsanduris.keySet().toArray(new String[] {});
		Arrays.sort(resultsarray);
		resultslistright.setListData(resultsarray);
	}

	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if ((o == findbox || o == findbutton || o == findchooser) && !findbox.getText().equals("")) {
			// if(findbutton.getText().equals("Go")){
			loadingbutton.setIcon(SemGenGUI.loadingicon);
			// findbutton.setText("Stop");
			findbox.setEnabled(false);
			findbutton.setEnabled(false);
			resultslistright.setListData(new Object[] {});
			//rightscrollerinfobutton.termuri = null;
			externalURLbutton.setTermURI(null);
			querythread = new GenericThread(this, "performSearch");
			querythread.start();
		}

		if (o == ontologychooser) {
			if(ontologychooser.getItemCount()>2){
				annotator.ontspref = (String) ontologychooser.getSelectedItem();
			}
		}

		if (o == addontsbutton) {
			weborlocaldia = new JDialog();
			weborlocaldia.setTitle("Select an option");
			fromwebbutton = new JButton("Enter web URL");
			fromwebbutton.addActionListener(this);
			fromlocalbutton = new JButton("Locate file on this computer");
			fromlocalbutton.addActionListener(this);
			Object[] buttonarray = new Object[] { fromwebbutton,
					fromlocalbutton };
			JOptionPane optpane = new JOptionPane(buttonarray,
					JOptionPane.PLAIN_MESSAGE, JOptionPane.NO_OPTION);
			Object[] optionsarray = new Object[] {};
			optpane.setOptions(optionsarray);
			weborlocaldia.setContentPane(optpane);
			weborlocaldia.setModal(true);
			weborlocaldia.pack();
			weborlocaldia.setLocationRelativeTo(this);
			weborlocaldia.setVisible(true);
		}
//
//		if (o == removeontsbutton) {
//			String onttoremove = (String) ontologychooser.getSelectedItem();
//			// int x = inputpane.showConfirmDialog(this, );
//			int x = JOptionPane.showConfirmDialog(this,
//					"Are you sure you want to remove " + onttoremove + " from the list?", "Remove ontology?", JOptionPane.YES_NO_OPTION);
//			if (x == 0) {
//				SemGenGUI.RefOntologiesAndURIsTable_all.remove(onttoremove);
//				try {
//					annotator.removeOntFromFile(onttoremove);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				ontologychooser.removeItem(onttoremove);
//				ontologychooser.setSelectedIndex(0);
//			}
//		}

//		if (o == fromwebbutton) {
//			addOnlineOWLFile();
//		}
////		if (o == fromlocalbutton) {
//			addLocalOWLFile();
//		}
	}

	public void addOnlineOWLFile() {
		weborlocaldia.setVisible(false);
		String urltoadd = JOptionPane.showInputDialog("Enter the URL of the ontology");
		if (!urltoadd.equals(null)) {
			String shortname = JOptionPane.showInputDialog("Enter the name of the ontology");
			if (!shortname.equals(null)) {
				addReferenceOntology(shortname, urltoadd);
			}
		}
	}

//	public void addLocalOWLFile() {
//		weborlocaldia.setVisible(false);
//		File file;
//		fc = new JFileChooser();
//		fc.addChoosableFileFilter(new FileFilter(new String[] { "owl", "OWL" }));
//		fc.setCurrentDirectory(SemGenGUI.currentdirectory);
//		fc.setDialogTitle("Select local OWL file");
//		int returnVal = fc.showOpenDialog(this);
//		if (returnVal == JFileChooser.APPROVE_OPTION) {
//			SemGenGUI.currentdirectory = fc.getCurrentDirectory();
//			file = fc.getSelectedFile();
//			String[] strarray = {};
//
//			if (SemGenGUI.RefOntologiesAndURIsTable_all.containsKey(file.getName())) {
//				strarray = (String[]) SemGenGUI.RefOntologiesAndURIsTable_all.get(file.getName());
//				if (strarray[0].equals(file.toURI().toString())) {
//					JOptionPane.showMessageDialog(this,
//							"A file with that name is already in ontology list",
//							"ERROR", JOptionPane.ERROR_MESSAGE);
//				}
//			} else {
//				addReferenceOntology(file.getName(), file.toURI().toString());
//			}
//		}
//	}

	public void addReferenceOntology(String nickname, String URL) {
//		try {
//			annotator.appendOntFile(nickname, URL);
//			SemGenGUI.RefOntologiesAndURIsTable_all.put(nickname,new String[] { URL });
//			ontologylist = (String[]) SemGenGUI.RefOntologiesAndURIsTable_all.keySet().toArray(new String[] {});
//			Arrays.sort(ontologylist);
//			ontologychooser.addItem(nickname);
//			ontologychooser.setSelectedItem(nickname);
//		} catch (IOException x) {
//			x.printStackTrace();
//		}
	}

	public void performSearch() {
		bioportalID = null;
		String text = findbox.getText();
		String ontologyselection = ontologychooser.getSelectedItem().toString();
		// Executed when the search button is pressed
		rdflabelsanduris.clear();
		classnamesandshortconceptids.clear();
		resultslistright.setEnabled(true);
		resultslistright.removeAll();
		//Hashtable<String,String[]> whichtable = SemGenGUI.RefOntologiesAndURIsTable_all;
		//String[] uriarray = (String[]) whichtable.get(ontologychooser.getSelectedItem().toString());

		//currentonturi = URI.create((String) uriarray[0]);
		//currentonturistring = (String) uriarray[0];
//		String results = "";
//		OWLOntology localont = null;
//		Set<String[]> seturisandrdflabels = new HashSet<String[]>();
		if (ontologySelectionsAndBioPortalIDmap.get(ontologyselection)!=null)
			bioportalID = ontologySelectionsAndBioPortalIDmap.get(ontologyselection);

		// If the ontology is local
		//if (currentonturistring.startsWith("file")) {
			/*
			try {
				localont = SemSimOWLFactory.getOntologyIfPreviouslyLoaded(IRI.create(currentonturi), annotator.manager);
				if (localont == null) {
					localont = annotator.manager.loadOntologyFromOntologyDocument(IRI.create(currentonturi));
				}
			} catch (OWLOntologyCreationException e) {
				JOptionPane.showMessageDialog(this,"Could not load the ontology");
				SemGenGUI.logfilewriter.println(e.toString());
			}

			Set<OWLClassExpression> classestosearch = new HashSet<OWLClassExpression>();
			Hashtable<String, String> classnamestosearchanduris = new Hashtable<String, String>();
			Hashtable<String, String> annotationstosearchanduris = new Hashtable<String, String>();
			Set<String> matcheduris = new HashSet<String>();

			if (localont.containsClassInSignature(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "SemSim_component"))) {
				classestosearch.addAll(SemGenGUI.factory.getOWLClass(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "Reference_physical_entity")).getSubClasses(localont));
				classestosearch.addAll(SemGenGUI.factory.getOWLClass(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "Reference_physical_property")).getSubClasses(localont));
				classestosearch.addAll(SemGenGUI.factory.getOWLClass(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "Reference_physical_process")).getSubClasses(localont));

			} else {classestosearch.addAll(localont.getClassesInSignature());}

			for (OWLClassExpression oneclass : classestosearch) {
				classnamestosearchanduris.put(
						SemSimOWLFactory.getIRIfragment(oneclass.asOWLClass().getIRI().toString()), oneclass.asOWLClass().getIRI().toString());
				for (String label : SemSimOWLFactory.getRDFLabels(localont,oneclass.asOWLClass())) {
					if (!label.equals("")) {
						annotationstosearchanduris.put(label, oneclass.asOWLClass().getIRI().toString());
					}
				}
			}
			// If we're looking for things that contain the search string
			// (case-insensitive)
			if (findchooser.getSelectedIndex() == 0) {
				for (String classname : classnamestosearchanduris.keySet()) {
					if (classname.toLowerCase().contains(text.toLowerCase())) {
						classnamesanduris.put(classname, classnamestosearchanduris.get(classname));
						matcheduris.add(classnamestosearchanduris.get(classname));
					}
				}
				for (String ann : annotationstosearchanduris.keySet()) {
					if (ann.toLowerCase().contains(text.toLowerCase())
							&& !matcheduris.contains(annotationstosearchanduris.get(ann))) {
						classnamesanduris.put(ann, annotationstosearchanduris.get(ann));
						rdflabelsanduris.put(ann, annotationstosearchanduris.get(ann));
					}
				}
			}
			// If we're looking for an exact match to the search string
			else if (findchooser.getSelectedIndex() == 1) {
				for (String classname : classnamestosearchanduris.keySet()) {
					if (classname.equals(text)) {
						classnamesanduris.put(classname, classnamestosearchanduris.get(classname));
						matcheduris.add(classnamestosearchanduris.get(classname));
					}
				}
				for (String ann : annotationstosearchanduris.keySet()) {
					if (ann.equals(text)
							&& !matcheduris.contains(annotationstosearchanduris.get(ann))) {
						classnamesanduris.put(ann, annotationstosearchanduris.get(ann));
						rdflabelsanduris.put(ann, annotationstosearchanduris.get(ann));
					}
				}
			}
			*/
		//}

		// If the user is searching BioPortal
		if (bioportalID!=null && SemGenGUI.annotateitemusebioportal.isSelected()) {
			BioPortalSearcher bps = new BioPortalSearcher();
			try {
				bps.search(text, bioportalID, findchooser.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				SemGenGUI.showWebConnectionError("BioPortal web service");
			} catch (JDOMException e) {e.printStackTrace();}
			
			currentonturistring = bps.currentonturistring;
			//currentonturi = bps.currentonturi;
			//classnamesanduris = bps.classnamesanduris;
			rdflabelsanduris = bps.rdflabelsanduris;
			classnamesandshortconceptids = bps.classnamesandshortconceptids;
			
			if(annotatable!=null){
				if(ontologyselection.startsWith(SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME) && 
						annotatable instanceof PhysicalProperty){
					rdflabelsanduris = removeNonPropertiesFromOPB(rdflabelsanduris);
				}
			}
		}
		else if(ontologyselection.startsWith(SemSimConstants.UNIPROT_FULLNAME)){
			UniProtSearcher ups = new UniProtSearcher();
			try {
				ups.search(text);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				SemGenGUI.showWebConnectionError("UniProt web service");
			}
			rdflabelsanduris = ups.rdflabelsanduris;
		}
		/*
		// If the ontology is elsewhere the web, use the vSPARQL service provided by Dr. Jim Brinkley's group at UW
		 * DEPRECATED
		 */
//		else {
//			if (!currentonturistring.equals("")) {
//				service = new VSparQLServiceProxy();
//				if (findchooser.getSelectedIndex() == 0) {
//					try {
//						// System.out.println(currentonturistring);
//						results = service
//								.executeQuery("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
//										+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//										+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
//										+ "PREFIX gleen:<java:edu.washington.sig.gleen.>"
//										+ "SELECT ?a str(?b) "
//										+ "FROM <"
//										+ currentonturistring
//										+ ">"
//										+ "WHERE {{?a rdfs:label ?b . FILTER regex(str(?b), '"
//										+ text
//										+ "', 'i')} UNION "
//										+ "{?a rdf:type owl:Class . FILTER regex(str(?a), '"
//										+ text + "', 'i') . }}");
//					} catch (RemoteException e) {
//						e.printStackTrace();
//					}
//				} else if (findchooser.getSelectedIndex() == 1) {
//					try {
//						results = service
//								.executeQuery(" PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
//										+ " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//										+ " PREFIX owl:<http://www.w3.org/2002/07/owl#>"
//										+ " PREFIX gleen:<java:edu.washington.sig.gleen.>"
//										+ "	SELECT ?a str(?b) "
//										+ "	FROM <"
//										+ currentonturistring
//										+ ">"
//										+ "WHERE {{?a rdfs:label ?b . FILTER regex(str(?b), '^"
//										+ text
//										+ "', 'i')} UNION "
//										+ "{?a rdf:type owl:Class . FILTER regex(str(?a), '^"
//										+ text + "', 'i') . }}");
//					} catch (RemoteException e) {
//						e.printStackTrace();
//					}
//				} else {
//					try {
//						results = service
//								.executeQuery(" PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
//										+ " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//										+ " PREFIX owl:<http://www.w3.org/2002/07/owl#>"
//										+ " PREFIX gleen:<java:edu.washington.sig.gleen.>"
//										+ "	SELECT ?a str(?b) "
//										+ "	FROM <"
//										+ currentonturistring
//										+ ">"
//										+ "	WHERE {{?a rdfs:label ?b . FILTER (str(?b) = \""
//										+ text
//										+ "\")} UNION "
//										+ "{?a rdf:type owl:Class . FILTER (str(?a) = \""
//										+ text + "\") . }}");
//					} catch (RemoteException e) {
//						e.printStackTrace();
//					}
//				}
//			} else {
//				JOptionPane.showMessageDialog(this,
//								"Sorry. No OWL version of " + ontologychooser.getSelectedItem().toString()
//										+ " available for search.\nTry using the BioPortal query service:\nAnnotater > Set query service > BioPortal");
//			}
//
//			if (results.contains("<")) {
//				// Do not include anonymous classes in results
//				// Process the results from the web service query
//				Scanner resultsscanner = new Scanner(results);
//				resultsscanner.nextLine();
//				resultsscanner.nextLine();
//				resultsscanner.nextLine();
//
//				while (resultsscanner.hasNext()) {
//					String nextline = resultsscanner.nextLine();
//					if (nextline.startsWith("|")) {
//						nextline = nextline.substring(2, nextline.length() - 1);
//						String uri = nextline.substring(0, nextline.indexOf("|"));
//						String rdflabel = nextline.substring(nextline.indexOf("|") + 2, nextline.length());
//						rdflabel = rdflabel.trim();
//						if (uri.startsWith("<")) {
//							uri = nextline.substring(nextline.indexOf("<") + 1, nextline.indexOf(">"));
//							classnamesanduris.put(uri.substring(uri.indexOf("#") + 1, uri.length()), uri);
//							rdflabelsanduris.put(rdflabel, uri);
//							seturisandrdflabels.add(new String[] { uri, rdflabel });
//						}
//					}
//				}
//			}
//		}

		// Sort the results
		if (!rdflabelsanduris.isEmpty()) {
			resultsanduris = rdflabelsanduris;
			String[] resultsarray = (String[]) resultsanduris.keySet().toArray(new String[] {});
			Arrays.sort(resultsarray);
			resultslistright.setListData(resultsarray);
			//resultslistright.setBackground(Color.white);
		} 
		else {
			resultslistright.setListData(new String[] { "---Search returned no results---" });
			resultslistright.setEnabled(false);
		}

		findbutton.setText("Go");
		loadingbutton.setIcon(SemGenGUI.blankloadingicon);
		findbox.setEnabled(true);
		findbutton.setEnabled(true);
	}
	
	
	// Remove any OPB terms that are not Physical Properties
	public Hashtable<String,String> removeNonPropertiesFromOPB(Hashtable<String, String> table){
		Hashtable<String,String> newtable = new Hashtable<String,String>();
		for(String key : table.keySet()){
			if(SemGenGUI.OPBproperties.contains(table.get(key)))
				newtable.put(key, table.get(key));
		}
		return newtable;
	}

	
	
	public void propertyChange(PropertyChangeEvent arg0) {}

	public void mouseClicked(MouseEvent arg0) {}

	
	public void mouseEntered(MouseEvent arg0) {
	//	if (arg0.getSource() == rightscrollerinfobutton) rightscrollerinfobutton.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent arg0) {
	//	if (arg0.getSource() == rightscrollerinfobutton) rightscrollerinfobutton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void mousePressed(MouseEvent arg0) {
	//	if (arg0.getSource() == rightscrollerinfobutton) rightscrollerinfobutton.setBorder(BorderFactory.createLineBorder(Color.blue,1));
	}

	public void mouseReleased(MouseEvent arg0) {
	//	if (arg0.getSource() == rightscrollerinfobutton) rightscrollerinfobutton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}
	

	public void valueChanged(ListSelectionEvent arg0) {
        boolean adjust = arg0.getValueIsAdjusting();
        if (!adjust) {
          JList list = (JList) arg0.getSource();
          if(list.getSelectedValue()!=null){
        	  String termuri = (String) resultsanduris.get(list.getSelectedValue());
        	 // rightscrollerinfobutton.termuri = termuri;
        	 // rightscrollerinfobutton.shortid = classnamesandshortconceptids.get(list.getSelectedValue());
        	  externalURLbutton.setTermURI(URI.create(termuri)); 
        	  Boolean uniprotterm = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(
    					SemSimOWLFactory.getNamespaceFromIRI(termuri))==SemSimConstants.UNIPROT_FULLNAME;
    		 // rightscrollerinfobutton.setEnabled(!uniprotterm);
          }
        }
	}
}
