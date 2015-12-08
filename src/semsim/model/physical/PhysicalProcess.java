package semsim.model.physical;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import semsim.definitions.SemSimTypes;

public abstract class PhysicalProcess extends PhysicalModelComponent{
	private LinkedHashMap<PhysicalEntity, Double> sources = new LinkedHashMap<PhysicalEntity, Double>();
	private LinkedHashMap<PhysicalEntity, Double> sinks = new LinkedHashMap<PhysicalEntity, Double>();
	private Set<PhysicalEntity> mediators = new HashSet<PhysicalEntity>();	
	
	protected PhysicalProcess(SemSimTypes type) {
		super(type);
	}
	
	public PhysicalProcess(PhysicalProcess processtocopy) {
		super(processtocopy);
		setSources(processtocopy.getSources());
		setSinks(processtocopy.getSinks());
		setMediators(processtocopy.getMediators());
	}
	
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

	public void setSources(LinkedHashMap<PhysicalEntity, Double> newsources) {
		sources = new LinkedHashMap<PhysicalEntity, Double>();
		for (PhysicalEntity pe : newsources.keySet()) {
			this.sources.put(pe, new Double(newsources.get(pe)));
		}
	}

	public LinkedHashMap<PhysicalEntity, Double> getSources() {
		return sources;
	}

	public void setSinks(LinkedHashMap<PhysicalEntity, Double> newsinks) {
		sinks = new LinkedHashMap<PhysicalEntity, Double>();
		for (PhysicalEntity pe : newsinks.keySet()) {
			this.sinks.put(pe, new Double(newsinks.get(pe)));
		}
	}

	public LinkedHashMap<PhysicalEntity, Double> getSinks() {
		return sinks;
	}

	public void setMediators(Set<PhysicalEntity> newmediators) {
		mediators = new HashSet<PhysicalEntity>();
		for (PhysicalEntity pe : newmediators) {
			this.mediators.add(pe);
		}
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
	
	public void removeParticipant(PhysicalEntity pe) {
		if (sources.keySet().contains(pe)) {
			sources.remove(pe);
		}
		if (sinks.keySet().contains(pe)) {
			sinks.remove(pe);
		}
		if (mediators.contains(pe)) {
			mediators.remove(pe);
		}
	}
	
	public void replaceParticipant(PhysicalEntity pe, PhysicalEntity rep) {
		if (sources.keySet().contains(pe)) {
			Double mult = sources.get(pe);
			sources.remove(pe);
			sources.put(rep, mult);
		}
		if (sinks.keySet().contains(pe)) {
			Double mult = sinks.get(pe);
			sinks.remove(pe);
			sinks.put(rep, mult);
		}
		if (mediators.contains(pe)) {
			mediators.remove(pe);
			mediators.add(rep);
		}
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
			boolean hasequiv = false;
			for (PhysicalEntity pe2 : proc.getSourcePhysicalEntities()) {
				if (pe.equals(pe2) && sources.get(pe).equals(proc.getSourceStoichiometry(pe2))) {
					hasequiv = true;
					break;
				}
			}
			if (!hasequiv) return false;
		}
		for (PhysicalEntity pe : getSinks().keySet()) {
			boolean hasequiv = false;
			for (PhysicalEntity pe2 : proc.getSinkPhysicalEntities()) {
				if (pe.equals(pe2) && sinks.get(pe).equals(proc.getSinkStoichiometry(pe2))) {
					hasequiv = true;
					break;
				}
			}
			if (!hasequiv) return false;
		}
		for (PhysicalEntity pe : getMediators()) {
			boolean hasequiv = false;
			for (PhysicalEntity pe2 : proc.getMediatorPhysicalEntities()) {
				if (pe.equals(pe2)) {
					hasequiv = true;
					break;
				}
			}
			if (!hasequiv) return false;
		}
		
		return true;
	}

	@Override
	public String getComponentTypeasString() {
		return "process";
	}
}
