package semsim.model;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.annotation.SemSimRelation;
import semsim.model.annotation.StructuralRelation;
import semsim.model.computational.ComputationalModelComponent;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.MMLchoice;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.CustomPhysicalEntity;
import semsim.model.physical.CustomPhysicalProcess;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.PhysicalDependency;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.ReferencePhysicalEntity;
import semsim.model.physical.ReferencePhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.writing.CellMLwriter;
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
 * creating a {@link DataStructure} with the name "cgluc" and a corresponding {@link PhysicalProperty}
 * that is annotated against the Ontology of Physics for Biology term "Chemical concentration."
 * This {@link PhysicalProperty} would be a property of a {@link PhysicalEntity} representing 
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

public class SemSimModel extends SemSimComponent implements Cloneable, Annotatable{
	
	// Computational model components
	private Set<DataStructure> dataStructures = new HashSet<DataStructure>();
	private Set<RelationalConstraint> relationalConstraints = new HashSet<RelationalConstraint>(); 
	private Set<UnitOfMeasurement> units = new HashSet<UnitOfMeasurement>();
	
	// Physical model components
	private Set<Submodel> submodels = new HashSet<Submodel>();
	private Set<PhysicalEntity> physicalentities = new HashSet<PhysicalEntity>();
	private Set<PhysicalProcess> physicalprocesses = new HashSet<PhysicalProcess>();
	private Set<String> errors = new HashSet<String>();
	
	// Model-specific data
	private String namespace;
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");
	private double semSimVersion = SemSimConstants.SEMSIM_VERSION;
	private int sourceModelType;
	public static String unspecifiedName = "*unspecified*";
	
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
			if(newcpe.compareTo(cpe)==0){
				return cpe;
			}
		}
		physicalentities.add(newcpe);
		
		// If there are physical entities that are part of the composite but not yet added to the model, add them
		// This comes into play when importing annotations from other models
		for(PhysicalEntity ent : newcpe.getArrayListOfEntities()){
			if(!getPhysicalEntities().contains(ent)){
				if(ent.hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = ent.getFirstRefersToReferenceOntologyAnnotation();
					addReferencePhysicalEntity(roa.getReferenceURI(), roa.getValueDescription());
				}
				else addCustomPhysicalEntity(ent.getName(), ent.getDescription());
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
	public CustomPhysicalEntity addCustomPhysicalEntity(String name, String description){
		CustomPhysicalEntity custompe = null;
		if(getCustomPhysicalEntityByName(name)!=null) custompe = getCustomPhysicalEntityByName(name);
		else{
			custompe = new CustomPhysicalEntity(name, description);
			physicalentities.add(custompe);
		}
		return custompe;
	}
	
	
	/**
	 * Add a new {@link CustomPhysicalProcess} to the model. 
	 * 
	 * @param name The name of the CustomPhysicalProcess to be added.
	 * @param description A free-text description of the CustomPhysicalProcess
	 * @return If the model already contains a CustomPhysicalProcess with the same name, it is returned.
	 * Otherwise a new CustomPhysicalProcess with the name and description specified is returned.
	 */
	public CustomPhysicalProcess addCustomPhysicalProcess(String name, String description){
		CustomPhysicalProcess custompp = null;
		if(getCustomPhysicalProcessByName(name)!=null) custompp = getCustomPhysicalProcessByName(name);
		else{
			custompp = new CustomPhysicalProcess(name, description);
			physicalprocesses.add(custompp);
		}
		return custompp;
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
	public ReferencePhysicalEntity addReferencePhysicalEntity(URI uri, String description){
		ReferencePhysicalEntity rpe = null;
		if(getPhysicalEntityByReferenceURI(uri)!=null) rpe = getPhysicalEntityByReferenceURI(uri);
		else{
			rpe = new ReferencePhysicalEntity(uri, description);
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
	public ReferencePhysicalProcess addReferencePhysicalProcess(URI uri, String description){
		ReferencePhysicalProcess rpp = null;
		if(getPhysicalProcessByReferenceURI(uri)!=null) rpp = getPhysicalProcessByReferenceURI(uri);
		else{
			rpp = new ReferencePhysicalProcess(uri, description);
			physicalprocesses.add(rpp);
		}
		return rpp;
	}
	
	
	/**
	 * @return True if the model contains a DataStructure with the specified name, otherwise false.
	 */
	public boolean containsDataStructure(String name){
		return getDataStructure(name)!=null;
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
	public Set<DataStructure> getDataStructures(){
		return dataStructures;
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
		return set;
	}
	
	
	/**
	 * @return The set of all computational elements in the model.
	 * This includes DataStructures, Computations, UnitsOfMeasurement and RelationalConstraints.
	 */
	public Set<ComputationalModelComponent> getComputationalModelComponents(){
		Set<ComputationalModelComponent> set = new HashSet<ComputationalModelComponent>();
		for(DataStructure ds : getDataStructures()){
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
		for(DataStructure ds : getDataStructures()){
			set.add(ds.getName());
		}
		return set;
	}
	
	
	/**
	 * Get a DataStructure in the model by its name.
	 * @param name The name to search for.
	 * @return The DataStructure with the specified name or null if not found.
	 */
	public DataStructure getDataStructure(String name){
		for(DataStructure dstest : getDataStructures()){
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
		for(DataStructure ds : getDataStructures()){
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
		for(DataStructure ds : getDataStructures()){
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
			if(sub instanceof FunctionalSubmodel) fxnalsubs.add((FunctionalSubmodel) sub);
		}
		return fxnalsubs;
	}
	
	
	/**
	 * @return All {@link SemSimInteger}s in the model.
	 */
	public Set<DataStructure> getIntegers(){
		Set<DataStructure> set = new HashSet<DataStructure>();
		for(DataStructure ds : getDataStructures()){
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
		for(DataStructure ds : getDataStructures()){
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
		for(DataStructure ds : getDataStructures()){
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
	 * @return A Map where the KeySet includes all PhysicalProperties in the model that are properties of 
	 * CompositePhyiscalEntities. The value for each key is the CompositePhysicalEntity that possesses the property. 
	 */
	public Map <PhysicalProperty,CompositePhysicalEntity> getPropertyAndCompositePhysicalEntityMap(){
		Map<PhysicalProperty,CompositePhysicalEntity> table = new HashMap<PhysicalProperty,CompositePhysicalEntity>();
		for(PhysicalProperty pp: getPhysicalProperties()){
			if(pp.getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
				CompositePhysicalEntity cpe = (CompositePhysicalEntity) pp.getPhysicalPropertyOf();
				table.put(pp,cpe);
			}
		}
		return table;
	}
	
	
	/**
	 * @return A Map where the KeySet includes all PhysicalProperties in the model that are properties of 
	 * PhysicalProcesses. The value for each key is the PhysicalProcess that possesses the property. 
	 */
	public Map <PhysicalProperty,PhysicalProcess> getPropertyAndPhysicalProcessTable(){
		Map<PhysicalProperty,PhysicalProcess> table = new HashMap<PhysicalProperty,PhysicalProcess>();
		for(PhysicalProperty pp: getPhysicalProperties()){
			if(pp.getPhysicalPropertyOf() instanceof PhysicalProcess){
				PhysicalProcess pproc = (PhysicalProcess) pp.getPhysicalPropertyOf();
				table.put(pp,pproc);
			}
		}
		return table;
	}
	
	
	/**
	 * @return A Map where the KeySet includes all PhysicalProperties in the model that are properties of 
	 * PhysicalEntities (composite or singular). The value for each key is the PhysicalEntity that possesses the property. 
	 */
	public Map <PhysicalProperty,PhysicalEntity> getPropertyAndPhysicalEntityMap(){
		Map<PhysicalProperty,PhysicalEntity> table = new HashMap<PhysicalProperty,PhysicalEntity>();
		for(PhysicalProperty pp: getPhysicalProperties()){
			if(pp.getPhysicalPropertyOf() instanceof PhysicalEntity){
				PhysicalEntity ent = (PhysicalEntity) pp.getPhysicalPropertyOf();
				table.put(pp,ent);
			}
		}
		return table;
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
	 * @return All PhysicalProperties in the model.
	 */
	public Set<PhysicalProperty> getPhysicalProperties() {
		Set<PhysicalProperty> pps = new HashSet<PhysicalProperty>();
		for(DataStructure ds : getDataStructures()){
			if(ds.getPhysicalProperty()!=null) pps.add(ds.getPhysicalProperty());
		}
		for(DataStructure ds : getDataStructuresFromFunctionalSubmodels()){
			if(ds.getPhysicalProperty()!=null) pps.add(ds.getPhysicalProperty());
		}
		return pps;
	}
	

	/**
	 * @return Retrieves all PhysicalEntities, PhysicalProperties, PhsicalProcesses, PhysicalDependencies and Submodels in the model
	 */
	public Set<PhysicalModelComponent> getPhysicalModelComponents(){
		Set<PhysicalModelComponent> set = new HashSet<PhysicalModelComponent>();
		set.addAll(getPhysicalProperties());
		set.addAll(getPhysicalEntities());
		set.addAll(getPhysicalProcesses());
		set.addAll(getPhysicalDependencies());
		set.addAll(getSubmodels());
		return set;
	}
	
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link PhysicalModelComponent} that is annotated against the URI using the REFERS_TO_RELATION.
	 */
	public PhysicalModelComponent getPhysicalModelComponentByReferenceURI(URI uri){
		for(PhysicalModelComponent pmcomp : getPhysicalModelComponents()){
			for(ReferenceOntologyAnnotation ann : pmcomp.getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION)){
				if(ann.getReferenceURI().compareTo(uri)==0) return pmcomp;
			}
		}
		return null;
	}
	
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalEntity} that is annotated against the URI using the REFERS_TO_RELATION.
	 * If no ReferencePhysicalEntities have been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalEntity getPhysicalEntityByReferenceURI(URI uri){
		if(getPhysicalModelComponentByReferenceURI(uri)!=null){
			Annotatable pmc = getPhysicalModelComponentByReferenceURI(uri);
			if(pmc instanceof ReferencePhysicalEntity) return (ReferencePhysicalEntity)pmc;
		}
		return null;
	}
	
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalProcess} that is annotated against the URI using the REFERS_TO_RELATION.
	 * If no ReferencePhysicalProcess has been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalProcess getPhysicalProcessByReferenceURI(URI uri){
		Annotatable pmc = getPhysicalModelComponentByReferenceURI(uri);
		if(pmc!=null){
			if(pmc instanceof ReferencePhysicalProcess) return (ReferencePhysicalProcess)pmc;
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
		for(PhysicalModelComponent apmc : getPhysicalEntities()){
			if(apmc instanceof CustomPhysicalEntity){
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
			if(proc instanceof CustomPhysicalProcess) custs.add((CustomPhysicalProcess) proc);
		}
		return custs;
	}
	
	
	/**
	 * @param name The name of CustomPhysicalProcess to return
	 * @return The CustomPhysicalProcess with the specified name or null if no match was found.
	 */
	public CustomPhysicalProcess getCustomPhysicalProcessByName(String name){
		for(PhysicalModelComponent apmc : getPhysicalProcesses()){
			if(apmc instanceof CustomPhysicalProcess){
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
	 * @return The version of the SemSim API used to generate the model.
	 */
	public double getSemSimVersion() {
		return semSimVersion;
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
		return new SemSimOWLwriter().createOWLOntologyFromModel(this);
	}
	
	
	/**
	 * Output the model as a Web Ontology Language (OWL) file
	 * @param destination The output location
	 * @throws OWLException
	 */
	public void writeSemSimOWLFile(File destination) throws OWLException{
		new SemSimOWLwriter().writeToFile(this, destination);
	}
	
	
	/**
	 * Output the model as a Web Ontology Language (OWL) file
	 * @param uri The output location as URI
	 * @throws OWLException
	 */
	public void writeSemSimOWLFile(URI uri) throws OWLException{
		new SemSimOWLwriter().writeToFile(this, uri);
	}
	
	
	/**
	 * Output the model as a CellML file
	 * @param destination The output location
	 */
	public void writeCellMLFile(File destination){
		new CellMLwriter().writeToFile(this, destination);
	}
	
//	public File writePhysioMap(File file) throws IOException, OWLException{
//		return PhysioMapWriter.translate(this, file);
//	}


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
			if(proc instanceof ReferencePhysicalProcess) refprocs.add((ReferencePhysicalProcess) proc);
		}
		return refprocs;
	}

	
	/**
	 * @return All PhysicalDependencies in the model.
	 */
	public Set<PhysicalDependency> getPhysicalDependencies() {
		Set<PhysicalDependency> deps = new HashSet<PhysicalDependency>();
		for(DataStructure ds : getDataStructures()){
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
		for(Annotation ann : getAnnotations()){
			if(ann.getRelation()==SemSimConstants.LEGACY_CODE_LOCATION_RELATION) return (String) ann.getValue();
		}
		return null;
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
		if(getDataStructure(name)!=null){
			DataStructure ds = getDataStructure(name);
			for(DataStructure otherds : ds.getUsedToCompute()){
				otherds.getComputation().getInputs().remove(ds);
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
		// Remove submodel if a submodel in another submodel
		for(Submodel sub : getSubmodels()) sub.removeSubmodel(submodel);
		submodels.remove(submodel);
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
	
	public Set<DataStructure> getDataStructuresWithUnspecifiedAnnotations(){
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		for(DataStructure ds : getDataStructures()){
			if(ds.hasPhysicalProperty()){
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null){
					if(ds.getPhysicalProperty().getPhysicalPropertyOf().getName().equals(unspecifiedName)){
						dsset.add(ds);
					}
					if(ds.getPhysicalProperty().getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
						for(PhysicalEntity pe : ((CompositePhysicalEntity)ds.getPhysicalProperty().getPhysicalPropertyOf()).getArrayListOfEntities()){
							if(pe.getName().equals(unspecifiedName))
								dsset.add(ds);
						}
					}
				}
			}
			else System.out.println(ds.getName() + " didn't have a physical property");
		}
		return dsset;
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
	 * Retrieve the first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses the SemSim:refersTo relation (SemSimConstants.REFERS_TO_RELATION).
	 */
	public ReferenceOntologyAnnotation getFirstRefersToReferenceOntologyAnnotation(){
		if(!getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION).isEmpty())
			return getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION).toArray(new ReferenceOntologyAnnotation[]{})[0];
		else{
			return null;
		}
	}
	
	
	/**
	 * Retrieve the first {@link ReferenceOntologyAnnotation} found applied to this object
	 * that uses the SemSim:refersTo relation (SemSimConstants.REFERS_TO_RELATION) and references
	 * a specific URI
	 * 
	 * @param uri The URI of the ontology term to search for in the set of {@link ReferenceOntologyAnnotation}s
	 * applied to this object.
	 */
	public ReferenceOntologyAnnotation getRefersToReferenceOntologyAnnotationByURI(URI uri){
		for(ReferenceOntologyAnnotation ann : getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION)){
			if(ann.getReferenceURI().compareTo(uri)==0) return ann;
		}
		return null;
	}
	
	
	/**
	 * @return True if an object has at least one {@link Annotation}, otherwise false.
	 */
	public Boolean isAnnotated(){
		return !getAnnotations().isEmpty();
	}
	
	
	/**
	 * @return True if an object has at least one {@link ReferenceOntologyAnnotation}, otherwise false;
	 */
	public Boolean hasRefersToAnnotation(){
		return getFirstRefersToReferenceOntologyAnnotation()!=null;
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
	

}
