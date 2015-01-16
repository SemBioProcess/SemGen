package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.utilities.SemGenTask;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;

public class MergerTask extends SemGenTask {
	private SemSimModel ssm1clone, ssm2clone, mergedmodel;
	private ModelOverlapMap overlapmap;
	private HashMap<String, String> cwnamemap;
	private ArrayList<ResolutionChoice> choicelist;
	private ArrayList<Pair<Double,String>> conversionfactors;
	
	MergerTask(Pair<SemSimModel, SemSimModel> modelpair,
			ModelOverlapMap modelmap,HashMap<String, String> namemap, ArrayList<ResolutionChoice> choices, 
			ArrayList<Pair<Double,String>> conversions, SemGenProgressBar bar) {
		overlapmap = modelmap;
		try {
			ssm1clone = modelpair.getLeft().clone();
			ssm2clone = modelpair.getRight().clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		cwnamemap = namemap;
		choicelist = choices;
		conversionfactors = conversions;
		progframe = bar;
	}
	
    @Override
    public Void doInBackground() {	
    	try {
    		removeDomainBounds(overlapmap.getSolutionDomain().getName());
    		resolveSyntacticOverlap();
			Merger merger = new Merger(ssm1clone, ssm2clone, overlapmap, choicelist, conversionfactors);
			mergedmodel = merger.merge();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }

	// Why isn't this working for Pandit-Hinch merge?
	// Prompt the user to resolve the points of SYNTACTIC overlap (same codeword names)
	private void resolveSyntacticOverlap() {
		for (String dsname : cwnamemap.keySet()) {
			String newdsname = cwnamemap.get(dsname);
			ssm1clone.getDataStructure(dsname).setName(newdsname);
			Boolean derivreplace = false;
			String derivname = null;
			
			// If there is a derivative of the data structure that we're renaming, rename it, too
			if(ssm1clone.getDataStructure(newdsname).hasSolutionDomain()){
				derivname = dsname + ":" + ssm1clone.getDataStructure(newdsname).getSolutionDomain().getName();
				if(ssm1clone.containsDataStructure(derivname)){
					ssm1clone.getDataStructure(derivname).setName(derivname.replace(dsname, newdsname));
					derivreplace = true;
				}
			}
			// Use the new name in all the equations
			SemSimUtil.replaceCodewordInAllEquations(ssm1clone.getDataStructure(newdsname), ssm1clone.getDataStructure(newdsname),
					ssm1clone, dsname, newdsname, 1);
			
			// IS THERE AN ISSUE WITH SELF_REF_ODEs HERE?
			if(derivreplace){
				SemSimUtil.replaceCodewordInAllEquations(ssm1clone.getDataStructure(derivname.replace(dsname, newdsname)),
						ssm1clone.getDataStructure(derivname.replace(dsname, newdsname)),
						ssm1clone, derivname, derivname.replace(dsname, newdsname), 1);
			}
		}
	}
    
	private void removeDomainBounds(String name) {
		// Remove .min, .max, .delta solution domain DataStructures
		ssm2clone.removeDataStructure(name + ".min");
		ssm2clone.removeDataStructure(name + ".max");
		ssm2clone.removeDataStructure(name + ".delta");
	}
	
    public SemSimModel getMergedModel() {
    	return mergedmodel;
    }
}
