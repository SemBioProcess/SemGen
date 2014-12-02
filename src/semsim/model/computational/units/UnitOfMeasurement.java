package semsim.model.computational.units;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.Importable;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.annotation.SemSimRelation;
import semsim.model.computational.ComputationalModelComponent;


public class UnitOfMeasurement extends ComputationalModelComponent implements Annotatable, Importable, Cloneable{
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private String computationalCode;
	private String customDeclaration;
	private boolean isFundamental = false;
	private double multiplier = 1.0;
	private Set<UnitFactor> unitFactors = new HashSet<UnitFactor>();
	
	private boolean isImported = false;
	private String hrefValue;
	private String referencedName;
	private String localName;
	private Importable parentImport;
	private String unitType;
	
	public UnitOfMeasurement(String name){
		setName(name);
		setComputationalCode(name);
	}
	
	public void addUnitFactor(UnitFactor factor){
		unitFactors.add(factor);
	}
	
	public String getComputationalCode(){
		return computationalCode;
	}
	
	public void setComputationalCode(String code){
		computationalCode = code;
	}

	public void setCustomDeclaration(String customDeclaration) {
		this.customDeclaration = customDeclaration;
	}

	public String getCustomDeclaration() {
		return customDeclaration;
	}
	
	public boolean hasCustomDeclaration(){
		return customDeclaration!=null;
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
		if(getName()==null){
			setDescription(description);
		}
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
	
	
	public ReferenceOntologyAnnotation getFirstRefersToReferenceOntologyAnnotation(){
		if(!getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION).isEmpty()){
			return getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION).toArray(new ReferenceOntologyAnnotation[]{})[0];
		}
		return null;
	}
	
	public ReferenceOntologyAnnotation getRefersToReferenceOntologyAnnotationByURI(URI uri){
		for(ReferenceOntologyAnnotation ann : getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION)){
			if(ann.getReferenceURI().compareTo(uri)==0){
				return ann;
			}
		}
		return null;
	}
	
	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}
	
	public Boolean hasRefersToAnnotation(){
		return getFirstRefersToReferenceOntologyAnnotation()!=null;
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

	public void setFundamental(boolean isFundamental) {
		this.isFundamental = isFundamental;
	}

	public boolean isFundamental() {
		return isFundamental;
	}

	public void setUnitFactors(Set<UnitFactor> unitFactors) {
		this.unitFactors = unitFactors;
	}

	public Set<UnitFactor> getUnitFactors() {
		return unitFactors;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getMultiplier() {
		return multiplier;
	}

	// For Importable interface
	public void setImported(boolean isImported) {
		this.isImported = isImported;
	}

	public boolean isImported() {
		return isImported;
	}

	public void setHrefValue(String hrefValue) {
		this.hrefValue = hrefValue;
		if(hrefValue!=null) setImported(true);
	}

	public String getHrefValue() {
		return hrefValue;
	}

	public void setReferencedName(String name) {
		this.referencedName = name;
		if(name!=null) this.setImported(true);
	}

	public String getReferencedName() {
		return referencedName;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String name) {
		localName = name;
		if(name!=null) this.setImported(true);
	}

	public Importable getParentImport() {
		return parentImport;
	}

	public void setParentImport(Importable parent) {
		this.parentImport = parent;
	}
	
	public UnitOfMeasurement clone() throws CloneNotSupportedException {
        return (UnitOfMeasurement) super.clone();
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public String getUnitType() {
		return unitType;
	}
}
