package semgen;

import java.io.File;
import java.util.Observable;

import semgen.encoding.Encoder;
import semgen.resource.NewTaskDialog;
import semgen.resource.uicomponents.SemGenTab;

public final class UniversalActions extends Observable {
	public Integer QUIT = 0;
	public Integer TABCHANGED = 1;
	public Integer TABCLOSED = 2;
	public Integer OPENACTION = 9;
	public Integer annotate = 10; 
	public Integer extract = 11; 
	public Integer merge = 12; 
	
	private SemGenTab currentTab;
	private File seed;
	
	public UniversalActions() {}
		
	public void quit() {
		notifyObservers(QUIT);
	}
	
	public void NewAnnotatorTab() {
		setChanged();
		notifyObservers(annotate);
	}
	
	public void NewExtractorTab() {
		setChanged();
		notifyObservers(extract);
	}

	public void NewMergerTab() {
		setChanged();
		notifyObservers(merge);
	}
	
	public void NewAnnotatorTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(annotate);
	}
	
	public void NewExtractorTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(extract);
	}

	public void NewMergerTab(File obj) {
		seed = obj;
		setChanged();
		notifyObservers(merge);
	}

	public File getSeed() {
		return seed;
	}
	
	public void newTask() {
		NewTaskDialog openDialog = new NewTaskDialog();
		switch(openDialog.getChoice()) {
		case 0:
			NewAnnotatorTab();
			break;
		case 1:
			NewExtractorTab();
			break;
		case 2:
			NewMergerTab();
			break;
		case 3:
			new Encoder();
			break;
		}
		setChanged();
		notifyObservers();
	}
	
	public void requestSave() {
		getCurrentTab().requestSaveAs();
	}
	
	public void requestSaveAs() {
		getCurrentTab().requestSaveAs();
	}
	
	public void setCurrentTab(SemGenTab tab) {
		currentTab = tab;
		setChanged();
		notifyObservers(TABCHANGED);
	}

	public void closeTab() {
		setChanged();
		notifyObservers(TABCLOSED);
	}
	
	public SemGenTab getCurrentTab() {
		return currentTab;
	}
}
