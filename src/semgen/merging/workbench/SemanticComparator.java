package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;

public class SemanticComparator {
	private SemSimModel model1, model2;

	public SemanticComparator(SemSimModel m1, SemSimModel m2) {
		model1 = m1; model2 = m2;
	}
	
	public Set<String> identifyIdenticalCodewords() {
		Set<String> matchedcdwds = new HashSet<String>();
		String slndomainname = null; 
		for (DataStructure ds : model1.getDataStructures()) {
			if (model2.containsDataStructure(ds.getName()))
				matchedcdwds.add(ds.getName());
			if (ds.isSolutionDomain()) {
				slndomainname = ds.getName();
			}
		}
		if (slndomainname != null) {
			matchedcdwds.remove(slndomainname);
			matchedcdwds.remove(slndomainname + ".min");
			matchedcdwds.remove(slndomainname + ".max");
			matchedcdwds.remove(slndomainname + ".delta");
		}
		return matchedcdwds;
	}
	
	public ArrayList<Pair<DataStructure, DataStructure>> identifyExactSemanticOverlap() {
		ArrayList<Pair<DataStructure, DataStructure>> dsmatchlist = new ArrayList<Pair<DataStructure, DataStructure>>();
		// Only include the annotated data structures in the resolution process
		for(DataStructure ds1 : model1.getDataStructures()){
			for(DataStructure ds2 : model2.getDataStructures()){
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
		} // end of iteration through model1 data structures
		return dsmatchlist;
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
