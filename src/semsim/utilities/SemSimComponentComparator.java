package semsim.utilities;

import java.util.Comparator;

import semsim.model.SemSimComponent;

public class SemSimComponentComparator implements Comparator<SemSimComponent>{
	public int compare(SemSimComponent A, SemSimComponent B) {
	    return A.getName().compareToIgnoreCase(B.getName());
	  }
}
