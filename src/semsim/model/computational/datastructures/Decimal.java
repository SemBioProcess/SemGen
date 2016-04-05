package semsim.model.computational.datastructures;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.Computation;

/** A {@link DataStructure} that is assigned a decimal value during simulation.*/
public class Decimal extends DataStructure{
	
	/** Constructor where only name of the data structure is specified. Automatically associates this Decimal with 
	 * a new {@link Computation} instance.
	 * @param name The name of the Decimal data structure. */
	public Decimal(String name){
		super(SemSimTypes.DECIMAL);
		setName(name);
		setComputation(new Computation(this));
	}
	
	/** Constructor where the name and {@link Computation} are both specified.
	 * @param name The name of the Decimal data structure.
	 * @param computation The {@link Computation} instance that solves for the value of this Decimal.*/
	public Decimal(String name, Computation computation){
		super(SemSimTypes.DECIMAL);
		setName(name);
		setComputation(computation);
	}
	
	public Decimal(Decimal dectocopy) {
		super(dectocopy);
	}
	
	public boolean isReal() {
		return true;
	}

	@Override
	public DataStructure copy() {
		return new Decimal(this);
	}
}
