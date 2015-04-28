package semgen.annotation.workbench.drawers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.utilities.SemSimComponentComparator;

public class SubModelToolDrawer extends AnnotatorDrawer<Submodel> {
	private Integer currentfocus = -1;
	
	public SubModelToolDrawer(Set<Submodel> modlist) {
		componentlist = new ArrayList<Submodel>();
		componentlist.addAll(modlist);
		refreshSubModels();
	}
	
	private void refreshSubModels() {
		Collections.sort(componentlist, new SemSimComponentComparator());
	}
	
	public ArrayList<Integer> getSubmodelstoDisplay(boolean showimports) {
		ArrayList<Integer> sms = new ArrayList<Integer>();
		
		int i = 0;
		for (Submodel sm : componentlist) {
			if (sm.isImported() && showimports) continue;
			sms.add(i);
			i++;
		}
		return sms;
	}
	
	public boolean isEditable(int index) {
		boolean editable = true;
		Submodel sm = componentlist.get(index);
		if(sm instanceof FunctionalSubmodel){
			editable = sm.getParentImport()==null;
		}
		return editable;
	}
	
	public String getCodewordName(int index) {
		return componentlist.get(index).getName();
	}
	
	public boolean hasSingularAnnotation(int index) {
		return componentlist.get(index).hasRefersToAnnotation();
	}

	public Submodel removeSubmodel() {
		Submodel sm = componentlist.get(currentfocus);
		componentlist.remove(currentfocus);
		refreshSubModels();
		return sm;
	}
	
	public Submodel addSubmodel(String name) {
		Submodel sm = new Submodel(name);
		componentlist.add(sm);
		refreshSubModels();
		return sm;
	}
	
	public void changeSubmodelName(String newname) {
		componentlist.get(currentfocus).setName(newname);
		setChanged();
		notifyObservers(modeledit.smlistchanged);
	}
	
	public ArrayList<DataStructure> getDataStructures(Integer index) {
		ArrayList<DataStructure> smdslist = new ArrayList<DataStructure>();
		smdslist.addAll(componentlist.get(index).getAssociatedDataStructures());
		
		Collections.sort(smdslist, new SemSimComponentComparator());
		
		return smdslist;
	}
	
	@Override
	protected void selectionNotification() {
		notifyObservers(modeledit.smselection);
	}
}
