package semsim.annotation;


import java.net.URI;
import java.util.Set;

import semsim.SemSimLibrary;
import semsim.definitions.SemSimRelations.SemSimRelation;

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
	 * @param lib A SemSimLibrary instance
	 */
	public void addReferenceOntologyAnnotation(Relation relation, URI uri, String description, SemSimLibrary lib);

	
	/**
	 * Get all SemSim {@link ReferenceOntologyAnnotation}s applied to an object
	 * that use a specific {@link SemSimRelation}.
	 * @param relation The {@link SemSimRelation} that filters the annotations 
	 * to return  
	 * @return All SemSim {@link ReferenceOntologyAnnotation}s applied to an object
	 * that use the specified {@link SemSimRelation}.
	 */
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(Relation relation);
	
	
	/**
	 * Delete all {@link ReferenceOntologyAnnotation}s applied to this object
	 */
	public void removeAllReferenceAnnotations();
	
	/**
	 * @return True if an object has at least one {@link Annotation}, otherwise false.
	 */
	public Boolean isAnnotated();
	
	
	/**
	 * @return True if an object has at least one {@link ReferenceOntologyAnnotation}, otherwise false;
	 */
	public Boolean hasPhysicalDefinitionAnnotation();
}
