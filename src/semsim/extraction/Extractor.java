package semsim.extraction;

import java.util.HashSet;
import java.util.Set;

import semsim.annotation.Annotation;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

/**
 * Class for extracting a portion of a SemSimModel and 
 * instantiating it as a new model. This is done by specifying 
 * the subset of DataStructures from the source model that are
 *  to be preserved in the extracted model. The user must also
 *  specify which inputs to those DataStructures are to be preserved
 *  as well. The extraction process often requires that "variable" 
 *  DataStructures (those that depend on other DataStructures to be computed)
 *  become independent, user-defined inputs.
 * 
 */
public class Extractor {

	/**
	 * Extract out a portion of a model as a new SemSim model
	 * 
	 *  @param srcmodel The SemSimModel to extract from
	 *  @param extraction A list of all DataStructures to preserve in the extracted model mapped
	 *  to the input DataStructures required to compute them (these inputs can differ from the source model)
	 *  @return A new SemSimModel representing the extract
	 */
	public static SemSimModel extract(SemSimModel srcmodel, 
			Extraction extractioncontents) throws CloneNotSupportedException {
		SemSimModel extractedmodel = new SemSimModel();
		
		// Copy over all the model-level information
		for(Annotation modann : srcmodel.getAnnotations()){
			extractedmodel.addAnnotation(modann.clone());
		}
		
		for(DataStructure soldom : srcmodel.getSolutionDomains()){
			extractedmodel.addDataStructure(soldom.clone());
		}
		for(DataStructure ds : extractioncontents.getDataStructuresToExtract().keySet()){
			
			// If the data structure has been changed from a dependent variable into an input
			if( ! extractioncontents.getDataStructuresToExtract().get(ds) && ds.getComputation().getInputs().size()>0){
				DataStructure newds = ds.clone();
				newds.setComputation(new Computation(newds));
				newds.setStartValue(null);
				extractedmodel.addDataStructure(newds);
			}
			else{
				extractedmodel.addDataStructure(ds.clone());
			}
		}
		
		extractSubModels(srcmodel, extractedmodel, extractioncontents);
		
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
