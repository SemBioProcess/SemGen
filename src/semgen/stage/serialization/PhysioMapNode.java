package semgen.stage.serialization;

public class PhysioMapNode extends Node {

	public String nodeType;

	public PhysioMapNode(String name, String parentModelId, String nodeType) {
		super(name, parentModelId);

		this.nodeType = nodeType;
	}
}
