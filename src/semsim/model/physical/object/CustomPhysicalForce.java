package semsim.model.physical.object;

import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalForce;
import semsim.model.physical.PhysicalModelComponent;

/**
 * Class for representing physical forces that are not
 * defined against any knowledge resource term.
 * @author mneal
 *
 */
public class CustomPhysicalForce extends PhysicalForce{


	public CustomPhysicalForce(){
		super(SemSimTypes.CUSTOM_PHYSICAL_FORCE);
	}
	
	/**
	 * Copy constructor
	 * @param force The CustomPhysicalForce to copy
	 */
	public CustomPhysicalForce(CustomPhysicalForce force) {
		super(force);
	}
	
	@Override
	public String getName(){
		return "anonymous force";
	}
	
	@Override
	public PhysicalModelComponent addToModel(SemSimModel model) {
		Set<PhysicalEntity> sources = new HashSet<PhysicalEntity>();
		for (PhysicalEntity entity : getSources()) {
			sources.add(entity.addToModel(model));
		}
		setSources(sources);
		
		Set<PhysicalEntity> sinks = new HashSet<PhysicalEntity>();
		for (PhysicalEntity entity : getSinks()) {
			sinks.add(entity.addToModel(model));
		}
		setSinks(sinks);
		
		return model.addCustomPhysicalForce(this);
	}

	@Override
	public void removeFromModel(SemSimModel model) {
		model.removePhysicalForceFromCache(this);
	}

}
