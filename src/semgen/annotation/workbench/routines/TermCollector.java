package semgen.annotation.workbench.routines;

import java.util.ArrayList;

import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semsim.annotation.SemSimTermLibrary;
import semsim.definitions.SemSimTypes;
import semsim.model.computational.datastructures.DataStructure;

public class TermCollector {
	private Integer termindex;
	private CodewordToolDrawer drawer;
	private SemSimTermLibrary library;
	private ArrayList<Integer> containingindicies = new ArrayList<Integer>();
	private ArrayList<Integer> containingcws = new ArrayList<Integer>();
			
	public TermCollector(AnnotatorWorkbench wb, Integer index) {
		drawer = wb.openCodewordDrawer();
		library = wb.openTermLibrary();
		termindex = index;
		
		collect();
	}
	
	/** 
	 * 
	 * @return copy of the affiliated codewords
	 */
	
	public ArrayList<Integer> getCodewordAffiliates() {
		return new ArrayList<Integer>(containingcws);
	}
	
	/** 
	 * 
	 * @return copy of the affiliated composites
	 */
	
	public ArrayList<Integer> getCompositeAffiliates() {
		return new ArrayList<Integer>(containingindicies);
	}
	
	public boolean isUsed() {
		return containingcws.size()>0;
	}
	
	/** 
	 * 
	 * @return copy of the target term index
	 */
	
	public Integer getTermLibraryIndex() {
		return new Integer(termindex);
	}
	
	public SemSimTypes getTargetTermType() {
		return library.getSemSimType(termindex);
	}
	
	public boolean targetIsReferenceTerm() {
		return library.isReferenceTerm(termindex);
	}
	
	public ArrayList<String> getCodewordNames() {
		return drawer.getComponentNamesfromIndicies(containingcws);
	}
	
	public ArrayList<String> getCompositeNames() {
		return library.getComponentNames(containingindicies);
	}
	
	//***********************************COLLECTION ROUTINES********************************//
	
	private void collect() {
		switch (library.getSemSimType(termindex)) {
		case PHYSICAL_PROPERTY:
			collectCodewordswithSingularProperty();
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			collectAssociateProperties();
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			collectforSingularEntity();
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			collectforSingularEntity();
			break;
		case COMPOSITE_PHYSICAL_ENTITY:
			collectforCompositeEntity();
			break;
		case CUSTOM_PHYSICAL_PROCESS:
			collectforProcess();
			break;
		case REFERENCE_PHYSICAL_PROCESS:
			collectforProcess();
			break;
		default:
			break;
		}
	}
	
	private void collectforSingularEntity() {
		collectCompositesContainingSingularEntity();
		collectProcessesContainingCPEs();
		collectForcesContainingCPEs();
		collectPhysicalModelComponentsOfDataStructures();
	}
	
	private void collectforCompositeEntity() {
		containingindicies.add(termindex);
		collectProcessesContainingCPEs();
		collectForcesContainingCPEs();
		collectPhysicalModelComponentsOfDataStructures();
		containingindicies.remove(termindex);
	}
	
	private void collectforProcess() {
		containingindicies.add(termindex);
		collectPhysicalModelComponentsOfDataStructures();
		containingindicies.remove(termindex);
	}
	
	private void collectCodewordswithSingularProperty() {
		for (DataStructure ds : drawer.getCodewords()) {
			if (ds.hasPhysicalDefinitionAnnotation()) {
				Integer si = library.getPhysicalPropertyIndex(ds.getSingularTerm());
				if (si.equals(termindex)) {
					containingcws.add(drawer.getIndexofComponent(ds));
				}
			}
		}
	}
	
	private void collectAssociateProperties() {
		for (DataStructure ds : drawer.getCodewords()) {
			if (ds.hasPhysicalProperty()) {
				Integer si = library.getPhysicalPropertyIndex(ds.getPhysicalProperty());
				if (si.equals(termindex)) {
					containingcws.add(drawer.getIndexofComponent(ds));
					if (ds.hasAssociatedPhysicalComponent()) {
						Integer i = library.getComponentIndex(ds.getAssociatedPhysicalModelComponent(), true);
						if (!containingindicies.contains(i)) {
							containingindicies.add(i);
						}
					}
				}
			}
		}
	}
	
	private void collectCompositesContainingSingularEntity() {
		for (Integer i : library.getSortedCompositePhysicalEntityIndicies()) {
			if (library.compositeEntityContainsSingular(i, termindex)) {
				containingindicies.add(i);
			}
		}
	}
	
	private void collectProcessesContainingCPEs() {
		ArrayList<Integer> cpes = new ArrayList<Integer>(containingindicies);
		
		for (Integer proc : library.getSortedPhysicalProcessIndicies()) {
			
			ArrayList<Integer> parts = library.getAllProcessParticipantIndicies(proc);
			
			for (Integer cpe : cpes) {
				
				if (parts.contains(cpe)) {
					containingindicies.add(proc);
					break;
				}
			}
		}
	}
	
	private void collectForcesContainingCPEs() {
		ArrayList<Integer> cpes = new ArrayList<Integer>(containingindicies);
		for (Integer force : library.getPhysicalForceIndicies()) {
			
			ArrayList<Integer> parts = library.getAllForceParticipantIndicies(force);
			
			for (Integer cpe : cpes) {
				
				if (parts.contains(cpe)) {
					containingindicies.add(force);
					break;
				}
			}
		}
	}
	
	private void collectPhysicalModelComponentsOfDataStructures() {
		for (DataStructure ds : drawer.getCodewords()) {
			collectDataStructurePhysicalModelComponent(ds);
		}
	}
	
	private void collectDataStructurePhysicalModelComponent(DataStructure ds) {
		if (ds.hasAssociatedPhysicalComponent()) {
				Integer pmci = library.getComponentIndex(ds.getAssociatedPhysicalModelComponent(), true);
				if (containingindicies.contains(pmci)) {
					containingcws.add(drawer.getIndexofComponent(ds));
				}
			}
	}
}
