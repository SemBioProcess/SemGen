package semsim.model.physical;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.definitions.SemSimRelation;
import semsim.model.SemSimComponent;
import semsim.model.SemSimTypes;
import semsim.utilities.SemSimCopy;

public abstract class PhysicalModelComponent extends SemSimComponent implements Annotatable {
	private Set<Annotation> annotations = new HashSet<Annotation>();
	
	public PhysicalModelComponent() {}
	
	public PhysicalModelComponent(PhysicalModelComponent pmctocopy) {
		super(pmctocopy);
		annotations = SemSimCopy.copyAnnotations(pmctocopy.getAnnotations());
	}
	
	// Required by annotable interface:
	public Set<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Set<Annotation> annset){
		annotations.clear();
		annotations.addAll(annset);
	}

	public void addAnnotation(Annotation ann) {
		annotations.add(ann);
	}
	
	public void addReferenceOntologyAnnotation(SemSimRelation relation, URI uri, String description){
		addAnnotation(new ReferenceOntologyAnnotation(relation, uri, description));
	}

	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(SemSimRelation relation) {
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(Annotation ann : getAnnotations()){
			if(ann instanceof ReferenceOntologyAnnotation && ann.getRelation()==relation){
				raos.add((ReferenceOntologyAnnotation)ann);
			}
		}
		return raos;
	}

	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}
	
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
	
	public void removeReferenceAnnotationsofType(SemSimRelation relation) {
		Set<ReferenceOntologyAnnotation> refs = getReferenceOntologyAnnotations(relation);
		for (ReferenceOntologyAnnotation ref : refs) {
			annotations.remove(ref);
		}
	}
	
	public PhysicalModelComponent clone() throws CloneNotSupportedException {
        return (PhysicalModelComponent) super.clone();
	}

	public abstract String getComponentTypeasString();
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (obj==this) return true;
		if (getClass()==obj.getClass()) return isEquivalent(obj);
		return false;
	}
	
	protected abstract boolean isEquivalent(Object obj);
	public abstract SemSimTypes getSemSimType();
}
