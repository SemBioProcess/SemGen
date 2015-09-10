package semsim.model;

public enum SemSimTypes {
	PHYSICAL_PROPERTY("Singular Physical Property"),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property of an Entity"),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Entity"),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity"),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity"),
	CUSTOM_PHYSICAL_PROCESS("Custom Physical Process"),
	REFERENCE_PHYSICAL_PROCESS("Reference Physical Process"),
	PHYSICAL_DEPENDENCY("Physical Dependency"),
	PHYSICAL_PROCESS("Physical Process");
	
	private String name;
	
	SemSimTypes(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
