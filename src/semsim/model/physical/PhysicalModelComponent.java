package semsim.model.physical;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimLibrary;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.SemSimTypes;
import semsim.model.SemSimComponent;
import semsim.model.collection.SemSimModel;
import semsim.utilities.SemSimCopy;

/**
 * Class for working with the physical, as opposed to computational,
 * elements in SemSim models. These include physical entities, processes,
 * dependencies, and their associated properties.
 * @author mneal
 *
 */
public abstract class PhysicalModelComponent extends SemSimComponent implements Annotatable {
	private Set<Annotation> annotations = new HashSet<Annotation>();
	
	public PhysicalModelComponent(SemSimTypes type) {
		super(type);
	}
	
	/**
	 * Copy constructor
	 * @param pmctocopy The PhysicalModelComponent to copy
	 */
	public PhysicalModelComponent(PhysicalModelComponent pmctocopy) {
		super(pmctocopy);
		annotations = SemSimCopy.copyAnnotations(pmctocopy.getAnnotations());
	}
	
	@Override
	public Set<Annotation> getAnnotations() {
		return annotations;
	}
	
	@Override
	public void setAnnotations(Set<Annotation> annset){
		annotations.clear();
		annotations.addAll(annset);
	}

	@Override
	public void addAnnotation(Annotation ann) {
		annotations.add(ann);
	}
	
	@Override
	public void addReferenceOntologyAnnotation(Relation relation, URI uri, String description, SemSimLibrary lib){
		addAnnotation(new ReferenceOntologyAnnotation(relation, uri, description, lib));
	}

	@Override
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(Relation relation) {
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(Annotation ann : getAnnotations()){
			if(ann instanceof ReferenceOntologyAnnotation && ann.getRelation()==relation){
				raos.add((ReferenceOntologyAnnotation)ann);
			}
		}
		return raos;
	}

	@Override
	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}
	
	@Override
	public void removeAllReferenceAnnotations() {
		Set<Annotation> newset = new HashSet<Annotation>();
		for(Annotation ann : this.getAnnotations()){
			if(!(ann instanceof ReferenceOntologyAnnotation)){
				newset.add(ann);
			}
		}
		annotations.clear();
		annotations.addAll(newset);
	}
	
	/**
	 * Remove the {@link ReferenceOntologyAnnotation}s that use a 
	 * particular {@link Relation}.
	 * @param relation The {@link Relation} used to identify annotations
	 * that should be removed
	 */
	public void removeReferenceAnnotationsWithRelation(Relation relation) {
		Set<ReferenceOntologyAnnotation> refs = getReferenceOntologyAnnotations(relation);
		for (ReferenceOntologyAnnotation ref : refs) {
			annotations.remove(ref);
		}
	}

	/** @return Get a String representation of this class's SemSim type*/
	public abstract String getComponentTypeAsString();
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (obj==this) return true;
		if (getClass()==obj.getClass()) return isEquivalent(obj);
		return false;
	}
	
	/**
	 * Test whether this object is equivalent to another
	 * @param obj An input Object
	 * @return Whethe the input Object is equivalent to this object
	 */
	protected abstract boolean isEquivalent(Object obj);
	
	@Override
	public Boolean isPhysicalComponent() {
		return true;
	}
	
	@Override
	public abstract PhysicalModelComponent addToModel(SemSimModel model);
	
	/**
	 * Remove this object from a {@link SemSimModel}
	 * @param model The model from which to remove the PhysicalModelComponent 
	 */
	public abstract void removeFromModel(SemSimModel model);
}
