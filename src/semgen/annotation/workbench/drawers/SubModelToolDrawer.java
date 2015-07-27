package semgen.annotation.workbench.drawers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.ModelEdit;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimComponentComparator;
import semsim.writing.CaseInsensitiveComparator;

public class SubModelToolDrawer extends AnnotatorDrawer<Submodel> {
	public SubModelToolDrawer(SemSimTermLibrary lib, Set<Submodel> modlist) {
		super(lib);
		componentlist.addAll(modlist);
		refreshSubModels();
	}
	
	public void refreshSubModels() {
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
		if(sm.isFunctional()){
			editable = sm.getParentImport()==null;
		}
		return editable;
	}
	
	public boolean isImported() {
		return componentlist.get(currentfocus).isImported();
	}
	
	public String getCodewordName(int index) {
		return componentlist.get(index).getName();
	}
	
	public boolean hasSingularAnnotation(int index) {
		return false;
	}

	public Submodel removeSubmodel() {
		return componentlist.remove(currentfocus);
	}
	
	public void addSubmodelstoSubmodel(ArrayList<Integer> sms) {
		Set<Submodel> smset = new HashSet<Submodel>();
		for (Integer i : sms) {
			smset.add(componentlist.get(i));
		}
		componentlist.get(currentfocus).setSubmodels(smset);
	}
	
	public Submodel addSubmodel(String name) {
		Submodel sm = new Submodel(name);
		componentlist.add(sm);
		refreshSubModels();
		currentfocus = componentlist.indexOf(sm);
		return sm;
	}
	
	public ArrayList<DataStructure> getSelectionDataStructures() {
		return getDataStructures(currentfocus);
	}
	
	public ArrayList<DataStructure> getDataStructures(Integer index) {
		ArrayList<DataStructure> smdslist = new ArrayList<DataStructure>();
		smdslist.addAll(componentlist.get(index).getAssociatedDataStructures());
		
		Collections.sort(smdslist, new SemSimComponentComparator());
		
		return smdslist;
	}
	
	public void setDataStructures(Set<DataStructure> dsset) {
		componentlist.get(currentfocus).setAssociatedDataStructures(dsset);
	}
	
	public ArrayList<String> getAssociatedSubModelDataStructureNames() {
		Set<DataStructure> smset = SemSimOWLFactory.getCodewordsAssociatedWithNestedSubmodels(componentlist.get(currentfocus));
		
		ArrayList<String> associated = new ArrayList<String>();
		for (DataStructure ds : smset) {
			String name = ds.getName();
			// Get rid of prepended submodel names if submodel is functional
			if (isFunctional()) name = name.substring(name.lastIndexOf(".")+1);
			associated.add(name);
		}
		
		Collections.sort(associated, new CaseInsensitiveComparator());
		return associated;
	}
	
	public ArrayList<String> getDataStructureNames() {
		ArrayList<String> smdslist = new ArrayList<String>();
		for (DataStructure ds : componentlist.get(currentfocus).getAssociatedDataStructures()) {
			String name = ds.getName();
			// Get rid of prepended submodel names if submodel is functional
			if (isFunctional()) name = name.substring(name.lastIndexOf(".")+1);
			smdslist.add(name);
		}
		
		Collections.sort(smdslist, new CaseInsensitiveComparator());
		
		return smdslist;
	}
	
	public void setSubmodelName(String newname) {
		Submodel sm = componentlist.get(currentfocus);
		sm.setName(newname);
		refreshSubModels();
		currentfocus = componentlist.indexOf(sm);
		setChanged();
		notifyObservers(ModelEdit.SMNAMECHANGED);
	}
	
	public ArrayList<String> getAssociatedSubmodelNames() {
		ArrayList<String> associated = new ArrayList<String>();
		for (Submodel sm : componentlist.get(currentfocus).getSubmodels()) {
			if (sm.getAssociatedDataStructures().isEmpty()) continue;
			associated.add(sm.getName());
		}
		
		Collections.sort(associated, new CaseInsensitiveComparator());
		
		return associated;
	}
	
	@Override
	public void setHumanReadableDefinition(String newdef, boolean autoann){
		componentlist.get(currentfocus).setDescription(newdef);
		setChanged();
		notifyObservers(ModelEdit.FREE_TEXT_CHANGED);
	}
	
	@Override
	protected void selectionNotification() {
		notifyObservers(WBEvent.SMSELECTION);
	}
	
	public boolean isFunctional() {
		return componentlist.get(currentfocus).isFunctional();
	}
	
	public boolean isFunctional(int index) {
		return componentlist.get(index).isFunctional();
	}

	public String getHrefValue() {
		return 	componentlist.get(currentfocus).getHrefValue();
	}
		
	public ArrayList<Integer> getSubmodelsWithoutFocus() {
		ArrayList<Integer> submodels = new ArrayList<Integer>();
		for (int i = 0; i < componentlist.size(); i++) {
			submodels.add(i);
		}
		submodels.remove(currentfocus);
		
		return submodels;
	}
	
	public ArrayList<Integer> getAssociatedSubmodelIndicies(ArrayList<Integer> sms) {
		ArrayList<Integer> associates = new ArrayList<Integer>();
		
		for (Submodel sm : componentlist.get(currentfocus).getSubmodels()) {
			associates.add(componentlist.indexOf(sm));
		}
		return associates;
	}
	
	public ArrayList<Integer> getFunctionalSubmodelIndicies(ArrayList<Integer> sms) {
		ArrayList<Integer> associates = new ArrayList<Integer>();
		
		for (Integer sm : sms) {
			if (componentlist.get(sm).isFunctional()) {
				associates.add(sm);
			}
		}
		return associates;
	}
	
	@Override
	protected void changeNotification() {
		setChanged();
		notifyObservers(ModelEdit.SUBMODEL_CHANGED);
	}
}
