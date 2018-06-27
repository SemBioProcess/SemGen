package semsim.model.computational;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;

/**
 * Class for representing the alterations made to data structures
 * when a simulation Event is triggered
 * @author mneal
 */
public class EventAssignment extends ComputationalModelComponent{
	private String mathML;
	private DataStructure output;
	
	// Constructor
	public EventAssignment(){
		super(SemSimTypes.EVENT_ASSIGNMENT);
	}
	
	/**
	 * Copy constructor
	 * @param eatocopy The EventAssignment to copy
	 */
	public EventAssignment(EventAssignment eatocopy){
		super(eatocopy);
		
		if (eatocopy.mathML != null)
			mathML = new String(eatocopy.mathML);
		
		output = eatocopy.output;
	}

	
	/** @return The MathML statement indicating how the value of the 
	 * EventAssignment's associated data structure should be altered
	 * when its associated Event is triggered*/
	public String getMathML() {
		return mathML;
	}

	
	/**
	 * Specify the MathML indicating how the value of the EventAssignment's
	 * associated data structure should be altered.
	 * @param mathML The MathML
	 */
	public void setMathML(String mathML) {
		this.mathML = mathML;
	}

	
	/** @return The DataStructure whose value is to be altered */
	public DataStructure getOutput() {
		return output;
	}

	
	/**
	 * Specify the DataStructure that will have its value altered
	 * by the EventAssignment
	 * @param output A DataStructure
	 */
	public void setOutput(DataStructure output) {
		this.output = output;
	}
	
	
	/** @return Whether the assignment is an SBML initial assignment*/
	public boolean isSBMLinitialAssignment(){
		return false;
	}
	
	
	@Override
	public EventAssignment addToModel(SemSimModel model) {
		return this;
	}
}
