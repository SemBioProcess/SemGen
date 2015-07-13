package semsim.annotation;

import java.net.URI;

public interface ReferenceTerm {
	
	/**
	 * Retrieve the first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses the SemSim:refersTo relation (SemSimConstants.REFERS_TO_RELATION).
	 */
	public ReferenceOntologyAnnotation getRefersToReferenceOntologyAnnotation();
	
	/**
	 * Retrieve the reference URI.
	 */
	public URI getReferstoURI();
	
	/**
	 * @return True if an object has at least one {@link ReferenceOntologyAnnotation}, otherwise false;
	 */
	public Boolean hasRefersToAnnotation();
	
	public String getName();
	
	public String getNamewithOntologyAbreviation();
	
	public String getDescription();
}
