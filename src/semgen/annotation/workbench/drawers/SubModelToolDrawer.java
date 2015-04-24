package semgen.annotation.workbench.drawers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.utilities.SemSimComponentComparator;

public class SubModelToolDrawer extends AnnotatorDrawer {
	private ArrayList<Submodel> submodels;
	private Integer currentfocus = -1;
	
	public SubModelToolDrawer(Set<Submodel> modlist) {
		submodels = new ArrayList<Submodel>();
		submodels.addAll(modlist);
		refreshSubModels();
	}
	
	private void refreshSubModels() {
		Collections.sort(submodels, new SemSimComponentComparator());
	}
	
	public ArrayList<Integer> getSubmodelstoDisplay(boolean showimports) {
		ArrayList<Integer> sms = new ArrayList<Integer>();
		
		int i = 0;
		for (Submodel sm : submodels) {
			if (sm.isImported() && showimports) continue;
			sms.add(i);
			i++;
		}
		return sms;
	}
	
	public boolean isEditable(int index) {
		boolean editable = true;
		Submodel sm = submodels.get(index);
		if(sm instanceof FunctionalSubmodel){
			editable = sm.getParentImport()==null;
		}
		return editable;
	}
	
	public String getCodewordName(int index) {
		return submodels.get(index).getName();
	}
	
	public boolean hasHumanReadableDef(int index) {
		return submodels.get(index).getDescription()!="";
	}
	
	public boolean hasSingularAnnotation(int index) {
		return submodels.get(index).hasRefersToAnnotation();
	}
	
	public void setFocusIndex(int index) {
		currentfocus = index;
	}
	
	public Submodel removeSubmodel() {
		Submodel sm = submodels.get(currentfocus);
		submodels.remove(currentfocus);
		refreshSubModels();
		return sm;
	}
	
	public Submodel addSubmodel(String name) {
		Submodel sm = new Submodel(name);
		submodels.add(sm);
		refreshSubModels();
		return sm;
	}
	
	public void changeSubmodelName(String newname) {
		submodels.get(currentfocus).setName(newname);
		setChanged();
		notifyObservers(modeledit.smlistchanged);
	}
	
	public Boolean containsComponentwithName(String name){
		for (Submodel sm : submodels) {
			if (sm.getName().equals(name)) return true;
		}
		return false;
	}
	
	//Temporary
	public Submodel getCurrentSelection() {
		return submodels.get(currentfocus);
	}

	@Override
	protected void selectionNotification() {
		notifyObservers(modeledit.smselection);
	}
}
