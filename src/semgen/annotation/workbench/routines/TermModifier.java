package semgen.annotation.workbench.routines;

import java.util.ArrayList;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semsim.definitions.SemSimTypes;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;

public class TermModifier {
	private Integer termindex;
	private AnnotatorWorkbench workbench;
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary library;
	private TermCollector termaffiliates;
	private ArrayList<Integer> blankcpes = new ArrayList<Integer>();
	
	public TermModifier(AnnotatorWorkbench wb, TermCollector collector) {
		workbench = wb;
		library = wb.openTermLibrary();
		drawer = wb.openCodewordDrawer();
		termaffiliates = collector;
		termindex = collector.getTermLibraryIndex();
	}
	
	public void runRemove() {
		workbench.removePhysicalComponentfromModel(library.getComponent(termindex));
		
		switch (library.getSemSimType(termindex)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			removeCompositeEntity();
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			removeSingularEntity();
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			removeProcess();
			break;
		case PHYSICAL_PROPERTY:
			removeSingularPhysicalProperty();
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			removeSingularEntity();
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			removeProcess();
			break;
		default:
			break;
		}
	}
	
	private void removeSingularPhysicalProperty() {
		drawer.batchSetSingularAnnotation(termaffiliates.getCodewordAffiliates(), -1);
		library.removeSingularPhysicalProperty(termindex);
	}
	
	private void removeSingularEntity() {
		for (Integer index : termaffiliates.getCompositeAffiliates()) {
			PhysicalModelComponent pmc = library.getComponent(index);
			if (pmc.getSemSimType().equals(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)) {
				CompositePhysicalEntity cpe = (CompositePhysicalEntity)pmc;
				cpe.removePhysicalEntity((PhysicalEntity) library.getComponent(termindex));
				if (cpe.getArrayListOfEntities().size() == 0) blankcpes.add(index);
			}
		}
		removeSingularfromLibrary();
		
		for (Integer cpei : blankcpes) {
			removeCompositeEntity(cpei);
		}
	}
	
	private void removeSingularfromLibrary() {
		if (library.isReferenceTerm(termindex)) {
			library.removeReferencePhysicalEntity(termindex);
		}
		else {
			library.removeCustomPhysicalEntity(termindex);
		}
	}
	
	private void removeCompositeEntity() {
		drawer.batchSetAssociatedPhysicalComponent(termaffiliates.getCodewordAffiliates(), -1);
		for (Integer procindex : termaffiliates.getCompositeAffiliates()) {
			PhysicalProcess proc = (PhysicalProcess) library.getComponent(procindex);
			proc.removeParticipant((PhysicalEntity) library.getComponent(termindex));
		}
		library.removeCompositePhysicalEntity(termindex);
	}
	
	private void removeCompositeEntity(Integer index) {
		new TermModifier(workbench, workbench.collectAffiliatedTermsandCodewords(index)).runRemove();
	}
	
	private void removeProcess() {
		drawer.batchSetAssociatedPhysicalComponent(termaffiliates.getCodewordAffiliates(), -1);
		library.removePhysicalProcesses(termindex);
	}
	
	//*****************************REPLACE METHODS********************************************//
	
	public void runReplace(int repindex, boolean remove) {
		if (remove) workbench.removePhysicalComponentfromModel(library.getComponent(termindex));
		
		switch (library.getSemSimType(termindex)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			replaceCompositeEntity(repindex, remove);
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			replaceSingularEntity(repindex, remove);
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			replaceProcess(repindex, remove);
			break;
		case PHYSICAL_PROPERTY:
			replaceSingularPhysicalProperty(repindex, remove);
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			replaceAssociatedProperty(repindex);
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			replaceSingularEntity(repindex, remove);
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			replaceProcess(repindex, remove);
			break;
		default:
			break;
		}
	}

	private void replaceSingularPhysicalProperty(int replacement, boolean remove) {
		drawer.batchSetSingularAnnotation(termaffiliates.getCodewordAffiliates(), replacement);
		if (remove) library.removeSingularPhysicalProperty(termindex);
	}
	
	private void replaceAssociatedProperty(int replacement) {
		drawer.batchSetDataStructurePhysicalProperty(termaffiliates.getCodewordAffiliates(), replacement);
	}
	
	private void replaceSingularEntity(int replacement, boolean remove) {
		for (Integer index : termaffiliates.getCompositeAffiliates()) {
			PhysicalModelComponent pmc = library.getComponent(index);
			if (pmc.getSemSimType().equals(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)) {
				CompositePhysicalEntity cpe = (CompositePhysicalEntity)pmc;
				cpe.replacePhysicalEntity((PhysicalEntity)library.getComponent(termindex), (PhysicalEntity)library.getComponent(replacement));
			}
		}
		if (remove) removeSingularfromLibrary();
	}
	
	private void replaceCompositeEntity(int replacement, boolean remove) {
		drawer.batchSetAssociatedPhysicalComponent(termaffiliates.getCodewordAffiliates(), replacement);
		for (Integer procindex : termaffiliates.getCompositeAffiliates()) {
			PhysicalProcess proc = (PhysicalProcess) library.getComponent(procindex);
			proc.removeParticipant((PhysicalEntity) library.getComponent(termindex));
		}
		if (remove) library.removeCompositePhysicalEntity(termindex);
	}
	
	private void replaceProcess(int replacement, boolean remove) {
		drawer.batchSetAssociatedPhysicalComponent(termaffiliates.getCodewordAffiliates(), replacement);
		if (remove) {
			library.removePhysicalProcesses(termindex);
		
		}
	}
}
