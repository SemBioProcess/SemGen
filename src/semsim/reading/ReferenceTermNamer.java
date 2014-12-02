package semsim.reading;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jdom.JDOMException;

import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.SemSimComponent;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.SemSimModel;
import semsim.owl.SemSimOWLFactory;
import semsim.webservices.BioPortalConstants;
import semsim.webservices.BioPortalSearcher;
import semsim.webservices.KEGGsearcher;
import semsim.webservices.UniProtSearcher;

public class ReferenceTermNamer {
	
	public static final String BioPortalOBOlibraryPrefix = "http://purl.obolibrary.org/obo/";
	public static final String BioPortalFMAnamespace = "http://sig.uw.edu/fma#";
	public static final String BioPortalSNOMEDCTnamespace = "http://purl.bioontology.org/ontology/SNOMEDCT/";
	public static final String BioPortalECGontNamespace = "http://www.cvrgrid.org/files/ECGOntologyv1.owl#";
	
	public static Map<String,String[]> getNamesForOntologyTermsInModel(SemSimModel model, Map<String, String[]> map, boolean online){	
		Map<String,String[]> URInameMap = null;
		if(map==null)
			URInameMap = new HashMap<String,String[]>();
		else URInameMap = map;
		
		// If we are online, get all the components of the model that can be annotated
		// then see if they are missing their Descriptions. Retrieve description from web services
		// or the local cache
		if(online){
			for(SemSimComponent ssc : model.getAllModelComponents()){
				if(ssc instanceof Annotatable){
					Annotatable annthing = (Annotatable)ssc;
					// If annotation present
					for(Annotation ann : annthing.getAnnotations()){
						if(ann instanceof ReferenceOntologyAnnotation){
							ReferenceOntologyAnnotation refann = (ReferenceOntologyAnnotation)ann;
							URI uri = refann.getReferenceURI();
							
							// If we need to fill in the description
							if(refann.getValueDescription()==uri.toString()){
								
								System.out.println("Need to find " + uri.toString());
								String name = null;
								if(URInameMap.containsKey(uri.toString())){
									name = URInameMap.get(uri.toString())[0];
								}
								else{
									name = getNameFromURI(uri);
									
									// If we retrieved the name
									if(name!=null){
										System.out.println("Found name for " + SemSimOWLFactory.getIRIfragment(uri.toString()) + ": " + name);
										URInameMap.put(uri.toString(), new String[]{name});
									}
									else{
										name = SemSimOWLFactory.getIRIfragment(uri.toString());
										System.out.println("Could not retrieve name for " + SemSimOWLFactory.getIRIfragment(uri.toString()));
									}
								}
								
								refann.setValueDescription(name);
								// Set the name of the semsim component to the annotation description if it's
								// a physical entity or physical process
								if(ann.getRelation()==SemSimConstants.REFERS_TO_RELATION && ((ssc instanceof PhysicalEntity)  || (ssc instanceof PhysicalProcess)))
									ssc.setName(name);
							}
						}
					}
				}
			}
		}
		
		// Rename the composite physical entities, given the newly named singular entities
		for(CompositePhysicalEntity cpe : model.getCompositePhysicalEntities())
			cpe.setName(cpe.makeName());
		
		return URInameMap;
	}
	
	private static String getNameFromURI(URI uri) {
		String uristring= uri.toString();
		String id = null;
		if(uristring.startsWith("urn:miriam:")) id = uristring.substring(uristring.lastIndexOf(":")+1);
		else id = SemSimOWLFactory.getIRIfragment(uristring);
		
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uristring);
		String name = null;
		
		String KBname = null;
		if(SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(namespace)) 
			KBname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(namespace);
		
		if(KBname!=null){
			System.out.println("Accessing " + KBname);
			if(KBname.equals(SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("FMA%3A", "");
				edittedid = id.replace("FMA:", "FMA_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalFMAnamespace + edittedid);

				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			if(KBname.equals(SemSimConstants.GENE_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("GO%3A", "");
				edittedid = id.replace("GO:", "GO_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.CELL_TYPE_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("CL%3A", "");
				edittedid = id.replace("CL:", "CL_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("CHEBI%3A", "");
				edittedid = id.replace("CHEBI:", "CHEBI_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.BRENDA_TISSUE_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("BTO%3A", "");
				edittedid = id.replace("BTO:", "BTO_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if (KBname.equals(SemSimConstants.SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("SBO%3A", "");
				edittedid = id.replace("SBO:", "SBO_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if (KBname.equals(SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("OPB%3A", "");
				edittedid = id.replace("OPB:", "OPB_");
				edittedid = SemSimOWLFactory.URIencoding(SemSimConstants.OPB_NAMESPACE + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("PATO%3A", "");
				edittedid = id.replace("PATO:", "PATO_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME)
					|| KBname.equals(SemSimConstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME)
					|| KBname.equals(SemSimConstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME)
					|| KBname.equals(SemSimConstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME)
					|| KBname.equals(SemSimConstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME)
					|| KBname.equals(SemSimConstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME)){
				String edittedid = SemSimOWLFactory.getIRIfragment(uri.toString());
				try {
					name = KEGGsearcher.getNameForID(edittedid);
				} catch (IOException e) {e.printStackTrace();}
			}
			else if(KBname.equals(SemSimConstants.BRAUNSCHWEIG_ENZYME_DATABASE_FULLNAME)){
				// Use KEGG for EC codes
				String edittedid = SemSimOWLFactory.getIRIfragment(uri.toString());
				try {
					name = KEGGsearcher.getNameForID("ec:" + edittedid);
				} catch (IOException e) {e.printStackTrace();}
	
			}
			else if(KBname.equals(SemSimConstants.UBERON_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("UBERON%3A", "");
				edittedid = id.replace("UBERON:", "UBERON_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				String edittedid = id.replace("MA%3A", "");
				edittedid = id.replace("MA:", "MA_");
				edittedid = SemSimOWLFactory.URIencoding(BioPortalOBOlibraryPrefix + edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.SNOMEDCT_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				id = SemSimOWLFactory.URIencoding(BioPortalSNOMEDCTnamespace + id);
				name = getRDFLabelUsingBioPortal(id, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME)){
				String bioportalontID = 
					BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.get(SemSimConstants.CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME);
				name = getRDFLabelUsingBioPortal(id, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.ECG_ONTOLOGY_FULLNAME)){
				String bioportalontID = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(KBname);
				id = SemSimOWLFactory.URIencoding(BioPortalECGontNamespace + id);
				name = getRDFLabelUsingBioPortal(id, bioportalontID);
			}
			else if(KBname.equals(SemSimConstants.UNIPROT_FULLNAME)){
				try {
					name = getRDFLabelUsingUniProt(id);
				} 
				catch (IOException | JDOMException e) {
					e.printStackTrace();
					} 
			}
		}
		return name; 
	}
	
	
	private static String getRDFLabelUsingBioPortal(String id, String bioportalontID){
		return BioPortalSearcher.getRDFLabelUsingBioPortal(id, bioportalontID);
	}
	
	private static String getRDFLabelUsingUniProt(String ID) throws IOException, JDOMException{
		return UniProtSearcher.getPreferredNameForID(ID);
	}
}
