package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Set;

import semgen.annotation.dialog.referencedialog.SingularAnnotationEditor;
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
	
	private Submodel getSubModel(Integer index) {
		return ((Submodel)ComponentList.get(index));
	}
}
