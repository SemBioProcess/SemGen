package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Set;

import semgen.annotation.dialog.HumanDefEditor;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.writing.CaseInsensitiveComparator;

public abstract class AnnotatorObservable extends Observable {
	protected Integer cindex;
	protected SemSimModel semsimmodel;
	protected ArrayList<SemSimComponent> ComponentList;
	public static final Integer CODEWORD = 0, SUBMODEL = 1;
	
	public AnnotatorObservable(SemSimModel ssm) {
		semsimmodel = ssm;
		generateList();
	}
	
	public String getName(Integer index) {
		return ComponentList.get(index).getName();
	}
	
	public String getName() {
		return ComponentList.get(cindex).getName();
	}
	
	protected void alphabetizeandCreateList(ArrayList<String> names, Set<? extends SemSimComponent> list) {
		Collections.sort(names, new CaseInsensitiveComparator());		
		for (String s : names) {
			for (SemSimComponent c : list) {
				if (s==c.getName()) {
					ComponentList.add(c);
				}
			}
		}
	}

	public int getNumberofElements() {
		return ComponentList.size();
	}
	
	public void replaceElement(int index, SemSimComponent ssc) {
		ComponentList.remove(index);
		ComponentList.add(index, ssc);
	}
		
	public boolean hasFreeText(int index) {
		return !ComponentList.get(index).getDescription().equals("");
	}
	
	public void setFreeText(int index) {
		SemSimComponent ssc = ComponentList.get(index);
		HumanDefEditor humdefeditor = new HumanDefEditor(ssc.getName(), ssc.getDescription());
		String newdef = humdefeditor.getFreeTextAnnotation();
		if (newdef!=ssc.getDescription()){
			ComponentList.get(index).setDescription(newdef);
			notifyObservers();
		}
	}
	
	public ArrayList<Boolean> getVisibilityList() {
		int size = ComponentList.size();
		ArrayList<Boolean> list = new ArrayList<Boolean>(size);
		for (int i=0; i > size; i++) {
			list.set(i, isVisible(i));
		}
		return list;
	}
	
	public int getNumberVisible() {
		return getVisibilityList().size();
	}
		
	public abstract boolean hasSingular(int index);
	
	public abstract void setSingular(int index);
	
	public abstract ReferenceOntologyAnnotation getSingular(int index);
	
	public abstract void generateList();
	
	public abstract Boolean isVisible();
	
	public abstract Boolean isVisible(int index);
	
	public abstract Integer getFocusIndex();
	
}
