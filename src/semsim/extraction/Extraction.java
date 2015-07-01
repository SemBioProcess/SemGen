package semsim.extraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import semsim.annotation.Annotation;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

/**
 * Class that contains the contents of a SemSimModel that are
 * staged for extraction to a new model.
 * 
 */
public class Extraction {

	private Map<PhysicalProcess, Boolean> processestoextract;
	private Map<PhysicalEntity, Boolean> entitiestoextract;
	private Map<DataStructure, Boolean> datastructurestoextract;
	private Map<Submodel, Boolean> submodelstoextract;
	
	public Extraction(){
		reset();
	}
	
	public void reset(){
		setProcessesToExtract(new HashMap<PhysicalProcess, Boolean>());
		setEntitiesToExtract(new HashMap<PhysicalEntity, Boolean>());
		setDataStructuresToExtract(new HashMap<DataStructure, Boolean>());
		setSubmodelsToExtract(new HashMap<Submodel, Boolean>());
	}

	// Physical processes
	public Map<PhysicalProcess, Boolean> getProcessesToExtract() {
		return processestoextract;
	}

	public void setProcessesToExtract(Map<PhysicalProcess, Boolean> processestoextract) {
		this.processestoextract = processestoextract;
	}
	
	public void addProcessToExtract(PhysicalProcess process, Boolean includeparticipants){
		this.getProcessesToExtract().put(process, includeparticipants);
	}

	// Physical entities
	public Map<PhysicalEntity, Boolean> getEntitiesToExtract() {
		return entitiestoextract;
	}

	public void setEntitiesToExtract(Map<PhysicalEntity, Boolean> entitiestoextract) {
		this.entitiestoextract = entitiestoextract;
	}
	
	public void addEntityToExtract(PhysicalEntity entity, Boolean val){
		this.getEntitiesToExtract().put(entity, val);
	}

	// Data structures
	public Map<DataStructure, Boolean> getDataStructuresToExtract() {
		return datastructurestoextract;
	}

	public void setDataStructuresToExtract(Map<DataStructure, Boolean> datastructurestoextract) {
		this.datastructurestoextract = datastructurestoextract;
	}
	
	public void addDataStructureToExtract(DataStructure ds, Boolean includeinputs){
		getDataStructuresToExtract().put(ds, includeinputs);
	}
	
	// Add a data structure's computational inputs to the extraction map
	public void addInputsToExtract(DataStructure onedatastr){
		
		for (DataStructure nextds : onedatastr.getComputationInputs()) {
			
			addDataStructureToExtract(nextds, true);
			
			for(DataStructure secondaryds : nextds.getComputationInputs()){
				if (! getDataStructuresToExtract().containsKey(secondaryds)) {
					addDataStructureToExtract(secondaryds, false);
				}
			}
		}
	}

	// Submodels
	public Map<Submodel, Boolean> getSubmodelsToExtract() {
		return submodelstoextract;
	}

	public void setSubmodelsToExtract(Map<Submodel, Boolean> submodelstoextract) {
		this.submodelstoextract = submodelstoextract;
	}
	
	public void addSubmodelToExtract(Submodel submodel, Boolean val){
		this.getSubmodelsToExtract().put(submodel, val);
	}
	
	
	public boolean isEmpty(){
		return (getDataStructuresToExtract().isEmpty() && getEntitiesToExtract().isEmpty() 
				&& getProcessesToExtract().isEmpty() && getSubmodelsToExtract().isEmpty());
				
	}
	
	
	public boolean isInputForExtraction(DataStructure ds){
		Boolean isinput = false;
		Boolean inputexplicitlyincluded = getDataStructuresToExtract().containsKey(ds);
		
		// If the input is explicitly included in the extract, and it retains its computational dependencies, 
		// then it's not a terminal input
		if(inputexplicitlyincluded)			
			isinput = (getDataStructuresToExtract().get(ds)==false || ds.getComputationInputs().isEmpty());
		
		return isinput;
	}
	
	
	public boolean isVariableConvertedToInput(DataStructure ds){
		
		Boolean inputexplicitlyincluded = getDataStructuresToExtract().containsKey(ds);
		Boolean convertedtoinput = false;

		if(inputexplicitlyincluded){
			convertedtoinput = (getDataStructuresToExtract().get(ds)==false && ds.getComputationInputs().size()>0);
		}
		else{
			convertedtoinput = ds.getComputationInputs().size()>0;
		}
		
		return convertedtoinput;
	}
	
	
	
	/**
	 * Extract out a portion of a model as a new SemSim model
	 * 
	 *  @param srcmodel The SemSimModel to extract from
	 *  @param extraction A list of all DataStructures to preserve in the extracted model mapped
	 *  to the input DataStructures required to compute them (these inputs can differ from the source model)
	 *  @return A new SemSimModel representing the extract
	 */
	public static SemSimModel extractToNewModel(SemSimModel srcmodel, 
			Extraction extraction) throws CloneNotSupportedException {
		SemSimModel extractedmodel = new SemSimModel();
		
		// Copy over all the model-level information
		for(Annotation modann : srcmodel.getAnnotations()){
			extractedmodel.addAnnotation(modann.clone());
		}
		
		for(DataStructure soldom : srcmodel.getSolutionDomains()){
			extractedmodel.addDataStructure(soldom.clone());
		}
		for(DataStructure ds : extraction.getDataStructuresToExtract().keySet()){
			
			// If the data structure has been changed from a dependent variable into an input
			DataStructure newds = null;
			
			if( ! extraction.getDataStructuresToExtract().get(ds) && ds.getComputation().getInputs().size()>0){
				newds = ds.clone();
				newds.setComputation(new Computation(newds));
				newds.setStartValue(null);
				newds.getAnnotations().addAll(ds.getAnnotations());
				extractedmodel.addDataStructure(newds);
			}
			else{
				extractedmodel.addDataStructure(ds.clone());
			}
		}
		
		extractSubModels(srcmodel, extractedmodel, extraction);
		
		// Copy the physical entity and process info into the model-level entity and process sets
		Set<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
		Set<PhysicalProcess> procs = new HashSet<PhysicalProcess>();
		
		for(DataStructure newds : extractedmodel.getAssociatedDataStructures()){
			if(newds.getPhysicalProperty()!=null){
				if(newds.getAssociatedPhysicalModelComponent() instanceof PhysicalEntity){
					ents.add((PhysicalEntity) newds.getAssociatedPhysicalModelComponent());
				}
				else if(newds.getAssociatedPhysicalModelComponent() instanceof PhysicalProcess){
					PhysicalProcess pproc = (PhysicalProcess)newds.getAssociatedPhysicalModelComponent();
					procs.add(pproc);
					ents.addAll(pproc.getSourcePhysicalEntities());
					ents.addAll(pproc.getSinkPhysicalEntities());
					ents.addAll(pproc.getMediatorPhysicalEntities());
				}
			}
		}
		extractedmodel.setPhysicalEntities(ents);
		extractedmodel.setPhysicalProcesses(procs);
		
		return extractedmodel;
	}
	
	// if all codewords in a component (submodel) are being preserved, preserve the component, but not if 
	// the component is what's being extracted
	private static void extractSubModels(SemSimModel srcmodel, SemSimModel extractedmodel,
			Extraction extractioncontents) throws CloneNotSupportedException {
		
		for(Submodel sub : extractioncontents.getSubmodelsToExtract().keySet()){
			
			Submodel newsub = extractedmodel.addSubmodel(sub.clone());
			
			for(Submodel subsub : newsub.getSubmodels()){
				
				// If we're not preserving all the data structures for a submodel of the submodel, don't preserve the sub-submodel
				if(!extractioncontents.getDataStructuresToExtract().keySet().containsAll(subsub.getAssociatedDataStructures())){
					newsub.removeSubmodel(subsub);
				}
			}
		}
	}
}
