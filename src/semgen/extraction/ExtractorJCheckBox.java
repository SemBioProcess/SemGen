package semgen.extraction;


import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import semsim.SemSimObject;
import semsim.model.computational.datastructures.DataStructure;


public class ExtractorJCheckBox extends JCheckBox {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3088569216874092127L;
	// data structures are the codewords, not the URIs
	public Set<DataStructure> associateddatastructures = new HashSet<DataStructure>();
	public SemSimObject smc;

	public ExtractorJCheckBox(String name, Set<DataStructure> assocdatastr) {
		super(name);
		associateddatastructures.addAll(assocdatastr);
		this.setToolTipText(concatCodewordsForToolTip(associateddatastructures));
	}
	
	public ExtractorJCheckBox(String name, SemSimObject smc, Set<DataStructure> assocdatastr){
		super(name);
		associateddatastructures.addAll(assocdatastr);
		this.setToolTipText(concatCodewordsForToolTip(associateddatastructures));
		this.smc = smc;
	}
	
	public static String concatCodewordsForToolTip(Set<DataStructure> associateddatastructures){
		String tooltip = "";
		for (DataStructure ds : associateddatastructures) {
			tooltip = tooltip + ds.getName() + ", ";
		}
		if(!tooltip.equals("")){
			tooltip = tooltip.substring(0, tooltip.lastIndexOf(","));
		}
		return tooltip;
	}
}
