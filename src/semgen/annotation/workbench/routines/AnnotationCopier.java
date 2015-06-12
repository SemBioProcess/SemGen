package semgen.annotation.workbench.routines;


import java.util.HashSet;
import java.util.Set;

import semgen.annotation.workbench.SemSimTermLibrary;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.object.PhysicalProperty;

public class AnnotationCopier {
	
	/** 
	 * Intra-model datastructure copy
	 * */
	public static void copyCompositeAnnotation(DataStructure targetds, DataStructure sourceds) {
		if (sourceds.hasPhysicalProperty()) {
			targetds.setAssociatePhysicalProperty(sourceds.getPhysicalProperty());
		}
		if (sourceds.hasAssociatedPhysicalComponent()) {
			targetds.setAssociatedPhysicalModelComponent(sourceds.getAssociatedPhysicalModelComponent());
		}
	}
	
	/** 
	 * Inter-model datastructure copy 
	 * */
	public static void copyCompositeAnnotation(SemSimTermLibrary lib, DataStructure targetds, DataStructure sourceds) {		
		if (sourceds.hasPhysicalProperty()) {
			int ppindex = lib.getPhysicalPropertyIndex(sourceds.getPhysicalProperty());
			targetds.setAssociatePhysicalProperty(lib.getAssociatePhysicalProperty(ppindex));
		}
		if (sourceds.hasAssociatedPhysicalComponent()) {
			int pmcindex = lib.getComponentIndex(sourceds.getAssociatedPhysicalModelComponent());
			targetds.setAssociatedPhysicalModelComponent(lib.getComponent(pmcindex));
		}
		if (sourceds.hasRefersToAnnotation()) {
			int pmcindex = lib.getComponentIndex(sourceds.getSingularTerm());
			targetds.setSingularAnnotation((PhysicalProperty) lib.getComponent(pmcindex));
		}
	}
	
	public static Set<MappableVariable> copyAllAnnotationsToMappedVariables(MappableVariable ds){
		Set<MappableVariable> allmappedvars = new HashSet<MappableVariable>();
		allmappedvars.addAll(getAllMappedVariables(ds, ds, new HashSet<MappableVariable>()));
		for(MappableVariable otherds : allmappedvars){
			System.out.println("Copying to " + otherds.getName());
			if(!otherds.isImportedViaSubmodel()){
				otherds.copyDescription(ds);
				otherds.copySingularAnnotations(ds);
				copyCompositeAnnotation(ds, otherds);
			}
		}
		return allmappedvars;
	}
	
	public static Set<MappableVariable> getAllMappedVariables(MappableVariable rootds, MappableVariable ds, Set<MappableVariable> runningset){		
		Set<MappableVariable> allmappedvars  = new HashSet<MappableVariable>();
		allmappedvars.addAll(ds.getMappedTo());
		allmappedvars.addAll(ds.getMappedFrom());
		
		Set<MappableVariable> returnset = runningset;

		for(MappableVariable var : allmappedvars){
			if(!returnset.contains(var) && var!=rootds){
				returnset.add(var);
				
				// Iterate recursively
				returnset.addAll(getAllMappedVariables(rootds, var, returnset));
			}
		}
	    return returnset;
	}
	
}
