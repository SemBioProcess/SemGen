package semsim.utilities;

import java.util.Comparator;

import semsim.SemSimObject;

public class SemSimComponentComparator implements Comparator<SemSimObject>{
	public int compare(SemSimObject A, SemSimObject B) {
	    return A.getName().compareToIgnoreCase(B.getName());
	  }
}
