package semsim.model.computational.units;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimLibrary;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimTypes;
import semsim.model.Importable;
import semsim.model.computational.ComputationalModelComponent;


public class UnitOfMeasurement extends ComputationalModelComponent implements Annotatable, Importable{
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private String computationalCode;
	private String customDeclaration;
	private Set<UnitFactor> unitFactors = new HashSet<UnitFactor>();
	
	private boolean isFundamental = false;
	private boolean isImported = false;
	private String hrefValue;
	private String referencedName;
	private String localName;
	private Importable parentImport;
	private String unitType;
	
	public UnitOfMeasurement(String name){
		super(SemSimTypes.UNIT_OF_MEASUREMENT);
		setName(name);
		setComputationalCode(name);
	}
	
	/**
	 * Copy constructor
	 * @param uomtocopy
	 */
	public UnitOfMeasurement(UnitOfMeasurement uomtocopy) {
		super(uomtocopy);
		computationalCode = new String(uomtocopy.computationalCode);
		if (uomtocopy.customDeclaration != null) {
			customDeclaration = new String(uomtocopy.customDeclaration);
		}
		unitFactors.addAll(uomtocopy.unitFactors);
		isFundamental = uomtocopy.isFundamental;
		isFundamental = uomtocopy.isImported;
		parentImport = uomtocopy.parentImport;
		if (uomtocopy.referencedName != null) {
			referencedName = new String(uomtocopy.referencedName);
		}
		if (uomtocopy.localName != null) {
			localName = new String(uomtocopy.localName);
		}
		unitType = uomtocopy.unitType;
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
	
	public void addReferenceOntologyAnnotation(Relation relation, URI uri, String description, SemSimLibrary lib){
		if(getName()==null){
			setDescription(description);
		}
		addAnnotation(new ReferenceOntologyAnnotation(relation, uri, description, lib));
	}

	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(Relation relation) {
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(Annotation ann : getAnnotations()){
			if(ann instanceof ReferenceOntologyAnnotation && ann.getRelation()==relation){
				raos.add((ReferenceOntologyAnnotation)ann);
			}
		}
		return raos;
	}
	
	
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(){
		if(!getReferenceOntologyAnnotations(SemSimRelation.HAS_PHYSICAL_DEFINITION).isEmpty()){
			return getReferenceOntologyAnnotations(SemSimRelation.HAS_PHYSICAL_DEFINITION).toArray(new ReferenceOntologyAnnotation[]{})[0];
		}
		return null;
	}
	
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotationByURI(URI uri){
		for(ReferenceOntologyAnnotation ann : getReferenceOntologyAnnotations(SemSimRelation.HAS_PHYSICAL_DEFINITION)){
			if(ann.getReferenceURI().compareTo(uri)==0){
				return ann;
			}
		}
		return null;
	}
	
	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}
	
	public Boolean hasPhysicalDefinitionAnnotation(){
		return getPhysicalDefinitionReferenceOntologyAnnotation()!=null;
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

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public String getUnitType() {
		return unitType;
	}

	public URI getPhysicalDefinitionURI() {
		return referenceuri;
	}
}
