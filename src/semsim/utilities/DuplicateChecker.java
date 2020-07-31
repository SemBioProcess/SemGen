package semsim.utilities;

import java.util.HashSet;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalEnergyDifferential;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;

/**
 * Utility class for finding and removing duplicate instances of the same annotation in two models
 * The duplicate instance is replaced with the instance in the first model
 * This allows for much faster comparisons during the mapping phase of a merge
 * **/
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
		for (DataStructure ds : model2.getDataStructuresWithCompositesEntities()) {
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
		
		for (PhysicalEnergyDifferential force : model2.getPhysicalForces()){
			removeDuplicateForceParticipants(force,model2);
		}
	}
	
	
	/**
	 * Make sure that process participants are not duplicates of {@link CompositePhysicalEntity}s
	 * already in the model
	 * @param proc A {@link PhysicalProcess} object
	 * @param model The {@link SemSimModel} containing the process
	 */
	private static void removeDuplicateProcessParticipants(PhysicalProcess proc, SemSimModel model) {
		for (PhysicalEntity part : proc.getParticipants()) {
			for (CompositePhysicalEntity proccpe : model.getCompositePhysicalEntities()) {
				if (part.equals(proccpe)) {
					proc.replaceParticipant(part, proccpe);
				}
			}
		}
	}
	
	/**
	 * Make sure that force participants are not duplicates of {@link CompositePhysicalEntity}s
	 * already in the model
	 * @param force A {@link PhysicalEnergyDifferential} object
	 * @param model The {@link SemSimModel} containing the force
	 */
	private static void removeDuplicateForceParticipants(PhysicalEnergyDifferential force, SemSimModel model) {
		for (PhysicalEntity part : force.getParticipants()) {
			for (CompositePhysicalEntity proccpe : model.getCompositePhysicalEntities()) {
				if (part.equals(proccpe)) {
					force.replaceParticipant(part, proccpe);
				}
			}
		}
	}
	
}
