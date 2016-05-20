package semgen.annotation.workbench;

/*
 * Maps a model as a tree structure. Used for the annotator tree view. 
 */

import java.util.ArrayList;

import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semsim.model.computational.datastructures.DataStructure;

public class AnnotatorTreeMap {
	private ArrayList<TreeBranch> branches = new ArrayList<TreeBranch>();
	private ArrayList<Integer> orphands;
	
	public AnnotatorTreeMap(boolean showimports, SubModelToolDrawer smdrawer, CodewordToolDrawer cwdrawer) {
		Boolean [] options = new Boolean[]{showimports, false, false};
		orphands = cwdrawer.getCodewordstoDisplay(options);
		
		makeTree(showimports, smdrawer, cwdrawer);
	}
	
	private void makeTree(boolean showimports, SubModelToolDrawer smdrawer, CodewordToolDrawer cwdrawer) {
		ArrayList<Integer> sm2disp = smdrawer.getSubmodelsToDisplay(showimports);
		
		for (Integer sm : sm2disp) {
			TreeBranch branch = new TreeBranch();
			branch.smindex = sm;
			
			for (DataStructure ds : smdrawer.getDataStructures(sm)) {
				int dsi = cwdrawer.getIndexofComponent(ds);
				if (showimports || !ds.isImportedViaSubmodel()) {
					branch.dsindicies.add(dsi);
				}
				removeOrphanedDataStructure(dsi);
			}
			
			branches.add(branch);
		}
	}
	
	private void removeOrphanedDataStructure(Integer ds) {
		if (orphands.contains(ds)) {
			orphands.remove(ds);
		}
	}

	public ArrayList<Integer> getSubmodelList() {
		ArrayList<Integer> smlist = new ArrayList<Integer>();
		for (TreeBranch branch : branches) {
			smlist.add(branch.smindex);
		}
		return smlist;
	}
	
	public ArrayList<Integer> getSubmodelDSIndicies(int index) {
		return branches.get(index).dsindicies;
	}
	
	public ArrayList<Integer> getOrphanDS() {
		return orphands;
	}
	
	//Local data structure for storing Submodel and associatiated DataStructure indicies
	private class TreeBranch {
		public Integer smindex;
		public ArrayList<Integer> dsindicies = new ArrayList<Integer>();
	}
}
