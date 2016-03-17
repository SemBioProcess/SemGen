package semsim.annotation;

import java.net.URI;

import org.semanticweb.owlapi.model.IRI;

import com.hp.hpl.jena.rdf.model.Property;

public interface Relation {
	public String getName();
	
	/** @return The URI of the relation */
	public URI getURI();
	
	public String getURIasString();
	
	public String getDescription();
	
	public String getSPARQLCode();
	
	public IRI getIRI();
	
	public Property getRDFproperty();
}
