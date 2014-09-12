package semsim.extraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.computational.Computation;
import semsim.model.computational.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.Submodel;

/**
 * Class for "carving out" a portion of a SemSimModel and 
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
	 *  @param allinds2keep A list of all DataStructures to preserve in the extracted model mapped
	 *  to the input DataStructures required to compute them (these inputs can differ from the source model)
	 *  @return A new SemSimModel representing the extract
	 */
	public static SemSimModel extract(SemSimModel srcmodel, 
			Map<DataStructure, Set<? extends DataStructure>> allinds2keep) throws CloneNotSupportedException {
		DataStructure[] indarray = {};
		indarray = (DataStructure[]) allinds2keep.keySet().toArray(indarray);

		SemSimModel extractedmodel = new SemSimModel();
		
		for(DataStructure soldom : srcmodel.getSolutionDomains()){
			extractedmodel.addDataStructure(soldom.clone());
		}
		for(DataStructure ds : allinds2keep.keySet()){
			// If the data structure has been changed from a dependent variable into an input
			if(allinds2keep.get(ds).isEmpty() && !ds.getComputation().getInputs().isEmpty()){
				DataStructure newds = ds.clone();
				newds.setComputation(new Computation(newds));
				newds.setStartValue(null);
				extractedmodel.addDataStructure(newds);
			}
			else{
				extractedmodel.addDataStructure(ds.clone());
			}
		}
		
		// if all codewords in a component (submodel) are being preserved, preserve the component, but not if 
		// the component is what's being extracted
		for(Submodel sub : srcmodel.getSubmodels()){
			Set<DataStructure> dsset = sub.getAssociatedDataStructures();
			if(allinds2keep.keySet().containsAll(dsset) && !dsset.isEmpty()){
				Submodel newsub = extractedmodel.addSubmodel(sub.clone());
				for(Submodel subsub : newsub.getSubmodels()){
					if(!allinds2keep.keySet().containsAll(subsub.getAssociatedDataStructures())){
						newsub.removeSubmodel(subsub);
					}
				}
			}
		}
		
		// Copy over all the model-level information
		for(Annotation modann : srcmodel.getAnnotations()){
			extractedmodel.addAnnotation(modann.clone());
		}
		
		// Copy the physical entity and process info into the model-level entity and process sets
		Set<PhysicalEntity> ents = new HashSet<PhysicalEntity>();
		Set<PhysicalProcess> procs = new HashSet<PhysicalProcess>();
		for(DataStructure newds : extractedmodel.getDataStructures()){
			if(newds.getPhysicalProperty()!=null){
				if(newds.getPhysicalProperty().getPhysicalPropertyOf() instanceof PhysicalEntity){
					ents.add((PhysicalEntity) newds.getPhysicalProperty().getPhysicalPropertyOf());
				}
				else if(newds.getPhysicalProperty().getPhysicalPropertyOf() instanceof PhysicalProcess){
					PhysicalProcess pproc = (PhysicalProcess)newds.getPhysicalProperty().getPhysicalPropertyOf();
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
}
