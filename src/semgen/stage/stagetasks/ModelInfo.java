package semgen.stage.stagetasks;

import semgen.stage.serialization.ModelNode;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public class ModelInfo {
	public SemSimModel Model;
	public ModelAccessor accessor;
	public ModelNode modelnode;
	
	public ModelInfo(SemSimModel model, ModelAccessor path, Integer modindex) {
		Model = model;
		accessor = path;
		modelnode = new ModelNode(model, modindex);
	}
	
	public ModelInfo(ModelInfo info, Integer modindex) {
		Model = info.Model.clone();
		accessor = info.accessor;
		modelnode = new ModelNode(Model, modindex);
	}
	
	public String getModelName() {
		return Model.getName();
	}
}
