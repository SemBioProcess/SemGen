package semgen;

import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.AnnotatorFactory;
import semgen.annotation.AnnotatorTab;
import semgen.extraction.ExtractorFactory;
import semgen.menu.SemGenMenuBar;
import semgen.merging.MergerTab;
import semgen.resource.SemGenTask;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenTab;

import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SemGenGUI extends JTabbedPane implements Observer {
	private static final long serialVersionUID = 3618439495848469800L;

	private SemGenSettings settings;
	protected GlobalActions globalactions;

	private ArrayList<AnnotatorTab> anntabs = new ArrayList<AnnotatorTab>(); //Open annotation tabs

	public int numtabs = 0;
	private SemGenMenuBar menu;

	public SemGenGUI(SemGenSettings sets,  SemGenMenuBar menubar, GlobalActions gacts){
		settings = new SemGenSettings(sets);
		menu = menubar;
		globalactions = gacts;
		globalactions.addObserver(this);
		
		setPreferredSize(sets.getAppSize());
		setOpaque(true);
		setBackground(Color.white);
			
		numtabs = 0;
	}
	
	// METHODS
	public void startNewAnnotatorTask(){
		addTab(new MakeTab("New Annotator Tab") {
			public void run() {
				AnnotatorFactory factory = new AnnotatorFactory(settings, globalactions);
				tab = factory.makeTab();
			}
		});
	}
	
	public void startNewAnnotatorTask(final File existingfile){
		addTab(new MakeTab("New Annotator Tab") {
			public void run() {
				AnnotatorFactory factory = new AnnotatorFactory(settings, globalactions);
				tab = factory.makeTab(existingfile);
			}
		});
	}
	
	public void startNewExtractorTask() {
		addTab(new MakeTab("New Extractor Tab") {
			public void run() {    	
				ExtractorFactory factory = new ExtractorFactory(settings, globalactions);
				tab = factory.makeTab();
			}
		});
	}
	
	public void startNewExtractorTask(final File existingfile){
		addTab(new MakeTab("New Extractor Tab") {
			public void run() {
				ExtractorFactory factory = new ExtractorFactory(settings, globalactions);
				tab = factory.makeTab(existingfile);
				setMnemonicAt(0, KeyEvent.VK_1);
			}
		});
	}
	
	public void startNewMergerTask(){
		addTab(new MakeTab("New Merger Tab") {
			public void run() {
				MergerTab merger = new MergerTab(settings, globalactions);
				tab = merger;
				merger.PlusButtonAction();
			}
		});
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
			anntabs.remove(component);
			remove(indexOfComponent(component));
			numtabs = numtabs - 1;
		}
		
		return returnval;
	}

	public boolean quit() throws HeadlessException, OWLException {
		Component[] desktopcomponents = getComponents();
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
		if (arg==GlobalActions.appactions.EXTRACT) {
			this.startNewExtractorTask();
		}
		if (arg==GlobalActions.appactions.EXTRACTEXISTING) {
			this.startNewExtractorTask(globalactions.getSeed());
		}
		if (arg==GlobalActions.appactions.MERGE) {
			startNewMergerTask();
		}
	}
	public void addTab(MakeTab maker) {
		AddTabTask task = new AddTabTask(maker);
		task.execute();
	}
	
	private class AddTabTask extends SemGenTask {
		MakeTab maker;
		AddTabTask(MakeTab maker) {
			this.maker = maker;
			progframe = new SemGenProgressBar(maker.getProgress(), true);
		}
		@Override
		protected Void doInBackground() throws Exception {		
			SwingUtilities.invokeAndWait(maker);

			return null;
		}
		
		public void endTask() {
			SemGenTab tab = maker.getTab();
			numtabs++;
			addTab(tab.getName(), tab);
			globalactions.setCurrentTab(tab);
			tab.addObservertoWorkbench(menu.filemenu);
			setTabComponentAt(numtabs-1, tab.getTabLabel());
			
			tab.addMouseListenertoTabLabel(new tabClickedListener(numtabs-1));
			tab.setClosePolicy(new tabCloseListener(tab));
			setSelectedComponent(getComponentAt(numtabs - 1));
			getComponentAt(numtabs - 1).repaint();
			getComponentAt(numtabs - 1).validate();
		}
	}
	
	private abstract class MakeTab implements Runnable {
		SemGenTab tab;
		String progress;
		public MakeTab(String prog) {
			progress = prog;
		}
		protected SemGenTab getTab() {
			return tab;
		}
		public String getProgress() {
			return progress;
		}
		
		public void setProgress(String prog) {
			progress = prog;
		}
	}
}
