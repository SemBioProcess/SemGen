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
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.DataStructure;
import semsim.owl.SemSimOWLFactory;

//Class for holding reference terms and data required for SemGen - intended to replace SemSimConstants class
public class SemSimLibrary {
<<<<<<< HEAD
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static File ontologyTermsAndNamesCacheFile = new File("cfg/ontologyTermsAndNamesCache.txt");
=======
	public static File ontologyTermsAndNamesCacheFile = new File("cfg/ontologyTermsAndNamesCache.txt");
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	public OWLOntology OPB;
	
	private Hashtable<String, String[]> OPBClassesForUnitsTable = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> compositeAnnRelationsTableLR = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> compositeAnnRelationsTableRL = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> metadataRelationsTable = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> ontologyTermsAndNamesCache = new Hashtable<String,String[]>();
<<<<<<< HEAD
	private Hashtable<String, Boolean> jsimUnitsTable = new Hashtable<String, Boolean>();
	private Hashtable<String, String[]> jsimUnitPrefixesTable = new Hashtable<String, String[]>();
=======
	private Hashtable<String, String[]> jsimUnitsTable;
	private Hashtable<String, String[]> jsimUnitPrefixesTable;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	
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
<<<<<<< HEAD
			buildJSimUnitsTable();
=======
			jsimUnitsTable = ResourcesManager.createHashtableFromFile("cfg/jsimUnits");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
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
	
<<<<<<< HEAD
	private void buildJSimUnitsTable() throws FileNotFoundException {
		Hashtable<String, String[]> table = ResourcesManager.createHashtableFromFile("cfg/jsimUnits");
		Boolean bool;
		for (String term : table.keySet()) {
			bool = table.get(term)[0].equals("true") ? true : false;
			jsimUnitsTable.put(term, bool);
		}
	}
	
=======
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	public String[] getOPBUnitRefTerm(String unit) {
		return OPBClassesForUnitsTable.get(unit);
	}
	
	public Hashtable<String, String[]> getOntTermsandNamesCache() {
		return ontologyTermsAndNamesCache;
	}
	
	public String[] getListofMetaDataRelations() {
		return metadataRelationsTable.keySet().toArray(new String[]{});
	}
	
<<<<<<< HEAD
	public Set<String> getAllJSimUnits() {
		return jsimUnitsTable.keySet();
	}
	public boolean jsimHasUnit(String unit) {
		return jsimUnitsTable.containsKey(unit);
	}
	
	public boolean jsimUnitHasPrefix(String unit) {
		return jsimUnitsTable.get(unit);
	}
	
=======
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	public ReferenceOntologyAnnotation getOPBAnnotationFromPhysicalUnit(DataStructure ds){
		ReferenceOntologyAnnotation roa = null;
		String[] candidateOPBclasses = getOPBUnitRefTerm(ds.getUnit().getName());
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
	
	public boolean OPBhasProperty(String s) {
		return OPBproperties.contains(s);
	}
	
	public boolean OPBhasFlowProperty(ReferenceOntologyAnnotation roa) {
		return OPBflowProperties.contains(roa);
	}
	
	public boolean OPBhasFlowProperty(String s) {
		return OPBflowProperties.contains(s);
	}
	
	public boolean OPBhasFlowProperty(URI u) {
		return OPBflowProperties.contains(u);
	}
	
	public boolean OPBhasForceProperty(ReferenceOntologyAnnotation roa) {
		return OPBforceProperties.contains(roa);
	}
	
	public boolean OPBhasAmountProperty(ReferenceOntologyAnnotation roa) {
		return OPBamountProperties.contains(roa);
	}
	
	public boolean OPBhasStateProperty(ReferenceOntologyAnnotation roa) {
		return OPBstateProperties.contains(roa);
	}
	
	public boolean OPBhasProcessProperty(ReferenceOntologyAnnotation roa) {
		return OPBprocessProperties.contains(roa);
	}
	
	public boolean OPBhasProcessProperty(String s) {
		return OPBprocessProperties.contains(s);
	}
	
	public boolean OPBhasProcessProperty(URI u) {
		return OPBprocessProperties.contains(u);
	}
}
