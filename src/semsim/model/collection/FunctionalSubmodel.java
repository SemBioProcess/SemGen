package semsim.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * Class created to represent CellML components.
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
	
	/**
	 * Constructor for FunctionalSubmodel with one output
	 * @param name The name of the FunctionalSubmodel
	 * @param output The output
	 */
	public FunctionalSubmodel(String name, DataStructure output) {
		super(name);
		computation = new Computation(output);
		functional = true;
	}
	
	/**
	 * Constructor for FunctionalSubmodel with a specified set of outputs
	 * @param name The name of the FunctionalSubmodel
	 * @param outputs The outputs
	 */
	public FunctionalSubmodel(String name, Set<DataStructure> outputs) {
		super(name);
		this.setLocalName(name);
		computation = new Computation(outputs);
		functional = true;
	}
	
	/**
	 * Constructor for FunctionalSubmodel with a specified ArrayList of associated {@link DataStructure}s
	 * @param name The name of the FunctionalSubmodel
	 * @param dscollection The ArrayList of associated {@link DataStructure}s
	 */
	public FunctionalSubmodel(String name, ArrayList<DataStructure> dscollection){ 
		super(name, dscollection);
	}
	
	/**
	 * Constructor for imported FunctionalSubmodels
	 * @param name Name of the FunctionalSubmodel
	 * @param localName The locally-used name of the FunctionalSubmodel in the model
	 * @param referencedName The reference name of the FunctionalSubmodel in the model from which it is imported
	 * @param hrefValue The href value used for referencing the imported FunctionalSubmodel
	 */
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
	
	/**
	 * Copy constructor
	 * @param fsmtocopy The FunctionalSubmodel to copy
	 */
	public FunctionalSubmodel(FunctionalSubmodel fsmtocopy) {
		super(fsmtocopy);
		relationshipSubmodelMap.putAll(fsmtocopy.relationshipSubmodelMap);
		functional = true;
		computation = new Computation(fsmtocopy.getComputation());
	}
	
	/**
	 * Copy constructor for more general {@link Submodel} class
	 * @param smtocopy The {@link Submodel} to copy
	 */
	public FunctionalSubmodel(Submodel smtocopy) {
		super(smtocopy);
		functional = true;
		computation = new Computation();
	}
	
	/** @return The Map that stores the relationships that the FunctionalSubmodel
	 * has with other FunctionalSubmodels in the model (containment, encapsulation, etc.)
	 */
	public Map<String, Set<FunctionalSubmodel>> getRelationshipSubmodelMap() {
		return relationshipSubmodelMap;
	}

	/**
	 * Set the Map that stores the relationships that the FunctionalSubmodel
	 * has with other FunctionalSubmodels in the model (containment, encapsulation, etc.)
	 * @param relsmmap The Map
	 */
	public void setRelationshipSubmodelMap(Map<String, Set<FunctionalSubmodel>> relsmmap) {
		relationshipSubmodelMap = relsmmap;
	}

	/** @return The {@link Computation} associated with a FunctionalSubmodel */
	public Computation getComputation() {
		return computation;
	}
	
	/**
	 * Set the {@link Computation} associated with a FunctionalSubmodel
	 * @param comp The {@link Computation}
	 */
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
	
	/**
	 * Remove the equation that solves for a variable from the FunctionalModel's MathML
	 * @param var The variable whose equation is to be removed
	 */
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
	
	/** Replace {@link DataStructure}s in the FunctionalModel with other {@link DataStructure}s*/
	public void replaceDataStructures(HashMap<DataStructure, DataStructure> dsmap) {
		super.replaceDataStructures(dsmap);
		
		computation.replaceAllDataStructures(dsmap);
	}
	
	/** Replace {@link Submodel}s contained in the FunctionalSubmodel */
	public void replaceSubmodels(HashMap<Submodel, Submodel> smmap) {
		super.replaceSubmodels(smmap);
		
		Map<String, Set<FunctionalSubmodel>> relsmmap = new HashMap<String, Set<FunctionalSubmodel>>();
		for (String rel : relationshipSubmodelMap.keySet()) {
			Set<FunctionalSubmodel> rsmset = new HashSet<FunctionalSubmodel>();
			for (FunctionalSubmodel rfsm : relationshipSubmodelMap.get(rel)) {
				rsmset.add((FunctionalSubmodel) smmap.get(rfsm));
			}
			relsmmap.put(new String(rel), rsmset);
		}
		setRelationshipSubmodelMap(relsmmap);
	}
	
	/** Replace a single {@link DataStructure} in the FunctionalSubmodel */
	public void replaceDataStructure(DataStructure replacee, DataStructure replacer) {
		super.replaceDataStructure(replacee, replacer);
		
		if (computation.getOutputs().contains(replacee)) {
			computation.getOutputs().remove(replacee);
			computation.addOutput(replacer);
		}
		if (computation.getInputs().contains(replacee)) {
			computation.getInputs().remove(replacee);
			computation.addInput(replacer);
		}
	}
	
	/** Clone the object. (To create a copy, probably better to use the copy constructor.) */
	public FunctionalSubmodel clone() {
		return new FunctionalSubmodel(this);
	}
	
	@Override
	public boolean isFunctional(){
		return true;
	}
}
