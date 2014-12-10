package semgen.annotation.routines;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import semgen.annotation.AnnotatorTab;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenTask;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.SemSimConstants;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;

public class AnnotationCopier {
	private SemSimModel targetmod, sourcemod;
	private boolean valid = true;

	public AnnotationCopier(SemSimModel target)  {
		targetmod = target;
		chooseSourceModel();
	}
	
	private void chooseSourceModel(){
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model containing annotations");
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
			return task.changesMade();
		}
		return false;
	}
	
	public static Set<MappableVariable> copyAllAnnotationsToMappedVariables(AnnotatorTab ann, MappableVariable ds){
		Set<MappableVariable> allmappedvars = new HashSet<MappableVariable>();
		allmappedvars.addAll(getAllMappedVariables(ds, ds, new HashSet<MappableVariable>()));
		for(MappableVariable otherds : allmappedvars){
			System.out.println("Copying to " + otherds.getName());
			if(!otherds.isImportedViaSubmodel()){
				ann.setModelSaved(false);
				otherds.copyDescription(ds);
				otherds.copySingularAnnotations(ds);
				copyCompositeAnnotation(ann.semsimmodel, ann.semsimmodel, ds, otherds);
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
			for(DataStructure ds : targetmod.getDataStructures()){
				if(sourcemod.containsDataStructure(ds.getName())){
					changed = true;
					DataStructure srcds = sourcemod.getDataStructure(ds.getName());
					
					ds.copyDescription(srcds);
					ds.copySingularAnnotations(srcds);
					copyCompositeAnnotation(targetmod, sourcemod, srcds, ds);
					
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
			for(PhysicalEntity source : srcprocess.getSources()){
				PhysicalEntity sourceentcopy = (PhysicalEntity) copyPhysicalModelComponent(targetmod, source);
				((PhysicalProcess) pmccopy).addSource(sourceentcopy);
			}
			for(PhysicalEntity sink : srcprocess.getSinks()){
				PhysicalEntity sinkentcopy = (PhysicalEntity) copyPhysicalModelComponent(targetmod, sink);
				((PhysicalProcess) pmccopy).addSink(sinkentcopy);
			}
			for(PhysicalEntity med : srcprocess.getMediators()){
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
			ReferenceOntologyAnnotation rpeann = pmc.getFirstRefersToReferenceOntologyAnnotation();
			pmccopy = targetmod.addReferencePhysicalEntity(rpeann.getReferenceURI(), rpeann.getValueDescription());
		}
		// If it's a custom concept...
		else{
			if(pmc instanceof PhysicalEntity)
				pmccopy = targetmod.addCustomPhysicalEntity(pmc.getName(), pmc.getDescription());
			else if(pmc instanceof PhysicalProcess)
				pmccopy = targetmod.addCustomPhysicalProcess(pmc.getName(), pmc.getDescription());
			
			// Add annotations
			for(Annotation ann : pmc.getAnnotations())
				pmccopy.addAnnotation(ann.clone());
			
			addClassesNeededToDefineCustomTerm(targetmod, pmc);
		}
		return pmccopy;
	}
	
	// Add any reference entities or processes so they are available during the annotation process
	private static void addClassesNeededToDefineCustomTerm(SemSimModel targetmod, PhysicalModelComponent source) throws CloneNotSupportedException{
		for(Annotation sourceann : source.getAnnotations()){
			if(sourceann instanceof ReferenceOntologyAnnotation){
				ReferenceOntologyAnnotation roa = (ReferenceOntologyAnnotation)sourceann;
				if(targetmod.getPhysicalModelComponentByReferenceURI(roa.getReferenceURI())==null){
					if(source instanceof PhysicalEntity)
						targetmod.addReferencePhysicalEntity(roa.getReferenceURI(), roa.getValueDescription());
					else targetmod.addReferencePhysicalProcess(roa.getReferenceURI(), roa.getValueDescription());
				}
			}
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
				
				// Copy singular annotations
				sub.removeAllReferenceAnnotations();
				for(ReferenceOntologyAnnotation ann : srcsub.getReferenceOntologyAnnotations(SemSimConstants.REFERS_TO_RELATION)){
					sub.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, ann.getReferenceURI(), ann.getValueDescription());
				}
			}
		}
		return changemadetosubmodels;
	}
	
	public static void copyCompositeAnnotation(SemSimModel targetmod, SemSimModel sourcemod, DataStructure srcds, DataStructure ds) {		
		if(srcds.getPhysicalProperty().hasRefersToAnnotation()){
			ds.getPhysicalProperty().removeAllReferenceAnnotations();
			ReferenceOntologyAnnotation roa = srcds.getPhysicalProperty().getFirstRefersToReferenceOntologyAnnotation();
			ds.getPhysicalProperty().addReferenceOntologyAnnotation(roa.getRelation(), roa.getReferenceURI(), roa.getValueDescription());
		}
		PhysicalModelComponent srcpropof = srcds.getPhysicalProperty().getPhysicalPropertyOf();
		
		// If we're just copying a composite annotation within the same model...
		if(targetmod==sourcemod) ds.getPhysicalProperty().setPhysicalPropertyOf(srcpropof);

		// otherwise...
		else{
			try{
				// If there is a property_of specified
				if(srcpropof!=null)
					ds.getPhysicalProperty().setPhysicalPropertyOf(copyPhysicalModelComponent(targetmod, srcpropof));
				
				// otherwise there is no specified target for the physical property
				else ds.getPhysicalProperty().setPhysicalPropertyOf(null);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
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
