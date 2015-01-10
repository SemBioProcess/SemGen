package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import semsim.model.computational.datastructures.DataStructure;
import semsim.owl.SemSimOWLFactory;

public class ModelOverlapMap {
	Pair<Integer, Integer> modelindicies;
	private Set<String> identicaldsnames;
	private ArrayList<Pair<DataStructure, DataStructure>> dsmap = new ArrayList<Pair<DataStructure, DataStructure>>();
	private ArrayList<ResolutionChoice> choicelist;
	private ArrayList<maptype> maptypelist = new ArrayList<maptype>();	
	
	public static enum ResolutionChoice {
		noselection, first, second, ignore; 
	}
	protected static enum maptype {
		exactsemaoverlap("(exact semantic match)"), 
		manualmapping("(manual mapping)"),
		automapping("automated solution domain mapping");
		private String label;
		maptype(String lbl) {
			label = lbl;
		}
		public String getLabel() {
			return label;
		}
	}

	public ModelOverlapMap(int ind1, int ind2, SemanticComparator comparator) {
		modelindicies = Pair.of(ind1, ind2);
		identicaldsnames = comparator.identifyIdenticalCodewords();
		for (Pair<DataStructure, DataStructure> dspair : comparator.identifyExactSemanticOverlap()) {
			addDataStructureMapping(dspair.getLeft(), dspair.getRight(), maptype.exactsemaoverlap);
		}
	}
	
	public String getMappingType(int index) {
		return maptypelist.get(index).getLabel();
	}
	
	public void addDataStructureMapping(DataStructure ds1, DataStructure ds2, maptype type) {
		dsmap.add(Pair.of(ds1, ds2));
		maptypelist.add(type);
	}
	
	public Set<String> getIdenticalNames() {
		return identicaldsnames;
	}

	public Pair<Integer, Integer> getModelIndicies() {
		return modelindicies;
	}
	
	public Pair<String, String> getDataStructurePairNames(int index) {
		Pair<DataStructure, DataStructure> dspair = dsmap.get(index); 
		return getDataStructurePairNames(dspair);
	}
	
	public Pair<String, String> getDataStructurePairNames(Pair<DataStructure, DataStructure> dspair) { 
		return Pair.of(dspair.getLeft().getName(), dspair.getRight().getName());
	}
	
	public Boolean codewordsAlreadyMapped(String cdwd1uri, String cdwd2uri) {
		String cdwd1 = SemSimOWLFactory.getIRIfragment(cdwd1uri);
		String cdwd2 = SemSimOWLFactory.getIRIfragment(cdwd2uri);
		Pair<String, String> cwnpr = Pair.of(cdwd1, cdwd2);
		Boolean alreadymapped = false;
		while (!alreadymapped) {
			for (Pair<DataStructure, DataStructure> dsp : dsmap) {
				alreadymapped = compareDataStructureNames(cwnpr, getDataStructurePairNames(dsp));		
			}
			break;
		}
		return alreadymapped;
	}
	
	public int getMappingCount() {
		return dsmap.size();
	}
	
	public boolean compareDataStructureNames(Pair<String, String> namepair1, Pair<String, String> namepair2) {
		return (namepair1.getLeft().equals(namepair2.getLeft()) && 
				namepair1.getRight().equals(namepair2.getRight())) || 
				(namepair1.getLeft().equals(namepair2.getRight()) && 
				namepair1.getRight().equals(namepair2.getLeft()));
	}
	
	public Pair<DataStructureDescriptor,DataStructureDescriptor> getDSPairDescriptors(int index) {
		DataStructureDescriptor dsd1 = new DataStructureDescriptor(dsmap.get(index).getLeft());
		DataStructureDescriptor dsd2 = new DataStructureDescriptor(dsmap.get(index).getLeft());
		return Pair.of(dsd1, dsd2);
	}
	
	public ArrayList<Pair<DataStructure, DataStructure>> getDataStructurePairs() {
		return dsmap;
	}
	
	public void setUserSelections(ArrayList<ResolutionChoice> choices) {
		choicelist = choices;
	}
	
	public ResolutionChoice getResolution(int index) {
		return choicelist.get(index);
	}
}
