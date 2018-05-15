package semsim.model.physical;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import semsim.definitions.SemSimTypes;

/**
 * Class for working with the physical processes simulated in a model.
 * Analogous to 'OPB:Physics process':
 * A physics processual entity that is the flow or exchange of matter,
 * energy and/or information amongst dynamical entities that are 
 * partcipants in the process.
 * @author mneal
 *
 */
public abstract class PhysicalProcess extends PhysicalModelComponent{
	private LinkedHashMap<PhysicalEntity, Double> sources = new LinkedHashMap<PhysicalEntity, Double>();
	private LinkedHashMap<PhysicalEntity, Double> sinks = new LinkedHashMap<PhysicalEntity, Double>();
	private Set<PhysicalEntity> mediators = new HashSet<PhysicalEntity>();	
	
	protected PhysicalProcess(SemSimTypes type) {
		super(type);
	}
	
	/**
	 * Copy constructor
	 * @param processtocopy The PhysicalProcess to copy
	 */
	public PhysicalProcess(PhysicalProcess processtocopy) {
		super(processtocopy);
		setSources(processtocopy.getSources());
		setSinks(processtocopy.getSinks());
		setMediators(processtocopy.getMediators());
	}
	
	/**
	 * Add a thermodynamic source to the process
	 * @param entity The {@link PhysicalEntity} that is a thermodynamic
	 * source for the process
	 * @param stoichiometry A Double indicating the relationship between 
	 * the relative quantity of the source participating in the process and
	 * the other process participants
	 */
	public void addSource(PhysicalEntity entity, Double stoichiometry){
		sources.put(entity, stoichiometry);
	}
	
	/**
	 * Add a thermodynamic sink to the process
	 * @param entity The {@link PhysicalEntity} that is a thermodynamic
	 * sink for the process
	 * @param stoichiometry A Double indicating the relationship between 
	 * the relative quantity of the sink participating in the process and
	 * the other process participants
	 */
	public void addSink(PhysicalEntity entity, Double stoichiometry){
		sinks.put(entity, stoichiometry);
	}
	
	/**
	 * Add a mediator for the process. Mediators influence the process rate
	 * without being consumed or produced by the process.
	 * @param entity The {@link PhysicalEntity} that is a process mediator
	 */
	public void addMediator(PhysicalEntity entity){
		mediators.add(entity);
	}
	
	/** @return All the thermodynamic sources for the process */
	public Set<PhysicalEntity> getSourcePhysicalEntities(){
		return sources.keySet();
	}
	
	/** @return All the thermodynamic sinks for the process */
	public Set<PhysicalEntity> getSinkPhysicalEntities(){
		return sinks.keySet();
	}
	
	/** @return All the process mediators */
	public Set<PhysicalEntity> getMediatorPhysicalEntities(){
		return mediators;
	}

	/**
	 * Specify the set of {@link PhysicalEntity}s that are thermodynamic
	 * sources for this process
	 * @param newsources A LinkedHashMap that provides the source physical entities
	 * and their stoichiometry in the process
	 */
	public void setSources(LinkedHashMap<PhysicalEntity, Double> newsources) {
		sources = new LinkedHashMap<PhysicalEntity, Double>();
		for (PhysicalEntity pe : newsources.keySet()) {
			this.sources.put(pe, new Double(newsources.get(pe)));
		}
	}

	/** @return All the thermodynamic sources of the process as well as their stoichiometries */
	public LinkedHashMap<PhysicalEntity, Double> getSources() {
		return sources;
	}

	/**
	 * Specify the set of {@link PhysicalEntity}s that are thermodynamic
	 * sinks for this process
	 * @param newsinks A LinkedHashMap that provides the sink physical entities
	 * and their stoichiometry in the process
	 */
	public void setSinks(LinkedHashMap<PhysicalEntity, Double> newsinks) {
		sinks = new LinkedHashMap<PhysicalEntity, Double>();
		for (PhysicalEntity pe : newsinks.keySet()) {
			this.sinks.put(pe, new Double(newsinks.get(pe)));
		}
	}

	/** @return All the thermodynamic sinks of the process as well as
	 * their stoichiometries */
	public LinkedHashMap<PhysicalEntity, Double> getSinks() {
		return sinks;
	}

	/**
	 * Specify the set of {@link PhysicalEntity}s that are mediators for this process
	 * @param newmediators A Set that provides the mediator physical entities
	 */
	public void setMediators(Set<PhysicalEntity> newmediators) {
		mediators = new HashSet<PhysicalEntity>();
		for (PhysicalEntity pe : newmediators) {
			this.mediators.add(pe);
		}
	}

	/** @return All the mediators of the process */
	public Set<PhysicalEntity> getMediators() {
		return mediators;
	}
	
	/**
	 * @param entity A {@link PhysicalEntity} process participant
	 * @return The stoichiometry of a particular {@link PhysicalEntity}
	 * participating in the process
	 */
	public Double getStoichiometry(PhysicalEntity entity) {
		return getParticipantswithMultipliers().get(entity);
	}
	
	/**
	 * @param entity A {@link PhysicalEntity} process participant
	 * that is a thermodynamic source
	 * @return The stoichiometry of a particular thermodynamic source
	 * participating in the process
	 */
	public Double getSourceStoichiometry(PhysicalEntity entity) {
		return sources.get(entity);
	}
	
	/**
	 * @param entity A {@link PhysicalEntity} process participant
	 * that is a thermodynamic sink
	 * @return The stoichiometry of a particular thermodynamic sink
	 * participating in the process
	 */
	public Double getSinkStoichiometry(PhysicalEntity entity) {
		return sinks.get(entity);
	}
	
	/**
	 * Set the stoichiometry for a particular {@link PhysicalEntity} process participant.
	 * @param entity The {@link PhysicalEntity} that will have its stoichiometry set
	 * @param stoich The stoichiometry for the {@link PhysicalEntity}
	 */
	public void setStoichiometry(PhysicalEntity entity, Double stoich) {
		getParticipantswithMultipliers().put(entity,stoich);
	}
	
	/** @return A LinkedHashMap listing all sources and sinks for the process as well 
	 * as their stoichiometries
	 */
	public LinkedHashMap<PhysicalEntity, Double> getParticipantswithMultipliers(){
		LinkedHashMap<PhysicalEntity, Double> allps = new LinkedHashMap<PhysicalEntity, Double>();
		allps.putAll(getSources());
		allps.putAll(getSinks());
		return allps;
	}
	
	/** @return All sources, sinks and mediators in the process */
	public Set<PhysicalEntity> getParticipants(){
		Set<PhysicalEntity> allpents = new HashSet<PhysicalEntity>();
		allpents.addAll(getSourcePhysicalEntities());
		allpents.addAll(getSinkPhysicalEntities());
		allpents.addAll(getMediatorPhysicalEntities());
		return allpents;
	}
	
	/** @return Whether any {@link PhysicalEntity} participants are specified
	 * for the process */
	public boolean hasParticipants(){
		return ! getParticipants().isEmpty();
	}
	
	/**
	 * Remove a {@link PhysicalEntity} participant from the process
	 * @param pe The {@link PhysicalEntity} to remove */
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
	
	/**
	 * Replace a {@link PhysicalEntity} process participant with another
	 * @param pe The participant to replace
	 * @param rep The replacement
	 */
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
		if (	(getParticipants().isEmpty() || proc.getParticipants().isEmpty()) ||
				(sources.size()!=proc.getSources().size()) || 
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
	public String getComponentTypeAsString() {
		return "process";
	}
}
