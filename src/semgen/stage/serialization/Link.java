package semgen.stage.serialization;

import java.util.ArrayList;

public class Link {
	public String sourceId;
	public String sinkId;
	public String parentModelId;
	public String label;
	public ArrayList<String> mediators;
	
	public Link(String sourceId, String parentModelId) {
		this.sourceId = sourceId;
		this.parentModelId = parentModelId;
	}

	public Link(String sourceId, String sinkId, String parentModelId, String label, ArrayList<String> mediators) {
		this.sourceId = sourceId;
		this.sinkId = sinkId;
		this.parentModelId = parentModelId;
		this.label = label;
		this.mediators = mediators;
	}
}
