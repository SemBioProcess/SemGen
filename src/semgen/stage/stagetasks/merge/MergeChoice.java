package semgen.stage.stagetasks.merge;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.annotations.Expose;

import semgen.stage.serialization.DependencyNode;
import semgen.stage.serialization.Link;
import semgen.stage.serialization.LinkableNode;
import semgen.stage.serialization.ModelNode;
import semgen.stage.serialization.SubModelNode;

public class MergeChoice {
	@Expose public ModelNode[] choices = new ModelNode[]{null, null};

	public MergeChoice(Pair<DependencyNode, DependencyNode> dsoverlap, ArrayList<ModelNode> models) {
		choices[0] = generatePreview(dsoverlap, models, 0);
		choices[1] = generatePreview(dsoverlap, models, 1);
	}

	//Create the model nodes containing the two choices
	private ModelNode generatePreview(Pair<DependencyNode, DependencyNode> dsoverlap, ArrayList<ModelNode> models, int target) {
		//Determine which node and model is to be kept
		boolean useleft = target==0;
		int discardmod = target==0 ? 1 : 0;
		DependencyNode keepnode = useleft ? dsoverlap.getLeft() : dsoverlap.getRight();
		DependencyNode discardnode = !useleft ? dsoverlap.getLeft() : dsoverlap.getRight();
		
		//Create deep copies of the target and discard nodes and nodes linked to them.
		//Place in a submodel
		Pair<SubModelNode, DependencyNode> newfocus = createCopyofNodeandLinks(keepnode, models.get(target));
		Pair<SubModelNode, DependencyNode> newinputs = createCopyofNodeandLinks(discardnode, models.get(discardmod));
		
		//Find all dependencies with <node> as input and then replace it with the selected node
		ArrayList<DependencyNode> inputs = newinputs.getLeft().findAllDependenciesWithInput(newinputs.getRight());
		for (DependencyNode input : inputs) {
			ArrayList<Link> newlinklist = new ArrayList<Link>();
			for (Link link : input.inputs) {
				if (link.input == newinputs.getRight()) {
					link.input = newfocus.getRight();
					newlinklist.add(link);
				}
				newlinklist.add(link);
			}
			input.inputs = newlinklist;
		}
		SubModelNode inputsm = newinputs.getLeft();
		inputsm.setDependencies(inputs);
		
		String modelside = (target==0) ? "left" : "right";
		ModelNode mnode = new ModelNode(modelside);
		 mnode.childsubmodels.add(inputsm);
		 mnode.childsubmodels.add(newfocus.getLeft());
		 
		 //mnode.prefixChildrenwithID(mnode.id);
		 return mnode;
	}
	
	private Pair<SubModelNode, DependencyNode> createCopyofNodeandLinks(DependencyNode centralnode, ModelNode modelnode) {		
		
		SubModelNode smn = new SubModelNode(modelnode.name);
		DependencyNode newfocus = new DependencyNode(centralnode, smn);
		
		HashMap<LinkableNode<?>, LinkableNode<?>> copymap = new HashMap<LinkableNode<?>, LinkableNode<?>>();
		copymap.put(centralnode, newfocus);
		
		ArrayList<DependencyNode> newlinkeddeps = new ArrayList<DependencyNode>();
		newlinkeddeps.add(newfocus);
			
		for (DependencyNode dn : modelnode.findAllDependenciesConnectedto(centralnode)) {
			DependencyNode copy = new DependencyNode(dn, smn);
			newlinkeddeps.add(copy);
			copymap.put(dn, copy);
		}
			
		for (DependencyNode dn : newlinkeddeps) {
			dn.replaceLinkInputs(copymap);
		}
		
		smn.dependencies = newlinkeddeps;
		
		return Pair.of(smn, newfocus);
	}
}
