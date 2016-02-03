package semsim.extraction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

/**
 * Class that contains the contents of a SemSimModel that are
 * staged for extraction to a new model.
 * 
 */
public class Extraction {

	/**
	 * The keyset for this map includes all physical processes that should be included in the extracted model.
	 * The boolean values indicate whether the process participants should also be included.
	 */
	private Map<PhysicalProcess, Boolean> processestoextract;
	
	/**
	 * The keyset for this map includes all physical entities that should be included in the extracted model.
	 * The boolean values are currently meaningless, but are included for homogeneity.
	 */
	private Map<PhysicalEntity, Boolean> entitiestoextract;
	
	/**
	 * The keyset for this map includes all data structures that should be included in the extracted model.
	 * The boolean values indicate whether the computational inputs for the data structure
	 * should also be included in the extraction.
	 */
	private Map<DataStructure, Boolean> datastructurestoextract;
	
	/**
	 * The keyset for this map includes all submodels that should be included in the extracted model.
	 * The boolean values indicate whether to attempt to preserved the submodels within the submodel.
	 */
	private Map<Submodel, Boolean> submodelstoextract;
	
	/**
	 * The model from which the extraction will be extracted.
	 */
	private SemSimModel sourcemodel;
	
	/**
	 * A friendly date format for time-stamping extractions
	 */
	private SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

	public Extraction(SemSimModel sourcemodel){
		this.setSourceModel(sourcemodel);
		reset();
	}
	
	/**
	 * Clear the processes, entities, data structures and submodels that
	 * were previously included in the extraction.
	 */
	public void reset(){
		setProcessesToExtract(new HashMap<PhysicalProcess, Boolean>());
		setEntitiesToExtract(new HashMap<PhysicalEntity, Boolean>());
		setDataStructuresToExtract(new HashMap<DataStructure, Boolean>());
		setSubmodelsToExtract(new HashMap<Submodel, Boolean>());
	}
	
	public SemSimModel getSourceModel() {
		return sourcemodel;
	}

	public void setSourceModel(SemSimModel sourcemodel) {
		this.sourcemodel = sourcemodel;
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
	
	public void addDataStructureToExtraction(DataStructure ds, Boolean includeinputs){
		getDataStructuresToExtract().put(ds, includeinputs);
	}
	
	/**
	 * Add a data structure's computational inputs to the extraction
	 * @param ds
	 */
	public void addInputsToExtract(DataStructure ds){
		
		for (DataStructure nextds : ds.getComputationInputs()) {
			
			addDataStructureToExtraction(nextds, true);
			
			for(DataStructure secondaryds : nextds.getComputationInputs()){
				if (! getDataStructuresToExtract().containsKey(secondaryds)) {
					addDataStructureToExtraction(secondaryds, false);
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
	
	/**
	 * 
	 * @return True if there is nothing specified for extraction, otherwise false
	 */
	public boolean isEmpty(){
		return (getDataStructuresToExtract().isEmpty() && getEntitiesToExtract().isEmpty() 
				&& getProcessesToExtract().isEmpty() && getSubmodelsToExtract().isEmpty());
				
	}
	
	/**
	 * 
	 * @param ds
	 * @return Whether a particular data structure is a user-defined 
	 * input for the extraction
	 */
	public boolean isInputForExtraction(DataStructure ds){
		
		Boolean isinput = false;
		Boolean inputexplicitlyincluded = getDataStructuresToExtract().containsKey(ds);
		
		// If the input is explicitly included in the extract, and it retains its computational dependencies, 
		// then it's not a terminal input
		if(inputexplicitlyincluded)			
			isinput = (getDataStructuresToExtract().get(ds)==false || ds.getComputationInputs().isEmpty());
		
		return isinput;
	}
	
	/**
	 * @param ds
	 * @return Whether a particular data structure is converted from
	 * a dependent variable to a user-defined input for the extraction
	 */
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
	public SemSimModel extractToNewModel() throws CloneNotSupportedException {
		SemSimModel extractedmodel = new SemSimModel();
		
		// Copy over all the model-level information
//		for(Annotation modann : getSourceModel().getAnnotations()){
//			extractedmodel.addAnnotation(new Annotation(modann));
//		}
		
		// Copy in units
		for(UnitOfMeasurement uom : getSourceModel().getUnits()){
			extractedmodel.addUnit(new UnitOfMeasurement(uom));
		}
		
		// Copy in solution domains
		for(DataStructure soldom : getSourceModel().getSolutionDomains()){
			extractedmodel.addDataStructure(soldom.clone());
		}
		
		// Copy in the data structures
		for(DataStructure ds : getDataStructuresToExtract().keySet()){
			
			DataStructure newds = ds.clone();
						
			// If the data structure has been changed from a dependent variable into an input
			if( ! getDataStructuresToExtract().get(ds) && ds.getComputation().getInputs().size()>0){
				newds.setComputation(new Computation(newds));
				newds.setStartValue(null);
				newds.getAnnotations().addAll(ds.getAnnotations());
				
				if(newds instanceof MappableVariable) ((MappableVariable)newds).getMappedFrom().clear();
			}
			
			extractedmodel.addDataStructure(newds);
			
			// If the variable is part of a functional submodel that wasn't explicity included
			// in the extraction, create a parent submodel for it. Reuse, if already created.
			if(newds instanceof MappableVariable){
			
				String parentname = newds.getName().substring(0, newds.getName().lastIndexOf("."));
				MappableVariable dsasmv = (MappableVariable)ds;
				
				// If the parent submodel for the variable is not being explicitly extracted 
				if( ! getSubmodelsToExtract().containsKey(sourcemodel.getSubmodel(parentname))){
					FunctionalSubmodel copyfs = null;
					
					// Copy in submodel if we haven't already
					if(extractedmodel.getSubmodel(parentname) == null){						
						copyfs = new FunctionalSubmodel(parentname, newds);
						copyfs.setLocalName(parentname);
						copyfs.getComputation().setMathML(newds.getComputation().getMathML());
						extractedmodel.addSubmodel(copyfs);
					}
					// Otherwise reuse existing submodel
					else{
						copyfs = (FunctionalSubmodel)extractedmodel.getSubmodel(parentname);
						
						// Add output to functional submodel's computation
						if(dsasmv.getPublicInterfaceValue().equals("out")) copyfs.getComputation().addOutput(newds);
						
						// Concat mathml
						String oldmathml = copyfs.getComputation().getMathML();
						
						if(newds.getComputation().getMathML() !=null)
							copyfs.getComputation().setMathML(oldmathml + "\n" + newds.getComputation().getMathML());
					}
					
					copyfs.addDataStructure(newds);
				}
			}
		}
		
		extractSubModels(extractedmodel);
		
		// Replace the mappedFrom/mappedTo info with the cloned data structures
		for(DataStructure ds : extractedmodel.getAssociatedDataStructures()){
			
			if(ds.isMapped()){
				MappableVariable mv = ((MappableVariable)ds);
				Set<MappableVariable> newmappedfromset = new HashSet<MappableVariable>();
				Set<MappableVariable> newmappedtoset = new HashSet<MappableVariable>();
				
				for(MappableVariable mappedfrommv : mv.getMappedFrom()){
					String mappedfromname = mappedfrommv.getName();
					
					// Only add if the mapped variable is also in the extracted model
					if(extractedmodel.containsDataStructure(mappedfromname))
						newmappedfromset.add((MappableVariable) extractedmodel.getAssociatedDataStructure(mappedfromname));
				}
				
				for(MappableVariable mappedtomv : mv.getMappedTo()){
					String mappedtoname = mappedtomv.getName();
					
					// Only add if the mapped variable is also in the extracted model
					if(extractedmodel.containsDataStructure(mappedtoname))
						newmappedtoset.add((MappableVariable) extractedmodel.getAssociatedDataStructure(mappedtoname));
				}
				
				mv.setMappedFrom(newmappedfromset);
				mv.setMappedTo(newmappedtoset);	
			}
		}
				
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
		
		extractedmodel.setDescription("Extracted from " + getSourceModel().getName() + " on " + sdf.format(new Date()));
		return extractedmodel;
	}
	
	/**
	 * Clones all submodels that should be preserved in the extraction.
	 * @param extractedmodel
	 * @throws CloneNotSupportedException
	 */
	private void extractSubModels(SemSimModel extractedmodel) throws CloneNotSupportedException {
		
		for(Submodel sub : getSubmodelsToExtract().keySet()){
						
			Submodel newsub = sub.isFunctional() ? new FunctionalSubmodel((FunctionalSubmodel)sub) : new Submodel(sub);
			
			extractedmodel.addSubmodel(newsub);
			
			// Associate submodel with the copies of the data structures it was previously associated with
			ArrayList<DataStructure> tempdsset = new ArrayList<DataStructure>();
			
			for(DataStructure ds : newsub.getAssociatedDataStructures()){
				String dsname = ds.getName();
				DataStructure dscopy = extractedmodel.getAssociatedDataStructure(dsname);
				tempdsset.add(dscopy);
			}
			newsub.setAssociatedDataStructures(tempdsset);
			
			// If we're preserving the submodel's submodels
			if(getSubmodelsToExtract().get(sub)==true){
				
				for(Submodel subsub : newsub.getSubmodels()){
					
					// If we're not preserving all the data structures for a submodel of the submodel, don't preserve the sub-submodel
					if(!getDataStructuresToExtract().keySet().containsAll(subsub.getAssociatedDataStructures())){
						newsub.removeSubmodel(subsub);
					}
				}
			}
		}
	}
}
