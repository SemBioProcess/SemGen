package semsim.model.physical;

import java.util.HashSet;
import java.util.Set;

public class PhysicalProcess extends PhysicalModelComponent{

	private Set<SourceParticipant> sources = new HashSet<SourceParticipant>();
	private Set<SinkParticipant> sinks = new HashSet<SinkParticipant>();;
	private Set<MediatorParticipant> mediators = new HashSet<MediatorParticipant>();
	
	public void addSource(PhysicalEntity entity){
		sources.add(new SourceParticipant(entity));
	}
	
	public void addSink(PhysicalEntity entity){
		sinks.add(new SinkParticipant(entity));
	}
	
	public void addMediator(PhysicalEntity entity){
		mediators.add(new MediatorParticipant(entity));
	}
	
	public Set<PhysicalEntity> getSourcePhysicalEntities(){
		Set<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
		for(SourceParticipant sp : getSources()){
			ents.add(sp.getPhysicalEntity());
		}
		return ents;
	}
	
	public Set<PhysicalEntity> getSinkPhysicalEntities(){
		Set<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
		for(SinkParticipant sp : getSinks()){
			ents.add(sp.getPhysicalEntity());
		}
		return ents;
	}
	
	public Set<PhysicalEntity> getMediatorPhysicalEntities(){
		Set<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
		for(MediatorParticipant sp : getMediators()){
			ents.add(sp.getPhysicalEntity());
		}
		return ents;
	}

	public void setSources(Set<SourceParticipant> sources) {
		this.sources = sources;
	}

	public Set<SourceParticipant> getSources() {
		return sources;
	}

	public void setSinks(Set<SinkParticipant> sinks) {
		this.sinks = sinks;
	}

	public Set<SinkParticipant> getSinks() {
		return sinks;
	}

	public void setMediators(Set<MediatorParticipant> mediators) {
		this.mediators = mediators;
	}

	public Set<MediatorParticipant> getMediators() {
		return mediators;
	}
	
	public Set<ProcessParticipant> getParticipants(){
		Set<ProcessParticipant> allps = new HashSet<ProcessParticipant>();
		allps.addAll(getSources());
		allps.addAll(getSinks());
		allps.addAll(getMediators());
		return allps;
	}
	
	// Get all sources, sinks and mediators as PhysicalEntities
	public Set<PhysicalEntity> getParticipantsAsPhysicalEntities(){
		Set<PhysicalEntity> allpents = new HashSet<PhysicalEntity>();
		allpents.addAll(getSourcePhysicalEntities());
		allpents.addAll(getSinkPhysicalEntities());
		allpents.addAll(getMediatorPhysicalEntities());
		return allpents;
	}
}
