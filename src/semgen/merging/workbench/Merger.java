package semgen.merging.workbench;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semgen.merging.dialog.ConversionFactorDialog;
import semgen.merging.workbench.ModelOverlapMap.maptype;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;
import JSim.util.Xcept;

public class Merger {
	private SemSimModel ssm1clone, ssm2clone;
	private ModelOverlapMap overlapmap;
	protected String error;
	private ArrayList<ResolutionChoice> choicelist;
	
	public static enum ResolutionChoice {
		noselection, first, second, ignore; 
	}
	
	public Merger(SemSimModel model1, SemSimModel model2, ModelOverlapMap modelmap, ArrayList<ResolutionChoice> choices) {
		ssm1clone = model1;
		ssm2clone = model2;
		overlapmap = modelmap;
		choicelist = choices;
	}
	
	public SemSimModel merge() throws IOException, CloneNotSupportedException, OWLException, InterruptedException, JDOMException, Xcept {
		// First collect all the data structures that aren't going to be used in the resulting merged model
		// Include a mapping between the solution domains
		
		DataStructure soldom1 = ssm1clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		DataStructure soldom2 = ssm2clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		overlapmap.addDataStructureMapping(soldom1, soldom2, maptype.automapping);
		choicelist.add(ResolutionChoice.first);
		
		SemSimModel modelfordiscardedds = null;
		int i = 0;
		DataStructure discardedds = null;
		DataStructure keptds = null;
		for (Pair<DataStructure, DataStructure> dsp : overlapmap.getDataStructurePairs()) {
			if (choicelist.get(i).equals(ResolutionChoice.first)) {
				discardedds = dsp.getRight();
				keptds = dsp.getLeft();
				modelfordiscardedds = ssm2clone;
			}
			else if(choicelist.get(i).equals(ResolutionChoice.second)){
				discardedds = dsp.getLeft();
				keptds = dsp.getRight();
				modelfordiscardedds = ssm1clone;
			}
			
			// If "ignore equivalency" is not selected"
			if(keptds!=null && discardedds !=null){	
				if (!replaceCodeWords(keptds, discardedds, modelfordiscardedds, soldom1, i)) 
					return null;
			}
			i++;
		}
		
		// What if both models have a custom phys component with the same name?
		SemSimModel mergedmodel = ssm1clone;
		
		// Create submodels representing the merged components, copy over all info from model2 into model1
		Submodel sub1 = new Submodel(ssm1clone.getName());
		sub1.setAssociatedDataStructures(ssm1clone.getDataStructures());
		sub1.setSubmodels(ssm1clone.getSubmodels());
		
		Submodel sub2 = new Submodel(ssm2clone.getName());
		sub2.setAssociatedDataStructures(ssm2clone.getDataStructures());
		sub2.addDataStructure(soldom1);
		
		if(ssm1clone.containsDataStructure(soldom1.getName() + ".min"))
			sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".min"));
		if(ssm1clone.containsDataStructure(soldom1.getName() + ".max"))
			sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".max"));
		if(ssm1clone.containsDataStructure(soldom1.getName() + ".delta"))
			sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".delta"));
		
		sub2.setSubmodels(ssm2clone.getSubmodels());
		mergedmodel.addSubmodel(sub1);
		mergedmodel.addSubmodel(sub2);
		
		// Copy in all data structures
		for(DataStructure dsfrom2 : ssm2clone.getDataStructures()){
			mergedmodel.addDataStructure(dsfrom2);
		}
		
		// Copy in the units
		mergedmodel.getUnits().addAll(ssm2clone.getUnits());
		
		// Copy in the submodels
		for(Submodel subfrom2 : ssm2clone.getSubmodels()){
			mergedmodel.addSubmodel(subfrom2);
		}
		
		// MIGHT NEED TO COPY IN PHYSICAL MODEL COMPONENTS?

		
		// WHAT TO DO ABOUT ONTOLOGY-LEVEL ANNOTATIONS?
		mergedmodel.setNamespace(mergedmodel.generateNamespaceFromDateAndTime());
		
		return mergedmodel;
	}
	
	private boolean replaceCodeWords(DataStructure keptds, DataStructure discardedds, 
			SemSimModel modelfordiscardedds, DataStructure soldom1, int index) {
		// If we need to add in a unit conversion factor
		String replacementtext = keptds.getName();
		Boolean cancelmerge = false;
		double conversionfactor = 1;
		if(keptds.hasUnits() && discardedds.hasUnits()){
			if (!keptds.getUnit().getComputationalCode().equals(discardedds.getUnit().getComputationalCode())){
				ConversionFactorDialog condia = new ConversionFactorDialog(
						keptds.getName(), discardedds.getName(), keptds.getUnit().getComputationalCode(),
						discardedds.getUnit().getComputationalCode());
				replacementtext = condia.cdwdAndConversionFactor;
				conversionfactor = condia.conversionfactor;
				cancelmerge = !condia.process;
			}
		}
		
		if(cancelmerge) return false;
		
		// if the two terms have different names, or a conversion factor is required
		if(!discardedds.getName().equals(keptds.getName()) || conversionfactor!=1){
			SemSimUtil.replaceCodewordInAllEquations(discardedds, keptds, modelfordiscardedds, discardedds.getName(), replacementtext, conversionfactor);
		}
		// What to do about sol doms that have different units?
		
		if(discardedds.isSolutionDomain()){
		  // Re-set the solution domain designations for all DataStructures in model 2
			for(DataStructure nsdds : ssm2clone.getDataStructures()){
				if(nsdds.hasSolutionDomain())
					nsdds.setSolutionDomain(soldom1);
			}
			// Remove .min, .max, .delta solution domain DataStructures
			modelfordiscardedds.removeDataStructure(discardedds.getName() + ".min");
			modelfordiscardedds.removeDataStructure(discardedds.getName() + ".max");
			modelfordiscardedds.removeDataStructure(discardedds.getName() + ".delta");
		}
		
		// Remove the discarded Data Structure
		modelfordiscardedds.removeDataStructure(discardedds.getName());
		
		// If we are removing a state variable, remove its derivative, if present
		if(discardedds.hasSolutionDomain()){
			if(modelfordiscardedds.containsDataStructure(discardedds.getName() + ":" + discardedds.getSolutionDomain().getName())){
				modelfordiscardedds.removeDataStructure(discardedds.getName() + ":" + discardedds.getSolutionDomain().getName());
			}
		}
		
		// If the semantic resolution took care of a syntactic resolution
		if(!choicelist.get(index).equals(ResolutionChoice.ignore)){
			overlapmap.getIdenticalNames().remove(discardedds.getName());
		}
		return true;
	}
	
}
