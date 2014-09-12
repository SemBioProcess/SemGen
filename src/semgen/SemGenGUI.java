package semgen;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import semgen.annotation.AddReferenceClassDialog;
import semgen.annotation.AnnotationComponentReplacer;
import semgen.annotation.AnnotationCopier;
import semgen.annotation.Annotator;
import semgen.annotation.BatchCellML;
import semgen.annotation.BatchSBML;
import semgen.annotation.CodewordButton;
import semgen.annotation.LegacyCodeChooser;
import semgen.annotation.ModelLevelMetadataEditor;
import semgen.annotation.RemovePhysicalComponentDialog;
import semgen.annotation.SemanticSummaryDialog;
import semgen.annotation.TextMinerDialog;
import semgen.extraction.ExtractorTab;
import semgen.merging.Merger;
import semsim.SemSimConstants;
import semsim.SemSimModelCache;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.annotation.SemSimRelation;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalModelComponent;
import semsim.owl.SemSimOWLFactory;
import semsim.reading.CellMLreader;
import semsim.reading.ModelClassifier;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLAnnotator;
import semsim.reading.SemSimOWLreader;
import semsim.reading.MMLreader;
import semsim.webservices.BioPortalConstants;
import semsim.webservices.WebserviceTester;
import semsim.writing.CaseInsensitiveComparator;
import semsim.writing.CellMLwriter;
import semsim.writing.MMLwriter;
import semsim.writing.Writer;

import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Date;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.xml.rpc.ServiceException;

import java.text.SimpleDateFormat;

public class SemGenGUI extends JFrame implements ActionListener, MenuListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3618439495848469800L;
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory factory;
	public static File semgenhomedir = new File(".");
	public static OWLOntology OPB;
	public static OWLOntology SemSimBase;
	public static SemSimModelCache modelCache = new SemSimModelCache();
	
	public static Set<String> OPBproperties = new HashSet<String>();
	public static Set<String> OPBflowProperties = new HashSet<String>();
	public static Set<String> OPBprocessProperties = new HashSet<String>();
	public static Set<String> OPBdisplacementsubclasses = new HashSet<String>();
	public static Set<String> OPBdynamicalProperties = new HashSet<String>();
	public static Set<String> OPBamountProperties = new HashSet<String>();
	public static Set<String> OPBforceProperties = new HashSet<String>();
	public static Set<String> OPBstateProperties = new HashSet<String>();

	public static Hashtable<String, String[]> OPBClassesForUnitsTable = new Hashtable<String, String[]>();
	public static Hashtable<String, String[]> compositeAnnRelationsTableLR = new Hashtable<String, String[]>();
	public static Hashtable<String, String[]> compositeAnnRelationsTableRL = new Hashtable<String, String[]>();
	public static Hashtable<String, String[]> metadataRelationsTable = new Hashtable<String, String[]>();
	public static Hashtable<String,String[]> ontologyTermsAndNamesCache = new Hashtable<String,String[]>();
	public static File tempdir = new File(System.getProperty("java.io.tmpdir"));
	public static File ontologyTermsAndNamesCacheFile = new File("cfg/ontologyTermsAndNamesCache.txt");
	public static Hashtable<String, String[]> jsimUnitsTable;
	public static Hashtable<String, String[]> jsimUnitPrefixesTable;
	public static CaseInsensitiveComparator cic = new CaseInsensitiveComparator();
	public static ImageIcon plusicon = createImageIcon("icons/plus.gif");
	public static ImageIcon minusicon = createImageIcon("icons/minus.gif");
	public static ImageIcon loadingicon = createImageIcon("icons/blackspinnerclear.gif");
	public static ImageIcon loadingiconsmall = createImageIcon("icons/preloader20x20.gif");
	public static ImageIcon blankloadingicon = createImageIcon("icons/blackspinnerclearempty.gif");
	public static ImageIcon blankloadingiconsmall = createImageIcon("icons/blackspinnersmallempty.gif");
	public static ImageIcon searchicon = createImageIcon("icons/Search2020.png");
	public static ImageIcon copyicon = createImageIcon("icons/Copy2020.png");
	public static ImageIcon createicon = createImageIcon("icons/Create2020.png");
	public static ImageIcon eraseicon = createImageIcon("icons/Erase2020.png");
	public static ImageIcon eraseiconsmall = createImageIcon("icons/Erase1313.png");
	public static ImageIcon modifyicon = createImageIcon("icons/Modify2020.png");
	public static ImageIcon loadsourceofimporticon = createImageIcon("icons/Load2020.png");
	public static ImageIcon homeicon = createImageIcon("icons/Home2020.png");
	public static ImageIcon homeiconsmall = createImageIcon("icons/Home1515.png");
	
	
	public static ImageIcon extractoricon = createImageIcon("icons/extractoricon2020.png");
	public static ImageIcon annotatoricon = createImageIcon("icons/annotatoricon2020.png");
	public static ImageIcon annotatoriconsmall = createImageIcon("icons/annotatoricon1515.png");
	public static ImageIcon codericon = createImageIcon("icons/codericon2020.png");
	public static ImageIcon mergeicon = createImageIcon("icons/mergeicon2020.png");
	public static ImageIcon moreinfoicon = createImageIcon("icons/moreinfoicon2020.png");
	public static ImageIcon externalURLicon = createImageIcon("icons/externalURL2020.png");
	public static ImageIcon expendcontracticon = createImageIcon("icons/expandcontracticon1.gif");
	public static Color lightblue = new Color(207, 215, 252, 255);
	public static Color lightgreen = new Color(204, 255, 204, 255);
	public static Color darkerblue = new Color(134, 156, 255, 255);
	public static Color dependencycolor = new Color(205, 92, 92, 255);
	public static Color entityblack = Color.black;
	public static Color processgreen = new Color(50,205,50);
	
	public static Dimension filechooserdims = new Dimension(550,550);
	
	public static File currentdirectory = new File("");
	public JDialog opendialog;
	public JButton annotatebutton;
	public JButton openmenuextractbutton;
	public JButton openmenumergebutton;
	public JButton encodebutton;
	public static String[] entityKBlist;
	public static String[] propertyKBlist;
	public static String[] depKBlist;
	public static String[] allKBlist;
	public static String logfileloc = tempdir.getAbsolutePath() + "/SemGen_log.txt";
	public static File logfile;
	public static PrintWriter logfilewriter;
	public static Date datenow;
	public static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");
	public SimpleDateFormat sdflog = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	public String owlfile;
	public String savefilename;
	public static String jsimhome;
	public static String jsimcmdloc;
	public static String jsbatchloc;
	public static String JSimBuildDir = "./jsimhome";
	public static JMenu extractmenu;
	public static JMenu filemenu;
	private JMenuItem fileitemnew;
	private JMenuItem fileitemopen;
	private JMenuItem fileitemsave;
	private JMenuItem fileitemsaveas;
	public JMenuItem fileitemclose;
	public JMenuItem fileitemexit;
	private JMenuItem edititemcut;
	private JMenuItem edititemcopy;
	private JMenuItem edititempaste;
	public static JMenuItem semanticsummary;
	public static JMenuItem annotateitemfindnext;
	private JMenuItem toolsitemannotate;
	private static JMenuItem annotateitemaddrefterm;
	private static JMenuItem annotateitemremoverefterm;
	public static JCheckBoxMenuItem autoannotate;
	private JMenuItem annotateitemchangesourcemodelcode;
	//public JMenuItem annotateitemfindjsbatch;
	public JMenuItem annotateitemreplacerefterm;
	public JMenuItem annotateitemcopy;
	public JMenuItem annotateitemharvestfromtext;
	public JMenuItem annotateitemeditmodelanns;
	public JMenuItem annotateitemcleanup;
	public JMenuItem annotateitemexportcsv;
	public JMenuItem annotateitemthai;
	public JMenuItem batchsbml;
	public static ButtonGroup querybuttons;
	public static ButtonGroup sortbuttons;
	public static JRadioButtonMenuItem annotateitemusebioportal;
	public static JRadioButtonMenuItem annotateitemusevsparql;
	public static JCheckBoxMenuItem annotateitemshowimports;
	public static JRadioButtonMenuItem annotateitemsortbytype;
	public static JRadioButtonMenuItem annotateitemsortalphabetically;
	public static JMenuItem annotateitemshowmarkers;
	public static JMenuItem annotateitemtreeview;
	private JMenuItem toolsitemmerge;
	private JMenuItem toolsitemcode;
	private JMenuItem toolsitemextract;
	private JMenuItem extractoritematomicdecomp;
	private JMenuItem extractoritembatchcluster;
	public JMenuItem extractoritemopenann;
	public JMenuItem encodeitemVPHcomposites;
	private JMenuItem viewlog;
	public JMenuItem upfont;
	private JMenuItem helpitemabout;
	private JMenuItem helpitemweb;
	private JMenuBar menubar;
	public static JFileChooser fc;
	private SimpleAttributeSet attrs;
	public static JTabbedPane desktop;
	public static double version = 2.0;
	public static int initxpos = 0;
	public static int initypos = 0;
	public static int numtabs = 0;
	public static int initwidth = 900;
	public static int initheight = 720;
	public static String initdir;
	public static int defaultfontsize = 12;
	public static Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
	public static Font underlinefont;
	public static Hashtable<String, String[]> startsettingstable;
	public static Map<String, String> envvars = System.getenv();
	public static SemGenGUI frame;
	public static boolean LINUXorUNIX;
	public static boolean WINDOWS;
	public static boolean MACOSX;
	public static int maskkey;
	public static ProgressFrame progframe;
	public static String unspecifiedName = "*unspecified*";
	public static Map<String, SemSimRelation> namestructuralrelationmap = new HashMap<String, SemSimRelation>();
	
	public static FileNameExtensionFilter owlfilter = new FileNameExtensionFilter("SemSim (*.owl)", "owl");
	public static FileNameExtensionFilter cellmlfilter = new FileNameExtensionFilter("CellML (*.cellml, .xml)", "cellml", "xml");
	public static FileNameExtensionFilter sbmlfilter = new FileNameExtensionFilter("SBML (*.sbml, .xml)", "sbml", "xml");
	public static FileNameExtensionFilter mmlfilter = new FileNameExtensionFilter("MML (*.mod)", "mod");
	public static FileNameExtensionFilter csvfilter = new FileNameExtensionFilter("CSV (*.csv)", "csv");


	public SemGenGUI(){
		super("OSXAdapter");
		
		maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		logfile = new File(logfileloc);
		try {
			logfilewriter = new PrintWriter(new FileWriter(logfile));
		} catch (IOException e4) {
			e4.printStackTrace();
		}

		MACOSX = OSValidator.isMac();
		WINDOWS = OSValidator.isWindows();
		LINUXorUNIX = OSValidator.isUnix();
		
		File libsbmlfile = null;
		
		if(WINDOWS) libsbmlfile = new File("cfg/sbmlj.dll"); ///ResourcesManager.writeResourceToTempDir("sbmlj.dll");}
		else if(MACOSX) libsbmlfile = new File("cfg/libsbmlj.jnilib");
		else libsbmlfile = new File("cfg/libsbmlj.so");
		
		// Needed because JSim API uses libSBML native library
		if(libsbmlfile.exists()){
			System.load(libsbmlfile.getAbsolutePath());
		}
		else 
			JOptionPane.showMessageDialog(this, "Couldn't open " + libsbmlfile.getAbsolutePath() + " for loading.");

		factory = manager.getOWLDataFactory();

		registerForMacOSXEvents();

		System.out.print("Loading SemGen...");
		logfilewriter.println("Loading SemGen");

		fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		underlinefont = new Font("SansSerif",Font.PLAIN, defaultfontsize).deriveFont(fontAttributes);

		// Load the local copy of the OPB and the SemSim base ontology, and other config files into memory
		try {
			OPB = manager.loadOntologyFromOntologyDocument(new File("cfg/OPB.970.owl"));
			SemSimBase = manager.loadOntologyFromOntologyDocument(new File("cfg/SemSimBase.owl"));
		} catch (OWLOntologyCreationException e3) {
			e3.printStackTrace();
		}
		//OntologyNicknamesTable = ResourcesManager.createHashtableFromFile("cfg/OntologyBaseNicknames.txt");
		
		try {
			compositeAnnRelationsTableLR = ResourcesManager.createHashtableFromFile("cfg/structuralRelationsLR.txt");
			compositeAnnRelationsTableRL = ResourcesManager.createHashtableFromFile("cfg/structuralRelationsRL.txt");
			metadataRelationsTable = ResourcesManager.createHashtableFromFile("cfg/metadataRelations.txt");
			ontologyTermsAndNamesCache = ResourcesManager.createHashtableFromFile("cfg/ontologyTermsAndNamesCache.txt");
			jsimUnitsTable = ResourcesManager.createHashtableFromFile("cfg/jsimUnits");
			jsimUnitPrefixesTable = ResourcesManager.createHashtableFromFile("cfg/jsimUnitPrefixes");
			OPBClassesForUnitsTable = ResourcesManager.createHashtableFromFile("cfg/OPBClassesForUnits.txt");
			startsettingstable = ResourcesManager.createHashtableFromFile("cfg/startSettings.txt");
		} catch (FileNotFoundException e3) {e3.printStackTrace();}
		
		try {
			OPBproperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00147", false);
			OPBflowProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00573", false);
			OPBprocessProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB01151", false);
			OPBdynamicalProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00568", false);
			OPBamountProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00135", false);
			OPBforceProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00574", false);
			OPBstateProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00569", false);
		} catch (OWLException e2) {e2.printStackTrace();}


		// Need this for programmatic use of jsbatch
		System.setProperty("jsim.home", "./jsimhome");
		
		// Set the window position and size
		initxpos = Integer.parseInt(startsettingstable.get("XstartPosition")[0].trim());
		initypos = Integer.parseInt(startsettingstable.get("YstartPosition")[0].trim());
		initwidth = Integer.parseInt(startsettingstable.get("startWidth")[0].trim());
		initheight = Integer.parseInt(startsettingstable.get("startHeight")[0].trim());

		currentdirectory = new File(startsettingstable.get("startDirectory")[0]);
		
		namestructuralrelationmap.put("contained_in", SemSimConstants.CONTAINED_IN_RELATION);
		namestructuralrelationmap.put("part_of", SemSimConstants.PART_OF_RELATION);


		setTitle(":: S e m  G e n ::");

		datenow = new Date();
		logfilewriter.println("Session started on: " + sdflog.format(datenow) + "\n");

		desktop = new JTabbedPane(); // a specialized layered pane
		//desktop.setUI(new CustomTabbedPaneUI());
		desktop.setOpaque(true);
		desktop.setBackground(Color.white);
		desktop.addChangeListener(this);
		setContentPane(desktop);

		numtabs = 0;
		attrs = new SimpleAttributeSet();
		StyleConstants.setFontSize(attrs, 13);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Create the menu bar.
		menubar = new JMenuBar();
		menubar.setOpaque(true);
		menubar.setPreferredSize(new Dimension(initwidth, 20));

		// Build the File menu
		filemenu = new JMenu("File");
		filemenu.getAccessibleContext().setAccessibleDescription("Create new files, opening existing files, import raw model code, etc.");
		filemenu.addMenuListener(this);

		// File menu items
		fileitemnew = formatMenuItem(fileitemnew,"New",KeyEvent.VK_N,false,true);
		fileitemopen = formatMenuItem(fileitemopen,"Open",KeyEvent.VK_O,true,true);
		filemenu.add(fileitemopen);
		fileitemsave = formatMenuItem(fileitemsave,"Save",KeyEvent.VK_S,true,true);
		filemenu.add(fileitemsave);
		fileitemsaveas = formatMenuItem(fileitemsaveas,"Save As",null,true,true);
		fileitemsaveas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + maskkey));
		filemenu.add(fileitemsaveas);
		
		fileitemclose = formatMenuItem(fileitemclose,"Close",KeyEvent.VK_W,true,true);
		filemenu.add(fileitemclose);
		fileitemexit = formatMenuItem(fileitemexit,"Quit SemGen",KeyEvent.VK_Q,true,true);
		filemenu.add(new JSeparator());
		filemenu.add(fileitemexit);

		// Build the Edit menu
		JMenu editmenu = new JMenu("Edit");
		editmenu.setMnemonic(KeyEvent.VK_D);
		editmenu.getAccessibleContext().setAccessibleDescription("Cut, copy, paste, etc.");

		// Edit menu items
		edititemcut = formatMenuItem(edititemcut, "Cut", KeyEvent.VK_X,true,true);
		editmenu.add(edititemcut);
		edititemcopy = formatMenuItem(edititemcopy, "Copy", KeyEvent.VK_C,true,true);
		editmenu.add(edititemcopy);
		edititempaste = formatMenuItem(edititempaste,"Paste",KeyEvent.VK_V,true,true);
		editmenu.add(edititempaste);

		// Build the Annotator menu
		JMenu annotatemenu = new JMenu("Annotate");
		// filemenu.setMnemonic(KeyEvent.VK_F);
		annotatemenu.getAccessibleContext().setAccessibleDescription("Annotate a model");

		toolsitemannotate = formatMenuItem(toolsitemannotate, "New Annotator",KeyEvent.VK_A,true,true);
		toolsitemannotate.setToolTipText("Open a new Annotator tool");
		annotatemenu.add(toolsitemannotate);
		annotatemenu.add(new JSeparator());
		
		semanticsummary = formatMenuItem(semanticsummary, "Biological summary", KeyEvent.VK_U, true, true);
		semanticsummary.setToolTipText("View summary of the biological concepts used in the model");
//		annotatemenu.add(semanticsummary);
//		annotatemenu.add(new JSeparator());
		
		autoannotate = new JCheckBoxMenuItem("Auto-annotate during translation");
		autoannotate.setSelected(startsettingstable.get("autoAnnotate")[0].trim().equals("true"));
		autoannotate.addActionListener(this);
		autoannotate.setToolTipText("Automatically add annotations based on physical units, etc. when possible");
		annotatemenu.add(autoannotate);
		annotatemenu.add(new JSeparator());
		
		annotateitemharvestfromtext = formatMenuItem(annotateitemharvestfromtext, "Find terms in text", KeyEvent.VK_F, true, true);
		annotateitemharvestfromtext.setToolTipText("Use natural language processing to find reference ontology terms in a block of text");
		//annotatemenu.add(annotateitemharvestfromtext);
		//annotatemenu.add(new JSeparator());
		
		annotateitemaddrefterm = formatMenuItem(annotateitemaddrefterm, "Add reference term", KeyEvent.VK_D,true,true);
		annotateitemaddrefterm.setToolTipText("Add a reference ontology term to use for annotating this model");
		annotatemenu.add(annotateitemaddrefterm);

		annotateitemremoverefterm = formatMenuItem(annotateitemremoverefterm, "Remove annotation component", KeyEvent.VK_R,true,true);
		annotateitemremoverefterm.setToolTipText("Remove a physical entity or process term from the model");
		annotatemenu.add(annotateitemremoverefterm);

		annotateitemcopy = formatMenuItem(annotateitemcopy, "Import annotations", KeyEvent.VK_I,true,true);
		annotateitemcopy.setToolTipText("Annotate codewords using data from identical codewords in another model");
		annotatemenu.add(annotateitemcopy);
		annotateitemcopy.setEnabled(true);
		
		annotateitemcleanup = formatMenuItem(annotateitemcleanup, "Remove unused terms",null, true,true);
		annotateitemcleanup.setToolTipText("Remove unused reference ontology terms");
		//annotatemenu.add(annotateitemcleanup);
		//annotateitemcleanup.setEnabled(false);
		
		annotateitemreplacerefterm = formatMenuItem(annotateitemreplacerefterm, "Replace reference term", KeyEvent.VK_P, true, true);
		annotateitemreplacerefterm.setToolTipText("Replace a reference ontology term with another");
		annotatemenu.add(annotateitemreplacerefterm);
		annotatemenu.add(new JSeparator());
		
		annotateitemchangesourcemodelcode = formatMenuItem(annotateitemchangesourcemodelcode, "Change legacy code", KeyEvent.VK_K, true, true);
		annotateitemchangesourcemodelcode.setToolTipText("Link the SemSim model with its computational code");
		annotatemenu.add(annotateitemchangesourcemodelcode);
		
		annotatemenu.add(new JSeparator());
		
		annotateitemexportcsv = formatMenuItem(annotateitemexportcsv, "Export codeword table", KeyEvent.VK_T, true, true);
		annotateitemexportcsv.setToolTipText("Create a .csv file that tabulates model codeword annotations for use in spreadsheets, manuscript preparation, etc.");

		annotatemenu.add(annotateitemexportcsv);
		annotateitemexportcsv.setEnabled(true);
		annotatemenu.add(new JSeparator());

		annotateitemeditmodelanns = formatMenuItem(annotateitemeditmodelanns, "Edit model-level annotations", KeyEvent.VK_J,true, true);
		annotateitemeditmodelanns.setToolTipText("Edit metadata for this SemSim model");
		annotatemenu.add(annotateitemeditmodelanns);
		annotatemenu.add(new JSeparator());
		
		JMenu ontsearchmenu = new JMenu("Set query service");
		annotateitemusebioportal = new JRadioButtonMenuItem("BioPortal");
		annotateitemusebioportal.setSelected(true);
		annotateitemusevsparql = new JRadioButtonMenuItem("vSPARQL");
		annotateitemusevsparql.setSelected(false);
		
		//annotateitemthai = formatMenuItem(annotateitemthai, "Batch process CellML models", KeyEvent.VK_0,true, true);
		//batchsbml = formatMenuItem(batchsbml, "Batch process SBML models", KeyEvent.VK_9,true, true);
		//annotatemenu.add(batchsbml);
		
		querybuttons = new ButtonGroup();
		querybuttons.add(annotateitemusebioportal);
		querybuttons.add(annotateitemusevsparql);

		ontsearchmenu.add(annotateitemusebioportal);
		ontsearchmenu.add(annotateitemusevsparql);
		//annotatemenu.add(ontsearchmenu);
		
		//annotatemenu.add(new JSeparator());
		annotateitemfindnext = formatMenuItem(annotateitemfindnext, "Find next instance of codeword in code", KeyEvent.VK_N, true, true);
		annotatemenu.add(annotateitemfindnext);
		
		annotatemenu.add(new JSeparator());
		
		annotateitemshowimports = new JCheckBoxMenuItem("Show imported codewords/sub-models");
		annotateitemshowimports.setSelected(startsettingstable.get("showImports")[0].trim().equals("true"));
		annotateitemshowimports.addActionListener(this);
		annotateitemshowimports.setToolTipText("Make imported codewords and submodels visible");
		annotatemenu.add(annotateitemshowimports);

		
		annotateitemshowmarkers = new JCheckBoxMenuItem("Display physical type markers");
		annotateitemshowmarkers.setSelected(startsettingstable.get("displayMarkers")[0].trim().equals("true"));
		annotateitemshowmarkers.addActionListener(this);
		annotateitemshowmarkers.setToolTipText("Display markers that indicate a codeword's property type");
		annotatemenu.add(annotateitemshowmarkers);
		
		annotateitemtreeview = new JCheckBoxMenuItem("Tree view");
		annotateitemtreeview.setSelected(startsettingstable.get("treeView")[0].trim().equals("true"));
		annotateitemtreeview.addActionListener(this);
		annotateitemtreeview.setToolTipText("Display codewords and submodels within the submodel tree");
		annotatemenu.add(annotateitemtreeview);
		
		JMenu sortCodewordsMenu = new JMenu("Sort codewords...");
		annotateitemsortbytype = new JRadioButtonMenuItem("By physical type");
		annotateitemsortbytype.setSelected(startsettingstable.get("organizeByPropertyType")[0].trim().equals("true"));
		annotateitemsortalphabetically = new JRadioButtonMenuItem("Alphabetically");
		annotateitemsortalphabetically.setSelected(!annotateitemsortbytype.isSelected());
		
		annotateitemsortbytype.addActionListener(this);
		annotateitemsortalphabetically.addActionListener(this);
		
		sortbuttons = new ButtonGroup();
		sortbuttons.add(annotateitemsortbytype);
		sortbuttons.add(annotateitemsortalphabetically);
		sortCodewordsMenu.add(annotateitemsortbytype);
		sortCodewordsMenu.add(annotateitemsortalphabetically);
		annotateitemsortbytype.setToolTipText("Sort codewords according to whether they represent a property of a physical entity, process, or dependency");
		annotatemenu.add(sortCodewordsMenu);
		
		
		//annotateitemthai = formatMenuItem(annotateitemthai, "Batch process CellML models", KeyEvent.VK_0,true, true);
		//batchsbml = formatMenuItem(batchsbml, "Batch process SBML models", KeyEvent.VK_9,true, true);
		//annotatemenu.add(batchsbml);
		
		// Extract menu
		extractmenu = new JMenu("Extract");
		extractmenu.getAccessibleContext().setAccessibleDescription("Extract out a portion of a SemSim model");

		toolsitemextract = formatMenuItem(toolsitemextract,"New Extractor",KeyEvent.VK_E,true,true);
		toolsitemextract.setToolTipText("Open a new Extractor tool");
		extractmenu.add(toolsitemextract);
		
		extractoritematomicdecomp = formatMenuItem(extractoritematomicdecomp, "Atomic decomposition",KeyEvent.VK_Y,true,true);
		extractoritematomicdecomp.setToolTipText("Extract separate SemSim models for each physical entity");
		//extractmenu.add(extractoritematomicdecomp);

		extractoritembatchcluster = formatMenuItem(extractoritembatchcluster, "Automated clustering analysis", KeyEvent.VK_B,true,true);
		extractoritembatchcluster.setToolTipText("Performs clustering analysis on model");
		extractmenu.add(extractoritembatchcluster);

		extractoritemopenann = formatMenuItem(extractoritemopenann, "Open model in Annotator", null, true, true);
		extractmenu.add(extractoritemopenann);
		
		// Merging menu
		JMenu mergermenu = new JMenu("Merge");
		mergermenu.getAccessibleContext().setAccessibleDescription("Create a composite model from two or more existing SemSim models");
		toolsitemmerge = formatMenuItem(toolsitemmerge,"New Merger",KeyEvent.VK_M,true,true);
		toolsitemmerge.setToolTipText("Open a new Merger tool");
		mergermenu.add(toolsitemmerge);

		// Encode menu
		JMenu codegenmenu = new JMenu("Encode");
		codegenmenu.getAccessibleContext().setAccessibleDescription("Translate a SemSim model into executable simulation code");

		toolsitemcode = formatMenuItem(toolsitemcode, "New code generator", KeyEvent.VK_G,true,true);
		toolsitemcode.setToolTipText("Open a new code generator tool");
		codegenmenu.add(toolsitemcode);

		encodeitemVPHcomposites = new JMenuItem("Encode composite annotations for VPH repository");
		encodeitemVPHcomposites.setEnabled(true);
		encodeitemVPHcomposites.addActionListener(this);
		//codegenmenu.add(encodeitemVPHcomposites);

		// upfont = new JMenuItem("Increase font size");
		// upfont.setEnabled(true);
		// upfont.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
		// maskkey));
		// viewmenu.add(upfont);
		// upfont.setToolTipText("Create a new model from selected subcomponents of a SemSim model");
		// upfont.addActionListener(this);

		// Help menu
		JMenu helpmenu = new JMenu("Help");
		helpmenu.getAccessibleContext().setAccessibleDescription("User help, Versioning, etc.");
		helpitemabout = formatMenuItem(helpitemabout,"About",null,true,true);
		helpmenu.add(helpitemabout);
		helpitemweb = formatMenuItem(helpitemweb,"Help manual (opens browser)",KeyEvent.VK_H,true,true);
		helpmenu.add(helpitemweb);
		//helpmenu.add(new JSeparator());
		viewlog = formatMenuItem(viewlog,"Session log",KeyEvent.VK_L,true,true);
		viewlog.setToolTipText("View the current session's log file");
		//helpmenu.add(viewlog);

		menubar.add(filemenu);
		// menubar.add(editmenu);
		menubar.add(annotatemenu);
		menubar.add(extractmenu);
		menubar.add(mergermenu);
		menubar.add(codegenmenu);
		menubar.add(helpmenu);

		this.setJMenuBar(menubar);
		
		// Set closing behavior
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					quit();
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (OWLException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		this.pack();

		setSize(initwidth, initheight);
		setLocation(initxpos, initypos);
		setVisible(true);
		System.out.println("Loaded.");
		setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		OpenFileAction();
	}
	
	// Format menu items, assign shortcuts, action listeners
	public JMenuItem formatMenuItem(JMenuItem item, String text, Integer accelerator, Boolean enabled, Boolean addactionlistener){
		item = new JMenuItem(text);
		item.setEnabled(enabled);
		if(accelerator!=null){item.setAccelerator(KeyStroke.getKeyStroke(accelerator,maskkey));}
		if(addactionlistener){item.addActionListener(this);}
		return item;
	}

	// METHODS
	
	public void menuCanceled(MenuEvent arg0) {
	}

	public void menuDeselected(MenuEvent arg0) {
	}

	public void menuSelected(MenuEvent arg0) {
		if(desktop.getSelectedComponent() instanceof Annotator){
			fileitemsave.setEnabled(!((Annotator)desktop.getSelectedComponent()).getModelSaved());
		}
	}
	
	
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == fileitemopen) {
			OpenFileAction();
		}

		if (o == fileitemsave) {
			if(desktop.getSelectedComponent() instanceof Annotator){
				Annotator ann = (Annotator)desktop.getSelectedComponent();
				SaveAction(ann, ann.lastSavedAs);
			}
		}

		if (o == fileitemsaveas) {
			SaveAsAction(desktop.getSelectedComponent(),null, new FileNameExtensionFilter[]{cellmlfilter, owlfilter});
		}
		if( o == fileitemclose){
			Component x = null;
			if(desktop.getSelectedComponent() instanceof Annotator){
				x = (Annotator)desktop.getSelectedComponent();
			}
			else if(desktop.getSelectedComponent() instanceof ExtractorTab){
				x = (ExtractorTab)desktop.getSelectedComponent();
			}
			else if(desktop.getSelectedComponent() instanceof Merger){
				x = (Merger)desktop.getSelectedComponent();
			}
			if(x !=null)
				try {
					closeTabAction(x);
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				}
		}

		if (o == fileitemexit) {try {
			quit();
		} catch (HeadlessException e1) {
			e1.printStackTrace();
		} catch (OWLException e1) {
			e1.printStackTrace();
		}}

		if (o == toolsitemannotate || o == annotatebutton) {
			opendialog.dispose();
			int x = showSemGenFileChooser(currentdirectory, new String[] { "owl", "mod", "xml", "sbml", "cellml" },
					"Select legacy code or SemSim model to annotate", SemGenFileChooser.NEW_ANNOTATOR_TASK, true);
			if (x == JFileChooser.APPROVE_OPTION) {
				startNewAnnotatorTask(fc.getSelectedFiles());
			}
		}
		
		if(o == semanticsummary){
			if(desktop.getSelectedComponent() instanceof Annotator){
				Annotator ann = (Annotator)desktop.getSelectedComponent();
				new SemanticSummaryDialog(ann.semsimmodel);
			}
		}
		
		if(o == annotateitemharvestfromtext){
			if(desktop.getSelectedComponent() instanceof Annotator){
				Annotator ann = (Annotator)desktop.getSelectedComponent();
				if(ann.tmd == null){
					try {ann.tmd = new TextMinerDialog(ann);} 
					catch (FileNotFoundException e1) {e1.printStackTrace();}
				}
				else{ann.tmd.setVisible(true);}
			}
			else{JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");}
		} 

		if (o == annotateitemaddrefterm) {
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				new AddReferenceClassDialog(ann, SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, 
						new Object[]{"Add as entity","Add as process","Close"}, ann.semsimmodel).packAndSetModality();
			} else {
				JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
			}
		}
		if (o == annotateitemremoverefterm) {
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				Set<PhysicalModelComponent> pmcs = new HashSet<PhysicalModelComponent>();
				for(PhysicalModelComponent pmc : ann.semsimmodel.getPhysicalModelComponents()){
					if(!(pmc instanceof CompositePhysicalEntity) && (pmc instanceof PhysicalEntity || pmc instanceof PhysicalProcess))
						pmcs.add(pmc);
				}
				new RemovePhysicalComponentDialog(ann, pmcs, null, false, "Select components to remove");
			} else {
				JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
			}
		}

		if (o == annotateitemchangesourcemodelcode) {
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				new LegacyCodeChooser(ann);
			} else {JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");}
		}

		if(o == annotateitemexportcsv){
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				try {
					new CSVExporter(ann.semsimmodel).exportCodewords();
				} catch (Exception e1) {e1.printStackTrace();} 
			}
		}
		
		if(o == annotateitemeditmodelanns){
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				new ModelLevelMetadataEditor(ann);
			}
		}

		if (o == annotateitemreplacerefterm) {
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				try {
					new AnnotationComponentReplacer(ann);
				} catch (OWLException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (o == annotateitemcopy) {
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				try {
					new AnnotationCopier(ann);
				} catch (OWLException e1) {
					e1.printStackTrace();
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
			}
			else JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
		}

		if (o == annotateitemcleanup) {
			if (desktop.getSelectedComponent() instanceof Annotator) {
				//Annotator ann = (Annotator) desktop.getSelectedComponent();
				//...
			} else {
				JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
			}
		}
		
		if( o == annotateitemfindnext){
			if (desktop.getSelectedComponent() instanceof Annotator) {
				Annotator ann = (Annotator) desktop.getSelectedComponent();
				String name = ann.getLookupNameForAnnotationObjectButton(ann.focusbutton);
				ann.findNextStringInText(name);
			}
		}
		
		if(o == batchsbml){
			try {
				new BatchSBML();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		if(o == annotateitemthai){
			try {new BatchCellML();}
			catch (Exception e1) {e1.printStackTrace();}
		}
		
		if (o == annotateitemshowmarkers){
			for(Component c : desktop.getComponents()){
				if(c instanceof Annotator){
					Annotator temp = (Annotator)c;
					for(String s : temp.codewordbuttontable.keySet()){
						CodewordButton cb = temp.codewordbuttontable.get(s);
						((CodewordButton)cb).propoflabel.setVisible(annotateitemshowmarkers.isSelected());
						cb.validate();
					}
				}
			}
		}
	
		if(o == annotateitemshowimports){
			
			// Set visbility of imported codewords and submodels
			if(desktop.getSelectedComponent() instanceof Annotator){
				Annotator temp = (Annotator)desktop.getSelectedComponent();
				temp.refreshAnnotatableElements();
			}
		}
		
		if(o == annotateitemsortbytype || o == annotateitemsortalphabetically || o == annotateitemtreeview){
			for(Component c : desktop.getComponents()){
				if(c instanceof Annotator){
					Annotator temp = (Annotator)c;
					temp.refreshAnnotatableElements();
					temp.codewordpanel.validate();
					temp.codewordpanel.repaint();
				}
			}
		}
		
		if (o == toolsitemextract || o == openmenuextractbutton) {
			opendialog.dispose();
			if(JFileChooser.APPROVE_OPTION == showSemGenFileChooser(currentdirectory, 
					new String[]{"owl", "mod", "xml", "sbml", "cellml"},
					"Extractor - Select source SemSim model", SemGenFileChooser.NEW_EXTRACTOR_TASK, false)){
				startNewExtractorTask(fc.getSelectedFile());
			}
		}

		if (o == extractoritematomicdecomp) {
			if (desktop.getSelectedComponent() instanceof ExtractorTab) {
				ExtractorTab extractor = (ExtractorTab) desktop.getSelectedComponent();
				extractor.atomicDecomposition();
			} 
			else {
				if(JFileChooser.APPROVE_OPTION == showSemGenFileChooser(currentdirectory,
							new String[]{"owl"}, "Extractor - Select source SemSim model", 
							SemGenFileChooser.ATOMIC_DECOMPOSITION_TASK, false)){
					startAtomicDecomposition(fc.getSelectedFile());
				}
			}
		}

		if (o == extractoritembatchcluster) {
			if (desktop.getSelectedComponent() instanceof ExtractorTab) {
				ExtractorTab extractor = (ExtractorTab) desktop.getSelectedComponent();
				try {
					extractor.batchCluster();
				} catch (IOException e1) {e1.printStackTrace();}
			} 
			else {
				if(JFileChooser.APPROVE_OPTION ==showSemGenFileChooser(currentdirectory, new String[] {
							"owl",".xml",".sbml",".mod",".cellml" },
							"Select a SemSim model for automated cluster analysis", 
							SemGenFileChooser.BATCH_CLUSTER_TASK, false)){
					startBatchClustering(fc.getSelectedFile());
				}
			}
		}

		if (o == extractoritemopenann) {
			if (desktop.getSelectedComponent() instanceof ExtractorTab) {
				ExtractorTab extractor = (ExtractorTab) desktop.getSelectedComponent();
				try {
					NewAnnotatorTask task = new NewAnnotatorTask(new File[]{extractor.sourcefile}, false);
					task.execute();
					//progframe = new ProgressFrame("Loading model for annotation...", true, task);
				} catch (Exception e1) {e1.printStackTrace();} 
			} else {
				JOptionPane.showMessageDialog(this,"Please first select an Extractor tab or open a new Extractor");
			}
		}

		if (o == toolsitemmerge || o == openmenumergebutton){
			NewMergerAction();
		}
		
		if (o == toolsitemcode || o == encodebutton) {
			opendialog.dispose();
			if (JFileChooser.APPROVE_OPTION == showSemGenFileChooser(
					currentdirectory, new String[]{"owl"}, "Select SemSim model to encode", 
					SemGenFileChooser.ENCODING_TASK, false)) {
				startEncoding(fc.getSelectedFile(), null);
			}
		}
			

		if (o == viewlog) {
			try {
				new LogViewer();
			} catch (FileNotFoundException k) {k.printStackTrace();}
		}

		if (o == upfont) 
			setUIFont(new javax.swing.plaf.FontUIResource("SansSerif",
					Font.PLAIN, SemGenGUI.defaultfontsize));

		if (o == helpitemabout) 
			AboutAction();

		if (o == helpitemweb) 
			BrowserLauncher.openURL(startsettingstable.get("helpURL")[0]);

		if (o instanceof TabCloser) {
			Component component = (Component) o;
			int i = desktop.indexOfComponent(component);
			desktop.remove(i);
		}
	}
	
	
	public static int showSemGenFileChooser(File currentdirectory, String[] fileextensions, String title, 
			int taskType, Boolean canselectmultiplefiles, Merger merger){
		fc = new SemGenFileChooser(taskType, merger);
		fc.setPreferredSize(filechooserdims);
		fc.setMultiSelectionEnabled(canselectmultiplefiles);
		fc.setCurrentDirectory(currentdirectory);
		if(fileextensions!=null) fc.addChoosableFileFilter(new FileFilter(fileextensions));
		fc.setDialogTitle(title);
		int returnVal = fc.showOpenDialog(desktop);
		if(returnVal==JFileChooser.APPROVE_OPTION){
			SemGenGUI.currentdirectory = fc.getCurrentDirectory();
		}
		return returnVal;
	}
	
	
	public static int showSemGenFileChooser(File currentdirectory, String[] fileextensions, String title, 
			int taskType, Boolean canselectmultiplefiles){
		return showSemGenFileChooser(currentdirectory, fileextensions, title, taskType, canselectmultiplefiles, null);
	}
	
	
	public static void startNewAnnotatorTask(File[] files){
		NewAnnotatorTask task = new NewAnnotatorTask(files, false);
		task.execute();
	}
	
	public static void startNewExtractorTask(File file){
		NewExtractorTask task = new NewExtractorTask(file);
		task.execute();
	}
	
	public static void startAtomicDecomposition(File file){
		ExtractorTab extractor = null;
		try {
			extractor = NewExtractorAction(file);
		} catch (OWLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		if (extractor != null) 
			extractor.atomicDecomposition();
	}
	
	public static void startBatchClustering(File file){
		ExtractorTab extractor;
		try {
			SemSimModel ssm = new SemSimOWLreader().readFromFile(file);
			extractor = new ExtractorTab(desktop, ssm, file);
			extractor.batchCluster();
		} catch (OWLException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (CloneNotSupportedException e3) {
			e3.printStackTrace();
		}
	}
	
	public static void startMergingTask(){
		
	}
	
	
	public static class NewAnnotatorTask extends SwingWorker<Void, Void> {
		public File[] files;
		public boolean autosave;
        public NewAnnotatorTask(File[] files, boolean autosave){
        	this.files = files;
        	this.autosave = autosave;
        }
        @Override
        public Void doInBackground(){
        	progframe = new ProgressFrame("Loading " + files[0].getName() + "...", true, this);
    		for(File file : files){
    			System.out.println("Loading " + file.getName());
    			progframe.updateMessage("Loading " + file.getName() + "...");
    			try{
    				AnnotateAction(file, autosave);
    			}
    			catch(Exception e){e.printStackTrace();}
    		}
            return null;
        }
        @Override
        public void done() {
        	progframe.setVisible(false);
        }
    }
	
	public static class NewExtractorTask extends SwingWorker<Void, Void> {
		public File file;
        public NewExtractorTask(File file){
        	this.file = file;
        }
        @Override
        public Void doInBackground() {
			progframe = new ProgressFrame("Loading model for extraction...", true, this);
        	try {
				NewExtractorAction(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
        @Override
        public void done() {
        	progframe.setVisible(false);
        }
    }
	
	public static class CoderTask extends SwingWorker<Void, Void> {
		public File inputfile;
		public File outputfile;
		public Writer writer;
		public SemSimModel model;
        
		public CoderTask(File inputfile, File outputfile, Writer writer){
        	this.inputfile = inputfile;
        	this.outputfile = outputfile;
        	this.writer = writer;
        }
		public CoderTask(SemSimModel model, File outputfile, Writer writer){
			this.model = model;
        	this.outputfile = outputfile;
        	this.writer = writer;
		}
        @Override
        public Void doInBackground() {
        	
    		if(model == null){
        		model = loadSemSimModelFromFile(inputfile, true);
    			if(!model.getErrors().isEmpty()){
    				JOptionPane.showMessageDialog(desktop, "Selected model had errors:", "Could not encode model", JOptionPane.ERROR_MESSAGE);
    				return null;
    			}
    		}
			CoderAction(model, outputfile, writer);
            return null;
        }
        @Override
        public void done() {
        	progframe.setVisible(false);
        }
    }
	

	public static Annotator AnnotateAction(File file, Boolean autosave) {
		
		SemSimModel semsimmodel = loadSemSimModelFromFile(file, true);
		
		Annotator annotator = null;
		URI selectedURI = file.toURI(); // The selected file for annotation
		URI existingURI = URI.create(""); // The existing file from which to
											// load the SemSim framework
		Boolean newannok = true;

		// Create a new tempfile using the date
		datenow = new Date();
		File tempfile = new File(tempdir.getAbsoluteFile() + "/" + sdf.format(datenow) + ".owl");
		URI tempURI = tempfile.toURI();
		URI saveURI = null;

		if(semsimmodel!=null){
			// If we are annotating an existing SemSim or CellML file...
			if (semsimmodel.getSourceModelType()==ModelClassifier.SEMSIM_MODEL || semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL) {
				existingURI = selectedURI;
				saveURI = selectedURI;
				newannok = true;
			}
			
			// Check to make sure SemSim model isn't already being annotated before proceeding
			if (newannok && !isOntologyOpenForEditing(existingURI)) {
	
				// Create new Annotater object in SemGen desktop
				annotator = new Annotator(file, tempURI, saveURI, entityKBlist, propertyKBlist, depKBlist, allKBlist);
				annotator.semsimmodel = semsimmodel; 
				
				if(annotator.semsimmodel.getErrors().isEmpty()){
					annotator.setModelSaved(annotator.semsimmodel.getSourceModelType()==ModelClassifier.SEMSIM_MODEL ||
							annotator.semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL);
					
					if(annotator.getModelSaved()) annotator.lastSavedAs = annotator.semsimmodel.getSourceModelType();
					
					// Add unspecified physical model components for use during annotation
					annotator.semsimmodel.addCustomPhysicalEntity(unspecifiedName, "Non-specific entity for use as a placeholder during annotation");
					annotator.semsimmodel.addCustomPhysicalProcess(unspecifiedName, "Non-specific process for use as a placeholder during annotation");
	
					if(annotator.semsimmodel!=null){
						numtabs++;
						desktop.addTab(formatTabName(file.getName()), annotatoricon, annotator, "Annotating " + file.getName());
						desktop.setTabComponentAt(numtabs-1, new SemGenTabComponent(formatTabName(file.getName()), annotator));
						
						annotator.NewAnnotatorAction();
						
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
						   public void run() { 
							   desktop.setSelectedComponent(desktop.getComponentAt(numtabs - 1));
							   desktop.getComponentAt(numtabs - 1).repaint();
							   desktop.getComponentAt(numtabs - 1).validate();
						   }
						});
	//						
						if(!autosave){
							annotateitemaddrefterm.setEnabled(true);
							annotateitemremoverefterm.setEnabled(true);
						}
					}
				}
			}
		}
		return annotator;
	}
	
	
	// Make this into task
	private static ExtractorTab NewExtractorAction(File file) throws OWLException, IOException, InterruptedException, JDOMException, ServiceException {
		ExtractorTab extractor = null;
		SemSimModel semsimmodel = loadSemSimModelFromFile(file, true);
		if(ModelClassifier.classify(file)==ModelClassifier.CELLML_MODEL || semsimmodel.getFunctionalSubmodels().size()>0){
			JOptionPane.showMessageDialog(desktop, "Sorry. Extraction of models with CellML-type components not yet supported.");
			return null;
		}
		if(semsimmodel!=null){
			extractor = new ExtractorTab(desktop, semsimmodel, file);
			numtabs++;
			desktop.addTab(formatTabName(extractor.semsimmodel.getName()),
					extractoricon, extractor,
					"Extracting from " + file.getName());
			desktop.setTabComponentAt(numtabs-1, new SemGenTabComponent(formatTabName(file.getName()), extractor));
			desktop.setMnemonicAt(0, KeyEvent.VK_1);
			desktop.setSelectedComponent(desktop.getComponentAt(numtabs - 1));
		}
		return extractor;
	}
	
	

	public void NewMergerAction(){
		opendialog.setVisible(false);
		Merger merger = new Merger(desktop);
		numtabs++;
		desktop.addTab("Merger", mergeicon, merger, "Tab for merging SemSim models");
		desktop.setTabComponentAt(numtabs-1, new SemGenTabComponent("Merger", merger));

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
				   desktop.setSelectedComponent(desktop.getComponentAt(numtabs - 1));
			   }});
		merger.PlusButtonAction();
	}
	
	
	public static SemSimModel loadSemSimModelFromFile(File file, Boolean testifonline) {
		
		SemSimModel semsimmodel = null;
		int modeltype = ModelClassifier.classify(file);
		
		Boolean autoannotatesbml = false;
		autoannotatesbml = (modeltype==ModelClassifier.SBML_MODEL && SemGenGUI.autoannotate.isSelected());
		
		try {
			switch (modeltype){
			
			case ModelClassifier.MML_MODEL:
					semsimmodel = new MMLreader(JSimBuildDir).readFromFile(file);
					if(semsimmodel.getErrors().isEmpty() && SemGenGUI.autoannotate.isSelected())
						semsimmodel = autoAnnotateWithOPB(semsimmodel);
				break;
					
			case ModelClassifier.SBML_MODEL:// MML
					semsimmodel = new MMLreader(JSimBuildDir).readFromFile(file);
					if(semsimmodel.getErrors().isEmpty() && autoannotatesbml){
						// If it's an SBML model and we should auto-annotate
						semsimmodel = autoAnnotateWithOPB(semsimmodel);
						if(progframe!=null) 
							progframe.updateMessage("Annotating with web services...");
						boolean online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
						if(!online) 
							SemGenGUI.showWebConnectionError("BioPortal search service");
						SBMLAnnotator.annotate(file, semsimmodel, online, ontologyTermsAndNamesCache);
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, ontologyTermsAndNamesCache, online);
						SBMLAnnotator.setFreeTextDefinitionsForDataStructuresAndSubmodels(semsimmodel);
						progframe.requestFocusInWindow();
					}
				break;
				
			case ModelClassifier.CELLML_MODEL:
				
				semsimmodel = new CellMLreader().readFromFile(file);
				if(semsimmodel.getErrors().isEmpty()){
					if(SemGenGUI.autoannotate.isSelected()){
						semsimmodel = autoAnnotateWithOPB(semsimmodel);
						if(progframe!=null) progframe.updateMessage("Annotating " + file.getName() + " with web services...");
						Boolean online = true;
						if(testifonline){
							online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
							if(!online) SemGenGUI.showWebConnectionError("BioPortal search service");
						}
						if(progframe!=null) progframe.requestFocusInWindow();
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, ontologyTermsAndNamesCache, online);
					}
				}
				break;
				
			case ModelClassifier.SEMSIM_MODEL:
				semsimmodel = new SemSimOWLreader().readFromFile(file);
				break;
				
			case -1:
				Component comp = (progframe!=null) ? progframe : desktop;
				JOptionPane.showMessageDialog(comp, "SemGen did not recognize the file type for " + file.getName(),
						"Error: Unrecognized model format", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		catch(Exception e){e.printStackTrace();}
		
		if(semsimmodel!=null){
			if(!semsimmodel.getErrors().isEmpty()){
				if(progframe!=null) progframe.setVisible(false);
				String errormsg = "";
				for(String catstr : semsimmodel.getErrors())
					errormsg = errormsg + catstr + "\n";
				JOptionPane.showMessageDialog(desktop, errormsg, "ERROR", JOptionPane.ERROR_MESSAGE);
				return semsimmodel;
			}
			semsimmodel.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			semsimmodel.setSourceModelType(modeltype);				
		}
		return semsimmodel;
	}
	
	// Automatically apply OPB annotations to the physical properties associated
	// with the model's data structures
	public static SemSimModel autoAnnotateWithOPB(SemSimModel semsimmodel) {
		
		Set<DataStructure> candidateamounts = new HashSet<DataStructure>();
		Set<DataStructure> candidateforces = new HashSet<DataStructure>();
		Set<DataStructure> candidateflows = new HashSet<DataStructure>();
		
		// If units present, set up physical property connected to each data structure
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(ds.hasUnits()){
				ReferenceOntologyAnnotation roa = SemGenGUI.getOPBAnnotationFromPhysicalUnit(ds);
				if(roa!=null){

					// If the codeword represents an OPB:Amount property (OPB_00135)
					if(OPBamountProperties.contains(roa.getReferenceURI().toString()))
						candidateamounts.add(ds);
					// If the codeword represents an OPB:Force property (OPB_00574)
					else if(OPBforceProperties.contains(roa.getReferenceURI().toString()))
						candidateforces.add(ds);
					// If the codeword represents an OPB:Flow rate property (OPB_00573)
					else if(OPBflowProperties.contains(roa.getReferenceURI().toString())){
						candidateflows.add(ds);
					}
				}
			}
		}
		// ID the amounts
		Set<DataStructure> unconfirmedamounts = new HashSet<DataStructure>();
		Set<DataStructure> confirmedamounts = new HashSet<DataStructure>();
		for(DataStructure camount : candidateamounts){
			Boolean hasinitval = camount.hasStartValue();
			if((camount instanceof MappableVariable)) hasinitval = (((MappableVariable)camount).getCellMLinitialValue()!=null);
			if(hasinitval && !camount.isDiscrete() 
					&& !camount.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = getOPBAnnotationFromPhysicalUnit(camount);
				camount.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				confirmedamounts.add(camount);
			}
			else unconfirmedamounts.add(camount);
		}
		// second pass at amounts
		Set<DataStructure> temp = new HashSet<DataStructure>();
		temp.addAll(confirmedamounts);
		for(DataStructure camount : temp){
			for(DataStructure newcamount : getDownstreamDataStructures(unconfirmedamounts, camount, camount)){
				confirmedamounts.add(newcamount);
				ReferenceOntologyAnnotation roa = getOPBAnnotationFromPhysicalUnit(newcamount);
				if(!newcamount.getPhysicalProperty().hasRefersToAnnotation())
					newcamount.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
			}
		}
		// ID the forces
		Set<DataStructure> unconfirmedforces = new HashSet<DataStructure>();
		Set<DataStructure> confirmedforces = new HashSet<DataStructure>();
		for(DataStructure cforce : candidateforces){
			Boolean annotate = false;
			// If the candidate force is solved using a confirmed amount, annotate it
			if(cforce.getComputation()!=null){
				for(DataStructure cforceinput : cforce.getComputation().getInputs()){
					if(confirmedamounts.contains(cforceinput)){ annotate=true; break;}
				}
			}
			// If already decided to annotate, or the candidate is solved with an ODE and it's not a discrete variable, annotate it
			if((cforce.hasStartValue() || annotate) && !cforce.isDiscrete() 
					&& !cforce.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = getOPBAnnotationFromPhysicalUnit(cforce);
				cforce.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				confirmedforces.add(cforce);
			}
			else unconfirmedforces.add(cforce);
		}
		
		// Second pass at forces
		temp.clear();
		temp.addAll(confirmedforces);
		for(DataStructure cforce : temp){
			for(DataStructure newcforce : getDownstreamDataStructures(unconfirmedforces, cforce, cforce)){
				confirmedforces.add(newcforce);
				if(!newcforce.getPhysicalProperty().hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = getOPBAnnotationFromPhysicalUnit(newcforce);
					newcforce.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				}
			}
		}
		
		// ID the flows
		Set<DataStructure> unconfirmedflows = new HashSet<DataStructure>();
		Set<DataStructure> confirmedflows = new HashSet<DataStructure>();
		for(DataStructure cflow : candidateflows){
			Boolean annotate = false;
			// If the candidate flow is solved using a confirmed amount or force, annotate it
			if(cflow.getComputation()!=null){
				for(DataStructure cflowinput : cflow.getComputation().getInputs()){
					if(confirmedamounts.contains(cflowinput) || confirmedforces.contains(cflowinput)){ annotate=true; break;}
				}
			}
			// If already decided to annotate, or the candidate is solved with an ODE and it's not a discrete variable, annotate it
			if((cflow.hasStartValue() || annotate || cflow.getName().contains(":")) && !cflow.isDiscrete()
					&& !cflow.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = getOPBAnnotationFromPhysicalUnit(cflow);
				cflow.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				confirmedflows.add(cflow);
			}
			else unconfirmedflows.add(cflow);
		}
		// Second pass at flows
		temp.clear();
		temp.addAll(confirmedflows);
		for(DataStructure cflow : temp){
			for(DataStructure newcflow : getDownstreamDataStructures(unconfirmedflows, cflow, cflow)){
				confirmedforces.add(newcflow);
				if(!newcflow.getPhysicalProperty().hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = getOPBAnnotationFromPhysicalUnit(newcflow);
					newcflow.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				}
			}
		}
		return semsimmodel;
	}
	
	public static Set<DataStructure> getDownstreamDataStructures(Set<DataStructure> candidates, DataStructure mainroot, DataStructure curroot){
		
		// traverse all nodes that belong to the parent
		Set<DataStructure> newamounts = new HashSet<DataStructure>();
		for(DataStructure downstreamds : curroot.getUsedToCompute()){
			if(candidates.contains(downstreamds) && !newamounts.contains(downstreamds) && downstreamds!=mainroot && downstreamds!=curroot){
				newamounts.add(downstreamds);
				newamounts.addAll(getDownstreamDataStructures(newamounts, mainroot, downstreamds));
			}
		}
		return newamounts;
	}
	
	public static ReferenceOntologyAnnotation getOPBAnnotationFromPhysicalUnit(DataStructure ds){
		ReferenceOntologyAnnotation roa = null;
		String[] candidateOPBclasses = (String[]) SemGenGUI.OPBClassesForUnitsTable.get(ds.getUnit().getName());
		if (candidateOPBclasses != null && candidateOPBclasses.length == 1) {
			OWLClass cls = factory.getOWLClass(IRI.create(SemSimConstants.OPB_NAMESPACE + candidateOPBclasses[0]));
			String OPBpropname = SemSimOWLFactory.getRDFLabels(SemGenGUI.OPB, cls)[0];
			roa = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, cls.getIRI().toURI(), OPBpropname);
		}
		return roa;
	}
	
	public static int getPropertyType(DataStructure ds){
		if(ds.hasPhysicalProperty()){
			// If there's already an OPB reference annotation
			if(ds.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = (ds.getPhysicalProperty().getFirstRefersToReferenceOntologyAnnotation());
				
				if(SemGenGUI.OPBstateProperties.contains(roa.getReferenceURI().toString()) ||
						SemGenGUI.OPBforceProperties.contains(roa.getReferenceURI().toString())){
					return SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY;
				}
				else if(SemGenGUI.OPBprocessProperties.contains(roa.getReferenceURI().toString())){
					return SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS;
				}
				else return SemSimConstants.UNKNOWN_PROPERTY_TYPE;
			}
			// Otherwise, see if there is already an entity or process associated with the codeword
			else if(ds.getPhysicalProperty().getPhysicalPropertyOf() instanceof PhysicalEntity){
				return SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY;
			}
			else if(ds.getPhysicalProperty().getPhysicalPropertyOf() instanceof PhysicalProcess){
				return SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS;
			}
			else return SemSimConstants.UNKNOWN_PROPERTY_TYPE;
		}
		else return SemSimConstants.UNKNOWN_PROPERTY_TYPE;
	}
	
	
	public static String formatTabName(String filename){
		String tabname = filename; //filename.replace(".owl", "");
		if(tabname.length()>=30)
			tabname = tabname.substring(0, 30) + "...";
		return tabname;
	}
	
	public static String getBioPortalIDfromTermURI(String termuri){
		if(SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(SemSimOWLFactory.getNamespaceFromIRI(termuri.toString()))){
			String fullname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(SemSimOWLFactory.getNamespaceFromIRI(termuri.toString()));
			if(BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.containsKey(fullname)){
				return BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.get(fullname);
			}
		}
		return null;
	}
	
	
	public static void startEncoding(Object inputfileormodel, String filenamesuggestion){
		
		File outputfile = null;
		Object[] optionsarray = new Object[] {"CellML", "MML (JSim)"};
		Object selection = JOptionPane.showInputDialog(desktop, "Select output format", "SemGen coder", JOptionPane.PLAIN_MESSAGE, null, optionsarray, "CellML");
		
		if(filenamesuggestion!=null && filenamesuggestion.contains(".")) filenamesuggestion = filenamesuggestion.substring(0, filenamesuggestion.lastIndexOf("."));
		
		Writer outwriter = null;
		if(selection == optionsarray[0]){
			outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, new FileNameExtensionFilter[]{cellmlfilter});
			outwriter = (Writer)new CellMLwriter();
		}
		
		if(selection == optionsarray[1]){
			outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, new FileNameExtensionFilter[]{mmlfilter});
			outwriter = (Writer)new MMLwriter();
		}
//			if(selection == optionsarray[1]){
//				File outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, "m");
//				//MatlabCoder.translate(model, outputfile);
//			}
//			else if(selection == optionsarray[2]){
//				File outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, "mod");
//				MMLwriter.translate(model, outputfile);
//			}
//			else if(selection == optionsarray[0]){
//				File outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, "xml");
//				ChalkboardCoder.translate(model, outputfile);
//			}
//			else if(selection == optionsarray[3]){
//				File outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, "rdf");
//				PhysioMapWriter.translate(model, outputfile);
//			}
//			else if(selection == optionsarray[4]){
//				File outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, "rdf");
//				SemSimRDFwriter.translate(model.toOWLOntology(), outputfile);
//			}
		if(outputfile!=null){
			CoderTask task = null;
			if(inputfileormodel instanceof File){
				task = new CoderTask((File)inputfileormodel, outputfile, outwriter);
			}
			else if(inputfileormodel instanceof SemSimModel){
				task = new CoderTask((SemSimModel)inputfileormodel, outputfile, outwriter);
			}
			task.execute();
			progframe = new ProgressFrame("Encoding...", true, task);
		}
	}

	
	
	public static void CoderAction(SemSimModel model, File outputfile, Writer writer){
		progframe.updateMessage("Encoding...");
		String content = writer.writeToString(model);
		if(content!=null)
			SemSimUtil.writeStringToFile(content, outputfile);
		else
			JOptionPane.showMessageDialog(desktop, "Sorry. There was a problem encoding " + model.getName() + 
					"\nThe JSim API threw an exception.",  
					"Error", JOptionPane.ERROR_MESSAGE);
	}
	
	
	
	private void OpenFileAction(){
		opendialog = new JDialog();
		//opendialog.setBackground(Color.white);
		opendialog.setTitle("OPEN: Select action");
		
		annotatebutton = new JButton("Annotate a model");
		annotatebutton.setIcon(annotatoricon);
		annotatebutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		annotatebutton.addActionListener(this);
		annotatebutton.setAlignmentX(JButton.CENTER_ALIGNMENT);


		openmenuextractbutton = new JButton("Extract a model");
		openmenuextractbutton.setEnabled(true);
		openmenuextractbutton.setIcon(extractoricon);
		openmenuextractbutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		openmenuextractbutton.addActionListener(this);
		openmenuextractbutton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		
		openmenumergebutton = new JButton("Merge models");
		openmenumergebutton.setEnabled(true);
		openmenumergebutton.setIcon(mergeicon);
		openmenumergebutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		openmenumergebutton.addActionListener(this);
		openmenumergebutton.setAlignmentX(JButton.CENTER_ALIGNMENT);

		encodebutton = new JButton("Encode a model");
		encodebutton.setEnabled(true);
		encodebutton.setIcon(codericon);
		encodebutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		encodebutton.addActionListener(this);
		encodebutton.setAlignmentX(JButton.CENTER_ALIGNMENT);

		JPanel openpanel = new JPanel();
		openpanel.setLayout(new BoxLayout(openpanel, BoxLayout.Y_AXIS));
		openpanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		//openpanel.setBackground(Color.white);
		openpanel.add(annotatebutton);
		openpanel.add(openmenuextractbutton);
		openpanel.add(openmenumergebutton);
		openpanel.add(encodebutton);
		openpanel.setPreferredSize(new Dimension(250,135));
		openpanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		
		JOptionPane selectopentype = new JOptionPane(openpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
		//selectopentype.setBackground(Color.white);
		//Object[] optionsarray = new Object[] {"Cancel"};
		selectopentype.setOptions(new Object[]{});
		//selectopentype.setInitialValue("Cancel");
		opendialog.setContentPane(selectopentype);
		opendialog.setModal(true);
		opendialog.pack();
		opendialog.setLocationRelativeTo(this);
		opendialog.setVisible(true);
	}
	

	// SAVE ACTION 
	public static boolean SaveAction(Object object, int modeltype){
		boolean success = false;
		if (object instanceof Annotator) {
			Annotator ann = (Annotator) object;
			if(ann.fileURI!=null){
				Set<DataStructure> unspecds = new HashSet<DataStructure>();

				unspecds.addAll(getDataStructuresWithUnspecifiedAnnotations(ann.semsimmodel));
				if(unspecds.isEmpty()){
					File targetfile = new File(ann.fileURI);
					try {
						ann.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						
						if(modeltype==ModelClassifier.SEMSIM_MODEL)
							ann.manager.saveOntology(ann.semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(ann.fileURI));
						else if(modeltype==ModelClassifier.CELLML_MODEL){
							File outputfile =  new File(ann.fileURI);
							String content = new CellMLwriter().writeToString(ann.semsimmodel);
							SemSimUtil.writeStringToFile(content, outputfile);
						}
						
						ann.lastSavedAs = modeltype;
						ann.setModelSaved(true);
						ann.sourcefile = targetfile;
					} catch (Exception e) {e.printStackTrace();}
					
					if(desktop.getComponentCount()>0){
						desktop.setTitleAt(desktop.indexOfComponent(ann), formatTabName(targetfile.getName()));
						desktop.setTabComponentAt(desktop.indexOfComponent(ann),
								new SemGenTabComponent(formatTabName(targetfile.getName()), ann));
						ann.semsimmodel.setName(targetfile.getName().substring(0, targetfile.getName().lastIndexOf(".")));
						desktop.setToolTipTextAt(desktop.indexOfComponent(ann), "Annotating " + targetfile.getName());
					}
					logfilewriter.println(targetfile.getName() + " was saved");
					ann.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					success = true;
				}
				else{
					showUnspecifiedAnnotationError(unspecds);
					success = false;
				}
			}
			else{
				String path = ann.sourcefile.getAbsolutePath();
				path = path.substring(0, path.lastIndexOf("."));
				success = SaveAsAction(ann, path, new FileNameExtensionFilter[]{cellmlfilter, owlfilter})!=null;
			}
		}
		if(object instanceof CSVExporter){
			CSVExporter exp = (CSVExporter) object;
			Scanner scanner = new Scanner(exp.datatosave);
			PrintWriter outfile;
			try {
				outfile = new PrintWriter(new FileWriter(new File(exp.savelocation)));
				while (scanner.hasNextLine()) {
					String nextline = scanner.nextLine();
					outfile.println(nextline);
				}
				outfile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			scanner.close();
			JOptionPane.showMessageDialog(desktop, "Finished exporting .csv file");
			success = true;
		}

		if (object instanceof ExtractorTab) {}
		return success;
	}

	
	public static File SaveAsAction(Object object, String selectedfilepath, FileNameExtensionFilter[] filters){
		JFileChooser filec = new JFileChooser();
		File file = null;
		Boolean saveok = false;
		while (!saveok) {
//			if(selectedfilepath!=null && !selectedfilepath.equals("")){
//				filec.setSelectedFile(new File(selectedfilepath + "." + extensions[0]));
//			}
			
			filec.setCurrentDirectory(currentdirectory);
			filec.setDialogTitle("Choose location to save file");
			filec.setPreferredSize(filechooserdims);
			
			filec.setAcceptAllFileFilterUsed(false);
			
			for(FileNameExtensionFilter filter : filters) filec.addChoosableFileFilter(filter);
			
			int returnVal = filec.showSaveDialog(desktop);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = new File(filec.getSelectedFile().getAbsolutePath());
				currentdirectory = filec.getCurrentDirectory();
				
				String extension = null;
				int modeltype = -1;
				
				if(filec.getFileFilter()==owlfilter){
					extension = "owl";
					modeltype = ModelClassifier.SEMSIM_MODEL;
				}
				if(filec.getFileFilter()==cellmlfilter){
					extension = "cellml";
					modeltype = ModelClassifier.CELLML_MODEL;
				}
				if(filec.getFileFilter()==mmlfilter){
					extension = "mod";
					modeltype = ModelClassifier.MML_MODEL;
				}
				
				// If there's an extension for the file type, make sure the filename ends in it
				if(extension!=null){
					if (!file.getAbsolutePath().endsWith("." + extension.toLowerCase())
							&& !file.getAbsolutePath().endsWith("." + extension.toUpperCase())) {
						file = new File(filec.getSelectedFile().getAbsolutePath() + "." + extension);
					} 
				}
				if (file.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(desktop,
							"Overwrite existing file?", file.getName() + " already exists",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (overwriteval == JOptionPane.OK_OPTION) saveok = true;
					else {
						file = null;
						saveok = false;
					}
				} 
				else saveok = true;

				if (object instanceof Annotator && saveok == true) {
					Annotator ann = (Annotator) desktop.getSelectedComponent();
					Set<DataStructure> unspecds = getDataStructuresWithUnspecifiedAnnotations(ann.semsimmodel);
					if(unspecds.isEmpty()){
						ann.fileURI = file.toURI();
						SaveAction(ann, modeltype);
					}
					else{
						showUnspecifiedAnnotationError(unspecds);
					}

				}
				else if(object instanceof CSVExporter && saveok == true){
					CSVExporter exp = (CSVExporter) object;
					exp.savelocation = file.getAbsolutePath();
					SaveAction(exp, -1);
				}
			} 
			else {
				saveok = true;
				file = null;
			}
		}
		return file;
	}
	
	
	public static Set<DataStructure> getDataStructuresWithUnspecifiedAnnotations(SemSimModel model){
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		for(DataStructure ds : model.getDataStructures()){
			if(ds.hasPhysicalProperty()){
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null){
					if(ds.getPhysicalProperty().getPhysicalPropertyOf().getName().equals(unspecifiedName)){
						dsset.add(ds);
					}
					if(ds.getPhysicalProperty().getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
						for(PhysicalEntity pe : ((CompositePhysicalEntity)ds.getPhysicalProperty().getPhysicalPropertyOf()).getArrayListOfEntities()){
							if(pe.getName().equals(unspecifiedName))
								dsset.add(ds);
						}
					}
				}
			}
			else System.out.println(ds.getName() + " didn't have a physical property");
		}
		return dsset;
	}
	
	public static void showUnspecifiedAnnotationError(Set<DataStructure> unspecds){
		String listofds = "";
		for(DataStructure ds : unspecds){
			listofds = listofds + ds.getName() + "\n";
		}
		JOptionPane.showMessageDialog(desktop, "Please first remove unspecified annotations for the following codewords:\n" + listofds);
		
	}
	
	
	public static Boolean isOntologyOpenForEditing(URI uritocheck) {
		Boolean open = false;
		for (int t = 0; t < desktop.getComponents().length; t++) {
			if (desktop.getComponent(t) instanceof Annotator) {
				Annotator tempann = (Annotator) desktop.getComponent(t);
				if(tempann.fileURI!=null){
					if (uritocheck.toString().equals(tempann.fileURI.toString())) {
						open = true;
						JOptionPane.showMessageDialog(null,"Cannot create or load \n"+ uritocheck.toString()+
								"\n because the file is already open for editing.",null, JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
		}
		return open;
	}
	
	public static void showWebConnectionError(String location){
		JOptionPane.showMessageDialog(desktop,
				"Please make sure you are online, otherwise the website or service \n" + 
				"may be experiencing difficulties.", "Error connecting to " + location, JOptionPane.ERROR_MESSAGE);
	}
	

	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)UIManager.put(key, f);}
	}

	public boolean AboutAction() {
		String COPYRIGHT = "\u00a9";
		JOptionPane.showMessageDialog(null, "SemGen\nVersion " + version + "\n"
						+ COPYRIGHT
						+ "2010-2014\nMaxwell Lewis Neal\n", "About SemGen",
						JOptionPane.PLAIN_MESSAGE);
		return true;
	}
	
	
	public static void scrollToTop(final JScrollPane scroller){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		   public void run() { 
		       scroller.getVerticalScrollBar().setValue(0);
		   }
		});
	}
	
	public static void scrollToLeft(final JScrollPane scroller){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
			       scroller.getHorizontalScrollBar().setValue(0);
			   }
			});
	}
	
	public static void scrollToComponent(final JComponent component, final Component cp){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
			       Rectangle rc = cp.getBounds();
				   component.scrollRectToVisible(rc);
			   }
			});
	}
	
	
	// WHEN THE CLOSE TAB ACTION IS PERFORMED ON AN annotator, MERGER OR EXTRACTOR FRAME
	public static int closeTabAction(Component component) {
		int returnval = -1;
		int i = SemGenGUI.desktop.indexOfComponent(component);
		// If the file has been altered, prompt for a save
		if(component instanceof Annotator){
			Annotator ann = (Annotator)component;
			if (ann.getModelSaved()==false) {
				String title = "[unsaved file]";
				if(ann.fileURI!=null){
					title =  new File(ann.fileURI).getName();
				}
				int savefilechoice = JOptionPane.showConfirmDialog(SemGenGUI.desktop,
						"Save changes before closing?", title,
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				returnval = savefilechoice;
				if (savefilechoice == JOptionPane.YES_OPTION) {
					if(SaveAction(component, ann.lastSavedAs)){
						desktop.remove(i);
						numtabs = numtabs - 1;
					}
					else{
						return JOptionPane.CANCEL_OPTION;
					}
				} 
				else if (savefilechoice == JOptionPane.NO_OPTION) {
					desktop.remove(i);
					numtabs = numtabs - 1;
				} 
				else if (savefilechoice == JOptionPane.CANCEL_OPTION){
					return savefilechoice;
				}
			} 
			else {
				desktop.remove(i);
				numtabs = numtabs - 1;
			}
			System.gc();
		}
		else if (component instanceof ExtractorTab || component instanceof Merger){
			desktop.remove(desktop.indexOfComponent(component));
			numtabs = numtabs - 1;
		}
		return returnval;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = SemGenGUI.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	// Generic registration with the Mac OS X application menu
	// Checks the platform, then attempts to register with the Apple EAWT
	// See OSXAdapter.java to see how this is done without directly referencing
	// any Apple APIs
	public void registerForMacOSXEvents() {
		if (MACOSX) {
			try {
				// Generate and register the OSXAdapter, passing it a hash of
				// all the methods we wish to
				// use as delegates for various
				// com.apple.eawt.ApplicationListener methods
				OSXAdapter.setQuitHandler(this,
						getClass().getDeclaredMethod("quit", (Class[]) null));

				OSXAdapter.setAboutHandler(this,
				 getClass().getDeclaredMethod("AboutAction", (Class[])null));
				// OSXAdapter.setPreferencesHandler(this,
				// getClass().getDeclaredMethod("preferences", (Class[])null));
				// OSXAdapter.setFileHandler(this,
				// getClass().getDeclaredMethod("loadImageFile", new Class[] {
				// String.class }));
				
			} catch (Exception e) {
				System.err.println("Error while loading the OSXAdapter:");
				e.printStackTrace();
			}
		}
	}
	

	public static boolean quit() throws HeadlessException, OWLException {
		Component[] desktopcomponents = desktop.getComponents();
		Boolean quit = true;
		Boolean contchecking = true;
		for (int x = 0; x < desktopcomponents.length; x++) {
			if (desktopcomponents[x] instanceof Annotator && contchecking) {
				Annotator temp = (Annotator) desktopcomponents[x];
				desktop.setSelectedComponent(temp);
				int val = 0;
				val = SemGenGUI.closeTabAction(temp);
				if (val == JOptionPane.CANCEL_OPTION) {
					contchecking = false;
					quit = false;
				}
			}
//			if (desktopcomponents[x] instanceof Extractor) {
//				Extractor temp = (Extractor) desktopcomponents[x];
//				desktop.setSelectedComponent(temp);
//				try {
//					OWLMethods.closeTabAction(temp, temp.sourcefile.toURI(), temp.sourcefile.toURI());
//				} catch (IOException ioe) {
//					ioe.printStackTrace();
//				}
//			}
		}
		if(quit){
			try {
				storeGUIsettings();
				storeCachedOntologyTerms();
				System.exit(0);
			} 
			catch (URISyntaxException e) {e.printStackTrace();}
			//deleteTempFiles();
		}
		return quit;
	}


	public static void storeGUIsettings() throws URISyntaxException {
		int endx = frame.getLocation().x;
		int endy = frame.getLocation().y;
		int endheight = frame.getHeight();
		int endwidth = frame.getWidth();
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(new File("cfg/startSettings.txt")));
			writer.println("XstartPosition; " + endx);
			writer.println("YstartPosition; " + endy);
			writer.println("startHeight; " + endheight);
			writer.println("startWidth; " + endwidth);
			writer.println("startDirectory; " + currentdirectory.getAbsolutePath());
			writer.println("autoAnnotate; " + autoannotate.isSelected());
			writer.println("showImports; " + SemGenGUI.annotateitemshowimports.isSelected());
			writer.println("displayMarkers; " + annotateitemshowmarkers.isSelected());
			writer.println("organizeByPropertyType; " + annotateitemsortbytype.isSelected());
			writer.println("treeView; " + annotateitemtreeview.isSelected());
			writer.println("helpURL; " + startsettingstable.get("helpURL")[0]);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void storeCachedOntologyTerms(){
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(ontologyTermsAndNamesCacheFile));
			for(String key : ontologyTermsAndNamesCache.keySet()){
				writer.println(key + "; " + ontologyTermsAndNamesCache.get(key)[0]);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stateChanged(ChangeEvent arg0) {
		updateSemGenMenuOptions();
	}
	
	public void updateSemGenMenuOptions(){
		Component comp = desktop.getSelectedComponent();
		
		annotateitemharvestfromtext.setEnabled(comp instanceof Annotator);
		annotateitemaddrefterm.setEnabled(comp instanceof Annotator);
		annotateitemremoverefterm.setEnabled(comp instanceof Annotator);
		annotateitemcopy.setEnabled(comp instanceof Annotator);
		annotateitemreplacerefterm.setEnabled(comp instanceof Annotator);
		annotateitemchangesourcemodelcode.setEnabled(comp instanceof Annotator);
		annotateitemexportcsv.setEnabled(comp instanceof Annotator);
		annotateitemeditmodelanns.setEnabled(comp instanceof Annotator);
		annotateitemfindnext.setEnabled(comp instanceof Annotator);
		
		extractoritembatchcluster.setEnabled(comp instanceof ExtractorTab);
		extractoritemopenann.setEnabled(comp instanceof ExtractorTab);
		
		if(comp instanceof Annotator){
			Annotator ann = (Annotator)comp;
			fileitemsave.setEnabled(!ann.getModelSaved());
		}
		else fileitemsave.setEnabled(false);
		fileitemsaveas.setEnabled(comp instanceof Annotator);
	}
}
