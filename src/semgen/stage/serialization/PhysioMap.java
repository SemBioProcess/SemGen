package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

import com.google.gson.annotations.Expose;

public class PhysioMap {
	@Expose public ArrayList<PhysioMapNode> processes = new ArrayList<PhysioMapNode>();
	@Expose public ArrayList<LinkableNode<PhysicalEntity>> entities = new ArrayList<LinkableNode<PhysicalEntity>>();
	
	
	public PhysioMap(ModelNode model) {
		new PhysiomapFactory(model);
	}
	
	private class PhysiomapFactory {
		private HashMap<PhysicalEntity, LinkableNode<PhysicalEntity>> nodeMap = new HashMap<PhysicalEntity, LinkableNode<PhysicalEntity>>();
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
			PhysioMapNode pmnode = new PhysioMapNode(proc, model);
			for (PhysicalEntity part : proc.getSourcePhysicalEntities()) {
				pmnode.addSourceLink(getParticipantNode(part));
			}
			for (PhysicalEntity part : proc.getSinkPhysicalEntities()) {
				pmnode.addSinkLink(getParticipantNode(part));
			}
			for (PhysicalEntity part : proc.getMediatorPhysicalEntities()) {
				pmnode.addMediatorLink(getParticipantNode(part));
			}
			
			return pmnode;
		}
		
		
		private LinkableNode<PhysicalEntity> getParticipantNode(PhysicalEntity cpe) {
			LinkableNode<PhysicalEntity> cpenode = nodeMap.get(cpe);
			if (cpenode==null) {
				cpenode = new LinkableNode<PhysicalEntity>(cpe, model, Node.ENTITY);
				nodeMap.put(cpe, cpenode);
			}
			return cpenode;
		}

	}
}
