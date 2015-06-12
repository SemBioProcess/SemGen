package semgen.annotation.workbench.routines;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semsim.annotation.Annotation;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;

public class AnnotationCopier {
	
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
		
	private static PhysicalModelComponent copyPhysicalModelComponent(SemSimModel targetmod, PhysicalModelComponent pmc) throws CloneNotSupportedException{
		PhysicalModelComponent pmccopy = null;
		
		// If a composite physical entity....////.,b
		if(pmc instanceof CompositePhysicalEntity){
			int z = 0;
			ArrayList<PhysicalEntity> newentarray = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> newrelarray = new ArrayList<StructuralRelation>();
			for(PhysicalEntity cpeent : ((CompositePhysicalEntity)pmc).getArrayListOfEntities()){
				newentarray.add((PhysicalEntity) copySingularPhysicalModelComponent(targetmod, cpeent));
				if(z<((CompositePhysicalEntity)pmc).getArrayListOfEntities().size()-1)
					newrelarray.add(((CompositePhysicalEntity)pmc).getArrayListOfStructuralRelations().get(z));
				z++;
			}
			pmccopy = targetmod.addCompositePhysicalEntity(newentarray, newrelarray);  // not sure if this needs to be cloned
		}
		// If it's a singular physical entity...
		else if(pmc instanceof PhysicalEntity) pmccopy = copySingularPhysicalModelComponent(targetmod, pmc);
		
		else if(pmc instanceof PhysicalProcess){
			pmccopy = copySingularPhysicalModelComponent(targetmod, pmc);
			PhysicalProcess srcprocess = (PhysicalProcess)pmc;
			for(PhysicalEntity source : srcprocess.getSourcePhysicalEntities()){
				PhysicalEntity sourceentcopy = (PhysicalEntity) copyPhysicalModelComponent(targetmod, source);
				((PhysicalProcess) pmccopy).addSource(sourceentcopy, srcprocess.getSourceStoichiometry(source));
			}
			for(PhysicalEntity sink : srcprocess.getSinkPhysicalEntities()){
				PhysicalEntity sinkentcopy = (PhysicalEntity) copyPhysicalModelComponent(targetmod, sink);
				((PhysicalProcess) pmccopy).addSink(sinkentcopy, srcprocess.getSinkStoichiometry(sink));
			}
			for(PhysicalEntity med : srcprocess.getMediatorPhysicalEntities()){
				PhysicalEntity medentcopy = (PhysicalEntity) copyPhysicalModelComponent(targetmod, med);
				((PhysicalProcess) pmccopy).addMediator(medentcopy);
			}
		}
		return pmccopy;
	}
	
	private static PhysicalModelComponent copySingularPhysicalModelComponent(SemSimModel targetmod, PhysicalModelComponent pmc) throws CloneNotSupportedException{
		PhysicalModelComponent pmccopy = null;
		
		// If it's a reference concept...
		if(pmc.hasRefersToAnnotation()){
			pmccopy = targetmod.addReferencePhysicalEntity((ReferencePhysicalEntity)pmc);
		}
		// If it's a custom concept...
		else{
			if(pmc instanceof PhysicalEntity)
				pmccopy = targetmod.addCustomPhysicalEntity((CustomPhysicalEntity)pmc);
			else if(pmc instanceof PhysicalProcess)
				pmccopy = targetmod.addCustomPhysicalProcess((CustomPhysicalProcess)(pmc));
			
			// Add annotations
			for(Annotation ann : pmc.getAnnotations())
				pmccopy.addAnnotation(ann.clone());
			
			addClassesNeededToDefineCustomTerm(targetmod, pmc);
		}
		return pmccopy;
	}
	
	// Add any reference entities or processes so they are available during the annotation process
	private static void addClassesNeededToDefineCustomTerm(SemSimModel targetmod, PhysicalModelComponent source) throws CloneNotSupportedException{
		if (source.hasRefersToAnnotation()) {
				if(source instanceof PhysicalEntity)
					targetmod.addReferencePhysicalEntity((ReferencePhysicalEntity)source);
				else targetmod.addReferencePhysicalProcess((ReferencePhysicalProcess)source);
			}
	}
	

	/** 
	 * Intra-model datastructure copy
	 * */
	public static void copyCompositeAnnotation(DataStructure targetds, DataStructure sourceds) {
		targetds.setAssociatePhysicalProperty(sourceds.getPhysicalProperty());
		PhysicalModelComponent srcpropof = sourceds.getAssociatedPhysicalModelComponent();
		
		// If we're just copying a composite annotation within the same model...
		targetds.setAssociatedPhysicalModelComponent(srcpropof);
	}
	
	/** 
	 * Inter-model datastructure copy 
	 * */
	public static void copyCompositeAnnotation(SemSimModel targetmod, DataStructure targetds, DataStructure sourceds) {		
		targetds.setAssociatePhysicalProperty(sourceds.getPhysicalProperty());
		
		PhysicalModelComponent srcpropof = sourceds.getAssociatedPhysicalModelComponent();
		
		try{
			// If there is a property_of specified
			if(srcpropof!=null)
				targetds.setAssociatedPhysicalModelComponent(copyPhysicalModelComponent(targetmod, srcpropof));
			
		} 
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
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
