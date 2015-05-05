package semgen.annotation.workbench.drawers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimComponentComparator;
import semsim.writing.CaseInsensitiveComparator;

public class SubModelToolDrawer extends AnnotatorDrawer<Submodel> {
	
	public SubModelToolDrawer(Set<Submodel> modlist) {
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
		return componentlist.get(index).hasRefersToAnnotation();
	}

	public Submodel removeSubmodel() {
		return componentlist.remove(currentfocus);
	}
	
	public Submodel addSubmodel(String name) {
		Submodel sm = new Submodel(name);
		componentlist.add(sm);
		return sm;
	}
	
	public ArrayList<DataStructure> getDataStructures() {
		return getDataStructures(currentfocus);
	}
	
	public ArrayList<DataStructure> getDataStructures(Integer index) {
		ArrayList<DataStructure> smdslist = new ArrayList<DataStructure>();
		smdslist.addAll(componentlist.get(index).getAssociatedDataStructures());
		
		Collections.sort(smdslist, new SemSimComponentComparator());
		
		return smdslist;
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
		componentlist.get(currentfocus).setName(newname);
		refreshSubModels();
		setChanged();
		notifyObservers(modeledit.smnamechange);
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
	protected void selectionNotification() {
		notifyObservers(WBEvent.smselection);
	}
	
	public boolean isFunctional() {
		return componentlist.get(currentfocus).isFunctional();
	}
	
	public boolean isFunctional(int index) {
		return componentlist.get(index).isFunctional();
	}

	@Override
	public String getSingularAnnotationasString(int index) {
		if (hasSingularAnnotation(index)) {
			return componentlist.get(index).getFirstRefersToReferenceOntologyAnnotation().getNamewithOntologyAbreviation();
		}
		return SemSimModel.unspecifiedName;
	}
	
	@Override
	public URI getSingularAnnotationURI(int index) {
		return componentlist.get(index).getFirstRefersToReferenceOntologyAnnotation().getReferenceURI();
	}
	
	public ArrayList<Integer> getSubmodelsWithoutFocus() {
		ArrayList<Integer> submodels = new ArrayList<Integer>();
		for (int i = 0; i < submodels.size(); i++) {
			submodels.add(i);
		}
		submodels.remove(currentfocus);
		
		return submodels;
	}
	
	@Override
	protected void changeNotification() {
		setChanged();
		notifyObservers(modeledit.submodelchanged);
	}
}
