package semgen.stage.serialization;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

public class SubModelDependencyNode extends DependencyNode {

	// When we split the submodel name and the variable name string into an array
	// we'll need to index into the array to get the correct parts
	public final static int SubmodelNamePart = 0;
	public final static int VariableNamePart = 1;

	public SubModelDependencyNode(DataStructure dataStructure, SubModelNode parentNode) {
		super(getDataStructureVariableName(dataStructure), dataStructure, parentNode.parentModelId);

		// Are there inputs from other models?
		if(dataStructure instanceof MappableVariable) {
			for(MappableVariable input : ((MappableVariable)dataStructure).getMappedFrom())
			{
				String[] nameParts = getNodeNameParts(input);
				String submodelName = nameParts[SubmodelNamePart];
				String variableName = nameParts[VariableNamePart];

				String submodelNameId = Node.buildId(submodelName, parentNode.parentModelId);

				// Mapped variables are treat special in the JS code.
				// We need to know the parent model and submodel name
				// so we can fetch the proper "mapped from" node
				this.inputs.add(new Link(variableName, submodelNameId));
			}
		}
	}

	/**
	 * Get the data structure's name (without the submodel prefix).
	 * If the data structure is a child of a submodel the submodel name will need to be removed.
	 * If the data structure is not a child of a submodel, it's name will be the variable name.
	 * @param dataStructure - data structure to get name from
	 * @return Data structure's name
	 */
	private static String getDataStructureVariableName(DataStructure dataStructure) {
		String[] nameParts = getNodeNameParts(dataStructure);
		if(nameParts.length != 2)
			return dataStructure.getName();
		return nameParts[VariableNamePart];
	}
	
	/**
	 * The dependency name is a concatenation of the submodel and the variable name
	 * We need to extract out each part.
	 * @param dataStrcuture - datastructure to extract parts from
	 * @return Each name part.
	 */
	private static String[] getNodeNameParts(DataStructure dataStrcuture) {
		return dataStrcuture.getName().split("\\.");
	}
	
	/**
	 * Unlike generic dependency nodes, we need to extract the submodel dependency node's variable name
	 * from it's entire name, which includes the submodel name as a prefix.
	 * @param dataStructure - data structure to get name from
	 * @return Data structure's name
	 */
	@Override
	protected String getName(DataStructure dataStructure) {
		return getDataStructureVariableName(dataStructure);
	}
}
