package semgen.annotation.workbench.routines;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semgen.utilities.SemGenError;
import semgen.utilities.SemGenTask;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.annotation.Annotation;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;

public class AnnotationCopier {
	private SemSimModel targetmod, sourcemod;
	private boolean valid = true;

	public AnnotationCopier(SemSimModel target)  {
		targetmod = target;
		chooseSourceModel();
	}
	
	private void chooseSourceModel(){
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model containing annotations", false);
		File sourcefile = sgc.getSelectedFile();
		if (sourcefile == null) {
			valid = false;
			return;
		}
		
		sourcemod = LoadSemSimModel.loadSemSimModelFromFile(sourcefile, false);
		if (sourcemod.getNumErrors() != 0) {
			for(String err : sourcemod.getErrors()){
				System.err.println(err);
			}
			SemGenError.showError("There were errors associated with the selected model. Not copying.", "Copy Model Failed");
			valid=false;
		}
	}
	
	public boolean doCopy() {
		if (isValid()) {
			CopierTask task = new CopierTask();
			task.execute();
			while (!task.isDone()) {
				
			}
			return task.changesMade();
		}
		return false;
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
	
	// Copy all annotations
	class CopierTask extends SemGenTask {
		private boolean changed = false;
		
		public CopierTask(){
			progframe = new SemGenProgressBar("Loading model...",true);
		}
		@Override
		protected Void doInBackground() throws Exception {
			progframe.updateMessage("Copying...");
			for(DataStructure ds : targetmod.getAssociatedDataStructures()){
				if(sourcemod.containsDataStructure(ds.getName())){
					changed = true;
					DataStructure srcds = sourcemod.getAssociatedDataStructure(ds.getName());
					
					ds.copyDescription(srcds);
					ds.copySingularAnnotations(srcds);
					copyCompositeAnnotation(targetmod, ds, srcds);
					
				} // otherwise no matching data structure found in source model
			} // end of data structure loop
			if (copySubmodels(targetmod)) changed = true;
			
			return null;
		}
		
		public boolean changesMade() {
			return changed;
		}
	}
	
	
	private static PhysicalModelComponent copyPhysicalModelComponent(SemSimModel targetmod, PhysicalModelComponent pmc) throws CloneNotSupportedException{
		PhysicalModelComponent pmccopy = null;
		
		// If a composite physical entity...
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
	
	// Copy over all the submodel data
	// Make sure to include change flag functionality
	private boolean copySubmodels(SemSimModel targetmod) {
		Boolean changemadetosubmodels = false;
		for(Submodel sub : targetmod.getSubmodels()){
			if(sourcemod.getSubmodel(sub.getName()) !=null){
				changemadetosubmodels = true;
				
				Submodel srcsub = sourcemod.getSubmodel(sub.getName());
				
				// Copy free-text description
				sub.setDescription(srcsub.getDescription());
				
				// Copy singular annotation
				sub.setSingularAnnotation(srcsub.getReferenceTerm());
			}
		}
		return changemadetosubmodels;
	}
	/** 
	 * Intra-model datastructure copy
	 * */
	public static void copyCompositeAnnotation(DataStructure targetds, DataStructure sourceds) {
		targetds.setPhysicalProperty(sourceds.getPhysicalProperty());
		PhysicalModelComponent srcpropof = sourceds.getAssociatedPhysicalModelComponent();
		
		// If we're just copying a composite annotation within the same model...
		targetds.setAssociatedPhysicalModelComponent(srcpropof);
	}
	
	/** 
	 * Inter-model datastructure copy 
	 * */
	public static void copyCompositeAnnotation(SemSimModel targetmod, DataStructure targetds, DataStructure sourceds) {		
		targetds.setPhysicalProperty(sourceds.getPhysicalProperty());
		
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
	
	public boolean isValid() {
		return valid;
	}
	
}
