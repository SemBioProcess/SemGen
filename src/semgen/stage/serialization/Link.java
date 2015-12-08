package semgen.stage.serialization;

public class Link {
	public String name;
	public String sourceId;
	public String sinkId;
	public String parentModelId;
	public String label;
	public String linkType;

	public Link(String name, String parentModelId) {
		this.name = name;
		this.sourceId = Node.buildId(name, parentModelId);
		this.parentModelId = parentModelId;
	}

	public Link(String sourceId, String sinkId, String parentModelId, String linkType) {
		this.sourceId = sourceId;
		this.sinkId = sinkId;
		this.parentModelId = parentModelId;
		this.linkType = linkType;
	}
}
