package semgen;

import java.io.File;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import semgen.utilities.uicomponent.SemGenTab;
/**
 * Class for notifying application level classes of requests and events 
 * from elsewhere in SemGen. Contains methods for passing a file
 * to classes outside of the calling object's ancestor hierarchy.
 */
public class GlobalActions extends Observable {
	public static enum appactions {
		ANNOTATE,
		ANNOTATEEXISTING,
		EXTRACT,
		EXTRACTEXISTING,
		MERGE,
		MERGEEXISTING,
		QUIT,
		SAVED,
		STAGE,
		TABCHANGED,
		TABCLOSED,
	};
	
	private SemGenTab currentTab;
	private File seed;
	private Set<File> seeds;
	
	GlobalActions() {}
	
	public void setCurrentTab(SemGenTab tab) {
		currentTab = tab;
		setChanged();
		notifyObservers(appactions.TABCHANGED);
	}

	public void closeTab() {
		setChanged();
		notifyObservers(appactions.TABCLOSED);
	}
	
	public SemGenTab getCurrentTab() {
		return currentTab;
	}
	public void NewAnnotatorTab() {
		setChanged();
		notifyObservers(appactions.ANNOTATE);
	}
	
	public void NewAnnotatorTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.ANNOTATEEXISTING);
	}
		
	public void NewExtractorTab() {
		setChanged();
		notifyObservers(appactions.EXTRACT);
	}
	
	public void NewExtractorTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.EXTRACTEXISTING);
	}
	
	public void NewMergerTab() {
		setChanged();
		notifyObservers(appactions.MERGE);
	}
	
	public void NewMergerTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.MERGE);
	}
	
	public void NewMergerTab(File model1, File model2) {
		seeds = new HashSet<File>();
		seeds.add(model1);
		seeds.add(model2);
		
		setChanged();
		notifyObservers(appactions.MERGEEXISTING);
	}
	
	public void NewStageTab() {
		setChanged();
		notifyObservers(appactions.STAGE);
	}

	/** 
	 * Retrieve the stored file and reset the pointer.
	 */
	public File getSeed() {
		File file = seed;
		seed = null;
		return file;
	}
	
	public Set<File> getSeeds() {
		Set<File> files = seeds;
		seeds = null;
		return files;
	}
	
	public void requestSave() {
		getCurrentTab().requestSave();
		setChanged();
		notifyObservers(appactions.SAVED);
	}
	
	public void requestSaveAs() {
		getCurrentTab().requestSaveAs();
		setChanged();
		notifyObservers(appactions.SAVED);
	}
	
	public void quit() {
		setChanged();
		notifyObservers(appactions.QUIT);
	}
}
