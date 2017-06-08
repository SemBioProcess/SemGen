package semgen.stage.serialization;

import java.util.Comparator;

public class StageNodeComparator implements Comparator<Node<?>> {
	public int compare(Node<?> A, Node<?> B) {
	    return A.name.compareToIgnoreCase(B.name);
	  }
}
