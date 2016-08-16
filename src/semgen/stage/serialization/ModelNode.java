package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.annotations.Expose;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class ModelNode extends ParentNode<SemSimModel>{
	@Expose public PhysioMap physionetwork;
	@Expose public Integer modelindex;
	
	//null model
	public ModelNode(String modname) {
		super(modname, MODEL);
	}
	
	public ModelNode(SemSimModel sourcemod, Integer modindex) {
		super(sourcemod);
		serializeModel();
		generatePhysioMapNetwork();
		typeIndex = MODEL;
		modelindex = modindex;
	}
	
	private void serializeModel() {
		ArrayList<Submodel> topsubmodels = sourceobj.getTopSubmodels();
		HashMap<DataStructure, DependencyNode> depnodemap = new HashMap<DataStructure, DependencyNode>();
		HashSet<DataStructure> bounds = sourceobj.getSolutionDomainBoundaries();
		for(Submodel subModel : topsubmodels){
			SubModelNode newsmnode = new SubModelNode(subModel, this);
			depnodemap.putAll(newsmnode.serialize(bounds));
			childsubmodels.add(newsmnode);
		}
		for (DataStructure ads : sourceobj.getAssociatedDataStructures()) {
			if (bounds.contains(ads)) continue;
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

}
