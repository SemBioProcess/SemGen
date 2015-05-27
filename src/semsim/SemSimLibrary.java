package semsim;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.ResourcesManager;

//Class for holding reference terms and data required for SemGen - intended to replace SemSimConstants class
public class SemSimLibrary {
	public static final double SEMSIM_VERSION = 0.2;
	public static final IRI SEMSIM_VERSION_IRI = IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "SemSimVersion");
	
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public OWLOntology OPB;
	
	private HashMap<String, String[]> OPBClassesForUnitsTable;
	private HashMap<String, String[]> compositeAnnRelationsTable;
	private HashMap<String, String[]> metadataRelationsTable;
	private HashMap<String, String[]> jsimUnitsTable;
	
	// Hashtable for mapping CellML base units to OPB classes.
	// Similar to OPBClassesForUnitsTable, but instead maps Hashtable of {baseunit:exponent} to OPB class.
    private HashMap<HashMap<String, Double>, String[]> OPBClassesForBaseUnitsTable;
	
	private Set<String> jsimUnitPrefixesTable;
	private Set<String> cellMLUnitsTable;
	
	private HashMap<String, PhysicalProperty> commonproperties = new HashMap<String, PhysicalProperty>();
	private Set<String> OPBproperties = new HashSet<String>();
	private Set<String> OPBflowProperties = new HashSet<String>();
	private Set<String> OPBprocessProperties = new HashSet<String>();
	private Set<String> OPBdynamicalProperties = new HashSet<String>();
	private Set<String> OPBamountProperties = new HashSet<String>();
	private Set<String> OPBforceProperties = new HashSet<String>();
	private Set<String> OPBstateProperties = new HashSet<String>();
	
	public SemSimLibrary() {
		loadLibrary();
	}
	
	private void loadLibrary() {
		try {
			loadCommonProperties();
			compositeAnnRelationsTable = ResourcesManager.createHashMapFromFile("cfg/structuralRelations.txt");
			metadataRelationsTable = ResourcesManager.createHashMapFromFile("cfg/metadataRelations.txt");
			jsimUnitsTable = ResourcesManager.createHashMapFromFile("cfg/jsimUnits");
			
			OPBClassesForUnitsTable = ResourcesManager.createHashMapFromFile("cfg/OPBClassesForUnits.txt");
            OPBClassesForBaseUnitsTable = ResourcesManager.createHashMapFromBaseUnitFile("cfg/OPBClassesForBaseUnits.txt");
			jsimUnitPrefixesTable = ResourcesManager.createSetFromFile("cfg/jsimUnitPrefixes");
			cellMLUnitsTable = ResourcesManager.createSetFromFile("cfg/CellMLUnits.txt");
		} catch (FileNotFoundException e3) {
			e3.printStackTrace();
		}
		

		// Load the local copy of the OPB and the SemSim base ontology, and other config files into memory
		try {
			OPB = manager.loadOntologyFromOntologyDocument(new File("cfg/OPBv1.04.owl"));
			manager.loadOntologyFromOntologyDocument(new File("cfg/SemSimBase.owl"));
		} catch (OWLOntologyCreationException e3) {
			e3.printStackTrace();
		}

		try {
			OPBproperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00147", false);
			OPBflowProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00573", false);
			OPBprocessProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_01151", false);
			OPBdynamicalProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00568", false);
			OPBamountProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00135", false);
			OPBforceProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00574", false);
			OPBstateProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00569", false);
		} catch (OWLException e2) {e2.printStackTrace();}
	}
	
	private void loadCommonProperties() throws FileNotFoundException {
		HashMap<String, String[]> ptable = ResourcesManager.createHashMapFromFile("cfg/CommonProperties.txt");
		for (String s : ptable.keySet()) {
			this.commonproperties.put(s, new PhysicalProperty(ptable.get(s)[0], URI.create(s)));
		}
	}
	
	public Set<PhysicalProperty> getCommonProperties() {
		return new HashSet<PhysicalProperty>(commonproperties.values());
	}
	
	public String[] getOPBUnitRefTerm(String unit) {
		return OPBClassesForUnitsTable.get(unit);
	}
	
	public String[] getOPBBaseUnitRefTerms(DataStructure ds) {
		// For each ds, store its base units in Hashtable as baseunitname:exponent
		HashMap<String, Double> baseUnits = new HashMap<String, Double>();
		for(UnitFactor factor : ds.getUnit().getUnitFactors()) {
			String baseunitname = factor.getBaseUnit().getName();
			Double exponent = factor.getExponent();
			baseUnits.put(baseunitname, exponent);
		}
		if(OPBClassesForBaseUnitsTable.containsKey(baseUnits)) {
			return OPBClassesForBaseUnitsTable.get(baseUnits);
		}
		return null;
	}
	
	public boolean isJSimUnitPrefixable(String unit) {
		return (jsimUnitsTable.get(unit)[0]).equals("true");
	}
	
	public boolean jsimHasUnit(String unit) {
		return jsimUnitsTable.containsKey(unit);
	}
	
	public Set<String> getUnitPrefixes() {
		return jsimUnitPrefixesTable;
	}
	
	public String[] getListofMetaDataRelations() {
		return metadataRelationsTable.keySet().toArray(new String[]{});
	}
	
	public PhysicalProperty getOPBAnnotationFromPhysicalUnit(DataStructure ds){
		String[] candidateOPBclasses = getOPBUnitRefTerm(ds.getUnit().getName());
		// If there is no OPB class, checkbase units.
		if (candidateOPBclasses == null) {
			candidateOPBclasses = getOPBBaseUnitRefTerms(ds);
		}
		PhysicalProperty pp = null;
		if (candidateOPBclasses != null && candidateOPBclasses.length == 1) {
			String term = SemSimConstants.OPB_NAMESPACE + candidateOPBclasses[0];
			pp = commonproperties.get(term);
			if (pp==null) {
				OWLClass cls = SemSimOWLFactory.factory.getOWLClass(IRI.create(term));
				pp = new PhysicalProperty(SemSimOWLFactory.getRDFLabels(OPB, cls)[0], URI.create(term));
			}
		}
		return pp;
	}
	
	public PhysicalProperty getOPBAnnotationFromReferenceID(String id){
		return commonproperties.get(id);
	}
	
	public OWLOntology getLoadedOntology(String onturi) throws OWLOntologyCreationException {
		OWLOntology localont = SemSimOWLFactory.getOntologyIfPreviouslyLoaded(IRI.create(onturi), manager);
		if (localont == null) {
			localont = manager.loadOntologyFromOntologyDocument(IRI.create(onturi));
		}
		return localont;
	}
	
	public Set<String> getOPBsubclasses(String parentclass) throws OWLException {
		Set<String> subclassset = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + parentclass, false);
		return subclassset;
	}
	
	public OWLDataFactory makeOWLFactory() {
		return manager.getOWLDataFactory() ;
	}
	
	public boolean OPBhasProperty(String s) {
		return OPBproperties.contains(s);
	}
	
	public boolean OPBhasFlowProperty(ReferenceOntologyAnnotation roa) {
		return OPBflowProperties.contains(roa.getReferenceURI().toString());
	}
	
	public boolean OPBhasFlowProperty(String s) {
		return OPBflowProperties.contains(s);
	}
	
	public boolean OPBhasFlowProperty(URI u) {
		return OPBflowProperties.contains(u.toString());
	}
	
	public boolean OPBhasForceProperty(ReferenceOntologyAnnotation roa) {
		return OPBforceProperties.contains(roa.getReferenceURI().toString());
	}
	
	public boolean OPBhasAmountProperty(ReferenceOntologyAnnotation roa) {
		return OPBamountProperties.contains(roa.getReferenceURI().toString());
	}
	
	public boolean OPBhasStateProperty(ReferenceOntologyAnnotation roa) {
		return OPBstateProperties.contains(roa.getReferenceURI().toString());
	}
	
	public boolean OPBhasProcessProperty(ReferenceOntologyAnnotation roa) {
		return OPBprocessProperties.contains(roa.getReferenceURI().toString());
	}
	
	public boolean OPBhasProcessProperty(String s) {
		return OPBprocessProperties.contains(s);
	}
	
	public boolean OPBhasProcessProperty(URI u) {
		return OPBprocessProperties.contains(u.toString());
	}
	
	public boolean isOPBprocessProperty(URI u){
		return (OPBhasFlowProperty(u) || OPBhasProcessProperty(u));
	}
	
	public boolean checkOPBpropertyValidity(PhysicalProperty prop, PhysicalModelComponent pmc){
		if(pmc!=null){
			
			Boolean URIisprocessproperty = isOPBprocessProperty(prop.getReferstoURI());
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
	
	public boolean isCellMLBaseUnit(String unit) {
		return cellMLUnitsTable.contains(unit);
	}
	
	// Remove any OPB terms that are not Physical Properties
	public HashMap<String,String> removeNonPropertiesFromOPB(HashMap<String, String> table){
		HashMap<String,String> newtable = new HashMap<String,String>();
		for(String key : table.keySet()){
			if(OPBhasProperty(table.get(key)))
				newtable.put(key, table.get(key));
		}
		return newtable;
	}
	
	public String getReferenceOntologyAbbreviation(URI uri) {
		String ontologyAbbreviation = "?";
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());
		if(SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(namespace)){
			String fullname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(namespace);
			if(SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.containsKey(fullname)){
				ontologyAbbreviation = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(fullname);
			}
		}
		return ontologyAbbreviation;
	}
	
	/**
	 * @return The version of the SemSim API used to generate the model.
	 */
	public double getSemSimVersion() {
		return SEMSIM_VERSION;
	}
}
