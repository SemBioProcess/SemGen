package semsim.utilities;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import semsim.SemSimLibrary;
import semsim.definitions.RDFNamespace;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Event;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.writing.CaseInsensitiveComparator;

/**
 * A collection of utility methods for working with SemSim models
 */
public class SemSimUtil {
	
	/**
	 * Replace all occurrences of a codeword in a model's equations with another
	 *  @param discardedds The {@link DataStructure} representing the codeword you want to replace
	 *  @param keptds The {@link DataStructure} representing the replacement codeword
	 *  @param modelfordiscardedds The model containing the discardedds parameter
	 *  @param oldtext The specific text you want to replace
	 *  @param replacementtext The replacement text
	 *  @param conversionfactor The multiplier needed to ensure unit balance between the replaced codeword 
	 *  and the replacement 
	 *  @return Whether the parameter "oldtext" is the name of a data structure that is used to compute its
	 *  own derivative
	 */
	public static Boolean replaceCodewordInAllEquations(DataStructure discardedds, DataStructure keptds, 
			SemSimModel modelfordiscardedds, String oldtext, String replacementtext, Pair<Double, String> conversionfactor){
		
		Boolean selfrefODE = false;
		Set<DataStructure> dsstocheck = new HashSet<DataStructure>();
		dsstocheck.add(discardedds);
		
		if(discardedds.isSolutionDomain()) dsstocheck.addAll(modelfordiscardedds.getAssociatedDataStructures());
		else dsstocheck.addAll(discardedds.getUsedToCompute());

		for(DataStructure dscheck : dsstocheck){
			
			if(discardedds!=keptds && !discardedds.isSolutionDomain()){ // Only reset the inputs if we're merging rather than renaming a DataStructure
				dscheck.getComputation().getInputs().remove(discardedds);
				dscheck.getComputation().addInput(keptds);
			}
			// If the two codeword names are actually different, perform replacements in equations, start value, and mathml
			if( ! oldtext.equals(replacementtext)){
				
				String neweq = dscheck.getComputation().getComputationalCode();
				
				if(dscheck.getComputation().getComputationalCode()!=null){
					neweq = replaceCodewordsInString(dscheck.getComputation().getComputationalCode(), replacementtext, oldtext);
					DataStructure ds = modelfordiscardedds.getAssociatedDataStructure(dscheck.getName());
					ds.getComputation().setComputationalCode(neweq);
				}
	
				// Assume that if discarded cdwd is in an IC for another cdwd, the discarded cdwd is set as an input to the other cdwd
				String newstart = dscheck.getStartValue();
				
				if(dscheck.hasStartValue()){
					newstart = replaceCodewordsInString(dscheck.getStartValue(), replacementtext, oldtext);
					modelfordiscardedds.getAssociatedDataStructure(dscheck.getName()).setStartValue(newstart);
				}
				
				// apply conversion factors in mathml
				if(dscheck.getComputation().getMathML()!=null){
					
					String oldmathml = dscheck.getComputation().getMathML();
					String newmathml = oldmathml;
					String olddsname = oldtext;
					String newdsname = keptds.getName();
					
					if(conversionfactor.getLeft() != 1.0){
						String operator = conversionfactor.getRight().equals("*") ? "<times />" : "<divide />";
						
						newmathml = oldmathml.replace("<ci>" + olddsname + "</ci>", 
							"<apply>" + operator + "<ci>" + newdsname + "</ci>" + 
							"<cn>" + String.valueOf(conversionfactor.getLeft()) + "</cn></apply>");
					}
					else newmathml = oldmathml.replace("<ci>" + olddsname + "</ci>", "<ci>" + newdsname + "</ci>");
					
					modelfordiscardedds.getAssociatedDataStructure(dscheck.getName()).getComputation().setMathML(newmathml);
					
					// If the data structure that needs to have its computations edited is a derivative,
					// Find the state variable and edit its computations, too.
					if(dscheck.getName().contains(":")){
						String statevarname = dscheck.getName().substring(0, dscheck.getName().indexOf(":"));
						
						// If the data structure is used to compute it's own derivative
						if(statevarname.equals(oldtext)){
							selfrefODE = true;
						}
						else{
							modelfordiscardedds.getAssociatedDataStructure(statevarname).getComputation().setComputationalCode(neweq);
							modelfordiscardedds.getAssociatedDataStructure(statevarname).getComputation().setMathML(newmathml);
						}
					}
				}
			}
		}
		return selfrefODE;
	}
	
	/**
	 * Replace all occurrences of a sub-string within a mathematical expression
	 *  @param exp The expression that will be processed for replacement
	 *  @param kept The replacement string
	 *  @param discarded The string to be replaced
	 *  @return A string containing any replacements
	 */
	public static String replaceCodewordsInString(String exp, String kept, String discarded) {	
		if(exp.contains(discarded)){
			// need end of line delimiter so the pattern matches against codewords that end the line
			String eqstring = " " + exp + " ";
			// Match each time the codeword appears (surrounded by non-word characters)
			Pattern p = Pattern.compile("\\W" + discarded + "\\W");
			Matcher m = p.matcher(eqstring);
			StringBuffer sb = new StringBuffer();
			boolean result = m.find();
	
			while (result) {
				String oldsubstring = eqstring.substring(m.start(), m.end());
				String newsubstring = oldsubstring.replace(discarded, kept);
				m.appendReplacement(sb, newsubstring);
				result = m.find();
			}
			m.appendTail(sb);
			return sb.toString().trim();
		}
		else return exp;
	}
	
	/**
	 *  Collect all the variables in a block of MathML that are inputs for the mathematical expression
	 * @param semsimmodel The source SemSim model
	 * @param mathmlstring The MathML string to process for inputs
	 * @param nameprefix Prefix to use for inputs that are local to a submodel
	 * @return
	 */
	public static Set<DataStructure> getComputationalInputsFromMathML(SemSimModel semsimmodel, String mathmlstring, String nameprefix){

		Set<DataStructure> inputs = new HashSet<DataStructure>();

		if(semsimmodel!=null && mathmlstring!=null){
			
			Pattern p = Pattern.compile("<ci>.+</ci>");
			Matcher m = p.matcher(mathmlstring);
			boolean result = m.find();
			
			while(result){
				
				String inputname = mathmlstring.substring(m.start()+4, m.end()-5).trim();
				String inputnameprefixed = nameprefix + "." + inputname;
				String inputnametouse = inputname;
							
				if(! semsimmodel.containsDataStructure(inputname)){
					
					if(! semsimmodel.containsDataStructure(inputnameprefixed)){
						String errmsg = "SEMSIM MODEL ERROR: MathML content refers to " + inputname + " but that variable is not in the model.";
						semsimmodel.addError(errmsg);
						System.err.println(errmsg);
						return new HashSet<DataStructure>();
					}
					else inputnametouse = inputnameprefixed;
				}
				
				DataStructure inputds = semsimmodel.getAssociatedDataStructure(inputnametouse);
				inputs.add(inputds);
				result = m.find();
			}			
		}
		return inputs;
	}
	
	/**
	 * Set the computational inputs for a DataStructure based on its main MathML block and the MathML
	 * associated with any Event that effects the DataStructure's value.
	 * @param semsimmodel The model containing the DataStructure
	 * @param outputds The DataStructure that will have its computational inputs set
	 * @param prefix An optional prefix to use for inputs that are local to a submodel
	 */
	public static void setComputationInputsForDataStructure(SemSimModel semsimmodel, DataStructure outputds, String prefix){
		
		outputds.getComputation().getInputs().clear();
		Set<DataStructure> allinputs = new HashSet<DataStructure>();
		
		// Collect the inputs from the data structure's main mathml block
		String mathmlstring = outputds.getComputation().getMathML();
		for(DataStructure input : SemSimUtil.getComputationalInputsFromMathML(semsimmodel, mathmlstring, prefix)){
			allinputs.add(input);
		}
		
		// set inputs based on the Events that the data structure participates in
		for(Event event : outputds.getComputation().getEvents()){
			
			// collect inputs for trigger
			String triggermathml = event.getTriggerMathML();
			Set<DataStructure> triggerinputs = SemSimUtil.getComputationalInputsFromMathML(semsimmodel, triggermathml, prefix);
			allinputs.addAll(triggerinputs);
			
			// collect inputs from event assignment mathml
			String assignmentmathml = event.getEventAssignmentForOutput(outputds).getMathML();
			Set<DataStructure> assignmentinputs = SemSimUtil.getComputationalInputsFromMathML(semsimmodel, assignmentmathml, prefix);
			allinputs.addAll(assignmentinputs);
		}
		
		// If the DataStructure is a mapped variable, include the mappings
		if(outputds instanceof MappableVariable){
			MappableVariable mv = (MappableVariable)outputds;
			allinputs.addAll(mv.getMappedFrom());
		}
		
		// Assert all the inputs
		for(DataStructure input : allinputs){
			if(! input.equals(outputds)) outputds.getComputation().addInput(input);
		}
	}
	
	/**
	 * Find the right hand side of the equation for a data structure from 
	 * the MathML associated with the data structure's computation.
	 * @param mathmlstring The MathML string for the data structure's computation 
	 * @param solvedvarlocalname The name of the data structure, stripped of all submodel prefixes
	 * @return A MathML string representing the right hand side of the equation used to compute
	 *  the data structure's value
	 */
	public static String getRHSofMathML(String mathmlstring, String solvedvarlocalname){
		SAXBuilder saxbuilder = new SAXBuilder();
		Namespace mmlns = Namespace.getNamespace(RDFNamespace.MATHML.getNamespace());
		
		try {
			Document doc = saxbuilder.build(new StringReader(mathmlstring));
			
			// Get the <eq> element if there is one...
			Boolean hastopeqel = false;
			if(doc.getRootElement().getChild("apply",mmlns)!=null){
				if(doc.getRootElement().getChild("apply",mmlns).getChild("eq", mmlns)!=null){
					hastopeqel = true;
				}
			}
			if(hastopeqel){
				String mathMLhead = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">";
				String mathMLtail = "</math>";
				
				Element eqel = doc.getRootElement().getChild("apply",mmlns).getChild("eq",mmlns);
				Element eqparentel = eqel.getParentElement();
				
				// Iterate over the <eq> element's siblings by getting its parent's children
				Iterator<?> eqparentelit = eqparentel.getChildren().iterator();
	
				while(eqparentelit.hasNext()){
					Element nextel = (Element)eqparentelit.next();
					Boolean iseqel = nextel.getName().equals("eq");
					
					Iterator<?> nexteldescit = nextel.getDescendants(new ElementFilter("diff"));
					
					Boolean isLHS = (nextel.getName().equals("ci") && nextel.getText().equals(solvedvarlocalname))
									|| nexteldescit.hasNext();
	
					// If the element doesn't represent the LHS of the equation, or isn't the <eq> element,
					// then we've found our RHS
					if(! isLHS && ! iseqel){
						XMLOutputter outputter = new XMLOutputter();
						return mathMLhead + "\n" + outputter.outputString(nextel) + "\n" + mathMLtail;
					}
				}
			}
			// Otherwise there's no <eq> element, we assume that the mathml is OK as it exists
			else return mathmlstring;
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}

		// If we're here we haven't found the RHS
		return null;
	}
	
	/**
	 * Write a string to a file
	 *  @param content The string to write out
	 *  @param outputfile The file to which the string will be written
	 */
	public static void writeStringToFile(String content, File outputfile){
		if(content!=null && !content.equals("") && outputfile!=null){
			try {
				PrintWriter pwriter = new PrintWriter(new FileWriter(outputfile));
				pwriter.print(content);
				pwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static CompositePhysicalEntity getEquivalentCompositeEntityIfAlreadyInMap(CompositePhysicalEntity cpe, Map<? extends PhysicalModelComponent, URI> map){		
		if(cpe == null) System.err.println("Next cpe was null");
		// Go through the composite physical entities already processed and see if there is one that
		// matches the cpe argument in terms of the physical entities and structural relations used
		for(PhysicalModelComponent testcomp : map.keySet()){

				// If we've found an equivalent composite entity, return it immediately
				if(cpe.equals(testcomp)){
					return (CompositePhysicalEntity)testcomp;
				}
		}
		// If we've gotten here, then there isn't a match, return the cpe argument
		return cpe;
	}	
	
	/** 
	 * Take collection of DataStructures and return an ArrayList sorted alphabetically
	 * */
	public static ArrayList<DataStructure> alphebetizeSemSimObjects(Collection<DataStructure>  collection) {
		TreeMap<String, DataStructure> dsnamemap = new TreeMap<String, DataStructure>(new CaseInsensitiveComparator());
		for (DataStructure ds : collection) {
			dsnamemap.put(ds.getName(), ds);
		}
		return new ArrayList<DataStructure>(dsnamemap.values());
	}
	
	/** 
	 * Replace all OPB physical properties with their equivalent in the list contained in the SemSimLibrary; therebye 
	 * maintaining a single set of unique property instances
	 * */
	public static void regularizePhysicalProperties(SemSimModel model, SemSimLibrary lib) {
		for (PhysicalPropertyinComposite pp : lib.getCommonProperties()) {
			model.replacePhysicalProperty(pp, pp);
		}
	}
	
	/**
	 * Given a SemSim model, recursively processes custom units and returns all of its units broken down into fundamental base units
	 * @param SemSim model
	 * @return HashMap of customUnit:(baseUnit1:exp1, baseUnit:exp2, ...)
	 */
	public static HashMap<String, Set<UnitFactor>> getAllUnitsAsFundamentalBaseUnits(SemSimModel semsimmodel, String cfgpath) {
		Set<UnitOfMeasurement> units = semsimmodel.getUnits();
		HashMap<String, Set<UnitFactor>> fundamentalBaseUnits = new HashMap<String, Set<UnitFactor>>();
		
		for(UnitOfMeasurement uom : units) {
			Set<UnitFactor> newUnitFactor = recurseBaseUnits(uom, 1.0, cfgpath);
			
			fundamentalBaseUnits.put(uom.getName(), newUnitFactor);
		}
		return fundamentalBaseUnits;
	}
	// Used in tandem with getFundamentalBaseUnits
	public static Set<UnitFactor> recurseBaseUnits(UnitOfMeasurement uom, Double oldExp, String cfgpath) {
		SemSimLibrary semsimlib = new SemSimLibrary(cfgpath);
		Set<UnitFactor> unitFactors = uom.getUnitFactors();
		Set<UnitFactor> newUnitFactors = new HashSet<UnitFactor>();
		for(UnitFactor factor : unitFactors) {
			UnitOfMeasurement baseuom = factor.getBaseUnit();
			double newExp = factor.getExponent()*oldExp;
			String prefix = factor.getPrefix();
			if(semsimlib.isCellMLBaseUnit(baseuom.getName())) {
				UnitFactor baseUnitFactor = new UnitFactor(baseuom, newExp, prefix);
				newUnitFactors.add(baseUnitFactor);
			}
			else {
				newUnitFactors.addAll(recurseBaseUnits(baseuom, newExp, cfgpath));
			}
		}
		return newUnitFactors;
	}
}
