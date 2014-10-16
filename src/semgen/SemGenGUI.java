package semgen;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.annotation.AddReferenceClassDialog;
import semgen.annotation.AnnotationComponentReplacer;
import semgen.annotation.AnnotationCopier;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.BatchCellML;
import semgen.annotation.CodewordButton;
import semgen.annotation.LegacyCodeChooser;
import semgen.annotation.ModelLevelMetadataEditor;
import semgen.annotation.RemovePhysicalComponentDialog;
import semgen.annotation.SemanticSummaryDialog;
import semgen.annotation.TextMinerDialog;
import semgen.extraction.ExtractorTab;
import semgen.merging.MergerTab;
import semgen.resource.BrowserLauncher;
import semgen.resource.CSVExporter;
import semgen.resource.LogViewer;
import semgen.resource.SemGenError;
import semgen.resource.SemGenIcon; 
import semgen.resource.uicomponent.ProgressFrame;
import semgen.resource.uicomponent.SemGenTab;
import semsim.SemSimConstants;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

import javax.xml.rpc.ServiceException;

public class SemGenGUI extends JTabbedPane implements ActionListener, MenuListener, ChangeListener {

	private static final long serialVersionUID = 3618439495848469800L;
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory factory;
	public static OWLOntology OPB;

	private SemGenSettings settings;
	private static SemGenSettings settingshandle; //Temporary work around for static functions
	
	public static Color lightblue = new Color(207, 215, 252, 255);
	
	public static Dimension filechooserdims = new Dimension(550,550);
	
	public static File currentdirectory = new File("");
	public JDialog opendialog;
	public JButton annotatebutton;
	public JButton openmenuextractbutton;
	public JButton openmenumergebutton;
	public JButton encodebutton;

	public static String JSimBuildDir = "./jsimhome";
	private JMenuBar menubar;
	private JMenuItem fileitemopen;
	private JMenuItem fileitemsave;
	private JMenuItem fileitemsaveas;
	public JMenuItem fileitemclose;
	public JMenuItem fileitemexit;
	public static JMenuItem semanticsummary;
	public static JMenuItem annotateitemfindnext;
	private JMenuItem toolsitemannotate;
	private static JMenuItem annotateitemaddrefterm;
	private static JMenuItem annotateitemremoverefterm;
	public static JCheckBoxMenuItem autoannotate;
	private JMenuItem annotateitemchangesourcemodelcode;
	public JMenuItem annotateitemreplacerefterm;
	public JMenuItem annotateitemcopy;
	public JMenuItem annotateitemharvestfromtext;
	public JMenuItem annotateitemeditmodelanns;
	public JMenuItem annotateitemcleanup;
	public JMenuItem annotateitemexportcsv;
	public JMenuItem annotateitemthai;
	public static ButtonGroup sortbuttons;
	public static JRadioButtonMenuItem annotateitemusebioportal;
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
	private JMenuItem viewlog;
	private JMenuItem helpitemabout;
	private JMenuItem helpitemweb;

	public static JFileChooser fc;
	public static SemGenGUI desktop;
	public static double version = 2.0;
	public static int numtabs = 0;
	public static int maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	public static ProgressFrame progframe;
	public static String unspecifiedName = "*unspecified*";

	public static FileNameExtensionFilter owlfilter = new FileNameExtensionFilter("SemSim (*.owl)", "owl");
	public static FileNameExtensionFilter cellmlfilter = new FileNameExtensionFilter("CellML (*.cellml, .xml)", "cellml", "xml");
	public static FileNameExtensionFilter sbmlfilter = new FileNameExtensionFilter("SBML (*.sbml, .xml)", "sbml", "xml");
	public static FileNameExtensionFilter mmlfilter = new FileNameExtensionFilter("MML (*.mod)", "mod");
	public static FileNameExtensionFilter csvfilter = new FileNameExtensionFilter("CSV (*.csv)", "csv");

	public SemGenGUI(SemGenSettings sets, JMenuBar mbar){
		settings = sets;
		settingshandle = sets;
		menubar = mbar;
		factory = manager.getOWLDataFactory();

		currentdirectory = new File(settings.getStartDirectory());
		setPreferredSize(settings.getAppSize());
		setOpaque(true);
		setBackground(Color.white);
		addChangeListener(this);
		
		desktop = this; // a specialized layered pane
			
		numtabs = 0;
		// Create the menu bar.
		menubar.setOpaque(true);
		menubar.setPreferredSize(new Dimension(settings.getAppWidth(), 20));

		// Build the File menu
		JMenu filemenu = new JMenu("File");
		filemenu.getAccessibleContext().setAccessibleDescription("Create new files, opening existing files, import raw model code, etc.");
		filemenu.addMenuListener(this);

		// File menu items
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

		// Build the Annotator menu
		JMenu annotatemenu = new JMenu("Annotate");
		annotatemenu.getAccessibleContext().setAccessibleDescription("Annotate a model");

		toolsitemannotate = formatMenuItem(toolsitemannotate, "New Annotator",KeyEvent.VK_A,true,true);
		toolsitemannotate.setToolTipText("Open a new Annotator tool");
		annotatemenu.add(toolsitemannotate);
		annotatemenu.add(new JSeparator());
		
		semanticsummary = formatMenuItem(semanticsummary, "Biological summary", KeyEvent.VK_U, true, true);
		semanticsummary.setToolTipText("View summary of the biological concepts used in the model");

		autoannotate = new JCheckBoxMenuItem("Auto-annotate during translation");
		autoannotate.setSelected(settings.doAutoAnnotate());
		autoannotate.addActionListener(this);
		autoannotate.setToolTipText("Automatically add annotations based on physical units, etc. when possible");
		annotatemenu.add(autoannotate);
		annotatemenu.add(new JSeparator());
		
		annotateitemharvestfromtext = formatMenuItem(annotateitemharvestfromtext, "Find terms in text", KeyEvent.VK_F, true, true);
		annotateitemharvestfromtext.setToolTipText("Use natural language processing to find reference ontology terms in a block of text");

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
		
		annotateitemfindnext = formatMenuItem(annotateitemfindnext, "Find next instance of codeword in code", KeyEvent.VK_N, true, true);
		annotatemenu.add(annotateitemfindnext);
		
		annotatemenu.add(new JSeparator());
		
		annotateitemshowimports = new JCheckBoxMenuItem("Show imported codewords/sub-models");
		annotateitemshowimports.setSelected(settings.showImports());
		annotateitemshowimports.addActionListener(this);
		annotateitemshowimports.setToolTipText("Make imported codewords and submodels visible");
		annotatemenu.add(annotateitemshowimports);

		annotateitemshowmarkers = new JCheckBoxMenuItem("Display physical type markers");
		annotateitemshowmarkers.setSelected(settings.useDisplayMarkers());
		annotateitemshowmarkers.addActionListener(this);
		annotateitemshowmarkers.setToolTipText("Display markers that indicate a codeword's property type");
		annotatemenu.add(annotateitemshowmarkers);
		
		annotateitemtreeview = new JCheckBoxMenuItem("Tree view");
		annotateitemtreeview.setSelected(settings.useTreeView());
		annotateitemtreeview.addActionListener(this);
		annotateitemtreeview.setToolTipText("Display codewords and submodels within the submodel tree");
		annotatemenu.add(annotateitemtreeview);
		
		JMenu sortCodewordsMenu = new JMenu("Sort codewords...");
		annotateitemsortbytype = new JRadioButtonMenuItem("By physical type");
		annotateitemsortbytype.setSelected(settings.organizeByPropertyType());
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

		// Extract menu
		JMenu extractmenu = new JMenu("Extract");
		extractmenu.getAccessibleContext().setAccessibleDescription("Extract out a portion of a SemSim model");

		toolsitemextract = formatMenuItem(toolsitemextract,"New Extractor",KeyEvent.VK_E,true,true);
		toolsitemextract.setToolTipText("Open a new Extractor tool");
		extractmenu.add(toolsitemextract);
		
		extractoritematomicdecomp = formatMenuItem(extractoritematomicdecomp, "Atomic decomposition",KeyEvent.VK_Y,true,true);
		extractoritematomicdecomp.setToolTipText("Extract separate SemSim models for each physical entity");

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

		// Help menu
		JMenu helpmenu = new JMenu("Help");
		helpmenu.getAccessibleContext().setAccessibleDescription("User help, Versioning, etc.");
		helpitemabout = formatMenuItem(helpitemabout,"About",null,true,true);
		helpmenu.add(helpitemabout);
		helpitemweb = formatMenuItem(helpitemweb,"Help manual (opens browser)",KeyEvent.VK_H,true,true);
		helpmenu.add(helpitemweb);
		viewlog = formatMenuItem(viewlog,"Session log",KeyEvent.VK_L,true,true);
		viewlog.setToolTipText("View the current session's log file");

		menubar.add(filemenu);
		menubar.add(annotatemenu);
		menubar.add(extractmenu);
		menubar.add(mergermenu);
		menubar.add(codegenmenu);
		menubar.add(helpmenu);

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
	public void menuCanceled(MenuEvent arg0) {}

	public void menuDeselected(MenuEvent arg0) {}

	public void menuSelected(MenuEvent arg0) {
		if(desktop.getSelectedComponent() instanceof AnnotatorTab){
			fileitemsave.setEnabled(!((AnnotatorTab)desktop.getSelectedComponent()).getModelSaved());
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == fileitemopen) {
			OpenFileAction();
		}

		if (o == fileitemsave) {
			if(desktop.getSelectedComponent() instanceof AnnotatorTab){
				AnnotatorTab ann = (AnnotatorTab)desktop.getSelectedComponent();
				SaveAction(ann, ann.lastSavedAs);
			}
		}

		if (o == fileitemsaveas) {
			SaveAsAction(desktop.getSelectedComponent(),null, new FileNameExtensionFilter[]{cellmlfilter, owlfilter});
		}
		if( o == fileitemclose){
			Component x = null;
			if(desktop.getSelectedComponent() instanceof AnnotatorTab){
				x = (AnnotatorTab)desktop.getSelectedComponent();
			}
			else if(desktop.getSelectedComponent() instanceof ExtractorTab){
				x = (ExtractorTab)desktop.getSelectedComponent();
			}
			else if(desktop.getSelectedComponent() instanceof MergerTab){
				x = (MergerTab)desktop.getSelectedComponent();
			}
			if(x !=null)
				try {
					closeTabAction(x);
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				}
		}

		if (o == fileitemexit) {
			try {
				quit();
			} catch (HeadlessException | OWLException e1 ) {
				e1.printStackTrace();
			} 
		}

		if (o == toolsitemannotate || o == annotatebutton) {
			opendialog.dispose();
			int x = showSemGenFileChooser(currentdirectory, new String[] { "owl", "mod", "xml", "sbml", "cellml" },
					"Select legacy code or SemSim model to annotate", SemGenFileChooser.NEW_ANNOTATOR_TASK, true);
			if (x == JFileChooser.APPROVE_OPTION) {
				startNewAnnotatorTask(fc.getSelectedFiles());
			}
		}
		
		if(o == semanticsummary){
			if(desktop.getSelectedComponent() instanceof AnnotatorTab){
				AnnotatorTab ann = (AnnotatorTab)desktop.getSelectedComponent();
				new SemanticSummaryDialog(ann.semsimmodel);
			}
		}
		
		if(o == annotateitemharvestfromtext){
			if(desktop.getSelectedComponent() instanceof AnnotatorTab){
				AnnotatorTab ann = (AnnotatorTab)desktop.getSelectedComponent();
				if(ann.tmd == null){
					try {ann.tmd = new TextMinerDialog(ann);} 
					catch (FileNotFoundException e1) {e1.printStackTrace();}
				}
				else{ann.tmd.setVisible(true);}
			}
			else{JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");}
		} 

		if (o == annotateitemaddrefterm) {
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				new AddReferenceClassDialog(ann, SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, 
						new Object[]{"Add as entity","Add as process","Close"}, ann.semsimmodel).packAndSetModality();
			} else {
				JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
			}
		}
		if (o == annotateitemremoverefterm) {
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
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
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				new LegacyCodeChooser(ann);
			} else {JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");}
		}

		if(o == annotateitemexportcsv){
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				try {
					new CSVExporter(ann.semsimmodel).exportCodewords();
				} catch (Exception e1) {e1.printStackTrace();} 
			}
		}
		
		if(o == annotateitemeditmodelanns){
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				new ModelLevelMetadataEditor(ann);
			}
		}

		if (o == annotateitemreplacerefterm) {
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				try {
					new AnnotationComponentReplacer(ann);
				} catch (OWLException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (o == annotateitemcopy) {
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				try {
					new AnnotationCopier(ann);
				} catch (OWLException | CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
			}
			else JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
		}

		if (o == annotateitemcleanup) {
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {

			} else {
				JOptionPane.showMessageDialog(this,"Please select an Annotator tab or open a new Annotator");
			}
		}
		
		if( o == annotateitemfindnext){
			if (desktop.getSelectedComponent() instanceof AnnotatorTab) {
				AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
				String name = ann.getLookupNameForAnnotationObjectButton(ann.focusbutton);
				ann.findNextStringInText(name);
			}
		}
		
		if(o == annotateitemthai){
			try {new BatchCellML();}
			catch (Exception e1) {e1.printStackTrace();}
		}
		
		if (o == annotateitemshowmarkers){
			for(Component c : desktop.getComponents()){
				if(c instanceof AnnotatorTab){
					AnnotatorTab temp = (AnnotatorTab)c;
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
			if(desktop.getSelectedComponent() instanceof AnnotatorTab){
				AnnotatorTab temp = (AnnotatorTab)desktop.getSelectedComponent();
				temp.refreshAnnotatableElements();
			}
		}
		
		if(o == annotateitemsortbytype || o == annotateitemsortalphabetically || o == annotateitemtreeview){
			for(Component c : desktop.getComponents()){
				if(c instanceof AnnotatorTab){
					AnnotatorTab temp = (AnnotatorTab)c;
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

		if (o == helpitemabout) 
			AboutAction();

		if (o == helpitemweb) 
			BrowserLauncher.openURL(settings.getHelpURL());

	}
	
	public static int showSemGenFileChooser(File currentdirectory, String[] fileextensions, String title, 
			int taskType, Boolean canselectmultiplefiles, MergerTab merger){
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
		} catch (OWLException | IOException| InterruptedException | JDOMException | ServiceException e) {
			e.printStackTrace();
		}
		if (extractor != null) 
			extractor.atomicDecomposition();
	}
	
	public static void startBatchClustering(File file){
		ExtractorTab extractor;
		try {
			SemSimModel ssm = new SemSimOWLreader().readFromFile(file);
			extractor = new ExtractorTab(file, ssm, settingshandle);
			extractor.batchCluster();
		} catch (OWLException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (CloneNotSupportedException e3) {
			e3.printStackTrace();
		}
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
    				JOptionPane.showMessageDialog(null, "Selected model had errors:", "Could not encode model", JOptionPane.ERROR_MESSAGE);
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
	
	public static AnnotatorTab AnnotateAction(File file, Boolean autosave) {	
		SemSimModel semsimmodel = loadSemSimModelFromFile(file, true);
		AnnotatorTab annotator = null;

		// Create a new tempfile using the date

		if(semsimmodel!=null){
		
		URI selectedURI = file.toURI(); // The selected file for annotation
		URI existingURI = URI.create(""); // The existing file from which to
			Boolean newannok = true;
			// If we are annotating an existing SemSim or CellML file...
			if (semsimmodel.getSourceModelType()==ModelClassifier.SEMSIM_MODEL || semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL) {
				existingURI = selectedURI;
			}
			
			// Check to make sure SemSim model isn't already being annotated before proceeding
			if (newannok && !isOntologyOpenForEditing(existingURI)) {
	
				// Create new Annotater object in SemGen desktop
				annotator = new AnnotatorTab(file,settingshandle);
				annotator.semsimmodel = semsimmodel; 
				
				if(annotator.semsimmodel.getErrors().isEmpty()){
					annotator.setModelSaved(annotator.semsimmodel.getSourceModelType()==ModelClassifier.SEMSIM_MODEL ||
							annotator.semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL);
					
					if(annotator.getModelSaved()) annotator.lastSavedAs = annotator.semsimmodel.getSourceModelType();
					
					// Add unspecified physical model components for use during annotation
					annotator.semsimmodel.addCustomPhysicalEntity(unspecifiedName, "Non-specific entity for use as a placeholder during annotation");
					annotator.semsimmodel.addCustomPhysicalProcess(unspecifiedName, "Non-specific process for use as a placeholder during annotation");
	
					if(annotator.semsimmodel!=null){
						desktop.addTab(annotator);
						
						annotator.NewAnnotatorAction();
						
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
						   public void run() { 
							   desktop.setSelectedComponent(desktop.getComponentAt(numtabs - 1));
							   desktop.getComponentAt(numtabs - 1).repaint();
							   desktop.getComponentAt(numtabs - 1).validate();
						   }
						});

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
			extractor = new ExtractorTab(file, semsimmodel, settingshandle);
			desktop.addTab(extractor);
			desktop.setMnemonicAt(0, KeyEvent.VK_1);
		}
		return extractor;
	}
	
	public void NewMergerAction(){
		opendialog.setVisible(false);
		MergerTab merger = new MergerTab(settings);
		desktop.addTab(merger);

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
							SemGenError.showWebConnectionError(desktop, "BioPortal search service");
						SBMLAnnotator.annotate(file, semsimmodel, online, SemGen.semsimlib.getOntTermsandNamesCache());
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.semsimlib.getOntTermsandNamesCache(), online);
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
							if(!online) SemGenError.showWebConnectionError(desktop, "BioPortal search service");
						}
						if(progframe!=null) progframe.requestFocusInWindow();
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.semsimlib.getOntTermsandNamesCache(), online);
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
					if(SemGen.semsimlib.OPBhasAmountProperty(roa))
						candidateamounts.add(ds);
					// If the codeword represents an OPB:Force property (OPB_00574)
					else if(SemGen.semsimlib.OPBhasForceProperty(roa))
						candidateforces.add(ds);
					// If the codeword represents an OPB:Flow rate property (OPB_00573)
					else if(SemGen.semsimlib.OPBhasFlowProperty(roa)){
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
		String[] candidateOPBclasses = (String[])SemGen.semsimlib.getOPBUnitRefTerm(ds.getUnit().getName());
		if (candidateOPBclasses != null && candidateOPBclasses.length == 1) {
			OWLClass cls = factory.getOWLClass(IRI.create(SemSimConstants.OPB_NAMESPACE + candidateOPBclasses[0]));
			String OPBpropname = SemSimOWLFactory.getRDFLabels(OPB, cls)[0];
			roa = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, cls.getIRI().toURI(), OPBpropname);
		}
		return roa;
	}
	
	public static int getPropertyType(DataStructure ds){
		if(ds.hasPhysicalProperty()){
			// If there's already an OPB reference annotation
			if(ds.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = (ds.getPhysicalProperty().getFirstRefersToReferenceOntologyAnnotation());
				
				if(SemGen.semsimlib.OPBhasStateProperty(roa) ||
						SemGen.semsimlib.OPBhasForceProperty(roa)){
					return SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY;
				}
				else if(SemGen.semsimlib.OPBhasProcessProperty(roa)){
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
		opendialog.setTitle("OPEN: Select action");
		
		annotatebutton = new JButton("Annotate a model");
		annotatebutton.setIcon(SemGenIcon.annotatoricon);
		annotatebutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		annotatebutton.addActionListener(this);
		annotatebutton.setAlignmentX(JButton.CENTER_ALIGNMENT);

		openmenuextractbutton = new JButton("Extract a model");
		openmenuextractbutton.setEnabled(true);
		openmenuextractbutton.setIcon(SemGenIcon.extractoricon);
		openmenuextractbutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		openmenuextractbutton.addActionListener(this);
		openmenuextractbutton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		
		openmenumergebutton = new JButton("Merge models");
		openmenumergebutton.setEnabled(true);
		openmenumergebutton.setIcon(SemGenIcon.mergeicon);
		openmenumergebutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		openmenumergebutton.addActionListener(this);
		openmenumergebutton.setAlignmentX(JButton.CENTER_ALIGNMENT);

		encodebutton = new JButton("Encode a model");
		encodebutton.setEnabled(true);
		encodebutton.setIcon(SemGenIcon.codericon);
		encodebutton.setFont(new Font("SansSerif", Font.PLAIN, 13));
		encodebutton.addActionListener(this);
		encodebutton.setAlignmentX(JButton.CENTER_ALIGNMENT);

		JPanel openpanel = new JPanel();
		openpanel.setLayout(new BoxLayout(openpanel, BoxLayout.Y_AXIS));
		openpanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		openpanel.add(annotatebutton);
		openpanel.add(openmenuextractbutton);
		openpanel.add(openmenumergebutton);
		openpanel.add(encodebutton);
		openpanel.setPreferredSize(new Dimension(250,135));
		openpanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		
		JOptionPane selectopentype = new JOptionPane(openpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
		selectopentype.setOptions(new Object[]{});
		opendialog.setContentPane(selectopentype);
		opendialog.setModal(true);
		opendialog.pack();
		opendialog.setLocationRelativeTo(this);
		opendialog.setVisible(true);
	}
	
	// SAVE ACTION 
	public static boolean SaveAction(Object object, int modeltype){
		boolean success = false;
		if (object instanceof AnnotatorTab) {
			AnnotatorTab ann = (AnnotatorTab) object;
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
						ann.setTabName(targetfile.getName());
						desktop.setTabComponentAt(desktop.indexOfComponent(ann), ann);
						ann.semsimmodel.setName(targetfile.getName().substring(0, targetfile.getName().lastIndexOf(".")));
						desktop.setToolTipTextAt(desktop.indexOfComponent(ann), "Annotating " + targetfile.getName());
					}
					SemGen.logfilewriter.println(targetfile.getName() + " was saved");
					ann.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					success = true;
				}
				else{
					SemGenError.showUnspecifiedAnnotationError(desktop, unspecds);
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

				if (object instanceof AnnotatorTab && saveok == true) {
					AnnotatorTab ann = (AnnotatorTab) desktop.getSelectedComponent();
					Set<DataStructure> unspecds = getDataStructuresWithUnspecifiedAnnotations(ann.semsimmodel);
					if(unspecds.isEmpty()){
						ann.fileURI = file.toURI();
						SaveAction(ann, modeltype);
					}
					else{
						SemGenError.showUnspecifiedAnnotationError(desktop,unspecds);
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
	
	public static Boolean isOntologyOpenForEditing(URI uritocheck) {
		Boolean open = false;
		for (int t = 0; t < desktop.getComponents().length; t++) {
			if (desktop.getComponent(t) instanceof AnnotatorTab) {
				AnnotatorTab tempann = (AnnotatorTab) desktop.getComponent(t);
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

	public boolean AboutAction() {
		String COPYRIGHT = "\u00a9";
		JOptionPane.showMessageDialog(null, "SemGen\nVersion " + version + "\n"
						+ COPYRIGHT
						+ "2010-2014\nMaxwell Lewis Neal\n", "About SemGen",
						JOptionPane.PLAIN_MESSAGE);
		return true;
	}
	
	// WHEN THE CLOSE TAB ACTION IS PERFORMED ON AN annotator, MERGER OR EXTRACTOR FRAME
	public static int closeTabAction(Component component) {
		int returnval = -1;
		int i = desktop.indexOfComponent(component);
		// If the file has been altered, prompt for a save
		if(component instanceof AnnotatorTab){
			AnnotatorTab ann = (AnnotatorTab)component;
			if (ann.getModelSaved()==false) {
				String title = "[unsaved file]";
				if(ann.fileURI!=null){
					title =  new File(ann.fileURI).getName();
				}
				int savefilechoice = JOptionPane.showConfirmDialog(desktop,
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
		else if (component instanceof ExtractorTab || component instanceof MergerTab){
			desktop.remove(desktop.indexOfComponent(component));
			numtabs = numtabs - 1;
		}
		return returnval;
	}

	public boolean quit() throws HeadlessException, OWLException {
		Component[] desktopcomponents = desktop.getComponents();
		Boolean quit = true;
		Boolean contchecking = true;
		for (int x = 0; x < desktopcomponents.length; x++) {
			if (desktopcomponents[x] instanceof AnnotatorTab && contchecking) {
				AnnotatorTab temp = (AnnotatorTab) desktopcomponents[x];
				desktop.setSelectedComponent(temp);
				int val = 0;
				val = closeTabAction(temp);
				if (val == JOptionPane.CANCEL_OPTION) {
					contchecking = false;
					quit = false;
				}
			}
		}
		if(quit){
			try {
				settings.storeSettings();
				SemGen.semsimlib.storeCachedOntologyTerms();
				System.exit(0);
			} 
			catch (URISyntaxException e) {e.printStackTrace();}
		}
		return quit;
	}

	public void stateChanged(ChangeEvent arg0) {
		updateSemGenMenuOptions();
	}
	
	public void updateSemGenMenuOptions(){
		Component comp = desktop.getSelectedComponent();
		
		annotateitemharvestfromtext.setEnabled(comp instanceof AnnotatorTab);
		annotateitemaddrefterm.setEnabled(comp instanceof AnnotatorTab);
		annotateitemremoverefterm.setEnabled(comp instanceof AnnotatorTab);
		annotateitemcopy.setEnabled(comp instanceof AnnotatorTab);
		annotateitemreplacerefterm.setEnabled(comp instanceof AnnotatorTab);
		annotateitemchangesourcemodelcode.setEnabled(comp instanceof AnnotatorTab);
		annotateitemexportcsv.setEnabled(comp instanceof AnnotatorTab);
		annotateitemeditmodelanns.setEnabled(comp instanceof AnnotatorTab);
		annotateitemfindnext.setEnabled(comp instanceof AnnotatorTab);
		
		extractoritembatchcluster.setEnabled(comp instanceof ExtractorTab);
		extractoritemopenann.setEnabled(comp instanceof ExtractorTab);
		
		if(comp instanceof AnnotatorTab){
			AnnotatorTab ann = (AnnotatorTab)comp;
			fileitemsave.setEnabled(!ann.getModelSaved());
		}
		else fileitemsave.setEnabled(false);
		fileitemsaveas.setEnabled(comp instanceof AnnotatorTab);
	}
	
	public void addTab(SemGenTab tab) {
		numtabs++;
		addTab(tab.getName(), tab);
		setTabComponentAt(numtabs-1, tab.getTabLabel());
		tab.setClosePolicy(new tabCloseListener(tab));
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
				   setSelectedComponent(getComponentAt(numtabs - 1));
		}});
	}
	
	private class tabCloseListener extends MouseAdapter {
		SemGenTab tab;
		tabCloseListener(SemGenTab t) {
			tab = t;
		}
		public void mouseClicked(MouseEvent arg0) {
			closeTabAction(tab);
		}
	}
}
