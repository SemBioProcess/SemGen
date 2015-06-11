package semsim.model;

public enum SemSimTypes {
	PHYSICAL_PROPERTY("Physical Property"),
	PHYSICAL_PROPERTY_IN_COMPOSITE("Physical Property"),
	REFERENCE_PHYSICAL_ENTITY("Reference Physical Property"),
	CUSTOM_PHYSICAL_ENTITY("Custom Physical Entity"),
	COMPOSITE_PHYSICAL_ENTITY("Composite Physical Entity"),
	CUSTOM_PHYSICAL_PROCESS("Physical Process"),
	REFERENCE_PHYSICAL_PROCESS("Physical Process"),
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
