package semsim.model.physical.object;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class CustomPhysicalProcess extends PhysicalProcess{
	public Set<CustomPhysicalEntity> setofinputs = new HashSet<CustomPhysicalEntity>(); // For CB output
	public Set<CustomPhysicalEntity> setofoutputs = new HashSet<CustomPhysicalEntity>(); // For CB output
	public Set<CustomPhysicalEntity> setofmediators = new HashSet<CustomPhysicalEntity>(); // For CB output
	
	public CustomPhysicalProcess(String name, String description){
		super(SemSimTypes.CUSTOM_PHYSICAL_PROCESS);
		setName(name);
		setDescription(description);
	}
	
	public CustomPhysicalProcess(CustomPhysicalProcess cuproc) {
		super(cuproc);
	}
	
	//Add Process and all participants and replace any that have equivalents in the model
	@Override
	public CustomPhysicalProcess addToModel(SemSimModel model) {
		LinkedHashMap<PhysicalEntity, Double> sources = new LinkedHashMap<PhysicalEntity, Double>();
		for (PhysicalEntity entity : this.getSources().keySet()) {
			sources.put(entity.addToModel(model), this.getSourceStoichiometry(entity));
		}
		this.setSources(sources);
		
		LinkedHashMap<PhysicalEntity, Double> sinks = new LinkedHashMap<PhysicalEntity, Double>();
		for (PhysicalEntity entity : this.getSinks().keySet()) {
			sinks.put(entity.addToModel(model), this.getSinkStoichiometry(entity));
		}
		this.setSinks(sinks);
		
		
		Set<PhysicalEntity> mediators = new HashSet<PhysicalEntity>();	
		for (PhysicalEntity entity : this.getMediators()) {
			mediators.add(entity.addToModel(model));
		}
		this.setMediators(mediators);
		
		

		return model.addCustomPhysicalProcess(this);
	}
}
