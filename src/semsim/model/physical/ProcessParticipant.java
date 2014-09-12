package semsim.model.physical;

public class ProcessParticipant extends PhysicalEntity{
	private double multiplier;
	private PhysicalEntity physicalEntity;
	
	public ProcessParticipant(PhysicalEntity ent, double multiplier){
		this.setPhysicalEntity(ent);
		this.multiplier = multiplier;
	}

	public void setPhysicalEntity(PhysicalEntity physicalEntity) {
		this.physicalEntity = physicalEntity;
	}

	public PhysicalEntity getPhysicalEntity() {
		return physicalEntity;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getMultiplier() {
		return multiplier;
	}
}
