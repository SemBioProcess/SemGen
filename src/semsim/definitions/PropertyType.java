package semsim.definitions;

public enum PropertyType {
	Unknown ("Constitutive"),
	PropertyOfPhysicalEntity ("State"),
	PropertyOfPhysicalProcess ("Rate");
	
	// String representation of enum
	private final String _name;

    private PropertyType(String name) {
        _name = name;
    }

    public String toString(){
       return _name;
    }
}