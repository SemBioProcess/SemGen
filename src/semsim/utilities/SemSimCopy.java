package semsim.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import semsim.annotation.Annotation;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.Event.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
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
 * Methods for performing a deep copy the structures in a SemSim model. This does NOT
 * create an exact copy, use the 'clone' method in SemSimModel instead.
 * @author Christopher
 *
 */
public class SemSimCopy {
	HashMap<PhysicalEntity, PhysicalEntity> entities = new HashMap<PhysicalEntity, PhysicalEntity>();
	HashMap<PhysicalProcess, PhysicalProcess> procs = new HashMap<PhysicalProcess, PhysicalProcess>();
	HashMap<UnitOfMeasurement, UnitOfMeasurement> unitmap = new HashMap<UnitOfMeasurement, UnitOfMeasurement>();
	LinkedHashMap<DataStructure, DataStructure> dsmap = new LinkedHashMap<DataStructure, DataStructure>();
	HashMap<Computation, Computation> compmap = new HashMap<Computation, Computation>();
	HashMap<Event, Event> eventmap = new HashMap<Event, Event>();
	LinkedHashMap<Submodel, Submodel> smmap = new LinkedHashMap<Submodel, Submodel>();
	
	SemSimModel modeltocopy;
	SemSimModel destmodel;
	
	public SemSimCopy(SemSimModel source, SemSimModel target) {
		modeltocopy = source;
		destmodel = target;
		copySemSimModelStructures();
	}
	
	/**
	 * Method for copying the physical and computational components from one model to another. 
	 * @param modeltocopy
	 * @param destmodel
	 */
	private void copySemSimModelStructures() {
		copyPhysicalEntities(modeltocopy);
		destmodel.setPhysicalEntities(new HashSet<PhysicalEntity>(entities.values()));
		
		copyPhysicalProcesses(modeltocopy);
		destmodel.setPhysicalProcesses(new HashSet<PhysicalProcess>(procs.values()));
		
		copyUnits();
		destmodel.setUnits(new HashSet<UnitOfMeasurement>(unitmap.values()));
		
		copyDataStructures(modeltocopy);
		destmodel.setAssociatedDataStructures(new ArrayList<DataStructure>(dsmap.values()));
		
		copySubModels();
		destmodel.setSubmodels(new ArrayList<Submodel>(smmap.values()));
		remapSubmodels();
		
		copyEvents();
		destmodel.setEvents(new ArrayList<Event>(eventmap.values()));
		
		copyComputations();
		
		copyRelationalConstraints();
		
	}

	public static Set<Annotation> copyAnnotations(Collection<Annotation> annstocopy) {
		Set<Annotation> annset = new HashSet<Annotation>();
		for (Annotation ann : annstocopy) {
			annset.add(new Annotation(ann));
		}
		
		return annset;
	}
	
	private void copyPhysicalEntities(SemSimModel modeltocopy) {
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
				if (!entities.containsKey(pe)) {
					if (pe.hasPhysicalDefinitionAnnotation()) {
						entities.put(pe, pe);
					}
					else {
						entities.put(pe, new CustomPhysicalEntity((CustomPhysicalEntity) pe));
					}
				}
				pes.add(entities.get(pe));
			}
			newcpe.setArrayListOfEntities(pes);
			entities.put(cpe, newcpe);
		}
	}
	
	private void copyPhysicalProcesses(SemSimModel modeltocopy) {		
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
		
	}
	
	private void copyUnits() {		
		HashMap<UnitFactor, UnitFactor> ufactormap = new HashMap<UnitFactor, UnitFactor>();
		
		for (UnitOfMeasurement old : modeltocopy.getUnits()) {
			UnitOfMeasurement newunit = new UnitOfMeasurement(old);
			unitmap.put(old, newunit);
			
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
			uf.setBaseUnit(unitmap.get(uf.getBaseUnit()));
		}

	}
	
	private void copyDataStructures(SemSimModel modeltocopy) {
		for(DataStructure ds : modeltocopy.getAssociatedDataStructures()) {
			dsmap.put(ds, ds.copy());
		}
		
		remapDataStructures();
	}
	
	private void remapDataStructures() {
		for (DataStructure ds : dsmap.values()) {
			Computation comp = ds.getComputation();
			if (comp!=null) {
				if (compmap.containsKey(comp)) {
					ds.setComputation(compmap.get(comp));
				} 
				else {
					Computation newcomp = new Computation(comp);
					ds.setComputation(newcomp);
					compmap.put(comp, newcomp);
				}
			}
			if (ds.hasAssociatedPhysicalComponent()) {
				if (entities.containsKey(ds.getAssociatedPhysicalModelComponent())) {
					ds.setAssociatedPhysicalModelComponent(entities.get(ds.getAssociatedPhysicalModelComponent()));
				}
				else {
					ds.setAssociatedPhysicalModelComponent(procs.get(ds.getAssociatedPhysicalModelComponent()));
				}
			}
			if (ds.hasSolutionDomain()) {
				ds.setSolutionDomain(dsmap.get(ds.getSolutionDomain()));
			}
			if (ds.hasUnits()) {
				ds.setUnit(unitmap.get(ds.getUnit()));
			}
			HashSet<DataStructure> used = new HashSet<DataStructure>();
			for (DataStructure utc : ds.getUsedToCompute()) {
				used.add(dsmap.get(utc));
			}
			ds.setUsedToCompute(used);
		}
	}
	
	private void copyComputations() {
		for (Computation comp : compmap.values()) {
			HashSet<DataStructure> inputs = new HashSet<DataStructure>();
			for (DataStructure in : comp.getInputs()) {
				if (dsmap.get(in) == null) {
					for (DataStructure key : dsmap.keySet()) {
						if (key.getName().equals(in.getName())) {
							in = key;
							break;
						}
					}
					
				}
				inputs.add(dsmap.get(in));
			}
			HashSet<DataStructure> outputs = new HashSet<DataStructure>();
			for (DataStructure out : comp.getOutputs()) {
				outputs.add(dsmap.get(out));
			}
			comp.setInputs(inputs);
			comp.setOutputs(outputs);
		}
	}
	
	private void copySubModels() {
		for (Submodel sm : modeltocopy.getSubmodels()) {
			Submodel newsm;
			if (sm.isFunctional()) {
				FunctionalSubmodel fsm = new FunctionalSubmodel(sm);
				if (!compmap.containsKey(fsm.getComputation())) {
					Computation newcomp = new Computation(fsm.getComputation());
					compmap.put(fsm.getComputation(), newcomp);
					fsm.setComputation(newcomp);
				}
				else {
					fsm.setComputation(compmap.get(fsm.getComputation()));
				}
				newsm = fsm;
			}
			else {
				newsm = new Submodel(sm);
			}
			smmap.put(sm, newsm);
		}
	}
	
	private void remapSubmodels() {
		
		// For each submodel copy, associate it with data structure copies
		for (Submodel newsm : smmap.values()) {
			ArrayList<DataStructure> dsset = new ArrayList<DataStructure>();
			
			for (DataStructure ds : newsm.getAssociatedDataStructures()) {
				dsset.add(dsmap.get(ds));
			}
			
			// Associate it with the submodel copies
			ArrayList<Submodel> smset = new ArrayList<Submodel>();
			
			for (Submodel assocsm : newsm.getSubmodels()) {
				smset.add(smmap.get(assocsm));
			}
			newsm.setAssociatedDataStructures(dsset);
			newsm.setSubmodels(smset);
			
			// If functional, establish its model subsumption map using the 
			// submodel copies
			if (newsm.isFunctional()) {
				FunctionalSubmodel fsm = (FunctionalSubmodel)newsm;
				Map<String, Set<FunctionalSubmodel>> oldrelsmmap = fsm.getRelationshipSubmodelMap();
				Map<String, Set<FunctionalSubmodel>> relsmmap = new HashMap<String, Set<FunctionalSubmodel>>();
				for (String rel : oldrelsmmap.keySet()) {
					Set<FunctionalSubmodel> rsmset = new HashSet<FunctionalSubmodel>();
					for (FunctionalSubmodel rfsm : oldrelsmmap.get(rel)) {
						rsmset.add((FunctionalSubmodel) smmap.get(rfsm));
					}
					relsmmap.put(new String(rel), rsmset);
				}
				fsm.setRelationshipSubmodelMap(relsmmap);
			}
		}
	}
	
	private void copyEvents(){
		
		for(Event oldev : modeltocopy.getEvents()){
			Event newev = new Event(oldev);
			eventmap.put(oldev, newev);
			
			if(oldev.getTimeUnit() != null)
				newev.setTimeUnit(unitmap.get(oldev.getTimeUnit()));
			
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
				neweventset.add(eventmap.get(oldevent));
			
			newcomp.setEvents(neweventset);
		}
	}
	

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
