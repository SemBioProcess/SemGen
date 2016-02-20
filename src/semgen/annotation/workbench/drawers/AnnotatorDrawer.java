package semgen.annotation.workbench.drawers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import semgen.annotation.workbench.SemSimTermLibrary;
import semsim.SemSimObject;

public abstract class AnnotatorDrawer<T extends SemSimObject> extends Observable {
	protected SemSimTermLibrary termlib;
	protected int currentfocus = -1;
	protected ArrayList<T> componentlist = new ArrayList<T>();
	protected Set<Integer> changeset = new HashSet<Integer>();
	
	public AnnotatorDrawer(SemSimTermLibrary lib) {
		termlib = lib;
	}
	
	public String getComponentName(int index) {
		return componentlist.get(index).getName();
	}
	
	public String getComponentName() {
		return componentlist.get(currentfocus).getName();
	}
	
	public ArrayList<String> getComponentNamesfromIndicies(ArrayList<Integer> sms) {
		ArrayList<String> components = new ArrayList<String>();
		
		for (Integer i : sms) {
			components.add(componentlist.get(i).getName());
		}
		return components;
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
	
	public boolean isEditable() {
		return isEditable(currentfocus);
	}
	
	public Set<Integer> getChangedComponents() {
		Set<Integer> cset = new HashSet<Integer>(changeset);
		changeset.clear();
		return cset;
	}
	
	public ArrayList<T> getComponentsfromIndicies(ArrayList<Integer> sms) {
		ArrayList<T> components = new ArrayList<T>();
		
		for (Integer i : sms) {
			components.add(componentlist.get(i));
		}
		return components;
	}
	
	protected void addComponentstoChangeSet(Set<? extends T> objs) {
		for (T t : objs) {
			changeset.add(componentlist.indexOf(t));
		}
		changeNotification();
	}

	
	protected abstract void selectionNotification();
	protected abstract void changeNotification();
	public abstract boolean isEditable(int index);
	public abstract boolean isImported();

	public abstract void setHumanReadableDefinition(String newdef, boolean b);
}
