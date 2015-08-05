package semsim.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.annotation.Annotation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
/**
 * Methods for performing a deep copy of SemSim structures
 * @author Christopher
 *
 */
public class SemSimCopy {
	
	public static Set<Annotation> copyAnnotations(Set<Annotation> annstocopy) {
		Set<Annotation> annset = new HashSet<Annotation>();
		for (Annotation ann : annstocopy) {
			annset.add(new Annotation(ann));
		}
		
		return annset;
	}
	
	public static void copySemSimModelStructures(SemSimModel modeltocopy, SemSimModel destmodel) {
		HashMap<PhysicalEntity, PhysicalEntity> entities = copyPhysicalEntities(modeltocopy);
		destmodel.setPhysicalEntities(new HashSet<PhysicalEntity>(entities.values()));
		
		HashMap<PhysicalProcess, PhysicalProcess> procs = copyPhysicalProcesses(modeltocopy, entities);
		destmodel.setPhysicalProcesses(new HashSet<PhysicalProcess>(procs.values()));
		
		HashMap<UnitOfMeasurement, UnitOfMeasurement> unitmap = copyUnits(modeltocopy.getUnits());
		destmodel.setUnits(new HashSet<UnitOfMeasurement>(unitmap.values()));
	}
	
	private static HashMap<PhysicalEntity, PhysicalEntity> copyPhysicalEntities(SemSimModel modeltocopy) {
		HashMap<PhysicalEntity, PhysicalEntity> entities = new HashMap<PhysicalEntity, PhysicalEntity>();
		for (ReferencePhysicalEntity rpe : modeltocopy.getReferencePhysicalEntities()) {
			entities.put(rpe,rpe);
		}
		for (CustomPhysicalEntity cupe : modeltocopy.getCustomPhysicalEntities()) {
			entities.put(cupe, new CustomPhysicalEntity(cupe));
		}
		for (CompositePhysicalEntity cpe : modeltocopy.getCompositePhysicalEntities()) {
			CompositePhysicalEntity newcpe = new CompositePhysicalEntity(cpe);
			ArrayList<PhysicalEntity> pes = new ArrayList<PhysicalEntity>();
			for (PhysicalEntity pe : cpe.getArrayListOfEntities()) {
				pes.add(entities.get(pe));
			}
			newcpe.setArrayListOfEntities(pes);
			entities.put(cpe, newcpe);
		}

		return entities;
	}
	
	private static HashMap<PhysicalProcess, PhysicalProcess> copyPhysicalProcesses(SemSimModel modeltocopy, HashMap<PhysicalEntity, PhysicalEntity> entities) {
		HashMap<PhysicalProcess, PhysicalProcess> procs = new HashMap<PhysicalProcess, PhysicalProcess>();
		for (ReferencePhysicalProcess rpp : modeltocopy.getReferencePhysicalProcesses()) {
			procs.put(rpp,rpp);
		}
		for (CustomPhysicalProcess cpp : modeltocopy.getCustomPhysicalProcesses()) {
			CustomPhysicalProcess newcpp = new CustomPhysicalProcess(cpp);
			for (PhysicalEntity part : cpp.getParticipants()) {
				newcpp.replaceParticipant(part, entities.get(part));
			}
			procs.put(cpp, newcpp);
		}
		
		return procs;
	}
	
	private static HashMap<UnitOfMeasurement, UnitOfMeasurement> copyUnits(Set<UnitOfMeasurement> units) {
		HashMap<UnitOfMeasurement, UnitOfMeasurement> unitmap = new HashMap<UnitOfMeasurement, UnitOfMeasurement>();
		HashMap<UnitFactor, UnitFactor> ufactormap = new HashMap<UnitFactor, UnitFactor>();
		for (UnitOfMeasurement old : units) {
			UnitOfMeasurement newunit = new UnitOfMeasurement(old);
			unitmap.put(old, newunit);
			
			UnitFactor newuf;
			for (UnitFactor uf : old.getUnitFactors()) {
				if (ufactormap.containsKey(uf)) {
					newuf = ufactormap.get(uf);
				}
				else {
					newuf = new UnitFactor(uf);
					ufactormap.put(uf, newuf);
				}
				newuf.setBaseUnit(newunit);
				newunit.addUnitFactor(newuf);
			}
			newunit.setAnnotations(copyAnnotations(old.getAnnotations()));
		}

		return unitmap;
	}
}
