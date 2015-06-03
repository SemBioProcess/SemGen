package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;

public class ModelOverlapMap {
	Pair<Integer, Integer> modelindicies;
	private Set<String> identicalsubmodelnames;
	private Set<String> identicaldsnames;
	private SemanticComparator comparator;
	private ArrayList<Pair<DataStructure, DataStructure>> dsmap = new ArrayList<Pair<DataStructure, DataStructure>>();

	private ArrayList<maptype> maptypelist = new ArrayList<maptype>();	
	private int slndomcnt = 0;
	
	protected static enum maptype {
		exactsemaoverlap("exact semantic match"), 
		manualmapping("manual mapping"),
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
		this.comparator = comparator;
		
		ArrayList<Pair<DataStructure, DataStructure>> equivlist = comparator.identifyExactSemanticOverlap();		
		
		Pair<DataStructure, DataStructure> dspair;
		if (comparator.hasSolutionMapping()) {
			slndomcnt = 1;
			dspair = equivlist.get(0);
			addDataStructureMapping(dspair.getLeft(), dspair.getRight(), maptype.automapping);
		}
		
		for (int i=slndomcnt; i<(equivlist.size()); i++ ) {
			dspair = equivlist.get(i);
			addDataStructureMapping(dspair.getLeft(), dspair.getRight(), maptype.exactsemaoverlap);
		}
		identicalsubmodelnames = comparator.getIdenticalSubmodels();
		identicaldsnames = comparator.getIdenticalCodewords();
	}
	
	
	public String getMappingType(int index) {
		return maptypelist.get(index).getLabel();
	}
	
	public void addDataStructureMapping(DataStructure ds1, DataStructure ds2, maptype type) {
		dsmap.add(Pair.of(ds1, ds2));
		maptypelist.add(type);
	}
	
	public Set<String> getIdenticalSubmodelNames(){
		return identicalsubmodelnames;
	}
	
	public Set<String> getIdenticalNames(){
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
	
	public Boolean dataStructuresAlreadyMapped(DataStructure ds1, DataStructure ds2) {
		for (Pair<DataStructure, DataStructure> dspair : dsmap) {
			if (dspair.getLeft().equals(ds1) || dspair.getRight().equals(ds2)) return true;
		}
		return false;
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
		DataStructure ds1 = dsmap.get(index).getLeft();
		DataStructure ds2 = dsmap.get(index).getRight();
		DataStructureDescriptor dsd1 = new DataStructureDescriptor(ds1);
		DataStructureDescriptor dsd2 = new DataStructureDescriptor(ds2);
		return Pair.of(dsd1, dsd2);
	}
	
	public ArrayList<Pair<DataStructure, DataStructure>> getDataStructurePairs() {
		return dsmap;
	}
	
	//Compare units of all Data Structures in the overlap map. Determine if terms are equivalent
	// for each. Return a list of comparisons
	public ArrayList<Boolean> compareDataStructureUnits() {
		ArrayList<Boolean> unitmatchlist = new ArrayList<Boolean>();
		
		Hashtable<String, Set<UnitFactor>> baseUnitsTableModel1 = SemSimUtil.getAllUnitsAsFundamentalBaseUnits(comparator.model1);
		Hashtable<String, Set<UnitFactor>> baseUnitsTableModel2 = SemSimUtil.getAllUnitsAsFundamentalBaseUnits(comparator.model2);
		
		for (Pair<DataStructure, DataStructure> dsp : dsmap) {
			if(dsp.getLeft().hasUnits() && dsp.getRight().hasUnits()){
				
				String dspleftunitname = dsp.getLeft().getUnit().getName();
				String dsprightunitname = dsp.getRight().getUnit().getName();
				
				Set<UnitFactor> dsfrommodel1baseunits = baseUnitsTableModel1.get(dspleftunitname);
				Set<UnitFactor> dsfrommodel2baseunits = baseUnitsTableModel2.get(dsprightunitname);
				
				// If the size of the unit factor sets aren't equal, then the units
				// aren't equivalent
				if(dsfrommodel1baseunits.size()!=dsfrommodel2baseunits.size()){
					unitmatchlist.add(false);
					continue;
				}
				// If the units on the data structures are both fundamental and
				// don't have the same name, then the units aren't equivalent
				else if(dsfrommodel1baseunits.size()==0){
					
					if(! dspleftunitname.equals(dsprightunitname)){
						unitmatchlist.add(false);
						continue;
					}
				}
				// Otherwise we do have some unit factors to compare
				else if(dsfrommodel1baseunits.size()>0){
					
					// Compare the name, prefix and exponent for each base factor
					// If any differences, then the units aren't equivalent
					Set<UnitFactor> baseunitsmatched = new HashSet<UnitFactor>();
					
					for(UnitFactor baseunitfactor1 : dsfrommodel1baseunits){
						for(UnitFactor baseunitfactor2 : dsfrommodel2baseunits){
							if(!baseunitsmatched.contains(baseunitfactor2)){
								
								boolean samename = baseunitfactor1.getBaseUnit().getName().equals(baseunitfactor2.getBaseUnit().getName());
								boolean sameexponent = false;
								boolean sameprefix = false;

								if(Double.valueOf(baseunitfactor1.getExponent())!=null 
										&& Double.valueOf(baseunitfactor2.getExponent())!=null){

									if(baseunitfactor1.getExponent()==baseunitfactor2.getExponent()){
										sameexponent = true;
									}
								}
								else sameexponent = true;
								 
								if(baseunitfactor1.getPrefix()!=null && baseunitfactor2.getPrefix()!=null){
									
									if(baseunitfactor1.getPrefix().equals(baseunitfactor2.getPrefix())){
										sameprefix = true;
									}
								}
								else sameprefix = true;
								
								if(samename && sameexponent && sameprefix){
									baseunitsmatched.add(baseunitfactor2);
									break;
								}
							}
						}
					}
					// If we haven't matched all the unit factors, then units aren't equivalent
					if(baseunitsmatched.size()!=dsfrommodel1baseunits.size()){
						unitmatchlist.add(false);
						continue;
					}
				} 
			}
			// If we are here, then the units are equivalent
			unitmatchlist.add(true);
		}
		return unitmatchlist;
	}
	
	public int getSolutionDomainCount() {
		return slndomcnt;
	}
}
