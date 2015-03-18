package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.Set;

import semgen.SemGen;
import semsim.PropertyType;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

/**
 * Represents a node in a d3 graph
 * 
 * @author Ryan
 *
 */
public class Node {
	public String name;
	public String nodeType;
	public ArrayList<String> inputs;
	
	public Node(DataStructure dataStructure)
	{
		this.name = dataStructure.getName();
		this.nodeType = dataStructure.getPropertyType(SemGen.semsimlib).toString();
		
		// Are there inputs?
		Set<? extends DataStructure> dataStructureInputs = null;
		if(dataStructure.getComputation()!=null)
			dataStructureInputs = dataStructure.getComputation().getInputs();
		else if(dataStructure instanceof MappableVariable)
			dataStructureInputs = ((MappableVariable)dataStructure).getMappedTo();

		// Add links
		inputs = new ArrayList<String>();
		for(DataStructure input : dataStructureInputs)
		{
			inputs.add(input.getName());
		}
	}
}
