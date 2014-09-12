package semsim.model.physical;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import semsim.model.computational.Computation;
import semsim.model.computational.DataStructure;

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
	}
	
	public FunctionalSubmodel(String name, Set<DataStructure> outputs) {
		super(name);
		this.setLocalName(name);
		computation = new Computation(outputs);
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
	}
	
	
	public void setRelationshipSubmodelMap(Map<String, Set<FunctionalSubmodel>> relationshipSubmodelMap) {
		this.relationshipSubmodelMap = relationshipSubmodelMap;
	}

	public Map<String, Set<FunctionalSubmodel>> getRelationshipSubmodelMap() {
		return relationshipSubmodelMap;
	}

	public void setComputation(Computation computation) {
		this.computation = computation;
	}

	public Computation getComputation() {
		return computation;
	}
}
