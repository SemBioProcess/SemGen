package semgen.stage.serialization;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semgen.stage.stagetasks.extractor.Extractor;
import semsim.SemSimObject;

/**
 * Represents a node in a d3 graph
 * 
 * @author Ryan
 *
 */
public abstract class Node<T extends SemSimObject> {
	@Expose public String id;
	@Expose public String name;
	@Expose public String parentModelId = "";
	@Expose public int xpos = -1;
	@Expose public int ypos = -1;
	@Expose public Boolean hidden = false;
	@Expose public Number typeIndex;
	//Record Java hashcode for rapid lookup
	@Expose public int hash = this.hashCode();

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
		this.id =  parent.id + "." + getBaseName(this.name);
	}
	
	protected Node(T obj, Node<?> parent, Number typeindex) {
		this.sourceobj = obj;
		this.name = obj.getName();
		this.parent = parent;
		this.parentModelId = parent.parentModelId;
		this.id =  parent.id + "." + this.name;
		typeIndex = typeindex;
	}
	
	//Copy constructor
	protected Node(Node<T> original) {
		this.sourceobj = original.sourceobj;
		this.name = new String(original.name);
		this.parent = original.parent;
		this.parentModelId = new String(original.parentModelId);
		this.id =  new String(original.id);
		this.typeIndex = original.typeIndex;
	}
	
	//Copy constructor with new parent
	protected Node(Node<T> original, Node<?> parent) {
		this.sourceobj = original.sourceobj;
		this.name = new String(original.name);
		this.parent = parent;
		this.parentModelId = new String(original.parentModelId);
		this.id =  new String(original.id);
		this.typeIndex = original.typeIndex;
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
		dtarray.add("Extraction");

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
	static Number EXTRACTION = 9;
	
	public Node<? extends SemSimObject> getFirstAncestor() {
		Node<? extends SemSimObject> par =  parent;
		while (par.parent!=null) {
			par = par.parent;
		}
		
		return par;
	}
	
	//Get the base name (remove any parent names that may be in the DataStructure name)
	protected String getBaseName(String name) {
		int lastper = name.lastIndexOf('.');
		if (lastper>-1) {
			name = name.substring(lastper+1);
		}
		return name;
	}
	
	public String getSourceObjectName() {
		return sourceobj.getName();
	}
	
	public String getID() {
		return id;
	}
	public void setID(String newid) {
		id = newid;
	}
	
	public boolean isJavaScriptNode(int nodehash, String nodeid) {
		if (nodehash == hash) {
			//Additional check to prevent hash collision
			if (id.matches(nodeid)) {
				return true;
			}
		}
		return false;
	}
	
	public T getSourceObject() {
		return sourceobj;
	}
	
	public abstract void collectforExtraction(Extractor extractor);
}
