package semgen.stage.stagetasks;

import java.io.File;

import semsim.model.collection.SemSimModel;

public class ModelInfo {
	public SemSimModel Model;
	public File Path;
	
	public ModelInfo(SemSimModel model, File path) {
		Model = model;
		Path = path;
	}
	
	public String getModelName() {
		return Model.getName();
	}
}
