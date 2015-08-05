package semsim.model.computational;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimConstants;
import semsim.model.computational.datastructures.DataStructure;

/** A class to represent relational statements in simulation models that express non-equivalencies.
 * Examples: x > y, z <= 0;
 */
public class RelationalConstraint extends ComputationalModelComponent{
	private Set<DataStructure> inputs = new HashSet<DataStructure>();
	private String computationalCode;
	private String mathML;
	private String errorMsg;
	
	public RelationalConstraint(String mathml){
		this.setMathML(mathml);
	}
	
	public RelationalConstraint(String compcode, String mathml){
		this.setComputationalCode(compcode);
		this.setMathML(mathml);
	}

	public RelationalConstraint(String compcode, String mathml, String errorMsg){
		this.setComputationalCode(compcode);
		this.setMathML(mathml);
		this.setErrorMessage(errorMsg);
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
	
	/** @return Optional error message to display when the constraint evaluates as false 
	 * (method provided in adherence to SBML best practices). */
	public String getErrorMessage(){
		return errorMsg;
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
	
	/** Sets the error message to display when the constraint evaluates as false 
	 * (method provided in adherence to SBML best practices). */
	public void setErrorMessage(String msg){
		errorMsg = msg;
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.RELATIONAL_CONSTRAINT_CLASS_URI;
	}
}


