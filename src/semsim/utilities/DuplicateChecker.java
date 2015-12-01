package semsim.utilities;

import java.util.HashSet;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;

public class DuplicateChecker {

	public static void removeDuplicatePhysicalEntities(SemSimModel model1, SemSimModel model2) {
		HashSet<PhysicalEntity> pes = new HashSet<PhysicalEntity>();
		for (PhysicalEntity pe2 : model2.getPhysicalEntities()) {
			PhysicalEntity pe = pe2;
			for (PhysicalEntity pe1 : model1.getPhysicalEntities()) {
				if ((pe.hasPhysicalDefinitionAnnotation() || pe.getSemSimType().equals(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)) && pe1.equals(pe)) {
					pe = pe1;
				}
			}
			pes.add(pe);
		}
		model2.setPhysicalEntities(pes);
		for (DataStructure ds : model2.getDataStructureswithCompositesEntities()) {
			for (CompositePhysicalEntity cpe : model2.getCompositePhysicalEntities()) {
				if (ds.getAssociatedPhysicalModelComponent().equals(cpe)) {
					ds.setAssociatedPhysicalModelComponent(cpe);
					break;
				}
			}
		}
		
		for (PhysicalProcess proc : model2.getPhysicalProcesses()) {
			removeDuplicateProcessParticipants(proc, model2);
		}
	}
	
	private static void removeDuplicateProcessParticipants(PhysicalProcess proc, SemSimModel model) {
		for (PhysicalEntity part : proc.getParticipants()) {
				for (CompositePhysicalEntity proccpe : model.getCompositePhysicalEntities()) {
					if (part.equals(proccpe)) {
						proc.replaceParticipant(part, proccpe);
					}
				}
			}
	}
	
}
