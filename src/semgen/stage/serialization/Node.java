package semgen.stage.serialization;

import java.util.ArrayList;

/**
 * Represents a node in a d3 graph
 * 
 * @author Ryan
 *
 */
public abstract class Node {
	public String id;
	public String name;
	public String parentModelId;
	public int xpos = -1;
	public int ypos = -1;
	public Boolean hidden = false;
	public ArrayList<Link> inputs = new ArrayList<Link>();
	
	public Node(String name) {
		this.name = name;
		this.id = buildId(this.name);
	}

	public Node(String name, String parentModelId) {
		this.name = name;
		this.parentModelId = parentModelId;
		this.id = buildId(this.name, this.parentModelId);
	}
	
	public static String buildId(String name) {
		return name;
	}
	
	public static String buildId(String name, String parentModelId) {
		return parentModelId + name;
	}
	//Dependency type array
	protected static ArrayList<String> deptypes;
	
	static {
		ArrayList<String> dtarray = new ArrayList<String>();
		dtarray.add("State");
		dtarray.add("Rate");
		dtarray.add("Constitutive");
		deptypes = dtarray;
	}
}
