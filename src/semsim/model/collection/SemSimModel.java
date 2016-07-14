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

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.Annotatable;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.SemSimComponent;
import semsim.model.computational.ComputationalModelComponent;
import semsim.model.computational.Event;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalDependency;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalDependency;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.SemSimCopy;
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

public class SemSimModel extends SemSimCollection implements Annotatable  {
	public static final IRI LEGACY_CODE_LOCATION_IRI = IRI.create(RDFNamespace.SEMSIM.getNamespaceasString() + "legacyCodeURI");
	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSSZ");
	
	private String namespace;
	private ModelType sourceModelType;
	private ModelAccessor sourcefilelocation;
	private double semsimversion;
	
	// Model-specific data
	private Set<Annotation> annotations = new HashSet<Annotation>();
	private CurationalMetadata metadata = new CurationalMetadata();
	private Set<String> errors = new HashSet<String>();
	
	// Computational model components
	private Set<RelationalConstraint> relationalConstraints = new HashSet<RelationalConstraint>(); 
	private Set<UnitOfMeasurement> units = new HashSet<UnitOfMeasurement>();
	private Set<Event> events = new HashSet<Event>();
	
	// Physical model components
	private Set<PhysicalEntity> physicalentities = new HashSet<PhysicalEntity>();
	private Set<PhysicalProperty> physicalproperties = new HashSet<PhysicalProperty>();
	private Set<PhysicalPropertyinComposite> associatephysicalproperties = new HashSet<PhysicalPropertyinComposite>();
	private Set<PhysicalProcess> physicalprocesses = new HashSet<PhysicalProcess>();
	private Set<PhysicalDependency> physicaldependencies = new HashSet<PhysicalDependency>();

	/**
	 * Constructor without namespace
	 */
	public SemSimModel(){
		super(SemSimTypes.MODEL);
		setNamespace(generateNamespaceFromDateAndTime());
	}
	
	/**
	 * Constructor with namespace
	 */
	public SemSimModel(String namespace){
		super(SemSimTypes.MODEL);
		setNamespace(namespace);
	}
	
	private SemSimModel(SemSimModel ssmtocopy) {
		super(ssmtocopy);
		namespace = new String(ssmtocopy.namespace);
		
		if(ssmtocopy.sourcefilelocation != null)
			sourcefilelocation = new ModelAccessor(ssmtocopy.sourcefilelocation);
		
		sourceModelType = ssmtocopy.sourceModelType;
		semsimversion = ssmtocopy.semsimversion;
		importCurationalMetadatafromModel(ssmtocopy, true);
		annotations = SemSimCopy.copyAnnotations(ssmtocopy.annotations);
		physicalproperties.addAll(ssmtocopy.physicalproperties);
		associatephysicalproperties.addAll(ssmtocopy.associatephysicalproperties);
		
		new SemSimCopy(ssmtocopy, this);
	}
	
	public CurationalMetadata getCurationalMetadata() {
		return metadata;
	}
	
	public void importCurationalMetadatafromModel(SemSimModel toimport, boolean overwrite) {
		metadata.importMetadata(toimport.metadata, overwrite);
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
		
		if( ! containsUnit(unit.getName())) units.add(unit);
		else System.err.println("Model already has units " + unit.getName() + ". Using existing unit object.");
	}
	
	public void setUnits(HashSet<UnitOfMeasurement> units) {
		this.units = units;
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
			
			if(newcpe.equals(cpe)) return cpe;
		}
		physicalentities.add(newcpe);
		
		// If there are physical entities that are part of the composite but not yet added to the model, add them
		// This comes into play when importing annotations from other models
		for(PhysicalEntity ent : newcpe.getArrayListOfEntities()){
			
			if(!getPhysicalEntities().contains(ent)){
				
				if(ent.hasPhysicalDefinitionAnnotation())
					addReferencePhysicalEntity((ReferencePhysicalEntity)ent);
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
		else physicalentities.add(cupe);
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
		else physicalprocesses.add(custompp);
		
		return custompp;
	}
	
	
	public PhysicalPropertyinComposite addAssociatePhysicalProperty(PhysicalPropertyinComposite pp){
		
		if(getAssociatePhysicalPropertybyURI(pp.getPhysicalDefinitionURI())!=null) pp = getAssociatePhysicalPropertybyURI(pp.getPhysicalDefinitionURI());
		else associatephysicalproperties.add(pp);
		
		return pp;
	}
	
	public PhysicalProperty addPhysicalProperty(PhysicalProperty pp){
		
		if(getPhysicalPropertybyURI(pp.getPhysicalDefinitionURI())!=null) pp = getPhysicalPropertybyURI(pp.getPhysicalDefinitionURI());
		else physicalproperties.add(pp);
		
		return pp;
	}
	
	public PhysicalPropertyinComposite getAssociatePhysicalPropertybyURI(URI uri) {
		for (PhysicalPropertyinComposite pp : associatephysicalproperties) {
			if (pp.getPhysicalDefinitionURI().equals(uri)) return pp;
		}
		return null;
	}
	
	public PhysicalProperty getPhysicalPropertybyURI(URI uri) {
		for (PhysicalProperty pp :physicalproperties) {
			if (pp.getPhysicalDefinitionURI().equals(uri)) return pp;
		}
		return null;
	}
	
	/**
	 * Add a new ReferencePhysicalEntity to the model. ReferencePhysicalEntities are subclasses of
	 * PhysicalEntities that are defined by their annotation against a reference ontology URI. In
	 * other words, a {@link PhysicalEntity} that is annotated using the SemSimConstants:HAS_PHYSICAL_DEFINITION_RELATION.
	 * 
	 * @param uri The URI of the reference ontology term that defines the entity.
	 * @param description A free-text description of the entity. Usually taken from the reference ontology.
	 * @return If the model already contains a ReferencePhysicalEntity with the same URI, that entity is returned.
	 * Otherwise a new ReferencePhysicalEntity with the URI and description specified is returned.
	 */
	public ReferencePhysicalEntity addReferencePhysicalEntity(ReferencePhysicalEntity rpe){
		
		if(getPhysicalEntityByReferenceURI(rpe.getPhysicalDefinitionURI())!=null) rpe = getPhysicalEntityByReferenceURI(rpe.getPhysicalDefinitionURI());
		else physicalentities.add(rpe);
		
		return rpe;
	}
	
	/**
	 * Add a new ReferencePhysicalProcess to the model. ReferencePhysicalProcesses are subclasses of
	 * PhysicalProcesses that are defined by their annotation against a reference ontology URI. In
	 * other words, a {@link PhysicalProcess} that is annotated using the SemSimConstants:HAS_PHYSICAL_DEFINITION_RELATION.
	 * 
	 * @param uri The URI of the reference ontology term that defines the process.
	 * @param description A free-text description of the process. Usually taken from the reference ontology.
	 * @return If the model already contains a ReferencePhysicalProcess with the same URI, that process is returned.
	 * Otherwise a new ReferencePhysicalProcess with the URI and description specified is returned.
	 */
	public ReferencePhysicalProcess addReferencePhysicalProcess(ReferencePhysicalProcess rpp){
		
		if(getPhysicalProcessByReferenceURI(rpp.getPhysicalDefinitionURI())!=null) 
			rpp = getPhysicalProcessByReferenceURI(rpp.getPhysicalDefinitionURI());
		else physicalprocesses.add(rpp);
		
		return rpp;
	}
	
	/**
	 * Add a new ReferencePhysicalDependency to the model.
	 * 
	 * @param uri The URI of the reference ontology term that defines the dependency.
	 * @param description A free-text description of the dependency. Usually taken from the reference ontology.
	 * @return If the model already contains a ReferencePhysicalDependency with the same URI, that dependency is returned.
	 * Otherwise a new ReferencePhysicalDependency with the URI and description specified is returned.
	 */
	public ReferencePhysicalDependency addReferencePhysicalDependency(ReferencePhysicalDependency dep){
		
		if(getPhysicalDependencyByReferenceURI(dep.getPhysicalDefinitionURI()) != null) 
				dep = getPhysicalDependencyByReferenceURI(dep.getPhysicalDefinitionURI());
		else physicaldependencies.add(dep);
		
		return dep;
	}
	
	/**
	 * @return True if the model contains a {@link UnitOfMeasurement} with the specified name, otherwise false.
	 */
	public boolean containsUnit(String name){
		return getUnit(name)!=null;
	}
		
	/**
	 * @return The set of all {@link Events} in the model
	 */
	public Set<Event> getEvents(){
		return events;
	}
	
	/**
	 * Specify the set of {@link Events} in the model
	 * @param theevents The set of {@link Events} that will be assigned to the model
	 */
	public void setEvents(Set<Event> theevents){
		this.events.clear();
		this.events.addAll(theevents);
	}
	
	
	/**
	 * Add an {@link Event} to the model
	 * @param theevent The {@link Event} to add
	 */
	public void addEvent(Event theevent){
		this.events.add(theevent);
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
	 * @return The set of all computational and physical elements in the model.
	 * This includes anything that is a {@link ComputationalModelComponent} or
	 * a {@link PhysicalModelComponent}.
	 */
	public Set<SemSimObject> getAllModelComponentsandCollections(){
		Set<SemSimObject> set = new HashSet<SemSimObject>();
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
		
	/**@return The parent FunctionalSubmodel for a MappableVariable.*/
	public FunctionalSubmodel getParentFunctionalSubmodelForMappableVariable(MappableVariable var){
		
		if(var.getName().contains(".")){
			String compname = var.getName().substring(0, var.getName().lastIndexOf("."));
			
			return (FunctionalSubmodel) getSubmodel(compname);
		}
		return null;
	}
	
	/**
	 * @return A Map that links MatadataIDs with their associated model component.
	 */
	public Map<String, SemSimObject> getMetadataIDcomponentMap(){
		Map<String, SemSimObject> map = new HashMap<String, SemSimObject>();
		for(SemSimObject ssc : getAllModelComponentsandCollections()){
			if(ssc.getMetadataID()!=null) map.put(ssc.getMetadataID(), ssc);
		}
		return map;
	}
	
	/**
	 * @param ID A metadata ID used to look up a model component.
	 * @return The model component assigned the given ID, or null if the ID is not associated with any
	 * model component
	 */
	public SemSimObject getModelComponentByMetadataID(String ID){
		for(SemSimObject ssc : getAllModelComponentsandCollections()){
			if(ssc.getMetadataID().equals(ID))
				return ssc;
		}
		return null;
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
				if(pe.isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)){
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
			if(ent.isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)){
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
			if(ent.isType(SemSimTypes.REFERENCE_PHYSICAL_ENTITY)) refents.add((ReferencePhysicalEntity) ent);
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
		return set;
	}
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalEntity} that is annotated against the URI using the HAS_PHYSICAL_DEFINITION_RELATION.
	 * If no ReferencePhysicalEntities have been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalEntity getPhysicalEntityByReferenceURI(URI uri){
		for (PhysicalEntity pe : physicalentities) {
			if (pe.hasPhysicalDefinitionAnnotation()) {
				if (((ReferencePhysicalEntity)pe).getPhysicalDefinitionURI().toString().equals(uri.toString())) {
					return (ReferencePhysicalEntity)pe;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalProcess} that is annotated against the URI using the HAS_PHYSICAL_DEFINITION_RELATION.
	 * If no ReferencePhysicalProcess has been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalProcess getPhysicalProcessByReferenceURI(URI uri){
		for (PhysicalProcess pp : physicalprocesses) {
			
			if (pp.hasPhysicalDefinitionAnnotation()) {
				
				if (((ReferenceTerm)pp).getPhysicalDefinitionURI().equals(uri))
					return (ReferencePhysicalProcess)pp;
			}
		}
		return null;
	}
	
	/**
	 * @param uri A reference term URI
	 * @return The {@link ReferencePhysicalDependency} that is annotated against the URI using the HAS_PHYSICAL_DEFINITION_RELATION.
	 * If no ReferencePhysicalDependency has been annotated against the URI, null is returned.
	 */
	public ReferencePhysicalDependency getPhysicalDependencyByReferenceURI(URI uri){
		for(PhysicalDependency dep : physicaldependencies){
			
			if(dep.hasPhysicalDefinitionAnnotation()){
				
				if(((ReferenceTerm)dep).getPhysicalDefinitionURI().equals(uri))
					return (ReferencePhysicalDependency)dep;
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
			if(ent.isType(SemSimTypes.CUSTOM_PHYSICAL_ENTITY)) custs.add((CustomPhysicalEntity) ent);
		}
		
		return custs;
	}
	
	
	/**
	 * @param name The name of CustomPhysicalEntity to return
	 * @return The CustomPhysicalEntity with the specified name or null if no match was found.
	 */
	public CustomPhysicalEntity getCustomPhysicalEntityByName(String name){
		
		for(PhysicalEntity apmc : getPhysicalEntities()){
			
			if(!apmc.hasPhysicalDefinitionAnnotation()){
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
			
			if(!proc.hasPhysicalDefinitionAnnotation()) custs.add((CustomPhysicalProcess) proc);
		}
		return custs;
	}
	
	
	/**
	 * @param name The name of CustomPhysicalProcess to return
	 * @return The CustomPhysicalProcess with the specified name or null if no match was found.
	 */
	public CustomPhysicalProcess getCustomPhysicalProcessByName(String name){
		
		for(PhysicalProcess apmc : getPhysicalProcesses()){
			
			if(!apmc.hasPhysicalDefinitionAnnotation()){
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
		namespace = RDFNamespace.SEMSIM.getNamespaceasString().replace("#", "/" + sdf.format(new Date()).replace("-", "m").replace("+", "p") + "#");
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
	 * a URI with the HAS_PHYSICAL_DEFINITION_RELATION (as opposed to CustomPhysicalProcesses).
	 */
	public Set<ReferencePhysicalProcess> getReferencePhysicalProcesses(){
		Set<ReferencePhysicalProcess> refprocs = new HashSet<ReferencePhysicalProcess>();
		
		for(PhysicalProcess proc : getPhysicalProcesses()){
			
			if(proc.hasPhysicalDefinitionAnnotation()) refprocs.add((ReferencePhysicalProcess) proc);
		}
		return refprocs;
	}
	
	/**
	 * @return The location of the raw computer source code associated with this model.
	 */
	
	public ModelAccessor getLegacyCodeLocation() {
		return sourcefilelocation;
	}

	public void setSourceFileLocation(ModelAccessor sourcefilelocation) {
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
				if(fs != null) fs.removeVariableEquationFromMathML(mapv);
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
		super.removeSubmodel(submodel);
		
		// If the submodel is subsumed by another submodel, remove the subsumption
		for(Submodel sub : getSubmodels()) sub.removeSubmodel(submodel);
	}
	
	/**
	 * @return A clone of the model.
	 */
	public SemSimModel clone() {
        return new SemSimModel(this);
	}

	/**
	 * Specify which format was used for the model's simulation source code.
	 * See {@link ModelClassifier} constants. 
	 * @param originalModelType An integer corresponding to the format of the original model code (see {@link ModelClassifier} ).
	 */
	public void setSourceModelType(ModelType originalModelType) {
		this.sourceModelType = originalModelType;
	}

	
	/**
	 * @return An integer representing the format of the original model code (see {@link ModelClassifier} ) 
	 * and associated constants.
	 */
	public ModelType getSourceModelType() {
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
	public void addReferenceOntologyAnnotation(Relation relation, URI uri, String description, SemSimLibrary lib){
		addAnnotation(new ReferenceOntologyAnnotation(relation, uri, description, lib));
	}

	
	/**
	 * Get all SemSim {@link ReferenceOntologyAnnotation}s applied to an object
	 * that have a specific {@link SemSimRelation}.
	 * 
	 * @param relation The {@link SemSimRelation} that filters the annotations 
	 * to return  
	 */
	public Set<ReferenceOntologyAnnotation> getReferenceOntologyAnnotations(Relation relation) {
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
	
	public double getSemSimVersion() {
		return semsimversion;
	}

	public void setSemSimVersion(double semsimversion) {
		this.semsimversion = semsimversion;
	}
	
	public void setSemSimVersion(String semsimversion) {
		this.semsimversion = Double.valueOf(semsimversion);
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
				if (ds.getPhysicalProperty().equals(tobereplaced))
					ds.setAssociatedPhysicalProperty(toreplace);
			}
		}
	}

	//Required by Annotatable
	@Override
	public Boolean hasPhysicalDefinitionAnnotation() {
		return false;
	}
}