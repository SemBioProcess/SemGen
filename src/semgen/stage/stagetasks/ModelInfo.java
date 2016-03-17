package semgen.stage.stagetasks;

import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public class ModelInfo {
	public SemSimModel Model;
	public ModelAccessor accessor;
	
	public ModelInfo(SemSimModel model, ModelAccessor path) {
		Model = model;
		accessor = path;
	}
	
	public String getModelName() {
		return Model.getName();
	}
}
