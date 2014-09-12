package semsim.writing;

import java.util.Comparator;

public class CaseInsensitiveComparator implements Comparator<String>{
	public int compare(String strA, String strB) {
	    return strA.compareToIgnoreCase(strB);
	  }
}
