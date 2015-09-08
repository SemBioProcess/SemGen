package semsim.annotation;

import java.net.URI;

/** A type of SemSimRelation for establishing structural relationships between
 * SemSim Physical Entities. */

// A structural relationship between two physical entities
public class StructuralRelation extends SemSimRelation{

	/** Class constructor. Generally you should use the structural relations
	 * provided in SemSimConstants (PART_OF_RELATION, CONTAINED_IN_RELATION)
	 * whenever possible, rather than construct new ones. 
	 * @param name The name of the relation
	 * @param referenceRelationURI The reference URI of the relation */
	public StructuralRelation(String name, URI referenceRelationURI) {
		super(name, referenceRelationURI);
	}
}
