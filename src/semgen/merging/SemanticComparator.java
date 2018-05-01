package semgen.merging;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import semgen.SemGen;
import semgen.merging.ModelOverlapMap.MapType;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.DuplicateChecker;
import semsim.utilities.SemSimUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SemanticComparator {
	public SemSimModel model1, model2;
	private DataStructure slndomain = null;

	public SemanticComparator(SemSimModel m1, SemSimModel m2) {
		model1 = m1; model2 = m2;
		DuplicateChecker.removeDuplicatePhysicalEntities(model1, model2);
		
		if ((model1.getSolutionDomains().size() > 0) && (model2.getSolutionDomains().size() > 0))
			slndomain = model1.getSolutionDomains().toArray(new DataStructure[]{})[0];
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
		
		// TODO: If we're merging a CellML model and a non-CellML model, do we need to flag instances where there
		// is a codeword X in the non-CellML model and component.X in the CellML one?
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
	
	public ArrayList<SemanticOverlap> identifySemanticOverlap() {
		
		ArrayList<SemanticOverlap> dsmatchlist = new ArrayList<SemanticOverlap>();
		
		if(slndomain != null){
			DataStructure soldom2 = model2.getSolutionDomains().toArray(new DataStructure[]{})[0];
			dsmatchlist.add(new SemanticOverlap(slndomain, soldom2, MapType.AUTO_MAPPING));
		}
		
		Set<DataStructure> model1ds = getComparableDataStructures(model1);
		Set<DataStructure> model2ds = getComparableDataStructures(model2);
		
		// For each comparable data structure in model 1...
		for(DataStructure ds1 : model1ds){
			
			// Ignore solution domains
			if (ds1 == slndomain) continue;
				
			// For each comparable data structure in model 2
			for(DataStructure ds2 : model2ds){
				
				// Test singular annotations
				if(ds1.hasPhysicalDefinitionAnnotation() && ds2.hasPhysicalDefinitionAnnotation()){
					
					if(ds1.getSingularTerm().equals(ds2.getSingularTerm())){
						dsmatchlist.add(new SemanticOverlap(ds1,ds2,MapType.SEMANTICALLY_EXACT));
						continue; // If the singular annotations match, don't bother with the composite annotation
					}
				}
			
				// If the physical properties in the composite annotation are not null...
				if(ds1.hasPhysicalProperty() && ds2.hasPhysicalProperty()){
					PhysicalPropertyInComposite prop1 = ds1.getPhysicalProperty();
					PhysicalPropertyInComposite prop2 = ds2.getPhysicalProperty();
						
					// Test whether the associated physical components are equivalent
					if(ds1.hasAssociatedPhysicalComponent() && ds2.hasAssociatedPhysicalComponent()){
						PhysicalModelComponent ds1pmc = ds1.getAssociatedPhysicalModelComponent();
						PhysicalModelComponent ds2pmc = ds2.getAssociatedPhysicalModelComponent();
						
						// If the associated physical components are equivalent...
						if(ds1pmc.equals(ds2pmc)){
							
							// If the properties are equivalent, add mapping...
							if(prop1.equals(prop2))
								dsmatchlist.add(new SemanticOverlap(ds1,ds2,MapType.SEMANTICALLY_EXACT));
							
							// Otherwise see if they are similar. For now just see if they are OPB siblings.
							else{
								OWLClass cls1 = SemSimOWLFactory.factory.getOWLClass(IRI.create(prop1.getPhysicalDefinitionURI()));
								OWLClass cls2 = SemSimOWLFactory.factory.getOWLClass(IRI.create(prop2.getPhysicalDefinitionURI()));
								Set<OWLClassExpression> super1 = cls1.getSuperClasses(SemGen.semsimlib.OPB);
								Set<OWLClassExpression> super2 = cls2.getSuperClasses(SemGen.semsimlib.OPB);
																	
								super1.retainAll(super2);
								
								// If the properties are siblings, add mapping.
								if(super1.size()>0)
									dsmatchlist.add(new SemanticOverlap(ds1, ds2, MapType.SEMANTICALLY_SIMILAR));
							}
						}					
					}
				}					
			} // end of iteration through model2 data structures
		} // end of iteration through model1 data structures
		return dsmatchlist;
	}
	
	
	// Identify semantically-equivalent units by decomposing units in both models
	// into their base units
	public HashMap<UnitOfMeasurement, UnitOfMeasurement> identifyEquivalentUnits(){
		
		HashMap<UnitOfMeasurement, UnitOfMeasurement> equivunitslist = 
				new HashMap<UnitOfMeasurement, UnitOfMeasurement>();
				
		HashMap<String, Set<UnitFactor>> baseUnitsTableModel1 = SemSimUtil.getAllUnitsAsFundamentalBaseUnits(model1, SemGen.cfgreadpath);
		HashMap<String, Set<UnitFactor>> baseUnitsTableModel2 = SemSimUtil.getAllUnitsAsFundamentalBaseUnits(model2, SemGen.cfgreadpath);
		
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
								boolean samemultiplier = false;

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
								
								// Compare multipliers
								if(Double.valueOf(baseunitfactor1.getMultiplier())!=null 
										&& Double.valueOf(baseunitfactor2.getMultiplier())!=null){
									
									if(baseunitfactor1.getMultiplier()==baseunitfactor2.getMultiplier()){
										samemultiplier = true;
									}
								}
								else if(Double.valueOf(baseunitfactor1.getMultiplier())==null 
										&& Double.valueOf(baseunitfactor2.getMultiplier())==null){
									samemultiplier = true;
								}
								
								// If everything matches, then we've got equivalent unit factors
								if(samename && sameexponent && sameprefix && samemultiplier){
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
	
	public class SemanticOverlap {
		Pair<DataStructure,DataStructure> pair;
		MapType mappingtype;
		
		public SemanticOverlap(DataStructure left, DataStructure right, MapType mappingtype){
			this.pair = Pair.of(left, right);
			this.mappingtype = mappingtype;
		}
		
		public MapType getMappingType(){
			return mappingtype;
		}
		
		public Pair<DataStructure,DataStructure> getMappedPair(){
			return pair;
		}
	}
}