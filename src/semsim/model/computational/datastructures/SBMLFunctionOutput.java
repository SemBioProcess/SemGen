package semsim.model.computational.datastructures;

import semsim.definitions.SemSimTypes;

/**
 * Class for working with SBML-style function definitions
 * @author mneal
 *
 */
public class SBMLFunctionOutput extends Decimal{
	
	
	/**
	 * Constructor with name argument
	 * @param name Name argument
	 */
	public SBMLFunctionOutput(String name){
		super(name, SemSimTypes.SBML_FUNCTION_OUTPUT);
	}
	
	
	/**
	 * Copy constructor
	 * @param tocopy The object to copy
	 */
	public SBMLFunctionOutput(SBMLFunctionOutput tocopy){
		super(tocopy);
	}

	@Override
	public boolean isReal() {
		return true;
	}

	@Override
	public SBMLFunctionOutput copy() {
		return new SBMLFunctionOutput(this);
	}
	
	
	@Override
	public SemSimTypes getSemSimType(){
		return SemSimTypes.SBML_FUNCTION_OUTPUT;
	}
}
