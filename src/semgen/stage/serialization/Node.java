package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.Set;

import semgen.SemGen;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

/**
 * Represents a node in a d3 graph
 * 
 * @author Ryan
 *
 */
public class Node {
	public String id;
	public int group;
	public ArrayList<String> links;
	
	public Node(DataStructure dataStructure)
	{
		this.id = dataStructure.getName();
		this.group = dataStructure.getPropertyType(SemGen.semsimlib).getIndex();
		
		// Are there inputs?
		Set<? extends DataStructure> inputs = null;
		if(dataStructure.getComputation()!=null)
			inputs = dataStructure.getComputation().getInputs();
		else if(dataStructure instanceof MappableVariable)
			inputs = ((MappableVariable)dataStructure).getMappedTo();

		// Add links
		links = new ArrayList<String>();
		for(DataStructure input : inputs)
		{
			links.add(input.getName());
		}
	}
}
