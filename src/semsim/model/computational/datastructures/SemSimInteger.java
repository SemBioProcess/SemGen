package semsim.model.computational.datastructures;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.computational.Computation;

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
	
	public SemSimInteger(SemSimInteger inttocopy) {
		super(inttocopy);
	}
	
	public boolean isReal() {
		return true;
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.SEMSIM_INTEGER_CLASS_URI;
	}
}
