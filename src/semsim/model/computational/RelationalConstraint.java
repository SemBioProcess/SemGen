package semsim.model.computational;

import java.util.HashSet;
import java.util.Set;

<<<<<<< HEAD
import semsim.model.SemSimComponent;

/** A class to represent relational statements in simulation models that express non-equivalencies.
 * Examples: x > y, z <= 0;
 */
public class RelationalConstraint extends SemSimComponent{
=======
/** A class to represent relational statements in simulation models that express non-equivalencies.
 * Examples: x > y, z <= 0;
 */
public class RelationalConstraint extends ComputationalModelComponent{
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	private Set<DataStructure> inputs = new HashSet<DataStructure>();
	private String computationalCode;
	private String mathML;

	public RelationalConstraint(String compcode, String mathml){
		this.setComputationalCode(compcode);
		this.setMathML(mathml);
	}

	/** @return A human-readable string representation of the relation. */
	public String getComputationalCode() {
		return computationalCode;
	}
	
	/** @return The set of DataStructures that participate in the relation.*/
	public Set<DataStructure> getInputs(){
		return inputs;
	}
	
	/** @return A MathML representation of the relation.*/
	public String getMathML() {
		return mathML;
	}
	
	/** Sets the human-readable string representation of the relation.*/
	public void setComputationalCode(String code){
		computationalCode = code;
	}
	
	/** Sets the DataStructures that participate in the relation.*/
	public void setInputs(Set<DataStructure> inputs){
		this.inputs.clear();
		this.inputs.addAll(inputs);
	}
	
	/** Sets the MathML representation of the relation.*/
	public void setMathML(String mathml){
		mathML = mathml;
	}
}


