package semgen.merging.workbench;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import semgen.utilities.Workbench;
import semgen.utilities.file.LoadSemSimModel;
import semsim.model.SemSimModel;
import semsim.reading.ModelClassifier;

public class MergerWorkbench extends Workbench {
	ArrayList<SemSimModel> loadedmodels = new ArrayList<SemSimModel>();	
	int selection = -1;
	
	public enum MergeEvent {
		functionalsubmodelerr, threemodelerror, modellistupdated, modelerrors;
		
		String message = null;
		
		private MergeEvent() {}
		
		public boolean hasMessage() {
			return message != null;
		}
		
		public String getMessage() {
			String msg = message;
			message = null;
			return msg;
		}
		
		public void setMessage(String msg) {
			message = msg;
		}
	}
	
	@Override
	public void initialize() {

	}

	private SemSimModel loadModel(String filepath, boolean autoannotate) {
		File file = new File(filepath);
		return loadModel(file, autoannotate);
	}
	
	private SemSimModel loadModel(File file, boolean autoannotate) {
		SemSimModel modeltoload = LoadSemSimModel.loadSemSimModelFromFile(file, autoannotate);
		
		if(modeltoload.getFunctionalSubmodels().size()>0) {
			CellMLModelError(file.getName());
		}
		
		return modeltoload;
	}
	
	public boolean addModels(Set<File> files, boolean autoannotate) {
		if (loadedmodels.size() == 2) {
			setChanged();
			notifyObservers(MergeEvent.threemodelerror);
			return false;
		}
		
		for (File file : files) {
			if (ModelClassifier.classify(file)==ModelClassifier.CELLML_MODEL) {
				CellMLModelError(file.getName());
				continue;
			} 
			loadedmodels.add(loadModel(file, autoannotate));
		}

		notifyModelListUpdated();
		return true;
	}
	
	public void reloadModel(int index, boolean autoannotate) {
		String path = loadedmodels.get(index).getLegacyCodeLocation();
		loadedmodels.set(index, loadModel(path, autoannotate));
	}
	
	public void reloadAllModels(boolean autoannotate) {
		for (int i=0; i<loadedmodels.size(); i++) {
			reloadModel(i, autoannotate);
		}
		notifyModelListUpdated();
	}
	
	public void removeSelectedModel() {
		if (selection == -1) return;
		loadedmodels.remove(selection);
		notifyModelListUpdated();
	}
	
	//Temporary
	public SemSimModel getModel(int index) {
		return loadedmodels.get(index);
	}
	
	public boolean hasMultipleModels() {
		return (loadedmodels.size() > 1);
	}
	
	public boolean validateModels() {
		for (SemSimModel model : loadedmodels) {
			if (!model.getErrors().isEmpty()) {
				MergeEvent.functionalsubmodelerr.setMessage(model.getName());
				setChanged();
				notifyObservers(MergeEvent.functionalsubmodelerr);
				return false;
			}
		}
		
		return true;
	}
	
	public ArrayList<String> getModelNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (SemSimModel model : loadedmodels) {
			names.add(model.getName());
		}
		return names;
	}
	
	public void setSelection(int index) {
		selection = index;
	}
	
	@Override
	public boolean getModelSaved() {
		return false;
	}

	@Override
	public void setModelSaved(boolean val) {
		
	}

	@Override
	public String getCurrentModelName() {
		if (selection == -1) return null;
		return loadedmodels.get(selection).getName();
	}

	@Override
	public String getModelSourceFile() {
		if (selection == -1) return null;
		return loadedmodels.get(selection).getLegacyCodeLocation();
	}

	@Override
	public File saveModel() {
		return null;
	}

	@Override
	public File saveModelAs() {
		return null;
	}

	private void CellMLModelError(String name) {
		MergeEvent.functionalsubmodelerr.setMessage(name);
		setChanged();
		notifyObservers(MergeEvent.functionalsubmodelerr);
		notifyModelListUpdated();
	}
	
	private void notifyModelListUpdated() {
		selection = -1;
		setChanged();
		notifyObservers(MergeEvent.modellistupdated);
	}
	
}
