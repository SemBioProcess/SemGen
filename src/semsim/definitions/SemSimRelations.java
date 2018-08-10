package semsim.definitions;

import java.net.URI;

import org.sbml.jsbml.CVTerm.Qualifier;
import org.semanticweb.owlapi.model.IRI;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import semsim.annotation.Relation;

/**
 * Constants and functions for working with relations (AKA predicates or qualifiers)
 * in SemSim models
 * @author mneal
 *
 */
public class SemSimRelations {

	/**
	 * @param q A BioModels.net biological qualifier
	 * @return The {@link Relation} corresponding to the qualifier
	 */
	public static Relation getRelationFromBiologicalQualifier(Qualifier q) {
		switch (q) {
		case BQB_ENCODES:
			return SemSimRelation.BQB_ENCODES;
		case BQB_HAS_PART:
			return StructuralRelation.HAS_PART; // Maybe this should be BQB_HAS_PART?
		case BQB_HAS_PROPERTY:
			return SemSimRelation.BQB_HAS_PROPERTY;
		case BQB_HAS_VERSION:
			return SemSimRelation.BQB_HAS_VERSION;
		case BQB_HAS_TAXON:
			return SemSimRelation.BQB_HAS_TAXON;
		case BQB_IS:
			return SemSimRelation.BQB_IS;
		case BQB_IS_DESCRIBED_BY:
			return SemSimRelation.BQB_IS_DESCRIBED_BY;
		case BQB_IS_ENCODED_BY:
			return SemSimRelation.BQB_IS_ENCODED_BY;
		case BQB_IS_HOMOLOG_TO:
			return SemSimRelation.BQB_IS_HOMOLOG_TO;
		case BQB_IS_PROPERTY_OF:
			return SemSimRelation.BQB_IS_PROPERTY_OF;
		case BQB_IS_VERSION_OF:
			return SemSimRelation.BQB_IS_VERSION_OF;
		case BQB_OCCURS_IN:
			return SemSimRelation.BQB_OCCURS_IN;
		case BQB_IS_PART_OF:
			return StructuralRelation.PART_OF;	 // Maybe this should be BQB_PART_OF?
		default:
			return null;
		}
	}
	
	
	/**
	 * @param r A {@link Relation}
	 * @return The BioModels.net biological qualifier corresponding to the relation
	 */
	public static Qualifier getBiologicalQualifierFromRelation(Relation r) {
		
		if(r == SemSimRelation.BQB_IS || r == SemSimRelation.HAS_PHYSICAL_DEFINITION)
			return Qualifier.BQB_IS;
		
		else if(r == StructuralRelation.BQB_HAS_PART || r == StructuralRelation.HAS_PART)
			return Qualifier.BQB_HAS_PART;
		
		else if(r == StructuralRelation.BQB_IS_PART_OF || r == StructuralRelation.PART_OF)
			return Qualifier.BQB_IS_PART_OF;
		
		else if(r == SemSimRelation.BQB_IS_VERSION_OF)
			return Qualifier.BQB_IS_VERSION_OF;
		
		else if(r == SemSimRelation.BQB_OCCURS_IN)
			return Qualifier.BQB_OCCURS_IN;
		
		else if(r == SemSimRelation.BQB_IS_PROPERTY_OF || r == SemSimRelation.PHYSICAL_PROPERTY_OF)
			return Qualifier.BQB_IS_PROPERTY_OF;
		
		else if(r == SemSimRelation.BQB_HAS_TAXON)
			return Qualifier.BQB_HAS_TAXON;
		
		else if(r == SemSimRelation.BQB_HAS_PROPERTY)
			return Qualifier.BQB_HAS_PROPERTY;
		
		else if(r == SemSimRelation.BQB_ENCODES)
			return Qualifier.BQB_ENCODES;
		
		else if(r == SemSimRelation.BQB_IS_ENCODED_BY)
			return Qualifier.BQB_IS_ENCODED_BY;
		
		else if(r == SemSimRelation.BQB_HAS_VERSION)
			return Qualifier.BQB_HAS_VERSION;
		
		else if(r == SemSimRelation.BQB_IS_HOMOLOG_TO)
			return Qualifier.BQB_IS_HOMOLOG_TO;
		
		else if(r == SemSimRelation.BQB_IS_DESCRIBED_BY)
			return Qualifier.BQB_IS_DESCRIBED_BY;
		
		else if(r == SemSimRelation.BQB_OCCURS_IN)
			return Qualifier.BQB_OCCURS_IN;
		
		else return null;
		
	}
	
	/**
	 * @param id A BioModels.net model qualifier ID
	 * @return The {@link Relation} corresponding to the qualifier
	 */
	public static Relation getRelationFromModelQualifier(Qualifier id) {
		switch (id) {
		case BQM_IS:
			return SemSimRelation.BQM_IS;
		case BQM_IS_DESCRIBED_BY:
			return SemSimRelation.BQM_IS_DESCRIBED_BY;
		case BQM_IS_DERIVED_FROM:
			return SemSimRelation.BQM_IS_DERIVED_FROM;
		case BQM_IS_INSTANCE_OF:
			return SemSimRelation.BQM_IS_INSTANCE_OF;
		case BQM_HAS_INSTANCE:
			return SemSimRelation.BQM_HAS_INSTANCE;
		default:
			return null;
		}
	}
	
	
	/**
	 * @param r A {@link Relation}
	 * @return The BioModels.net model qualifier corresponding to the relation
	 */
	public static Qualifier getModelQualifierFromRelation(Relation r) {
		
		if(r == SemSimRelation.BQM_IS)
			return Qualifier.BQM_IS;
		
		else if(r == SemSimRelation.BQM_IS_DERIVED_FROM)
			return Qualifier.BQM_IS_DERIVED_FROM;
		
		else if(r == SemSimRelation.BQM_IS_DESCRIBED_BY)
			return Qualifier.BQM_IS_DESCRIBED_BY;
		
		else if (r == SemSimRelation.BQM_HAS_INSTANCE)
			return Qualifier.BQM_HAS_INSTANCE;
		
		else if (r == SemSimRelation.BQM_IS_INSTANCE_OF)
			return Qualifier.BQM_IS_INSTANCE_OF;
		
		else return null;
		
	}
	
	
	
	
	/**
	 * @param rel A {@link StructuralRelation}
	 * @return The logical inverse of the StructuralRelation
	 */
	public static StructuralRelation getInverseStructuralRelation(StructuralRelation rel) {
		switch (rel) {
		case PART_OF:
			return StructuralRelation.HAS_PART;
		case HAS_PART:
			return StructuralRelation.PART_OF;
		case CONTAINED_IN:
			return StructuralRelation.CONTAINS;
		case CONTAINS:
			return StructuralRelation.CONTAINED_IN;
		case INDEX_ENTITY_FOR:
			return StructuralRelation.HAS_INDEX_ENTITY;
		case HAS_INDEX_ENTITY:
			return StructuralRelation.INDEX_ENTITY_FOR;
		case ADJACENT:
			return StructuralRelation.ADJACENT;
		case BQB_IS_PART_OF:
			return StructuralRelation.BQB_HAS_PART;
		case BQB_HAS_PART:
			return StructuralRelation.BQB_IS_PART_OF;
	}

		return null;
	}
	
	/**
	 * Look up a URI's corresponding {@link SemSimRelation}
	 * 
	 * @param uri The URI key
	 * @return The SemSimRelation value for the URI key or else null if not found
	 */
	public static Relation getRelationFromURI(URI uri){
		for (SemSimRelation rel : SemSimRelation.values()) {
			if (rel.getURI().equals(uri)) return rel;
		}
		for (StructuralRelation rel : StructuralRelation.values()) {
			if (rel.getURI().equals(uri)) return rel;
		}
		return SemSimRelation.UNKNOWN;
	}
	
	public enum SemSimRelation implements Relation {
		//Model level Relations
		
		// Separate relation for model-level metadata ID needed b/c OWL Annotation properties used to annotate the ontology as a whole are disjoint 
		// from Datatype properties used to annotate, say, a data structure in a model
		MODEL_METADATA_ID("modelMetadataID", RDFNamespace.SEMSIM.getNamespaceAsString(), "a semsim model has some metadata id (to support SBML and CellML metadata IDs)", RDFNamespace.SEMSIM.getOWLid()),
		MODEL_NAME("modelName", RDFNamespace.SEMSIM.getNamespaceAsString(), "a semsim model has some name", RDFNamespace.SEMSIM.getOWLid()),
		MODEL_DESCRIPTION("modelDescription", RDFNamespace.SEMSIM.getNamespaceAsString(), "a semsim model has some description", RDFNamespace.SEMSIM.getOWLid()),
		
		HAS_NAME("name", RDFNamespace.SEMSIM.getNamespaceAsString(), "semsim component has name", RDFNamespace.SEMSIM.getOWLid()),
		KEY_TERM("keyTerm", RDFNamespace.SEMSIM.getNamespaceAsString(), "semsim model represents", RDFNamespace.SEMSIM.getOWLid()),
		
		//CellML Specific Relations:
		CELLML_COMPONENT_SUBSUMPTION_TYPE("submodelSubsumptionType", RDFNamespace.SEMSIM.getNamespaceAsString(), "The type of relation between a parent and child component (either containment, encapsulation or a custom term)", RDFNamespace.SEMSIM.getOWLid()),
		CELLML_RDF_MARKUP("hasCellMLrdfMarkup", RDFNamespace.SEMSIM.getNamespaceAsString(), "CellML RDF markup documentation", RDFNamespace.SEMSIM.getOWLid()),
		CELLML_DOCUMENTATION("hasCellMLdocumentation",RDFNamespace.SEMSIM.getNamespaceAsString(), "CellML curatorial documentation", RDFNamespace.SEMSIM.getOWLid()),
		CELLML_COMPONENT_PRIVATE_INTERFACE("hasCellMLprivateInterface",RDFNamespace.SEMSIM.getNamespaceAsString(), "A variable in a CellML component has a private interface specification", RDFNamespace.SEMSIM.getOWLid()),
		CELLML_COMPONENT_PUBLIC_INTERFACE("hasCellMLpublicInterface", RDFNamespace.SEMSIM.getNamespaceAsString(), "A variable in a CellML component has a public interface specification", RDFNamespace.SEMSIM.getOWLid()),
		MAPPED_FROM("mappedFrom", RDFNamespace.SEMSIM.getNamespaceAsString(), "Data structure value passed from another data structure", RDFNamespace.SEMSIM.getOWLid()),
		MAPPED_TO("mappedTo", RDFNamespace.SEMSIM.getNamespaceAsString(), "Data structure value passed to another data structure", RDFNamespace.SEMSIM.getOWLid()),
		CELLML_INITIAL_VALUE("hasCellMLinitialValue",RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		
		//Collection Relations
		INCLUDES_SUBMODEL("includesSubmodel",RDFNamespace.SEMSIM.getNamespaceAsString(), "a submodel encompasses another submodel", RDFNamespace.SEMSIM.getOWLid()),
		ENCAPSULATES("encapsulates", RDFNamespace.SEMSIM.getNamespaceAsString(), "A submodel encapsulates another", RDFNamespace.SEMSIM.getOWLid()),
		
		//Computational Relations
		HAS_COMPUTATIONAL_COMPONENT("hasComputationalComponent", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical property has a data structure as a computational component", RDFNamespace.SEMSIM.getOWLid()),
		IS_COMPUTATIONAL_COMPONENT_FOR("isComputationalComponentFor", RDFNamespace.SEMSIM.getNamespaceAsString(), "data structure is computational component for some property", RDFNamespace.SEMSIM.getOWLid()),
		COMPUTATIONAL_REPRESENTATION_OF("computationalRepresentationOf", RDFNamespace.SEMSIM.getNamespaceAsString(), "a data structure or submodel represents something from the real world", RDFNamespace.SEMSIM.getOWLid()),
		HAS_MATHML("hasMathML", RDFNamespace.SEMSIM.getNamespaceAsString(), "MathML for computation", RDFNamespace.SEMSIM.getOWLid()),
		HAS_COMPUTATIONAL_CODE("hasComputationalCode", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		IS_OUTPUT_FOR("isOutputFor", RDFNamespace.SEMSIM.getNamespaceAsString(), "data structure is output for computation", RDFNamespace.SEMSIM.getOWLid()),
		IS_INPUT_FOR("isInputFor", RDFNamespace.SEMSIM.getNamespaceAsString(), "data structure is input for computation", RDFNamespace.SEMSIM.getOWLid()),
		HAS_ASSOCIATED_DATA_STRUCTURE("hasAssociatedDataStructure", RDFNamespace.SEMSIM.getNamespaceAsString(), "submodel includes data structure", RDFNamespace.SEMSIM.getOWLid()),		
		HAS_TRIGGER_MATHML("hasTriggerMathML", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_EVENT("hasEvent", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_EVENT_ASSIGNMENT("hasEventAssignment", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_PRIORITY_MATHML("hasPriorityMathML", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_DELAY_MATHML("hasDelayMathML", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_TIME_UNIT("hasTimeUnit", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_INPUT("hasInput", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		IS_DECLARED ("isDeclared", RDFNamespace.SEMSIM.getNamespaceAsString(), "declaration status of data structure", RDFNamespace.SEMSIM.getOWLid()),
		IS_DETERMINED_BY ("isDeterminedBy", RDFNamespace.SEMSIM.getNamespaceAsString(), "connects a physical property to the physical dependency that determines it", RDFNamespace.SEMSIM.getOWLid()),
		IS_DISCRETE("isDiscrete", RDFNamespace.SEMSIM.getNamespaceAsString(), "discrete/continuous status of data structure", RDFNamespace.SEMSIM.getOWLid()),
		IS_FUNDAMENTAL_UNIT("isFundamentalUnit", RDFNamespace.SEMSIM.getNamespaceAsString(), "if true, identifies a custom unit not derived from another unit", RDFNamespace.SEMSIM.getOWLid()),
		IS_SOLUTION_DOMAIN ("isSolutionDomain", RDFNamespace.SEMSIM.getNamespaceAsString(), "is data structure a solution domain", RDFNamespace.SEMSIM.getOWLid()),
		HAS_SOLUTION_DOMAIN ("hasSolutionDomain", RDFNamespace.SEMSIM.getNamespaceAsString(), "data structure solved within solution domain", RDFNamespace.SEMSIM.getOWLid()),
		HAS_START_VALUE ("hasStartValue", RDFNamespace.SEMSIM.getNamespaceAsString(), "data structure has initial value", RDFNamespace.SEMSIM.getOWLid()),
		HAS_OUTPUT ("hasOutput", RDFNamespace.SEMSIM.getNamespaceAsString(), "computation has data structure as output", RDFNamespace.SEMSIM.getOWLid()),
		HAS_UNIT_FACTOR ("hasUnitFactor", RDFNamespace.SEMSIM.getNamespaceAsString(), "a unit is derived from some other unit", RDFNamespace.SEMSIM.getOWLid()),
		UNIT_FACTOR_EXPONENT("hasUnitFactorExponent", RDFNamespace.SEMSIM.getNamespaceAsString(), "the exponent applied to a unit factor", RDFNamespace.SEMSIM.getOWLid()),
		UNIT_FACTOR_FOR("unitFactorFor", RDFNamespace.SEMSIM.getNamespaceAsString(), "a unit is used to derive another unit", RDFNamespace.SEMSIM.getOWLid()),
		UNIT_FACTOR_PREFIX("hasUnitFactorPrefix", RDFNamespace.SEMSIM.getNamespaceAsString(), "the prefix applied to a unit factor", RDFNamespace.SEMSIM.getOWLid()),
		UNIT_FACTOR_MULTIPLIER("hasUnitFactorMultiplier", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		
		//Process participant relations0
		HAS_SOURCE("hasSource", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical process or force has thermodynamic source entity", RDFNamespace.SEMSIM.getOWLid()),
		HAS_SINK("hasSink", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical process or force has thermodynamic sink entity", RDFNamespace.SEMSIM.getOWLid()),
		HAS_MEDIATOR("hasMediator", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical process has thermodynamic mediator entity", RDFNamespace.SEMSIM.getOWLid()),
		HAS_SOURCE_PARTICIPANT("hasSourceParticipant", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_SINK_PARTICIPANT("hasSinkParticipant", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_MEDIATOR_PARTICIPANT("hasMediatorParticipant", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_MULTIPLIER("hasMultiplier", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		
		DETERMINES("determines", RDFNamespace.SEMSIM.getNamespaceAsString(), "Connects a physical dependency to the physical property it determines", RDFNamespace.SEMSIM.getOWLid()),
	
		HREF_VALUE_OF_IMPORT("hrefValueOfImport", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		IMPORTED_FROM("importedFrom", RDFNamespace.SEMSIM.getNamespaceAsString(), "a unit or submodel is imported from a local file or remote location", RDFNamespace.SEMSIM.getOWLid()),
		REFERENCE_NAME_OF_IMPORT("referenceNameOfImport", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		METADATA_ID("metadataID", RDFNamespace.SEMSIM.getNamespaceAsString(), "a semsim model component has some metadata id (to support SBML and CellML metadata IDs)", RDFNamespace.SEMSIM.getOWLid()),
		
		HAS_PHYSICAL_DEFINITION("hasPhysicalDefinition", RDFNamespace.SEMSIM.getNamespaceAsString(), "Refers to ontology term", RDFNamespace.SEMSIM.getOWLid()),
		HAS_PHYSICAL_ENTITY_REFERENCE("hasPhysicalEntityReference", RDFNamespace.SEMSIM.getNamespaceAsString(), "", RDFNamespace.SEMSIM.getOWLid()),
		HAS_PHYSICAL_PROPERTY("hasPhysicalProperty", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical property of an entity or process", RDFNamespace.SEMSIM.getOWLid()),
		PHYSICAL_PROPERTY_OF("physicalPropertyOf", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical entity or process associated with a property", RDFNamespace.SEMSIM.getOWLid()),
		HAS_UNIT("hasUnit", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical property has physical units", RDFNamespace.SEMSIM.getOWLid()),
		UNIT_FOR("unitFor", RDFNamespace.SEMSIM.getNamespaceAsString(), "physical units for a property", RDFNamespace.SEMSIM.getOWLid()),
		
		//BioModels qualifiers
		BQB_ENCODES("encodes", RDFNamespace.BQB.getNamespaceAsString(),"The biological entity represented by the model element encodes, directly or transitively, the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_HAS_PROPERTY("hasProperty",RDFNamespace.BQB.getNamespaceAsString(),"The subject of the referenced resource is a property of the biological entity represented by the model element", RDFNamespace.BQB.getOWLid()),
		BQB_HAS_VERSION("hasVersion",RDFNamespace.BQB.getNamespaceAsString(),"The subject of the referenced resource (biological entity B) is a version or an instance of the biological entity represented by the model element", RDFNamespace.BQB.getOWLid()),
		BQB_IS("is", RDFNamespace.BQB.getNamespaceAsString(), "The biological entity represented by the model element has identity with the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_IS_DESCRIBED_BY("isDescribedBy", RDFNamespace.BQB.getNamespaceAsString(), "The biological entity represented by the model element is described by the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_IS_ENCODED_BY("isEncodedBy", RDFNamespace.BQB.getNamespaceAsString(), "The biological entity represented by the model element is encoded, directly or transitively, by the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_IS_HOMOLOG_TO("isHomologTo", RDFNamespace.BQB.getNamespaceAsString(), "The biological entity represented by the model element is homologous to the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_IS_PROPERTY_OF("isPropertyOf", RDFNamespace.BQB.getNamespaceAsString(), "The model element is a physical property of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_IS_VERSION_OF("isVersionOf", RDFNamespace.BQB.getNamespaceAsString(), "The biological entity represented by the model element is a version or an instance of the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_HAS_TAXON("hasTaxon", RDFNamespace.BQB.getNamespaceAsString(), "The biological entity represented by the model element is taxonomically restricted, where the restriction is the subject of the referenced resource", RDFNamespace.BQB.getOWLid()),
		BQB_OCCURS_IN("occursIn", RDFNamespace.BQB.getNamespaceAsString(), "Model processes occur in some taxon", RDFNamespace.BQB.getOWLid()),

		BQM_IS("is", RDFNamespace.BQM.getNamespaceAsString(), "The modelling object represented by the model element is identical with the subject of the referenced resource", RDFNamespace.BQM.getOWLid()),
		BQM_IS_DESCRIBED_BY("isDescribedBy", RDFNamespace.BQM.getNamespaceAsString(), "The modelling object represented by the model element is described by the subject of the referenced resource", RDFNamespace.BQM.getOWLid()),
		BQM_IS_DERIVED_FROM("isDerivedFrom", RDFNamespace.BQM.getNamespaceAsString(), "The modelling object represented by the model element is derived from the modelling object represented by the referenced resource", RDFNamespace.BQM.getOWLid()),
		BQM_HAS_INSTANCE("hasInstance", RDFNamespace.BQM.getNamespaceAsString(), "The modelling object represented by the model element has for instance (is a class of) the subject of the referenced resource (modelling object B). For instance, this qualifier might be used to link a generic model with its specific forms.", RDFNamespace.BQM.getOWLid()),
		BQM_IS_INSTANCE_OF("isInstanceOf", RDFNamespace.BQM.getNamespaceAsString(), "The modelling object represented by the model element is an instance of the subject of the referenced resource (modelling object B). For instance, this qualifier might be used to link a specific model with its generic form.", RDFNamespace.BQM.getOWLid()),
			
		UNKNOWN("unknown", RDFNamespace.SEMSIM.getNamespaceAsString(), "Unrecognized relation", RDFNamespace.SEMSIM.getOWLid());
		
		private String name;
		private String uri;
		private String description;
		private String sparqlcode;
	
	/** Class constructor */
		SemSimRelation(String name, String namespace, String desc, String owlid) {
			this.name = name;
			this.uri = namespace + name;
			description = desc;
			sparqlcode = owlid + ":" + name;
		}

		/** @return Get the name of the SemSimRelation */
		public String getName() {
			return name;
		}
		
		/** @return The URI of the relation */
		public URI getURI() {
			return URI.create(uri);
		}
		
		/** @return The URI of the relation as a string */
		public String getURIasString() {
			return uri;
		}
		
		/** @return The relation's specified description */
		public String getDescription() {
			return description;
		}
		
		/** @return Namespace of relation for use in SPARQL queries */
		public String getSPARQLCode() {
			return sparqlcode;
		}
		
		/** @return The URI for the relation converted into an IRI */
		public IRI getIRI() {
			return IRI.create(uri);
		}
		
		/** @return The relation as an RDF property */
		public Property getRDFproperty(){
			return ResourceFactory.createProperty(getURIasString());
		}
	
	}
	
	/** A type of SemSimRelation for establishing structural relationships between
	 * SemSim physical entities. */

	// A structural relationship between two physical entities
	public enum StructuralRelation implements Relation {
		PART_OF("part of", "physical entity is part of another physical entity", 
				RDFNamespace.RO.getNamespaceAsString() + "part_of", "ro:part_of"),
		HAS_PART("has part", "physical entity has part other physical entity",
				RDFNamespace.RO.getNamespaceAsString() + "has_part", "ro:has_part"),
		CONTAINED_IN("contained in", "physical entity is contained in another physical entity",
				RDFNamespace.RO.getNamespaceAsString() + "contained_in", "ro:contained_in"),
		CONTAINS("contains", "physical entity contains another physical entity",
				RDFNamespace.RO.getNamespaceAsString() + "contains", "ro:contains"),
		ADJACENT("adjacent to", "physical entity is adjacent to another physical entity",
				RDFNamespace.SEMSIM.getNamespaceAsString() + "adjacentTo", "semsim:adjacent_to"),
		INDEX_ENTITY_FOR("index entity for", "physical entity is index entity for another physical entity", 
				RDFNamespace.SEMSIM.getNamespaceAsString() + "isIndexEntityFor", "semsim:isIndexEntityFor"),
		HAS_INDEX_ENTITY("has index entity", "physical entity has entity as its index entity",
				RDFNamespace.SEMSIM.getNamespaceAsString() + "hasIndexEntity", "semsim:hasIndexEntity"),
		BQB_HAS_PART("has part", "physical entity has part other another physical entity",
				RDFNamespace.BQB.getNamespaceAsString() + "hasPart", "bqbiol:hasPart"),
		BQB_IS_PART_OF("part of", "physical entity is part of another physical entity",
				RDFNamespace.BQB.getNamespaceAsString() + "isPartOf", "bqbiol:isPartOf");
		
		
		String description;
		String shortdesc;
		String sparqlcode;
		String uri;
		
		private StructuralRelation(String sdesc, String desc, String u, String sparql) {
			shortdesc = sdesc;
			description = desc;
			uri = u;
			sparqlcode = sparql;
		}
		
		public String getDescription() {
			return description;
		}
		
		public URI getURI() {
			return URI.create(uri);
		}
		
		public String getURIasString() {
			return uri;
		}
		
		@Override
		public String getName() {
			return shortdesc;
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
	
}
