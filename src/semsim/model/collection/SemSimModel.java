package semsim.model.collection;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import semsim.SemSimConstants;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.Annotatable;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimComponent;
import semsim.model.computational.ComputationalModelComponent;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.MMLchoice;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalDependency;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.writing.SemSimOWLwriter;

/**
 * A SemSim model is a declarative representation of a quantified physical system.
 * 
 * SemSim models represent both the computational and physical aspects of a model.
 * In other words, they capture not only the model's mathematics, but also what the mathematics 
 * represent, physically.
 * <p>
 * At the most fundamental level, SemSim models are comprised of {@link DataStructure}s,
 * which represent the numerical values of physical properties (concentrations, pressures, etc.).
 * These physical properties are properties of either physical entities or physical processes.
 * <p>
 * For example, a model of cellular glycolysis may represent the chemical concentration of
 * intracellular glucose with a variable called "cgluc." This can be captured within a SemSim model by
 * creating a {@link DataStructure} with the name "cgluc" and a corresponding {@link PhysicalPropertyinComposite}
 * that is annotated against the Ontology of Physics for Biology term "Chemical concentration."
 * This {@link PhysicalPropertyinComposite} would be a property of a {@link PhysicalEntity} representing 
 * intracellular glucose. In this case we would use a {@link CompositePhysicalEntity} that consists of
 * a singular {@link PhysicalEntity} representing glucose that is linked to another singular {@link PhysicalEntity}
 * representing the cytosol via the "part of" {@link SemSimRelation}. To capture the biological meaning
 * of this physical entity in a machine-readable way, we could annotate the singular physical entities 
 * against CHEBI:glucose and FMA:cytosol.
 * <p>
 * This is an example of a "composite annotation," which are explained in more detail in
 * <p>
 * Gennari et al. Multiple ontologies in action: Managing composite annotations. Journal of Biomedical
 *  Informatics, 2011. 44(1):146-154.
 * <p>
 * SemSim model DataStructures can also be linked to the mathematical code in the model that solves 
 * them and their physical units (e.g. kg, molar, mmHg - see {@link UnitOfMeasurement}).
 * DataStructures can also be grouped within SemSim models to form {@link Submodel}s. These groupings
 * are intended to facilitate model extraction tasks. Submodels may represent a biological or computational
 * model aspect.
 */

public class SemSimModel extends SemSimObject implements Cloneable, Annotatable, SemSimCollection {
	public static final IRI LEGACY_CODE_LOCATION_IRI = IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "legacyCodeURI");
	private CurationalMetadata metadata = new CurationalMetadata();
	private double semsimversion;
	
	// Computational model components
	private Set<DataStructure> dataStructures = new HashSet<DataStructure>();
	private Set<RelationalConstraint> relationalConstraints = new HashSet<RelationalConstraint>(); 
	private Set<UnitOfMeasurement> units = new HashSet<UnitOfMeasurement>();
	
	// Physical model components
	private Set<Submodel> submodels = new HashSet<Submodel>();
	private Set<PhysicalEntity> physicalentities = new HashSet<PhysicalEntity>();
	private Set<PhysicalProperty> physicalproperties = new HashSet<PhysicalProperty>();
	private Set<PhysicalPropertyinComposite> associatephysicalproperties = new HashSet<PhysicalPropertyinComposite>();
	private Set<PhysicalProcess> physicalprocesses = new HashSet<PhysicalProcess>();
	private Set<String> errors = new HashSet<String>();
	
	// Model-specific data
	private String namespace;
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");

	private int sourceModelType;
	private String sourcefilelocation;
	
	/**
	 * Constructor without namespace
	 */
	public SemSimModel(){
		setNamespace(generateNamespaceFromDateAndTime());
	}
	
	/**
	 * Constructor with namespace
	 */
	public SemSimModel(String namespace){
		setNamespace(namespace);
	}
	
	public CurationalMetadata getCurationalMetadata() {
		return metadata;
	}
	
	/**
	 * Add an error to the model. Errors are just string notifications indicating a 
	 * problem with the model
	 * @param error A string describing the error
	 */
	public void addError(String error){
		errors.add(error);
	}
	
	
	/**
	 * Add a new {@link Submodel} to the model. If a Submodel with the same name already exists, the new Submodels' name
	 * is appended with a suffix until a unique name is found.
	 * 
	 * @param sub The Submodel to be added.
	 * @return The new Submodel added.
	 */
	public Submodel addSubmodel(Submodel sub){
		while(getSubmodel(sub.getName())!=null){
			sub.setName(sub.getName() + "_");
		}
		submodels.add(sub);
		return sub;
	}
	
	
	/**
	 * Add a new {@link UnitOfMeasurement} to the model. If a unit with the same name already exists, it
	 * is not added.
	 * 
	 * @param unit The UnitOfMeasurement to be added.
	 */
	public void addUnit(UnitOfMeasurement unit){
		if(!containsUnit(unit.getName())){
			getUnits().add(unit);
		}
		else{
			System.err.println("Model already has units " + unit.getName() + ". Using existing unit object.");
		}
	}
	
	
	/**
	 * Add a new {@link CompositePhysicalEntity} to the model. 
	 * 
	 * @param cpe The CompositePhysicalEntity to be added.
	 */
	public CompositePhysicalEntity addCompositePhysicalEntity(CompositePhysicalEntity cpe){
		return addCompositePhysicalEntity(cpe.getArrayListOfEntities(), cpe.getArrayListOfStructuralRelations());
	}
	
	
	/**
	 * Add a new {@link CompositePhysicalEntity} to the model. 
	 * 
	 * @param entlist The ArrayList of physical entities for the composite entity.
	 * @param rellist The ArrayList of SemSimRelations that logically link the physical entities ("part of", "contained in", etc.).
	 * @return If the model already contains a semantically-identical composite physical entity, that entity is returned.
	 * Otherwise, the new CompositePhysicalEntity added to the model is returned.
	 */
	public CompositePhysicalEntity addCompositePhysicalEntity(ArrayList<PhysicalEntity> entlist, ArrayList<StructuralRelation> rellist){
		CompositePhysicalEntity newcpe = new CompositePhysicalEntity(entlist, rellist);
		for(CompositePhysicalEntity cpe : getCompositePhysicalEntities()){
			// If there's already an equivalent CompositePhysicalEntity in the model, return it and don't do anything else.
			if(newcpe.equals(cpe)){
				return cpe;
			}
		}
		physicalentities.add(newcpe);
		
		// If there are physical entities that are part of the composite but not yet added to the model, add them
		// This comes into play when importing annotations from other models
		for(PhysicalEntity ent : newcpe.getArrayListOfEntities()){
			if(!getPhysicalEntities().contains(ent)){
				if(ent.hasRefersToAnnotation()){
					addReferencePhysicalEntity((ReferencePhysicalEntity)ent);
				}
				else addCustomPhysicalEntity((CustomPhysicalEntity)ent);
			}
		}
		return newcpe;
	}
	
	
	/**
	 * Add a new {@link CustomPhysicalEntity} to the model. 
	 * 
	 * @param name The name of the CustomPhysicalEntity to be added.
	 * @param description A free-text description of the CustomPhysicalEntity
	 * @return If the model already contains a CustomPhysicalEntity with the same name, it is returned.
	 * Otherwise a new CustomPhysicalEntity with the name and description specified is returned.
	 */
	public CustomPhysicalEntity addCustomPhysicalEntity(CustomPhysicalEntity cupe){
		if(getCustomPhysicalEntityByName(cupe.getName())!=null) cupe = getCustomPhysicalEntityByName(cupe.getName());
		else{
			physicalentities.add(cupe);
		}
		return cupe;
	}
	
	
	/**
	 * Add a new {@link CustomPhysicalProcess} to the model. 
	 * 
	 * @param name The name of the CustomPhysicalProcess to be added.
	 * @param description A free-text description of the CustomPhysicalProcess
	 * @return If the model already contains a CustomPhysicalProcess with the same name, it is returned.
	 * Otherwise a new CustomPhysicalProcess with the name and description specified is returned.
	 */
	public CustomPhysicalProcess addCustomPhysicalProcess(CustomPhysicalProcess custompp){
		if(getCustomPhysicalProcessByName(custompp.getName())!=null) custompp = getCustomPhysicalProcessByName(custompp.getName());
		else{
			physicalprocesses.add(custompp);
		}
		return custompp;
	}
	
	
	public PhysicalPropertyinComposite addAssociatePhysicalProperty(PhysicalPropertyinComposite pp){
		if(getAssociatePhysicalPropertybyURI(pp.getReferstoURI())!=null) pp = getAssociatePhysicalPropertybyURI(pp.getReferstoURI());
		else{
			associatephysicalproperties.add(pp);
		}
		return pp;
	}
	
	public PhysicalProperty addPhysicalProperty(PhysicalProperty pp){
		if(getPhysicalPropertybyURI(pp.getReferstoURI())!=null) pp = getPhysicalPropertybyURI(pp.getReferstoURI());
		else{
			physicalproperties.add(pp);
		}
		return pp;
	}
	
	public PhysicalPropertyinComposite getAssociatePhysicalPropertybyURI(URI uri) {
		for (PhysicalPropertyinComposite pp : associatephysicalproperties) {
			if (pp.getReferstoURI().equals(uri)) return pp;
		}
		return null;
	}
	
	public PhysicalProperty getPhysicalPropertybyURI(URI uri) {
		for (PhysicalProperty pp :physicalproperties) {
			if (pp.getReferstoURI().equals(uri)) return pp;
		}
		return null;
	}
	
	/**
	 * Add a new ReferencePhysicalEntity to the model. ReferencePhysicalEntities are subclasses of
	 * PhysicalEntities that are defined by their annotation against a reference ontology URI. In
	 * other words, a {@link PhysicalEntity} that is annotated using the SemSimConstants:REFERS_TO_RELATION.
	 * 
	 * @param uri The URI of the reference ontology term that defines the entity.
	 * @param description A free-text description of the entity. Usually taken from the reference ontology.
	 * @return If the model already contains a ReferencePhysicalEntity with the same URI, it is returned.
	 * Otherwise a new ReferencePhysicalEntity with the URI and description specified is returned.
	 */
	public ReferencePhysicalEntity addReferencePhysicalEntity(ReferencePhysicalEntity rpe){
		if(getPhysicalEntityByReferenceURI(rpe.getReferstoURI())!=null) rpe = getPhysicalEntityByReferenceURI(rpe.getReferstoURI());
		else{
			physicalentities.add(rpe);
		}
		return rpe;
	}
	
	
	/**
	 * Add a new ReferencePhysicalProcess to the model. ReferencePhysicalProcesses are subclasses of
	 * PhysicalProcesses that are defined by their annotation against a reference ontology URI. In
	 * other words, a {@link PhysicalProcess} that is annotated using the SemSimConstants:REFERS_TO_RELATION.
	 * 
	 * @param uri The URI of the reference ontology term that defines the process.
	 * @param description A free-text description of the process. Usually taken from the reference ontology.
	 * @return If the model already contains a ReferencePhysicalProcess with the same URI, it is returned.
	 * Otherwise a new ReferencePhysicalProcess with the URI and description specified is returned.
	 */
	public ReferencePhysicalProcess addReferencePhysicalProcess(ReferencePhysicalProcess rpp){
		if(getPhysicalProcessByReferenceURI(rpp.getReferstoURI())!=null) rpp = getPhysicalProcessByReferenceURI(rpp.getReferstoURI());
		else physicalprocesses.add(rpp);
		return rpp;
	}
	
	
	/**
	 * @return True if the model contains a DataStructure with the specified name, otherwise false.
	 */
	public boolean containsDataStructure(String name){
		return getAssociatedDataStructure(name)!=null;
	}
	
	
	/**
	 * @return True if the model contains a {@link UnitOfMeasurement} with the specified name, otherwise false.
	 */
	public boolean containsUnit(String name){
		return getUnit(name)!=null;
	}
	
	
	/**
	 * @return The set of all {@link DataStructure}s in the model.
	 */
	public Set<DataStructure> getAssociatedDataStructures(){
		return dataStructures;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with composite entities in the model.
	 */
	public Set<DataStructure> getDataStructureswithCompositesEntities(){
		Set<DataStructure> dswcpes = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (ds.hasAssociatedPhysicalComponent()) {
				if (ds.getAssociatedPhysicalModelComponent() instanceof CompositePhysicalEntity) {
					dswcpes.add(ds);
				}
			}
		}
		return dswcpes;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with physical properties in the model.
	 */
	public Set<DataStructure> getDataStructureswithPhysicalProcesses(){
		Set<DataStructure> dswprocs = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (ds.hasAssociatedPhysicalComponent()) {
				if (ds.getAssociatedPhysicalModelComponent() instanceof PhysicalProcess) {
					dswprocs.add(ds);
				}
			}
		}
		return dswprocs;
	}
	
	/**
	 * @return The set of {@link DataStructure}s with physical properties in the model.
	 */
	public Set<DataStructure> getDataStructureswithoutAssociatedPhysicalComponents(){
		Set<DataStructure> dswprocs = new HashSet<DataStructure>();
		for (DataStructure ds : dataStructures) {
			if (!ds.hasAssociatedPhysicalComponent()) {
				dswprocs.add(ds);
			}
		}
		return dswprocs;
	}

	/**
	 * Add a {@link DataStructure} to the model. DataStructure is not added to model 
	 * if one with the same name already exists.
	 * 
	 * @param ds The DataStructure to add
	 * @return The DataStructure to add
	 */
	public DataStructure addDataStructure(DataStructure ds){
		if(!containsDataStructure(ds.getName())){
			dataStructures.add(ds);
		}
		else System.err.println("Model already has data structure named " + ds.getName() + ". Using existing data structure.");
		return ds;
	}
	
	/**
	 * @return The set of all computational and physical elements in the model.
	 * This includes anything that is a {@link ComputationalModelComponent} or
	 * a {@link PhysicalModelComponent}.
	 */
	public Set<SemSimComponent> getAllModelComponents(){
		Set<SemSimComponent> set = new HashSet<SemSimComponent>();
		set.addAll(getComputationalModelComponents());
		set.addAll(getPhysicalModelComponents());
		set.addAll(getSubmodels());
		return set;
	}
	
	
	/**
	 * @return The set of all computational elements in the model.
	 * This includes DataStructures, Computations, UnitsOfMeasurement and RelationalConstraints.
	 */
	public Set<ComputationalModelComponent> getComputationalModelComponents(){
		Set<ComputationalModelComponent> set = new HashSet<ComputationalModelComponent>();
		for(DataStructure ds : getAssociatedDataStructures()){
			set.add(ds);
			if(ds.getComputation()!=null) set.add(ds.getComputation());
		}
		set.addAll(getRelationalConstraints());
		set.addAll(getUnits());
		return set;
	}
	
	
	/**
	 * @return A set of all the names of DataStructures contained in the model.
	 */
	public Set<String> getDataStructureNames(){
		Set<String> set = new HashSet<String>();
		for(DataStructure ds : getAssociatedDataStructures()){
			set.add(ds.getName());
		}
		return set;
	}
	
	
	/**
	 * Get a DataStructure in the model by its name.
	 * @param name The name to search for.
	 * @return The DataStructure with the specified name or null if not found.
	 */
	public DataStructure getAssociatedDataStructure(String name){
		for(DataStructure dstest : getAssociatedDataStructures()){
			if(dstest.getName().equals(name)) return dstest;
		}
		return null;
	}
	
	
	/**
	 * @return All DataStructures that are explicitly declared in the model.
	 * Some DataStructures may not be explicitly declared. For example, in MML code one can use
	 * x:t in the RHS of an equation. This can instantiate a variable in the model called "x:t"
	 * without an explicit declaration.
	 */
	public Set<DataStructure> getDeclaredDataStructures(){
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.isDeclared()) dsset.add(ds);
		}
		return dsset;
	}
	
	
	/**
	 * @return All Decimals, Integers and MMLchoiceVariables in the model.
	 */
	public Set<DataStructure> getReals(){
		Set<DataStructure> reals = new HashSet<DataStructure>();
		reals.addAll(getDecimals());
		reals.addAll(getIntegers());
		reals.addAll(getMMLchoiceVars());
		return reals;
	}
	
	
	/**
	 * @return All Decimals contained in the model.
	 */
	public Set<DataStructure> getDecimals(){
		Set<DataStructure> set = new HashSet<DataStructure>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds instanceof Decimal) set.add(ds);
		}
		return set;
	}
	
	
	/**
	 * @return All DataStructures that are associated with {@link FunctionalSubmodel}s.
	 */
	public Set<DataStructure> getDataStructuresFromFunctionalSubmodels(){
		Set<DataStructure> dss = new HashSet<DataStructure>();
		for(FunctionalSubmodel submodel : getFunctionalSubmodels()){
			dss.addAll(submodel.getAssociatedDataStructures());
		}
		return dss;
	}
	
	
	/**
	 * @return All {@link FunctionalSubmodel}s in the model.
	 */
	public Set<FunctionalSubmodel> getFunctionalSubmodels(){
		Set<FunctionalSubmodel> fxnalsubs = new HashSet<FunctionalSubmodel>();
		for(Submodel sub : getSubmodels()){
			if(sub.isFunctional()) fxnalsubs.add((FunctionalSubmodel) sub);
		}
		return fxnalsubs;
	}
	
	
	/**@return The parent FunctionalSubmodel for a MappableVariable.*/
	public FunctionalSubmodel getParentFunctionalSubmodelForMappableVariable(MappableVariable var){
		String compname = var.getName().substring(0, var.getName().lastIndexOf("."));
		return (FunctionalSubmodel) getSubmodel(compname);
	}
	
	
	/**
	 * @return All {@link SemSimInteger}s in the model.
	 */
	public Set<DataStructure> getIntegers(){
		Set<DataStructure> set = new HashSet<DataStructure>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds instanceof SemSimInteger) set.add(ds);
		}
		return set;
	}
	
	
	/**
	 * @return A Map that links MatadataIDs with their associated model component.
	 */
	public Map<String, SemSimComponent> getMetadataIDcomponentMap(){
		Map<String, SemSimComponent> map = new HashMap<String, SemSimComponent>();
		for(SemSimComponent ssc : getAllModelComponents()){
			if(ssc.getMetadataID()!=null) map.put(ssc.getMetadataID(), ssc);
		}
		return map;
	}
	
	
	/**
	 * @param ID A metadata ID used to look up a model component.
	 * @return The model component assigned the given ID, or null if the ID is not associated with any
	 * model component
	 */
	public SemSimComponent getModelComponentByMetadataID(String ID){
		for(SemSimComponent ssc : getAllModelComponents()){
			if(ssc.getMetadataID().equals(ID))
				return ssc;
		}
		return null;
	}
	
	
	/**
	 * @return The MMLchoiceVariables in the model. 
	 */
	public Set<DataStructure> getMMLchoiceVars(){
		Set<DataStructure> set = new HashSet<DataStructure>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds instanceof MMLchoice) set.add(ds);
		}
		return set;
	}
	
	
	/**
	 * @return The solution domain DataStructures used in the model.
	 * These are the DataStructures that specify the domain in which the model is solved. 
	 * Popular examples include time and space.
	 */
	public Set<DataStructure> getSolutionDomains(){
		Set<DataStructure> sdset = new HashSet<DataStructure>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.isSolutionDomain()) sdset.add(ds);
		}
		return sdset;
	}
	
	
	/**
	 * Specify the set of {@link RelationalConstraint}s used in the model.
	 * @param relationalConstraints The set of constraints.
	 */
	public void setRelationalConstraints(Set<RelationalConstraint> relationalConstraints) {
		this.relationalConstraints = relationalConstraints;
	}

	
	/**
	 * @return The {@link RelationalConstraint}s used in the model.
	 */
	public Set<RelationalConstraint> getRelationalConstraints() {
		return relationalConstraints;
	}
	
	
	/**
	 * Add a {@link RelationalConstraint} to the model.
	 */
	public void addRelationalConstraint(RelationalConstraint rel){
		this.relationalConstraints.add(rel);
	}
	
	
	/**
	 * 
	 * @return The model's namespace.
	 */
	public String getNamespace(){
		return namespace;
	}
	
	/**
	 * @return All PhysicalEntities in the model. 
	 */
	public Set<PhysicalEntity> getPhysicalEntities() {
		return physicalentities;
	}
	
	
	/**
	 * @return All PhysicalEntities in the model, except those that either are, or use, a specifically excluded entity 
	 */
	public Set<PhysicalEntity> getPhysicalEntitiesAndExclude(PhysicalEntity entityToExclude) {
		Set<PhysicalEntity> includedents = new HashSet<PhysicalEntity>();
		
		if(entityToExclude!=null){
			for(PhysicalEntity pe : getPhysicalEntities()){
				// If pe is a composite entity, check if it uses the entityToExclue, ignore if so
				if(pe instanceof CompositePhysicalEntity){
					if(!((CompositePhysicalEntity) pe).getArrayListOfEntities().contains(entityToExclude))
						includedents.add(pe);
				}
				else if(pe!=entityToExclude) includedents.add(pe);
			}
		}
		else return getPhysicalEntities();
		
		return includedents;
	}
	
	
	/**
	 * Specify the set of PhysicalEntities in the model. 
	 */
	public void setPhysicalEntities(Set<PhysicalEntity> physicalentities) {
		this.physicalentities.clear();
		this.physicalentities.addAll(physicalentities);
	}

	
	/**
	 * @return All the CompositePhysicalEntities in the model. 
	 */
	public Set<CompositePhysicalEntity> getCompositePhysicalEntities(){
		Set<CompositePhysicalEntity> set = new HashSet<CompositePhysicalEntity>();
		for(PhysicalEntity ent : getPhysicalEntities()){
			if(ent instanceof CompositePhysicalEntity){
				CompositePhysicalEntity cpe = (CompositePhysicalEntity)ent;
				set.add(cpe);
			}
		}
		return set;
	}

	
	/**
	 * @return All ReferencePhysicalEntities in the model.
	 */
	public Set<ReferencePhysicalEntity> getReferencePhysicalEntities(){
		Set<ReferencePhysicalEntity> refents = new HashSet<ReferencePhysicalEntity>();
		for(PhysicalEntity ent : getPhysicalEntities()){
			if(ent instanceof ReferencePhysicalEntity) refents.add((ReferencePhysicalEntity) ent);
		}
		return refents;
	}
	
	
	/**
	 * @return All PhysicalProperties which can be associated with a composite in the model.
	 */
	public Set<PhysicalPropertyinComposite> getAssociatePhysicalProperties() {
		return associatephysicalproperties;
	}
	
	/**
	 * @return All PhysicalProperties which cannot be associated with a composite in the model.
	 */
	public Set<PhysicalProperty> getPhysicalProperties() {
		return physicalproperties;
	}
	
	/**
	 * @return Retrieves all PhysicalEntities, PhysicalProperties, PhsicalProcesses and PhysicalDependencies in the model
	 */
	public Set<PhysicalModelComponent> getPhysicalModelComponents(){
		Set<PhysicalModelComponent> set = new HashSet<PhysicalModelComponent>();
		set.addAll(getAssociatePhysicalProperties());
		set.addAll(getPhysicalEntities());
		set.addAll(getPhysicalProperties());
		set.addAll(getPhysicalProcesses());
		set.addAll(getPhysicalDependencies());
		return set;
	}
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalEntity} that is annotated against the URI using the REFERS_TO_RELATION.
	 * If no ReferencePhysicalEntities have been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalEntity getPhysicalEntityByReferenceURI(URI uri){
		for (PhysicalEntity pe : physicalentities) {
			if (pe.hasRefersToAnnotation()) {
				if (((ReferencePhysicalEntity)pe).getReferstoURI().toString().equals(uri.toString())) {
					return (ReferencePhysicalEntity)pe;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalProcess} that is annotated against the URI using the REFERS_TO_RELATION.
	 * If no ReferencePhysicalProcess has been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalProcess getPhysicalProcessByReferenceURI(URI uri){
		for (PhysicalProcess pp : physicalprocesses) {
			if (pp.hasRefersToAnnotation()) {
					if (((ReferenceTerm)pp).getReferstoURI().equals(uri)) {
						return (ReferencePhysicalProcess)pp;
					}
			}
		}
		return null;
	}
	
	
	/**
	 * @return The set of all CustomPhysicalEntities in the model.
	 */
	public Set<CustomPhysicalEntity> getCustomPhysicalEntities(){
		Set<CustomPhysicalEntity> custs = new HashSet<CustomPhysicalEntity>();
		for(PhysicalEntity ent : getPhysicalEntities()){
			if(ent instanceof CustomPhysicalEntity) custs.add((CustomPhysicalEntity) ent);
		}
		return custs;
	}
	
	
	/**
	 * @param name The name of CustomPhysicalEntity to return
	 * @return The CustomPhysicalEntity with the specified name or null if no match was found.
	 */
	public CustomPhysicalEntity getCustomPhysicalEntityByName(String name){
		for(PhysicalEntity apmc : getPhysicalEntities()){
			if(!apmc.hasRefersToAnnotation()){
				if(apmc.getName().equals(name)) return (CustomPhysicalEntity)apmc;
			}
		}
		return null;
	}
	
	
	/**
	 * @return The set of all CustomPhysicalProcesses in the model.
	 */
	public Set<CustomPhysicalProcess> getCustomPhysicalProcesses(){
		Set<CustomPhysicalProcess> custs = new HashSet<CustomPhysicalProcess>();
		for(PhysicalProcess proc : getPhysicalProcesses()){
			if(!proc.hasRefersToAnnotation()) custs.add((CustomPhysicalProcess) proc);
		}
		return custs;
	}
	
	
	/**
	 * @param name The name of CustomPhysicalProcess to return
	 * @return The CustomPhysicalProcess with the specified name or null if no match was found.
	 */
	public CustomPhysicalProcess getCustomPhysicalProcessByName(String name){
		for(PhysicalProcess apmc : getPhysicalProcesses()){
			if(!apmc.hasRefersToAnnotation()){
				if(apmc.getName().equals(name)) return (CustomPhysicalProcess)apmc;
			}
		}
		return null;
	}
	
	
	/**
	 * @param nametomatch The name of the DataStructure to return
	 * @return The solution domain DataStructure with the specified name or null if no DataStructure that 
	 * is a solution domain was found with that name.
	 */
	public DataStructure getSolutionDomainByName(String nametomatch){
		for(DataStructure ds : getSolutionDomains()){
			if(ds.getName().equals(nametomatch)) return ds;
		}
		return null;
	}
	
	/**
	 * @param name The name of a {@link Submodel} to retrieve
	 * @return The Submodel with the specified name or null if no Submodel found with that name. 
	 */
	public Submodel getSubmodel(String name){
		Submodel sub = null;
		for(Submodel sub1 : getSubmodels()){
			if(sub1.getName().equals(name)){
				sub = sub1;
				break;
			}
		}
		return sub;
	}
	
	
	/**
	 * @param name The name of a {@link UnitOfMeasurement} to retrieve
	 * @return The UnitOfMeasurement with the specified name or null if no UnitOfMeasurement found with that name. 
	 */
	public UnitOfMeasurement getUnit(String name){
		for(UnitOfMeasurement unit : getUnits()){
			if(unit.getName().equals(name)) return unit;
		}
		return null;
	}
	
	
	/**
	 * @return All UnitsOfMeasurement in the model.
	 */
	public Set<UnitOfMeasurement> getUnits(){
		return units;
	}
	
	
	/**
	 * @param nametomatch The name to look up
	 * @return True if the model contains a solution domain with the name specified, otherwise false.
	 */
	public Boolean hasSolutionDomainName(String nametomatch){
		Boolean test = false;
		for(DataStructure ds : getSolutionDomains()){
			if(ds.getName().equals(nametomatch)) test = true;
		}
		return test;
	}
	
	
	/**
	 * @return The names of all the solution domains in the model.
	 */
	public Set<String> getSolutionDomainNames(){
		Set<String> sdnames = new HashSet<String>();
		for(DataStructure ds : getSolutionDomains()){
			sdnames.add(ds.getName());
		}
		return sdnames;
	}
	
	
	/**
	 * Set the namespace of the model.
	 * @param namespace The namespace to use.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	/**
	 * Converts the SemSimModel into an OWLOntology object
	 * @return An OWLOntology representation of the SemSimModel
	 * @throws OWLException
	 */
	public OWLOntology toOWLOntology() throws OWLException{
		return new SemSimOWLwriter(this).createOWLOntologyFromModel();
	}

	/**
	 * @return A new SemSim model namespace from the current date and time
	 */
	public String generateNamespaceFromDateAndTime(){
		namespace = SemSimConstants.SEMSIM_NAMESPACE.replace("#", "/" + sdf.format(new Date()).replace("-", "m").replace("+", "p") + "#");
		return namespace;
	}

	
	/**
	 * @return All ReferenceOntologyAnnotations in the model.
	 */
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations() {
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(Annotation ann : getAnnotations()){
			if(ann instanceof ReferenceOntologyAnnotation) raos.add((ReferenceOntologyAnnotation)ann);
		}
		return raos;
	}

	
	/**
	 * Specify the set of {@link Submodel}s in the model
	 * @param submodels The new set of Submodels to include
	 */
	public void setSubmodels(Set<Submodel> submodels) {
		this.submodels = submodels;
	}

	
	/**
	 * @return All {@link Submodel}s in the model.
	 */
	public Set<Submodel> getSubmodels() {
		return submodels;
	}
	
	/**
	 * Specify the set of PhysicalProcesses in the model.
	 * @param physicalprocess The new set of PhysicalProcesses to include
	 */
	public void setPhysicalProcesses(Set<PhysicalProcess> physicalprocess) {
		this.physicalprocesses.clear();
		this.physicalprocesses.addAll(physicalprocess);
	}

	
	/**
	 * @return All {@link PhysicalProcess}es in the model.
	 */
	public Set<PhysicalProcess> getPhysicalProcesses() {
		return physicalprocesses;
	}
	
	
	/**
	 * @return All {@link PhysicalProcess}es in the model that are annotated against
	 * a URI with the REFERS_TO_RELATION (as opposed to CustomPhysicalProcesses).
	 */
	public Set<ReferencePhysicalProcess> getReferencePhysicalProcesses(){
		Set<ReferencePhysicalProcess> refprocs = new HashSet<ReferencePhysicalProcess>();
		for(PhysicalProcess proc : getPhysicalProcesses()){
			if(proc.hasRefersToAnnotation()) refprocs.add((ReferencePhysicalProcess) proc);
		}
		return refprocs;
	}

	
	/**
	 * @return All PhysicalDependencies in the model.
	 */
	public Set<PhysicalDependency> getPhysicalDependencies() {
		Set<PhysicalDependency> deps = new HashSet<PhysicalDependency>();
		for(DataStructure ds : getAssociatedDataStructures()){
			if(ds.getComputation()!=null){
				if(ds.getComputation().getPhysicalDependency()!=null)
					deps.add(ds.getComputation().getPhysicalDependency());
			}
		}
		return deps;
	}
	
	/**
	 * @return The location of the raw computer source code associated with this model.
	 */
	
	public String getLegacyCodeLocation() {
		return sourcefilelocation;
	}

	public void setSourceFileLocation(String sourcefilelocation) {
		this.sourcefilelocation = sourcefilelocation;
	}
	
	/**
	 * Set the errors associated with the model.
	 * @param errors A set of errors written as strings.
	 */
	public void setErrors(Set<String> errors) {
		this.errors = errors;
	}

	
	/**
	 * @return All errors associated with the model.
	 */
	public Set<String> getErrors() {
		return errors;
	}
	
	
	/**
	 * @return The number of errors associated with the model.
	 */
	public int getNumErrors(){
		return errors.size();
	}
	
	
	/**
	 * Print all the errors associated with the model to System.err.
	 */
	public void printErrors(){
		for(String err : getErrors()) System.err.println("***************\n" + err + "\n");
	}
	
	
	/**
	 * Delete a {@link DataStructure} from the model.
	 * @param name The name of the DataStructure to delete.
	 */
	public void removeDataStructure(String name) {
		if(getAssociatedDataStructure(name)!=null){
			DataStructure ds = getAssociatedDataStructure(name);
			
			for(DataStructure otherds : ds.getUsedToCompute()){
				otherds.getComputation().getInputs().remove(ds);
			}
			
			// If a MappableVariable, remove mappings and remove equation from parent 
			// FunctionalSubmodel
			if(ds instanceof MappableVariable){
				MappableVariable mapv = (MappableVariable)ds;
				
				// Remove mappings to the data structure
				for(MappableVariable fromv: mapv.getMappedFrom()){
					fromv.getMappedTo().remove(mapv);
				}
				
				// Remove mappings from the data structure
				for(MappableVariable tov : mapv.getMappedTo()){
					tov.getMappedFrom().remove(mapv);
				}
				
				FunctionalSubmodel fs = getParentFunctionalSubmodelForMappableVariable(mapv);
				fs.removeVariableEquationFromMathML(mapv);
			}
			
			for(Submodel sub : getSubmodels()){
				if(sub.getAssociatedDataStructures().contains(ds)) sub.getAssociatedDataStructures().remove(ds);
			}
			dataStructures.remove(ds);
		}
	}
	
	/**
	 * Remove a physical entity from the model cache
	 * @param ent The physical entity to remove
	 */
	public void removePhysicalEntityFromCache(PhysicalEntity ent){
		if(physicalentities.contains(ent))
			physicalentities.remove(ent);
	}
	
	/**
	 * Remove a physical process from the model cache
	 * @param ent The physical process to remove
	 */
	public void removePhysicalProcessFromCache(PhysicalProcess ent){
		if(physicalprocesses.contains(ent))
			physicalprocesses.remove(ent);
	}
	
	
	/**
	 * Delete a {@link Submodel} from the model (does not remove the Submodel's associated DataStructures, 
	 * just the Submodel) 
	 * @param submodel The Submodel to be deleted.
	 */
	public void removeSubmodel(Submodel submodel) {
		// Remove from the model's collection of submodels
		submodels.remove(submodel);

		// If the submodel is subsumed by another submodel, remove the subsumption
		for(Submodel sub : getSubmodels()) sub.removeSubmodel(submodel);
	}
	
	
	/**
	 * @return A clone of the model.
	 */
	public SemSimModel clone() throws CloneNotSupportedException {
        return (SemSimModel) super.clone();
	}

	
	/**
	 * Specify which modeling language was used for the original version of the model.
	 * See {@link ModelClassifier} constants. 
	 * @param originalModelType An integer corresponding to the language of the original model code (see {@link ModelClassifier} ).
	 */
	public void setSourceModelType(int originalModelType) {
		this.sourceModelType = originalModelType;
	}

	
	/**
	 * @return An integer representing the language of the original model code (see {@link ModelClassifier} ) 
	 * and associated constants.
	 */
	public int getSourceModelType() {
		return sourceModelType;
	}
	
	// Required by annotable interface:
	/**
	 * @return All SemSim Annotations applied to an object
	 */
	public Set<Annotation> getAnnotations() {
		return annotations;
	}
	
	
	/**
	 * Set the SemSim Annotations for an object
	 * @param annset The set of annotations to apply
	 */
	public void setAnnotations(Set<Annotation> annset){
		annotations.clear();
		annotations.addAll(annset);
	}

	
	/**
	 * Add a SemSim {@link Annotation} to this object
	 * @param ann The {@link Annotation} to add
	 */
	public void addAnnotation(Annotation ann) {
		annotations.add(ann);
	}
	
	/**
	 * Add a SemSim {@link Annotation} to this object
	 * @param ann The {@link Annotation} to add
	 */
	public void setModelAnnotation(Metadata metaID, String value) {
		metadata.setAnnotationValue(metaID, value);
	}
	
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
	public void addReferenceOntologyAnnotation(SemSimRelation relation, URI uri, String description){
		addAnnotation(new ReferenceOntologyAnnotation(relation, uri, description));
	}

	
	/**
	 * Get all SemSim {@link ReferenceOntologyAnnotation}s applied to an object
	 * that have a specific {@link SemSimRelation}.
	 * 
	 * @param relation The {@link SemSimRelation} that filters the annotations 
	 * to return  
	 */
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(SemSimRelation relation) {
		Set<ReferenceOntologyAnnotation> raos = new HashSet<ReferenceOntologyAnnotation>();
		for(Annotation ann : getAnnotations()){
			if(ann instanceof ReferenceOntologyAnnotation && ann.getRelation()==relation)
				raos.add((ReferenceOntologyAnnotation)ann);
		}
		return raos;
	}

	/**
	 * @return True if an object has at least one {@link Annotation}, otherwise false.
	 */
	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}

	/**
	 * Delete all {@link ReferenceOntologyAnnotation}s applied to this object
	 */
	public void removeAllReferenceAnnotations() {
		Set<Annotation> newset = new HashSet<Annotation>();
		for(Annotation ann : this.getAnnotations()){
			if(!(ann instanceof ReferenceOntologyAnnotation)) newset.add(ann);
		}
		annotations.clear();
		annotations.addAll(newset);
	}
	// End of methods required by Annotatable interface	

	@Override
	public String getDescription() {
		return metadata.getAnnotationValue(Metadata.description);
	}
	@Override
	public void setDescription(String value) {
		metadata.setAnnotationValue(Metadata.description, value);
	}
	
	public double getSemsimversion() {
		return semsimversion;
	}

	public void setSemsimversion(double semsimversion) {
		this.semsimversion = semsimversion;
	}
	
	public void setSemsimversion(String semsimversion) {
		this.semsimversion = Double.valueOf(semsimversion);
	}
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.SEMSIM_MODEL_CLASS_URI;
	}
		
	public void replacePhysicalProperty(PhysicalPropertyinComposite tobereplaced, PhysicalPropertyinComposite toreplace) {
		Set<PhysicalPropertyinComposite> pps = new HashSet<PhysicalPropertyinComposite>();
		pps.addAll(associatephysicalproperties);
		for (PhysicalPropertyinComposite pp : pps) {
			if (pp.equals(tobereplaced)) {
				associatephysicalproperties.remove(pp);
				associatephysicalproperties.add(toreplace);
			}
		}
		for (DataStructure ds : dataStructures) {
			if (ds.hasPhysicalProperty()) {
				if (ds.getPhysicalProperty().equals(tobereplaced)) {
					ds.setAssociatedPhysicalProperty(toreplace);
				}
			}
		}
	}
}
