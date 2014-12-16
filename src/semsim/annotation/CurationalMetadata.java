package semsim.annotation;

import java.net.URI;
import java.util.ArrayList;

import semsim.SemSimConstants;

public class CurationalMetadata {
	public static final String DCTERMS_NAMESPACE = "http://purl.org/dc/terms/";
	public static final String DCTERMS_NAMESPACE_11 = "http://purl.org/dc/elements/1.1/";
	private static String SEMSIM_NAMESPACE = SemSimConstants.SEMSIM_NAMESPACE;
	
	public static final URI MODEL_NAME_URI = URI.create(SEMSIM_NAMESPACE + "modelName");
	public static final URI MODEL_DESCRIPTION_URI = URI.create(SEMSIM_NAMESPACE + "ModelDescription");
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
	public static final SemSimRelation MODEL_ID_RELATION = new SemSimRelation("the ID of the model from which the SemSim model was generated", MODEL_ID_URI);
	public static final SemSimRelation MODEL_NAME_RELATION = new SemSimRelation("a human-readable name for the model", MODEL_NAME_URI);
	public static final SemSimRelation MODEL_DESCRIPTION_RELATION = new SemSimRelation("a free-text description of the model", MODEL_DESCRIPTION_URI);	
	public static final SemSimRelation ANNOTATOR_NAME_RELATION = new SemSimRelation("who to contact about the annotations in the model", ANNOTATOR_NAME_URI);	
	public static final SemSimRelation ANNOTATOR_CONTACT_RELATION = new SemSimRelation("email address of annotator", ANNOTATOR_CONTACT_INFO_URI);
	public static final SemSimRelation MODELER_NAME_RELATION = new SemSimRelation("who to contact about the model", MODELER_NAME_URI);	
	public static final SemSimRelation MODELER_CONTACT_RELATION = new SemSimRelation("email address of modeler", MODELER_CONTACT_INFO_URI);	
	
	public static final SemSimRelation REFERENCE_PUBLICATION_PUBMED_ID_RELATION = new SemSimRelation("the PubMed ID of the model's reference publication", REFERENCE_PUBLICATION_PUBMED_ID_URI);
	public static final SemSimRelation REFERENCE_PUBLICATION_ABSTRACT_TEXT_RELATION = new SemSimRelation("the abstract text of the model's reference publication", REFERENCE_PUBLICATION_ABSTRACT_TEXT_URI);
	public static final SemSimRelation REFERENCE_PUBLICATION_CITATION_RELATION = new SemSimRelation("the citation for the reference publication", REFERENCE_PUBLICATION_CITATION_URI);
	
	public static final SemSimRelation MATLAB_URL_RELATION = new SemSimRelation("the URL for the Matlab version of the model", MATLAB_URL_URI);
	public static final SemSimRelation CELLML_URL_RELATION = new SemSimRelation("the URL for the CellML version of the model", CELLML_URL_URI);
	public static final SemSimRelation SBML_URL_RELATION = new SemSimRelation("the URL for the SBML version of the model", SBML_URL_URI);
	public static final SemSimRelation JSIM_URL_RELATION = new SemSimRelation("the URL for the JSim version of the model", JSIM_URL_URI);
	
	public enum Metadata {
		fullname("Full Name", MODEL_NAME_RELATION),
		description("Description", MODEL_DESCRIPTION_RELATION),
		annotatorauthor("Annotator Name", ANNOTATOR_NAME_RELATION),
		annotatorcontact("Annotator Contact", ANNOTATOR_CONTACT_RELATION),
		modelauthor("Model Author", MODELER_NAME_RELATION),
		modelcontact("Model Contact", MODELER_CONTACT_RELATION),
		sourcemodelid("Source Model ID", MODEL_ID_RELATION),
		cellmlurl("CellML URL",CELLML_URL_RELATION),
		matlaburl("Matlab URL",MATLAB_URL_RELATION),
		mmlurl("JSim URL",JSIM_URL_RELATION),
		sbmlurl("SBML URL",SBML_URL_RELATION);
		
		private final String text;
		private final SemSimRelation relation;
		private String value = "";
	
		private Metadata(final String text, SemSimRelation rel) {
		   this.text = text;
		   relation = rel;
		}
	
		@Override
		public String toString() {
		   return text;
		}
		 
		private void setValue(String val) {
			value = val;
		 }
		 private String getValue() {
			 return value;
		 }
		 private boolean hasValue() {
			 return !value.isEmpty();
		 }
		 private SemSimRelation getRelation() {
			return relation; 
		 }
		 
		 public Annotation getAsAnnotation() {
			 return new Annotation(relation, value);
		 }
		 
		 private boolean equals(String valuetomatch) {
			 return value.equals(valuetomatch);
		 }
	}
	

	
	public String getAnnotationName(Metadata item) {
		return item.toString();
	}
	
	public String getAnnotationValue(Metadata item) {
		return item.getValue();
	}
	
	public void setAnnotationValue(Metadata item, String value) {
		 item.setValue(value);
	}
	
	public boolean hasAnnotationValue(Metadata item) {
		return item.hasValue();
	}
	
	public ArrayList<Annotation> createAnnotations() {
		ArrayList<Annotation> list = new ArrayList<Annotation>();
		
		
		
		return list;
	}
	
	public boolean isItemValueEqualto(Metadata item, String value) {
		return item.equals(value);
	}
	
	public SemSimRelation getRelation(Metadata item) {
		return item.getRelation();
	}
}


