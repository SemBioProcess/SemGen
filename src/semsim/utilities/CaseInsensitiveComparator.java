package semsim.utilities;

import java.util.Comparator;

/**
 * Convenience class for performing case-insensitive comparisons between Strings
 * @author mneal
 *
 */
public class CaseInsensitiveComparator implements Comparator<String>{
	public int compare(String strA, String strB) {
	    return strA.compareToIgnoreCase(strB);
	  }
}
