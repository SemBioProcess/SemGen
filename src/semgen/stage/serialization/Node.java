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
	@Expose public String type;

	protected T sourceobj;
	protected Node<?> parent = null;
	
	//Named node
	protected Node(String name) {
		this.name = name;
		this.id = name;
		this.parentModelId = name;
		type = sourceobj.getSemSimType().getSparqlCode();
	}
	
	//Named node with parent
	protected Node(String name, Node<?> parent) {
		this.name = name;
		this.id = name;
		this.parentModelId = name;
		type = sourceobj.getSemSimType().getSparqlCode();
	}

	protected Node(T obj) {
		this.sourceobj = obj;
		this.name = obj.getName();
		this.id =  this.name;
		type = sourceobj.getSemSimType().getSparqlCode();
	}
	
	protected Node(T obj, Node<?> parent) {
		this.sourceobj = obj;
		this.name = obj.getName();
		this.parent = parent;
		this.parentModelId = parent.parentModelId;
		this.id =  parent.id + "." + this.name;
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
	
	public Node<? extends SemSimObject> getFirstAncestor() {
		Node<? extends SemSimObject> par =  parent;
		while (par!=null) {
			par = parent.parent;
		}
		
		return par;
	}
}
