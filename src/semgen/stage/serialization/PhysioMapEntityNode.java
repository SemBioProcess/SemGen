package semgen.stage.serialization;

import semgen.stage.stagetasks.extractor.Extractor;
import semsim.model.collection.SemSimCollection;
import semsim.model.physical.PhysicalEntity;

public class PhysioMapEntityNode extends LinkableNode<PhysicalEntity> {

	public PhysioMapEntityNode(PhysicalEntity entity, Node<? extends SemSimCollection> parent) {
		super(entity, parent);
		typeIndex = ENTITY;
	}

	@Override
	public void collectforExtraction(Extractor extractor) {
		extractor.addEntity(sourceobj);
		
	}
}
