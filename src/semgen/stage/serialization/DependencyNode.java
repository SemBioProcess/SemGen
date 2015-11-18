package semgen.stage.serialization;

import semgen.SemGen;
import semsim.model.computational.datastructures.DataStructure;

/**
 * Represents a dependency node in the d3 graph
 * 
 * @author Ryan
 *
 */
public class DependencyNode extends Node {	
	
	public String nodeType;
	
	public DependencyNode(DataStructure dataStructure, String parentModelId)
	{
		this(dataStructure.getName(), dataStructure, parentModelId);
	}
	
	/**
	 * Allow descendant classes to pass in a node name
	 * @param name of node
	 * @param dataStructure node data
	 */
	protected DependencyNode(String name, DataStructure dataStructure, String parentModelId)
	{
		super(name, parentModelId);
		
		this.nodeType = dataStructure.getPropertyType(SemGen.semsimlib).toString();

		// Are there intra-model inputs?
		if(dataStructure.getComputation() != null) {
			for(DataStructure input : dataStructure.getComputation().getInputs())
			{
				String inputName = getName(input);
				
				// Don't add self pointing links
				if(!this.name.equals(inputName))
					this.inputs.add(new Link(inputName, this.parentModelId));
			}
		}
	}

	/**
	 * Get the data structure's name
	 * @param dataStructure
	 * @return the data structure's name
	 */
	protected String getName(DataStructure dataStructure) {
		return dataStructure.getName();
	}
}
