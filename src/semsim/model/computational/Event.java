package semsim.model.computational;

import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitOfMeasurement;

public class Event extends ComputationalModelComponent{

	private String eventTriggerMathML;
	private Set<EventAssignment> eventAssignments;
	private String delayMathML;
	private String priorityMathML; // only for SBML Level 3 models
	private UnitOfMeasurement timeUnit;  // only for SBML Level 2 versions 1 and 2
	
	// Constructor
	public Event(){
		super(SemSimTypes.EVENT);
		eventAssignments = new HashSet<EventAssignment>();
	} 
	
	
	
	public String getTriggerMathML() {
		return eventTriggerMathML;
	}

	
	
	public void setTriggerMathML(String trigger) {
		this.eventTriggerMathML = trigger;
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
	
	
	public EventAssignment getEventAssignmentForOutput(DataStructure outputds){
		
		for(EventAssignment ea : this.getEventAssignments()){
			
			if(ea.getOutput().equals(outputds)) return ea;
		}
		
		return null;
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

	
	// Nested subclass for event assignments
	public class EventAssignment extends ComputationalModelComponent{
		private String mathML;
		private DataStructure output;
		
		// Constructor
		public EventAssignment(){
			super(SemSimTypes.EVENT_ASSIGNMENT);
		}

		public String getMathML() {
			return mathML;
		}

		public void setMathML(String mathML) {
			this.mathML = mathML;
		}

		public DataStructure getOutput() {
			return output;
		}

		public void setOutput(DataStructure output) {
			this.output = output;
		}

	}

}
