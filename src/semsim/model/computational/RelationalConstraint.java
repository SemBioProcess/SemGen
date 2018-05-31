package semsim.model.computational;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;

/** A class to represent relational statements in simulation models that express non-equivalencies.
 * Examples: x &gt; y, z &lt;= 0;
 */
public class RelationalConstraint extends ComputationalModelComponent{
	private Set<DataStructure> inputs = new HashSet<DataStructure>();
	private String computationalCode;
	private String mathML;
	private String errorMsg;
	
	public RelationalConstraint(String mathml){
		super(SemSimTypes.RELATIONAL_CONSTRAINT);
		this.setMathML(mathml);
	}
	
	public RelationalConstraint(String compcode, String mathml){
		super(SemSimTypes.RELATIONAL_CONSTRAINT);
		this.setComputationalCode(compcode);
		this.setMathML(mathml);
	}

	public RelationalConstraint(String compcode, String mathml, String errorMsg){
		super(SemSimTypes.RELATIONAL_CONSTRAINT);
		this.setComputationalCode(compcode);
		this.setMathML(mathml);
		this.setErrorMessage(errorMsg);
	}
	
	/**
	 * Copy constructor
	 * @param rctocopy The RelationConstraint to copy
	 */
	public RelationalConstraint(RelationalConstraint rctocopy) {
		super(rctocopy);
		computationalCode = new String(rctocopy.computationalCode);
		mathML = new String(rctocopy.mathML);
		errorMsg = new String(rctocopy.errorMsg);
		inputs.addAll(rctocopy.inputs);
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
	
	/**
	 * Replace all {@link DataStructure}s
	 * @param dsmap A HashMap that maps {@link DataStructure}s to replace with their replacements
	 */
	public void replaceAllDataStructures(HashMap<DataStructure, DataStructure> dsmap) {
		Set<DataStructure> newinputs = new HashSet<DataStructure>();
		for (DataStructure dstoreplace : inputs) {
			newinputs.add(dsmap.get(dstoreplace));
		}
	}

	@Override
	public RelationalConstraint addToModel(SemSimModel model) {
		return model.addRelationalConstraint(this);
	}
}


