package semsim.model.computational;

/** A {@link DataStructure} that is assigned an integer value during simulation.*/
public class SemSimInteger extends DataStructure{

	/** Constructor where only the name of the data structure is specified.
	 * Automatically associates this SemSimInteger with a new {@link Computation} class.*/
	public SemSimInteger(String name){
		setName(name);
		setComputation(new Computation(this));
	}
	
	/** Constructor where the name of the data structure is specified along with the 
	 * associated {@link Computation} that specifies how the SemSimInteger is solved.*/
	public SemSimInteger(String name, Computation computation){
		setName(name);
		setComputation(computation);
	}
	
	public boolean isReal() {
		return true;
	}
}
