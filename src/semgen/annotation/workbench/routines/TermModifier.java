package semgen.annotation.workbench.routines;

import java.util.ArrayList;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;

public class TermModifier {
	private Integer termindex;
	private AnnotatorWorkbench workbench;
	private SemSimTermLibrary library;
	private TermCollector termaffiliates;
	
	public TermModifier(AnnotatorWorkbench wb, Integer index) {
		workbench = wb;
		library = wb.openTermLibrary();
		termindex = index;
	}
	
	public void runRemove() {
		switch (library.getSemSimType(termindex)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			break;
		case PHYSICAL_PROPERTY:
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			break;
		default:
			break;
		}
	}
	
	public void runReplace(int repindex) {
		switch (library.getSemSimType(termindex)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			break;
		case PHYSICAL_PROPERTY:
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			break;
		default:
			break;
		}
	}
	

}
