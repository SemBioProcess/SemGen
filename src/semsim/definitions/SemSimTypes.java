package semsim.definitions;

import org.semanticweb.owlapi.model.IRI;

import java.net.URI;

public enum SemSimTypes {
	MODEL("SemSim Model", RDFNamespace.SEMSIM.getNamespaceAsString() + "SemSim_Model", "", "SemSim:SemSim_Model"),
	SUBMODEL("Submodel", RDFNamespace.SEMSIM.getNamespaceAsString() + "Submodel", "", "SemSim:Submodel"),
	PHYSICAL_MODEL_COMPONENT("Physical Model Component", RDFNamespace.SEMSIM.getNamespaceAsString() + "Physical_model_component", "", ""),
	PHYSICAL_PROPERTY("Singular Physical Property", RDFNamespace.SEMSIM.getNamespaceAsString() + "Physical_property", 
			"A reference term that precisely defines a biophysical property simulated in the model.", "SemSim:Physical_property"),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property for a Composite", RDFNamespace.SEMSIM.getNamespaceAsString() + "Physical_property_in_composite", 
			"A reference term that defines the physical property component of a composite annotation.", ""),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Entity", RDFNamespace.SEMSIM.getNamespaceAsString() + "Reference_physical_entity", 
			"A reference term that defines a physical entity represented in the model.", "SemSim:Reference_physical_entity"),
	PHYSICAL_DEPENDENCY("Physical Dependency", RDFNamespace.SEMSIM.getNamespaceAsString() + "Physical_dependency", "", ""),
	PHYSICAL_ENTITY("Physical Entity", RDFNamespace.SEMSIM.getNamespaceAsString() + "Physical_entity", "", ""),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity", RDFNamespace.SEMSIM.getNamespaceAsString() + "Custom_physical_entity", 
			"A physical entity which is not defined against a reference term, but instead created and defined in an ad hoc manner within a model.", ""),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity", RDFNamespace.SEMSIM.getNamespaceAsString() + "Composite_physical_entity",
			"A physical entity that is defined by a composition of multiple physical entity terms.", ""),
	CUSTOM_PHYSICAL_PROCESS("Custom Physical Process", RDFNamespace.SEMSIM.getNamespaceAsString() + "Custom_physical_process",
			"A physical process which is not defined against a reference term, but instead created and defined in an ad hoc manner within the model.", ""),
	REFERENCE_PHYSICAL_PROCESS("Reference Physical Process", RDFNamespace.SEMSIM.getNamespaceAsString() + "Reference_physical_process",
			"A reference term that defines a physical process simulated by the model.", "semsim:Reference_physical_process"),
	PHYSICAL_PROCESS("Physical Process", RDFNamespace.SEMSIM.getNamespaceAsString() + "Physical_process","", ""),
	DATASTRUCTURE("Data Structure", RDFNamespace.SEMSIM.getNamespaceAsString() + "Data_structure", "", ""),
	DECIMAL("Decimal", RDFNamespace.SEMSIM.getNamespaceAsString() + "Decimal", "", "semsim:Decimal"),
	MMLCHOICE("MML Model Choice", RDFNamespace.SEMSIM.getNamespaceAsString() + "MMLchoice", "", "semsim:MMLchoice"),
	INTEGER("SemSim Integer", RDFNamespace.SEMSIM.getNamespaceAsString() + "Integer", "", "semsim:Integer"),
	COMPUTATION("Computation", RDFNamespace.SEMSIM.getNamespaceAsString() + "Computation", "", "semsim:Computation"),
	RELATIONAL_CONSTRAINT("Relational Constraint", RDFNamespace.SEMSIM.getNamespaceAsString() + "Relational_constraint", "", "semsim:Relational_constraint"),
	EVENT("Event", RDFNamespace.SEMSIM.getNamespaceAsString() + "Event", "", "semsim:Event"),
	EVENT_ASSIGNMENT("Event Assignment", RDFNamespace.SEMSIM.getNamespaceAsString() + "EventAssignment", "", "semsim:EventAssignment"),
	UNIT_OF_MEASUREMENT("Unit of Measurement", RDFNamespace.SEMSIM.getNamespaceAsString() + "Unit_of_measurement", "", "semsim:Unit_of_measurement");
	
	private String name;
	private String uri;
	private String sparqlcode;
	private String description;
	
	SemSimTypes(String name, String uri, String desc, String sparqlcode) {
		this.name = name;
		this.uri = uri;
		this.sparqlcode = sparqlcode;
	}
	
	public String getName() {
		return name;
	}
	
	public String getURIasString() {
		return uri;
	}
	
	public URI getURI() {
		return URI.create(uri);
	}
	
	public IRI getIRI() {
		return IRI.create(uri);
	}
	
	public String getSparqlCode() {
		return sparqlcode;
	}
	
	public String getDescription() {
		return description;
	}
}
