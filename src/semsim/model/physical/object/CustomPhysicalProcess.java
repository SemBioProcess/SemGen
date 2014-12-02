package semsim.model.physical.object;

import java.util.HashSet;
import java.util.Set;

import semsim.model.physical.PhysicalProcess;

public class CustomPhysicalProcess extends PhysicalProcess{
	public Set<CustomPhysicalEntity> setofinputs = new HashSet<CustomPhysicalEntity>(); // For CB output
	public Set<CustomPhysicalEntity> setofoutputs = new HashSet<CustomPhysicalEntity>(); // For CB output
	public Set<CustomPhysicalEntity> setofmediators = new HashSet<CustomPhysicalEntity>(); // For CB output
	
	public CustomPhysicalProcess(String name, String description){
		setName(name);
		setDescription(description);
	}
}
