package semgen.stage.serialization;

public class MappableVariableDependency {
	String name;
	String[] parents;
	
	public MappableVariableDependency(String name, String[] parents) {
		this.name = name;
		this.parents = parents;
	}
}
