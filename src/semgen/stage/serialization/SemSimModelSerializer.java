package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import semgen.visualizations.JsonString;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;

public class SemSimModelSerializer {

	/**
	 * Turn the SemSim model dependency network into a json string.
	 * This string defines nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to get dependency network from
	 * @return json
	 */
	public static JsonString dependencyNetworkToJson(SemSimModel semSimModel)
	{
		// Get solution domain declarations
		Set<DataStructure> domaincodewords = new HashSet<DataStructure>();
		for(DataStructure ds : semSimModel.getSolutionDomains()){
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".min"));
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".max"));
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".delta"));
		}

		// Loop over all of the data structures (variables) and create nodes for them
		ArrayList<Node> dependencies = new ArrayList<Node>();
		for(DataStructure dataStructure : semSimModel.getDataStructures()){
			// If the data structure is part of a solution domain declaration or
			// it is not used to compute any other terms, ignore it.
			if(dataStructure.isSolutionDomain() ||
				domaincodewords.contains(dataStructure) && dataStructure.getUsedToCompute().isEmpty())
			{
				continue;
			}
			
			dependencies.add(new Node(dataStructure));
		}
		
		// Turn the dependencies into a string
		Gson gson = new Gson();
		String dependencyJson = gson.toJson(dependencies);
		return new JsonString(dependencyJson);
	}
}
