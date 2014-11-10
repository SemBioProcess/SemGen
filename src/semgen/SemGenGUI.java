package semgen;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.AnnotatorTab;
import semgen.encoding.Encoder;
import semgen.extraction.ExtractorTab;
import semgen.menu.HelpMenu;
import semgen.menu.SemGenMenuBar;
import semgen.merging.MergerTab;
import semgen.resource.SemGenTask;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenTab;
import semsim.model.SemSimModel;
import semsim.reading.ModelClassifier;
import semsim.reading.SemSimOWLreader;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.xml.rpc.ServiceException;

public class SemGenGUI extends JTabbedPane implements ActionListener, Observer {
	private static final long serialVersionUID = 3618439495848469800L;

	private SemGenSettings settings;
	private static SemGenSettings settingshandle; //Temporary work around for static functions
	protected GlobalActions globalactions;

	public static SemGenGUI desktop;
	private ArrayList<AnnotatorTab> anntabs = new ArrayList<AnnotatorTab>(); //Open annotation tabs

	public int numtabs = 0;
	private SemGenMenuBar menu;

	public SemGenGUI(SemGenSettings sets,  SemGenMenuBar menubar, GlobalActions gacts){
		settings = new SemGenSettings(sets);
		menu = menubar;
		settingshandle = settings;
		globalactions = gacts;
		globalactions.addObserver(this);
		
		setPreferredSize(sets.getAppSize());
		setOpaque(true);
		setBackground(Color.white);
		
		desktop = this; // a specialized layered pane
			
		numtabs = 0;

		// Build the File menu
		JMenu filemenu = new JMenu("File");
		filemenu.getAccessibleContext().setAccessibleDescription("Create new files, opening existing files, import raw model code, etc.");

		// File menu items
		menu.filemenu.fileitemnew.addActionListener(this);

		// Tools menu
		JMenu toolsmenu = new JMenu("Tasks");
		toolsmenu.getAccessibleContext().setAccessibleDescription("Select a new SemGen task");
		
		menu.toolsmenu.toolsitemannotate.addActionListener(this);
		menu.toolsmenu.toolsitemextract.addActionListener(this);
		menu.toolsmenu.toolsitemmerge.addActionListener(this);

		menubar.add(filemenu);
		menubar.add(toolsmenu);
		menubar.add(new HelpMenu(settings));
	}

	public void startNewTaskDialog() {
		NewTaskDialog ntd = new NewTaskDialog();
		switch (ntd.getChoice()) {
		case Annotate:
			startNewAnnotatorTask();
			break;
		case Encode:
			new Encoder();
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

		if (o == menu.filemenu.fileitemnew) {
			startNewTaskDialog();
		}

		if (o == menu.toolsmenu.toolsitemannotate) {
			startNewAnnotatorTask();
		}

		if (o == menu.toolsmenu.toolsitemextract) {
			startNewExtractorTask();
		}

		if (o == menu.toolsmenu.toolsitemmerge){
			NewMergerAction();
		}
		
	}
	
	public static void startNewAnnotatorTask(){
		NewAnnotatorTask task = new NewAnnotatorTask(true);
		task.execute();
	}
	
	public static AnnotatorTab startNewAnnotatorTask(File existingfile){
		NewAnnotatorTask task = new NewAnnotatorTask(existingfile, true);
		task.execute();
		
		return task.annotator;
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
				ExtractorTab extractor = new ExtractorTab(file, ssm, settings, globalactions);
				extractor.batchCluster();
			}
		} catch (OWLException | IOException | CloneNotSupportedException e1  ) {
			e1.printStackTrace();
		}
	}
	
	public static class NewAnnotatorTask extends SemGenTask {
		public File file;
		public boolean autosave;
		
		AnnotatorTab annotator = null;
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
    				AnnotateAction(autosave);
    			}
    			catch(Exception e){e.printStackTrace();}
            return null;
        }
        
        public void AnnotateAction(Boolean autosave) {	
		// Create a new tempfile using the date

			// Check to make sure SemSim model isn't already being annotated before proceeding
			if (!desktop.isOntologyOpenForEditing(file.toURI())) {
	
				// Create new Annotater object in SemGen desktop
				annotator = new AnnotatorTab(file,settingshandle, desktop.globalactions);
				
				desktop.addTab(annotator);
				desktop.anntabs.add(annotator);
						
				annotator.NewAnnotatorAction();
			}
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
			extractor = new ExtractorTab(file, semsimmodel, settingshandle, desktop.globalactions);
			desktop.addTab(extractor);
			desktop.setMnemonicAt(0, KeyEvent.VK_1);
		}
		return extractor;
	}
	
	public void NewMergerAction(){
		MergerTab merger = new MergerTab(settings, globalactions);
		desktop.addTab(merger);

		merger.PlusButtonAction();
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
	
	public void addTab(SemGenTab tab) {
		numtabs++;
		addTab(tab.getName(), tab);
		setTabComponentAt(numtabs-1, tab.getTabLabel());
		globalactions.setCurrentTab(tab);
		tab.addMouseListenertoTabLabel(new tabClickedListener(numtabs-1));
		tab.setClosePolicy(new tabCloseListener(tab));
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			   public void run() { 
				   setSelectedComponent(getComponentAt(numtabs - 1));
				   getComponentAt(numtabs - 1).repaint();
				   getComponentAt(numtabs - 1).validate();
		}});
		tab.addObservertoWorkbench(menu.filemenu);
	}
	
	private class tabClickedListener extends MouseAdapter {
		int tabindex;
		tabClickedListener(int index) {
			tabindex = index;
		}
		public void mouseClicked(MouseEvent arg0) {
			setSelectedIndex(tabindex);
			globalactions.setCurrentTab((SemGenTab) getComponentAt(tabindex));
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

	@Override
	public void update(Observable o, Object arg) {
		if (arg==GlobalActions.appactions.TABCLOSED) {
			closeTabAction(globalactions.getCurrentTab());
		}
		
	}
}
