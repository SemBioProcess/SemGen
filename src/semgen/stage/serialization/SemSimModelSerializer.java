package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class SemSimModelSerializer {

	/**
	 * Gets the SemSim model dependency network, which defines
	 * nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get dependency network from
	 * @return Dependencies
	 */
	public static DependencyNode[] getDependencyNetwork(SemSimModel semSimModel)
	{
		// Get solution domain declarations
		Set<DataStructure> domaincodewords = new HashSet<DataStructure>();
		for(DataStructure ds : semSimModel.getSolutionDomains()){
			domaincodewords.add(semSimModel.getAssociatedDataStructure(ds.getName() + ".min"));
			domaincodewords.add(semSimModel.getAssociatedDataStructure(ds.getName() + ".max"));
			domaincodewords.add(semSimModel.getAssociatedDataStructure(ds.getName() + ".delta"));
		}

		// Loop over all of the data structures (variables) and create nodes for them
		ArrayList<DependencyNode> dependencies = new ArrayList<DependencyNode>();
		for(DataStructure dataStructure : semSimModel.getAssociatedDataStructures()){
			// If the data structure is part of a solution domain declaration or
			// it is not used to compute any other terms, ignore it.
			if(dataStructure.isSolutionDomain() ||
					domaincodewords.contains(dataStructure) && dataStructure.getUsedToCompute().isEmpty())
			{
				continue;
			}

			dependencies.add(new DependencyNode(dataStructure, semSimModel.getName()));
		}

		return dependencies.toArray(new DependencyNode[dependencies.size()]);
	}
	
	/**
	 * Get the SemSim model submodel network, which defines
	 * submodels and their internal dependency networks that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get submodel network from
	 * @return submodel network
	 */
	public static SubModelNode[] getSubmodelNetwork(SemSimModel semSimModel)
	{
		ArrayList<SubModelNode> subModelNetwork = new ArrayList<SubModelNode>();
		for(Submodel subModel : semSimModel.getSubmodels()){
			subModelNetwork.add(new SubModelNode(subModel, semSimModel.getName()));
		}
		
		return subModelNetwork.toArray(new SubModelNode[subModelNetwork.size()]);
	}
	
	/**
	 * Get the SemSim model PhysioMap network, which defines
	 * nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get PhysioMap network from
	 * @return PhysioMap network
	 */
	public static PhysioMapNode[] getPhysioMapNetwork(SemSimModel semSimModel) {

		HashMap<String, PhysioMapNode> nodeMap = new HashMap<String, PhysioMapNode>();
		ArrayList<PhysioMapNode> physioMapNetwork = new ArrayList<PhysioMapNode>();

		Set<PhysicalProcess> processSet = semSimModel.getPhysicalProcesses();

		for(PhysicalProcess proc : processSet) {

			updateNodeMapForProcess(
					semSimModel.getName(),
					proc.getName(),
					proc.getSourcePhysicalEntities(),
					proc.getSinkPhysicalEntities(),
					proc.getMediatorPhysicalEntities(),
					nodeMap);
		}

		physioMapNetwork.addAll(nodeMap.values());
		return physioMapNetwork.toArray(new PhysioMapNode[physioMapNetwork.size()]);
	}
	
	private static void updateNodeMapForProcess(String parentModelId, String processName, Set<PhysicalEntity> sources, Set<PhysicalEntity> sinks, Set<PhysicalEntity> mediators, HashMap<String, PhysioMapNode> nodeMap) {
		for(PhysicalEntity source : sources) {
			for(PhysicalEntity sink : sinks) {
				ArrayList<String> mediatorsNames = new ArrayList<String>();

				// If the source or sink is not specified, set it to "Null node"
				String sinkName = sink.getName();
				if(sinkName == "") sinkName = "Null node";
				String sourceName = source.getName();
				if(sourceName == "") sourceName = "Null node";

				getOrCreatePhysioMapNode(sourceName, parentModelId, nodeMap, "Entity");
				getOrCreatePhysioMapNode(sinkName, parentModelId, nodeMap, "Entity");
				PhysioMapNode procNode = getOrCreatePhysioMapNode(processName, parentModelId, nodeMap, "Process");

				String processId = Node.buildId(processName, parentModelId);

				procNode.inputs.add(new Link(
						processId,
						Node.buildId(sinkName, parentModelId),
						parentModelId,
						null
				));

				procNode.inputs.add(new Link(
						Node.buildId(sourceName, parentModelId),
						processId,
						parentModelId,
						null
				));

				for(PhysicalEntity mediator : mediators) {
					getOrCreatePhysioMapNode(mediator.getName(), parentModelId, nodeMap, "Mediator");
					procNode.inputs.add(new Link(
							Node.buildId(mediator.getName(), parentModelId),
							processId,
							parentModelId,
							null
					));
				}
			}
		}

	}

	private static PhysioMapNode getOrCreatePhysioMapNode(String name, String parentModelId, HashMap<String, PhysioMapNode> nodeMap, String nodeType){
		PhysioMapNode node = nodeMap.get(name);
		if(node == null){
			node = new PhysioMapNode(name, parentModelId, nodeType);
			nodeMap.put(name, node);
		}

		return node;
	}

}
