package semsim.model;

public enum SemSimTypes {
	PHYSICAL_PROPERTY("Singular Physical Property", "A reference term that precisely defines a biophysical property simulated in the model."),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property of an Entity or Process", 
			"A reference term that defines the physical property component of a composite annotation."),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Entity", "A reference term that defines a physical entity represented in the model."),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity", 
			"A physical entity which is not defined against a reference term, but instead created and defined in an ad hoc manner within a model."),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity", "A physical entity that is defined by a composition of multiple physical entity terms."),
	CUSTOM_PHYSICAL_PROCESS("Custom Physical Process", 
			"A physical process which is not defined against a reference term, but instead created and defined in an ad hoc manner within the model."),
	REFERENCE_PHYSICAL_PROCESS("Reference Physical Process", "A reference term that defines a physical process simulated by the model."),
	PHYSICAL_DEPENDENCY("Physical Dependency", ""),
	PHYSICAL_PROCESS("Physical Process", "");
	
	private String name;
	private String description;
	
	SemSimTypes(String name, String desc) {
		this.name = name; description = desc;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
}
