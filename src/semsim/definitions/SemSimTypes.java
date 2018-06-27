package semsim.definitions;

import java.net.URI;

import org.semanticweb.owlapi.model.IRI;

/**
 * Enum and functions for the different types of objects used in SemSim models
 * @author mneal
 *
 */
public enum SemSimTypes {
	SEMSIM_COMPONENT("SemSim Component", RDFNamespace.SEMSIM.getNamespaceasString() + "SemSim_component", "", ""),
	MODEL("SemSim Model", RDFNamespace.SEMSIM.getNamespaceasString() + "SemSim_Model", "", "SemSim:SemSim_Model"),
	SUBMODEL("Submodel", RDFNamespace.SEMSIM.getNamespaceasString() + "Submodel", "", "SemSim:Submodel"),
	PHYSICAL_MODEL_COMPONENT("Physical Model Component", RDFNamespace.SEMSIM.getNamespaceasString() + "Physical_model_component", "", ""),
	PHYSICAL_PROPERTY("Singular Physical Property", RDFNamespace.SEMSIM.getNamespaceasString() + "Physical_property", 
			"A reference term that precisely defines a biophysical property simulated in the model.", "SemSim:Physical_property"),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property for a Composite", RDFNamespace.SEMSIM.getNamespaceasString() + "Physical_property_in_composite", 
			"A reference term that defines the physical property component of a composite annotation.", ""),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Entity", RDFNamespace.SEMSIM.getNamespaceasString() + "Reference_physical_entity", 
			"A reference term that defines a physical entity represented in the model.", "SemSim:Reference_physical_entity"),
	PHYSICAL_DEPENDENCY("Physical Dependency", RDFNamespace.SEMSIM.getNamespaceasString() + "Physical_dependency", "", ""),
	REFERENCE_PHYSICAL_DEPENDENCY("Reference Physical Dependency", RDFNamespace.SEMSIM.getNamespaceasString() + "Reference_physical_dependency",
			"A reference term that defines a physical dependency represented in the model.", "SemSim:Reference_physical_dependency"),
	PHYSICAL_ENTITY("Physical Entity", RDFNamespace.SEMSIM.getNamespaceasString() + "Physical_entity", "", ""),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity", RDFNamespace.SEMSIM.getNamespaceasString() + "Custom_physical_entity", 
			"A physical entity which is not defined against a reference term, but instead created and defined in an ad hoc manner within a model.", ""),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity", RDFNamespace.SEMSIM.getNamespaceasString() + "Composite_physical_entity",
			"A physical entity that is defined by a composition of multiple physical entity terms.", ""),
	CUSTOM_PHYSICAL_PROCESS("Custom Physical Process", RDFNamespace.SEMSIM.getNamespaceasString() + "Custom_physical_process",
			"A physical process which is not defined against a reference term, but instead created and defined in an ad hoc manner within the model.", ""),
	REFERENCE_PHYSICAL_PROCESS("Reference Physical Process", RDFNamespace.SEMSIM.getNamespaceasString() + "Reference_physical_process",
			"A reference term that defines a physical process simulated by the model.", "semsim:Reference_physical_process"),
	PHYSICAL_PROCESS("Physical Process", RDFNamespace.SEMSIM.getNamespaceasString() + "Physical_process","", ""),
	DATASTRUCTURE("Data Structure", RDFNamespace.SEMSIM.getNamespaceasString() + "Data_structure", "", ""),
	DECIMAL("Decimal", RDFNamespace.SEMSIM.getNamespaceasString() + "Decimal", "", "semsim:Decimal"),
	MMLCHOICE("MML Model Choice", RDFNamespace.SEMSIM.getNamespaceasString() + "MMLchoice", "", "semsim:MMLchoice"),
	INTEGER("SemSim Integer", RDFNamespace.SEMSIM.getNamespaceasString() + "Integer", "", "semsim:Integer"),
	COMPUTATION("Computation", RDFNamespace.SEMSIM.getNamespaceasString() + "Computation", "", "semsim:Computation"),
	SBML_FUNCTION_OUTPUT("SBML Function Output", RDFNamespace.SEMSIM.getNamespaceasString() + "SBML_function_output", "", "semsim:SBML_function_output"),
	RELATIONAL_CONSTRAINT("Relational Constraint", RDFNamespace.SEMSIM.getNamespaceasString() + "Relational_constraint", "", "semsim:Relational_constraint"),
	EVENT("Event", RDFNamespace.SEMSIM.getNamespaceasString() + "Event", "", "semsim:Event"),
	EVENT_ASSIGNMENT("Event Assignment", RDFNamespace.SEMSIM.getNamespaceasString() + "EventAssignment", "", "semsim:EventAssignment"),
	SBML_INITIAL_ASSIGNMENT("SBML Initial Assignment", RDFNamespace.SEMSIM.getNamespaceasString() + "SBML_initial_assignment","","sesmsim:SBML_initial_assignment"),
	UNIT_OF_MEASUREMENT("Unit of Measurement", RDFNamespace.SEMSIM.getNamespaceasString() + "Unit_of_measurement", "", "semsim:Unit_of_measurement");
	
	private String name;
	private String uri;
	private String sparqlcode;
	private String description;
	
	SemSimTypes(String name, String uri, String desc, String sparqlcode) {
		this.name = name;
		this.uri = uri;
		this.sparqlcode = sparqlcode;
	}
	
	/** @return The name of the SemSimType */
	public String getName() {
		return name;
	}
	
	/** @return The URI of the SemSimType as a string*/
	public String getURIasString() {
		return uri;
	}
	
	/** @return The URI of the SemSimType */
	public URI getURI() {
		return URI.create(uri);
	}
	
	/** @return The URI of the SemSimType cast as an IRI */
	public IRI getIRI() {
		return IRI.create(uri);
	}
	
	/** @return The SemSimType formatted for SPARQL queries */
	public String getSparqlCode() {
		return sparqlcode;
	}
	
	/** @return The description of the SemSimType */
	public String getDescription() {
		return description;
	}
}
