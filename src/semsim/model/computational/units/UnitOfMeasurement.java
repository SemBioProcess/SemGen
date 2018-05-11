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
import semsim.model.collection.SemSimModel;
import semsim.model.computational.ComputationalModelComponent;

/**
 * Class for working with the physical units assigned to data structures.
 * @author mneal
 *
 */
public class UnitOfMeasurement extends ComputationalModelComponent implements Annotatable, Importable{
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private String computationalCode = "";
	private String customDeclaration = "";
	private Set<UnitFactor> unitFactors = new HashSet<UnitFactor>();
	
	private boolean isFundamental = false;
	private boolean isImported = false;
	private String hrefValue = "";
	private String referencedName = "";
	private String localName = "";
	private Importable parentImport;
	private String unitType = "";
	
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
	
	/** 
	 * Add a {@link UnitFactor} unit factor for this UnitOfMeasurement.
	 * The UnitFactors indicate which fundamental units comprise a UnitOfMeasurement,
	 * as well as any scaling factors, exponents, etc. required for conversion.
	 * @param factor The {@link UnitFactor} to add 
	 * the 
	 */
	public void addUnitFactor(UnitFactor factor){
		unitFactors.add(factor);
	}
	
	/** @return The units identifier in the model code. */
	public String getComputationalCode(){
		return computationalCode;
	}
	
	/**
	 * Set the units identifier to use when writing simulation code.
	 * @param code The identifier
	 */
	public void setComputationalCode(String code){
		computationalCode = code;
	}

	/**
	 * Set the custom declaration code (for use in JSim models), if needed
	 * @param customDeclaration The declaration
	 */
	public void setCustomDeclaration(String customDeclaration) {
		this.customDeclaration = customDeclaration;
	}

	/** @return The JSim-specific custom unit declaration (empty if not needed) */
	public String getCustomDeclaration() {
		return customDeclaration;
	}
	
	/** @return Whether a JSim-specific, custom unit declaration is specified */
	public boolean hasCustomDeclaration(){
		return customDeclaration != null && ! customDeclaration.isEmpty();
	}
	
	// Required by annotable interface:
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
		if(getName()==null){
			setDescription(description);
		}
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
	
	/** @return The first {@link ReferenceOntologyAnnotation} applied to the UnitOfMeasurement */
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(){
		if(!getReferenceOntologyAnnotations(SemSimRelation.HAS_PHYSICAL_DEFINITION).isEmpty()){
			return getReferenceOntologyAnnotations(SemSimRelation.HAS_PHYSICAL_DEFINITION).toArray(new ReferenceOntologyAnnotation[]{})[0];
		}
		return null;
	}
	
	/**
	 * Look up a {@link ReferenceOntologyAnnotation} on the UnitOfMeasurement
	 * that refers to an input URI
	 * @param uri The input URI
	 * @return The first {@link ReferenceOntologyAnnotation} that has a URI matching the input URI
	 */
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotationByURI(URI uri){
		for(ReferenceOntologyAnnotation ann : getReferenceOntologyAnnotations(SemSimRelation.HAS_PHYSICAL_DEFINITION)){
			if(ann.getReferenceURI().compareTo(uri)==0){
				return ann;
			}
		}
		return null;
	}
	
	@Override
	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}
	
	@Override
	public Boolean hasPhysicalDefinitionAnnotation(){
		return getPhysicalDefinitionReferenceOntologyAnnotation()!=null;
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
	 * @param isFundamental Whether the unit is fundamental and does not
	 * require unit factors to define it
	 */
	public void setFundamental(boolean isFundamental) {
		this.isFundamental = isFundamental;
	}

	/** @return Whether the unit is fundamental and does not
	 * require unit factors to define it
	 */
	public boolean isFundamental() {
		return isFundamental;
	}

	/**
	 * Set the {@link UnitFactor}s that define this UnitOfMeasurement
	 * @param unitFactors A set of {@link UnitFactor}s
	 */
	public void setUnitFactors(Set<UnitFactor> unitFactors) {
		this.unitFactors = unitFactors;
	}

	/** @return The set of {@link UnitFactor}s that define this UnitOfMeasurement  */
	public Set<UnitFactor> getUnitFactors() {
		return unitFactors;
	}
	
	/** @return Whether any unit factors have been added to define this UnitOfMeasurement */
	public boolean hasUnitFactors(){
		return unitFactors.size()>0;
	}

	// For Importable interface
	@Override
	public void setImported(boolean isImported) {
		this.isImported = isImported;
	}

	@Override
	public boolean isImported() {
		return isImported;
	}

	@Override
	public void setHrefValue(String hrefValue) {
		this.hrefValue = hrefValue;
		if(hrefValue!=null) setImported(true);
	}

	@Override
	public String getHrefValue() {
		return hrefValue;
	}

	@Override
	public void setReferencedName(String name) {
		this.referencedName = name;
		if(name!=null) this.setImported(true);
	}

	@Override
	public String getReferencedName() {
		return referencedName;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public void setLocalName(String name) {
		localName = name;
		if(name!=null) this.setImported(true);
	}

	@Override
	public Importable getParentImport() {
		return parentImport;
	}

	@Override
	public void setParentImport(Importable parent) {
		this.parentImport = parent;
	}

	/**
	 * Set the UnitOfMeasurement's type (used in JSim models)
	 * @param unitType
	 */
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	/** @return The unit type (used in JSim models) */
	public String getUnitType() {
		return unitType;
	}

	/** @return The URI of the class that provides the UnitOfMeasurement's physical definition */
	public URI getPhysicalDefinitionURI() {
		return referenceuri;
	}

	@Override
	public UnitOfMeasurement addToModel(SemSimModel model) {
		
		for (UnitFactor factor : unitFactors) {
			factor.setBaseUnit(factor.getBaseUnit().addToModel(model));
		}
		return model.addUnit(this);
	}
}
