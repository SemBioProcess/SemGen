package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.Set;

import semgen.SemGen;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class PhysioMapNode extends Node {

	public PhysioMapNode(String name, String parentModelId, String processName, Set<PhysicalEntity> sources, Set<PhysicalEntity> mediators) {
		super(name, parentModelId);
		
		for(PhysicalEntity source : sources) {
			ArrayList<String> mediatorsNames = new ArrayList<String>();
			for(PhysicalEntity mediator : mediators) {
				mediatorsNames.add(mediator.getName());
			}
			
			inputs.add(new Link(
					Node.buildId(source.getName(), this.parentModelId),
					processName,
					mediatorsNames));
		}

		
	}
}
