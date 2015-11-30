package semsim.model.physical.object;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimConstants;
import semsim.model.SemSimTypes;
import semsim.model.physical.PhysicalProcess;

public class CustomPhysicalProcess extends PhysicalProcess{
	public Set<CustomPhysicalEntity> setofinputs = new HashSet<CustomPhysicalEntity>(); // For CB output
	public Set<CustomPhysicalEntity> setofoutputs = new HashSet<CustomPhysicalEntity>(); // For CB output
	public Set<CustomPhysicalEntity> setofmediators = new HashSet<CustomPhysicalEntity>(); // For CB output
	
	public CustomPhysicalProcess(String name, String description){
		setName(name);
		setDescription(description);
	}
	
	public CustomPhysicalProcess(CustomPhysicalProcess cuproc) {
		super(cuproc);
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.CUSTOM_PHYSICAL_PROCESS_CLASS_URI;
	}
	@Override
	public SemSimTypes getSemSimType() {
		return SemSimTypes.CUSTOM_PHYSICAL_PROCESS;
	}
}
