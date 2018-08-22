package semgen.merging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimObject;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalForce;
import semsim.model.physical.PhysicalProcess;
import semsim.utilities.SemSimUtil;
import JSim.util.Xcept;

public class Merger {
	private SemSimModel ssm1clone, ssm2clone;
	private ModelOverlapMap overlapmap;
	protected String error;
	private ArrayList<ResolutionChoice> choicelist;
	private ArrayList<Pair<Double,String>> conversionfactors;
	private HashMap<String, String> oldnewdsnamemap;
	private SemSimModel flattenedmodel = null;
	
	public static enum ResolutionChoice {
		noselection, first, second, ignore; 
	}
	
	public Merger(SemSimModel model1, SemSimModel model2, ModelOverlapMap modelmap, HashMap<String, String> dsnamemap,
			ArrayList<ResolutionChoice> choices, ArrayList<Pair<Double,String>> conversions) {
		ssm1clone = model1;
		ssm2clone = model2;
		overlapmap = modelmap;
		oldnewdsnamemap =dsnamemap;
		choicelist = choices;
		conversionfactors = conversions;
	}
	
	public SemSimModel merge() throws IOException, CloneNotSupportedException, OWLException, InterruptedException, JDOMException, Xcept {
		// First collect all the data structures that aren't going to be used in the resulting merged model
		
		SemSimModel modelfordiscardedds = null;
		DataStructure discardedds = null;
		DataStructure keptds = null;
		
		// If there is one solution domain, get it, otherwise set soldom1 to null
		DataStructure soldom1 = (ssm1clone.getSolutionDomains().size()==1) ? ssm1clone.getSolutionDomains().toArray(new DataStructure[]{})[0] : null;
		
		Set<DataStructure> discardeddsset = new HashSet<DataStructure>();
		Set<DataStructure> keptdsset = new HashSet<DataStructure>();
		Set<DataStructure> dsdonotprune = new HashSet<DataStructure>();
		boolean prune = true;
		
		// If one of the models contains functional submodels and the other doesn't,
		 // flatten the one that does
		 boolean fxnalsubsinmodel1 = ssm1clone.containsFunctionalSubmodels();
		 boolean fxnalsubsinmodel2 = ssm2clone.containsFunctionalSubmodels();
		 Map<String,String> mod1renamemap = new HashMap<String,String>();
		 Map<String,String> mod2renamemap = new HashMap<String,String>();
		 
		 if(fxnalsubsinmodel1 && ! fxnalsubsinmodel2){ 
			 mod1renamemap = SemSimUtil.flattenModel(ssm1clone);
			 flattenedmodel = ssm1clone;
		 }
		 		
		 else if(fxnalsubsinmodel2 && ! fxnalsubsinmodel1){
			 mod2renamemap = SemSimUtil.flattenModel(ssm2clone);
			 flattenedmodel = ssm2clone;
		 }
		
		 
		int i = 0;
		
		// Step through resolution points and replace/rewire codewords as needed
		for (Pair<DataStructure, DataStructure> dsp : overlapmap.getDataStructurePairs()) {
			
			if (choicelist.get(i).equals(ResolutionChoice.first)) {
				//Flip the conversion operator
				Pair<Double, String> factor = conversionfactors.get(i);
				if (factor.getRight()=="*")
					conversionfactors.set(i, Pair.of(factor.getLeft(), "/"));
				else
					conversionfactors.set(i, Pair.of(factor.getLeft(), "*"));
				
				String keptname = oldnewdsnamemap.get(dsp.getLeft().getName());
				
				if (keptname==null) keptname=dsp.getLeft().getName();
				
				if(mod1renamemap.containsKey(keptname)) keptname = mod1renamemap.get(keptname);

				keptds = ssm1clone.getAssociatedDataStructure(keptname);
				modelfordiscardedds = ssm2clone;
				String discardedname = dsp.getRight().getName();
				
				if(mod2renamemap.containsKey(discardedname)) discardedname = mod2renamemap.get(discardedname);
				
				discardedds = modelfordiscardedds.getAssociatedDataStructure(discardedname);
			}
			else if(choicelist.get(i).equals(ResolutionChoice.second)){
				
				String keptname = dsp.getRight().getName();
				
				if(mod2renamemap.containsKey(keptname)) keptname = mod2renamemap.get(keptname);
				
				keptds = ssm2clone.getAssociatedDataStructure(keptname);
				
				modelfordiscardedds = ssm1clone;
				
				String newname = oldnewdsnamemap.get(dsp.getLeft().getName());
				if (newname==null) newname=dsp.getLeft().getName();
				
				if(mod1renamemap.containsKey(newname)) newname = mod1renamemap.get(newname);
				
				discardedds = modelfordiscardedds.getAssociatedDataStructure(newname);
			}
			
			// If "ignore equivalency" is NOT selected
			if(keptds!=null && discardedds !=null){	
				
				discardeddsset.add(discardedds);
				keptdsset.add(keptds);
				dsdonotprune.add(keptds);
								
				if(keptds instanceof MappableVariable && discardedds instanceof MappableVariable){
					rewireMappedVariableDependencies((MappableVariable)keptds, (MappableVariable)discardedds, modelfordiscardedds, i);
					
					 // MappableVariables that are turned into "receivers" should not be pruned, unless they are a solution domain
					if( ! discardedds.isSolutionDomain())
						dsdonotprune.add(discardedds);
				}
				// else we assume that we're dealing with two flattened models
				else replaceCodeWords(keptds, discardedds, modelfordiscardedds, soldom1, i);
			}
			i++;
		}
		
		// Determine which data structures should be pruned	
		if(prune){
			Set<DataStructure> runningsettoprune = new HashSet<DataStructure>();
			runningsettoprune.addAll(discardeddsset);
			
			runningsettoprune = getDataStructuresToPrune(discardeddsset, runningsettoprune, dsdonotprune);
			runningsettoprune.removeAll(dsdonotprune);
			
			for(DataStructure dstoprune : runningsettoprune){
				SemSimModel parentmodel = ssm1clone.getAssociatedDataStructures().contains(dstoprune) ? ssm1clone : ssm2clone;
				parentmodel.removeDataStructure(dstoprune);  // Pruning
				
				// If the data structure is a mappable variable from a CellML model,
				// remove the equation for it in its parent CellML component (its parent FunctionalSubmodel)
				if(dstoprune instanceof MappableVariable){
					MappableVariable mvtoprune = (MappableVariable)dstoprune;
					FunctionalSubmodel fs = parentmodel.getParentFunctionalSubmodelForMappableVariable(mvtoprune);
					
					if(fs!=null) // A MappableVariable may not have a parent FunctionalSubmodel if its parent model was flattened so check first
						fs.removeVariableEquationFromMathML(mvtoprune); 			
				}

				// If we are removing a state variable, remove its JSim-style derivative, if present
				if(dstoprune.hasSolutionDomain()){
					
					if(parentmodel.containsDataStructure(dstoprune.getName() + ":" + dstoprune.getSolutionDomain().getName())){
						parentmodel.removeDataStructurebyName(dstoprune.getName() + ":" + dstoprune.getSolutionDomain().getName());
					}
				}
			}
		}
			
		// Remove the computational dependency information for the discarded/receiverized codewords
		for(DataStructure onediscardedds : discardeddsset)
			onediscardedds.getComputationInputs().clear();
	
		// TODO: What if both models have a custom physical component with the same name?
		SemSimModel mergedmodel = ssm1clone;
		
		// Find any metadata ID's that are identical and create unique ones
		Set<String> intersection = new HashSet<String>(ssm1clone.getMetadataIDcomponentMap().keySet()); // use the copy constructor
		intersection.retainAll(ssm2clone.getMetadataIDcomponentMap().keySet());
		
		for(String ssm2metaid : intersection){
			
			SemSimObject ssm2object = ssm2clone.getModelComponentByMetadataID(ssm2metaid);
			System.err.println("Duplicate metadata ID " + ssm2metaid + ": The ID for " + ssm2object.getSemSimType().getName() + 
					" " + ssm2object.getName() + " in model " + ssm2clone.getName() + " will be renamed for merging.");
			ssm1clone.assignValidMetadataIDtoSemSimObject(ssm2metaid, ssm2object);
		}
		
		// Create two submodels within the merged model where one consists of all data structures
		// from the first model used in the merge, and the second consists of all those from the second.
		createSubmodelForMergeComponent(mergedmodel, ssm1clone);
		createSubmodelForMergeComponent(mergedmodel, ssm2clone);
		
		//Add processes to the merged model
		for (PhysicalProcess pp : ssm2clone.getPhysicalProcesses())
			pp.addToModel(mergedmodel);
		
		for (PhysicalForce pf : ssm2clone.getPhysicalForces())
			pf.addToModel(mergedmodel);
		
		//Create map with units from the cloned model
		Map<UnitOfMeasurement,UnitOfMeasurement> equnitsmap = new HashMap<UnitOfMeasurement,UnitOfMeasurement>();
		for (UnitOfMeasurement uom : overlapmap.getEquivalentUnitPairs().keySet()) {
			equnitsmap.put(ssm1clone.getUnit(uom.getName()), ssm2clone.getUnit(overlapmap.getEquivalentUnitPairs().get(uom).getName()));
		}
		
		// Create mirror map where model 2's units are the key set and model 1's are the values
		Map<UnitOfMeasurement,UnitOfMeasurement> mirrorunitsmap = new HashMap<UnitOfMeasurement,UnitOfMeasurement>();

		for(UnitOfMeasurement uom1 : equnitsmap.keySet())
			mirrorunitsmap.put(equnitsmap.get(uom1), uom1);
		
		// Replace any in-line unit declarations for equivalent units
		// First collect the submodels we need to check
		Set<FunctionalSubmodel> submodelswithconstants = new HashSet<FunctionalSubmodel>();

		for(FunctionalSubmodel fs : ssm2clone.getFunctionalSubmodels()){
			
			if(fs.getComputation().getMathML()!=null){
				
				if(fs.getComputation().getMathML().contains("<cn")) 
					submodelswithconstants.add(fs);
			}
		}
		
		for(FunctionalSubmodel fswithcon : submodelswithconstants){
			String oldmathml = fswithcon.getComputation().getMathML();
			String newmathml = oldmathml;
			
			for(UnitOfMeasurement uom : mirrorunitsmap.keySet())
				newmathml = newmathml.replace("\"" + uom.getName() + "\"", "\"" + mirrorunitsmap.get(uom).getName() + "\"");
			
			fswithcon.getComputation().setMathML(newmathml);
		}
		
		// Copy in the units, deal with equivalencies along the way
		for(UnitOfMeasurement model2unit : ssm2clone.getUnits()){
				model2unit.addToModel(mergedmodel);
		}
		
		// Copy in all the events and event assignments
		for(Event event : ssm2clone.getEvents()){
			mergedmodel.addEvent(event);
		}	
		
		// Copy in all data structures. If one of the models needed to be flattened,
		// check that flattening didn't create a conflict in data structure names
		for(DataStructure dsfrom2 : ssm2clone.getAssociatedDataStructures()){
			
			if(flattenedmodel != null){
				
				if(mergedmodel.containsDataStructure(dsfrom2.getName())){
					
					// TODO: Should probably create a "renameDataStructure" method in SemSimModel or elsewhere
					String oldname = dsfrom2.getName();
					String newname = oldname;
					
					Set<String> dsnames = mergedmodel.getDataStructureNames();
					
					// Find a new name for the data structure
					while(dsnames.contains(newname)){
						newname = newname + "_";
					}
					
					dsfrom2.setName(newname);
					
					Set<DataStructure> dssettoedit = new HashSet<DataStructure>();
					dssettoedit.addAll(dsfrom2.getUsedToCompute());
					dssettoedit.add(dsfrom2);
					
					// Go through all data structures that are dependent on the one we are renaming and replace occurrences of old name in equations
					for(DataStructure depds : dssettoedit){ 
						
						if(depds.hasComputation()){
							Computation depcomp = depds.getComputation();
							String oldmathml = depcomp.getMathML();
							String newmathml = SemSimUtil.replaceCodewordsInString(oldmathml, newname, oldname);
							depcomp.setMathML(newmathml);
							
							if( ! depcomp.getComputationalCode().equals("") && depcomp.getComputationalCode()!=null){
								String newcompcode = SemSimUtil.replaceCodewordsInString(depcomp.getComputationalCode(), newname, oldname);
								depcomp.setComputationalCode(newcompcode);
							}
						}
					}
					System.err.println("Both models had data structure : " + oldname + " so it was renamed to " + newname + " when adding contents of " + ssm2clone.getName() + " to merged model");
				}
			}
			
			dsfrom2.addToModel(mergedmodel);
			
		}		

		// Copy in the submodels
		for(Submodel subfrom2 : ssm2clone.getSubmodels()){
			mergedmodel.addSubmodel(subfrom2);
		}
		
		// Prune empty submodels
		if(prune) pruneSubmodels(mergedmodel);
		
		// Remove legacy code info
		mergedmodel.setSourceFileLocation(null);
		
		//TODO: WHAT TO DO ABOUT MODEL-LEVEL ANNOTATIONS?
		mergedmodel.setNamespace(mergedmodel.generateNamespaceFromDateAndTime());
		mergedmodel.setName("model_0");
		
		return mergedmodel;
	}
	
	// Changes to variables when merging two models with CellML-style mapped variables
	private void rewireMappedVariableDependencies(MappableVariable sourceds, MappableVariable receiverds, 
			SemSimModel modelforrecieverds, int index){
				
		//For the codeword that will now receive values from the source codeword,
		// turn it into a component input and create a mapping from the source codeword to the receiver.
		receiverds.setPublicInterfaceValue("in");
		sourceds.addVariableMappingTo(receiverds);
		
		//Take all mappedTo values for receiver codeword and apply them to source codeword.
		for(MappableVariable mappedtods : receiverds.getMappedTo()){
			sourceds.addVariableMappingTo(mappedtods);
		}
		
		// Remove all mappedTo DataStructures for receiver codeword.
		receiverds.getMappedTo().clear();
		
		//Also remove any initial_value that the receiver DS has.
		receiverds.setCellMLinitialValue("");
		receiverds.setStartValue(null);
		
		// Find mathML block for the receiver codeword and remove it from the FunctionalSubmodel's computational code
		FunctionalSubmodel fs = modelforrecieverds.getParentFunctionalSubmodelForMappableVariable(receiverds);
		fs.removeVariableEquationFromMathML(receiverds);
	}
	
	
	// Collect those data structures that are "orphaned" by the merge and flag them for pruning
	private Set<DataStructure> getDataStructuresToPrune(Set<DataStructure> settocheck, Set<DataStructure> runningset, Set<DataStructure> flagimmune){
		
		Set<DataStructure> allinputs = new HashSet<DataStructure>();
		Set<DataStructure> nextsettoprocess = new HashSet<DataStructure>();
		
		// Get all inputs to codewords that are edited (replaced, turned into receivers, or orphaned)
		for(DataStructure dstocheck : settocheck){
			allinputs.addAll(dstocheck.getComputationInputs());
		}
		
		for(DataStructure inputds : allinputs){
			
			// If the input is only used to compute codewords that are pruned, prune it
			if(runningset.containsAll(inputds.getUsedToCompute())){
				
//				 Don't prune data structures that must be included in the model
				if(! flagimmune.contains(inputds)){
					runningset.add(inputds);
					nextsettoprocess.add(inputds);
				}
			}
		}
		
		// Process recursively
		if(nextsettoprocess.size()>0)
			getDataStructuresToPrune(nextsettoprocess, runningset, flagimmune);
		
		return runningset;
	}
	
	
	private void replaceCodeWords(DataStructure keptds, DataStructure discardedds, 
			SemSimModel modelfordiscardedds, DataStructure soldom1, int index) {
				
		Pair<Double, String> conversionfactor = conversionfactors.get(index);
		
		// if the two terms have different names, or a conversion factor is required, perform string replacements
		if( ! discardedds.getName().equals(keptds.getName()) || conversionfactor.getLeft()!=1.0){
			String replacementtext = keptds.getName();
			
			if(conversionfactor.getLeft()!=1.0) 
				replacementtext = "(" + keptds.getName() + conversionfactor.getRight() + String.valueOf(conversionfactor.getLeft()) + ")";
			
			 SemSimUtil.replaceCodewordInAllEquations(discardedds, keptds, modelfordiscardedds, 
					discardedds.getName(), replacementtext, conversionfactor);
		}
		
		// TODO: What to do about sol doms that have different units?
		if(discardedds.isSolutionDomain()){
		  
			// Re-set the solution domain designations for all DataStructures in model 2
			for(DataStructure nsdds : ssm2clone.getAssociatedDataStructures()){
				if(nsdds.hasSolutionDomain()) nsdds.setSolutionDomain(soldom1);
			}
		}
		
		// If the semantic resolution took care of a syntactic resolution
		if(!choicelist.get(index).equals(ResolutionChoice.ignore))
			overlapmap.getIdenticalNames().remove(discardedds.getName());
		
	}
	
	// Collects ungrouped data structures from one of the models used in the merge, along with all
	// its submodels and puts them into a single parent submodel
	private void createSubmodelForMergeComponent(SemSimModel mergedmodel, SemSimModel componentmodel){
		
		// For CellML-style merges, create CellML-style submodels to house the contents 
		// of the component models in the merge. Otherwise, create SemSim-style submodels to house them.
		if(ssm1clone.containsFunctionalSubmodels() && ssm2clone.containsFunctionalSubmodels()){
			Set<FunctionalSubmodel> fsset = new HashSet<FunctionalSubmodel>();
			fsset.addAll(componentmodel.getTopFunctionalSubmodels());
			
			FunctionalSubmodel fs = new FunctionalSubmodel(componentmodel.getName(), new HashSet<DataStructure>());
			Map<String,Set<FunctionalSubmodel>> map = fs.getRelationshipSubmodelMap();
			map.put("encapsulation", fsset);
			fs.setRelationshipSubmodelMap(map);
			mergedmodel.addSubmodel(fs);
		}
		else{
			Submodel submodel = new Submodel(componentmodel.getName());
			submodel.setAssociatedDataStructures(componentmodel.getUngroupedDataStructures());
			submodel.setSubmodels(componentmodel.getSubmodels());
			mergedmodel.addSubmodel(submodel);
		}
	}
	
	// Remove empty submodels
	private void pruneSubmodels(SemSimModel model){
		Set<Submodel> tempset = new HashSet<Submodel>();
		tempset.addAll(model.getSubmodels());
		
		for(Submodel sub : tempset){
			
			if(sub.getAssociatedDataStructures().isEmpty() && sub.getSubmodels().isEmpty()){
				
				if(sub instanceof FunctionalSubmodel){
					
					// Preserve empty FunctionalSubmodels that have some encapsulation or containment relation
					if(((FunctionalSubmodel) sub).getRelationshipSubmodelMap().isEmpty()){
						model.removeSubmodel(sub);
					}
				}
				else model.removeSubmodel(sub); // If SemSim-style submodel, remove
			}
		}
	}
}
