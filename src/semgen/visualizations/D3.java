package semgen.visualizations;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

import com.google.gson.Gson;

import semgen.SemGen;
import semsim.PropertyType;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

/**
 * Displays D3 visualizations of a SemSim model
 * 
 * To add a new visualization:
 * 1) Create a new html template in the resources folder
 * 2) Add an entry in for the template in _visualizationTemplates
 * 
 * @author Ryan
 *
 */
public class D3 {

	// D3 main html file
	private final static String D3MainHtml = "/resources/d3/main.html";
	
	// Json representation of the semsim model
	private String _graphJson;
	
	public D3(SemSimModel semSimModel)
	{
		// Get the json graph representation of the SemSim model.
		// Later we'll insert it into a d3 template that visualizes it
		_graphJson = toJson(semSimModel);
	}
	
	/**
	 * Given a visualization type, this function does the following:
	 * 
	 * 1) Loads a d3 template
	 * 2) Creates a new, temporary html file
	 * 3) Inserts the graph json (created in the constructor) into the temporary html file
	 * 4) Opens the temporary html file
	 * 
	 * @param visualizationType - type of d3 visualization
	 * @throws URISyntaxException 
	 * @throws IOException
	 */
	public JPanel visualize()
	{
		System.out.println("Loading d3 graph");
		
		NativeInterface.open();
		
		// Embed the browser in a java panel
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		CommunicatingWebBrowser<D3WebBrowserCommandSender> webBrowser = new CommunicatingWebBrowser<D3WebBrowserCommandSender>(D3WebBrowserCommandSender.class);
	    webBrowser.setMenuBarVisible(false);
        webBrowser.setBarsVisible(false);
        webBrowser.setFocusable(false);
	    webBrowser.navigate(WebServer.getDefaultWebServer().getClassPathResourceURL(getClass().getName(), D3MainHtml));
	    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
	    
	    // Used to send commands to javascript
	    final D3WebBrowserCommandSender commandSender = webBrowser.getCommandSender();
	    
	    // When the page is loaded send the graph json to the browser
	    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {

	    	@Override
			public void loadingProgressChanged(WebBrowserEvent e) {
	    		if(e.getWebBrowser().getLoadingProgress() == 100)
	    		{
	    			// Send the json to javascript
	    			commandSender.loadGraph(D3.this._graphJson);
	    			e.getWebBrowser().removeWebBrowserListener(this);
	    		}
	    	}
		});

	    System.out.println("D3 graph loaded.");
	    
	    return webBrowserPanel;
	}
	
	/**
	 * Turn the SemSim model into a json string.
	 * This string defines nodes and links that the d3.js engine will use
	 * to create visualizations
	 * 
	 * @param semSimModel - SemSim model to turn into json
	 * @return json
	 */
	private String toJson(SemSimModel semSimModel)
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
	private class Graph {
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
