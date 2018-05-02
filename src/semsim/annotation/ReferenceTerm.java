package semsim.annotation;

import java.net.URI;

import semsim.SemSimLibrary;

public interface ReferenceTerm {
	
	/**
	 * Retrieve the first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses the SemSim:hasPhysicalDefinition relation (SemSimConstants.HAS_PHYSICAL_DEFINITION_RELATION).
	 */
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary semsimlib);
	
	/** Retrieve the reference URI */
	public URI getPhysicalDefinitionURI();
	
	/** @return True if an object has at least one {@link ReferenceOntologyAnnotation}, otherwise false */
	public Boolean hasPhysicalDefinitionAnnotation();
	
	/** Get the ReferenceTerm's name */
	public String getName();
	
	/** Get the ReferenceTerm's name suffixed with an abbreviation for the ontology containing it */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib);
	
	/** Get the name of the ontology containing the ReferenceTerm */
	public String getOntologyName(SemSimLibrary semsimlib);
	
	/** Get the URI of the ReferenceTerm as a string */
	public String getTermID();
	
	/** Get the description of the ReferenceTerm */
	public String getDescription();
}
