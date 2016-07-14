package semgen.stage.serialization;

import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SemSimModelSerializer {
	private HashMap<PhysicalEntity, Node<PhysicalEntity>> nodeMap = new HashMap<PhysicalEntity, Node<PhysicalEntity>>();
	private ModelNode model;
	
	public SemSimModelSerializer(ModelNode mod) {
		model = mod;
	}
	
	/**
	 * Get the SemSim model PhysioMap network, which defines
	 * nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get PhysioMap network from
	 * @return PhysioMap network
	 */
	public ArrayList<PhysioMapNode> generatePhysioMapNetwork() {

		ArrayList<PhysioMapNode> physioMapNetwork = new ArrayList<PhysioMapNode>();

		Set<PhysicalProcess> processSet = model.sourceobj.getPhysicalProcesses();

		for(PhysicalProcess proc : processSet) {
			physioMapNetwork.add(makePhysioMapNode(proc));
		}

		return physioMapNetwork;
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
	
	private Node<PhysicalEntity> getParticipantNode(PhysicalEntity cpe) {
		Node<PhysicalEntity> cpenode = nodeMap.get(cpe);
		if (cpenode==null) {
			cpenode = new Node<PhysicalEntity>(cpe, model);
			nodeMap.put(cpe, cpenode);
		}
		return cpenode;
	}

}
