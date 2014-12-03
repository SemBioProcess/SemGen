package semsim.model.computational.datastructures;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.computational.Computation;

/** This class maps to the JSim-specific data structure called a "choice" 
 * variable. See http://www.physiome.org/jsim/docs16/MML_Topics.html for more info.
 */
public class MMLchoice extends DataStructure {
	
	public MMLchoice(String name){
		setName(name);
		setComputation(new Computation(this));
	}
	
	public MMLchoice(String name, Computation computation){
		setName(name);
		setComputation(computation);
	}
	
	public boolean isReal() {
		return true;
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.MML_CHOICE_CLASS_URI;
	}
}
