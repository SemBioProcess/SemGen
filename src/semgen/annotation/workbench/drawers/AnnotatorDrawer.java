package semgen.annotation.workbench.drawers;

import java.util.ArrayList;
import java.util.Observable;

import semsim.SemSimObject;

public abstract class AnnotatorDrawer<T extends SemSimObject> extends Observable {
	protected int currentfocus = -1;
	protected ArrayList<T> componentlist = new ArrayList<T>();
	
	public AnnotatorDrawer() {
		
	}
	
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
	
	public boolean hasHumanReadableDef(int index) {
		return componentlist.get(index).getDescription()!="";
	}
	
	public String getHumanReadableDef() {
		String desc = componentlist.get(currentfocus).getDescription();
		if (desc!="") desc = "[unspecified]";
		return desc;
	}

	public Boolean containsComponentwithName(String name){
		for (T comp : componentlist) {
			if (comp.getName().equals(name)) return true;
		}
		return false;
	}
	
	public abstract boolean hasSingularAnnotation(int index);
	protected abstract void selectionNotification();
	public abstract boolean isEditable(int index);
	
	public boolean isEditable() {
		return isEditable(currentfocus);
	}
}
