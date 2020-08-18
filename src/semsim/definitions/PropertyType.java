package semsim.definitions;

/**
 * Enumeration of the main types of physical properties
 * represented in biosimulation models. These correspond to the main
 * classes of physical properties in the Ontology of Physics for Biology.
 * 
 * - Properties of physical entities ("State properties"). Examples: The chemical
 * concentration of a molecular species, the fluid volume of a portion of blood.
 * 
 * - Properties of physical processes ("Rate properties"). Examples: The rate of
 * the phosphofructokinase reaction, the rate of blood flow through an artery.
 * 
 * * - Properties of physical forces ("Force properties"). Examples: Electrical
 * potential across a cell membrane, fluid pressure in an artery.
 * 
 * - Properties of physical dependencies ("Constitutive properties"). Examples: 
 * A first order reaction rate constant used to determine a reaction rate,
 *  the fluid resistance term in an instance of Ohm's Law for fluids. 
 */
public enum PropertyType {
	PROPERTY_OF_PHYSICAL_ENTITY ("State"),
	PROPERTY_OF_PHYSICAL_PROCESS ("Rate"),
	PROPERTY_OF_PHYSICAL_ENERGY_DIFFERENTIAL ("Force"),
	UNKNOWN ("Constitutive");
	
	// String representation of enum
	private final String _name;

    private PropertyType(String name) {
        _name = name;
    }

    /**
     * Returns the name of the property type as a string.
     */
    public String toString(){
       return _name;
    }
}