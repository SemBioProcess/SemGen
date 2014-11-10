package semgen;

import java.io.File;
import java.util.Observable;

import semgen.resource.uicomponent.SemGenTab;

public class GlobalActions extends Observable {
	public static enum appactions { QUIT, TABCHANGED, TABCLOSED, SAVED, ANNOTATE, EXTRACT, MERGE }
	private SemGenTab currentTab;
	private File seed;
	
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
	
	public void NewExtractorTab() {
		setChanged();
		notifyObservers(appactions.EXTRACT);
	}

	public void NewMergerTab() {
		setChanged();
		notifyObservers(appactions.MERGE);
	}
	
	public void NewAnnotatorTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.ANNOTATE);
	}
	
	public void NewExtractorTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.EXTRACT);
	}

	public void NewMergerTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(appactions.MERGE);
	}

	public File getSeed() {
		return seed;
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
