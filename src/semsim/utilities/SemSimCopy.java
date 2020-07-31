package semsim.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import semsim.annotation.Annotation;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalEnergyDifferential;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalForce;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
/**
 * Methods for performing a deep copy of the structures in a SemSim model. This does NOT
 * create an exact copy, use the 'clone' method in SemSimModel instead.
 * @author Christopher
 *
 */
public class SemSimCopy {
	HashMap<PhysicalEntity, PhysicalEntity> entitiesmap = new HashMap<PhysicalEntity, PhysicalEntity>();
	HashMap<PhysicalProcess, PhysicalProcess> processesmap = new HashMap<PhysicalProcess, PhysicalProcess>();
	HashMap<PhysicalEnergyDifferential, PhysicalEnergyDifferential> forcesmap = new HashMap<PhysicalEnergyDifferential,PhysicalEnergyDifferential>();
	HashMap<UnitOfMeasurement, UnitOfMeasurement> unitsmap = new HashMap<UnitOfMeasurement, UnitOfMeasurement>();
	LinkedHashMap<DataStructure, DataStructure> dsmap = new LinkedHashMap<DataStructure, DataStructure>();
	HashMap<Event, Event> eventsmap = new HashMap<Event, Event>();
	LinkedHashMap<Submodel, Submodel> submodelsmap = new LinkedHashMap<Submodel, Submodel>();
	
	SemSimModel modeltocopy;
	SemSimModel destmodel;
	
	public SemSimCopy(SemSimModel source, SemSimModel target) {
		modeltocopy = source;
		destmodel = target;
		copySemSimModelStructures();
	}
	
	/** Copy the physical and computational components from one model to another. */
	private void copySemSimModelStructures() {
		copyPhysicalEntities(modeltocopy);
		destmodel.setPhysicalEntities(new HashSet<PhysicalEntity>(entitiesmap.values()));
		
		copyPhysicalProcesses(modeltocopy);
		destmodel.setPhysicalProcesses(new HashSet<PhysicalProcess>(processesmap.values()));
		
		copyPhysicalForces(modeltocopy);
		destmodel.setPhysicalForces(new HashSet<PhysicalEnergyDifferential>(forcesmap.values()));
		
		copyUnits();
		destmodel.setUnits(new HashSet<UnitOfMeasurement>(unitsmap.values()));
		
		copyDataStructures(modeltocopy);
		destmodel.setAssociatedDataStructures(new ArrayList<DataStructure>(dsmap.values()));
		
		copySubModels();
		destmodel.setSubmodels(submodelsmap.values());
		remap();
		
		copyEvents();
		destmodel.setEvents(new ArrayList<Event>(eventsmap.values()));
				
		copyRelationalConstraints();
		
	}

	
	/**
	 * @param annstocopy A set of {@link Annotation} objects
	 * @return A copy of a set of {@link Annotation} objects
	 */
	public static Set<Annotation> copyAnnotations(Collection<Annotation> annstocopy) {
		Set<Annotation> annset = new HashSet<Annotation>();
		for (Annotation ann : annstocopy) {
			annset.add(new Annotation(ann));
		}
		
		return annset;
	}
	
	
	/**
	 * Copy physical entities present in a SemSimModel
	 * @param modeltocopy The model containing physical entities to copy
	 */
	private void copyPhysicalEntities(SemSimModel modeltocopy) {
		for (ReferencePhysicalEntity rpe : modeltocopy.getReferencePhysicalEntities())
			entitiesmap.put(rpe,rpe);
		
		for (CustomPhysicalEntity cupe : modeltocopy.getCustomPhysicalEntities())
			entitiesmap.put(cupe, new CustomPhysicalEntity(cupe));
		
		for (CompositePhysicalEntity cpe : modeltocopy.getCompositePhysicalEntities()) {
			CompositePhysicalEntity newcpe = new CompositePhysicalEntity(cpe);
			ArrayList<PhysicalEntity> pes = new ArrayList<PhysicalEntity>();
			
			for (PhysicalEntity pe : cpe.getArrayListOfEntities()) {
				
				if (!entitiesmap.containsKey(pe)) {
					
					if (pe.hasPhysicalDefinitionAnnotation())
						entitiesmap.put(pe, pe);
					else
						entitiesmap.put(pe, new CustomPhysicalEntity((CustomPhysicalEntity) pe));
				}
				pes.add(entitiesmap.get(pe));
			}
			newcpe.setArrayListOfEntities(pes);
			entitiesmap.put(cpe, newcpe);
		}
	}
	
	
	/**
	 * Copy physical processes present in a SemSimModel
	 * @param modeltocopy The SemSimModel containing processes to copy
	 */
	private void copyPhysicalProcesses(SemSimModel modeltocopy) {		
		for (ReferencePhysicalProcess rpp : modeltocopy.getReferencePhysicalProcesses()) {
			processesmap.put(rpp,rpp);
		}
		for (CustomPhysicalProcess cpp : modeltocopy.getCustomPhysicalProcesses()) {
			CustomPhysicalProcess newcpp = new CustomPhysicalProcess(cpp);
			for (PhysicalEntity part : cpp.getParticipants()) {
				PhysicalEntity newpart = entitiesmap.get(part);
				if (newpart==null) {
					
				}
				
				newcpp.replaceParticipant(part, newpart);
			}
			processesmap.put(cpp, newcpp);
		}
	}
	
	
	/**
	 * Copy physical forces present in a SemSimModel
	 * @param modeltocopy The SemSimModel containing processes to copy
	 */
	private void copyPhysicalForces(SemSimModel modeltocopy){
		for (CustomPhysicalForce cpf : modeltocopy.getCustomPhysicalForces()) { 

			CustomPhysicalForce newcpf = new CustomPhysicalForce(cpf);
			for (PhysicalEntity part : cpf.getParticipants()) {
				
				PhysicalEntity newpart = entitiesmap.get(part);
				
				if (newpart==null) {}
				
				newcpf.replaceParticipant(part, newpart);
			}
			forcesmap.put(cpf, newcpf);
		}
	}
	
	
	/** Copy in units */
	private void copyUnits() {		
		HashMap<UnitFactor, UnitFactor> ufactormap = new HashMap<UnitFactor, UnitFactor>();
		
		for (UnitOfMeasurement old : modeltocopy.getUnits()) {
			UnitOfMeasurement newunit = new UnitOfMeasurement(old);
			unitsmap.put(old, newunit);
			
			HashSet<UnitFactor> ufset = new HashSet<UnitFactor>();
			
			for (UnitFactor uf : newunit.getUnitFactors()) {
				UnitFactor newuf;
				
				if (ufactormap.containsKey(uf)) {
					newuf = ufactormap.get(uf);
				}
				else {
					newuf = new UnitFactor(uf);
					ufactormap.put(uf, newuf);
				}
				
				ufset.add(newuf);
			}
			newunit.setUnitFactors(ufset);
			newunit.setAnnotations(copyAnnotations(old.getAnnotations()));
		}
		//Set unit factor base units using the new units
		for (UnitFactor uf : ufactormap.values()) {
			uf.setBaseUnit(unitsmap.get(uf.getBaseUnit()));
		}

	}
	
	
	/**
	 * Copy in {@link DataStructure}s from an input {@link SemSimModel}
	 * @param modeltocopy The {@link SemSimModel} containing {@link DataStructure}s to copy
	 */
	private void copyDataStructures(SemSimModel modeltocopy) {
		
		for(DataStructure ds : modeltocopy.getAssociatedDataStructures()) {
			dsmap.put(ds, ds.copy());
		}
		
		remapDataStructures();
	}
	
	
	/**
	 * Establish links between the {@link DataStructure} copies and their associated physical 
	 * model components as well as their units
	 */
	private void remapDataStructures() {
		for (DataStructure ds : dsmap.values()) {
			if (ds.hasAssociatedPhysicalComponent()) {
				
				PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
				
				if (pmc instanceof PhysicalEntity && entitiesmap.containsKey(pmc)) {
					ds.setAssociatedPhysicalModelComponent(entitiesmap.get(pmc));
				}
				else if(pmc instanceof PhysicalProcess){
					ds.setAssociatedPhysicalModelComponent(processesmap.get(pmc));
				}
				else{ // Assume it's a physical force
					ds.setAssociatedPhysicalModelComponent(forcesmap.get(pmc));
				}
			}
			if (ds.hasUnits()) {
				ds.setUnit(unitsmap.get(ds.getUnit()));
			}
		}
	}
	
	
	/**
	 * Copy in info related to the model's {@link Submodel}s
	 */
	private void copySubModels() {
		for (Submodel sm : modeltocopy.getSubmodels()) {
			Submodel newsm;
			
			if (sm.isFunctional()) {
				FunctionalSubmodel fsm = new FunctionalSubmodel((FunctionalSubmodel)sm);
				newsm = fsm;
			}
			else newsm = new Submodel(sm);
			
			submodelsmap.put(sm, newsm);
		}
		for (Submodel sm : submodelsmap.values()) sm.replaceSubmodels(submodelsmap);
		
	}
	
	
	/** Replaces data structures in the destination model with those copied 
	 * from the source model */
	private void remap() {		
		this.destmodel.replaceDataStructures(dsmap);
	}
	
	
	/** Copy in info related to {@link Event}s */
	private void copyEvents(){
		
		for(Event oldev : modeltocopy.getEvents()){
			Event newev = new Event(oldev);
			eventsmap.put(oldev, newev);
			
			if(oldev.getTimeUnit() != null)
				newev.setTimeUnit(unitsmap.get(oldev.getTimeUnit()));
			
			// Reassign the new data structure output instances to the event assignments
			for(EventAssignment newea : newev.getEventAssignments()){
				DataStructure oldds = newea.getOutput();
				newea.setOutput(dsmap.get(oldds));
			}
		}
		
		// Replace the old events with the new ones in all Computations
		for(DataStructure newds : destmodel.getAssociatedDataStructures()){
			
			Computation newcomp = newds.getComputation();
			Set<Event> neweventset = new HashSet<Event>();
			
			// The new Computation instances refer to the old Events here. Replace with new Events.
			for(Event oldevent : newcomp.getEvents())
				neweventset.add(eventsmap.get(oldevent));
			
			newcomp.setEvents(neweventset);
		}
	}
	

	/** Copy in info related to relational constraints */
	private void copyRelationalConstraints() {
		HashMap<RelationalConstraint, RelationalConstraint> relconmap = new HashMap<RelationalConstraint, RelationalConstraint>();
		for (RelationalConstraint recon : modeltocopy.getRelationalConstraints()) {
			RelationalConstraint newrecon = new RelationalConstraint(recon);
			newrecon.replaceAllDataStructures(dsmap);
			relconmap.put(recon, newrecon);
		}
		destmodel.setRelationalConstraints(relconmap.values());
	}

}
