package semsim.reading;

import java.net.URI;

import semsim.owl.SemSimOWLFactory;

/**
 * Class for working with annotations formatted according to 
 * MIRIAM guidelines
 * @author mneal
 *
 */
public class MIRIAMannotation {
	public String rdflabel;
	public URI fulluri;
	
	/**
	 * Constructor
	 * @param uristring The URI referenced in the annotation
	 * @param rdflabel RDF label for the annotation
	 */
	public MIRIAMannotation(String uristring, String rdflabel){
		this.rdflabel = rdflabel;
		fulluri = URI.create(uristring);
	}
	
	
	/**
	 * Converts URIs that used older "urn:miriam..." formatting into
	 * identifiers.org-formatted URIs
	 * @param urn An input MIRIAM URN
	 * @return Identifiers.org-formatted version of the URN
	 */
	protected static String convertOldMiriamURNtoURI(String urn){
		String ns = null;
		if(urn.startsWith("urn:miriam:obo.go")){
			ns = "https://identifiers.org/obo.go/";
		}
		else if(urn.startsWith("urn:miriam:obo.chebi")){
			ns = "https://identifiers.org/obo.chebi/";
		}
		else if(urn.startsWith("urn:miriam:obo.bto")){
			ns = "https://identifiers.org/obo.bto/";
		}
		else if(urn.startsWith("urn:miriam:kegg.compound")){
			ns = "https://identifiers.org/kegg.compound/";
		}
		else if(urn.startsWith("urn:miriam:kegg.drug")){
			ns = "https://identifiers.org/kegg.drug/";
		}
		else if(urn.startsWith("urn:miriam:kegg.genes")){
			ns = "https://identifiers.org/kegg.genes/";
		}
		else if(urn.startsWith("urn:miriam:kegg.orthology")){
			ns = "https://identifiers.org/kegg.orthology/";
		}
		else if(urn.startsWith("urn:miriam:kegg.pathway")){
			ns = "https://identifiers.org/kegg.pathway/";
		}
		else if(urn.startsWith("urn:miriam:kegg.reaction")){
			ns = "https://identifiers.org/kegg.reaction/";
		}
		else if(urn.startsWith("urn:miriam:ec-code")){
			ns = "https://identifiers.org/ec-code/";
		}
		else if(urn.startsWith("urn:miriam:ensembl")){
			ns = "https://identifiers.org/ensembl/";
		}
		else if(urn.startsWith("urn:miriam:interpro")){
			ns = "https://identifiers.org/interpro/";
		}
		else if(urn.startsWith("urn:miriam:uniprot")){
			ns = "https://identifiers.org/uniprot/";
		}
		return ns + SemSimOWLFactory.getURIdecodedFragmentFromIRI(urn);
	}
}
