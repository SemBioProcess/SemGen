package semgen.stage.serialization;

import java.util.ArrayList;

public class Link {
	public String name;
	public String sourceId;
	public String sinkId;
	public String parentModelId;
	public String label;
	public ArrayList<String> mediators;
	
	public Link(String name, String parentModelId) {
		this.name = name;
		this.sourceId = Node.buildId(name, parentModelId);
		this.parentModelId = parentModelId;
	}

	public Link(String sourceId, String sinkId, String parentModelId, String label) {
		this.sourceId = sourceId;
		this.sinkId = sinkId;
		this.parentModelId = parentModelId;
		this.label = label;
	}
}
