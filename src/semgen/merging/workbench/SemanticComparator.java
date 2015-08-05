package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.utilities.DuplicateChecker;
import semsim.utilities.SemSimUtil;

public class SemanticComparator {
	public SemSimModel model1, model2;
	private DataStructure slndomain = null;

	public SemanticComparator(SemSimModel m1, SemSimModel m2) {
		model1 = m1; model2 = m2;
		DuplicateChecker.removeDuplicatePhysicalEntities(model1, model2);
		
		if ((model1.getSolutionDomains().size() > 0) && (model2.getSolutionDomains().size() > 0)) {
			slndomain = model1.getSolutionDomains().toArray(new DataStructure[]{})[0];
		}
	}
		
	// Collect the submodels that have the same name
	public Set<String> getIdenticalSubmodels(){
		Set<String> matchedsubmodels = new HashSet<String>();
		
		for (Submodel submodel : model1.getSubmodels()) {
			if (model2.getSubmodel(submodel.getName())!=null) matchedsubmodels.add(submodel.getName());
		}
		return matchedsubmodels;
	}
	
	
	// Collect the data structures that have the same name. Ignore CellML-type component inputs (mapped variables that have an "in" interface)
	public Set<String> getIdenticalCodewords() {
		Set<String> matchedcdwds = new HashSet<String>();
		
		for (DataStructure ds : model1.getAssociatedDataStructures()) {
			if(! ds.isFunctionalSubmodelInput()){
				if (model2.containsDataStructure(ds.getName()))	matchedcdwds.add(ds.getName());
			}
		}
		if (slndomain != null) {
			String slndomainname = slndomain.getName(); 
			matchedcdwds.remove(slndomainname);
			matchedcdwds.remove(slndomainname + ".min");
			matchedcdwds.remove(slndomainname + ".max");
			matchedcdwds.remove(slndomainname + ".delta");
		}
		return matchedcdwds;
	}
	
	public ArrayList<Pair<DataStructure, DataStructure>> identifyExactSemanticOverlap() {
		ArrayList<Pair<DataStructure, DataStructure>> dsmatchlist = new ArrayList<Pair<DataStructure, DataStructure>>();
		
		if(slndomain != null){
			DataStructure soldom2 = model2.getSolutionDomains().toArray(new DataStructure[]{})[0];
			dsmatchlist.add(Pair.of(slndomain, soldom2));
		}
		
		Set<DataStructure> model1ds = getComparableDataStructures(model1);
		Set<DataStructure> model2ds = getComparableDataStructures(model2);
		
		// For each comparable data structure in model 1...
		for(DataStructure ds1 : model1ds){
			
			// Exclude solution domains
			if (ds1 != slndomain) {
				
				// For each comparable data structure in model 2
				for(DataStructure ds2 : model2ds){
					Boolean match = false;
					
					// Test singular annotations
					if(ds1.hasRefersToAnnotation() && ds2.hasRefersToAnnotation()) {
						match = ds1.getSingularTerm().equals(ds2.getSingularTerm());
					}
					
					// If the physical properties are not null...
					if(!match && ds1.hasPhysicalProperty() && ds2.hasPhysicalProperty()){
						PhysicalPropertyinComposite prop1 = ds1.getPhysicalProperty();
						PhysicalPropertyinComposite prop2 = ds2.getPhysicalProperty();
							
						// Test equivalency of physical properties
						if(prop1.equals(prop2)) {
							if(ds1.hasAssociatedPhysicalComponent() && ds2.hasAssociatedPhysicalComponent()){
								PhysicalModelComponent ds1pmc = ds1.getAssociatedPhysicalModelComponent();
								PhysicalModelComponent ds2pmc = ds2.getAssociatedPhysicalModelComponent();
								
								// And they are properties of a specified physical model component
								// If the property annotations are the same, test the equivalency of what they are properties of
							
								match = ds1pmc.equals(ds2pmc);
							}
						}
					}
					if(match){
						dsmatchlist.add(Pair.of(ds1, ds2));
					}
				} // end of iteration through model2 data structures
			}
		} // end of iteration through model1 data structures
		return dsmatchlist;
	}
	
	
	// Identify semantically-equivalent units by decomposing units in both models
	// into their base units
	public HashMap<UnitOfMeasurement, UnitOfMeasurement> identifyEquivalentUnits(){
		
		HashMap<UnitOfMeasurement, UnitOfMeasurement> equivunitslist = 
				new HashMap<UnitOfMeasurement, UnitOfMeasurement>();
				
		HashMap<String, Set<UnitFactor>> baseUnitsTableModel1 = SemSimUtil.getAllUnitsAsFundamentalBaseUnits(model1);
		HashMap<String, Set<UnitFactor>> baseUnitsTableModel2 = SemSimUtil.getAllUnitsAsFundamentalBaseUnits(model2);
		
		for (String unitnamemodel1 : baseUnitsTableModel1.keySet()) {
			
			for(String unitnamemodel2 : baseUnitsTableModel2.keySet()){
								
				Set<UnitFactor> unitfrommodel1baseunits = baseUnitsTableModel1.get(unitnamemodel1);
				Set<UnitFactor> unitfrommodel2baseunits = baseUnitsTableModel2.get(unitnamemodel2);
				
				// If the size of the unit factor sets aren't equal, then the units
				// aren't equivalent
				if(unitfrommodel1baseunits.size()!=unitfrommodel2baseunits.size()) continue;
				
				// If the units on the data structures are both fundamental and
				// don't have the same name, then the units aren't equivalent
				else if(unitfrommodel1baseunits.size()==0){
					if(! unitnamemodel1.equals(unitnamemodel2)) continue;
				}
				// Otherwise we do have some unit factors to compare
				else if(unitfrommodel1baseunits.size()>0){
					
					// Compare the name, prefix and exponent for each base factor
					// If any differences, then the units aren't equivalent
					Set<UnitFactor> baseunitsmatched = new HashSet<UnitFactor>();
					
					for(UnitFactor baseunitfactor1 : unitfrommodel1baseunits){
						for(UnitFactor baseunitfactor2 : unitfrommodel2baseunits){
							if(!baseunitsmatched.contains(baseunitfactor2)){
								
								boolean samename = false;
								
								String unitfactor1name = baseunitfactor1.getBaseUnit().getName();
								String unitfactor2name = baseunitfactor2.getBaseUnit().getName();
								
								// Account for liter/litre and meter/metre synonymy
								boolean unitfactor1inliters = (unitfactor1name.equals("liter") || unitfactor1name.equals("litre"));
								boolean unitfactor1inmeters = (unitfactor1name.equals("meter") || unitfactor1name.equals("metre"));
								boolean unitfactor2inliters = (unitfactor2name.equals("liter") || unitfactor2name.equals("litre"));
								boolean unitfactor2inmeters = (unitfactor2name.equals("meter") || unitfactor2name.equals("metre"));
								
								if((unitfactor1inliters && unitfactor2inliters) || (unitfactor1inmeters && unitfactor2inmeters))
									samename = true;
								else 
									samename = baseunitfactor1.getBaseUnit().getName().equals(baseunitfactor2.getBaseUnit().getName());
								
								boolean sameexponent = false;
								boolean sameprefix = false;

								// Compare exponents
								if(Double.valueOf(baseunitfactor1.getExponent())!=null 
										&& Double.valueOf(baseunitfactor2.getExponent())!=null){

									if(baseunitfactor1.getExponent()==baseunitfactor2.getExponent()){
										sameexponent = true;
									}
								}
								else if(Double.valueOf(baseunitfactor1.getExponent())==null 
										&& Double.valueOf(baseunitfactor2.getExponent())==null){
									sameexponent = true;
								}
								
								// Compare prefixes
								if(baseunitfactor1.getPrefix()!=null && baseunitfactor2.getPrefix()!=null){
									
									if(baseunitfactor1.getPrefix().equals(baseunitfactor2.getPrefix())){
										sameprefix = true;
									}
								}
								else if(baseunitfactor1.getPrefix()==null && baseunitfactor2.getPrefix()==null){
									sameprefix = true;
								}
								
								if(samename && sameexponent && sameprefix){
									baseunitsmatched.add(baseunitfactor2);
									break;
								}
							}
						}
					}
					// If we haven't matched all the unit factors, then units aren't equivalent
					if(baseunitsmatched.size()!=unitfrommodel1baseunits.size()){
						continue;
					}
				} 
				
				// If we are here, then we've found equivalent units
				equivunitslist.put(model1.getUnit(unitnamemodel1), model2.getUnit(unitnamemodel2));
			}
		}
		return equivunitslist;		
	}
	
	
	// Find all the data structures that should be compared. This weeds out 
	// MappableVariables that have an "in" interface. For CellML-type models, the Merger should not 
	// propose mappings between variables with an "in" interface.
	public Set<DataStructure> getComparableDataStructures(SemSimModel model){
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		for(DataStructure ds : model.getAssociatedDataStructures()){
			if(!ds.isFunctionalSubmodelInput()) dsset.add(ds);
		}
		return dsset;
	}
	
	public boolean hasSolutionMapping() {
		return slndomain != null;
	}
}
