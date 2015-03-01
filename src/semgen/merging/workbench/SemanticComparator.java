package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;

public class SemanticComparator {
	private SemSimModel model1, model2;
	private DataStructure slndomain;

	public SemanticComparator(SemSimModel m1, SemSimModel m2) {
		model1 = m1; model2 = m2;
		
		if(model1.getSolutionDomains().size() > 0)
			slndomain = model1.getSolutionDomains().toArray(new DataStructure[]{})[0];
	}
	
	public Set<String> identifyIdenticalCodewords() {
		Set<String> matchedcdwds = new HashSet<String>();
		
		for (DataStructure ds : model1.getDataStructures()) {
			if (model2.containsDataStructure(ds.getName()))	matchedcdwds.add(ds.getName());
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
		
		DataStructure soldom2 = null;
		if(model2.getSolutionDomains().size() > 0)
			soldom2 = model2.getSolutionDomains().toArray(new DataStructure[]{})[0];
		
		dsmatchlist.add(Pair.of(slndomain, soldom2));
		
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
						match = testNonCompositeAnnotations(ds1.getFirstRefersToReferenceOntologyAnnotation(),
								ds2.getFirstRefersToReferenceOntologyAnnotation());
					}
					
					// If the physical properties are not null
					if(!match && ds1.getPhysicalProperty()!=null && ds2.getPhysicalProperty()!=null){
						// And they are properties of a specified physical model component
						if(ds1.getPhysicalProperty().getPhysicalPropertyOf()!=null && ds2.getPhysicalProperty().getPhysicalPropertyOf()!=null){
							PhysicalProperty prop1 = ds1.getPhysicalProperty();
							PhysicalProperty prop2 = ds2.getPhysicalProperty();
							
							// and they are annotated against reference ontologies
							if(prop1.hasRefersToAnnotation() && prop2.hasRefersToAnnotation()){
								// and the annotations match
								if(prop1.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString().equals(prop2.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){
									
									// and they are properties of the same kind of physical model component
									if(prop1.getPhysicalPropertyOf().getClass() == prop2.getPhysicalPropertyOf().getClass()){
										
										// if they are properties of a composite physical entity
										if(prop1.getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
											CompositePhysicalEntity cpe1 = (CompositePhysicalEntity)prop1.getPhysicalPropertyOf();
											CompositePhysicalEntity cpe2 = (CompositePhysicalEntity)prop2.getPhysicalPropertyOf();
											match = testCompositePhysicalEntityEquivalency(cpe1, cpe2);
										}
										// if they are properties of a physical process or singular physical entity
										else{
											// and if they are both annotated against reference ontology terms
											if(prop1.getPhysicalPropertyOf().hasRefersToAnnotation() && prop2.getPhysicalPropertyOf().hasRefersToAnnotation()){
												// and if the annotations match
												if(prop1.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString().equals(
														prop2.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){
													match = true;
												}
											}
										}
									}
								}
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
	
	// Find all the data structures that should be compared. This weeds out 
	// MappableVariables that have an "in" interface. For CellML-type models, the Merger should not 
	// propose mappings between variables with an "in" interface.
	public Set<DataStructure> getComparableDataStructures(SemSimModel model){
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		for(DataStructure ds : model.getDataStructures()){
			if(ds instanceof MappableVariable){
				MappableVariable mappedds = (MappableVariable)ds;
				
				// If the mapped variable has an "in" interface value, don't add to the set of comparable DataStructures
				if(mappedds.getPublicInterfaceValue()!=null){
					if (mappedds.getPublicInterfaceValue().equals("in"))
						continue;
				}
				if(mappedds.getPrivateInterfaceValue()!=null){	
					if(mappedds.getPrivateInterfaceValue().equals("in"))
						continue;
				}
			}
			dsset.add(ds);
		}
		return dsset;
	}
	
	
	public Boolean testNonCompositeAnnotations(ReferenceOntologyAnnotation ann1, ReferenceOntologyAnnotation ann2){
		return (ann1.getReferenceURI().toString().equals(ann2.getReferenceURI().toString()));
	}
	
	public Boolean testCompositePhysicalEntityEquivalency(CompositePhysicalEntity cpe1, CompositePhysicalEntity cpe2){
		if(cpe1.getArrayListOfEntities().size()!=cpe2.getArrayListOfEntities().size())
			return false;
		for(int i=0; i<cpe1.getArrayListOfEntities().size(); i++){
			if(cpe1.getArrayListOfEntities().get(i).hasRefersToAnnotation() && cpe2.getArrayListOfEntities().get(i).hasRefersToAnnotation()){
				if(!cpe1.getArrayListOfEntities().get(i).getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString().equals( 
					cpe2.getArrayListOfEntities().get(i).getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){

					return false;
				}
			}
			else return false;
		}
		return true;
	}
	
}
