package semgen.stage.serialization;

import java.util.ArrayList;

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
		ArrayList<Node> dependencies = new ArrayList<Node>();
		
		// Loop over all of the data structures (variables) and create nodes for them
		for(DataStructure dataStructure : semSimModel.getDataStructures()){
			dependencies.add(new Node(dataStructure));
		}
		
		// Turn the dependencies into a string
		Gson gson = new Gson();
		String dependencyJson = gson.toJson(dependencies);
		return new JsonString(dependencyJson);
	}
}
