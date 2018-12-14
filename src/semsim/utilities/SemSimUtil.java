package semsim.utilities;


import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import semsim.SemSimLibrary;
import semsim.definitions.RDFNamespace;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Event;
import semsim.model.computational.SBMLInitialAssignment;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalPropertyInComposite;

/** A collection of utility methods for working with SemSim models */
public class SemSimUtil {

	public static final String mathMLelementStart = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">";
	public static final String mathMLelementEnd = "</math>";
	
	public static enum regexQualifier{GREEDY, RELUCTANT, POSSESSIVE, NONE}; 
	
	
	/**
	 * Removes the FunctionalSubmodel structures within a model. Creates a "flattened"
	 * model where there are no MappableVariables. For merging a model that has a CellML component
	 * structure with one that does not.
	 * @param model The model to flatten
	 * @return Map of the changed data structure names
	 */
	public static Map<String,String> flattenModel(SemSimModel model){
		
		Map<String,String> renamingmap = new HashMap<String,String>();
		
		Set<FunctionalSubmodel> fsset = new HashSet<FunctionalSubmodel>();
		fsset.addAll(model.getFunctionalSubmodels());
		
		// Remove functional submodel structures
		for(FunctionalSubmodel fs : fsset) model.removeSubmodel(fs);
		
		// Remove mapping info on MappableVariables, set eqs. for variables that are inputs
		ArrayList<String> dslist = new ArrayList<String>();
		dslist.addAll(model.getDataStructureNames());
		Collections.sort(dslist, new CaseInsensitiveComparator());
		
		ArrayList<DataStructure> dsListToRemove = new ArrayList<DataStructure>();
				
		for(String dsname : dslist){
			
			DataStructure ds = model.getAssociatedDataStructure(dsname);
			
			if(ds instanceof MappableVariable){	
				
				// TODO: Need to flatten names of variables. Can we get rid of the inputs?
				// How to deal with solution domain resolution?
				MappableVariable mv = (MappableVariable)ds;
								
				if(mv.isFunctionalSubmodelInput()){
										
					MappableVariable sourcemv = mv;
					MappableVariable tempmv = null;
					
					boolean sourcefound = ! sourcemv.isMapped();
					
					// follow mappings until source variable is found
					while( ! sourcefound){		
						
						tempmv = sourcemv;
						sourcemv = tempmv.getMappedFrom();
						if (sourcemv!=null) {
							//Remove the computational dependency between source and input var
							sourcemv.getUsedToCompute().remove(tempmv); // remove mapping link because we are flattening
							
							if( ! sourcemv.isFunctionalSubmodelInput()) sourcefound = true;
						}
					}
					
					String sourcevarglobalname = sourcemv.getName();
					String sourcelocalname = sourcevarglobalname.substring(sourcevarglobalname.lastIndexOf(".")+1, sourcevarglobalname.length());
					
					String mvglobalname = mv.getName();
					String mvlocalname = mvglobalname.substring(mvglobalname.lastIndexOf(".")+1, mvglobalname.length());
					
								
					// Go through all the variables that are computed from the one we're going to remove
					// and set them dependent on the source variable
					for(DataStructure dependentmv : mv.getUsedToCompute()){
						
						if( ! mv.getMappedTo().contains(dependentmv)){ // Only process the mathematical dependencies, not the mappings
							dependentmv.getComputationInputs().remove(mv);
							dependentmv.getComputation().addInput(sourcemv);
						}
						
						// If the local name of the input doesn't match that of its source,
						// replace all occurrences of the input's name in the equations that use it
						if( ! sourcelocalname.equals(mvlocalname)){
							
							String newmathml = replaceCodewordsInString(dependentmv.getComputation().getMathML(), sourcelocalname, mvlocalname);
							dependentmv.getComputation().setMathML(newmathml);
							
							if(!dependentmv.getComputation().getComputationalCode().isEmpty()){
								String newcompcode = replaceCodewordsInString(dependentmv.getComputation().getComputationalCode(), sourcelocalname, mvlocalname);
								dependentmv.getComputation().setComputationalCode(newcompcode);
							}
						}
					}
					
					dsListToRemove.add(mv);
				}
			}
		}
			
		// Remove those data structures that were flagged for removal
		for(DataStructure ds : dsListToRemove) model.removeDataStructure(ds);
		
		// Second pass through new data structures to remove the prefixes on their names and clear mapping info
		for(DataStructure ds : model.getAssociatedDataStructures()){
			String oldname = ds.getName();
			String newname = oldname.substring(oldname.lastIndexOf(".")+1, oldname.length());
			
			// if model already contains a data structure with the new name, use prefix name without "."
			if(model.containsDataStructure(newname)){
				newname = oldname.replace(".", "_");
				replaceCodewordInAllEquations(ds, ds, model, oldname, newname, Pair.of(1.0, "*"));
			}
			
			ds.setName(newname); 
			renamingmap.put(oldname, newname);
			
			if(ds instanceof MappableVariable){
				MappableVariable mv = (MappableVariable)ds;
			
				mv.setPublicInterfaceValue("");
				mv.setPrivateInterfaceValue("");
					
					// Clear mappings
				mv.setMappedFrom(null);
				mv.getMappedTo().clear();
			}
		}
		
		return renamingmap;
	}
	
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
				
				if(!dscheck.getComputation().getComputationalCode().isEmpty()){
					neweq = replaceCodewordsInString(dscheck.getComputation().getComputationalCode(), replacementtext, oldtext);
					//DataStructure ds = modelfordiscardedds.getAssociatedDataStructure(dscheck.getName());					
					dscheck.getComputation().setComputationalCode(neweq);
				}
	
				// Assume that if discarded cdwd is in an IC for another cdwd, the discarded cdwd is set as an input to the other cdwd
				String newstart = dscheck.getStartValue();
				
				if(dscheck.hasStartValue()){
					newstart = replaceCodewordsInString(dscheck.getStartValue(), replacementtext, oldtext);
					dscheck.setStartValue(newstart);
				}
				
				// apply conversion factors in mathml
				if(dscheck.getComputation().getMathML()!=null){
					
					String oldmathml = dscheck.getComputation().getMathML();
					String newmathml = oldmathml;
					String newdsname = keptds.getName();
					
					Pattern p = Pattern.compile("<ci>\\s*" + oldtext + "\\s*</ci>");
					Matcher m = p.matcher(oldmathml);
	
					String replacementmathml = "";
					
					if(conversionfactor.getLeft() == 1.0 || (dscheck.hasStartValue() && keptds.isSolutionDomain()))
						replacementmathml = "<ci>" + newdsname + "</ci>";
					else{
						String operator = conversionfactor.getRight().equals("*") ? "<times />" : "<divide />";
						replacementmathml = "<apply>" + operator + "<ci>" + newdsname + "</ci>" + 
						"<cn>" + String.valueOf(conversionfactor.getLeft()) + "</cn></apply>";
					}
					
					newmathml = m.replaceAll(replacementmathml);

				    dscheck.getComputation().setMathML(newmathml);
				    					
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
	 * Replace all occurrences of a sub-string within a mathematical expression. Uses greedy qualifier.
	 *  @param exp The expression that will be processed for replacement
	 *  @param kept The replacement string
	 *  @param discarded The string to be replaced
	 *  @return A string containing any replacements
	 */
	public static String replaceCodewordsInString(String exp, String kept, String discarded) {
		return replaceCodewordsInString(exp, kept, discarded, regexQualifier.NONE);
	}

	
	/**
	 * Replace all occurrences of a sub-string within a mathematical expression. Overloaded method for specifying regex qualifier type.
	 *  @param exp The expression that will be processed for replacement
	 *  @param kept The replacement string
	 *  @param discarded The string to be replaced
	 *  @param qual The type of regex qualifier to use
	 *  @return A string containing any replacements
	 */
	public static String replaceCodewordsInString(String exp, String kept, String discarded, regexQualifier qual) {	
		if(exp.contains(discarded)){
			
			String qualstring = "";

			if(qual==regexQualifier.NONE){}
			else if(qual == regexQualifier.GREEDY) qualstring = "?";
			else if(qual == regexQualifier.RELUCTANT) qualstring = "??";
			else if(qual == regexQualifier.POSSESSIVE) qualstring = "?+";
			else{}
			
			// need end of line delimiter so the pattern matches against codewords that end the line
			String eqstring = " " + exp + " ";
			// Match each time the codeword appears (surrounded by non-word characters)
			
			Pattern p = Pattern.compile("\\W" + discarded + "\\W" + qualstring); 
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
		return exp;
	}
	
	/**
	 *  Collect all the variables in a block of MathML that are inputs for the mathematical expression
	 * @param semsimmodel The source SemSim model
	 * @param mathmlstring The MathML string to process for inputs
	 * @param nameprefixanddelimiter Optional prefix/delimiter pair to use for inputs that are local to a submodel
	 * @return All the variables in a block of MathML that are inputs for the MathML expression
	 */
	public static Set<DataStructure> getComputationalInputsFromMathML(SemSimModel semsimmodel, String mathmlstring, Pair<String,String> nameprefixanddelimiter){

		Set<DataStructure> inputs = new HashSet<DataStructure>();

		if(semsimmodel!=null && mathmlstring!=null){
			
			Map<String,String> inputnames = getInputNamesFromMathML(mathmlstring, nameprefixanddelimiter);
			
			// Go through all the input names we found, make sure they are in the model
			// and if so, add them to the returned list of inputs
			for(String inputname : inputnames.keySet()){
				
				String theinputnametouse = inputname;
				
				boolean foundinput = false;
				boolean modelcontainsunprefixedname = semsimmodel.containsDataStructure(inputname);
				
				if(nameprefixanddelimiter==null){
					foundinput = modelcontainsunprefixedname;
				}
				else{
					String prefixedinputname = inputnames.get(inputname);
					
					if(semsimmodel.containsDataStructure(prefixedinputname)){
						foundinput = true;
						theinputnametouse = prefixedinputname;
					}
					else if(modelcontainsunprefixedname){
						foundinput = true;
					}
					else{
						String errmsg = "SEMSIM ERROR: MathML content refers to " + inputname + " but that variable is not in the model.";
						semsimmodel.addError(errmsg);
						System.err.println(errmsg);
						return new HashSet<DataStructure>();
					}

				}
				
				if(foundinput){
					DataStructure inputds = semsimmodel.getAssociatedDataStructure(theinputnametouse);
					inputs.add(inputds);
				}
			}
		}
		return inputs;
	}
	
	
	/**
	 * Get names of terms used in a MathML string
	 * @param mathmlstring A MathML string
	 * @param nameprefixanddelimiter Optional prefix/delimiter pair for mapping an input name to a SemSim-formatted name
	 * @return Names of inputs used in MathML mapped to option prefixed names
	 */
	public static Map<String,String> getInputNamesFromMathML(String mathmlstring, Pair<String,String> nameprefixanddelimiter){
		Map<String,String> inputnames = new HashMap<String,String>();
		
		String prefix = nameprefixanddelimiter==null ? "" : nameprefixanddelimiter.getLeft() + nameprefixanddelimiter.getRight();
		Pattern p1 = Pattern.compile("<ci>.+</ci>");
		Matcher m1 = p1.matcher(mathmlstring);
		boolean result1 = m1.find();
		
		while(result1){
			String inputname = mathmlstring.substring(m1.start()+4, m1.end()-5).trim();
			inputnames.put(inputname, prefix + inputname);
			result1 = m1.find();
		}	
		
		// Look for instances where csymbols are used for controlled symbols
		// as in SBML's use of the 't' symbol for time
		Pattern p2 = Pattern.compile("<csymbol.+</csymbol>");
		Matcher m2 = p2.matcher(mathmlstring);
		boolean result2 = m2.find();
		
		while(result2){
			String matchedstring = mathmlstring.substring(m2.start(), m2.end());
			
			// Store csymbol text as an input only if it's not the "delay" csymbol used in SBML
			if( ! matchedstring.contains("definitionURL=\"http://www.sbml.org/sbml/symbols/delay\"")){
				String inputname = matchedstring.substring(matchedstring.indexOf(">")+1, matchedstring.lastIndexOf("<")-1).trim();
				inputnames.put(inputname, prefix + inputname);
			}
			
			result2 = m2.find();
		}
		return inputnames;
	}
	
	/**
	 * Set the computational inputs for a DataStructure based on its main MathML block and the MathML
	 * associated with any Event that effects the DataStructure's value.
	 * @param semsimmodel The model containing the DataStructure
	 * @param outputds The DataStructure that will have its computational inputs set
	 * @param prefixanddelimiter An optional prefix and delimiter pair to use for inputs that are local
	 *  to a submodel or function definition, etc.
	 */
	public static void setComputationInputsForDataStructure(SemSimModel semsimmodel, DataStructure outputds, Pair<String,String> prefixanddelimiter){
		
		outputds.getComputation().getInputs().clear();
		Set<DataStructure> allinputs = new HashSet<DataStructure>();
		
		// Collect the inputs from the data structure's main mathml block
		String mathmlstring = outputds.getComputation().getMathML();
		for(DataStructure input : SemSimUtil.getComputationalInputsFromMathML(semsimmodel, mathmlstring, prefixanddelimiter)){
			allinputs.add(input);
		}
		
		// set inputs based on the Events that the data structure participates in
		for(Event event : outputds.getComputation().getEvents()){
			
			// collect inputs for trigger
			String triggermathml = event.getTriggerMathML();
			Set<DataStructure> triggerinputs = SemSimUtil.getComputationalInputsFromMathML(semsimmodel, triggermathml, prefixanddelimiter);
			allinputs.addAll(triggerinputs);
			
			// collect inputs from event assignment mathml
			String assignmentmathml = event.getEventAssignmentForOutput(outputds).getMathML();
			Set<DataStructure> assignmentinputs = SemSimUtil.getComputationalInputsFromMathML(semsimmodel, assignmentmathml, prefixanddelimiter);
			allinputs.addAll(assignmentinputs);
		}
		
		// set inputs based on the SBML initial assignments that set the data structure's values
		for(SBMLInitialAssignment sia : outputds.getComputation().getSBMLintialAssignments()){
			String assignmentmathml = sia.getMathML();
			Set<DataStructure> inputs = SemSimUtil.getComputationalInputsFromMathML(semsimmodel, assignmentmathml, prefixanddelimiter);
			allinputs.addAll(inputs);
		}
		
		// If the DataStructure is a mapped variable, include the mappings
		if(outputds instanceof MappableVariable){
			MappableVariable mv = (MappableVariable)outputds;
			if (mv.getMappedFrom()!=null) {
				allinputs.add(mv.getMappedFrom());
			}
		}
		
		// Assert all the inputs
		for(DataStructure input : allinputs){
			if(! input.equals(outputds)) outputds.getComputation().addInput(input);
		}
	}
	
	
	/**
	 * Add the left-hand side of a MathML equation
	 * @param mathmlstring The right-hand side of a MathML equation
	 * @param varname The name of the solved variable
	 * @param isODE Whether the variable is solved with an ODE
	 * @param timedomainname Name of time domain to use in MathML
	 * @return The MathML equation containing both the left- and right-hand side
	 */
	public static String addLHStoMathML(String mathmlstring, String varname, boolean isODE, String timedomainname){
		String LHSstart = null;
		if(isODE) 
			LHSstart = makeLHSforStateVariable(varname, timedomainname);
		else LHSstart = " <apply>\n  <eq />\n  <ci>" + varname + "  </ci>\n";
		String LHSend = "</apply>\n";
		mathmlstring = mathmlstring.replaceFirst(mathMLelementStart, mathMLelementStart + "\n" + LHSstart);
		mathmlstring = mathmlstring.replace(mathMLelementEnd, LHSend + mathMLelementEnd);
		return mathmlstring;
	}
	
	/**
	 * Create the MathML left-hand side for a variable that is solved using an ODE
	 * @param varname Name of the variable
	 * @param timedomainname Name of the time domain to use when formulating the MathML
	 * @return Left-hand side of the MathML used for a variable that is solved using an ODE
	 */
	public static String makeLHSforStateVariable(String varname, String timedomainname){
		return " <apply>\n <eq/>\n  <apply>\n  <diff/>\n   <bvar>\n    <ci>" 
				+ timedomainname + "</ci>\n   </bvar>\n   <ci>" + varname + "</ci>\n  </apply>\n  ";
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
		
		try {
			if(mathmlHasLHS(mathmlstring)){
				
				SAXBuilder saxbuilder = new SAXBuilder();
				Namespace mmlns = Namespace.getNamespace(RDFNamespace.MATHML.getNamespaceAsString());
				Document doc = saxbuilder.build(new StringReader(mathmlstring));
				
				Element eqel = doc.getRootElement().getChild("apply",mmlns).getChild("eq",mmlns);
				Element eqparentel = eqel.getParentElement();
				
				// Iterate over the <eq> element's siblings by getting its parent's children
				Iterator<?> eqparentelit = eqparentel.getChildren().iterator();
	
				while(eqparentelit.hasNext()){
					Element nextel = (Element)eqparentelit.next();
					boolean iseqel = nextel.getName().equals("eq");
					boolean isLHSdifferential = false;
					
					// Check if the element is a LHS differential term 
					Iterator<?> nexteldescit = nextel.getDescendants(new ElementFilter("diff"));
					if(nexteldescit.hasNext()) {
						Element nextdiffel = (Element)nexteldescit.next();
						String diffnumerator = getNumeratorOfDiffEq(nextdiffel.getParentElement());
						isLHSdifferential =  (diffnumerator.equals(solvedvarlocalname));
					}
					
					Boolean isLHS = (nextel.getName().equals("ci") && nextel.getText().trim().equals(solvedvarlocalname))
									|| isLHSdifferential; // this line used to be just nexteldescit.hasNext();
					
					// If the element doesn't represent the LHS of the equation, or isn't the <eq> element,
					// then we've found our RHS
					if(! isLHS && ! iseqel){
						
						XMLOutputter outputter = new XMLOutputter();
						outputter.setFormat(Format.getPrettyFormat());
						Element newtopel = new Element("math");
						newtopel.setNamespace(Namespace.getNamespace(RDFNamespace.MATHML.getNamespaceAsString()));
						newtopel.addContent(nextel.detach());
						return outputter.outputString(newtopel);
					}
				}
			}
			// Otherwise there's no <eq> element, we assume that the mathml is the RHS
			else return mathmlstring;
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}

		// If we're here we haven't found the RHS
		return "";
	}
	
	
	/**
	 * @param mathml Some MathML as a String
	 * @return Whether the MathML includes a left-hand side
	 */
	public static boolean mathmlHasLHS(String mathml){
		SAXBuilder saxbuilder = new SAXBuilder();
		Document doc;
		try {
			doc = saxbuilder.build(new StringReader(mathml));
			Namespace mmlns = Namespace.getNamespace(RDFNamespace.MATHML.getNamespaceAsString());

			// Get the <eq> element if there is one...
			if(doc.getRootElement().getChild("apply",mmlns)!=null){
				if(doc.getRootElement().getChild("apply",mmlns).getChild("eq", mmlns)!=null){
					return true;
				}
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * @param parentofdiffelement The immediate parent element of a <diff/> element
	 * @return The numerator variable of a differential expression (e.g. the "P" in dP/dt)
	 */
	private static String getNumeratorOfDiffEq(Element parentofdiffelement){
		@SuppressWarnings("unchecked")
		List<Element> diffsiblinglist = parentofdiffelement.getChildren("ci",Namespace.getNamespace(RDFNamespace.MATHML.getNamespaceAsString()));
		
		if(diffsiblinglist.size() != 1) return "";
		else return diffsiblinglist.get(0).getText().trim();
	}
	

	/**
	 * Determine if a given {@link CompositePhysicalEntity} is in the keyset of an input
	 * map that relates {@link PhysicalModelComponent}s to associated URIs
	 * @param cpe An input {@link CompositePhysicalEntity} 
	 * @param map A Map that relates {@link PhysicalModelComponent}s to associated URIs
	 * @return The Map key that is equivalent to the input {@link CompositePhysicalEntity}, if present,
	 * otherwise the input {@link CompositePhysicalEntity}
	 */
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
	
	
	/** Take collection of DataStructures and return an ArrayList sorted alphabetically 
	 * @param collection A set of data structures
	 * @return Alphabetized list of data structures based on their names
	 */
	public static ArrayList<DataStructure> alphebetizeSemSimObjects(Collection<DataStructure>  collection) {
		TreeMap<String, DataStructure> dsnamemap = new TreeMap<String, DataStructure>(new CaseInsensitiveComparator());
		for (DataStructure ds : collection) {
			dsnamemap.put(ds.getName(), ds);
		}
		return new ArrayList<DataStructure>(dsnamemap.values());
	}
	
	
	/** 
	 * Replace all OPB physical properties with their equivalent in the list contained in the SemSimLibrary; thereby 
	 * maintaining a single set of unique property instances
	 * @param model The model containing the properties to regularize
	 * @param lib A SemSimLibrary instance
	 * */
	public static void regularizePhysicalProperties(SemSimModel model, SemSimLibrary lib) {
		for (PhysicalPropertyInComposite pp : lib.getCommonProperties()) {
			model.replacePhysicalProperty(pp, pp);
		}
	}
	
	/**
	 * Given a SemSim model, recursively processes custom units and returns all of its units broken down into fundamental base units
	 * @param semsimmodel A SemSim model
	 * @param cfgpath Path to configuration folder
	 * @return HashMap of customUnit:(baseUnit1:exp1, baseUnit:exp2, ...)
	 */
	public static HashMap<String, Set<UnitFactor>> getAllUnitsAsFundamentalBaseUnits(SemSimModel semsimmodel, String cfgpath) {
		Set<UnitOfMeasurement> units = semsimmodel.getUnits();
		HashMap<String, Set<UnitFactor>> fundamentalBaseUnits = new HashMap<String, Set<UnitFactor>>();
		SemSimLibrary sslib = new SemSimLibrary(cfgpath);
		
		for(UnitOfMeasurement uom : units) {
			Set<UnitFactor> newUnitFactor = recurseBaseUnits(uom, 1.0, sslib);
			
			fundamentalBaseUnits.put(uom.getName(), newUnitFactor);
		}
		return fundamentalBaseUnits;
	}
	
	/**
	 * Used in tandem with method "getFundamentalBaseUnits" to decompose a unit into its fundamental elements
	 * @param uom Unit to decompose
	 * @param oldExp Exponent applied to the unit from its use as a unit factor in another "parent" unit
	 * @param sslib A SemSimLibrary instance
	 * @return Fundamental set of {@link UnitFactor}s comprising the unit
	 */
	// TODO: should add a parameter here that allows the user to choose whether they want to decompose into
	// CellML OR SBML base units
	// TODO: This should also be rewritten so that for units with more than one
	// unit factor, the multipliers on all factors for the units should be multiplied first,
	// then compared. Otherwise, equivalent units might get flagged as unequivalent.
	// Currently, multipliers are compared for each unit factor.
	public static Set<UnitFactor> recurseBaseUnits(UnitOfMeasurement uom, Double oldExp, SemSimLibrary sslib) {

		Set<UnitFactor> unitFactors = uom.getUnitFactors();
		Set<UnitFactor> newUnitFactors = new HashSet<UnitFactor>();
		
		for(UnitFactor factor : unitFactors) {
						
			UnitOfMeasurement baseuom = factor.getBaseUnit();
			
			double newExp = factor.getExponent()*oldExp;
			
			String prefix = factor.getPrefix();
			double multiplier = factor.getMultiplier();
			
			
			if(sslib.isCellMLBaseUnit(baseuom.getName())) {
				UnitFactor baseUnitFactor = new UnitFactor(baseuom, newExp, prefix, multiplier);
				newUnitFactors.add(baseUnitFactor);
			}
			
			else newUnitFactors.addAll(recurseBaseUnits(baseuom, newExp, sslib));
		}
		return newUnitFactors;
	}
	
	/**
	 * Create map of unit names that needed to be changed (i.e. given a valid name)
	 * when writing out a CellML or SBML model
	 * @param semsimmodel The SemSimModel that was written out
	 * @param map An old-to-new name map that will be appended by the method
	 * @return An old-to-new name map that includes mappings between old and new unit names
	 */
	public static Map<String,String> createUnitNameMap(SemSimModel semsimmodel,Map<String,String> map){
		
		for(UnitOfMeasurement uom : semsimmodel.getUnits()){
			String oldname = uom.getName();
			map.put(oldname, SemSimUtil.makeValidUnitNameForCellMLorSBML(oldname));
		}
		return map;
		
	}
	
	/**
	 * Create a CellML- and SBML-friendly unit name from an input name
	 * @param oldname An input unit name
	 * @return A CellML- and SBML-friendly unit name
	 */
	public static String makeValidUnitNameForCellMLorSBML(String oldname){
		String newname = oldname;
		newname = newname.replaceAll("\\s", "_");
		newname = newname.replace("/", "_per_");
		newname = newname.replace("*", "_times_");
		newname = newname.replace("^", "_exp_");
		newname = newname.replace("(", "_");
		newname = newname.replace(")", "_");
		newname = newname.replace("-", "minus");

				
		if(Character.isDigit(newname.charAt(0))) newname = "x" + newname;
		
		return newname;
	}
}
