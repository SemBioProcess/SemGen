package semgen.annotation.workbench.routines;

import java.util.HashSet;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ModelComponentValidator {
	private SemSimModel semsimmodel;
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary library;
	
	private HashSet<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
	private HashSet<PhysicalProcess> procs = new HashSet<PhysicalProcess>();
	
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
				validateComposite(index);
			}
			else {
				validateProcess(index);
			}
		}
	}
	
	private void validateProcess(Integer index) {
		procs.add(library.getPhysicalProcess(index));
		for (Integer i : library.getAllProcessParticipantIndicies(index)) {
			validateComposite(i);
		}
	}
	
	private void validateComposite(Integer index) {
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
	
	private void setModelComponents() {
		semsimmodel.setPhysicalEntities(ents);
		semsimmodel.setPhysicalProcesses(procs);
	}
}
