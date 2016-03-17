package semgen;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import semgen.utilities.uicomponent.SemGenTab;
import semsim.reading.ModelAccessor;
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
		TABOPENED,
		TABCHANGED,
		TABCLOSEREQUEST,
		TABCLOSED
	};
	private Integer tabsopen = 0;
	private SemGenTab currentTab;
	private ModelAccessor seed;
	private Set<ModelAccessor> seeds;
	
	GlobalActions() {}
	
	public void setCurrentTab(SemGenTab tab) {
		currentTab = tab;
		setChanged();
		notifyObservers(appactions.TABCHANGED);
	}

	public void closeTab() {
		setChanged();
		notifyObservers(appactions.TABCLOSEREQUEST);
	}
	
	public SemGenTab getCurrentTab() {
		return currentTab;
	}
	public void NewAnnotatorTab() {
		setChanged();
		notifyObservers(appactions.ANNOTATE);
	}
	
	public void NewAnnotatorTab(ModelAccessor obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.ANNOTATEEXISTING);
	}
		
	public void NewExtractorTab() {
		setChanged();
		notifyObservers(appactions.EXTRACT);
	}
	
	public void NewExtractorTab(ModelAccessor obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.EXTRACTEXISTING);
	}
	
	public void NewMergerTab() {
		setChanged();
		notifyObservers(appactions.MERGE);
	}
	
	public void NewMergerTab(ModelAccessor accessor1, ModelAccessor accessor2) {
		seeds = new HashSet<ModelAccessor>();
		seeds.add(accessor1);
		
		if(accessor2 != null)
			seeds.add(accessor2);
		
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
	public ModelAccessor getSeed() {
		ModelAccessor accessor = seed;
		seed = null;
		return accessor;
	}
	
	public Set<ModelAccessor> getSeeds() {
		Set<ModelAccessor> accessors = seeds;
		seeds = null;
		return accessors;
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
	
	public void incTabCount() {
		tabsopen++;
		setChanged();
		notifyObservers(appactions.TABOPENED);
	}
	
	public void decTabCount() {
		tabsopen--;
		setChanged();
		notifyObservers(appactions.TABCLOSED);
	}
	
	public int getNumOpenTabs() {
		return tabsopen;
	}
	
	public void quit() {
		setChanged();
		notifyObservers(appactions.QUIT);
	}
}
