package semgen.annotation.workbench.routines;

import java.util.HashSet;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semsim.annotation.SemSimTermLibrary;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalEnergyDifferential;
import semsim.model.physical.PhysicalProcess;

public class ModelComponentValidator {
	private SemSimModel semsimmodel;
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary library;
	
	private HashSet<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
	private HashSet<PhysicalProcess> procs = new HashSet<PhysicalProcess>();
	private HashSet<PhysicalEnergyDifferential> forces = new HashSet<PhysicalEnergyDifferential>();
	
	public ModelComponentValidator(AnnotatorWorkbench wb, SemSimModel mod) {
		library = wb.openTermLibrary();
		drawer = wb.openCodewordDrawer();
		semsimmodel = mod;
		
		validate();
		setModelComponents();
	}
	
	private void validate() {
		for (Integer index : drawer.getAllAssociatedComposites()) {
			SemSimTypes type = library.getSemSimType(index);
			if (type==SemSimTypes.COMPOSITE_PHYSICAL_ENTITY) {
				validateCompositeEntity(index);
			}
			else if(type==SemSimTypes.CUSTOM_PHYSICAL_FORCE){
				validateForce(index);
			}
			else validateProcess(index);
		}
	}
	
	
	
	private void validateCompositeEntity(Integer index) {
		ents.add(library.getCompositePhysicalEntity(index));
		for (Integer i : library.getCompositeEntityIndicies(index)) {
			if (library.getSemSimType(i)==SemSimTypes.CUSTOM_PHYSICAL_ENTITY) {
				ents.add(library.getCustomPhysicalEntity(i));
			}
			else {
				ents.add(library.getReferencePhysicalEntity(i));
			}
		}
	}
	
	private void validateProcess(Integer index) {
		procs.add(library.getPhysicalProcess(index));
		for (Integer i : library.getAllProcessParticipantIndicies(index)) {
			validateCompositeEntity(i);
		}
	}
	
	private void validateForce(Integer index){
		forces.add(library.getPhysicalForce(index));
		for(Integer i : library.getAllForceParticipantIndicies(index)){
			validateCompositeEntity(i);
		}
	}
	
	
	private void setModelComponents() {
		semsimmodel.setPhysicalEntities(ents);
		semsimmodel.setPhysicalProcesses(procs);
	}
}
