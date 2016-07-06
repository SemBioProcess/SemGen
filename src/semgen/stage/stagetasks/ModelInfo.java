package semgen.stage.stagetasks;

import semgen.stage.serialization.ModelNode;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public class ModelInfo {
	public SemSimModel Model;
	public ModelAccessor accessor;
	public ModelNode modelnode;
	
	public ModelInfo(SemSimModel model, ModelAccessor path) {
		Model = model;
		accessor = path;
		modelnode = new ModelNode(model);
	}
	
	public ModelInfo(ModelInfo info) {
		Model = Model.clone();
		accessor = null;
		modelnode = new ModelNode(Model);
	}
	
	public String getModelName() {
		return Model.getName();
	}
}
