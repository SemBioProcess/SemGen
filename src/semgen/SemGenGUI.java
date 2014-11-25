package semgen;

import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.AnnotationTabFactory;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.workbench.AnnotatorFactory;
import semgen.extraction.ExtractorTabFactory;
import semgen.extraction.workbench.ExtractorFactory;
import semgen.menu.SemGenMenuBar;
import semgen.merging.MergerTabFactory;
import semgen.merging.workbench.MergerFactory;
import semgen.utilities.SemGenTask;
import semgen.utilities.Workbench;
import semgen.utilities.WorkbenchFactory;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.utilities.uicomponent.TabFactory;

import java.net.URI;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SemGenGUI extends JTabbedPane implements Observer{
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
		AnnotatorFactory factory = new AnnotatorFactory(settings.doAutoAnnotate());
		AnnotationTabFactory tabfactory = new AnnotationTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewAnnotatorTask(final File existingfile){
		AnnotatorFactory factory = new AnnotatorFactory(settings.doAutoAnnotate(), existingfile);
		AnnotationTabFactory tabfactory = new AnnotationTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewExtractorTask() {
		ExtractorFactory factory = new ExtractorFactory();
		ExtractorTabFactory tabfactory = new ExtractorTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewExtractorTask(final File existingfile){
		ExtractorFactory factory = new ExtractorFactory(existingfile);
		ExtractorTabFactory tabfactory = new ExtractorTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewMergerTask(){
		MergerFactory factory = new MergerFactory();
		MergerTabFactory tabfactory = new MergerTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
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
		for (int x = 0; x < desktopcomponents.length; x++) {
			if (desktopcomponents[x] instanceof AnnotatorTab) {
				AnnotatorTab temp = (AnnotatorTab) desktopcomponents[x];
				if (!closeTabAction(temp)) {
					return false;
				}
			}
		}

		return true;
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
	public void addTab(WorkbenchFactory<? extends Workbench> workbenchmaker, TabFactory<? extends Workbench> tabmaker) {
		if (!workbenchmaker.isValid()) return;
		AddTabTask<? extends Workbench> task = new AddTabTask(workbenchmaker, tabmaker);
		task.execute();
	}
	
	private class AddTabTask<T extends Workbench> extends SemGenTask {
		WorkbenchFactory<T> workbenchfactory;
		TabFactory<T> tabfactory;
		AddTabTask(WorkbenchFactory<T> maker, TabFactory<T> tabmaker) {
			workbenchfactory = maker;
			tabfactory = tabmaker;
			progframe = new SemGenProgressBar(maker.getStatus(), true);
		}
		@Override
		protected Void doInBackground() throws Exception {
			while (workbenchfactory.isValid()) {
				SwingUtilities.invokeAndWait(workbenchfactory);
				break;
			}
			if (!workbenchfactory.isValid()) {
				cancel(true);
			}
			return null;
		}
		
		public void endTask() {	
			SemGenTab tab = tabfactory.makeTab(workbenchfactory.getWorkbench());
			numtabs++;
			addTab(tab.getName(), tab);
			globalactions.setCurrentTab(tab);
			tab.loadTab();
			tab.addObservertoWorkbench(menu.filemenu);
			setTabComponentAt(numtabs-1, tab.getTabLabel());
			
			tab.addMouseListenertoTabLabel(new tabClickedListener(numtabs-1));
			tab.setClosePolicy(new tabCloseListener(tab));
			setSelectedComponent(getComponentAt(numtabs - 1));
			getComponentAt(numtabs - 1).repaint();
			getComponentAt(numtabs - 1).validate();
		}
	}
}
