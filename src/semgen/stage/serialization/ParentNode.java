package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import semsim.model.SemSimComponent;
import semsim.model.collection.SemSimCollection;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class ParentNode<T extends SemSimCollection> extends Node<T> {
	@Expose public ArrayList<SubModelNode> childsubmodels = new ArrayList<SubModelNode>();
	@Expose public ArrayList<DependencyNode> dependencies = new ArrayList<DependencyNode>();
	@Expose public int[] deptypecounts = {0, 0 ,0};

	
	protected ParentNode(String name, Number type) {
		super(name, type);
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
	
	public HashMap<DataStructure, DependencyNode> serialize() {
		HashMap<DataStructure, DependencyNode> depmap = new HashMap<DataStructure, DependencyNode>();
		if (!sourceobj.getSubmodels().isEmpty()) {
			for (Submodel sm : sourceobj.getSubmodels()) {
				SubModelNode smn = new SubModelNode(sm, this);
				depmap.putAll(smn.serialize());
				childsubmodels.add(smn);
			}
		}
		for(DataStructure dependency : sourceobj.getAssociatedDataStructures()) {
			DependencyNode sdn = new DependencyNode(dependency, this);
			depmap.put(dependency, sdn);
			dependencies.add(sdn);
			incrementType(sdn.typeIndex);
		}
		
		return depmap;
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
	
	public DependencyNode getDependencyNode(DataStructure sourceds) {
			for (DependencyNode dn : dependencies) {
				if (dn.sourceobj == sourceds) {
					return dn;
				}
			}
			for (SubModelNode smn : childsubmodels) {
				DependencyNode dn = smn.getDependencyNode(sourceds);
				if (dn!=null) return dn;
			}
			return null;
		}

	public ArrayList<Link> findLinksWithInput(LinkableNode<? extends SemSimComponent> node) {
		ArrayList<Link> links = new ArrayList<Link>();
		
		for (DependencyNode dnode : dependencies) {
			if (node == dnode) continue;
			for (Link link : dnode.inputs) {
				if (link.hasInput(dnode)) links.add(link);
			}
		}
		for (SubModelNode smn : this.childsubmodels) {
			links.addAll(smn.findLinksWithInput(node));
		}
		return links;
	}
	
	protected void incrementType(Number type) {
		deptypecounts[(int) type-2]++;
	}
	
	protected void copyInLinkedDependencyArray(ArrayList<DependencyNode> linkeddeps) {
		HashMap<DependencyNode, DependencyNode> copymap = new HashMap<DependencyNode, DependencyNode>();
		ArrayList<DependencyNode> newlinkeddeps = new ArrayList<DependencyNode>();
		for (DependencyNode dn : linkeddeps) {
			DependencyNode copy = new DependencyNode(dn, this);
			newlinkeddeps.add(copy);
			copymap.put(dn, copy);
		}
		
		for (DependencyNode dn : newlinkeddeps) {
			dn.replaceLinks(copymap);
		}
		dependencies.addAll(newlinkeddeps);
	}
}
