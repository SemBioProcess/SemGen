package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

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
			
			dependencies.add(new DependencyNode(dataStructure));
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
}