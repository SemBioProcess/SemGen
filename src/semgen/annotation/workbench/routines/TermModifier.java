package semgen.annotation.workbench.routines;

import java.util.ArrayList;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;

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
		switch (library.getSemSimType(termindex)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			break;
		case PHYSICAL_PROPERTY:
			removeSingularPhysicalProperty();
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
	
	public void runReplace(int repindex, boolean remove) {
		switch (library.getSemSimType(termindex)) {
		case COMPOSITE_PHYSICAL_ENTITY:
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			break;
		case PHYSICAL_PROPERTY:
			replaceSingularPhysicalProperty(repindex, remove);
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
	
	private void removeSingularPhysicalProperty() {
		drawer.batchSetSingularAnnotation(termaffiliates.getCodewordAffiliates(), -1);
		library.removeSingularPhysicalProperty(termindex);
	}
	
	private void replaceSingularPhysicalProperty(int replacement, boolean remove) {
		drawer.batchSetSingularAnnotation(termaffiliates.getCodewordAffiliates(), replacement);
		if (remove) library.removeSingularPhysicalProperty(termindex);
	}
}
