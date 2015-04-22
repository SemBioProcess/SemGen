package semsim;


import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;

/**
 * A set of constants for working with SemSim models
 */
public class SemSimConstants {
	
	// Full names of ontologies & knowledge bases
	public static final String BRAUNSCHWEIG_ENZYME_DATABASE_FULLNAME = "Braunschweig Enzyme Database";
	public static final String BRENDA_TISSUE_ONTOLOGY_FULLNAME = "Brenda Tissue Ontology";
	public static final String CELL_TYPE_ONTOLOGY_FULLNAME = "Cell Type Ontology";
	public static final String CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME = "Clinical Measurement Ontology";
	public static final String CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME = "Chemical Entities of Biological Interest";
	public static final String ECG_ONTOLOGY_FULLNAME = "Electrocardiography Ontology";
	public static final String FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME = "Foundational Model of Anatomy";
	public static final String GENE_ONTOLOGY_FULLNAME = "Gene Ontology";
	public static final String HUMAN_DISEASE_ONTOLOGY_FULLNAME = "Human Disease Ontology";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Compound";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Drug";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Genes";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Orthology";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Pathway";
	public static final String KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME = "Kyoto Encyclopedia of Genes and Genomes - Reaction";
	// need to add interpro here: http://identifiers.org/interpro/
	public static final String MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME = "Mouse Adult Gross Anatomy Ontology";
	public static final String ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME = "Ontology of Phyiscs for Biology";
	public static final String PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME = "Phenotype and Trait Ontology";
	public static final String PROTEIN_ONTOLOGY_FULLNAME = "Protein Ontology";
	public static final String RELATIONS_ONTOLOGY_FULLNAME = "Relations Ontology";
	public static final String SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME = "Systems Biology Ontology";
	public static final String SNOMEDCT_FULLNAME = "SNOMED - Clinical Terms";
	public static final String UBERON_FULLNAME = "Uberon";
	public static final String UNIPROT_FULLNAME = "Universal Protein Resource";
		
	// Namespaces
	public static final String SEMSIM_NAMESPACE = "http://www.bhi.washington.edu/SemSim#";
	public static final String OPB_NAMESPACE = "http://bhi.washington.edu/OPB#";
	public static final String RO_NAMESPACE = "http://www.obofoundry.org/ro/ro.owl#";
	public static final String BQB_NAMESPACE = "http://biomodels.net/biology-qualifiers/";
	public static final String BQM_NAMESPACE = "http://biomodels.net/model-qualifiers/";
	public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	// Groups of ontologies
	public static final String[] ALL_SEARCHABLE_ONTOLOGIES = new String[]{
			SemSimConstants.BRENDA_TISSUE_ONTOLOGY_FULLNAME,
			SemSimConstants.CELL_TYPE_ONTOLOGY_FULLNAME,
			SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME,
			SemSimConstants.CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME,
			SemSimConstants.ECG_ONTOLOGY_FULLNAME,
			SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME,
			SemSimConstants.GENE_ONTOLOGY_FULLNAME,
			SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME,
			SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME,
			SemSimConstants.PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME,
			SemSimConstants.SNOMEDCT_FULLNAME,
			SemSimConstants.SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME,
			SemSimConstants.UNIPROT_FULLNAME
	};
	
	// URIs
	public static final URI COMPUTATION_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Computation");
	public static final URI SEMSIM_MODEL_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "SemSim_Model");
	public static final URI RELATIONAL_CONSTRAINT_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Relational_constraint");
	public static final URI DATA_STRUCTURE_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Data_structure");
	public static final URI DECIMAL_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Decimal");
	public static final URI SEMSIM_INTEGER_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Integer");
	public static final URI MML_CHOICE_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "MMLchoice");
	public static final URI PHYSICAL_MODEL_COMPONENT_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Physical_model_component");
	public static final URI COMPOSITE_PHYSICAL_ENTITY_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Composite_physical_entity");
	public static final URI UNITS_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Unit_of_measurement");
	public static final URI PHYSICAL_PROPERTY_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Physical_property");
	public static final URI PHYSICAL_ENTITY_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Physical_entity");
	public static final URI REFERENCE_PHYSICAL_ENTITY_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Reference_physical_entity");
	public static final URI PHYSICAL_PROCESS_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Physical_process");
	public static final URI REFERENCE_PHYSICAL_PROCESS_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Reference_physical_process");
	public static final URI PHYSICAL_DEPENDENCY_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Physical_dependency");
	public static final URI CUSTOM_PHYSICAL_ENTITY_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Custom_physical_entity");
	public static final URI CUSTOM_PHYSICAL_PROCESS_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Custom_physical_process");
	public static final URI SUBMODEL_CLASS_URI = URI.create(SEMSIM_NAMESPACE + "Submodel");
	
	public static final URI CELLML_COMPONENT_SUBSUMPTION_TYPE_URI = URI.create(SEMSIM_NAMESPACE + "submodelSubsumptionType");
	public static final URI DETERMINES_URI = URI.create(SEMSIM_NAMESPACE + "determines");
	public static final URI ENCAPSULATES_URI = URI.create(SEMSIM_NAMESPACE + "encapsulates");
	public static final URI HAS_SOURCE_URI = URI.create(SEMSIM_NAMESPACE + "hasSource");
	public static final URI HAS_SINK_URI = URI.create(SEMSIM_NAMESPACE + "hasSink");
	public static final URI HAS_MEDIATOR_URI = URI.create(SEMSIM_NAMESPACE + "hasMediator");
	public static final URI HAS_SOURCE_PARTICIPANT_URI = URI.create(SEMSIM_NAMESPACE + "hasSourceParticipant");
	public static final URI HAS_SINK_PARTICIPANT_URI = URI.create(SEMSIM_NAMESPACE + "hasSinkParticipant");
	public static final URI HAS_MEDIATOR_PARTICIPANT_URI = URI.create(SEMSIM_NAMESPACE + "hasMediatorParticipant");
	public static final URI HAS_PHYSICAL_ENTITY_REFERENCE_URI = URI.create(SEMSIM_NAMESPACE + "hasPhysicalEntityReference");
	
	public static final URI HAS_COMPUTATIONAL_CODE_URI = URI.create(SEMSIM_NAMESPACE + "hasComputationalCode");
	public static final URI HAS_COMPUTATATIONAL_COMPONENT_URI = URI.create(SEMSIM_NAMESPACE + "hasComputationalComponent");
	public static final URI HAS_MATHML_URI = URI.create(SEMSIM_NAMESPACE + "hasMathML");
	public static final URI HAS_SOLUTION_DOMAIN_URI = URI.create(SEMSIM_NAMESPACE + "hasSolutionDomain");
	public static final URI HAS_START_VALUE_URI = URI.create(SEMSIM_NAMESPACE + "hasStartValue");
	public static final URI HAS_INPUT_URI = URI.create(SEMSIM_NAMESPACE + "hasInput");
	public static final URI HAS_UNIT_FACTOR_URI = URI.create(SEMSIM_NAMESPACE + "hasUnitFactor");
	public static final URI HAS_OUTPUT_URI = URI.create(SEMSIM_NAMESPACE + "hasOutput");
	public static final URI HREF_VALUE_OF_IMPORT_URI = URI.create(SEMSIM_NAMESPACE + "hrefValueOfImport");
	public static final URI IMPORTED_FROM_URI = URI.create(SEMSIM_NAMESPACE + "importedFrom");
	public static final URI IS_COMPUTATIONAL_COMPONENT_FOR_URI = URI.create(SEMSIM_NAMESPACE + "isComputationalComponentFor");
	public static final URI IS_OUTPUT_FOR_URI = URI.create(SEMSIM_NAMESPACE + "isOutputFor");
	public static final URI IS_INPUT_FOR_URI = URI.create(SEMSIM_NAMESPACE + "isInputFor");
	public static final URI IS_DECLARED_URI = URI.create(SEMSIM_NAMESPACE + "isDeclared");
	public static final URI IS_DETERMINED_BY_URI = URI.create(SEMSIM_NAMESPACE + "isDeterminedBy");
	public static final URI IS_DISCRETE_URI = URI.create(SEMSIM_NAMESPACE + "isDiscrete");
	public static final URI IS_FUNDAMENTAL_UNIT_URI = URI.create(SEMSIM_NAMESPACE + "isFundamentalUnit");
	public static final URI IS_SOLUTION_DOMAIN_URI = URI.create(SEMSIM_NAMESPACE + "isSolutionDomain");
	public static final URI MAPPED_FROM_URI = URI.create(SEMSIM_NAMESPACE + "mappedFrom");
	public static final URI MAPPED_TO_URI = URI.create(SEMSIM_NAMESPACE + "mappedTo");
	public static final URI METADATA_ID_URI = URI.create(SEMSIM_NAMESPACE + "metadataID");
	public static final URI REFERS_TO_URI = URI.create(SEMSIM_NAMESPACE + "refersTo");
	public static final URI REFERENCE_NAME_OF_IMPORT_URI = URI.create(SEMSIM_NAMESPACE + "referenceNameOfImport");

	public static final URI HAS_UNIT_URI = URI.create(SEMSIM_NAMESPACE + "hasUnit");
	public static final URI UNIT_FOR_URI = URI.create(SEMSIM_NAMESPACE + "unitFor");
	public static final URI HAS_ASSOCIATED_DATA_STRUCTURE_URI = URI.create(SEMSIM_NAMESPACE + "hasAssociatedDataStructure");
	public static final URI PHYSICAL_PROPERTY_OF_URI = URI.create(SEMSIM_NAMESPACE + "physicalPropertyOf");
	public static final URI HAS_PHYSICAL_PROPERTY_URI = URI.create(SEMSIM_NAMESPACE + "hasPhysicalProperty");
	public static final URI PART_OF_URI = URI.create(RO_NAMESPACE + "part_of");
	public static final URI HAS_PART_URI = URI.create(RO_NAMESPACE + "has_part");
	public static final URI CONTAINED_IN_URI = URI.create(RO_NAMESPACE + "contained_in");
	public static final URI CONTAINS_URI = URI.create(RO_NAMESPACE + "contains");
	public static final URI HAS_INDEX_ENTITY_URI = URI.create(SEMSIM_NAMESPACE + "hasIndexEntity");
	public static final URI INDEX_ENTITY_FOR_URI = URI.create(SEMSIM_NAMESPACE + "isIndexEntityFor");
	public static final URI INCLUDES_SUBMODEL_URI = URI.create(SEMSIM_NAMESPACE + "includesSubmodel");
	public static final URI COMPUTATIONAL_REPRESENTATION_OF_URI = URI.create(SEMSIM_NAMESPACE + "computationalRepresentationOf");
	public static final URI HAS_MULTIPLIER_URI = URI.create(SEMSIM_NAMESPACE + "hasMultiplier");
	public static final URI UNIT_FACTOR_EXPONENT_URI = URI.create(SEMSIM_NAMESPACE + "hasUnitFactorExponent");
	public static final URI UNIT_FACTOR_FOR_URI = URI.create(SEMSIM_NAMESPACE + "unitFactorFor");
	public static final URI UNIT_FACTOR_PREFIX_URI = URI.create(SEMSIM_NAMESPACE + "hasUnitFactorPrefix");
	
	// Model-level relations	
	public static final URI BQB_HAS_PART_URI = URI.create(BQB_NAMESPACE + "hasPart");
	public static final URI BQB_IS_PART_OF_URI = URI.create(BQB_NAMESPACE + "isPartOf");
	public static final URI BQB_IS_URI = URI.create(BQB_NAMESPACE + "is");
	public static final URI BQB_IS_VERSION_OF_URI = URI.create(BQB_NAMESPACE + "isVersionOf");
	public static final URI BQB_OCCURS_IN_URI = URI.create(BQB_NAMESPACE + "occursIn");
	public static final URI BQM_IS_URI = URI.create(BQM_NAMESPACE + "is");
	public static final URI BQM_IS_DESCRIBED_BY_URI = URI.create(BQM_NAMESPACE + "isDescribedBy");
	public static final URI BQM_IS_DERIVED_FROM_URI = URI.create(BQM_NAMESPACE + "isDerivedFrom");
	
	public static final URI HAS_NOTES_URI = URI.create(SEMSIM_NAMESPACE + "hasNotes");

	public static final URI CELLML_INITIAL_VALUE_URI = URI.create(SEMSIM_NAMESPACE + "hasCellMLinitialValue");
	public static final URI CELLML_COMPONENT_PRIVATE_INTERFACE_URI = URI.create(SEMSIM_NAMESPACE + "hasCellMLprivateInterface");
	public static final URI CELLML_COMPONENT_PUBLIC_INTERFACE_URI = URI.create(SEMSIM_NAMESPACE + "hasCellMLpublicInterface");
	public static final URI CELLML_DOCUMENTATION_URI = URI.create(SEMSIM_NAMESPACE + "hasCellMLdocumentation");
	public static final URI CELLML_RDF_MARKUP_URI = URI.create(SEMSIM_NAMESPACE + "hasCellMLrdfMarkup");
	public static final URI HAS_NAME_URI = URI.create(SEMSIM_NAMESPACE + "name");
	public static final URI KEY_TERM_URI = URI.create(SEMSIM_NAMESPACE + "keyTerm");

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
	
	public static final SemSimRelation REFERS_TO_RELATION = new SemSimRelation("Refers to ontology term", REFERS_TO_URI);
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

	public static final Map<String, String> ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP;
	public static final Map<String, String> ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP;
	public static final Map<URI, SemSimRelation> URIS_AND_SEMSIM_RELATIONS;
	public static final Map<URI,URI> INVERSE_STRUCTURAL_RELATIONS_MAP;
	public static final Map<Integer, SemSimRelation> BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS;
	public static final Map<Integer, SemSimRelation> MODEL_QUALIFIER_TYPES_AND_RELATIONS;
	
	public static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
	
	static{       
        // URIsAndSemSimRelations Map
        Map<URI,SemSimRelation> aMap0 = new HashMap<URI,SemSimRelation>();
		aMap0.put(CELLML_COMPONENT_SUBSUMPTION_TYPE_URI, CELLML_COMPONENT_SUBSUMPTION_TYPE_RELATION);
		aMap0.put(DETERMINES_URI, DETERMINES_RELATION);
		aMap0.put(ENCAPSULATES_URI, ENCAPSULATES_RELATION);
		aMap0.put(MAPPED_FROM_URI, MAPPED_FROM_RELATION);
		aMap0.put(MAPPED_TO_URI, MAPPED_TO_RELATION);
		aMap0.put(REFERS_TO_URI, REFERS_TO_RELATION);
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
		
		// Namespaces and Ontology Names Map
		Hashtable<String, String> aMap1 = new Hashtable<String,String>();
		aMap1.put(OPB_NAMESPACE, ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME);
		aMap1.put("http://identifiers.org/opb/", ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME);
		aMap1.put("http://www.owl-ontologies.com/unnamed.owl#", ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME);
		aMap1.put("http://biomodels.net/SBO/", SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/SBO#", SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/SBO", SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.org/sig/ont/fma/", FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME);
		aMap1.put("http://sig.biostr.washington.edu/fma3.0#", FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME);
		aMap1.put("http://sig.uw.edu/fma#", FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME);
		aMap1.put("http://identifiers.org/fma/", FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/GO#", GENE_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/GO", GENE_ONTOLOGY_FULLNAME);
		aMap1.put("urn:miriam:obo.go:", GENE_ONTOLOGY_FULLNAME);
		aMap1.put("http://identifiers.org/go/", GENE_ONTOLOGY_FULLNAME);
		aMap1.put("http://identifiers.org/obo.go/", GENE_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.bioontology.org/ontology/SNOMEDCT/", SNOMEDCT_FULLNAME);
		aMap1.put("http://www.cvrgrid.org/ECGOntology1229347266968.owl#", ECG_ONTOLOGY_FULLNAME);
		aMap1.put("http://www.cvrgrid.org/files/ECGOntologyv0.1.7.owl#", ECG_ONTOLOGY_FULLNAME);
		aMap1.put("http://www.cvrgrid.org/files/ECGOntologyv1.owl#", ECG_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/CHEBI#", CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/CHEBI", CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME);
		aMap1.put("http://identifiers.org/chebi/", CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME);
		aMap1.put("http://identifiers.org/obo.chebi/", CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME);
		aMap1.put("urn:miriam:obo.chebi:", CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME);
		aMap1.put("http://www.obofoundry.org/ro/ro.owl#", RELATIONS_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/PATO#", PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/PATO", PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/BTO#", BRENDA_TISSUE_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/bto.owl#", BRENDA_TISSUE_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/BTO", BRENDA_TISSUE_ONTOLOGY_FULLNAME);
		aMap1.put("http://identifiers.org/ec-code/", BRAUNSCHWEIG_ENZYME_DATABASE_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/UBERON#", UBERON_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/UBERON", UBERON_FULLNAME);
		aMap1.put("http://purl.bioontology.org/ontology/CMO/", CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/CMO", CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.uniprot.org/uniprot/", UNIPROT_FULLNAME);
		aMap1.put("http://identifiers.org/uniprot/", UNIPROT_FULLNAME);
		aMap1.put("http://www.uniprot.org/uniprot/", UNIPROT_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/DOID#", HUMAN_DISEASE_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.bioontology.org/ontology/MA", MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.org/obo/owl/MA#", MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/MA", MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME);
		aMap1.put("http://identifiers.org/ma/", MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME);
		aMap1.put("http://purl.obolibrary.org/obo/PR", PROTEIN_ONTOLOGY_FULLNAME);
		aMap1.put("http://identifiers.org/kegg.compound/", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME);
		aMap1.put("urn:miriam:kegg.compound:", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME );
		aMap1.put("http://identifiers.org/kegg.reaction/", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME );
		aMap1.put("urn:miriam:kegg.reaction:", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME );
		aMap1.put("http://identifiers.org/kegg.drug/", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME );
		aMap1.put("urn:miriam:kegg.drug:", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME );
		aMap1.put("http://identifiers.org/kegg.genes/", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME );
		aMap1.put("urn:miriam:kegg.genes:", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME );
		aMap1.put("http://identifiers.org/kegg.orthology/", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME );
		aMap1.put("urn:miriam:kegg.orthology:", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME );
		aMap1.put("http://identifiers.org/kegg.pathway/", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME );
		aMap1.put("urn:miriam:kegg.pathway:", KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME );
		aMap1.put("http://purl.obolibrary.org/obo/CL", CELL_TYPE_ONTOLOGY_FULLNAME );
		aMap1.put("http://identifiers.org/cl/", CELL_TYPE_ONTOLOGY_FULLNAME );
		ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP = Collections.unmodifiableMap(aMap1);
		
		// Mappings between full ontology names and nicknames
		Hashtable<String, String> aMap6 = new Hashtable<String,String>();
		
		// Nicknames of ontologies & knowledge bases		
		aMap6.put(BRAUNSCHWEIG_ENZYME_DATABASE_FULLNAME, "BRENDA");
		aMap6.put(BRENDA_TISSUE_ONTOLOGY_FULLNAME, "BTO");
		aMap6.put(CELL_TYPE_ONTOLOGY_FULLNAME, "CL");
		aMap6.put(CLINICAL_MEASUREMENT_ONTOLOGY_FULLNAME, "CMO");
		aMap6.put(CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME, "CHEBI");
		aMap6.put(ECG_ONTOLOGY_FULLNAME, "ECG");
		aMap6.put(FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME, "FMA");
		aMap6.put(GENE_ONTOLOGY_FULLNAME, "GO");
		aMap6.put(HUMAN_DISEASE_ONTOLOGY_FULLNAME, "DOID");
		aMap6.put(KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_COMPOUND_KB_FULLNAME, "KEGG-compound");
		aMap6.put(KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_DRUG_KB_FULLNAME, "KEGG-drug");
		aMap6.put(KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_GENES_KB_FULLNAME, "KEGG-genes");
		aMap6.put(KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_ORTHOLOGY_KB_FULLNAME, "KEGG-orthology");
		aMap6.put(KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_PATHWAY_KB_FULLNAME, "KEGG-pathway");
		aMap6.put(KYOTO_ENCYCLOPEDIA_OF_GENES_AND_GENOMES_REACTION_KB_FULLNAME, "KEGG-reaction");
		aMap6.put(MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME, "MA");
		aMap6.put(ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME, "OPB");
		aMap6.put(PHENOTYPE_AND_TRAIT_ONTOLOGY_FULLNAME, "PATO");
		aMap6.put(PROTEIN_ONTOLOGY_FULLNAME, "PR");
		aMap6.put(RELATIONS_ONTOLOGY_FULLNAME, "RO");
		aMap6.put(SYSTEMS_BIOLOGY_ONTOLOGY_FULLNAME, "SBO");
		aMap6.put(SNOMEDCT_FULLNAME, "SNOMEDCT");
		aMap6.put(UBERON_FULLNAME, "UBERON");
		aMap6.put(UNIPROT_FULLNAME, "UNIPROT");
		ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP = Collections.unmodifiableMap(aMap6);
		
		// inverseRelations Map
		Map<URI,URI> aMap2 = new HashMap<URI,URI>();
		aMap2.put(PART_OF_URI, HAS_PART_URI);
		aMap2.put(CONTAINED_IN_URI, CONTAINS_URI);
		INVERSE_STRUCTURAL_RELATIONS_MAP = Collections.unmodifiableMap(aMap2);
		
		// BiologicalQualifierTypesAndRelations Map
		Map<Integer, SemSimRelation> aMap4 = new HashMap<Integer, SemSimRelation>();
		aMap4.put(0, BQB_IS_RELATION);
		aMap4.put(1, BQB_HAS_PART_RELATION);
		aMap4.put(2, BQB_IS_PART_OF_RELATION);
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
