package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.Set;

import semsim.model.physical.PhysicalEntity;

public class PhysioMapNode extends Node {

	public PhysioMapNode(String name, String parentModelId, String processName, Set<PhysicalEntity> sources, Set<PhysicalEntity> sinks, Set<PhysicalEntity> mediators) {
		super(name, parentModelId);

		for(PhysicalEntity source : sources) {
			for(PhysicalEntity sink : sinks) {
				ArrayList<String> mediatorsNames = new ArrayList<String>();
				for(PhysicalEntity mediator : mediators) {
					mediatorsNames.add(mediator.getName());
				}

				String sourceName = source.getName();
				String sinkName = sink.getName();
				if(sourceName == "") sourceName = "Null node";
				if(sinkName == "") sinkName = "Null node";
				inputs.add(new Link(
						Node.buildId(sourceName, this.parentModelId),
						Node.buildId(sinkName, this.parentModelId),
						parentModelId,
						processName,
						mediatorsNames));
				System.out.println(sourceName + " -> " + processName + " -> " + sinkName);
			}
		}
	}
}
