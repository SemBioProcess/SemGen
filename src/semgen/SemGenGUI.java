package semgen;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.AnnotatorTab;
import semgen.encoding.Encoder;
import semgen.extraction.ExtractorTab;
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

	private static SemGenGUI desktop;
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
		
		// File menu items
		menu.filemenu.fileitemnew.addActionListener(this);
		
		menu.toolsmenu.toolsitemextract.addActionListener(this);
	}

	public void startNewTaskDialog() {
		NewTaskDialog ntd = new NewTaskDialog();
		switch (ntd.getChoice()) {
		case Annotate:
			globalactions.NewAnnotatorTab();
			break;
		case Encode:
			new Encoder();
			break;
		case Extract:
			startNewExtractorTask();
			break;
		case Merge:
			globalactions.NewMergerTab();
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
		
		if (o == menu.toolsmenu.toolsitemextract) {
			startNewExtractorTask();
		}
	}
	
	public void startNewAnnotatorTask(){
		Thread createann = new Thread() {
			public void run() {
				AnnotatorTab anntab = new AnnotatorTab(settings, globalactions);
				if (anntab.initialize()) addTab(anntab);
			}
		};
		createann.run();
	}
	
	public void startNewAnnotatorTask(File existingfile){
		AnnotatorTab anntab = new AnnotatorTab(existingfile, settings, globalactions);
		if (anntab.initialize(existingfile)) addTab(anntab);
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
	private boolean closeTabAction(SemGenTab component) {
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
		if (arg==GlobalActions.appactions.ANNOTATE) {
			this.startNewAnnotatorTask();
		}
		if (arg==GlobalActions.appactions.ANNOTATEEXISTING) {
			this.startNewAnnotatorTask(globalactions.getSeed());
		}
	}
}
