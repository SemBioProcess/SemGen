package semsim.model.annotation;

import java.net.URI;
import semsim.SemSimConstants;
import semsim.owl.SemSimOWLFactory;

/**
 * A type of Annotation where the annotation value is a URI
 * from a reference ontology or other controlled knowledge base.
 */
public class ReferenceOntologyAnnotation extends Annotation{
	private String bioPortalOntologyID;
	private String bioPortalOntologyVersionID;
	private String ontologyName;
	private String ontologyAbbreviation;
	private URI referenceUri;
	private String altNumericalID;
	
	/**
	 * Constructor for annotation
	 * @param relation The relationship between the object being annotated and the knowledge base URI
	 * @param uri The URI annotation value
	 * @param description A free-text description of the resource corresponding to the URI
	 */
	public ReferenceOntologyAnnotation(SemSimRelation relation, URI uri, String valueDescription){
		super(relation, uri);
		setReferenceURI(uri);
		setReferenceOntologyAbbreviation(uri);
		setValueDescription(valueDescription);
	}

	/**
	 * Set the BioPortal Ontology ID of the knowledge base that contains the URI annotation value
	 * @param bioPortalOntologyID The BioPortal Ontology ID
	 */
	public void setBioPortalOntologyID(String bioPortalOntologyID) {
		this.bioPortalOntologyID = bioPortalOntologyID;
	}

	/**
	 * @return The BioPortal Ontology ID of the knowledge base that contains the URI annotation value
	 */
	public String getBioPortalOntologyID() {
		return bioPortalOntologyID;
	}

	/**
	 * Set the BioPortal Ontology version ID
	 * @param bioPortalOntologyVersionID The ID
	 */
	public void setBioPortalOntologyVersionID(String bioPortalOntologyVersionID) {
		this.bioPortalOntologyVersionID = bioPortalOntologyVersionID;
	}

	/**
	 * @return The BioPortal Ontology version ID of the knowledge base that contains the URI annotation value
	 */
	public String getBioPortalOntologyVersionID() {
		return bioPortalOntologyVersionID;
	}
	
	/**
	 * @return The free-text description of the resource corresponding to the URI 
	 */
	public String getValueDescription() {
		if(valueDescription==null){
			if(getReferenceURI()!=null){
				return getReferenceURI().toString();
			}
			else return "?";
		}
		else return valueDescription;
	}
	

	/**
	 * Set the name of the knowledge base that contains the URI annotation value
	 * @param ontologyName The knowledge base name
	 */
	public void setReferenceOntologyName(String ontologyName) {
		this.ontologyName = ontologyName;
	}

	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getReferenceOntologyName() {
		return ontologyName;
	}

	/**
	 * Set the abbreviation of the knowledge base containing a specified URI
	 * @param uri A URI from a knowledge base
	 */
	public void setReferenceOntologyAbbreviation(URI uri) {
		if(SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(getNamespaceFromIRI(uri.toString()))){
			String fullname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(getNamespaceFromIRI(uri.toString()));
			if(SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.containsKey(fullname)){
				ontologyAbbreviation = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(fullname);
			}
		}
		else ontologyAbbreviation = "?";
	}

	/**
	 * @return The abbreviation of the knowledge base containing the URI used for the annotation value
	 */
	public String getOntologyAbbreviation() {
		return ontologyAbbreviation;
	}

	/**
	 * Set the URI annotation value
	 * @param uri The URI to reference
	 */
	public void setReferenceURI(URI uri) {
		this.referenceUri = uri;
	}

	/**
	 * @return The URI used as the annotation value
	 */
	public URI getReferenceURI() {
		return referenceUri;
	}
	
	/**
	 * Convenience method for getting the namespace of a URI
	 * @param uri A URI
	 * @return The namespace of the URI
	 */
	public String getNamespaceFromIRI(String uri) {
		return SemSimOWLFactory.getNamespaceFromIRI(uri);
	}
	
	/**
	 * @return A numerical ID for the reference concept (only used to
	 * map Foundational Model of Anatomy URIs to their numerical FMA IDs.
	 */
	public String getAltNumericalID(){
		return altNumericalID;
	}
	
	/**
	 * @param ID The numerical ID of the reference term (only used for
	 * the Foundational Model of Anatomy)
	 */
	public void setAltNumericalID(String ID){
		altNumericalID = ID;
	}
}
