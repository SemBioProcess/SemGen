package semgen.annotation.workbench.drawers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Observable;

import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semsim.SemSimObject;

public abstract class AnnotatorDrawer<T extends SemSimObject> extends Observable {
	protected int currentfocus = -1;
	protected ArrayList<T> componentlist = new ArrayList<T>();
	
	public AnnotatorDrawer() {}
	
	public String getCodewordName(int index) {
		return componentlist.get(index).getName();
	}
	
	public String getCodewordName() {
		return componentlist.get(currentfocus).getName();
	}
	
	public Integer getIndexofComponent(T comp) {
		return componentlist.indexOf(comp);
	}
	
	public void setSelectedIndex(int index) {
		currentfocus = index;
		setChanged();
		selectionNotification();
	}
	
	public void setFocusIndex(int index) {
		currentfocus = index;
	}
	
	public int getSelectedIndex() {
		return currentfocus;
	}
	
	public void setHumanReadableDefinition(String newdef) {
		componentlist.get(currentfocus).setDescription(newdef);
		setChanged();
		notifyObservers(modeledit.freetextchange);
	}
	
	public boolean hasHumanReadableDef(int index) {
		return !componentlist.get(index).getDescription().isEmpty();
	}
	
	public boolean hasHumanReadableDef() {
		return !componentlist.get(currentfocus).getDescription().isEmpty();
	}
	
	public String getHumanReadableDef() {
		String desc = componentlist.get(currentfocus).getDescription();
		
		if (desc.isEmpty()) {
			if (isImported()) desc = "No free-text description specified";
			else desc = "Click to set free-text description";
		}
		return desc;
	}

	public Boolean containsComponentwithName(String name){
		for (T comp : componentlist) {
			if (comp.getName().equals(name)) return true;
		}
		return false;
	}
	
	public boolean hasSingularAnnotation() {
		return hasSingularAnnotation(currentfocus);
	}
	
	public String getSingularAnnotationasString() {
		return getSingularAnnotationasString(currentfocus);
	}
	
	public URI getSingularAnnotationURI() {
		return getSingularAnnotationURI(currentfocus);
	}
	
	public boolean isEditable() {
		return isEditable(currentfocus);
	}
	
	public abstract URI getSingularAnnotationURI(int index);
	public abstract boolean hasSingularAnnotation(int index);
	public abstract String getSingularAnnotationasString(int index);
	protected abstract void selectionNotification();
	public abstract boolean isEditable(int index);
	public abstract boolean isImported();
}
