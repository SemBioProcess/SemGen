package semsim.model.computational;

import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitOfMeasurement;

/**
 * Class for representing discrete occurrences in a simulation
 * that alter the value of one or more data structures.
 * @author mneal
 *
 */
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
	
	/**
	 * Copy constructor
	 * @param eventtocopy The Event to copy
	 */
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
	
	
	/**
	 * @return The MathML statement that triggers the Event
	 */
	public String getTriggerMathML() {
		return eventTriggerMathML;
	}

	/**
	 * Specify the MathML that triggers the Event
	 * @param trigger The MathML trigger
	 */
	public void setTriggerMathML(String trigger) {
		this.eventTriggerMathML = trigger;
	}

	
	/**
	 * @return The set of EventAssignments that accompany the Event
	 */
	public Set<EventAssignment> getEventAssignments() {
		return eventAssignments;
	}

	
	/**
	 * @return Whether there is a MathML statement indicating an Event delay
	 */
	public boolean hasDelayMathML(){
		return (delayMathML != null && ! delayMathML.equals(""));
	}

	
	/**
	 * @return Whether there is a MathML statement indicating Event priority
	 */
	public boolean hasPriorityMathML(){
		return (priorityMathML !=null && ! priorityMathML.equals(""));
	}

	
	/**
	 * Specify the set of EventAssignments that accompany the Event
	 * @param eventAssignments A set of EventAssignments
	 */
	public void setEventAssignments(Set<EventAssignment> eventAssignments) {
		this.eventAssignments.clear();
		this.eventAssignments.addAll(eventAssignments);
	}

	
	/**
	 * Add an EventAssignment to accompany the Event
	 * @param ea An EventAssignment
	 */
	public void addEventAssignment(EventAssignment ea){
		this.getEventAssignments().add(ea);
	}
	
	
	/**
	 * Remove an EventAssignment that accompanies the Event
	 * @param ea The EventAssignment to remove
	 */
	public void removeEventAssignment(EventAssignment ea){
		this.getEventAssignments().remove(ea);
	}
	
	
	/**
	 * @param outputds A data structure
	 * @return The EventAssignment accompanying the Event that alters the
	 * value of the input data structure.
	 */
	public EventAssignment getEventAssignmentForOutput(DataStructure outputds){
		
		for(EventAssignment ea : this.getEventAssignments()){
			
			if(ea.getOutput().equals(outputds)) return ea;
		}
		
		return null;
	}
	
	
	/** @return The MathML indicating the Event's delay*/
	public String getDelayMathML() {
		return delayMathML;
	}


	/**
	 * Set the MathML indicating the Event's delay
	 * @param delayMathML MathML statement
	 */
	public void setDelayMathML(String delayMathML) {
		this.delayMathML = delayMathML;
	}


	/** @return The priority MathML statement associated with the Event  */
	public String getPriorityMathML() {
		return priorityMathML;
	}


	/**
	 * Set the MathML statement indicating the Event's priority
	 * @param priorityMathML A MathML statement
	 */
	public void setPriorityMathML(String priorityMathML) {
		this.priorityMathML = priorityMathML;
	}


	/** @return The temporal unit used when triggering the Event */
	public UnitOfMeasurement getTimeUnit() {
		return timeUnit;
	}


	/**
	 * Specify the temporal unit used when triggering the Event
	 * @param timeUnit The temporal unit
	 */
	public void setTimeUnit(UnitOfMeasurement timeUnit) {
		this.timeUnit = timeUnit;
	}


	@Override
	public Event addToModel(SemSimModel model) {
		return model.addEvent(this);
	}

}
