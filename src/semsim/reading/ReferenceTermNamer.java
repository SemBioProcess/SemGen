package semsim.reading;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

import semsim.SemSimLibrary;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.Ontology;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.OtherKBconstants;
import semsim.model.SemSimComponent;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.webservices.BioPortalSearcher;
import semsim.utilities.webservices.KEGGsearcher;
import semsim.utilities.webservices.UniProtSearcher;

/**
 * Class for looking up human-readable names for reference URIs
 * @author mneal
 */
public class ReferenceTermNamer {
		
	public static final String BioPortalOBOlibraryPrefix = "http://purl.obolibrary.org/obo/";
	
	/**
	 * @param model A SemSim model containing the SemSimObjects that will be processed
	 * @param lib A {@link SemSimLibrary} instance
	 * @return The set of SemSimObjects annotated with reference terms that are missing names.
	 */
	public static Set<SemSimComponent> getModelComponentsWithUnnamedAnnotations(SemSimModel model, SemSimLibrary lib){
		
		Set<SemSimComponent> unnamed = new HashSet<SemSimComponent>();
		
		for(SemSimComponent ssc : model.getAllModelComponents()){
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
	
	
	/**
	 * Retrieve human-readable names for reference URIs in a SemSim model
	 * @param model The model to process
	 * @param map An optional mapping between reference URIs and valid human-readable names
	 * @param lib A {@link SemSimLibrary} instance
	 * @return The input map extended to include URI-to-names mappings discovered during execution 
	 * of this function
	 */
	public static Map<String,String[]> getNamesForReferenceTermsInModel(SemSimModel model, Map<String, String[]> map, SemSimLibrary lib){	
		Map<String,String[]> URInameMap = null;
		if(map==null)
			URInameMap = new HashMap<String,String[]>();
		else URInameMap = map;
		
		// If we are online, get all the components of the model that can be annotated
		// then see if they are missing their Descriptions. Retrieve description from web services
		// or the local cache
		for(SemSimComponent ssc : getModelComponentsWithUnnamedAnnotations(model, lib)){
			
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
						if(name != null){
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
					if(ann.getRelation()==SemSimRelation.HAS_PHYSICAL_DEFINITION
							&& ssc.isPhysicalComponent())
						ssc.setName(name);
				}
			}
		}
		
		
		// Rename the composite physical entities, given the newly named singular entities
		for(CompositePhysicalEntity cpe : model.getCompositePhysicalEntities())
			cpe.setName(cpe.makeName());
		
		return URInameMap;
	}
	
	
	/**
	 * Internal function for looking up human-readable names for a given URI.
	 * Determines which lookup service to use based on the URI's parent 
	 * knowledge resource
	 * @param uri The URI to process
 	 * @param lib A {@link SemSimLibrary} instance
	 * @return The human-readable name for the input URI
	 */
	private static String getNameFromURI(URI uri, SemSimLibrary lib) {
		String name = null;

		String uristring= uri.toString();
		
		Ontology ont = lib.getOntologyfromTermURI(uristring);
		if (ont==ReferenceOntology.UNKNOWN.getAsOntology()) return null;
		
		String id = null;
		if(uristring.startsWith("urn:miriam:")) id = uristring.substring(uristring.lastIndexOf(":")+1);
		else id = SemSimOWLFactory.getIRIfragment(uristring);
		
		
		String KBname = ont.getFullName();
		System.out.println("Accessing " + KBname);
		
		if ( ! ont.getBioPortalNamespace().isEmpty()) {
			String edittedid = id;
			
			String ns = ont.getBioPortalNamespace();
			if(ns.startsWith(BioPortalOBOlibraryPrefix)) ns = BioPortalOBOlibraryPrefix;
					
			
			if (ont==ReferenceOntology.FMA.getAsOntology()) {
				edittedid = edittedid.replace("FMA%3A", "");
				edittedid = ns + edittedid.replace("FMA:", "fma");
			}
			else if (ont==ReferenceOntology.OPB.getAsOntology()) {
				edittedid = edittedid.replace("OPB%3A", "");
				edittedid = ns + edittedid.replace("OPB:", "OPB_");
			}
			else if (ont==ReferenceOntology.CHEBI.getAsOntology()) {
				edittedid = edittedid.replace("CHEBI%3A", "");
				edittedid = ns + edittedid.replace("CHEBI:", "CHEBI_");
			}
			else if (ont==ReferenceOntology.GO.getAsOntology()) {
				edittedid = edittedid.replace("GO%3A", "");
				edittedid = ns + edittedid.replace("GO:", "GO_");
			}
			else if (ont==ReferenceOntology.CL.getAsOntology()) {
				edittedid = edittedid.replace("CL%3A", "");
				edittedid = ns + edittedid.replace("CL:", "CL_");
			}
			else if (ont==ReferenceOntology.MA.getAsOntology()) {
				edittedid = edittedid.replace("MA%3A", "");
				edittedid = ns + edittedid.replace("MA:", "MA_");
			}
			else if (ont==ReferenceOntology.PR.getAsOntology()) {
				edittedid = edittedid.replace("PR%3A", "");
				edittedid = ns + edittedid.replace("PR:", "PR_");
			}
			else if (ont.getNickName().equals("BTO")) {
				edittedid = edittedid.replace("BTO%3A", "");
				edittedid = ns + edittedid.replace("BTO:", "BTO_");
			}
			else if (ont==ReferenceOntology.SBO.getAsOntology()) {
				edittedid = edittedid.replace("SBO%3A", "");
				edittedid = ns + edittedid.replace("SBO:", "SBO_");
			}
			
			else if (ont==ReferenceOntology.PATO.getAsOntology()) {
				edittedid = edittedid.replace("PATO%3A", "");
				edittedid = ns + edittedid.replace("PATO:", "PATO_");
			}
			else if (ont.getNickName().equals("UBERON")) {
				edittedid = edittedid.replace("UBERON%3A", "");
				edittedid = ns + edittedid.replace("UBERON:", "UBERON_");
			}
			
			else if (ont==ReferenceOntology.SNOMED.getAsOntology()) {
				edittedid = ns + edittedid;
			}
			edittedid = SemSimOWLFactory.URIencoding(edittedid);
			name = 	BioPortalSearcher.getRDFLabelUsingBioPortal(edittedid, ont.getNickName());

		}

		else if(KBname.equals(OtherKBconstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME)
				|| KBname.equals(OtherKBconstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME)
				|| KBname.equals(OtherKBconstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME)
				|| KBname.equals(OtherKBconstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME)
				|| KBname.equals(OtherKBconstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME)
				|| KBname.equals(OtherKBconstants.KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME)){
			try {
				name = KEGGsearcher.getNameForID(id);
			} catch (IOException e) {e.printStackTrace();}
		}
		else if(KBname.equals(OtherKBconstants.BRAUNSCHWEIG_ENZYME_DATABASE_FULLNAME)){
			// Use KEGG for EC codes
			try {
				name = KEGGsearcher.getNameForID("ec:" + id);
			} catch (IOException e) {e.printStackTrace();}

		}

		else if(KBname.equals(OtherKBconstants.UNIPROT_FULLNAME)){
			try {
				name = UniProtSearcher.getPreferredNameForID(id);
			} 
			catch (IOException | JDOMException e) {
				e.printStackTrace();
			} 
		}
		
		return name; 
	}
	
}
