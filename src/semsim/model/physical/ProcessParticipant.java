package semsim.model.physical;

public class ProcessParticipant extends PhysicalEntity{
<<<<<<< HEAD
	private PhysicalEntity physicalEntity;
	
	public ProcessParticipant(PhysicalEntity ent){
		this.setPhysicalEntity(ent);
=======
	private double multiplier;
	private PhysicalEntity physicalEntity;
	
	public ProcessParticipant(PhysicalEntity ent, double multiplier){
		this.setPhysicalEntity(ent);
		this.multiplier = multiplier;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	}

	public void setPhysicalEntity(PhysicalEntity physicalEntity) {
		this.physicalEntity = physicalEntity;
	}

	public PhysicalEntity getPhysicalEntity() {
		return physicalEntity;
	}
<<<<<<< HEAD
=======

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getMultiplier() {
		return multiplier;
	}
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
}
