package semsim;


import java.net.URI;
import java.util.Set;

import semsim.model.annotation.Annotation;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.annotation.SemSimRelation;

/**
 * Interface providing methods for annotating
 * SemSim model elements.
 * 
 */
public interface Annotatable {

	
	/** @return All SemSim Annotations applied to this object */
	public Set<Annotation> getAnnotations();
	
	
	/**
	 * Set the SemSim Annotations for an object
	 * @param annset The set of annotations to apply
	 */
	public void setAnnotations(Set<Annotation> annset);

	
	/**
	 * Add a SemSim {@link Annotation} to this object
	 * @param ann The {@link Annotation} to add
	 */
	public void addAnnotation(Annotation ann);
	
	/**
	 * Add a SemSim {@link ReferenceOntologyAnnotation} to an object
	 * 
	 * @param relation The {@link SemSimRelation} that qualifies the
	 * relationship between the object and what it's annotated against
	 * @param uri The URI of the reference ontology term used for
	 * annotation
	 * @param description A free-text description of the reference
	 * ontology term (obtained from the ontology itself whenever possible). 
	 */
	public void addReferenceOntologyAnnotation(SemSimRelation relation, URI uri, String description);

<<<<<<< HEAD
	public void addReferenceOntologyAnnotation(ReferenceOntologyAnnotation roa);	
=======
	
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	/**
	 * Get all SemSim {@link ReferenceOntologyAnnotation}s applied to an object
	 * that have a specific {@link SemSimRelation}.
	 * 
	 * @param relation The {@link SemSimRelation} that filters the annotations 
	 * to return  
	 */
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(SemSimRelation relation);
	
	
	/**
	 * Delete all {@link ReferenceOntologyAnnotation}s applied to this object
	 */
	public void removeAllReferenceAnnotations();
	
	/**
	 * Retrieve the first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses the SemSim:refersTo relation (SemSimConstants.REFERS_TO_RELATION).
	 */
	public ReferenceOntologyAnnotation getFirstRefersToReferenceOntologyAnnotation();
	
	
	/**
	 * Retrieve the first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses the SemSim:refersTo relation (SemSimConstants.REFERS_TO_RELATION) and references
	 * a specific URI
	 * 
	 * @param uri The URI of the ontology term to search for in the set of {@link ReferenceOntologyAnnotation}s
	 * applied to this object.
	 */
	public ReferenceOntologyAnnotation getRefersToReferenceOntologyAnnotationByURI(URI uri);
	
	
	/**
	 * @return True if an object has at least one {@link Annotation}, otherwise false.
	 */
	public Boolean isAnnotated();
	
	
	/**
	 * @return True if an object has at least one {@link ReferenceOntologyAnnotation}, otherwise false;
	 */
	public Boolean hasRefersToAnnotation();
}
