package semsim.model.collection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.reading.CellMLreader;
import semsim.writing.CellMLwriter;

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
	
	public FunctionalSubmodel(FunctionalSubmodel fsmtocopy) {
		super(fsmtocopy);
		relationshipSubmodelMap.putAll(fsmtocopy.relationshipSubmodelMap);
		functional = true;
		computation = fsmtocopy.getComputation();
	}
	
	public FunctionalSubmodel(Submodel smtocopy) {
		super(smtocopy);
		functional = true;
		computation = new Computation();
	}
	
	public Map<String, Set<FunctionalSubmodel>> getRelationshipSubmodelMap() {
		return relationshipSubmodelMap;
	}

	public void setRelationshipSubmodelMap(Map<String, Set<FunctionalSubmodel>> relsmmap) {
		relationshipSubmodelMap = relsmmap;
	}

	
	public Computation getComputation() {
		return computation;
	}
	
	public void setComputation(Computation comp) {
		computation = comp;
	}
	
	@Override
	public void removeSubmodel(Submodel sub){
		
		getSubmodels().remove(sub);
		
		// Remove FunctionalSubmodel subsumption assertions (encapsulation, containment, etc.)
		for(String rel : getRelationshipSubmodelMap().keySet()){
			getRelationshipSubmodelMap().get(rel).remove(sub);
		}
	}
	
	// Remove the MathML block for a particular codeword from a functional submodel
	public void removeVariableEquationFromMathML(MappableVariable var){
		
		String componentMathMLstring = getComputation().getMathML();
		String varname = var.getName().replace(getName() + ".", "");

		if(componentMathMLstring!=null){
			List<Content> componentMathML = CellMLwriter.makeXMLContentFromStringForMathML(componentMathMLstring);
			Iterator<Content> compmathmlit = componentMathML.iterator();
			Element varmathmlel = CellMLreader.getElementForOutputVariableFromComponentMathML(varname, compmathmlit);
			if(varmathmlel!=null){
				componentMathML.remove(varmathmlel.detach());
				getComputation().setMathML(new XMLOutputter().outputString(componentMathML));
			}
		}
	}
	
	public FunctionalSubmodel clone() {
		return new FunctionalSubmodel(this);
	}
	
	@Override
	public boolean isFunctional(){
		return true;
	}
}
