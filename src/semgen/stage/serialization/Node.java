package semgen.stage.serialization;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semsim.SemSimObject;

/**
 * Represents a node in a d3 graph
 * 
 * @author Ryan
 *
 */
public class Node<T extends SemSimObject> {
	@Expose public String id;
	@Expose public String name;
	@Expose public String parentModelId = "";
	@Expose public int xpos = -1;
	@Expose public int ypos = -1;
	@Expose public Boolean hidden = false;
	@Expose public Number typeIndex;

	protected T sourceobj;
	protected Node<?> parent = null;
	
	//Named node
	protected Node(String name, Number type) {
		this.name = name;
		this.id = name;
		this.parentModelId = name;
		this.typeIndex = type;
	}
	
	//Named node with parent
	protected Node(String name, Node<?> parent) {
		this.name = name;
		this.id = name;
		this.parentModelId = name;
	}

	protected Node(T obj) {
		this.sourceobj = obj;
		this.name = obj.getName();
		this.id =  this.name;
	}
	
	protected Node(T obj, Node<?> parent) {
		this.sourceobj = obj;
		this.name = obj.getName();
		this.parent = parent;
		this.parentModelId = parent.parentModelId;
		this.id =  parent.id + "." + this.name;
	}
	
	protected Node(T obj, Node<?> parent, Number typeindex) {
		this.sourceobj = obj;
		this.name = obj.getName();
		this.parent = parent;
		this.parentModelId = parent.parentModelId;
		this.id =  parent.id + "." + this.name;
		typeIndex = typeindex;
	}
	
	//Node type array - must correspond to var NodeType in definitions.js 
	protected static ArrayList<String> nodetypes;
	
	static {
		ArrayList<String> dtarray = new ArrayList<String>();

		dtarray.add("Model");
		dtarray.add("Submodel");
		dtarray.add("State");
		dtarray.add("Rate");
		dtarray.add("Constitutive");
		dtarray.add("Entity");
		dtarray.add("Process");
		dtarray.add("Mediator");
		dtarray.add("Null");

		nodetypes = dtarray;
	}
	
	static Number MODEL = 0;
	static Number SUBMODEL = 1;
	static Number STATE = 2;
	static Number RATE = 3;
	static Number CONSTITUTIVE = 4;
	static Number ENTITY = 5;
	static Number PROCESS = 6;
	static Number MEDIATOR = 7;
	static Number NULL = 8;
	
	public Node<? extends SemSimObject> getFirstAncestor() {
		Node<? extends SemSimObject> par =  parent;
		while (par!=null) {
			par = parent.parent;
		}
		
		return par;
	}
}
