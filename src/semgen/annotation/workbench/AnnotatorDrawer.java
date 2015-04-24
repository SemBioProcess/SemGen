package semgen.annotation.workbench;

import java.util.Observable;

public abstract class AnnotatorDrawer extends Observable {
	protected int currentfocus = -1;
	
	public abstract String getCodewordName(int index);
	public abstract boolean isEditable(int index);
	public abstract Boolean containsComponentwithName(String name);
	
	public void setSelectedIndex(int index) {
		currentfocus = index;
	}
	
	public int getSelectedIndex() {
		return currentfocus;
	}
}
