package semsim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.annotation.workbench.routines.AutoAnnotate;
import semsim.annotation.Ontology;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.definitions.PropertyType;
import semsim.definitions.RDFNamespace;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.ResourcesManager;

/** Class for holding OPB reference properties, physical unit settings and other data required
 *  for reading and writing models**/
public class SemSimLibrary {
	public static final double SEMSIM_VERSION = 0.2;
	public static final IRI SEMSIM_VERSION_IRI = IRI.create(RDFNamespace.SEMSIM.getNamespaceAsString() + "SemSimVersion");

	public static final String DEFAULT_CFG_PATH = "/semsim/cfg_default/";
	public static final String SEMSIM_IN_JSIM_CONTROL_VALUE = "semSimAnnotate";
	
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public OWLOntology OPB;
	
	private HashMap<String, String[]> OPBClassesForUnitsTable;
	private HashMap<String, String[]> jsimUnitsTable;
	
	private ArrayList<Ontology> ontologies = new ArrayList<Ontology>();
	
	// Hashtable for mapping CellML base units to OPB classes.
	// Similar to OPBClassesForUnitsTable, but instead maps Hashtable of {baseunit:exponent} to OPB class.
    private HashMap<HashMap<String, Double>, String[]> OPBClassesForBaseUnitsTable;
	
	private Map<String, Integer> unitPrefixesAndPowersTable;
	private Set<String> cellMLUnitsTable;
	
	private HashMap<String, PhysicalPropertyInComposite> commonproperties = new HashMap<String, PhysicalPropertyInComposite>();
	private Set<String> OPBproperties = new HashSet<String>();
	private Set<String> OPBflowProperties = new HashSet<String>();
	private Set<String> OPBprocessProperties = new HashSet<String>();
	private Set<String> OPBamountProperties = new HashSet<String>();
	private Set<String> OPBforceProperties = new HashSet<String>();
	private Set<String> OPBstateProperties = new HashSet<String>();
	
	public static URI OPB_PHYSICAL_PROPERTY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00147");
	public static URI OPB_AMOUNT_PROPERTY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00135"); // TODO: this ID changed in in OPB version 1.05, on
	public static URI OPB_FORCE_PROPERTY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00574");
	public static URI OPB_DYNAMICAL_STATE_PROPERTY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00569");
	public static URI OPB_FLOW_RATE_PROPERTY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00573");
	public static URI OPB_TEMPORAL_RATE_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01151");
 	public static URI OPB_FLUID_VOLUME_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00154");
	public static URI OPB_AREA_OF_SPATIAL_ENTITY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00295");
	public static URI OPB_SPAN_OF_SPATIAL_ENTITY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01064");
	public static URI OPB_CHEMICAL_MOLAR_AMOUNT_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00425");
	public static URI OPB_CHEMICAL_CONCENTRATION_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00340");
	public static URI OPB_PARTICLE_COUNT_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01001");
	public static URI OPB_PARTICLE_CONCENTRATION_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01000");
	public static URI OPB_MASS_OF_SOLID_ENTITY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01226");
	public static URI OPB_MASS_LINEAL_DENSITY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00190");
	public static URI OPB_MASS_AREAL_DENSITY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00258");
	public static URI OPB_MASS_VOLUMETRIC_DENSITY_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00101");
	public static URI OPB_STATE_FRACTION_OF_CHEMICAL_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01298");
	public static URI OPB_CHEMICAL_MOLAR_FLOW_RATE_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00592");
	public static URI OPB_PARTICLE_FLOW_RATE_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_00544");
	public static URI OPB_MATERIAL_FLOW_RATE_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01220");
	public static URI OPB_TIME_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01023");
	public static URI OPB_DERIVATIVE_CONSTRAINT_URI = URI.create(RDFNamespace.OPB.getNamespaceAsString() + "OPB_01180");
	
	private String cfgpath;
	
	/**
	 * Use this constructor to load library from the default, read-only configuration folder 
	 * provided within the SemSim Java package
	 */
	public SemSimLibrary(){
		loadLibrary(false);
	}
	
	/**
	 * Use this constructor to load library info from a locally-stored, writable
	 * configuration directory
	 * @param pathToConfigFolder Path to configuration folder
	 */
	public SemSimLibrary(String pathToConfigFolder) {
		cfgpath = pathToConfigFolder;
		loadLibrary(true);
	}
	
	/**
	 * Load the library from standalone files.
	 * @param loadFromExternalDir Whether to load from an external directory
	 * or from within the Java package
	 */
	private void loadLibrary(boolean loadFromExternalDir) {
		try {
			loadCommonProperties();
			
			if(loadFromExternalDir){
				jsimUnitsTable = ResourcesManager.createHashMapFromFile(cfgpath + "jsimUnits", true);
				OPBClassesForUnitsTable = ResourcesManager.createHashMapFromFile(cfgpath + "OPBClassesForUnits.txt", true);
	            OPBClassesForBaseUnitsTable = ResourcesManager.createHashMapFromBaseUnitFile(cfgpath + "OPBClassesForBaseUnits.txt");
				unitPrefixesAndPowersTable = makeUnitPrefixesAndPowersTable();
				cellMLUnitsTable = ResourcesManager.createSetFromFile(cfgpath + "CellMLUnits.txt");
				loadOntologyDescriptions(cfgpath);
			}
			else{
				jsimUnitsTable = ResourcesManager.createHashMapFromResource(DEFAULT_CFG_PATH + "jsimUnits", true);
				OPBClassesForUnitsTable = ResourcesManager.createHashMapFromResource(DEFAULT_CFG_PATH + "OPBClassesForUnits.txt", true);
	            OPBClassesForBaseUnitsTable = ResourcesManager.createHashMapFromBaseUnitFile(DEFAULT_CFG_PATH + "OPBClassesForBaseUnits.txt");
				unitPrefixesAndPowersTable = makeUnitPrefixesAndPowersTable();
				cellMLUnitsTable = ResourcesManager.createSetFromResource(DEFAULT_CFG_PATH + "CellMLUnits.txt");
				loadOntologyDescriptions(DEFAULT_CFG_PATH);
			}
		} catch (FileNotFoundException e3) {
			e3.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		// Load the local copy of the OPB and the SemSim base ontology, and other config files into memory
		try {
			InputStream opbin = getClass().getResourceAsStream("/semsim/owl/OPBv1.04.owl");
			OPB = manager.loadOntologyFromOntologyDocument(opbin);
			InputStream ssbin = getClass().getResourceAsStream("/semsim/owl/SemSimBase.owl");			
			manager.loadOntologyFromOntologyDocument(ssbin);
		} catch (OWLOntologyCreationException e3) {
			e3.printStackTrace();
		}

		try {

			OPBproperties = SemSimOWLFactory.getAllSubclasses(OPB, OPB_PHYSICAL_PROPERTY_URI.toString(), false);
			OPBflowProperties = SemSimOWLFactory.getAllSubclasses(OPB, OPB_FLOW_RATE_PROPERTY_URI.toString(), false);
			OPBprocessProperties = SemSimOWLFactory.getAllSubclasses(OPB, OPB_TEMPORAL_RATE_URI.toString(), false);
			//OPBdynamicalProperties = SemSimOWLFactory.getAllSubclasses(OPB, RDFNamespace.OPB.getNamespace() + "OPB_00568", false);

			OPBamountProperties = SemSimOWLFactory.getAllSubclasses(OPB, OPB_AMOUNT_PROPERTY_URI.toString(), false);
			OPBforceProperties = SemSimOWLFactory.getAllSubclasses(OPB, OPB_FORCE_PROPERTY_URI.toString(), false);
			OPBstateProperties = SemSimOWLFactory.getAllSubclasses(OPB, OPB_DYNAMICAL_STATE_PROPERTY_URI.toString(), false);
		} catch (OWLException e2) {e2.printStackTrace();}
	}
	
	/**
	 * Load OPB properties that are commonly represented in models
	 * @throws IOException
	 */
	private void loadCommonProperties() throws IOException {
		HashMap<String, String[]> ptable = ResourcesManager.createHashMapFromResource("/semsim/cfg_default/CommonProperties.txt", true);
		for (String s : ptable.keySet()) {
			this.commonproperties.put(s, new PhysicalPropertyInComposite(ptable.get(s)[0], URI.create(s)));
		}
	}
	
	/**
	 * Load textual descriptions of ontologies used for annotation
	 * @param pathtodescriptions Path to file containing ontology descriptions
	 * @throws IOException
	 */
	//Map ontology namespaces to ontologies
	private void loadOntologyDescriptions(String pathtodescriptions) throws IOException {
		HashSet<Ontology> onts = ResourcesManager.loadOntologyDescriptions(pathtodescriptions);
		for (Ontology ont : onts) {
			ontologies.add(ont);
		}
	}
	
	/** @return The set of OPB properties commonly used in annotation */
	public Set<PhysicalPropertyInComposite> getCommonProperties() {
		return new HashSet<PhysicalPropertyInComposite>(commonproperties.values());
	}
	
	/**
	 * Look up OPB terms associated with a particular unit
	 * @param unit A unit name
	 * @return String array of OPB properties that could be quantified using
	 * the given unit
	 */
	public String[] getOPBUnitRefTerm(String unit) {
		return OPBClassesForUnitsTable.get(unit);
	}
	
	/**
	 * Get OPB properties associated with the units on a given {@link DataStructure}
	 * @param ds A data structure
	 * @return String array of candidate OPB properties that could be quantified using
	 * the units on the {@link DataStructure}
	 */
	public String[] getOPBBaseUnitRefTerms(DataStructure ds) {
		UnitOfMeasurement uom = ds.getUnit();
		String unitName = uom.getName();
		HashMap<String, Double> baseUnits = new HashMap<String, Double>();
		Set<UnitFactor> unitfactors= new HashSet<UnitFactor>();
		unitfactors = AutoAnnotate.fundamentalBaseUnits.get(unitName); // Recursively processed base units
		for(UnitFactor unitfactor : unitfactors) {
			String baseUnit = unitfactor.getBaseUnit().getName();
			Double exponent = unitfactor.getExponent();
			baseUnits.put(baseUnit, exponent);
		}
		
		// Some units do not have unit factors. Instead its name is a base unit.
		if(unitfactors.isEmpty() && isCellMLBaseUnit(unitName)) {
			baseUnits.put(unitName, 1.0);
		}

		if(OPBClassesForBaseUnitsTable.containsKey(baseUnits)) {
			return OPBClassesForBaseUnitsTable.get(baseUnits);
		}
		return null;
	}
	
	/**
	 * @param unit Unit name
	 * @return Whether the unit is prefixable within JSim
	 */
	public boolean isJSimUnitPrefixable(String unit) {
		return (jsimUnitsTable.get(unit)[0]).equals("true");
	}
	
	/**
	 * @param unit Unit name
	 * @return Whether the unit is in the list of fundamental JSim units
	 */
	public boolean jsimHasUnit(String unit) {
		return jsimUnitsTable.containsKey(unit);
	}

	/**
	 * Creates a {@link PhysicalPropertyInComposite} instance annotated against an OPB term
	 * based on the given {@link DataStructure}'s physical units.
	 * @param ds A data structure
	 * @return A {@link PhysicalPropertyInComposite} instance annotated against an OPB term
	 * based on the given {@link DataStructure}'s physical units.
	 */
	public PhysicalPropertyInComposite getOPBAnnotationFromPhysicalUnit(DataStructure ds){
		String[] candidateOPBclasses = getOPBUnitRefTerm(ds.getUnit().getName());
		// If there is no OPB class, check base units.
		if (candidateOPBclasses == null) {
			candidateOPBclasses = getOPBBaseUnitRefTerms(ds);
		}
		PhysicalPropertyInComposite pp = null;
		if (candidateOPBclasses != null && candidateOPBclasses.length == 1) {
			String term = RDFNamespace.OPB.getNamespaceAsString() + candidateOPBclasses[0];
			pp = commonproperties.get(term);
			if (pp==null) {
				OWLClass cls = SemSimOWLFactory.factory.getOWLClass(IRI.create(term));
				pp = new PhysicalPropertyInComposite(SemSimOWLFactory.getRDFLabels(OPB, cls)[0], URI.create(term));
			}
		}
		return pp;
	}
	
	/** Returns a {@link PhysicalPropertyInComposite} instance corresponding to an input OPB class URI
	 * @param id OPB class URI
	 * @return A {@link PhysicalPropertyInComposite} instance corresponding to the input OPB class URI
	 */
	public PhysicalPropertyInComposite getOPBAnnotationFromReferenceID(String id){
		return commonproperties.get(id);
	}
	
	/** Read an OWL ontology into a new OWLOntology instance. If ontology is already loaded,
	 * return the existing instance.
	 * @param onturi URI of an OWL ontology
	 * @return OWLOntology instance read in from specified URI
	 * @throws OWLOntologyCreationException
	 */
	public OWLOntology getLoadedOntology(String onturi) throws OWLOntologyCreationException {
		OWLOntology localont = SemSimOWLFactory.getOntologyIfPreviouslyLoaded(IRI.create(onturi), manager);
		if (localont == null) {
			localont = manager.loadOntologyFromOntologyDocument(IRI.create(onturi));
		}
		return localont;
	}
	
	/** Get all subclasses of a given OPB class.
	 * @param parentclass String of an OPB class URI
	 * @return All subclasses of the parent class
	 * @throws OWLException
	 */
	public Set<String> getOPBsubclasses(String parentclass) throws OWLException {
		Set<String> subclassset = SemSimOWLFactory.getAllSubclasses(OPB, RDFNamespace.OPB.getNamespaceAsString() + parentclass, false);
		return subclassset;
	}
	
	/** @return A new OWLDataFactory object */
	public OWLDataFactory makeOWLFactory() {
		return manager.getOWLDataFactory() ;
	}
	
	/**
	 * @param s A URI string 
	 * @return Whether the URI string corresponds to an OPB physical property class
	 */
	public boolean OPBhasProperty(String s) {
		return OPBproperties.contains(s);
	}
	
	/** 
	 * @param roa A {@link ReferenceOntologyAnnotation}
	 * @return Whether the URI referred to in the {@link ReferenceOntologyAnnotation}
	 * matches the URI of any OPB flow property class
	 */
	public boolean OPBhasFlowProperty(ReferenceOntologyAnnotation roa) {
		return OPBflowProperties.contains(roa.getReferenceURI().toString());
	}
	
	/**
	 * @param s A string
	 * @return Whether the string matches the URI of any OPB flow property class
	 */
	public boolean OPBhasFlowProperty(String s) {
		return OPBflowProperties.contains(s);
	}
	
	/**
	 * @param u A URI
	 * @return Whether the URI matches the URI of any OPB flow property class
	 */
	public boolean OPBhasFlowProperty(URI u) {
		return OPBflowProperties.contains(u.toString());
	}
	
	/**
	 * @param roa A {@link ReferenceOntologyAnnotation}
	 * @return Whether the reference URI in the annotation matches any OPB force property class
	 */
	public boolean OPBhasForceProperty(ReferenceOntologyAnnotation roa) {
		return OPBforceProperties.contains(roa.getReferenceURI().toString());
	}
	
	/**
	 * @param uri A URI
	 * @return Whether the URI matches any OPB force property class
	 */
	public boolean OPBhasForceProperty(URI uri) {
		return OPBforceProperties.contains(uri.toString());
	}
	
	/**
	 * @param uri A URI
	 * @return Whether the URI matches any OPB amount property class
	 */
	public boolean OPBhasAmountProperty(URI uri) {
		return OPBamountProperties.contains(uri.toString());
	}
	
	/**
	 * @param roa A {@link ReferenceOntologyAnnotation}
	 * @return Whether the referenced URI in the annotation matches any OPB state property class
	 */
	public boolean OPBhasStateProperty(ReferenceOntologyAnnotation roa) {
		return OPBstateProperties.contains(roa.getReferenceURI().toString());
	}
	
	/**
	 * @param uri A URI
	 * @return Whether the URI matches any OPB state property class
	 */
	public boolean OPBhasStateProperty(URI uri) {
		return OPBstateProperties.contains(uri);
	}
	
	/**
	 * @param roa A {@link ReferenceOntologyAnnotation}
	 * @return Whether the reference URI in the annotation matches any OPB process property class
	 */
	public boolean OPBhasProcessProperty(ReferenceOntologyAnnotation roa) {
		return OPBprocessProperties.contains(roa.getReferenceURI().toString());
	}
	
	/**
	 * @param s A String
	 * @return Whether the string matches the URI of any OPB process property class
	 */
	public boolean OPBhasProcessProperty(String s) {
		return OPBprocessProperties.contains(s);
	}
	
	/**
	 * @param u A URI
	 * @return Whether the URI matches any OPB process property class
	 */
	public boolean OPBhasProcessProperty(URI u) {
		return OPBprocessProperties.contains(u.toString());
	}
	
	/**
	 * @param u A URI
	 * @return Whether the URI matches an OPB property that is applicable 
	 * for use in annotating a dynamical rate variable
	 */
	public boolean isOPBprocessProperty(URI u){
		return (OPBhasFlowProperty(u) || OPBhasProcessProperty(u));
	}
	
	
	/**
	 * @param u A URI
	 * @return Whether the URI matches an OPB property that is applicable 
	 * for use in annotating a dynamical force variable
	 */
	public boolean isOPBforceProperty(URI u){
		return OPBhasForceProperty(u);
	}
	
	
	/**
	 * @param prop A {@link PhysicalPropertyInComposite} instance
	 * @param pmc A {@link PhysicalModelComponent} instance
	 * @return Whether the {@link PhysicalPropertyInComposite} is a valid property of the {@link PhysicalModelComponent}.
	 * For example, a {@link PhysicalModelComponent} that is a {@link PhysicalProcess} should only bear
	 * OPB process properties.
	 */
	public boolean checkOPBpropertyValidity(PhysicalPropertyInComposite prop, PhysicalModelComponent pmc){
		if(pmc!=null){
			
			Boolean URIisprocessproperty = isOPBprocessProperty(prop.getPhysicalDefinitionURI());
			// This conditional statement makes sure that physical processes are annotated with appropriate OPB terms
			// It only limits physical entity properties to non-process properties. It does not limit based on whether
			// the OPB term is for a constitutive property. Not sure if it should, yet.
			if((pmc instanceof PhysicalEntity && URIisprocessproperty)
					|| (pmc instanceof PhysicalProcess && !URIisprocessproperty)){
			
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param pp A {@link PhysicalPropertyInComposite} instance
	 * @return The type of physical property represented by the {@link PhysicalPropertyInComposite} instance
	 */
	public PropertyType getPropertyinCompositeType(PhysicalPropertyInComposite pp) {
		URI roa = (pp.getPhysicalDefinitionURI());
		
		if(OPBhasStateProperty(roa) || OPBhasAmountProperty(roa)){
			return PropertyType.PropertyOfPhysicalEntity;
		}
		else if(OPBhasFlowProperty(roa) || OPBhasProcessProperty(roa)){
			return PropertyType.PropertyOfPhysicalProcess;
		}
		else if(OPBhasForceProperty(roa)){
			return PropertyType.PropertyOfPhysicalForce;
		}
		else return PropertyType.Unknown;
	}
	
	/**
	 * @param unit A unit name
	 * @return Whether the name is in the list of CellML base units
	 */
	public boolean isCellMLBaseUnit(String unit) {
		return cellMLUnitsTable.contains(unit);
	}
	
	/**
	 * Remove any OPB terms that are not physical properties from a HashMap that links 
	 * OPB RDF labels to URI strings
	 * @param table HashMap linking OPB RDF labels to URI Strings
	 * @return HashMap with non-physical properties removed
	 */
	public HashMap<String,String> removeNonPropertiesFromOPB(HashMap<String, String> table){
		HashMap<String,String> newtable = new HashMap<String,String>();
		for(String key : table.keySet()){
			if(OPBhasProperty(table.get(key)))
				newtable.put(key, table.get(key));
		}
		return newtable;
	}
	
	

	/**
	 * Remove any OPB terms that are not physical process properties from a HashMap that links 
	 * OPB RDF labels to URI strings
	 * @param table HashMap linking OPB RDF labels to their URI Strings
	 * @return HashMap with non-process properties removed
	 */
	public HashMap<String, String> removeNonProcessProperties(HashMap<String, String> table) {
		HashMap<String,String> newtable = new HashMap<String,String>();
		for(String key : table.keySet()){
			if(OPBhasFlowProperty(table.get(key)) || OPBhasProcessProperty(table.get(key)))
				newtable.put(key, table.get(key));
		}
		return newtable;
	}
	
	/**
	 * Remove any OPB terms that are not physical entity properties from a HashMap that links 
	 * OPB RDF labels to URI strings
	 * @param table HashMap linking OPB RDF labels and URI Strings
	 * @return HashMap with non-properties of entities removed
	 */
	public HashMap<String, String> removeNonPropertiesofEntities(HashMap<String, String> table) {
		HashMap<String,String> newtable = new HashMap<String,String>();
		for(String key : table.keySet()){
			if(OPBhasFlowProperty(table.get(key)) || OPBhasProcessProperty(table.get(key)))
				newtable.put(key, table.get(key));
		}
		return newtable;
	}
	
	/**
	 * @param uri A class URI from an ontology
	 * @return The name of the ontology containing the class
	 */
	public String getReferenceOntologyName(URI uri) {
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());
		return getOntologyByNamespace(namespace).getFullName();
	}
	
	/**
	 * @param uri A class URI from an ontology
	 * @return The abbreviation of the ontology containing the class
	 */
	public String getReferenceOntologyAbbreviation(URI uri) {
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());
		
		return getOntologyByNamespace(namespace).getNickName();
	}
	
	/**
	 * @param namespace The namespace of an ontology
	 * @return The {@link Ontology} identified by the namespace
	 */
	private Ontology getOntologyByNamespace(String namespace) {
		Ontology ont = ReferenceOntologies.getOntologyByNamespace(namespace);
		
		if (ont == ReferenceOntology.UNKNOWN.getAsOntology()) {
			for (Ontology o : ontologies) {
				if (o.hasNamespace(namespace))
					return o;
			}
		}
		if (ont == null) ont = ReferenceOntology.UNKNOWN.getAsOntology();
		
		return ont;
	}
	
	/**
	 * @param termuri The URI of an ontology class
	 * @return The {@link Ontology} containing the class
	 */
	public Ontology getOntologyfromTermURI(String termuri) {
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(termuri);
		return getOntologyByNamespace(namespace);
	}
	
	/**
	 * @param uri The URI of an ontology class
	 * @return The URI with its namespace removed
	 */
	public String getReferenceID(URI uri) {
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());
		return uri.toString().replace(namespace, "");
	}
	
	/**
	 * @return A map that links unit prefix strings with the exponent value used in unit conversions
	 */
	public Map<String, Integer> makeUnitPrefixesAndPowersTable(){
		Map<String,Integer> map = new HashMap<String,Integer>();
		map.put("yotta", 24);
		map.put("zetta", 21);	
		map.put("exa", 18);	
		map.put("peta", 15); 	
		map.put("tera", 12); 	
		map.put("giga", 9); 	
		map.put("mega", 6); 	
		map.put("kilo", 3); 	
		map.put("hecto", 2); 	
		map.put("deka", 1); 	
		map.put("deca", 1); 	
		map.put("deci", -1);
		map.put("centi", -2);
		map.put("milli", -3);
		map.put("micro", -6);
		map.put("nano", -9);
		map.put("pico", -12);
		map.put("femto", -15);
		map.put("atto", -18);
		map.put("zepto", -21);
		map.put("yocto", -24);
		return map;
	}
	
	/** @return Get the unit prefixes and exponents map */
	public Map<String,Integer> getUnitPrefixesAndPowersMap(){
		return unitPrefixesAndPowersTable;
	}
	
	/**
	 * @return The version of the SemSim API used to generate the model.
	 */
	public double getSemSimVersion() {
		return SEMSIM_VERSION;
	}
}
