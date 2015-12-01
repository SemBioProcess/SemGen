package semsim.definitions;

import java.net.URI;

import org.semanticweb.owlapi.model.IRI;

public enum SemSimTypes {
	MODEL("SemSim Model", RDFNamespace.SEMSIM.getNamespace() + "SemSim_Model", "", "SemSim:SemSim_Model"),
	SUBMODEL("Submodel", RDFNamespace.SEMSIM.getNamespace() + "Submodel", "", "SemSim:Submodel"),
	PHYSICAL_MODEL_COMPONENT("Physical Model Component", RDFNamespace.SEMSIM.getNamespace() + "Physical_model_component", "", ""),
	PHYSICAL_PROPERTY("Singular Physical Property", RDFNamespace.SEMSIM.getNamespace() + "Physical_property", 
			"A reference term that precisely defines a biophysical property simulated in the model.", "SemSim:Physical_property"),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property for a Composite", RDFNamespace.SEMSIM.getNamespace() + "Physical_property_in_composite", 
			"A reference term that defines the physical property component of a composite annotation.", ""),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Reference_physical_entity", 
			"A reference term that defines a physical entity represented in the model.", "SemSim:Reference_physical_entity"),
	PHYSICAL_DEPENDENCY("Physical Dependency", RDFNamespace.SEMSIM.getNamespace() + "Physical_dependency", "", ""),
	PHYSICAL_ENTITY("Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Physical_entity", "", ""),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Custom_physical_entity", 
			"A physical entity which is not defined against a reference term, but instead created and defined in an ad hoc manner within a model.", ""),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Composite_physical_entity",
			"A physical entity that is defined by a composition of multiple physical entity terms.", ""),
	CUSTOM_PHYSICAL_PROCESS("Custom Physical Process", RDFNamespace.SEMSIM.getNamespace() + "Custom_physical_process",
			"A physical process which is not defined against a reference term, but instead created and defined in an ad hoc manner within the model.", ""),
	REFERENCE_PHYSICAL_PROCESS("Reference Physical Process", RDFNamespace.SEMSIM.getNamespace() + "Reference_physical_process",
			"A reference term that defines a physical process simulated by the model.", "semsim:Reference_physical_process"),
	PHYSICAL_PROCESS("Physical Process", RDFNamespace.SEMSIM.getNamespace() + "Physical_process","", ""),
	DATASTRUCTURE("Data Structure", RDFNamespace.SEMSIM.getNamespace() + "Data_structure", "", ""),
	DECIMAL("Decimal", RDFNamespace.SEMSIM.getNamespace() + "Decimal", "", "semsim:Decimal"),
	MMLCHOICE("MML Model Choice", RDFNamespace.SEMSIM.getNamespace() + "MMLchoice", "", "semsim:MMLchoice"),
	INTEGER("SemSim Integer", RDFNamespace.SEMSIM.getNamespace() + "Integer", "", "semsim:Integer"),
	COMPUTATION("Computation", RDFNamespace.SEMSIM.getNamespace() + "Computation", "", "semsim:Computation"),
	RELATIONAL_CONSTRAINT("Relational Constraint", RDFNamespace.SEMSIM.getNamespace() + "Relational_constraint", "", "semsim:Relational_constraint"),
	EVENT("Event", RDFNamespace.SEMSIM.getNamespace() + "Event", "", "semsim:Event"),
	EVENT_ASSIGNMENT("Event Assignment", RDFNamespace.SEMSIM.getNamespace() + "EventAssignment", "", "semsim:EventAssignment"),
	UNIT_OF_MEASUREMENT("Unit of Measurement", RDFNamespace.SEMSIM.getNamespace() + "Unit_of_measurement", "", "semsim:Unit_of_measurement"),
	RELATION("Relation", "", "", "");
	
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
