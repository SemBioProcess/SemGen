package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.Set;

import semsim.model.physical.PhysicalEntity;

public class PhysioMapNode extends Node {

	public PhysioMapNode(String name, String parentModelId, String processName, Set<PhysicalEntity> sources, Set<PhysicalEntity> mediators) {
		super(name, parentModelId);
		
		if(sources != null) {
			for(PhysicalEntity source : sources) {
				ArrayList<String> mediatorsNames = new ArrayList<String>();
				for(PhysicalEntity mediator : mediators) {
					mediatorsNames.add(mediator.getName());
				}

				inputs.add(new Link(
						Node.buildId(source.getName(), this.parentModelId),
						parentModelId,
						processName,
						mediatorsNames));
			}
		}
		
	}
}
