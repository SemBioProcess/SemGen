package semsim.annotation;

import com.hp.hpl.jena.rdf.model.Property;
import org.semanticweb.owlapi.model.IRI;

import java.net.URI;

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
