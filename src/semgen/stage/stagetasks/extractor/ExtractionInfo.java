package semgen.stage.stagetasks.extractor;

import semgen.stage.serialization.ExtractionNode;
import semgen.stage.stagetasks.StageRootInfo;
import semsim.model.collection.SemSimModel;

public class ExtractionInfo extends StageRootInfo<ExtractionNode>  {
	protected SemSimModel sourcemodel;
	private boolean changesSaved = false; // Whether the extraction has been saved in its current form

	public ExtractionInfo(SemSimModel source, SemSimModel model, Integer modindex) {
		super(model, null, modindex);
		sourcemodel = source;
		modelnode = new ExtractionNode(model, modindex);
	}
	
	public ExtractionInfo(ExtractionInfo info, Integer modindex) {
		super(info, modindex);
		sourcemodel = info.sourcemodel;
		modelnode = new ExtractionNode(Model, modindex);
	}
	
	public void setModel(SemSimModel model) {
		Model = model;
		modelnode = new ExtractionNode(Model, modelindex);
	}
	
	public String getModelName() {
		return Model.getName();
	}
	
	public void setChangesSaved(boolean saved){
		changesSaved = saved;
	}
	
	/** @return Whether the extraction has been saved in its current form*/
	public boolean getChangesSaved(){
		return changesSaved;
	}
}
