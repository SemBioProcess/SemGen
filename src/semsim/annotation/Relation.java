package semsim.annotation;

import org.apache.jena.rdf.model.Property;
import org.semanticweb.owlapi.model.IRI;

import java.net.URI;

/**
 * Interface that provides methods for getting information about
 * relations (AKA predicates, AKA qualifiers) used in annotations.
 */
public interface Relation {
	public String getName();
	
	/** @return The URI of the relation */
	public URI getURI();
	
	/** @return The URI of the relation as a string*/
	public String getURIasString();
	
	/** @return The free-text description of the relation*/
	public String getDescription();
	
	/** @return The relation encoded for SPARQL queries*/
	public String getSPARQLCode();
	
	/** @return The IRI of the relation*/
	public IRI getIRI();
	
	/** @return The relation instantiated as an RDF property*/
	public Property getRDFproperty();
}
