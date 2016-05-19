package semgen.stage.stagetasks.merge;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;

import semgen.merging.dialog.ConversionFactorDialog;
import semgen.merging.workbench.Merger.ResolutionChoice;
import semgen.merging.workbench.MergerWorkbench;
import semgen.utilities.SemGenError;
import semgen.utilities.uicomponent.SemGenProgressBar;

public class MergeConflictResolvers {
	MergerWorkbench workbench;
	public MergeConflictResolvers(MergerWorkbench wb) {
		workbench = wb;
	}
	
	public void mergeButtonAction(ArrayList<ResolutionChoice> choicelist) {
		if (choicelist != null) {				
				HashMap<String, String> smnamemap = workbench.createIdenticalSubmodelNameMap();
				for(String oldsubmodelname : smnamemap.keySet()){
					String newsubmodelname = changeSubmodelNameDialog(oldsubmodelname);
					if (newsubmodelname==null) return;
					smnamemap.put(oldsubmodelname, newsubmodelname);
				}
				
				// Then refresh the identical codeword name mappings in ModelOverlapMap
				
				HashMap<String, String> cwnamemap = workbench.createIdenticalNameMap(choicelist, smnamemap.keySet());
				for (String name : cwnamemap.keySet()) {
					String newname = changeCodeWordNameDialog(name);
					if (newname==null) return;
					cwnamemap.put(name, newname);
				}
				
				ArrayList<Boolean> unitoverlaps = workbench.getUnitOverlaps();
				
				ArrayList<Pair<Double,String>> conversionlist = new ArrayList<Pair<Double,String>>(); 
				for (int i=0; i<unitoverlaps.size(); i++) {
					if (!unitoverlaps.get(i)) {
						ResolutionChoice choice = choicelist.get(i);
						if (!choice.equals(ResolutionChoice.ignore)) {
							if (!getConversion(conversionlist, i, choice.equals(ResolutionChoice.first))) return;
							continue;
						}
					}
					conversionlist.add(Pair.of(1.0, "*"));
				}

				SemGenProgressBar progframe = new SemGenProgressBar("Merging...", true);
				String error = workbench.executeMerge(cwnamemap, smnamemap, choicelist, conversionlist, progframe);
				if (error!=null){
					SemGenError.showError(
							"ERROR: " + error, "Merge Failed");
				}
		}
		else {
			JOptionPane.showMessageDialog(null, "Some codeword overlaps are unresolved.");
			return;
		}
	}
	
	//Prompt user for conversion factors, selection cancel returns 0 and cancels the merge
	private boolean getConversion(ArrayList<Pair<Double,String>> list, int index, boolean isfirst) {		
		ConversionFactorDialog condia = new ConversionFactorDialog(workbench.getDSDescriptors(index), isfirst);
		if (condia.getConversionFactor().getLeft().equals(0.0)) return false;
		list.add(condia.getConversionFactor());
		return true;
	}
	
	public String changeSubmodelNameDialog(String oldname){
		String newname = null;
		while(true){
			newname = JOptionPane.showInputDialog(null, "Both models contain a submodel called " + oldname + ".\n" +
					"Enter new name for the submodel from " + workbench.getModelName(0) + ".\nNo special characters, no spaces.", 
					"Duplicate submodel name", JOptionPane.OK_OPTION);
			if (newname==null) break;
			if(newname!=null && !newname.equals("")){
				if(newname.equals(oldname)){
					JOptionPane.showMessageDialog(null, "That is the existing name. Please choose a new one.");
				}
				else break;
			}
		}
		return newname;
	}
	
	
	public String changeCodeWordNameDialog(String dsname) {
		String newdsname = null;
		while(true){
			newdsname = JOptionPane.showInputDialog(null, "Both models contain codeword " + dsname + ".\n" +
					"Enter new name for use in " + workbench.getOverlapMapModelNames() + " equations.\nNo special characters, no spaces.", 
					"Duplicate codeword", JOptionPane.OK_OPTION);
			if (newdsname==null) break;
			if(!newdsname.equals("")){
				if(newdsname.equals(dsname)){
					JOptionPane.showMessageDialog(null, "That is the existing name. Please choose a new one.");
				}
				else break;
			}
		}
		return newdsname;
	}
}
