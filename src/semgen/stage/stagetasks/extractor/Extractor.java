package semgen.stage.stagetasks.extractor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public abstract class Extractor {
	protected SemSimModel sourcemodel;
	protected SemSimModel extraction;
	protected HashMap<Submodel,Submodel> submodels = new HashMap<Submodel, Submodel>();
	protected HashMap<DataStructure,DataStructure> datastructures = new HashMap<DataStructure,DataStructure>();

	public Extractor(SemSimModel source, SemSimModel extractionmodel) {
		sourcemodel = source;
		extraction = extractionmodel;
		includeSolutionDomains();
	}
	
	public Extractor(SemSimModel source) {
		sourcemodel = source;
		extraction = new SemSimModel();
		includeSolutionDomains();
	}
	
	private void includeSolutionDomains() {
		for (DataStructure sdom : sourcemodel.getSolutionDomains()) {
			this.includeDependency(sdom);
		}
	}
	
	public abstract SemSimModel run();
	
	protected void collectDataStructureInputs() {
		Set<DataStructure> datastructurestokeep = new HashSet<DataStructure>(this.datastructures.keySet());
		
		for (DataStructure ds : datastructurestokeep) {
			
			for (DataStructure input : ds.getComputationInputs()) {
				
				if ( ! datastructures.keySet().contains(input)) {	
					DataStructure newinput = input.copy();
					
					//Retain inputs which are constants
					boolean hasinputs = ! newinput.getComputationInputs().isEmpty();
					boolean clearinputs = true;
					
					if(hasinputs){
						
						// Retain the data structure's dependency if it's mapped from
						// a CellML model's temporal solution domain
						if(newinput instanceof MappableVariable){
							MappableVariable mv = (MappableVariable)newinput;
							
							if(mv.getMappedFrom() != null){
								
								if(mv.getMappedFrom().isSolutionDomain())
									clearinputs = false;
							}
						}
						
						if (clearinputs) {
							newinput.clearInputs();
							newinput.setExternal(true);
						}
					}
					datastructures.put(input, newinput);
				}
			}	
		}
	}
	
	protected void replaceComputations() {
		for (DataStructure dscopy : datastructures.values()) {
			dscopy.replaceAllDataStructures(datastructures);
		}
	}
	
	protected void replaceSubmodelDataStructures() {
		for (Submodel smcopy : submodels.values()) {
			smcopy.replaceDataStructures(datastructures);
		}
	}
	
	protected void replaceSubmodels() {
		for (Submodel smcopy : submodels.values()) {
			smcopy.replaceSubmodels(submodels);
		}
	}
	
	/**
	 * Adds data structures and submodels to the newly constructed model extraction
	 */
	protected void buildExtraction() {
		
		extraction.setSubmodels(submodels.values());
		
		for (DataStructure dstoadd : datastructures.values()) {
			
			dstoadd.addToModel(extraction);
			
			// If the variable is part of a functional submodel that wasn't explicity included
			// in the extraction, create a parent submodel for it. Reuse, if already created.
			if(dstoadd instanceof MappableVariable){
			
				String parentname = dstoadd.getName().substring(0, dstoadd.getName().lastIndexOf("."));
				MappableVariable dsasmv = (MappableVariable)dstoadd;
				
				// If the parent submodel for the variable has not being explicitly added to the extraction
				if( ! submodels.values().contains(extraction.getSubmodel(parentname))){
					
					FunctionalSubmodel copyfs = null;
					
					// Copy in submodel if we haven't already
					if(extraction.getSubmodel(parentname) == null){	
						
						copyfs = new FunctionalSubmodel(parentname, dstoadd);
						copyfs.setLocalName(parentname);
						copyfs.getComputation().setMathML(dstoadd.getComputation().getMathML());
						extraction.addSubmodel(copyfs);
					}
					// Otherwise reuse existing submodel
					else{
						
						copyfs = (FunctionalSubmodel)extraction.getSubmodel(parentname);
						
						// Add output to functional submodel's computation
						if(dsasmv.getPublicInterfaceValue().equals("out")) copyfs.getComputation().addOutput(dstoadd);
						
						// Concat mathml
						String oldmathml = copyfs.getComputation().getMathML();
						
						if(dstoadd.getComputation().getMathML() !=null)
							copyfs.getComputation().setMathML(oldmathml + "\n" + dstoadd.getComputation().getMathML());
					}
					
					copyfs.addDataStructure(dstoadd);
				}
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		extraction.setDescription("Extracted from " + sourcemodel.getName() + " on " + sdf.format(new Date()));

	}
	
	protected void includeSubModel(Submodel sourcesubmodel, Set<Submodel> smstoignore) {
		
		if(smstoignore.contains(sourcesubmodel)) 
			return;
		
		submodels.put(sourcesubmodel, sourcesubmodel.clone());
		
		for (Submodel subsubmodel : sourcesubmodel.getSubmodels()) {
			includeSubModel(subsubmodel, smstoignore);
		}
		
		for (DataStructure ds : sourcesubmodel.getAssociatedDataStructures()) {
			
			if ( ! ds.isExternal()) includeDependency(ds);
		}
	}
	
	protected void includeDependency(DataStructure sourceobj) {
		if (!this.datastructures.containsKey(sourceobj)) {
			datastructures.put(sourceobj, sourceobj.copy());
		}
	}
	
	public abstract void addEntity(PhysicalEntity pe);

	public abstract void addProcess(PhysicalProcess proc);
	
	public abstract void addSubmodel(Submodel sourceobj);
	
	public abstract void addDataStructure(DataStructure sourceobj);
	
	public boolean containsDataStructures() {
		return !this.datastructures.isEmpty();
	}
	
}
