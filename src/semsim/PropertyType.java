package semsim;

public enum PropertyType {
	Unknown (0, "Constitutive"),
	PropertyOfPhysicalEntity (1, "State"),
	PropertyOfPhysicalProcess (2, "Rate");
	
	// String representation of enum
	private final String _name;
	
	// Int representation of enum
	private final int _index;

    private PropertyType(int index, String name) {
        _name = name;
        _index = index;
    }

    public int getIndex() {
    	return _index;
    }
    
    public String toString(){
       return _name;
    }
}