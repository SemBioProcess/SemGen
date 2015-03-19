package semgen.stage.serialization;

import java.util.ArrayList;
import semgen.SemGen;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

/**
 * Represents a dependency node in the d3 graph
 * 
 * @author Ryan
 *
 */
public class DependencyNode extends Node {
	public String nodeType;
	public ArrayList<Object> inputs;
	
	public DependencyNode(DataStructure dataStructure)
	{
		super(dataStructure.getName());
		
		this.nodeType = dataStructure.getPropertyType(SemGen.semsimlib).toString();

		inputs = new ArrayList<Object>();
		
		// Are there intra model inputs?
		if(dataStructure.getComputation() != null) {
			for(DataStructure input : dataStructure.getComputation().getInputs())
			{
				inputs.add(input.getName());
			}
		}
		
		// Are there inputs from other models?
		if(dataStructure instanceof MappableVariable) {
			for(MappableVariable input : ((MappableVariable)dataStructure).getMappedTo())
			{
				// Finish mapping variable when you know how to
			}
		}
	}
}
