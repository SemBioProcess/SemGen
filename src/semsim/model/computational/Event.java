package semsim.model.computational;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.SemSimConstants;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitOfMeasurement;

public class Event extends ComputationalModelComponent{

	private EventTrigger trigger;
	private Set<EventAssignment> eventAssignments;
	private String delayMathML;
	private String priorityMathML; // only for SBML Level 3 models
	private UnitOfMeasurement timeUnit;  // only for SBML Level 2 versions 1 and 2
	
	// Constructor
	public Event(){
		setTrigger(new EventTrigger());
		eventAssignments = new HashSet<EventAssignment>();
	} 
	
	
	
	public EventTrigger getTrigger() {
		return trigger;
	}

	
	
	public void setTrigger(EventTrigger trigger) {
		this.trigger = trigger;
	}



	public Set<EventAssignment> getEventAssignments() {
		return eventAssignments;
	}



	public void setEventAssignments(Set<EventAssignment> eventAssignments) {
		this.eventAssignments = eventAssignments;
	}



	public void addEventAssignment(EventAssignment ea){
		this.getEventAssignments().add(ea);
	}
	
	
	
	public void removeEventAssignment(EventAssignment ea){
		this.getEventAssignments().remove(ea);
	}
	
	
	
	public String getDelayMathML() {
		return delayMathML;
	}



	public void setDelayMathML(String delayMathML) {
		this.delayMathML = delayMathML;
	}



	public String getPriorityMathML() {
		return priorityMathML;
	}



	public void setPriorityMathML(String priorityMathML) {
		this.priorityMathML = priorityMathML;
	}



	public UnitOfMeasurement getTimeUnit() {
		return timeUnit;
	}



	public void setTimeUnit(UnitOfMeasurement timeUnit) {
		this.timeUnit = timeUnit;
	}



	// Nested subclass for event trigger
	public class EventTrigger{
		private String mathML;
		private Set<DataStructure> inputs;

		// Constructor
		public EventTrigger(){
			inputs = new HashSet<DataStructure>();
		} 
		
		public String getMathML() {
			return mathML;
		}

		public void setMathML(String mathML) {
			this.mathML = mathML;
		}

		public Set<DataStructure> getInputs() {
			return inputs;
		}

		public void setInputs(Set<DataStructure> inputs) {
			this.inputs = inputs;
		}
		
		public void addInput(DataStructure input){
			inputs.add(input);
		}
	}
	
	// Nested subclass for event assignments
	public class EventAssignment{
		private String mathML;
		private Set<DataStructure> inputs;
		private DataStructure output;
		
		// Constructor
		public EventAssignment(){
			inputs = new HashSet<DataStructure>();
		}

		public String getMathML() {
			return mathML;
		}

		public void setMathML(String mathML) {
			this.mathML = mathML;
		}

		public Set<DataStructure> getInputs() {
			return inputs;
		}

		public void setInputs(Set<DataStructure> inputs) {
			this.inputs = inputs;
		}
		
		public void addInput(DataStructure input){
			inputs.add(input);
		}

		public DataStructure getOutput() {
			return output;
		}

		public void setOutput(DataStructure output) {
			this.output = output;
		}
	}
	
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.EVENT_CLASS_URI;
	}

}
