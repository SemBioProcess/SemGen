package semsim;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;

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
			SemSimModel modelfordiscardedds, String oldtext, String replacementtext, double conversionfactor){
		
		Boolean selfrefODE = false;
		Set<DataStructure> dsstocheck = new HashSet<DataStructure>();
		dsstocheck.add(discardedds);
		
		if(discardedds.isSolutionDomain()) dsstocheck.addAll(modelfordiscardedds.getDataStructures());
		else dsstocheck.addAll(discardedds.getUsedToCompute());

		for(DataStructure dscheck : dsstocheck){
			if(discardedds!=keptds && !discardedds.isSolutionDomain()){ // Only reset the inputs if we're merging rather than renaming a DataStructure
				dscheck.getComputation().getInputs().remove(discardedds);
				dscheck.getComputation().addInput(keptds);
			}
			// If the two codeword names are actually different, perform replacements in equations, start value, and mathml
			if(!oldtext.equals(replacementtext)){
				
				String neweq = dscheck.getComputation().getComputationalCode();
				if(dscheck.getComputation().getComputationalCode()!=null){
					neweq = replaceCodewordsInString(dscheck.getComputation().getComputationalCode(), replacementtext, oldtext);
					modelfordiscardedds.getDataStructure(dscheck.getName()).getComputation().setComputationalCode(neweq);
				}
	
				// Assume that if discarded cdwd is in an IC for another cdwd, the discarded cdwd is set as an input to the other cdwd
				String newstart = dscheck.getStartValue();
				if(dscheck.hasStartValue()){
					newstart = replaceCodewordsInString(dscheck.getStartValue(), replacementtext, oldtext);
					modelfordiscardedds.getDataStructure(dscheck.getName()).setStartValue(newstart);
				}
				
				// apply conversion factors in mathml
				String newmathml = null;
				if(dscheck.getComputation().getMathML()!=null){
					if(conversionfactor!=1)
						newmathml = dscheck.getComputation().getMathML().replace("<ci>" + oldtext + "</ci>", 
							"<apply><times /><ci>" + replacementtext + "</ci><cn>" + conversionfactor + "</cn></apply>");
					else newmathml = dscheck.getComputation().getMathML().replace("<ci>" + oldtext + "</ci>",
							"<ci>" + replacementtext + "</ci>");
					modelfordiscardedds.getDataStructure(dscheck.getName()).getComputation().setMathML(newmathml);
				}
				
				// If the data structure that needs to have its computations edited is a derivative,
				// Find the state variable and edit its computations, too.
				if(dscheck.getName().contains(":")){
					String statevarname = dscheck.getName().substring(0, dscheck.getName().indexOf(":"));
					// If the data structure is used to compute it's own derivative
					if(statevarname.equals(oldtext)){
						selfrefODE = true;
					}
					else{
						System.out.println(statevarname);
						System.out.println(statevarname + ": " + modelfordiscardedds.getDataStructure(statevarname).getComputation());
						modelfordiscardedds.getDataStructure(statevarname).getComputation().setComputationalCode(neweq);
						modelfordiscardedds.getDataStructure(statevarname).getComputation().setMathML(newmathml);
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
			String newcompcode = sb.toString().trim();
			return newcompcode;
		}
		else return exp;
	}
	
	/**
	 * Write a string to a file
	 *  @param content The string to write out
	 *  @param outputfile The file to which the string will be written
	 */
	public static void writeStringToFile(String content, File outputfile){
		if(content!=null){
			if(!content.equals("") && outputfile!=null){
				PrintWriter pwriter;
				try {
					pwriter = new PrintWriter(new FileWriter(outputfile));
					pwriter.print(content);
					pwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}			
			}
		}
	}
	
	
	public static CompositePhysicalEntity getEquivalentCompositeEntityIfAlreadyInMap(CompositePhysicalEntity cpe, Map<? extends PhysicalModelComponent, URI> map){
		
		// Go through the composite physical entities already processed and see if there is one that
		// matches the cpe argument in terms of the physical entities and structural relations used
		for(PhysicalModelComponent testcomp : map.keySet()){
			if(testcomp instanceof CompositePhysicalEntity){
				CompositePhysicalEntity testcpe = (CompositePhysicalEntity)testcomp;

				// If we've found an equivalent composite entity, return it immediately
				if(testcpe.compareTo(cpe)==0){
					return testcpe;
				}
			}
		}
		// If we've gotten here, then there isn't a match, return the cpe argument
		if(cpe == null) System.out.println("Next cpe was null");
		return cpe;
	}	
}
