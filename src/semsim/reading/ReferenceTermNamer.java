package semsim.reading;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.Ontology;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.definitions.SemSimConstants;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.webservices.BioPortalSearcher;
import semsim.utilities.webservices.KEGGsearcher;
import semsim.utilities.webservices.UniProtSearcher;

public class ReferenceTermNamer {
	
	public static final String BioPortalOBOlibraryPrefix = "http://purl.obolibrary.org/obo/";
	public static final String BioPortalFMAnamespace = "http://purl.org/sig/ont/fma/";
	public static final String BioPortalSNOMEDCTnamespace = "http://purl.bioontology.org/ontology/SNOMEDCT/";
	public static final String BioPortalECGontNamespace = "http://www.cvrgrid.org/files/ECGOntologyv1.owl#";
	
	/**
	 * @param model The SemSim model containing the SemSimObjects that will be processed
	 * @return The set of SemSimObjects annotated with reference terms that are missing names.
	 */
	public static Set<SemSimObject> getModelComponentsWithUnnamedAnnotations(SemSimModel model, SemSimLibrary lib){
		
		Set<SemSimObject> unnamed = new HashSet<SemSimObject>();
		
		for(SemSimObject ssc : model.getAllModelComponents()){
			if(ssc instanceof Annotatable){

				Annotatable annthing = (Annotatable)ssc;
				Set<Annotation> anns = new HashSet<Annotation>();
				anns.addAll(annthing.getAnnotations());
				
				if(ssc instanceof ReferenceTerm) 
					anns.add(((ReferenceTerm)ssc).getPhysicalDefinitionReferenceOntologyAnnotation(lib));
				
				// If annotation present
				for(Annotation ann : anns){

					if(ann instanceof ReferenceOntologyAnnotation){
						ReferenceOntologyAnnotation refann = (ReferenceOntologyAnnotation)ann;
						URI uri = refann.getReferenceURI();
						
						// If we need to fill in the description by going online
						if(refann.getValueDescription().equals(uri.toString()) || refann.getValueDescription().equals("")){
							unnamed.add(ssc);
						}
					}
				}
			}
		}
		return unnamed;
	}
	
	
	public static Map<String,String[]> getNamesForOntologyTermsInModel(SemSimModel model, Map<String, String[]> map, SemSimLibrary lib){	
		Map<String,String[]> URInameMap = null;
		if(map==null)
			URInameMap = new HashMap<String,String[]>();
		else URInameMap = map;
		
		// If we are online, get all the components of the model that can be annotated
		// then see if they are missing their Descriptions. Retrieve description from web services
		// or the local cache
		for(SemSimObject ssc : getModelComponentsWithUnnamedAnnotations(model, lib)){
			
			Annotatable annthing = (Annotatable)ssc;
			Set<Annotation> anns = new HashSet<Annotation>();
			anns.addAll(annthing.getAnnotations());
			
			if(ssc instanceof ReferenceTerm) 
				anns.add(((ReferenceTerm)ssc).getPhysicalDefinitionReferenceOntologyAnnotation(lib));
			
			for(Annotation ann : anns){

				if(ann instanceof ReferenceOntologyAnnotation){
					ReferenceOntologyAnnotation refann = (ReferenceOntologyAnnotation)ann;
					URI uri = refann.getReferenceURI();
					
					System.out.println("Need to find " + uri.toString());
					String name = null;
					if(URInameMap.containsKey(uri.toString())){
						name = URInameMap.get(uri.toString())[0];
						System.out.println(uri.toString() + " was already cached: " + name);
					}
					else{
						name = getNameFromURI(uri, lib);
						
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
					// a physical entity, physical process or physical property
					if(ann.getRelation()==SemSimConstants.HAS_PHYSICAL_DEFINITION_RELATION 
							&& ((ssc instanceof PhysicalEntity)  || (ssc instanceof PhysicalProcess) 
									|| (ssc instanceof PhysicalProperty) || (ssc instanceof PhysicalPropertyinComposite)))
						ssc.setName(name);
				}
			}
		}
		
		
		// Rename the composite physical entities, given the newly named singular entities
		for(CompositePhysicalEntity cpe : model.getCompositePhysicalEntities())
			cpe.setName(cpe.makeName());
		
		return URInameMap;
	}
	
	private static String getNameFromURI(URI uri, SemSimLibrary lib) {
		String uristring= uri.toString();
		
		Ontology ont = lib.getOntologyfromTermURI(uristring);
		if (ont==ReferenceOntologies.unknown) return null;
		
		String id = null;
		if(uristring.startsWith("urn:miriam:")) id = uristring.substring(uristring.lastIndexOf(":")+1);
		else id = SemSimOWLFactory.getIRIfragment(uristring);
		
		String name = null;
		String KBname = ont.getFullName();
		System.out.println("Accessing " + KBname);
		
		while (name==null) {
			if (!ont.getBioPortalID().isEmpty()) {
				String edittedid = id;
				
				if (ont.getNickName()==ReferenceOntology.FMA.getNickName()) {
					edittedid = edittedid.replace("FMA%3A", "");
					edittedid = BioPortalFMAnamespace + edittedid.replace("FMA:", "fma");
				}
				else if (ont.getNickName()==ReferenceOntology.GO.getNickName()) {
					edittedid = edittedid.replace("GO%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("GO:", "GO_");
				}
				else if (ont.getNickName()==ReferenceOntology.CL.getNickName()) {
					edittedid = edittedid.replace("CL%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("CL:", "CL_");
				}
				else if (ont.getNickName()==ReferenceOntology.CHEBI.getNickName()) {
					edittedid = edittedid.replace("CHEBI%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("CHEBI:", "CHEBI_");
				}
				else if (ont.getNickName()==ReferenceOntology.PR.getNickName()) {
					edittedid = edittedid.replace("PR%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("PR:", "PR_");
				}
				else if (ont.getNickName().equals("BTO")) {
					edittedid = edittedid.replace("BTO%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("BTO:", "BTO_");
				}
				else if (ont.getNickName()==ReferenceOntology.SBO.getNickName()) {
					edittedid.replace("SBO%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("SBO:", "SBO_");
				}
				else if (ont.getNickName()==ReferenceOntology.OPB.getNickName()) {
					edittedid = edittedid.replace("OPB%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("OPB:", "OPB_");
				}
				else if (ont.getNickName()==ReferenceOntology.PATO.getNickName()) {
					edittedid = edittedid.replace("PATO%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("PATO:", "PATO_");
				}
				else if (ont.getNickName().equals("UBERON")) {
					edittedid = edittedid.replace("UBERON%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("UBERON:", "UBERON_");
				}
				else if (ont.getNickName()==ReferenceOntology.MA.getNickName()) {
					edittedid = edittedid.replace("MA%3A", "");
					edittedid = BioPortalOBOlibraryPrefix + edittedid.replace("MA:", "MA_");
				}
				else if (ont.getNickName().equals("ECG")) {
					edittedid = BioPortalECGontNamespace + edittedid;
				}
				else if (ont.getNickName()==ReferenceOntology.SNOMED.getNickName()) {
					edittedid = BioPortalSNOMEDCTnamespace + edittedid;
				}
				edittedid = SemSimOWLFactory.URIencoding(edittedid);
				name = getRDFLabelUsingBioPortal(edittedid, ont.getNickName());
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
