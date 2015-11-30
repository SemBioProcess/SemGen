package semsim.definitions;

import java.net.URI;

public enum SemSimTypes {
	PHYSICAL_PROPERTY("Singular Physical Property", RDFNamespace.SEMSIM.getNamespace() + "Physical_property", "SemSim:Physical_property"),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property for a Composite", RDFNamespace.SEMSIM.getNamespace() + "Physical_property_in_composite", ""),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Reference_physical_entity", "SemSim:Reference_physical_entity"),
	PHYSICAL_DEPENDENCY("Physical Dependency", RDFNamespace.SEMSIM.getNamespace() + "Physical_dependency", ""),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Custom_physical_entity", ""),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity", RDFNamespace.SEMSIM.getNamespace() + "Composite_physical_entity", ""),
	CUSTOM_PHYSICAL_PROCESS("Custom Physical Process", RDFNamespace.SEMSIM.getNamespace() + "Custom_physical_process", ""),
	REFERENCE_PHYSICAL_PROCESS("Reference Physical Process", RDFNamespace.SEMSIM.getNamespace() + "Reference_physical_process", "");
	
	private String name;
	private String uri;
	private String sparqlcode;
	
	SemSimTypes(String name, String uri, String sparqlcode) {
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
	
	public String getSparqlCode() {
		return sparqlcode;
	}
}
