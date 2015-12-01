package semsim.model.computational.datastructures;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.Computation;

/** This class maps to the JSim-specific data structure called a "choice" 
 * variable. See http://www.physiome.org/jsim/docs16/MML_Topics.html for more info.
 */
public class MMLchoice extends DataStructure {
	
	public MMLchoice(String name){
		super(SemSimTypes.MMLCHOICE);
		setName(name);
		setComputation(new Computation(this));
	}
	
	public MMLchoice(String name, Computation computation){
		super(SemSimTypes.MMLCHOICE);
		setName(name);
		setComputation(computation);
	}
	
	public MMLchoice(MMLchoice mmltocopy) {
		super(mmltocopy);
	}
	
	public boolean isReal() {
		return true;
	}
}
