package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.annotations.Expose;

import semsim.model.SemSimComponent;
import semsim.model.collection.SemSimCollection;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class ParentNode<T extends SemSimCollection> extends Node<T> {
	@Expose public ArrayList<SubModelNode> childsubmodels = new ArrayList<SubModelNode>();
	@Expose public ArrayList<DependencyNode> dependencies = new ArrayList<DependencyNode>();
	@Expose public int[] deptypecounts = {0, 0 ,0};
	@Expose public Boolean expanded = false;

	
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
	
	public HashMap<DataStructure, DependencyNode> serialize(HashSet<DataStructure> soldomainbounds) {
		HashMap<DataStructure, DependencyNode> depmap = new HashMap<DataStructure, DependencyNode>();
		if (!sourceobj.getSubmodels().isEmpty()) {
			for (Submodel sm : sourceobj.getSubmodels()) {
				SubModelNode smn = new SubModelNode(sm, this);
				depmap.putAll(smn.serialize(soldomainbounds));
				childsubmodels.add(smn);
			}
		}
		for(DataStructure dependency : sourceobj.getAssociatedDataStructures()) {
			if (soldomainbounds.contains(dependency)) continue;
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

	public ArrayList<Link> findAllLinksContaining(LinkableNode<? extends SemSimComponent> node) {
		ArrayList<Link> links = new ArrayList<Link>();
		for (LinkableNode<?> dnode : dependencies) {
			for (Link link : dnode.inputs) {
				if (link.input == link.output) continue;
				if (link.hasInput(node) || link.hasOutput(node)) links.add(link);
			}
		}
		for (SubModelNode smn : this.childsubmodels) {
			links.addAll(smn.findAllLinksContaining(node));
		}
		return links;
	}
	
	public ArrayList<DependencyNode> findAllDependenciesConnectedto(DependencyNode node) {
		ArrayList<Link> links = findAllLinksContaining(node);
		ArrayList<DependencyNode> linkednodes = new ArrayList<DependencyNode>();
		for (Link link : links) {
			if (link.input == node) {
				linkednodes.add((DependencyNode) link.output);
			}
			else {
				linkednodes.add((DependencyNode) link.input);
			}
		}

		return linkednodes;
	}
	
	public ArrayList<DependencyNode> findAllDependenciesWithInput(DependencyNode node) {
		ArrayList<Link> links = findAllLinksContaining(node);
		ArrayList<DependencyNode> linkednodes = new ArrayList<DependencyNode>();
		for (Link link : links) {
			if (link.input == node) {
				linkednodes.add((DependencyNode) link.output);
			}
		}

		return linkednodes;
	}
	
	public void setDependencies(ArrayList<DependencyNode> newdeps) {
		dependencies = newdeps;
	}
	
	protected void incrementType(Number type) {
		deptypecounts[(int) type-2]++;
	}
}
