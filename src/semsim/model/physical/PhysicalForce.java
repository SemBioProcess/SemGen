package semsim.model.physical;

import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;

/**
 * Class for working with physical forces such as voltages
 * and fluid pressures.
 * @author mneal
 *
 */
public abstract class PhysicalForce extends PhysicalModelComponent {

	private Set<PhysicalEntity> sources = new HashSet<PhysicalEntity>();
	private Set<PhysicalEntity> sinks = new HashSet<PhysicalEntity>();
	
	protected PhysicalForce(SemSimTypes type) {
		super(type);
	}
	
	/**
	 * Copy constructor
	 * @param forcetocopy The PhysicalForce to copy
	 */
	public PhysicalForce(PhysicalForce forcetocopy) {
		super(forcetocopy);
		setSources(forcetocopy.getSources());
		setSinks(forcetocopy.getSinks());
	}
	
	
	public void addSource(PhysicalEntity ent){
		sources.add(ent);
	}
	
	
	public void addSink(PhysicalEntity ent){
		sinks.add(ent);
	}
	
	
	public void setSources(Set<PhysicalEntity> ents){
		sources.clear();
		sources.addAll(ents);
	}
	
	
	public void setSinks(Set<PhysicalEntity> ents){
		sinks.clear();
		sinks.addAll(ents);
	}
	
	
	/**
	 * Replace a {@link PhysicalEntity} force participant with another
	 * @param pe The participant to replace
	 * @param rep The replacement
	 */
	public void replaceParticipant(PhysicalEntity pe, PhysicalEntity rep) {
		if (sources.contains(pe)) {
			sources.remove(pe);
			sources.add(rep);
		}
		if (sinks.contains(pe)) {
			sinks.remove(pe);
			sinks.add(rep);
		}
	}
	
	
	public Set<PhysicalEntity> getSources(){
		return sources;
	}
	
	
	public Set<PhysicalEntity> getSinks(){
		return sinks;
	}
	
	
	public Set<PhysicalEntity> getParticipants(){
		Set<PhysicalEntity> allpents = new HashSet<PhysicalEntity>();
		allpents.addAll(getSources());
		allpents.addAll(getSinks());
		return allpents;
	}
	

	@Override
	public String getComponentTypeAsString() {
		return "force";
	}

	@Override
	public boolean isEquivalent(Object obj) {
		PhysicalForce force = (PhysicalForce)obj;
		if (	(getParticipants().isEmpty() || force.getParticipants().isEmpty()) ||
				(sources.size()!=force.getSources().size()) || 
				(sinks.size()!=force.getSinks().size())) {
			return false;
		}
		for (PhysicalEntity pe : getSources()) {
			boolean hasequiv = false;
			for (PhysicalEntity pe2 : force.getSources()) {
				if (pe.equals(pe2)) {
					hasequiv = true;
					break;
				}
			}
			if (!hasequiv) return false;
		}
		for (PhysicalEntity pe : getSinks()) {
			boolean hasequiv = false;
			for (PhysicalEntity pe2 : force.getSinks()) {
				if (pe.equals(pe2)) {
					hasequiv = true;
					break;
				}
			}
			if (!hasequiv) return false;
		}
		
		return true;
	}

}
