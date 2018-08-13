package semsim.annotation;

import java.net.URI;

import semsim.SemSimLibrary;

/**
 * Interface for physical model classes that are defined against
 * controlled knowledge resource terms.
 * @author mneal
 *
 */
public interface ReferenceTerm {
	
	/** 
	 * @param semsimlib A SemSimLibrary instance
	 * @return The first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses an identity relation (e.g., SemSimConstants.HAS_PHYSICAL_DEFINITION_RELATION). */
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary semsimlib);
	
	/** @return The reference URI */
	public URI getPhysicalDefinitionURI();
	
	/** @return True if an object has at least one {@link ReferenceOntologyAnnotation}, otherwise false */
	public Boolean hasPhysicalDefinitionAnnotation();
	
	/** @return The ReferenceTerm's name */
	public String getName();
	
	/** 
	 * @param semsimlib A SemSimLibrary instance
	 * @return The ReferenceTerm's name suffixed with an abbreviation for the ontology containing it */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib);
	
	/** @param semsimlib A SemSimLibrary instance
	 * @return The name of the ontology containing the ReferenceTerm */
	public String getOntologyName(SemSimLibrary semsimlib);
	
	/** @return The URI of the ReferenceTerm as a string */
	public String getTermFragment();
	
	/** @return The description of the ReferenceTerm */
	public String getDescription();
}
