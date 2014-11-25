package semgen.utilities;

import java.awt.Component;
import java.util.Comparator;

public class ComparatorByName implements Comparator<Component> {
	// Comparator interface requires defining compare method.
	public int compare(Component comp1, Component comp2) {
		return comp1.getName().compareToIgnoreCase(comp2.getName());
	}	
}
