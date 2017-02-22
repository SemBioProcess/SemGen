package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

import com.google.gson.annotations.Expose;
import semsim.model.physical.object.CustomPhysicalEntity;

public class PhysioMap {
	@Expose public ArrayList<PhysioMapNode> processes = new ArrayList<PhysioMapNode>();
	@Expose public ArrayList<PhysioMapEntityNode> entities = new ArrayList<PhysioMapEntityNode>();
	
	
	public PhysioMap(ModelNode model) {
		new PhysiomapFactory(model);
	}
	
	public Node<?> getPhysioMapNodebyHash(int nodehash, String nodeid) {
		for (PhysioMapNode proc : processes) {
			if (proc.isJavaScriptNode(nodehash, nodeid)) return proc;
		}
		for (LinkableNode<PhysicalEntity> entity : entities) {
			if (entity.isJavaScriptNode(nodehash, nodeid)) return entity;
		}
		return null;
	}
	
	private class PhysiomapFactory {
		private HashMap<PhysicalEntity, PhysioMapEntityNode> nodeMap = new HashMap<PhysicalEntity, PhysioMapEntityNode>();
		private ModelNode model;
		
		public PhysiomapFactory(ModelNode mod) {
			model = mod;
			generatePhysioMapNetwork(); 
		}
		
		/**
		 * Get the SemSim model PhysioMap network, which defines
		 * nodes and links that the d3.js engine will use
		 * to create visualizations
		 * 
		 * @param semSimModel - SemSim model to get PhysioMap network from
		 * @return PhysioMap network
		 */
		public void generatePhysioMapNetwork() {

			Set<PhysicalProcess> processSet = model.sourceobj.getPhysicalProcesses();

			for(PhysicalProcess proc : processSet) {
				processes.add(makePhysioMapNode(proc));
			}

			entities.addAll(nodeMap.values());
			
		}
		
		private PhysioMapNode makePhysioMapNode(PhysicalProcess proc){
			boolean hasSource = !proc.getSourcePhysicalEntities().isEmpty();
			boolean hasSink = !proc.getSinkPhysicalEntities().isEmpty();
//			Don't add processes that doesn't have associated physical property
//			boolean hasPhysicalProperty = proc.?();

			PhysioMapNode pmnode = new PhysioMapNode(proc, model);
			if(!hasSource && hasSink) {
				pmnode.addSourceLink(getNullNode(proc, "source"));
			} else {
				for (PhysicalEntity part : proc.getSourcePhysicalEntities()) {
					pmnode.addSourceLink(getParticipantNode(part));
				}
			}

			if(!hasSink && hasSource) {
				pmnode.addSinkLink(getNullNode(proc, "sink"));
			} else {
				for (PhysicalEntity part : proc.getSinkPhysicalEntities()) {
					pmnode.addSinkLink(getParticipantNode(part));
				}
			}

			for (PhysicalEntity part : proc.getMediatorPhysicalEntities()) {
				pmnode.addMediatorLink(getParticipantNode(part));
			}

			return pmnode;
		}
		
		
		private PhysioMapEntityNode getParticipantNode(PhysicalEntity cpe) {
			PhysioMapEntityNode cpenode = nodeMap.get(cpe);
			if (cpenode==null) {
				cpenode = new PhysioMapEntityNode(cpe, model);
				nodeMap.put(cpe, cpenode);
			}
			return cpenode;
		}

		private PhysioMapEntityNode getNullNode(PhysicalProcess proc, String sinkOrSource) {
			PhysicalEntity cpe = new CustomPhysicalEntity("Null " + proc.getName() + sinkOrSource, "Null PhysioMap node");
			PhysioMapEntityNode nullNode = new PhysioMapEntityNode(cpe, model, Node.NULL);
			nodeMap.put(cpe, nullNode);
			return nullNode;
		}

	}
}
