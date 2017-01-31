package semsim.model.computational;

import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitOfMeasurement;

public class Event extends ComputationalModelComponent{

	private String eventTriggerMathML;
	private Set<EventAssignment> eventAssignments = new HashSet<EventAssignment>();
	private String delayMathML;
	private String priorityMathML; // only for SBML Level 3 models
	private UnitOfMeasurement timeUnit;  // only for SBML Level 2 versions 1 and 2
	
	// Constructor
	public Event(){
		super(SemSimTypes.EVENT);
	}
	
	// Constructor for copying
	public Event(Event eventtocopy) {
		super(eventtocopy);
				
		if (eventtocopy.eventTriggerMathML != null)
			eventTriggerMathML = new String(eventtocopy.eventTriggerMathML);
		
		if (eventtocopy.eventAssignments != null)
			eventAssignments.addAll(eventtocopy.eventAssignments);

		if (eventtocopy.delayMathML != null)
			delayMathML = eventtocopy.delayMathML;
		
		if (eventtocopy.priorityMathML != null)
			priorityMathML = eventtocopy.priorityMathML;
		
		timeUnit = eventtocopy.timeUnit;
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

	public boolean hasDelayMathML(){
		return (delayMathML != null && ! delayMathML.equals(""));
	}

	public boolean hasPriorityMathML(){
		return (priorityMathML !=null && ! priorityMathML.equals(""));
	}

	public void setEventAssignments(Set<EventAssignment> eventAssignments) {
		this.eventAssignments.clear();
		this.eventAssignments.addAll(eventAssignments);
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
		
		// Constructor for copying
		public EventAssignment(EventAssignment eatocopy){
			super(eatocopy);
			
			if (eatocopy.mathML != null)
				mathML = new String(eatocopy.mathML);
			
			output = eatocopy.output;
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
		@Override
		public void addToModel(SemSimModel model) {
			return;
		}
	}


	@Override
	public void addToModel(SemSimModel model) {
		model.addEvent(this);
	}

}
