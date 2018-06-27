package semsim.model.computational;

/**
 * Class for representing SBML Initial Assignments
 * @author mneal
 *
 */
public class SBMLInitialAssignment extends EventAssignment{

	@Override
	public boolean isSBMLinitialAssignment(){
		return true;
	}
}
