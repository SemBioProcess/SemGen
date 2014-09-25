package semsim.model.physical;

public class ProcessParticipant extends PhysicalEntity{
	private PhysicalEntity physicalEntity;
	
	public ProcessParticipant(PhysicalEntity ent){
		this.setPhysicalEntity(ent);
	}

	public void setPhysicalEntity(PhysicalEntity physicalEntity) {
		this.physicalEntity = physicalEntity;
	}

	public PhysicalEntity getPhysicalEntity() {
		return physicalEntity;
	}
}
