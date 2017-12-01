package semgen.stage.stagetasks;

import semgen.stage.serialization.ModelNode;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;

public abstract class StageRootInfo<N extends ModelNode> {
	public SemSimModel Model;
	public ModelAccessor accessor;
	public N modelnode;
	public Integer modelindex;
	
	public StageRootInfo(SemSimModel model, ModelAccessor path, Integer modindex) {
		Model = model;
		accessor = path;
		modelindex = modindex;
	}
	
	public StageRootInfo(StageRootInfo<?> info, Integer modindex) {
		accessor = info.accessor;
		this.modelindex = modindex;
		Model = info.Model.clone();
	}
	
	
	public SemSimModel getModel() {
		return Model;
	}
		
	public String getModelName() {
		return Model.getName();
	}
}
