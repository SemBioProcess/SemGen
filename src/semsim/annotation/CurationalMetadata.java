package semsim.annotation;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import semsim.definitions.RDFNamespace;

/**
 * Constants and methods for working with annotations at the level of 
 * the whole model. These types of annotations include model name, description
 * keywords, annotator name, etc. 
 */
public class CurationalMetadata {
	private static String SEMSIM_NAMESPACE = RDFNamespace.SEMSIM.getNamespaceasString();
	
	public static final URI MODEL_NAME_URI = URI.create(SEMSIM_NAMESPACE + "modelName");
	public static final URI MODEL_DESCRIPTION_URI = URI.create(SEMSIM_NAMESPACE + "ModelDescription");
	public static final URI KEYWORDS_URI = URI.create(SEMSIM_NAMESPACE + "Keywords");
	public static final URI MODEL_ID_URI = URI.create(SEMSIM_NAMESPACE + "modelId");
	public static final URI ANNOTATOR_NAME_URI = URI.create(SEMSIM_NAMESPACE + "AnnotatorName");
	public static final URI ANNOTATOR_CONTACT_INFO_URI = URI.create(SEMSIM_NAMESPACE + "AnnotatorContactInfo");
	public static final URI MODELER_NAME_URI = URI.create(SEMSIM_NAMESPACE + "ModelerName");
	public static final URI MODELER_CONTACT_INFO_URI = URI.create(SEMSIM_NAMESPACE + "ModelerInfo");

	public static final URI REFERENCE_PUBLICATION_PUBMED_ID_URI = URI.create(SEMSIM_NAMESPACE + "PubMedIDofReferencePublication");
	public static final URI REFERENCE_PUBLICATION_ABSTRACT_TEXT_URI = URI.create(SEMSIM_NAMESPACE + "ReferencePublicationAbstractText");
	public static final URI REFERENCE_PUBLICATION_CITATION_URI = URI.create(SEMSIM_NAMESPACE + "ReferencePublicationCitation");
	
	public static final URI MATLAB_URL_URI = URI.create(SEMSIM_NAMESPACE + "MatLabURL");
	public static final URI CELLML_URL_URI = URI.create(SEMSIM_NAMESPACE + "cellmlURL");
	public static final URI SBML_URL_URI = URI.create(SEMSIM_NAMESPACE + "sbmlURL");
	public static final URI JSIM_URL_URI = URI.create(SEMSIM_NAMESPACE + "jsimURL");
	
	// Model-level relations
	//REFERENCE_PUBLICATION_ABSTRACT_TEXT_RELATION = new SemSimRelation("the abstract text of the model's reference publication", REFERENCE_PUBLICATION_ABSTRACT_TEXT_URI);
	//REFERENCE_PUBLICATION_CITATION_RELATION = new SemSimRelation("the citation for the reference publication", REFERENCE_PUBLICATION_CITATION_URI);

	private LinkedHashMap<Metadata, String> curationmap = new LinkedHashMap<Metadata, String>();
	
	public CurationalMetadata() {
		for (Metadata m : Metadata.values()) {
			curationmap.put(m, "");
		}
	}
	
	/**
	 * Copy curational metadata from one {@link CurationalMetadata} object to another
	 */
	public void importMetadata(CurationalMetadata toimport, boolean overwrite) {
		for (Metadata m : Metadata.values()) {
			if (curationmap.get(m)=="" || overwrite) {
				curationmap.put(m, toimport.getAnnotationValue(m));
			}
		}
	}
	
	/**
	 * Enum of the different relations available for curational metadata annotations
	 *
	 */
	public enum Metadata implements Relation {
		fullname("Full Name", "a human-readable name for the model", MODEL_NAME_URI, "dc:title"),
		description("Description", "a free-text description of the model", MODEL_DESCRIPTION_URI, "dc:description"),
		keywords("Keywords", "keywords", KEYWORDS_URI, "SemSim:keyords"),
		annotatorauthor("Annotator Name", "who to contact about the annotations in the model", ANNOTATOR_NAME_URI, "SemSim:annotator"),
		annotatorcontact("Annotator Contact", "email address of annotator", ANNOTATOR_CONTACT_INFO_URI, "SemSim:annotatoremail"),
		modelauthor("Model Author", "who to contact about the model", MODELER_NAME_URI, "dc:creator"),
		modelcontact("Model Contact", "email address of modeler", MODELER_CONTACT_INFO_URI, "SemSim:creatoremail"),
		sourcemodelid("Source Model ID", "the ID of the model from which the SemSim model was generated", MODEL_ID_URI, "rdf:id"),
		cellmlurl("CellML URL","the URL for the CellML version of the model", CELLML_URL_URI, "SemSim:cellmlloc"),
		matlaburl("Matlab URL","the URL for the Matlab version of the model", MATLAB_URL_URI, "SemSim:matlabloc"),
		mmlurl("JSim URL","the URL for the JSim version of the model", JSIM_URL_URI, "SemSim:jsimloc"),
		sbmlurl("SBML URL","the URL for the SBML version of the model", SBML_URL_URI, "SemSim:sbmlloc"),
		pubmedid("PubMed ID", "the PubMed ID of the model's reference publication", REFERENCE_PUBLICATION_PUBMED_ID_URI, "SemSim:pubmedid");
		
		private final String text;
		private final URI uri;
		private final String metadatadescription;
		private final String sparqlcode;
		
		
		private Metadata(final String text, String desc, URI uri, String spqlcode) {
		   this.text = text;
		   this.uri = uri;
		   this.metadatadescription = desc;
		   sparqlcode = spqlcode;
		}
	
		@Override
		public String toString() {
		   return text;
		}
			 
		public String getDescription() {
			return metadatadescription;
		}
		
		 public URI getURI() {
			 return uri;
		 }
		 
		@Override
		public String getName() {
			return text;
		}

		@Override
		public String getURIasString() {
			return uri.toString();
		}

		@Override
		public String getSPARQLCode() {
			return sparqlcode;
		}

		@Override
		public IRI getIRI() {
			return IRI.create(uri);
		}
		
		@Override
		public Property getRDFproperty(){
			return ResourceFactory.createProperty(getURIasString());
		}
	}
	
	/**
	 * @param item A curational metadata object
	 * @return A textual description of the Metadata relation
	 */
	public String getAnnotationName(Metadata item) {
		return item.toString();
	}
	
	/**
	 * @param item A curational metadata object
	 * @return The annotation value for the metadata object
	 */
	public String getAnnotationValue(Metadata item) {
		return curationmap.get(item);
	}
	
	/**
	 * @param item A curational metadata object
	 * @param value The annotation value for the curational metadata
	 * object
	 */
	public void setAnnotationValue(Metadata item, String value) {
		curationmap.put(item, value);
	}
	
	/**
	 * @param item A curational metadata object
	 * @return Whether the metadata object has an associated value
	 */
	public boolean hasAnnotationValue(Metadata item) {
		return !curationmap.get(item).isEmpty();
	}
	
	/**
	 * @param item A curational metadata item with an annotation value
	 * @param value The annotation value for testing equivalency
	 * @return Whether the metadata object's annotation value is equivalent
	 * to the specified annotation value 
	 */
	public boolean isItemValueEqualto(Metadata item, String value) {
		return curationmap.get(item).equals(value);
	}

	/**
	 * @param list A set of OWLAnnotation objects
	 * @param removelist
	 */
	public void setCurationalMetadata(Set<OWLAnnotation> list, Set<OWLAnnotation> removelist) {
		for (Metadata m : Metadata.values()) {
			for (OWLAnnotation a : list) {
				if (m.getURI().equals(a.getProperty().getIRI().toURI())) {
					setAnnotationValue(m, ((OWLLiteral)a.getValue()).getLiteral());
					removelist.add(a);
				}
			}
		}
	}
	
	/**
	 * @param item A curational metadata item
	 * @return The curational metadata item as an {@link Annotation} object
	 */
	public Annotation getAsAnnotation(Metadata item) {
		return new Annotation(item, curationmap.get(item));
	}
	
	/**
	 * @return The set of available curational metadata items
	 *  cast as a set of {@link Annotation} objects.
	 */
	public ArrayList<Annotation> getAnnotationList() {
		ArrayList<Annotation> list = new  ArrayList<Annotation>();
		for (Metadata m : Metadata.values()) {
			if (hasAnnotationValue(m)) {
				list.add(getAsAnnotation(m));
			}
		}
		return list;
	 }
}