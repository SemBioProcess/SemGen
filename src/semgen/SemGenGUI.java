package semgen;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import semgen.annotation.AnnotatorTab;
import semgen.extraction.ExtractorTab;
import semgen.menu.HelpMenu;
import semgen.merging.MergerTab;
import semgen.resource.CSVExporter;
import semgen.resource.SemGenError;
import semgen.resource.SemGenTask;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenFileChooser;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenTab;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalEntity;
import semsim.reading.ModelClassifier;
import semsim.reading.SemSimOWLreader;
import semsim.writing.CellMLwriter;
import semsim.writing.MMLwriter;
import semsim.writing.Writer;

import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

import javax.xml.rpc.ServiceException;

public class SemGenGUI extends JTabbedPane implements ActionListener, ChangeListener {

	private static final long serialVersionUID = 3618439495848469800L;

	private SemGenSettings settings;
	private static SemGenSettings settingshandle; //Temporary work around for static functions
	
	private JMenuItem fileitemopen;
	private JMenuItem fileitemsave;
	private JMenuItem fileitemsaveas;
	public JMenuItem fileitemproperties;
	public JMenuItem fileitemclose;
	public JMenuItem fileitemexit;
	
	private JMenuItem toolsitemannotate;
	private JMenuItem toolsitemmerge;
	private JMenuItem toolsitemcode;
	private JMenuItem toolsitemextract;

	public static SemGenGUI desktop;
	private ArrayList<AnnotatorTab> anntabs = new ArrayList<AnnotatorTab>(); //Open annotation tabs

	public int numtabs = 0;
	public static int maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	public static String unspecifiedName = "*unspecified*";

	public SemGenGUI(SemGenSettings sets, JMenuBar menubar){
		settings = new SemGenSettings(sets);
		settingshandle = settings;

		setPreferredSize(sets.getAppSize());
		setOpaque(true);
		setBackground(Color.white);
		addChangeListener(this);
		
		desktop = this; // a specialized layered pane
			
		numtabs = 0;
		// Create the menu bar.
		menubar.setOpaque(true);
		menubar.setPreferredSize(new Dimension(sets.getAppWidth(), 20));

		// Build the File menu
		JMenu filemenu = new JMenu("File");
		filemenu.getAccessibleContext().setAccessibleDescription("Create new files, opening existing files, import raw model code, etc.");

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
		filemenu.add(new JSeparator());
		fileitemproperties = formatMenuItem(fileitemproperties,"Properties", KeyEvent.VK_Q,true,true);
		filemenu.add(fileitemproperties);
		fileitemexit = formatMenuItem(fileitemexit,"Quit SemGen",KeyEvent.VK_Q,true,true);
		filemenu.add(new JSeparator());
		filemenu.add(fileitemexit);

		// Tools menu
		JMenu toolsmenu = new JMenu("Tasks");
		toolsmenu.getAccessibleContext().setAccessibleDescription("Select a new SemGen task");
		
		toolsitemannotate = formatMenuItem(toolsitemannotate, "New Annotator",KeyEvent.VK_A,true,true);
		toolsitemannotate.setToolTipText("Open a new Annotator tool");
		toolsmenu.add(toolsitemannotate);
		
		toolsitemextract = formatMenuItem(toolsitemextract,"New Extractor",KeyEvent.VK_E,true,true);
		toolsitemextract.setToolTipText("Open a new Extractor tool");
		toolsmenu.add(toolsitemextract);
		
		toolsitemmerge = formatMenuItem(toolsitemmerge,"New Merger",KeyEvent.VK_M,true,true);
		toolsitemmerge.setToolTipText("Open a new Merger tool");
		toolsmenu.add(toolsitemmerge);

		toolsitemcode = formatMenuItem(toolsitemcode, "New code generator", KeyEvent.VK_G,true,true);
		toolsitemcode.setToolTipText("Open a new code generator tool");
		toolsmenu.add(toolsitemcode);

		menubar.add(filemenu);
		menubar.add(toolsmenu);
		menubar.add(new HelpMenu(settings));
	}
	
	// Format menu items, assign shortcuts, action listeners
	public JMenuItem formatMenuItem(JMenuItem item, String text, Integer accelerator, Boolean enabled, Boolean addactionlistener){
		item = new JMenuItem(text);
		item.setEnabled(enabled);
		if(accelerator!=null){item.setAccelerator(KeyStroke.getKeyStroke(accelerator,maskkey));}
		if(addactionlistener){item.addActionListener(this);}
		return item;
	}

	public void startNewTaskDialog() {
		NewTaskDialog ntd = new NewTaskDialog();
		switch (ntd.getChoice()) {
		case Annotate:
			startNewAnnotatorTask();
			break;
		case Encode:
			toolsitemcode.doClick();
			break;
		case Extract:
			startNewExtractorTask();
			break;
		case Merge:
			NewMergerAction();
			break;
		default:
			break;
		}
	}
	
	// METHODS
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == fileitemopen) {
			startNewTaskDialog();
		}

		if (o == fileitemsave) {
			if(desktop.getSelectedComponent() instanceof AnnotatorTab){
				AnnotatorTab ann = (AnnotatorTab)desktop.getSelectedComponent();
				SaveAction(ann, ann.lastSavedAs);
			}
		}

		if (o == fileitemsaveas) {
			SaveAsAction(desktop.getSelectedComponent(),null, 
					new FileNameExtensionFilter[]{SemGenFileChooser.cellmlfilter, SemGenFileChooser.owlfilter});
		}
		if( o == fileitemclose){
			Component x = desktop.getSelectedComponent();
			if(x !=null)
				try {
					closeTabAction((SemGenTab)x);
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				}
		}
		if (o == fileitemproperties) {
			new PreferenceDialog(settings);
		}
		
		if (o == fileitemexit) {
			try {
				quit();
			} catch (HeadlessException | OWLException e1 ) {
				e1.printStackTrace();
			} 
		}

		if (o == toolsitemannotate) {
			startNewAnnotatorTask();
		}

		if (o == toolsitemextract) {
			startNewExtractorTask();
		}

		if (o == toolsitemmerge){
			NewMergerAction();
		}
		
		if (o == toolsitemcode) {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model to encode", 
					new String[] {"owl"});

			startEncoding(sgc.getSelectedFile(), sgc.getSelectedFile().getAbsolutePath());
		}
	}
	
	public static void startNewAnnotatorTask(){
		NewAnnotatorTask task = new NewAnnotatorTask(true);
		task.execute();
	}
	
	public static void startNewExtractorTask(){
		NewExtractorTask task = new NewExtractorTask();
		task.execute();
	}
	
	public static void startAtomicDecomposition(){
		SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model",
				new String[] {"owl"});
		try {
			ExtractorTab extractor = NewExtractorAction(sgc.getSelectedFile());
			if (extractor != null) 
				extractor.atomicDecomposition();
		} catch (OWLException | IOException| InterruptedException | JDOMException | ServiceException e) {
			e.printStackTrace();
		}
	}
	
	public void startBatchClustering(){
		SemGenOpenFileChooser sgc = 
				new SemGenOpenFileChooser("Select a SemSim model for automated cluster analysis", 
						new String[] {"owl",".xml",".sbml",".mod",".cellml"} );
		try {
				File file = sgc.getSelectedFile();
				if (file.exists()) {
				SemSimModel ssm = new SemSimOWLreader().readFromFile(file);
				ExtractorTab extractor = new ExtractorTab(file, ssm, settings);
				extractor.batchCluster();
			}
		} catch (OWLException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (CloneNotSupportedException e3) {
			e3.printStackTrace();
		}
	}
	
	public static class NewAnnotatorTask extends SemGenTask {
		public File file;
		public boolean autosave;
        public NewAnnotatorTask(boolean autosave){
        	SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate");
        	file = sgc.getSelectedFile();
        	this.autosave = autosave;
        }
        
        public NewAnnotatorTask(File existingfile, boolean autosave){
        	file = existingfile;
        	this.autosave = autosave;
        }
        
        @Override
        public Void doInBackground(){
        	if (file==null) endTask(); //If no file was selected, abort
        	progframe = new SemGenProgressBar("Loading " + file.getName() + "...", true);
    			System.out.println("Loading " + file.getName());
    			progframe.updateMessage("Loading " + file.getName() + "...");
    			try{
    				AnnotateAction(file, autosave);
    			}
    			catch(Exception e){e.printStackTrace();}
            return null;
        }
    }
	
	public static class NewExtractorTask extends SemGenTask {
		public File file;
        public NewExtractorTask(){
        	SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model",
        			new String[]{"owl"} );
        	file = sgc.getSelectedFile();
        }
        public NewExtractorTask(File existingfile){
        	file = existingfile;
        }
        @Override
        public Void doInBackground() {
			progframe = new SemGenProgressBar("Loading model for extraction...", true, this);
        	try {
				NewExtractorAction(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
    }
	
	public static class CoderTask extends SemGenTask {
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
        		model = LoadSemSimModel.loadSemSimModelFromFile(inputfile, settingshandle.doAutoAnnotate());
    			if(!model.getErrors().isEmpty()){
    				JOptionPane.showMessageDialog(null, "Selected model had errors:", "Could not encode model", JOptionPane.ERROR_MESSAGE);
    				return null;
    			}
    		}
    		progframe = new SemGenProgressBar("Encoding...", true);
			CoderAction(model, outputfile, writer);
            return null;
        }
    }
	
	public static AnnotatorTab AnnotateAction(File file, Boolean autosave) {	
		SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, settingshandle.doAutoAnnotate());
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
			if (newannok && !desktop.isOntologyOpenForEditing(existingURI)) {
	
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
						desktop.anntabs.add(annotator);
						
						annotator.NewAnnotatorAction();
					}
				}
			}
		}
		return annotator;
	}
	
	// Make this into task
	private static ExtractorTab NewExtractorAction(File file) throws OWLException, IOException, InterruptedException, JDOMException, ServiceException {
		if ((file == null) || !file.exists()) return null;
		SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, settingshandle.doAutoAnnotate());
		if(ModelClassifier.classify(file)==ModelClassifier.CELLML_MODEL || semsimmodel.getFunctionalSubmodels().size()>0){
			JOptionPane.showMessageDialog(null, "Sorry. Extraction of models with CellML-type components not yet supported.");
			return null;
		}
		ExtractorTab extractor = null;
		if(semsimmodel!=null){
			extractor = new ExtractorTab(file, semsimmodel, settingshandle);
			desktop.addTab(extractor);
			desktop.setMnemonicAt(0, KeyEvent.VK_1);
		}
		return extractor;
	}
	
	public void NewMergerAction(){
		MergerTab merger = new MergerTab(settings);
		desktop.addTab(merger);

		merger.PlusButtonAction();
	}

	public static void startEncoding(Object inputfileormodel, String filenamesuggestion){
		File outputfile = null;
		Object[] optionsarray = new Object[] {"CellML", "MML (JSim)"};
		Object selection = JOptionPane.showInputDialog(null, "Select output format", "SemGen coder", JOptionPane.PLAIN_MESSAGE, null, optionsarray, "CellML");
		
		if(filenamesuggestion!=null && filenamesuggestion.contains(".")) filenamesuggestion = filenamesuggestion.substring(0, filenamesuggestion.lastIndexOf("."));
		
		Writer outwriter = null;
		
		if(selection == optionsarray[0]){
			outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, 
					new FileNameExtensionFilter[]{SemGenFileChooser.cellmlfilter});
			outwriter = (Writer)new CellMLwriter();
		}
		
		if(selection == optionsarray[1]){
			outputfile = SemGenGUI.SaveAsAction(null, filenamesuggestion, 
					new FileNameExtensionFilter[]{SemGenFileChooser.mmlfilter});
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
		}
	}

	public static void CoderAction(SemSimModel model, File outputfile, Writer writer){
		String content = writer.writeToString(model);
		if(content!=null)
			SemSimUtil.writeStringToFile(content, outputfile);
		else
			JOptionPane.showMessageDialog(null, "Sorry. There was a problem encoding " + model.getName() + 
					"\nThe JSim API threw an exception.",  
					"Error", JOptionPane.ERROR_MESSAGE);
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
				success = SaveAsAction(ann, path, 
						new FileNameExtensionFilter[]{SemGenFileChooser.cellmlfilter, SemGenFileChooser.owlfilter})!=null;
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
			filec.setCurrentDirectory(SemGenFileChooser.currentdirectory);
			filec.setDialogTitle("Choose location to save file");
			filec.setPreferredSize(new Dimension(550,550));
			
			filec.setAcceptAllFileFilterUsed(false);
			
			for(FileNameExtensionFilter filter : filters) filec.addChoosableFileFilter(filter);
			
			int returnVal = filec.showSaveDialog(desktop);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = new File(filec.getSelectedFile().getAbsolutePath());
				SemGenFileChooser.currentdirectory = filec.getCurrentDirectory();
				
				String extension = null;
				int modeltype = -1;
				
				if(filec.getFileFilter()==SemGenFileChooser.owlfilter){
					extension = "owl";
					modeltype = ModelClassifier.SEMSIM_MODEL;
				}
				if(filec.getFileFilter()==SemGenFileChooser.cellmlfilter){
					extension = "cellml";
					modeltype = ModelClassifier.CELLML_MODEL;
				}
				if(filec.getFileFilter()==SemGenFileChooser.mmlfilter){
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
	
	public Boolean isOntologyOpenForEditing(URI uritocheck) {
		for (AnnotatorTab at : anntabs) {
			if (at.checkFile(uritocheck)) {
				JOptionPane.showMessageDialog(null,"Cannot create or load \n"+ uritocheck.toString()+
					"\n because the file is already open for editing.",null, JOptionPane.PLAIN_MESSAGE);
				return true;
				}
		}
		return false;
	}

	// WHEN THE CLOSE TAB ACTION IS PERFORMED ON AN AnnotatorTab, MergerTab OR EXTRACTOR FRAME
	public static boolean closeTabAction(SemGenTab component) {
		// If the file has been altered, prompt for a save
		boolean returnval =  component.closeTab();
		if (returnval) {
			desktop.anntabs.remove(component);
			desktop.remove(desktop.indexOfComponent(component));
			desktop.numtabs = desktop.numtabs - 1;
			System.gc();
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
				if (!closeTabAction(temp)) {
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
		tab.addMouseListenertoTabLabel(new tabClickedListener(numtabs-1));
		tab.setClosePolicy(new tabCloseListener(tab));
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
				   setSelectedComponent(getComponentAt(numtabs - 1));
				   getComponentAt(numtabs - 1).repaint();
				   getComponentAt(numtabs - 1).validate();
		}});
	}
	
	private class tabClickedListener extends MouseAdapter {
		int tabindex;
		tabClickedListener(int index) {
			tabindex = index;
		}
		public void mouseClicked(MouseEvent arg0) {
			setSelectedIndex(tabindex);
		}
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
