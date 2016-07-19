package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class ModelNode extends ParentNode<SemSimModel>{
	@Expose public PhysioMap physionetwork;
	
	//null model
	public ModelNode(String modname) {
		super(modname, MODEL);
		
	}
	
	public ModelNode(SemSimModel sourcemod) {
		super(sourcemod);
		serializeModel();
		generatePhysioMapNetwork();
		typeIndex = MODEL;
	}
	
	private void serializeModel() {
		ArrayList<Submodel> topsubmodels = sourceobj.getTopSubmodels();
		HashMap<DataStructure, DependencyNode> depnodemap = new HashMap<DataStructure, DependencyNode>();
		for(Submodel subModel : topsubmodels){
			SubModelNode newsmnode = new SubModelNode(subModel, this);
			depnodemap.putAll(newsmnode.serialize());
			childsubmodels.add(newsmnode);
		}
		for (DataStructure ads : sourceobj.getAssociatedDataStructures()) {
			if (!depnodemap.containsKey(ads)) {
				DependencyNode dnode = depnodemap.put(ads, new DependencyNode(ads, this));
				incrementType(dnode.typeIndex);
				dependencies.add(dnode);
			}
		}
		
		for (DependencyNode dn : depnodemap.values()) {
			dn.setInputs(depnodemap);
		}
	}

	private void generatePhysioMapNetwork() {
		physionetwork = new PhysioMap(this);
	}
	
	public ArrayList<DependencyNode> getLinkedNodes(DependencyNode target) {
		
		ArrayList<DependencyNode> deps = new ArrayList<DependencyNode>();
		for (Link targlink : target.inputs) {
			deps.add((DependencyNode) targlink.input);
		}
		
		for (Link targlink : this.findLinksWithInput(target)) {
			deps.add((DependencyNode) targlink.output);
		}
		
		return deps;
	}
	
}
