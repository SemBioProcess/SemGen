package semgen.annotation.workbench.routines;


import java.util.HashSet;
import java.util.Set;

import semgen.SemGen;
import semgen.annotation.workbench.SemSimTermLibrary;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.object.PhysicalProperty;

public class AnnotationCopier {
	
	/** 
	 * Intra-model datastructure copy
	 * */
	public static void copyCompositeAnnotation(DataStructure sourceds, DataStructure targetds) {
		if (sourceds.hasPhysicalProperty()) {
			targetds.setAssociatedPhysicalProperty(sourceds.getPhysicalProperty());
		}
		else targetds.setAssociatedPhysicalProperty(null);
		
		if (sourceds.hasAssociatedPhysicalComponent()) {
			targetds.setAssociatedPhysicalModelComponent(sourceds.getAssociatedPhysicalModelComponent());
		}
		else targetds.setAssociatedPhysicalModelComponent(null);
	}
	
	/** 
	 * Inter-model datastructure copy 
	 * */
	public static void copyCompositeAnnotation(SemSimTermLibrary lib, DataStructure targetds, DataStructure sourceds) {		
		
		// Copy over physical property, physical model component, and singular annotations.
		// If no annotations present in source variable, remove annotations on target variable
		
		if (sourceds.hasPhysicalProperty()) {
			int ppindex = lib.getPhysicalPropertyIndex(sourceds.getPhysicalProperty());
			targetds.setAssociatedPhysicalProperty(lib.getAssociatePhysicalProperty(ppindex));
		}
		else targetds.setAssociatedPhysicalProperty(null);
		
		if (sourceds.hasAssociatedPhysicalComponent()) {
			int pmcindex = lib.getComponentIndex(sourceds.getAssociatedPhysicalModelComponent(), true);
			targetds.setAssociatedPhysicalModelComponent(lib.getComponent(pmcindex));
		}
		else targetds.setAssociatedPhysicalModelComponent(null);
		
		if (sourceds.hasPhysicalDefinitionAnnotation()) {
			int pmcindex = lib.getPhysicalPropertyIndex(sourceds.getSingularTerm());
			targetds.setSingularAnnotation((PhysicalProperty) lib.getComponent(pmcindex));
		}
		else targetds.setSingularAnnotation(null);
	}
	
	public static Set<MappableVariable> copyAllAnnotationsToMappedVariables(MappableVariable ds){
		Set<MappableVariable> allmappedvars = new HashSet<MappableVariable>();
		allmappedvars.addAll(getAllMappedVariables(ds, ds, new HashSet<MappableVariable>()));
		copyAllAnnotations(ds, allmappedvars);
		return allmappedvars;
	}
	
	public static Set<MappableVariable> copyAllAnnotationsToLocallyMappedVariables(MappableVariable ds){
		Set<MappableVariable> allmappedvars = new HashSet<MappableVariable>();
		allmappedvars.addAll(getAllLocallyMappedVariables(ds, ds, new HashSet<MappableVariable>()));
		copyAllAnnotations(ds, allmappedvars);
		return allmappedvars;
	}
	
	private static void copyAllAnnotations(MappableVariable sourceds, Set<MappableVariable> targetdsset){
		for(MappableVariable otherds : targetdsset){
			if(!otherds.isImportedViaSubmodel()){
				otherds.copyDescription(sourceds);
				otherds.copySingularAnnotations(sourceds, SemGen.semsimlib);
				copyCompositeAnnotation(sourceds, otherds);
			}
		}
	}
	
	public static Set<MappableVariable> getAllMappedVariables(MappableVariable rootds, MappableVariable ds, Set<MappableVariable> runningset){		
		Set<MappableVariable> allmappedvars  = new HashSet<MappableVariable>();
		allmappedvars.addAll(ds.getMappedTo());
		if (ds.getMappedFrom()!=null) {
			allmappedvars.add(ds.getMappedFrom());
		}
		
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
	
	public static Set<MappableVariable> getAllLocallyMappedVariables(MappableVariable rootds, MappableVariable ds, Set<MappableVariable> runningset){		
		Set<MappableVariable> allmappedvars  = new HashSet<MappableVariable>();
		allmappedvars.addAll(ds.getMappedTo());
		if (ds.getMappedFrom()!=null) {
			allmappedvars.add(ds.getMappedFrom());
		}
		
		Set<MappableVariable> returnset = runningset;

		for(MappableVariable var : allmappedvars){
			if(!returnset.contains(var) && var!=rootds && ! var.isImportedViaSubmodel()){
				returnset.add(var);
				
				// Iterate recursively
				returnset.addAll(getAllLocallyMappedVariables(rootds, var, returnset));
			}
		}
	    return returnset;
	}
}
