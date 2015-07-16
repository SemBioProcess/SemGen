package semsim.utilities;

import java.util.ArrayList;

public class ReferenceOntologies {
	public static ArrayList<ReferenceOntology> getAllOntologies() {
		ArrayList<ReferenceOntology> allont = new ArrayList<ReferenceOntology>();
		for (ReferenceOntology ont : ReferenceOntology.values()) {
			allont.add(ont);
		}
		return allont;
	}
	
	public static ReferenceOntology getReferenceOntologybyFullName(String name) {
		for (ReferenceOntology ro : ReferenceOntology.values()) {
			if (ro.getFullName().equals(name)) return ro; 
		}
		return null;
	}
	
	public static ReferenceOntology getReferenceOntologybyNamespace(String namespace) {
		for (ReferenceOntology ro : ReferenceOntology.values()) {
			if (ro.hasNamespace(namespace)) return ro;
		}
		return null;
	}
	
	public enum OntologyDomain {
		AssociatePhysicalProperty(new ReferenceOntology[]{ReferenceOntology.OPB}),
		PhysicalProperty(new ReferenceOntology[]{ReferenceOntology.CMO, ReferenceOntology.OPB, ReferenceOntology.PATO, 
				ReferenceOntology.SBO, ReferenceOntology.SNOMED}),
		PhysicalEntity(new ReferenceOntology[]{ReferenceOntology.CHEBI, ReferenceOntology.CL, ReferenceOntology.FMA, ReferenceOntology.GO,
				ReferenceOntology.MA, ReferenceOntology.OBI, ReferenceOntology.PR}),
		PhysicalProcess(new ReferenceOntology[]{ReferenceOntology.OPB, ReferenceOntology.GO});
		
		private ArrayList<ReferenceOntology> domainontologies = new ArrayList<ReferenceOntology>();
		private OntologyDomain(ReferenceOntology[] onts) {
			for (ReferenceOntology ont : onts) {
				domainontologies.add(ont);
			}
		}
		
		public ArrayList<ReferenceOntology> getDomainOntologies() {
			return domainontologies;
		}
		
		public String[] getArrayofOntologyNames() {
			String[] names = new String[domainontologies.size()];
			for (int i = 0; i<domainontologies.size(); i++) {
				names[i] = domainontologies.get(i).getFullName() + " (" + domainontologies.get(i).getNickName() + ")";
			}
			return names;
		}
		
		public ReferenceOntology getDomainOntologyatIndex(int index) {
			return domainontologies.get(index);
		}
		
		public int getOrdinalofOntology(ReferenceOntology ont) {
			return domainontologies.indexOf(ont);
		}

		public boolean domainhasReferenceOntology(ReferenceOntology ont) {
			if (ont==null) return false;
			return domainontologies.contains(ont);
		}
	};
	
	public enum ReferenceOntology {
		CHEBI("Chemical Entities of Biological Interest", "CHEBI", "1007",
				new String[]{"http://purl.org/obo/owl/CHEBI#", "http://purl.obolibrary.org/obo/CHEBI","http://identifiers.org/chebi/",
				"http://identifiers.org/obo.chebi/", "urn:miriam:obo.chebi:"}),
		CL("Cell Type Ontology", "CL", "1006",
				new String[]{"http://purl.obolibrary.org/obo/CL", "http://identifiers.org/cl/"}),
		CMO("Clinical Measurement Ontology", "CMO", "1583",
				new String[]{"http://purl.bioontology.org/ontology/CMO/", "http://purl.obolibrary.org/obo/CMO"}),
		FMA("Foundational Model of Anatomy", "FMA", "1053",
				new String[]{"http://purl.org/sig/ont/fma/", "http://sig.biostr.washington.edu/fma3.0#", "http://sig.uw.edu/fma#", "http://identifiers.org/fma/"}),
		GO("Gene Ontology", "GO", "1070",
				new String[]{"http://purl.org/obo/owl/GO#", "http://purl.obolibrary.org/obo/GO", "urn:miriam:obo.go:",
				"http://identifiers.org/go/", "http://identifiers.org/obo.go/"}),
		MA("Mouse Adult Gross Anatomy Ontology", "MA", "1000",
				new String[]{"http://purl.bioontology.org/ontology/MA", "http://purl.org/obo/owl/MA#", "http://purl.obolibrary.org/obo/MA", 
				"http://identifiers.org/ma/"}),
		OBI("Ontology for Biomedical Investigations", "OBI", "", new String[]{"http://purl.bioontology.org/ontology/OBI"}),
		OPB("Ontology of Physics for Biology", "OPB", "1141",
				new String[]{"http://bhi.washington.edu/OPB#", "http://www.owl-ontologies.com/unnamed.owl#", "http://identifiers.org/opb/"}),
		PATO("Phenotype and Trait Ontology", "PATO", "1107",
				new String[]{"http://purl.org/obo/owl/PATO#", "http://purl.obolibrary.org/obo/PATO"}),
		PR("Protein Ontology", "PR", "1062",
				new String[]{"http://purl.obolibrary.org/obo/PR"}),
		SBO("Systems Biology Ontology", "SBO", "1046",
				new String[]{"http://biomodels.net/SBO/", "http://purl.org/obo/owl/SBO#","http://purl.obolibrary.org/obo/SBO"}),
		SNOMED("SNOMED - Clinical Terms", "SNOMEDCT", "1353",
				new String[]{"http://purl.bioontology.org/ontology/SNOMEDCT/"}),
		UNIPROT("Universal Protein Resource", "UNIPROPT", "",
				new String[]{"http://purl.uniprot.org/uniprot/", "http://identifiers.org/uniprot/","http://www.uniprot.org/uniprot/"});
		
		private String fullname;
		private String nickname;
		private String bioportalid = null;
		private ArrayList<String> namespaces = new ArrayList<String>();
		
		private ReferenceOntology(String name, String abrev, String bpid, String[] ns) {
			fullname = name;
			nickname = abrev;
			bioportalid = bpid;
			for (String s : ns) {
				namespaces.add(s);
			}
		}
		
		public boolean hasNamespace(String nspace) {
			for (String ns : namespaces) {
				if (nspace.startsWith(ns)) return true; 
			}
			return false;
		}
		
		public String getFullName() {
			return new String(fullname);
		}
		
		public String getNickName() {
			return new String(nickname);
		}
		
		public String getBioPortalID() {
			return bioportalid;
		}
	}
}
