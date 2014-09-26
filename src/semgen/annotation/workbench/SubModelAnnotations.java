package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import semgen.annotation.dialog.referencedialog.SingularAnnotationEditor;
import semgen.annotation.dialog.selectordialog.SemSimComponentSelectorDialog;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.DataStructure;
import semsim.model.physical.Submodel;

public class SubModelAnnotations extends AnnotatorObservable {	
	public SubModelAnnotations(SemSimModel ssm) {
		super(ssm);		
	}

	public void generateList() {
		Set<Submodel> submodels = semsimmodel.getSubmodels();
		
		ArrayList<String> names = new ArrayList<String>();
		
		for (Submodel sm : submodels ) {
			names.add(sm.getName());
		}
		alphabetizeandCreateList(names, submodels);
	}
	
	public void addSubModel(String name) {
		semsimmodel.addSubmodel(new Submodel(name));
		generateList();
		setChanged();
		notifyObservers();
	}

	public void notifyObservers() {
		notifyObservers(SUBMODEL);
	}
	
	public void modifyChildCodewords(int index, Set<DataStructure> dss) {
		String name = getSubModel(index).getName();
		semsimmodel.getSubmodel(name).setAssociatedDataStructures(dss);
		replaceElement(index, semsimmodel.getSubmodel(getSubModel(index).getName()));
	}
	
	public void modifyChildSubModel(int index, Set<Submodel> sms) {
		String name = ComponentList.get(index).getName();
		semsimmodel.getSubmodel(name).setSubmodels(sms);
		replaceElement(index, semsimmodel.getSubmodel(name));
	}
	
	public Integer getFocusIndex() {
		return cindex;
	}
		
	public void removeSubmodel(Integer index) {	
		semsimmodel.removeSubmodel(getSubModel(index));
		ComponentList.remove(index);
		notifyObservers();
	}
		
	public ReferenceOntologyAnnotation getSingular(int index) {
		return getSubModel(index).getFirstRefersToReferenceOntologyAnnotation();
	}
	
	public void assignSubModelstoSubmodel(Integer index) {
		Submodel target = getSubModel(index);
		
		ArrayList<Integer> children = new ArrayList<Integer>(getAllDownstreamSubmodelIndicies(target));
		Collections.sort(children);
		
		ArrayList<Integer> substodisable = new ArrayList<Integer>();
		substodisable.add(index);
		
		SemSimComponentSelectorDialog dss 
			= new SemSimComponentSelectorDialog(
					SemSimUtil.getNamesfromComponentSet(ComponentList, false), children, substodisable,
					"Select components for " + target.getName());
		if (dss.isChanged()) {
			Set<Submodel> selection = new HashSet<Submodel>(getSubmodelsfromIndicies(dss.getUserSelections()));
			target.setSubmodels(selection);
		}
	}
	
	public Set<Integer> getAllDownstreamSubmodelIndicies(Submodel model) {
		Set<Submodel> childsubs = model.getSubmodels();
		Set<Integer> children = new HashSet<Integer>();
		for (Submodel child : childsubs) {
			if (!child.getSubmodels().isEmpty()) {
				children.addAll(getAllDownstreamSubmodelIndicies(child));
			}
			children.add(ComponentList.indexOf(child));
		}
		return children;
	}
	
	protected ArrayList<Integer> getListofIndicies(ArrayList<Submodel> subs) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (Submodel sub : subs) {
			indicies.add(ComponentList.indexOf(sub));
		}
		return indicies;
	}
	
	protected ArrayList<Submodel> getSubmodelsfromIndicies(ArrayList<Integer> indicies) {
		ArrayList<Submodel> models = new ArrayList<Submodel>();
		for (Integer index : indicies) {
			models.add((Submodel) ComponentList.get(index));
		}
		return models;
	}
	
	@Override
	public boolean hasSingular(int index) {
		 return !getSubModel(index).hasRefersToAnnotation();
	}

	public int countSubmodels() {
		return semsimmodel.getSubmodels().size();
	}
	
	@Override
	public Boolean isVisible() {
		return !getSubModel(getFocusIndex()).isImported();
	}
	
	public Boolean isVisible(int index) {
		return !getSubModel(index).isImported();
	}

	@Override
	public void setSingular(int index) {
		Submodel sm = getSubModel(index);
		ReferenceOntologyAnnotation roa = sm.getFirstRefersToReferenceOntologyAnnotation();
		SingularAnnotationEditor sae = new SingularAnnotationEditor(roa);
		if (!sae.getAnnotation().matches(roa)) {
			sm.removeAllReferenceAnnotations();
			sm.addReferenceOntologyAnnotation(sae.getAnnotation());
		}
		notifyObservers();
	}
	
	Submodel getSubModel(Integer index) {
		return ((Submodel)ComponentList.get(index));
	}
}
