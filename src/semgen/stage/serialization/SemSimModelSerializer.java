package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import semgen.visualizations.JsonString;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;

public class SemSimModelSerializer {

	/**
	 * Gets the SemSim model dependency network, which defines
	 * nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get dependency network from
	 * @return Dependencies
	 */
	public static ArrayList<DependencyNode> getDependencyNetwork(SemSimModel semSimModel)
	{
		// Get solution domain declarations
		Set<DataStructure> domaincodewords = new HashSet<DataStructure>();
		for(DataStructure ds : semSimModel.getSolutionDomains()){
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".min"));
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".max"));
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".delta"));
		}

		// Loop over all of the data structures (variables) and create nodes for them
		ArrayList<DependencyNode> dependencies = new ArrayList<DependencyNode>();
		for(DataStructure dataStructure : semSimModel.getDataStructures()){
			// If the data structure is part of a solution domain declaration or
			// it is not used to compute any other terms, ignore it.
			if(dataStructure.isSolutionDomain() ||
				domaincodewords.contains(dataStructure) && dataStructure.getUsedToCompute().isEmpty())
			{
				continue;
			}
			
			dependencies.add(new DependencyNode(dataStructure));
		}
		
		// Turn the dependencies into a string
		return dependencies;
	}
	
	/**
	 * Get the SemSim model submodel network, which defines
	 * submodels and their internal dependency networks that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get submodel network from
	 * @return submodel network
	 */
	public static ArrayList<SubModelNode> getSubmodelNetwork(SemSimModel semSimModel)
	{
		ArrayList<SubModelNode> subModelNetwork = new ArrayList<SubModelNode>();
		for(Submodel subModel : semSimModel.getSubmodels()){
			subModelNetwork.add(new SubModelNode(subModel));
		}
		
		return subModelNetwork;
	}
	
	public static JsonString toJsonString(Object obj) {
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		return new JsonString(json);
	}
}
