package semsim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semgen.SemGen;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;

//Class for holding reference terms and data required for SemGen - intended to replace SemSimConstants class
public class SemSimLibrary {
	public static File ontologyTermsAndNamesCacheFile = new File("cfg/ontologyTermsAndNamesCache.txt");
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public OWLOntology OPB;
	
	private Hashtable<String, String[]> OPBClassesForUnitsTable = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> compositeAnnRelationsTableLR = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> compositeAnnRelationsTableRL = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> metadataRelationsTable = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> ontologyTermsAndNamesCache = new Hashtable<String,String[]>();
	private Hashtable<String, String[]> jsimUnitsTable;
	private Hashtable<String, String[]> jsimUnitPrefixesTable;
	
	private Hashtable<Hashtable<String, Double>, String[]> OPBClassesForBaseUnitsTable = new Hashtable<Hashtable<String, Double>, String[]>(); // Similar to OPBClassesForUnitsTable, but instead maps Hashtable of {baseunit:exponent} to OPB class.
	
	private static Set<String> OPBproperties = new HashSet<String>();
	private static Set<String> OPBflowProperties = new HashSet<String>();
	private static Set<String> OPBprocessProperties = new HashSet<String>();
	private static Set<String> OPBdynamicalProperties = new HashSet<String>();
	private static Set<String> OPBamountProperties = new HashSet<String>();
	private static Set<String> OPBforceProperties = new HashSet<String>();
	private static Set<String> OPBstateProperties = new HashSet<String>();
	
	public SemSimLibrary() {
		loadLibrary();
	}
	
	private void loadLibrary() {
		try {
			compositeAnnRelationsTableLR = ResourcesManager.createHashtableFromFile("cfg/structuralRelationsLR.txt");
			compositeAnnRelationsTableRL = ResourcesManager.createHashtableFromFile("cfg/structuralRelationsRL.txt");
			metadataRelationsTable = ResourcesManager.createHashtableFromFile("cfg/metadataRelations.txt");
			ontologyTermsAndNamesCache = ResourcesManager.createHashtableFromFile("cfg/ontologyTermsAndNamesCache.txt");
			jsimUnitsTable = ResourcesManager.createHashtableFromFile("cfg/jsimUnits");
			jsimUnitPrefixesTable = ResourcesManager.createHashtableFromFile("cfg/jsimUnitPrefixes");
			OPBClassesForUnitsTable = ResourcesManager.createHashtableFromFile("cfg/OPBClassesForUnits.txt");
		} catch (FileNotFoundException e3) {e3.printStackTrace();}	
		

		// Load the local copy of the OPB and the SemSim base ontology, and other config files into memory
		try {
			OPB = manager.loadOntologyFromOntologyDocument(new File("cfg/OPB.970.owl"));
			manager.loadOntologyFromOntologyDocument(new File("cfg/SemSimBase.owl"));
		} catch (OWLOntologyCreationException e3) {
			e3.printStackTrace();
		}

		try {
			OPBproperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00147", false);
			OPBflowProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00573", false);
			OPBprocessProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB01151", false);
			OPBdynamicalProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00568", false);
			OPBamountProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00135", false);
			OPBforceProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00574", false);
			OPBstateProperties = SemSimOWLFactory.getAllSubclasses(OPB, SemSimConstants.OPB_NAMESPACE + "OPB_00569", false);
		} catch (OWLException e2) {e2.printStackTrace();}
	}
	
	public String[] getOPBUnitRefTerm(String unit) {
		return OPBClassesForUnitsTable.get(unit);
	}
	
	// Method for getting OPB classes based on base units
	public String[] getOPBBaseUnitRefTerms(DataStructure ds) {
		// For each ds, store its base units in Hashtable as baseunitname:exponent
		Hashtable<String, Double> baseUnits = new Hashtable<String, Double>();
		
		for(UnitFactor factor : ds.getUnit().getUnitFactors()){
			String baseunitname = factor.getBaseUnit().getName();
			Double exponent = factor.getExponent();
			baseUnits.put(baseunitname, exponent);
		}
		
		// Mapping table is embedded here for now, but should be separated out into a file soon.
		
		// Frequently occurring unit combinations. 
		Hashtable<String, Double> volt = new Hashtable<String, Double>() {{
			put("volt", 1.0);
		}};
		Hashtable<String, Double> chemicalconcentration = new Hashtable<String, Double>() {{
			put("mole", 1.0);
			put("litre", -1.0);
		}};
		Hashtable<String, Double> chemicalconcentration1 = new Hashtable<String, Double>() {{
			put("mole", 1.0);
			put("metre", -3.0);
		}};
		Hashtable<String, Double> flux = new Hashtable<String, Double>() {{
			put("mole", 1.0);
			put("litre", -1.0);
			put("second", -1.0);
		}};
		Hashtable<String, Double> area = new Hashtable<String, Double>() {{
			put("metre", 2.0);
		}};
		Hashtable<String, Double> area1 = new Hashtable<String, Double>() {{
			put("meter", 2.0);
		}};
		Hashtable<String, Double> current = new Hashtable<String, Double>() {{
			put("ampere", 1.0);
		}};
		Hashtable<String, Double> currentdensity = new Hashtable<String, Double>() {{
			put("ampere", 1.0);
			put("metre", -2.0);
		}};
		Hashtable<String, Double> volume = new Hashtable<String, Double>() {{
			put("litre", 1.0);
		}};
		Hashtable<String, Double> volume1 = new Hashtable<String, Double>() {{
			put("metre", 3.0);
		}};
		Hashtable<String, Double> flow = new Hashtable<String, Double>() {{
			put("litre", 1.0);
			put("second", -1.0);
		}};
		Hashtable<String, Double> pressure = new Hashtable<String, Double>() {{
			put("newton", 1.0);
			put("metre", -2.0);
		}};
		Hashtable<String, Double> pressure1 = new Hashtable<String, Double>() {{
			put("pascal", 1.0);
		}};
		Hashtable<String, Double> amount = new Hashtable<String, Double>() {{
			put("mole", 1.0);
		}};
		Hashtable<String, Double> mass = new Hashtable<String, Double>() {{
			put("kilogram", 1.0);
		}};
		Hashtable<String, Double> mass1 = new Hashtable<String, Double>() {{
			put("gram", 1.0);
		}};
		Hashtable<String, Double> temperature = new Hashtable<String, Double>() {{
			put("celsius", 1.0);
		}};
		Hashtable<String, Double> temperature1 = new Hashtable<String, Double>() {{
			put("kelvin", 1.0);
		}};
		Hashtable<String, Double> charge = new Hashtable<String, Double>() {{
			put("coulomb", 1.0);
		}};
		Hashtable<String, Double> frequency = new Hashtable<String, Double>() {{
			put("hertz", 1.0);
		}};
		Hashtable<String, Double> length = new Hashtable<String, Double>() {{
			put("metre", 1.0);
		}};
		Hashtable<String, Double> length1 = new Hashtable<String, Double>() {{
			put("meter", 1.0);
		}};
		
		// OPB classes
		String[] opb_electricalpotential = {"OPB_00506"};
		String[] opb_chemicalconcentration = {"OPB_00340"};
		String[] opb_flux = {"OPB_00593"};
		String[] opb_area = {"OPB_00295"};
		String[] opb_volume = {"OPB_00154"};
		String[] opb_current = {"OPB_00318"};
		String[] opb_flow = {"OPB_00299"};
		String[] opb_pressure = {"OPB_00509"};
		String[] opb_amount = {"OPB_00425"};
		String[] opb_mass = {"OPB_01319"};
		String[] opb_temperature = {"OPB_00165"};
		String[] opb_charge = {"OPB_00411"};
		String[] opb_frequency = {"OPB_00045"};
		String[] opb_length = {"OPB_00451"};
		
		// Hashtable of Hashtables of base unit combinations and their corresponding OPB classes
		OPBClassesForBaseUnitsTable.put(volt, opb_electricalpotential);
		OPBClassesForBaseUnitsTable.put(chemicalconcentration, opb_chemicalconcentration);
		OPBClassesForBaseUnitsTable.put(chemicalconcentration1, opb_chemicalconcentration);
		OPBClassesForBaseUnitsTable.put(flux, opb_flux);
		OPBClassesForBaseUnitsTable.put(area, opb_area);
		OPBClassesForBaseUnitsTable.put(area1, opb_area);
		OPBClassesForBaseUnitsTable.put(current, opb_current);
		OPBClassesForBaseUnitsTable.put(currentdensity, opb_current);
		OPBClassesForBaseUnitsTable.put(volume, opb_volume);
		OPBClassesForBaseUnitsTable.put(volume1, opb_volume);
		OPBClassesForBaseUnitsTable.put(flow, opb_flow);
		OPBClassesForBaseUnitsTable.put(pressure, opb_pressure);
		OPBClassesForBaseUnitsTable.put(pressure1, opb_pressure);
		OPBClassesForBaseUnitsTable.put(amount, opb_amount);
		OPBClassesForBaseUnitsTable.put(mass, opb_mass);
		OPBClassesForBaseUnitsTable.put(mass1, opb_mass);
		OPBClassesForBaseUnitsTable.put(temperature, opb_temperature);
		OPBClassesForBaseUnitsTable.put(temperature1, opb_temperature);
		OPBClassesForBaseUnitsTable.put(charge, opb_charge);
		OPBClassesForBaseUnitsTable.put(frequency, opb_frequency);
		OPBClassesForBaseUnitsTable.put(length, opb_length);
		OPBClassesForBaseUnitsTable.put(length1, opb_length);
		
		if(OPBClassesForBaseUnitsTable.containsKey(baseUnits)){
			System.out.println(ds.getName() + " FROM BASE UNITS"); // For testing purposes. To be deleted later.
			return OPBClassesForBaseUnitsTable.get(baseUnits);
		}
		return null;
	}
	
	public Hashtable<String, String[]> getOntTermsandNamesCache() {
		return ontologyTermsAndNamesCache;
	}
	
	public String[] getListofMetaDataRelations() {
		return metadataRelationsTable.keySet().toArray(new String[]{});
	}
	
	public ReferenceOntologyAnnotation getOPBAnnotationFromPhysicalUnit(DataStructure ds){
		ReferenceOntologyAnnotation roa = null;
		String[] candidateOPBclasses = getOPBUnitRefTerm(ds.getUnit().getName());
		// If there is no OPB class, checkbase units.
		if (candidateOPBclasses == null) {
			candidateOPBclasses = getOPBBaseUnitRefTerms(ds);
		}
		if (candidateOPBclasses != null && candidateOPBclasses.length == 1) {
			OWLClass cls = SemSimOWLFactory.factory.getOWLClass(IRI.create(SemSimConstants.OPB_NAMESPACE + candidateOPBclasses[0]));
			String OPBpropname = SemSimOWLFactory.getRDFLabels(OPB, cls)[0];
			roa = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, cls.getIRI().toURI(), OPBpropname);
		}
		return roa;
	}
	
	public void storeCachedOntologyTerms(){
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(ontologyTermsAndNamesCacheFile));
			for(String key : ontologyTermsAndNamesCache.keySet()){
				writer.println(key + "; " + ontologyTermsAndNamesCache.get(key)[0]);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	public boolean checkOPBpropertyValidity(PhysicalProperty prop, URI OPBuri){
		if(prop.getPhysicalPropertyOf()!=null){
			
			// This conditional statement makes sure that physical processes are annotated with appropriate OPB terms
			// It only limits physical entity properties to non-process properties. It does not limit based on whether
			// the OPB term is for a constitutive property. Not sure if it should, yet.
			if((prop.getPhysicalPropertyOf() instanceof PhysicalEntity || prop.getPhysicalPropertyOf() instanceof PhysicalProcess ) && 
				(OPBhasFlowProperty(OPBuri) || OPBhasProcessProperty(OPBuri))) {
				return false;
			}
		}
		return true;
	}
}
