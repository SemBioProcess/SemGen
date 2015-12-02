package semsim.definitions;


import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of constants for working with SemSim models
 */
public class SemSimConstants {
	
	// Full names of ontologies & knowledge bases
	public static final String BRAUNSCHWEIG_ENZYME_DATABASE_FULLNAME = "Braunschweig Enzyme Database";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Compound";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Drug";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Genes";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Orthology";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Pathway";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Reaction";
	public static final String UNIPROT_FULLNAME = "Universal Protein Resource";
			
	// URIs
	public static final URI CELLML_COMPONENT_SUBSUMPTION_TYPE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "submodelSubsumptionType");
	public static final URI DETERMINES_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "determines");
	public static final URI ENCAPSULATES_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "encapsulates");
	public static final URI HAS_SOURCE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasSource");
	public static final URI HAS_SINK_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasSink");
	public static final URI HAS_MEDIATOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasMediator");
	public static final URI HAS_SOURCE_PARTICIPANT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasSourceParticipant");
	public static final URI HAS_SINK_PARTICIPANT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasSinkParticipant");
	public static final URI HAS_MEDIATOR_PARTICIPANT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasMediatorParticipant");
	public static final URI HAS_PHYSICAL_ENTITY_REFERENCE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasPhysicalEntityReference");
	
	public static final URI HAS_COMPUTATIONAL_CODE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasComputationalCode");
	public static final URI HAS_COMPUTATATIONAL_COMPONENT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasComputationalComponent");
	public static final URI HAS_MATHML_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasMathML");
	public static final URI HAS_TRIGGER_MATHML_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasTriggerMathML");
	public static final URI HAS_EVENT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasEvent");
	public static final URI HAS_EVENT_ASSIGNMENT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasEventAssignment");
	public static final URI HAS_PRIORITY_MATHML_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasPriorityMathML");
	public static final URI HAS_DELAY_MATHML_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasDelayMathML");
	public static final URI HAS_TIME_UNIT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasTimeUnit");
	public static final URI HAS_SOLUTION_DOMAIN_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasSolutionDomain");
	public static final URI HAS_START_VALUE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasStartValue");
	public static final URI HAS_INPUT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasInput");
	public static final URI HAS_UNIT_FACTOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasUnitFactor");
	public static final URI HAS_OUTPUT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasOutput");
	public static final URI HREF_VALUE_OF_IMPORT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hrefValueOfImport");
	public static final URI IMPORTED_FROM_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "importedFrom");
	public static final URI IS_COMPUTATIONAL_COMPONENT_FOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isComputationalComponentFor");
	public static final URI IS_OUTPUT_FOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isOutputFor");
	public static final URI IS_INPUT_FOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isInputFor");
	public static final URI IS_DECLARED_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isDeclared");
	public static final URI IS_DETERMINED_BY_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isDeterminedBy");
	public static final URI IS_DISCRETE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isDiscrete");
	public static final URI IS_FUNDAMENTAL_UNIT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isFundamentalUnit");
	public static final URI IS_SOLUTION_DOMAIN_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isSolutionDomain");
	public static final URI MAPPED_FROM_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "mappedFrom");
	public static final URI MAPPED_TO_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "mappedTo");
	public static final URI METADATA_ID_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "metadataID");
	public static final URI HAS_PHYSICAL_DEFINITION_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasPhysicalDefinition");
	public static final URI REFERENCE_NAME_OF_IMPORT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "referenceNameOfImport");

	public static final URI HAS_UNIT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasUnit");
	public static final URI UNIT_FOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "unitFor");
	public static final URI HAS_ASSOCIATED_DATA_STRUCTURE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasAssociatedDataStructure");
	public static final URI PHYSICAL_PROPERTY_OF_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "physicalPropertyOf");
	public static final URI HAS_PHYSICAL_PROPERTY_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasPhysicalProperty");
	public static final URI PART_OF_URI = URI.create(RDFNamespace.RO.getNamespace() + "part_of");
	public static final URI HAS_PART_URI = URI.create(RDFNamespace.RO.getNamespace() + "has_part");
	public static final URI CONTAINED_IN_URI = URI.create(RDFNamespace.RO.getNamespace() + "contained_in");
	public static final URI CONTAINS_URI = URI.create(RDFNamespace.RO.getNamespace() + "contains");
	public static final URI HAS_INDEX_ENTITY_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasIndexEntity");
	public static final URI INDEX_ENTITY_FOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "isIndexEntityFor");
	public static final URI INCLUDES_SUBMODEL_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "includesSubmodel");
	public static final URI COMPUTATIONAL_REPRESENTATION_OF_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "computationalRepresentationOf");
	public static final URI HAS_MULTIPLIER_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasMultiplier");
	public static final URI UNIT_FACTOR_EXPONENT_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasUnitFactorExponent");
	public static final URI UNIT_FACTOR_FOR_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "unitFactorFor");
	public static final URI UNIT_FACTOR_PREFIX_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasUnitFactorPrefix");
	public static final URI UNIT_FACTOR_MULTIPLIER_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasUnitFactorMultiplier");
	
	// Model-level relations	
	public static final URI BQB_HAS_PART_URI = URI.create(RDFNamespace.BQB.getNamespace() + "hasPart");
	public static final URI BQB_IS_PART_OF_URI = URI.create(RDFNamespace.BQB.getNamespace() + "isPartOf");
	public static final URI BQB_IS_URI = URI.create(RDFNamespace.BQB.getNamespace() + "is");
	public static final URI BQB_IS_VERSION_OF_URI = URI.create(RDFNamespace.BQB.getNamespace() + "isVersionOf");
	public static final URI BQB_OCCURS_IN_URI = URI.create(RDFNamespace.BQB.getNamespace() + "occursIn");
	public static final URI BQM_IS_URI = URI.create(RDFNamespace.BQM.getNamespace() + "is");
	public static final URI BQM_IS_DESCRIBED_BY_URI = URI.create(RDFNamespace.BQM.getNamespace() + "isDescribedBy");
	public static final URI BQM_IS_DERIVED_FROM_URI = URI.create(RDFNamespace.BQM.getNamespace() + "isDerivedFrom");
	
	public static final URI HAS_NOTES_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasNotes");

	public static final URI CELLML_INITIAL_VALUE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasCellMLinitialValue");
	public static final URI CELLML_COMPONENT_PRIVATE_INTERFACE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasCellMLprivateInterface");
	public static final URI CELLML_COMPONENT_PUBLIC_INTERFACE_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasCellMLpublicInterface");
	public static final URI CELLML_DOCUMENTATION_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasCellMLdocumentation");
	public static final URI CELLML_RDF_MARKUP_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "hasCellMLrdfMarkup");
	public static final URI HAS_NAME_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "name");
	public static final URI KEY_TERM_URI = URI.create(RDFNamespace.SEMSIM.getNamespace() + "keyTerm");

	// Relations
	public static final SemSimRelation BQB_HAS_PART_RELATION = new SemSimRelation("The biological entity represented by the model element includes the subject of the referenced resource, either physically or logically", BQB_HAS_PART_URI);
	public static final SemSimRelation BQB_IS_RELATION = new SemSimRelation("The biological entity represented by the model element has identity with the subject of the referenced resource", BQB_IS_URI);
	public static final SemSimRelation BQB_IS_PART_OF_RELATION = new SemSimRelation("The biological entity represented by the model element is a physical or logical part of the subject of the referenced resource", BQB_IS_PART_OF_URI);
	public static final SemSimRelation BQB_IS_VERSION_OF_RELATION = new SemSimRelation("The biological entity represented by the model element is a version or an instance of the subject of the referenced resource", BQB_IS_VERSION_OF_URI);
	public static final SemSimRelation BQB_OCCURS_IN_RELATION = new SemSimRelation("Model processes occur in some taxon", BQB_OCCURS_IN_URI);
	public static final SemSimRelation BQM_IS_RELATION = new SemSimRelation("The modelling object represented by the model element is identical with the subject of the referenced resource", BQM_IS_URI);
	public static final SemSimRelation BQM_IS_DESCRIBED_BY_RELATION = new SemSimRelation("The modelling object represented by the model element is described by the subject of the referenced resource", BQM_IS_DESCRIBED_BY_URI);
	public static final SemSimRelation BQM_IS_DERIVED_FROM_RELATION = new SemSimRelation("The modelling object represented by the model element is derived from the modelling object represented by the referenced resource", BQM_IS_DERIVED_FROM_URI);
	
	public static final SemSimRelation CELLML_COMPONENT_PRIVATE_INTERFACE_RELATION = new SemSimRelation("A variable in a CellML component has a private interface specification", CELLML_COMPONENT_PRIVATE_INTERFACE_URI);
	public static final SemSimRelation CELLML_COMPONENT_PUBLIC_INTERFACE_RELATION = new SemSimRelation("A variable in a CellML component has a public interface specification", CELLML_COMPONENT_PUBLIC_INTERFACE_URI);
	public static final SemSimRelation CELLML_DOCUMENTATION_RELATION = new SemSimRelation("CellML curatorial documentation", CELLML_DOCUMENTATION_URI);
	public static final SemSimRelation CELLML_RDF_MARKUP_RELATION = new SemSimRelation("CellML RDF markup documentation", CELLML_RDF_MARKUP_URI);
	
	public static final SemSimRelation DETERMINES_RELATION = new SemSimRelation("Connects a physical dependency to the physical property it determines", DETERMINES_URI);
	public static final SemSimRelation ENCAPSULATES_RELATION = new SemSimRelation("A submodel encapsulates another", ENCAPSULATES_URI);
	public static final SemSimRelation HAS_NOTES_RELATION = new SemSimRelation("Model has notes", HAS_NOTES_URI);
	public static final SemSimRelation MAPPED_FROM_RELATION = new SemSimRelation("Data structure value passed from another data structure", MAPPED_FROM_URI);
	public static final SemSimRelation MAPPED_TO_RELATION = new SemSimRelation("Data structure value passed to another data structure", MAPPED_TO_URI);
	public static final SemSimRelation CELLML_COMPONENT_SUBSUMPTION_TYPE_RELATION = new SemSimRelation("The type of relation between a parent and child component (either containment, encapsulation or a custom term)", CELLML_COMPONENT_SUBSUMPTION_TYPE_URI);
	
	public static final SemSimRelation HAS_PHYSICAL_DEFINITION_RELATION = new SemSimRelation("Refers to ontology term", HAS_PHYSICAL_DEFINITION_URI);
	public static final SemSimRelation HAS_MATHML_RELATION = new SemSimRelation("MathML for computation", HAS_MATHML_URI);
	public static final SemSimRelation HAS_COMPUTATIONAL_COMPONENT_RELATION = new SemSimRelation("physical property has a data structure as a computational component", HAS_COMPUTATATIONAL_COMPONENT_URI);

	public static final SemSimRelation IMPORTED_FROM_RELATION = new SemSimRelation("a unit or submodel is imported from a local file or remote location", IMPORTED_FROM_URI);
	public static final SemSimRelation IS_OUTPUT_FOR_RELATION = new SemSimRelation("data structure is output for computation", IS_OUTPUT_FOR_URI);
	public static final SemSimRelation IS_INPUT_FOR_RELATION = new SemSimRelation("data structure is input for computation", IS_INPUT_FOR_URI);
	public static final SemSimRelation IS_DECLARED_RELATION = new SemSimRelation("declaration status of data structure", IS_DECLARED_URI);
	public static final SemSimRelation IS_DETERMINED_BY_RELATION = new SemSimRelation("connects a physical property to the physical dependency that determines it", IS_DETERMINED_BY_URI);
	public static final SemSimRelation IS_DISCRETE_RELATION = new SemSimRelation("discrete/continuous status of data structure", IS_DISCRETE_URI);
	public static final SemSimRelation IS_FUNDAMENTAL_UNIT_RELATION = new SemSimRelation("if true, identifies a custom unit not derived from another unit", IS_FUNDAMENTAL_UNIT_URI);
	public static final SemSimRelation IS_SOLUTION_DOMAIN_RELATION = new SemSimRelation("is data structure a solution domain", IS_SOLUTION_DOMAIN_URI);
	public static final SemSimRelation HAS_SOLUTION_DOMAIN_RELATION = new SemSimRelation("data structure solved within solution domain", HAS_SOLUTION_DOMAIN_URI);
	public static final SemSimRelation IS_COMPUTATIONAL_COMPONENT_FOR_RELATION = new SemSimRelation("data structure is computational component for some property", IS_COMPUTATIONAL_COMPONENT_FOR_URI);
	public static final SemSimRelation HAS_START_VALUE_RELATION = new SemSimRelation("data structure has initial value", HAS_START_VALUE_URI);
	public static final SemSimRelation HAS_OUTPUT_RELATION = new SemSimRelation("computation has data structure as output", HAS_OUTPUT_URI);
	public static final SemSimRelation HAS_UNIT_RELATION = new SemSimRelation("physical property has physical units", HAS_UNIT_URI);
	public static final SemSimRelation HAS_UNIT_FACTOR_RELATION = new SemSimRelation("a unit is derived from some other unit", HAS_UNIT_FACTOR_URI);
	public static final SemSimRelation METADATA_ID_RELATION = new SemSimRelation("a semsim model component has some metadata id (to support SBML and CellML metadata IDind)", METADATA_ID_URI);
	public static final SemSimRelation UNIT_FOR_RELATION = new SemSimRelation("physical units for a property", HAS_UNIT_URI);
	public static final SemSimRelation HAS_ASSOCIATED_DATA_STRUCTURE_RELATION = new SemSimRelation("submodel includes data structure", HAS_ASSOCIATED_DATA_STRUCTURE_URI);
	public static final SemSimRelation PHYSICAL_PROPERTY_OF_RELATION = new SemSimRelation("physical entity or process associated with a property ", PHYSICAL_PROPERTY_OF_URI);
	public static final SemSimRelation HAS_PHYSICAL_PROPERTY_RELATION = new SemSimRelation("physical property of an entity or process", HAS_PHYSICAL_PROPERTY_URI);
	public static final StructuralRelation PART_OF_RELATION = new StructuralRelation("physical entity is part of another physical entity", PART_OF_URI);
	public static final StructuralRelation HAS_PART_RELATION = new StructuralRelation("physical entity has part other physical entity", HAS_PART_URI);
	public static final StructuralRelation CONTAINED_IN_RELATION = new StructuralRelation("physical entity is contained in another physical entity", CONTAINED_IN_URI);
	public static final StructuralRelation CONTAINS_RELATION = new StructuralRelation("physical entity contains another physical entity", CONTAINS_URI);
	public static final SemSimRelation HAS_SOURCE_RELATION = new SemSimRelation("physical process has thermodynamic source entity", HAS_SOURCE_URI);
	public static final SemSimRelation HAS_SINK_RELATION = new SemSimRelation("physical process has thermodynamic sink entity", HAS_SINK_URI);
	public static final SemSimRelation HAS_MEDIATOR_RELATION = new SemSimRelation("physical process has thermodynamic mediator entity", HAS_MEDIATOR_URI);
	public static final SemSimRelation INCLUDES_SUBMODEL_RELATION = new SemSimRelation("a submodel encompasses another submodel", INCLUDES_SUBMODEL_URI);
	public static final SemSimRelation COMPUTATIONAL_REPRESENTATION_OF_RELATION = new SemSimRelation("a data structure or submodel represents something from the real world", COMPUTATIONAL_REPRESENTATION_OF_URI);
	
	public static final SemSimRelation HAS_INDEX_ENTITY_RELATION = new SemSimRelation("composite physical entity has index entity", HAS_INDEX_ENTITY_URI);
	public static final SemSimRelation INDEX_ENtity_FOR_RELATION = new SemSimRelation("physical entity is index for composite physical entity", INDEX_ENTITY_FOR_URI);
	public static final SemSimRelation UNIT_FACTOR_EXPONENT_RELATION = new SemSimRelation("the exponent applied to a unit factor", UNIT_FACTOR_EXPONENT_URI);
	public static final SemSimRelation UNIT_FACTOR_FOR_RELATION = new SemSimRelation("a unit is used to derive another unit", UNIT_FACTOR_FOR_URI);
	public static final SemSimRelation UNIT_FACTOR_PREFIX_RELATION = new SemSimRelation("the prefix applied to a unit factor", UNIT_FACTOR_PREFIX_URI);

	public static final SemSimRelation HAS_NAME_RELATION = new SemSimRelation("semsim component has name", HAS_NAME_URI);
	public static final SemSimRelation KEY_TERM_RELATION = new SemSimRelation("semsim model represents", KEY_TERM_URI);
	
	public static final String BIOPORTAL_API_KEY = "c4192e4b-88a8-4002-ad08-b4636c88df1a";

	public static final Map<URI, SemSimRelation> URIS_AND_SEMSIM_RELATIONS;
	public static final Map<URI,URI> INVERSE_STRUCTURAL_RELATIONS_MAP;
	public static final Map<Integer, SemSimRelation> BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS;
	public static final Map<Integer, SemSimRelation> MODEL_QUALIFIER_TYPES_AND_RELATIONS;
	
	static{       
        // URIsAndSemSimRelations Map
        Map<URI,SemSimRelation> aMap0 = new HashMap<URI,SemSimRelation>();
		aMap0.put(CELLML_COMPONENT_SUBSUMPTION_TYPE_URI, CELLML_COMPONENT_SUBSUMPTION_TYPE_RELATION);
		aMap0.put(DETERMINES_URI, DETERMINES_RELATION);
		aMap0.put(ENCAPSULATES_URI, ENCAPSULATES_RELATION);
		aMap0.put(MAPPED_FROM_URI, MAPPED_FROM_RELATION);
		aMap0.put(MAPPED_TO_URI, MAPPED_TO_RELATION);
		aMap0.put(HAS_PHYSICAL_DEFINITION_URI, HAS_PHYSICAL_DEFINITION_RELATION);
		aMap0.put(HAS_UNIT_URI, HAS_UNIT_RELATION);
		aMap0.put(UNIT_FOR_URI, UNIT_FOR_RELATION);
		aMap0.put(HAS_ASSOCIATED_DATA_STRUCTURE_URI, HAS_ASSOCIATED_DATA_STRUCTURE_RELATION);
		aMap0.put(HAS_NAME_URI, HAS_NAME_RELATION);
		aMap0.put(KEY_TERM_URI, KEY_TERM_RELATION);
		aMap0.put(IS_OUTPUT_FOR_URI, IS_OUTPUT_FOR_RELATION);
		aMap0.put(HAS_OUTPUT_URI, HAS_OUTPUT_RELATION);
		aMap0.put(HAS_MATHML_URI, HAS_MATHML_RELATION);
		aMap0.put(HAS_START_VALUE_URI, HAS_START_VALUE_RELATION);
		aMap0.put(HAS_UNIT_FACTOR_URI, HAS_UNIT_FACTOR_RELATION);
		aMap0.put(IMPORTED_FROM_URI, IMPORTED_FROM_RELATION);
		aMap0.put(IS_INPUT_FOR_URI, IS_INPUT_FOR_RELATION);
		aMap0.put(IS_DECLARED_URI, IS_DECLARED_RELATION);
		aMap0.put(IS_DETERMINED_BY_URI, IS_DETERMINED_BY_RELATION);
		aMap0.put(IS_DISCRETE_URI, IS_DISCRETE_RELATION);
		aMap0.put(IS_FUNDAMENTAL_UNIT_URI, IS_FUNDAMENTAL_UNIT_RELATION);
		aMap0.put(IS_SOLUTION_DOMAIN_URI, IS_SOLUTION_DOMAIN_RELATION);
		aMap0.put(HAS_SOLUTION_DOMAIN_URI, HAS_SOLUTION_DOMAIN_RELATION);
		aMap0.put(IS_COMPUTATIONAL_COMPONENT_FOR_URI, IS_COMPUTATIONAL_COMPONENT_FOR_RELATION);
		aMap0.put(METADATA_ID_URI, METADATA_ID_RELATION);
		aMap0.put(PHYSICAL_PROPERTY_OF_URI, PHYSICAL_PROPERTY_OF_RELATION); 
		aMap0.put(HAS_PHYSICAL_PROPERTY_URI, HAS_PHYSICAL_PROPERTY_RELATION);
		aMap0.put(PART_OF_URI, PART_OF_RELATION);
		aMap0.put(HAS_PART_URI, HAS_PART_RELATION);
		aMap0.put(CONTAINED_IN_URI, CONTAINED_IN_RELATION);
		aMap0.put(CONTAINS_URI, CONTAINS_RELATION);
		aMap0.put(HAS_INDEX_ENTITY_URI, HAS_INDEX_ENTITY_RELATION);
		aMap0.put(INDEX_ENTITY_FOR_URI, INDEX_ENtity_FOR_RELATION);
		aMap0.put(HAS_SOURCE_URI, HAS_SOURCE_RELATION);
		aMap0.put(HAS_SINK_URI, HAS_SINK_RELATION);
		aMap0.put(HAS_MEDIATOR_URI, HAS_MEDIATOR_RELATION);
		aMap0.put(HAS_COMPUTATATIONAL_COMPONENT_URI, HAS_COMPUTATIONAL_COMPONENT_RELATION);
		aMap0.put(INCLUDES_SUBMODEL_URI, INCLUDES_SUBMODEL_RELATION);
		aMap0.put(COMPUTATIONAL_REPRESENTATION_OF_URI, COMPUTATIONAL_REPRESENTATION_OF_RELATION);
		aMap0.put(UNIT_FACTOR_EXPONENT_URI, UNIT_FACTOR_EXPONENT_RELATION);
		aMap0.put(UNIT_FACTOR_FOR_URI, UNIT_FACTOR_FOR_RELATION);
		aMap0.put(UNIT_FACTOR_PREFIX_URI, UNIT_FACTOR_PREFIX_RELATION);
		// Model-level stuff
		aMap0.put(BQB_HAS_PART_URI, BQB_HAS_PART_RELATION);
		aMap0.put(BQB_IS_PART_OF_URI, BQB_IS_PART_OF_RELATION);
		aMap0.put(BQB_IS_URI, BQB_IS_RELATION);
		aMap0.put(BQB_IS_VERSION_OF_URI, BQB_IS_VERSION_OF_RELATION);
		aMap0.put(BQB_OCCURS_IN_URI, BQB_OCCURS_IN_RELATION);
		aMap0.put(BQM_IS_URI, BQM_IS_RELATION);
		aMap0.put(BQM_IS_DESCRIBED_BY_URI, BQM_IS_DESCRIBED_BY_RELATION);
		aMap0.put(BQM_IS_DERIVED_FROM_URI, BQM_IS_DERIVED_FROM_RELATION);
		aMap0.put(CELLML_COMPONENT_PRIVATE_INTERFACE_URI, CELLML_COMPONENT_PRIVATE_INTERFACE_RELATION);
		aMap0.put(CELLML_COMPONENT_PUBLIC_INTERFACE_URI, CELLML_COMPONENT_PUBLIC_INTERFACE_RELATION);
		aMap0.put(CELLML_DOCUMENTATION_URI, CELLML_DOCUMENTATION_RELATION);
		aMap0.put(CELLML_RDF_MARKUP_URI, CELLML_RDF_MARKUP_RELATION);
		URIS_AND_SEMSIM_RELATIONS = Collections.unmodifiableMap(aMap0);
		
		// inverseRelations Map
		Map<URI,URI> aMap2 = new HashMap<URI,URI>();
		aMap2.put(PART_OF_URI, HAS_PART_URI);
		aMap2.put(CONTAINED_IN_URI, CONTAINS_URI);
		INVERSE_STRUCTURAL_RELATIONS_MAP = Collections.unmodifiableMap(aMap2);
		
		// BiologicalQualifierTypesAndRelations Map
		Map<Integer, SemSimRelation> aMap4 = new HashMap<Integer, SemSimRelation>();
		aMap4.put(0, BQB_IS_RELATION);
		aMap4.put(1, HAS_PART_RELATION);  // Use RO relation, not BQB here for consistency in SemSim models
		aMap4.put(2, PART_OF_RELATION);   // Use RO relation, not BQB here for consistency in SemSim models
		aMap4.put(3, BQB_IS_VERSION_OF_RELATION);
		aMap4.put(9, BQB_OCCURS_IN_RELATION);
		BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS = Collections.unmodifiableMap(aMap4);
		
		//ModelQualifierTypesAndRelations Map
		Map<Integer, SemSimRelation> aMap5 = new HashMap<Integer, SemSimRelation>();
		aMap5.put(0, BQM_IS_RELATION);
		aMap5.put(1, BQM_IS_DESCRIBED_BY_RELATION);
		aMap5.put(2, BQM_IS_DERIVED_FROM_RELATION);
		MODEL_QUALIFIER_TYPES_AND_RELATIONS = Collections.unmodifiableMap(aMap5);
	}
	
	/**
	 * Look up a URI's corresponding {@link SemSimRelation}
	 * 
	 * @param uri The URI key
	 * @return The SemSimRelation value for the URI key or else null if not found
	 */
	public static SemSimRelation getRelationFromURI(URI uri){
		if(URIS_AND_SEMSIM_RELATIONS.containsKey(uri)){
			return URIS_AND_SEMSIM_RELATIONS.get(uri);
		}
		return null;
	}
}
