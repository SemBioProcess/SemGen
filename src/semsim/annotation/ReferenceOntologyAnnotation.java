package semsim.annotation;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.definitions.SemSimRelation;

/**
 * A type of Annotation where the annotation value is a URI
 * from a reference ontology or other controlled knowledge base.
 */
public class ReferenceOntologyAnnotation extends Annotation{
	private Ontology refontology;
	private URI referenceUri;
	private String altNumericalID;
	
	/**
	 * Constructor for annotation
	 * @param relation The relationship between the object being annotated and the knowledge base URI
	 * @param uri The URI annotation value
	 * @param description A free-text description of the resource corresponding to the URI
	 */
	public ReferenceOntologyAnnotation(SemSimRelation relation, URI uri, String valueDescription, SemSimLibrary lib){
		super(relation, uri);
		refontology = lib.getOntologyfromTermURI(uri.toString());
		setReferenceURI(uri);
		setValueDescription(valueDescription);
	}

	/**
	 * @return The BioPortal Ontology ID of the knowledge base that contains the URI annotation value
	 */
	public String getBioPortalOntologyID() {
		return refontology.getBioPortalID();
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
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getReferenceOntologyName() {
		return refontology.getFullName();
	}

	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation() {
		return valueDescription + " (" + getOntologyAbbreviation() + ")";
	}

	/**
	 * @return The abbreviation of the knowledge base containing the URI used for the annotation value
	 */
	public String getOntologyAbbreviation() {
		return refontology.getNickName();
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
