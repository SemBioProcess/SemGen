package semsim;

import java.io.File;
import java.io.FileNotFoundException;
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

import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;

//Class for holding reference terms and data required for SemGen - intended to replace SemSimConstants class
public class SemSimLibrary {
	public static final double SEMSIM_VERSION = 0.2;
	public static final IRI SEMSIM_VERSION_IRI = IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "SemSimVersion");
	
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public OWLOntology OPB;
	
	private Hashtable<String, String[]> OPBClassesForUnitsTable;
	private Hashtable<String, String[]> compositeAnnRelationsTable;
	private Hashtable<String, String[]> metadataRelationsTable;
	private Hashtable<String, String[]> jsimUnitsTable;
	
	private Set<String> jsimUnitPrefixesTable;
	private Set<String> cellMLUnitsTable;
	
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
			compositeAnnRelationsTable = ResourcesManager.createHashtableFromFile("cfg/structuralRelations.txt");
			metadataRelationsTable = ResourcesManager.createHashtableFromFile("cfg/metadataRelations.txt");
			jsimUnitsTable = ResourcesManager.createHashtableFromFile("cfg/jsimUnits");
			
			OPBClassesForUnitsTable = ResourcesManager.createHashtableFromFile("cfg/OPBClassesForUnits.txt");
			jsimUnitPrefixesTable = ResourcesManager.createSetFromFile("cfg/jsimUnitPrefixes");
			cellMLUnitsTable = ResourcesManager.createSetFromFile("cfg/CellMLUnits.txt");
		} catch (FileNotFoundException e3) {
			e3.printStackTrace();
		}
		

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
	
	public boolean isCellMLBaseUnit(String unit) {
		return cellMLUnitsTable.contains(unit);
	}
	
	/**
	 * @return The version of the SemSim API used to generate the model.
	 */
	public double getSemSimVersion() {
		return SEMSIM_VERSION;
	}
}
