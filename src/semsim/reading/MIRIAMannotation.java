package semsim.reading;

import java.net.URI;

import semsim.owl.SemSimOWLFactory;

public class MIRIAMannotation {
	public String rdflabel;
	public URI fulluri;
	
	public MIRIAMannotation(String uristring, String rdflabel){
		this.rdflabel = rdflabel;
		fulluri = URI.create(uristring);
	}
	
	protected static String convertOldMiriamURNtoURI(String urn){
		String ns = null;
		if(urn.startsWith("urn:miriam:obo.go")){
			ns = "http://identifiers.org/obo.go/";
		}
		else if(urn.startsWith("urn:miriam:obo.chebi")){
			ns = "http://identifiers.org/obo.chebi/";
		}
		else if(urn.startsWith("urn:miriam:obo.bto")){
			ns = "http://identifiers.org/obo.bto/";
		}
		else if(urn.startsWith("urn:miriam:kegg.compound")){
			ns = "http://identifiers.org/kegg.compound/";
		}
		else if(urn.startsWith("urn:miriam:kegg.drug")){
			ns = "http://identifiers.org/kegg.drug/";
		}
		else if(urn.startsWith("urn:miriam:kegg.genes")){
			ns = "http://identifiers.org/kegg.genes/";
		}
		else if(urn.startsWith("urn:miriam:kegg.orthology")){
			ns = "http://identifiers.org/kegg.orthology/";
		}
		else if(urn.startsWith("urn:miriam:kegg.pathway")){
			ns = "http://identifiers.org/kegg.pathway/";
		}
		else if(urn.startsWith("urn:miriam:kegg.reaction")){
			ns = "http://identifiers.org/kegg.reaction/";
		}
		else if(urn.startsWith("urn:miriam:ec-code")){
			ns = "http://identifiers.org/ec-code/";
		}
		else if(urn.startsWith("urn:miriam:ensembl")){
			ns = "http://identifiers.org/ensembl/";
		}
		else if(urn.startsWith("urn:miriam:interpro")){
			ns = "http://identifiers.org/interpro/";
		}
		else if(urn.startsWith("urn:miriam:uniprot")){
			ns = "http://identifiers.org/uniprot/";
		}
		return ns + SemSimOWLFactory.getURIdecodedFragmentFromIRI(urn);
	}
}
