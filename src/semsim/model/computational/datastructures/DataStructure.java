package semsim.model.computational.datastructures;


import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimLibrary;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.PropertyType;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;
import semsim.model.computational.ComputationalModelComponent;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.utilities.SemSimCopy;

/**
 * This class represents a named element in a simulation model that
 * is assigned some computational value during simulation.
 */
public abstract class DataStructure extends ComputationalModelComponent implements Annotatable, Cloneable {	
	private Computation computation = null;
	private PhysicalPropertyinComposite physicalProperty = null;
	private PhysicalModelComponent physicalcomponent = null;
	private PhysicalProperty singularterm = null;
	private DataStructure solutionDomain;
	private Set<DataStructure> usedToCompute = new HashSet<DataStructure>();
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private boolean isSolutionDomain, isDiscrete, isDeclared, isImported;
	protected boolean mappable = false;
	private String startValue = "";
	private UnitOfMeasurement unit;
	private boolean external = false;
	
	public DataStructure(SemSimTypes type) {
		super(type);
	}
	
	public DataStructure(DataStructure dstocopy) {
		super(dstocopy);
		if (dstocopy.computation != null) {
			computation = new Computation(dstocopy.computation);
		}
		physicalProperty = dstocopy.physicalProperty;
		physicalcomponent = dstocopy.physicalcomponent;
		singularterm = dstocopy.singularterm;
		solutionDomain = dstocopy.solutionDomain;
		usedToCompute.addAll(dstocopy.usedToCompute);
		annotations = SemSimCopy.copyAnnotations(dstocopy.annotations);
		isSolutionDomain = dstocopy.isSolutionDomain;
		isDiscrete = dstocopy.isDiscrete;
		isDeclared = dstocopy.isDeclared;
		isImported = dstocopy.isImported;
		if (dstocopy.startValue!=null) {
			startValue = new String(dstocopy.startValue);
		}
		unit = dstocopy.unit;
		external = dstocopy.external;
	}
	
	
	/**
	 * Append the list of DataStructures that this DataStructure
	 * is used to compute
	 * @param ds A DataStructure that is computed from this DataStructure
	 */
	public void addUsedToCompute(DataStructure ds){
		
		if( ! getUsedToCompute().contains(ds) && ds.isDeclared){
			usedToCompute.add(ds);
		}
	}
	
	/**
	 * Append the list of DataStructures that this DataStructure
	 * is used to compute
	 * @param ds A list of DataStructures that are computed from this DataStructure
	 */
	public void addUsedToCompute(Set<DataStructure> dss){
		
		for(DataStructure ds : dss){
			
			if( ! getUsedToCompute().contains(ds)){ // && ds.isDeclared){
				usedToCompute.add(ds);
			}
		}
	}
	
	/**
	 * @return The {@link Computation} that solves the DataStructure
	 */
	public Computation getComputation(){
		return computation;
	}
	
	public Set<DataStructure> getComputationInputs() {
		return computation.getInputs();
	}
	
	public Set<DataStructure> getComputationOutputs() {
		return computation.getOutputs();
	}
	
	/**
	 * @return The {@link PhysicalPropertyinComposite} simulated by the DataStructure
	 */
	public PhysicalPropertyinComposite getPhysicalProperty(){
		return physicalProperty;
	}
	
	public PhysicalModelComponent getAssociatedPhysicalModelComponent() {
		return physicalcomponent;
	}
	
	public void setAssociatedPhysicalModelComponent(PhysicalModelComponent pmc) {
		physicalcomponent = pmc;
	}
	
	/** @return The domain in which the data structure is solved
	 *  (time, length, height, breadth, e.g.)
	 */
	public DataStructure getSolutionDomain(){
		return solutionDomain;
	}
	
	/** @return The DataStructure's value at the beginning of the simulation */
	public String getStartValue(){
		return startValue;
	}
	
	/** @return The DataStructure's unit of measurement*/
	public UnitOfMeasurement getUnit() {
		return unit;
	}
	
	/** @return Whether the DataStructure has been assigned a unit of measurement */
	public boolean hasUnits(){
		return getUnit()!=null;
	}
	
	/** @return Whether the DataStructure has been associated with a physical property */
	public Boolean hasPhysicalProperty(){
		return physicalProperty != null;
	}
	
	/** @return Whether the DataStructure has been assigned a start value */
	public Boolean hasStartValue(){
		return (startValue != null);
	}
	
	/** @return Whether the DataStructure has been assigned a solution domaint */
	public Boolean hasSolutionDomain(){
		return (solutionDomain != null);
	}
	
	/** @return Whether the DataStructure is explicitly declared in the simulation code */
	public boolean isDeclared() {
		return isDeclared;
	}
	
	/** @return Whether the DataStructure is solved as a non-continuous variable */
	public Boolean isDiscrete(){
		return this.getComputation().getEvents().size()>0;
	}
	
	/** @return Whether the DataStructure is a solution domain for the model */
	public Boolean isSolutionDomain(){
		return isSolutionDomain;
	}
	
	/**
	 * Set the start value of the DataStructure for the simulation
	 * @param val A string representing the start value
	 */
	public void setStartValue(String val){
		
		if(val == null) startValue = null;
		else if(val.equals("")) return;
		else startValue = val;
	}
	
	/**
	 * Assign a Computation instance to the DataStructure to specify
	 * how it is solved during simulation
	 * @param computation The Computation instance to assign
	 */
	public void setComputation(Computation computation){
		this.computation = computation;
	}
	
	/**
	 * Assign a {@link PhysicalPropertyinComposite} to the DataStructure 
	 * @param pp The PhysicalProperty instance to assign to the DataStructure
	 */
	public void setAssociatedPhysicalProperty(PhysicalPropertyinComposite pp){
		physicalProperty = pp;
	}
	
	/** Set whether this DataStructure is explicitly declared in the model or not */

	public void setDeclared(boolean isDeclared) {
		this.isDeclared = isDeclared;
	}
	
	/** Set whether this DataStructure is a solution domain in the model or not */
	public void setIsSolutionDomain(boolean issoldom){
		isSolutionDomain = issoldom;
	}
	
	/** Set the solution domain in which this DataStructure is solved */
	public void setSolutionDomain(DataStructure soldom){
		solutionDomain = soldom;
	}

	/** Set the unit of measurement assigned to the DataStructure */
	public void setUnit(UnitOfMeasurement unit) {
		this.unit = unit;
	}

	/** Get a string representation of the composite annotation applied to 
	 * the DataStructure.
	 * @param appendcodeword Whether to add the DataStructure's name in parentheses at the end of the string 
	 */
	public String getCompositeAnnotationAsString(Boolean appendcodeword) {
		String compann = "[unspecified]";
		if(getPhysicalProperty()!=null){
			compann = "[unspecified property] of ";
			if(getPhysicalProperty().hasPhysicalDefinitionAnnotation()){
				compann = getPhysicalProperty().getName() + " of ";
			}
			// if physical entity or process
			String target = "?";
			if(getAssociatedPhysicalModelComponent()!=null){
				if(getAssociatedPhysicalModelComponent().hasPhysicalDefinitionAnnotation()){
					target = getAssociatedPhysicalModelComponent().getDescription();
				}
				// otherwise it's a composite physical entity or custom term
				else{
					target = getAssociatedPhysicalModelComponent().getName();
				}
			}
			compann = compann + target; 

		}
		if (appendcodeword) {
			compann = compann + " (" + getName() + ") ";
		}
		return compann;
	}

	public  Set<DataStructure> getDownstreamDataStructures(Set<DataStructure> candidates, DataStructure mainroot){
		// traverse all nodes that belong to the parent
		Set<DataStructure> newamounts = new HashSet<DataStructure>();
		for(DataStructure downstreamds : getUsedToCompute()){
			if(candidates.contains(downstreamds) && !newamounts.contains(downstreamds) && downstreamds!=mainroot && downstreamds!=this){
				newamounts.add(downstreamds);
				newamounts.addAll(downstreamds.getDownstreamDataStructures(newamounts, mainroot));
			}
		}
		return newamounts;
	}

	
	/** Specify which DataStructures in the model are computationally dependent on this DataStructure */
	public void setUsedToCompute(Set<DataStructure> usedToCompute) {
		this.usedToCompute = usedToCompute;
	}

	/** @return The set of DataStructures that are computationally dependent on this DataStructure */
	public Set<DataStructure> getUsedToCompute() {
		return usedToCompute;
	}
	
	public boolean hasComputation() {
		return computation!=null;
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
		addAnnotation(new ReferenceOntologyAnnotation(relation, uri, description, lib));
	}

	
	public Set<ReferenceOntologyAnnotation> getAllReferenceOntologyAnnotations(){
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(Annotation ann : getAnnotations()){
			if(ann instanceof ReferenceOntologyAnnotation){
				raos.add((ReferenceOntologyAnnotation) ann);
			}
		}
		return raos;
	}
	
	
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(Relation relation) {
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(ReferenceOntologyAnnotation ann : getAllReferenceOntologyAnnotations()){
			if(ann.getRelation()==relation){
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
	
	/** Clone this DataStructure */
	public DataStructure clone() throws CloneNotSupportedException {
        return (DataStructure) super.clone();
	}

	/** Specify whether this DataStructure is included in the model via an imported {@link Submodel} */
	public void setImportedViaSubmodel(boolean isImported) {
		this.isImported = isImported;
	}

	/** @return Whether this DataStructure instance is included in the model via an imported {@link Submodel}.*/
	public boolean isImportedViaSubmodel() {
		return isImported;
	}
	
	public abstract boolean isReal();
	
	
	/** @return Whether this DataStructure instance is a mapped variable that is a component input as in CellML models.*/
	public boolean isFunctionalSubmodelInput(){
		return false;
	}
	
	public PropertyType getPropertyType(SemSimLibrary lib){
		if(hasPhysicalProperty()){
			// If there's already an OPB reference annotation
			if(getPhysicalProperty().hasPhysicalDefinitionAnnotation()){
				return lib.getPropertyinCompositeType(physicalProperty);
			}
			// Otherwise, see if there is already an entity or process associated with the codeword
			else if(getAssociatedPhysicalModelComponent() instanceof PhysicalEntity){
				return PropertyType.PropertyOfPhysicalEntity;
			}
			else if(getAssociatedPhysicalModelComponent() instanceof PhysicalProcess){
				return PropertyType.PropertyOfPhysicalProcess;
			}
			else return PropertyType.Unknown;
		}
		return PropertyType.Unknown;
	}
	
	public void copySingularAnnotations(DataStructure srcds, SemSimLibrary lib){
		removeAllReferenceAnnotations();
		setSingularAnnotation(srcds.getSingularTerm());
		for(ReferenceOntologyAnnotation ann : srcds.getAllReferenceOntologyAnnotations()){
			addReferenceOntologyAnnotation(ann.getRelation(), ann.getReferenceURI(), ann.getValueDescription(), lib);
		}
	}
	
	public boolean hasAssociatedPhysicalComponent() {
		return getAssociatedPhysicalModelComponent()!=null;
	}
	
	public void setSingularAnnotation(PhysicalProperty refterm) {
		singularterm = refterm;
	}
	
	public void removeSingularAnnotation() {
		singularterm = null;
	}
	
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary lib){
		if(hasPhysicalDefinitionAnnotation()){
			return singularterm.getPhysicalDefinitionReferenceOntologyAnnotation(lib);
		}
		return null;
	}
	
	public void replaceDataStructureReference(DataStructure replacer, DataStructure replacee) {
		if (computation.getOutputs().contains(replacee)) {
			computation.getOutputs().remove(replacee);
			computation.addOutput(replacer);
		}
		if (computation.getInputs().contains(replacee)) {
			computation.getInputs().remove(replacee);
			computation.addInput(replacer);
		}
		if (this.usedToCompute.contains(replacee)) {
			usedToCompute.remove(replacee);
			usedToCompute.add(replacer);
		}
		if (this.solutionDomain == replacee) {
			solutionDomain = replacer;
		}
	}

	public void replaceUsedtoCompute(HashMap<DataStructure, DataStructure> dsmap) {
		Set<DataStructure> newused = new HashSet<DataStructure>();
		for (DataStructure used : this.getUsedToCompute()) {
			DataStructure replacer = dsmap.get(used);
			if (replacer != null) {
				newused.add(replacer);
			}
		}
		solutionDomain = dsmap.get(solutionDomain);
		setUsedToCompute(newused);
	}
	
	
	public void replaceAllDataStructures(HashMap<DataStructure, DataStructure> dsmap) {
		computation.replaceAllDataStructures(dsmap);
		replaceUsedtoCompute(dsmap);
		if (this.solutionDomain!=null) {
			solutionDomain = dsmap.get(solutionDomain);
		}
	}
	
	public void replaceOutputs(HashMap<DataStructure, DataStructure> dsmap) {
		computation.replaceOutputs(dsmap);
	}
	
	public void replaceInputs(HashMap<DataStructure, DataStructure> dsmap) {
		computation.replaceInputs(dsmap);
	}
	
	@Override
	public Boolean hasPhysicalDefinitionAnnotation() {
		return singularterm!=null;
	}
	
	public URI getPhysicalDefinitionURI() {
		return singularterm.getPhysicalDefinitionURI();
	}
	
	public PhysicalProperty getSingularTerm() {
		return singularterm;
	}
	
	public boolean isMapped() {
		return false;
	}
	
	public abstract DataStructure copy();
	
	public void addToModel(SemSimModel model) {
		model.addDataStructure(this);
		if (this.hasPhysicalDefinitionAnnotation()) {
			model.addPhysicalProperty(singularterm);
		}
		else if (hasPhysicalProperty()) {
			model.addAssociatePhysicalProperty(physicalProperty);
		}
		unit.addToModel(model);
		if (hasAssociatedPhysicalComponent()) {
			physicalcomponent.addToModel(model);
		}
	}
	
	public DataStructure removeFromModel(SemSimModel model) {
		model.removeDataStructure(this);
		return this;
	}
	
	public void clearInputs() {
		this.computation = new Computation(computation);
		this.computation.setInputs(new HashSet<DataStructure>());
		this.computation.setComputationalCode(new String());
		this.computation.setMathML(new String());
		this.setStartValue(new String());
	}
	
	public void removeOutput(DataStructure dstoremove) {
		this.computation.removeOutput(dstoremove);
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}
}
