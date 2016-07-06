package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimCollection;
import semsim.model.computational.datastructures.DataStructure;

public class ParentNode<T extends SemSimCollection> extends Node<T> {
	@Expose public ArrayList<SubModelNode> childsubmodels = new ArrayList<SubModelNode>();
	@Expose public ArrayList<DependencyNode> dependencies = new ArrayList<DependencyNode>();

	
	protected ParentNode(String name, SemSimTypes type) {
		super(name);
		this.type = type.getSparqlCode();
	}

	public ParentNode(T collection, Node<? extends SemSimCollection> parent) {
		super(collection, parent);
	}

	public ParentNode(T collection) {
		super(collection);
	}
	
	public ArrayList<DependencyNode> requestAllChildDependencies() {
		ArrayList<DependencyNode> alldeps = new ArrayList<DependencyNode>(dependencies);
		for (SubModelNode csm : childsubmodels) {
			alldeps.addAll(csm.requestAllChildDependencies());
		}
		return alldeps;
	}
	
	public HashMap<DataStructure, DependencyNode> getDataStructureandNodeMap() {
		HashMap<DataStructure, DependencyNode> dsnodemap = new HashMap<DataStructure, DependencyNode>();
		for (SubModelNode child : childsubmodels) {
			dsnodemap.putAll(child.getDataStructureandNodeMap());
		}
		for (DependencyNode dep : dependencies) {
			dsnodemap.put(dep.sourceobj, dep);
		}
		return dsnodemap;
	}
}
