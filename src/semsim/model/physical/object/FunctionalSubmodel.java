package semsim.model.physical.object;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;

/**
 * Class created to represent CellML component constructs.
 * Instances are associated with a Computation that outputs
 * the variables in the component that have a public interface 
 * of "out." 
 * 
 * In accordance with the CellML 1.0 spec, these submodels can
 * have encapsulation, containment, or other user-defined 
 * relationships with other submodels. 
 **/

public class FunctionalSubmodel extends Submodel {
	private Map<String, Set<FunctionalSubmodel>> relationshipSubmodelMap = new HashMap<String, Set<FunctionalSubmodel>>();
	private Computation computation;
	
	public FunctionalSubmodel(String name, DataStructure output) {
		super(name);
		computation = new Computation(output);
		functional = true;
	}
	
	public FunctionalSubmodel(String name, Set<DataStructure> outputs) {
		super(name);
		this.setLocalName(name);
		computation = new Computation(outputs);
		functional = true;
	}
	
	public FunctionalSubmodel(String name, String localName, String referencedName, String hrefValue){
		super(name);
		if(localName!=null){
			setLocalName(localName);
		}
		if(referencedName!=null){
			setReferencedName(referencedName);
		}
		if(hrefValue!=null){
			setHrefValue(hrefValue);
		}
		computation = new Computation();
		functional = true;
	}

	public Map<String, Set<FunctionalSubmodel>> getRelationshipSubmodelMap() {
		return relationshipSubmodelMap;
	}

	public Computation getComputation() {
		return computation;
	}
}
