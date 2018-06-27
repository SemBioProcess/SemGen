package semsim.model.computational;

import semsim.definitions.SemSimTypes;

/**
 * Class for representing SBML Initial Assignments
 * @author mneal
 *
 */
public class SBMLInitialAssignment extends EventAssignment{

	// Constructor
	public SBMLInitialAssignment(){
		super(SemSimTypes.SBML_INITIAL_ASSIGNMENT);
	}
		
	@Override
	public boolean isSBMLinitialAssignment(){
		return true;
	}
}
