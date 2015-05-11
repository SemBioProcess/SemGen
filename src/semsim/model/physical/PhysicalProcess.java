package semsim.model.physical;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public abstract class PhysicalProcess extends PhysicalModelComponent{
	private LinkedHashMap<PhysicalEntity, Double> sources = new LinkedHashMap<PhysicalEntity, Double>();
	private LinkedHashMap<PhysicalEntity, Double> sinks = new LinkedHashMap<PhysicalEntity, Double>();
	private Set<PhysicalEntity> mediators = new HashSet<PhysicalEntity>();	
	
	public void addSource(PhysicalEntity entity, Double stoichiometry){
		sources.put(entity, stoichiometry);
	}
	
	public void addSink(PhysicalEntity entity, Double stoichiometry){
		sinks.put(entity, stoichiometry);
	}
	
	public void addMediator(PhysicalEntity entity){
		mediators.add(entity);
	}
	
	public Set<PhysicalEntity> getSourcePhysicalEntities(){
		return sources.keySet();
	}
	
	public Set<PhysicalEntity> getSinkPhysicalEntities(){
		return sinks.keySet();
	}
	
	public Set<PhysicalEntity> getMediatorPhysicalEntities(){
		return mediators;
	}

	public void setSources(LinkedHashMap<PhysicalEntity, Double> sources) {
		this.sources = sources;
	}

	public LinkedHashMap<PhysicalEntity, Double> getSources() {
		return sources;
	}

	public void setSinks(LinkedHashMap<PhysicalEntity, Double> sinks) {
		this.sinks = sinks;
	}

	public LinkedHashMap<PhysicalEntity, Double> getSinks() {
		return sinks;
	}

	public void setMediators(Set<PhysicalEntity> mediators) {
		this.mediators = mediators;
	}

	public Set<PhysicalEntity> getMediators() {
		return mediators;
	}
	
	public Double getStoichiometry(PhysicalEntity entity) {
		return getParticipantswithMultipliers().get(entity);
	}
	
	public Double getSourceStoichiometry(PhysicalEntity entity) {
		return sources.get(entity);
	}
	
	public Double getSinkStoichiometry(PhysicalEntity entity) {
		return sinks.get(entity);
	}
	
	public void setStoichiometry(PhysicalEntity entity, Double stoich) {
		getParticipantswithMultipliers().put(entity,stoich);
	}
	
	public LinkedHashMap<PhysicalEntity, Double> getParticipantswithMultipliers(){
		LinkedHashMap<PhysicalEntity, Double> allps = new LinkedHashMap<PhysicalEntity, Double>();
		allps.putAll(getSources());
		allps.putAll(getSinks());
		return allps;
	}
	
	// Get all sources, sinks and mediators as PhysicalEntities
	public Set<PhysicalEntity> getParticipants(){
		Set<PhysicalEntity> allpents = new HashSet<PhysicalEntity>();
		allpents.addAll(getSourcePhysicalEntities());
		allpents.addAll(getSinkPhysicalEntities());
		allpents.addAll(getMediatorPhysicalEntities());
		return allpents;
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		PhysicalProcess proc = (PhysicalProcess)obj;
		if ((sources.size()!=proc.getSources().size()) || 
				(sinks.size()!=proc.getSinks().size()) || 
				(mediators.size()!=proc.getMediators().size())) {
			return false;
		}
		for (PhysicalEntity pe : getSources().keySet()) {
			for (PhysicalEntity pe2 : proc.getSourcePhysicalEntities()) {
				if (pe.isEquivalent(pe2) && (sources.get(pe)==getSourceStoichiometry(pe2)))
					continue;
			}
			return false;
		}
		for (PhysicalEntity pe : getSinks().keySet()) {
			for (PhysicalEntity pe2 : proc.getSinkPhysicalEntities()) {
				if (pe.isEquivalent(pe2) && (sinks.get(pe)==getSinkStoichiometry(pe2)))
					continue;
			}
			return false;
		}
		for (PhysicalEntity pe : getMediators()) {
			for (PhysicalEntity pe2 : proc.getMediatorPhysicalEntities()) {
				if (pe.isEquivalent(pe2))
					continue;
			}
			return false;
		}
		
		return true;
	}

	@Override
	public String getComponentTypeasString() {
		return "process";
	}
}
