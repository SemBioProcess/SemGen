package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.datastructures.DataStructure;
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

			Set<PhysicalProcess> allProcesses = model.sourceobj.getPhysicalProcesses();
			Set<PhysicalProcess> processesWithProperties = new HashSet<PhysicalProcess>();
			
			// Processes that aren't physical components of data structures and that don't 
			// have any specified participants are excluded from PhysioMap. These include processes
			// that are just used for classifying other processes in the model. For example, 
			// a reference process from GO used in an isVersionOf statement on a custom process would
			// be excluded.
			
			// Here we identify those processes that have associated data structures (i.e. those
			// whose properties are computed by the model)
			for(DataStructure ds : model.sourceobj.getDataStructureswithPhysicalProcesses()){
				processesWithProperties.add((PhysicalProcess)ds.getAssociatedPhysicalModelComponent());
			}
			
			// Here we add the process nodes to the graph, excluding those that have no participants
			// and no data structures that compute their physical property
			for(PhysicalProcess proc : allProcesses) {
				
				if(proc.hasParticipants() || processesWithProperties.contains(proc)){
					processes.add(makePhysioMapNode(proc));
				}
			}

			//Add any entities not part of a process.
			for (DataStructure ds : model.getSourceObject().getAssociatedDataStructures()) {
				if (ds.getAssociatedPhysicalModelComponent()!=null) {
					if (ds.getAssociatedPhysicalModelComponent().isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)) {
							getParticipantNode((PhysicalEntity)ds.getAssociatedPhysicalModelComponent());
					}
				
				}
			}
			entities.addAll(nodeMap.values());
		}
		
		private PhysioMapNode makePhysioMapNode(PhysicalProcess proc){

			PhysioMapNode pmnode = new PhysioMapNode(proc, model);
			
			// Add source nodes
			if(proc.getSourcePhysicalEntities().isEmpty()) {
				pmnode.addSourceLink(getNullNode(proc, "source"));
			} 
			else {
				
				for (PhysicalEntity part : proc.getSourcePhysicalEntities()) {
					pmnode.addSourceLink(getParticipantNode(part));
				}
			}

			// Add sink nodes
			if(proc.getSinkPhysicalEntities().isEmpty()) {
				pmnode.addSinkLink(getNullNode(proc, "sink"));
			} 
			else {
				
				for (PhysicalEntity part : proc.getSinkPhysicalEntities()) {
					pmnode.addSinkLink(getParticipantNode(part));
				}
			}

			// Add mediator nodes
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
			PhysioMapEntityNode nullNode = new PhysioMapEntityNode(cpe, model, Node.UNSPECIFIED);
			nodeMap.put(cpe, nullNode);
			return nullNode;
		}

	}
}
