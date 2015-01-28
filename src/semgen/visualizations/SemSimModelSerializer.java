package semgen.visualizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import semgen.SemGen;
import semsim.PropertyType;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

/**
 * Serializes a semsim model
 *
 */
public class SemSimModelSerializer {
	
	/**
	 * Turn the SemSim model into a json string.
	 * This string defines nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to turn into json
	 * @return json
	 */
	public String toJson(SemSimModel semSimModel)
	{
		// The d3 graph that gets turned into json. We need to fill this graph
		// out with nodes and links before we turn it to json.
		Graph d3Graph = new Graph();
		
		// Get solution domain declarations
		Set<DataStructure> domaincodewords = new HashSet<DataStructure>();
		for(DataStructure ds : semSimModel.getSolutionDomains()){
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".min"));
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".max"));
			domaincodewords.add(semSimModel.getDataStructure(ds.getName() + ".delta"));
		}
		
		// Loop over all of the data structures (variables) and insert relationships into
		// the d3 graph
		for(DataStructure dataStructure : semSimModel.getDataStructures()){
			// If the data structure is part of a solution domain declaration or
			// it is not used to compute any other terms, ignore it.
			if(dataStructure.isSolutionDomain() ||
				domaincodewords.contains(dataStructure) && dataStructure.getUsedToCompute().isEmpty())
			{
				continue;
			}
			
			// Are there inputs?
			Set<? extends DataStructure> inputs = null;
			if(dataStructure.getComputation()!=null)
				inputs = dataStructure.getComputation().getInputs();
			else if(dataStructure instanceof MappableVariable)
				inputs = ((MappableVariable)dataStructure).getMappedTo();
			
			// Ensure the target node is added to the map
			Node target = d3Graph.EnsureNodeAdded(dataStructure);
			
			// Add links between the target variable and it's dependencies
			for(DataStructure input : inputs)
			{
				Node source = d3Graph.EnsureNodeAdded(input);
				d3Graph.links.add(new Link(source.getNodeIndex(), target.getNodeIndex()));
			}
		}
		
		// Turn the d3Graph object into a string
		Gson gson = new Gson();
		String d3MapJson = gson.toJson(d3Graph);
		return d3MapJson;
	}
	
	/**
	 * Represents a node in a d3 graph
	 * 
	 * @author Ryan
	 *
	 */
	private class Node {
		@SuppressWarnings("unused")
		public int group;
		
		public String name;
		
		private transient int _nodeIndex;
		
		public Node(String name, int group, int nodeIndex)
		{
			this.name = name;
			this.group = group;
			_nodeIndex = nodeIndex;
		}
		
		public int getNodeIndex() { return _nodeIndex; }
	}
	
	/**
	 * Represents a link between nodes in a d3 graph
	 * 
	 * @author Ryan
	 *
	 */
	private class Link {
		@SuppressWarnings("unused")
		public int source;
		
		@SuppressWarnings("unused")
		public int target;
		
		@SuppressWarnings("unused")
		public int value;
		
		public Link(int source, int target)
		{
			this.source = source;
			this.target = target;
			this.value = 1;
		}
	}
	
	/**
	 * Contains the links and nodes in a d3 graph
	 * 
	 * @author Ryan
	 *
	 */
	public class Graph {
		public ArrayList<Node> nodes;
		public ArrayList<Link> links;
		public Map<Integer, String> groups;
		
		private transient Map<String, Node> _nodeCache;
		
		public Graph()
		{
			nodes = new ArrayList<Node>();
			links = new ArrayList<Link>();
			
			_nodeCache = new Hashtable<String, Node>();
			groups = new HashMap<Integer, String>();
		}
		
		/**
		 * Retrieves a node if it already exists. If it doesn't already exist
		 * in the d3 graph this function creates a new node and inserts it into
		 * the array of existing nodes, remembering it's index in the array
		 * that's later used for creating links between nodes.
		 * 
		 * @param dataStructure - variable
		 * @return Node representation of variable
		 */
		private Node EnsureNodeAdded(DataStructure dataStructure)
		{
			String name = dataStructure.getName();
			
			// Return the node if it already exists
			if(_nodeCache.containsKey(name))
				return _nodeCache.get(name);
				
			PropertyType type = dataStructure.getPropertyType(SemGen.semsimlib);
			if(!groups.containsKey(type.getIndex()))
				groups.put(type.getIndex(), type.toString());
			
			// Create a the new node representing this data structure (variable)
			Node node = new Node(dataStructure.getName(),
					type.getIndex(),
					this.nodes.size());
			
			// Add the new node to our list of nodes
			this.nodes.add(node);
			
			// Save the node so we can quickly look it up by name
			_nodeCache.put(node.name, node);
			
			return node;
		}
	}
}
