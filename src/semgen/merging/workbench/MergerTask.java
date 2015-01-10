package semgen.merging.workbench;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import semgen.utilities.SemGenTask;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;

public class MergerTask extends SemGenTask {
	private SemSimModel ssm1clone, ssm2clone, mergedmodel;
	private ModelOverlapMap overlapmap;
	private HashMap<String, String> cwnamemap;
	
	MergerTask(Pair<SemSimModel, SemSimModel> modelpair,
			ModelOverlapMap modelmap,HashMap<String, String> namemap, SemGenProgressBar bar) {
		overlapmap = modelmap;
		try {
			ssm1clone = modelpair.getLeft().clone();
			ssm2clone = modelpair.getRight().clone();
			cwnamemap = namemap;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		progframe = bar;
	}
	
    @Override
    public Void doInBackground() {	
    	try {
    		resolveSyntacticOverlap();
			Merger merger = new Merger(ssm1clone, ssm2clone, overlapmap);
			mergedmodel = merger.merge();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }

	// Why isn't this working for Pandit-Hinch merge?
	// Prompt the user to resolve the points of SYNTACTIC overlap (same codeword names)
	private void resolveSyntacticOverlap() {
		for (String dsname : overlapmap.getIdenticalNames()) {
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
    
    public SemSimModel getMergedModel() {
    	return mergedmodel;
    }
}
