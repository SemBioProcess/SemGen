package semsim.writing;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;

import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.utilities.SemSimUtil;


public class MATLABwriter extends ModelWriter{
	
	public String timevectorname;
	public String statevarvectorname;
	public String solutiondomain;
	private SAXBuilder saxbuilder = new SAXBuilder();
	private String mathMLhead = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">";
	private String mathMLtail = "</math>";
	private XMLOutputter outputter = new XMLOutputter();

	public MATLABwriter(SemSimModel model) {
		super(model);
	}
	
	public String writeToString() {
				
		outputter.setFormat(Format.getPrettyFormat());

		// Find the solution domain
		// ACCOUNT FOR MULTIPLE SOL. DOMs HERE.
		if(semsimmodel.getSolutionDomains().size()>0)
			solutiondomain = semsimmodel.getSolutionDomainNames().toArray(new String[]{})[0];
		
		
		// Get all the computations in the model
		ArrayList<DataStructure> algdslist = new ArrayList<DataStructure>();
		ArrayList<DataStructure> orderedalgdslist = new ArrayList<DataStructure>();
		ArrayList<DataStructure> ODEdslist = new ArrayList<DataStructure>();
		
		Hashtable<String,String> dsnamesandformulas = new Hashtable<String,String>();
		Hashtable<String,String> dsnamesandstartvals = new Hashtable<String,String>();
		
		Set<String> dsnames = semsimmodel.getDataStructureNames();

		for(DataStructure ds : semsimmodel.getDataStructures()){
			
			// Use libSBML to convert MathML to infix notation
			String mathmlstring = ds.getComputation().getMathML();
			
			String dsname = ds.getName();
			String dslocalname = dsname.contains(".") ? dsname.substring(dsname.lastIndexOf(".")+1, dsname.length()) : dsname;
			
			if(mathmlstring!=null) mathmlstring = SemSimUtil.getRHSofMathML(mathmlstring, dslocalname);
			
			ASTNode ast_result   = libsbml.readMathMLFromString(mathmlstring);
			String ast_as_string = libsbml.formulaToString(ast_result);
			
			String formula = null;
			
			boolean ispiecewise = false;
			boolean isODE = ds.hasStartValue();
			
			if(ast_as_string!=null){

				// If variable solved with piecewise statement, create if-else block
				if(ast_as_string.startsWith("piecewise(")){
					ispiecewise = true;
					formula = makePiecewiseExpression(mathmlstring, ds);
				}
				else formula = ast_as_string;
				
				formula = formula.replace("geq(", "ge(");
				formula = formula.replace("leq(", "le(");
				
				// If data structure is mappable variable, change local variable names in equation
				// to global names, also include the data structure itself, in the case of ODEs
				if(ds instanceof MappableVariable){
					
					String componentname = ds.getName().substring(0, ds.getName().lastIndexOf("."));

					Set<DataStructure> reqinputs = new HashSet<DataStructure>();
					reqinputs.addAll(ds.getComputationInputs());
					
					if(isODE || ispiecewise){
						// If ODE, perform replacement of ds first, otherwise this can lead to 
						// incorrect formulas when the ds name is the same as the component name
						formula = SemSimUtil.replaceCodewordsInString(formula, dsname, dslocalname);
					}
					
					for(DataStructure inputds : reqinputs){
						
						String inglobalname = inputds.getName();						
						String inlocalname = inglobalname.substring(inglobalname.lastIndexOf(".")+1, inglobalname.length());
						
						// If the input is from the same component, replace any occurrences of its local name
						// with its global name
						if(inputds.getName().equals(componentname + "." + inlocalname)){
							formula = SemSimUtil.replaceCodewordsInString(formula, inglobalname, inlocalname);
						}
					}
				}	
			}
			// If no formula present, check if the DataStructure is a CellML-type variable
			// and whether it has an initial value
			else if(ds instanceof MappableVariable){
				
				MappableVariable mv = (MappableVariable)ds;
				if(mv.getCellMLinitialValue()!=null && ! mv.getCellMLinitialValue().equals("")){
					formula = mv.getCellMLinitialValue();
				}
				
				// Otherwise, if the data structure's value comes from a component-to-component mapping ala CellML models,
				// make the RHS just the name of the source data structure
				else if(mv.getMappedFrom().size()>0)
					formula = mv.getMappedFrom().toArray(new MappableVariable[]{})[0].getName();
			}
			
			// If we've got a formula, store it in lookup table, otherwise we assume 
			// the data structure is the temporal solution domain
			if(formula!=null){
				
				if(isODE || ispiecewise) 
					dsnamesandformulas.put(dsname, formula);
				else 
					dsnamesandformulas.put(dsname, dsname + " = " + formula + ";");
			}
			else{
				solutiondomain = ds.getName();
			}

			// Get initial condition values for ODE state variables, if they exist
			if(isODE){
				String startval = ds.getStartValue();
				
				// If the start value uses a mathematical expression (is dependent on other vars, as can sometimes happen in JSim models)
				if(startval.startsWith(mathMLhead)){
					ASTNode ast_startexp   = libsbml.readMathMLFromString(startval);
					startval = libsbml.formulaToString(ast_startexp);
				}
					
				dsnamesandstartvals.put(dsname, startval);
				ODEdslist.add(ds);
			}
			// If not solved with an ODE, add to cdwd list
			else if(! dsname.endsWith(":" + solutiondomain)) algdslist.add(ds);
		}
		
		String modelname = semsimmodel.getName();
		modelname = modelname.replace(".", "_");
		timevectorname = createUniqueNameForMatlabVariable("t", dsnames);
		statevarvectorname = createUniqueNameForMatlabVariable("y", dsnames);
		
		// write out header and rest of file
		String outstring = "";
		outstring = outstring + "% Autogenerated by SemSim version " + SemSimLibrary.SEMSIM_VERSION + "\n";
		outstring = outstring +("function [ T,Y ] = " + modelname + "()\n");
		outstring = outstring + "\n";
		
		// sort codewords so they are in the order needed for procedural execution
		orderedalgdslist.addAll(algdslist);
		
		Boolean moveneeded = true;
		while(moveneeded){
			moveneeded = false;
			for(int x=0; x<algdslist.size(); x++){
				
				DataStructure ds = orderedalgdslist.get(x);
				
				for(DataStructure inputds : ds.getComputation().getInputs()){
					if(orderedalgdslist.indexOf(inputds) > orderedalgdslist.indexOf(ds) 
							&& !ds.equals(inputds)){
						orderedalgdslist = swap(ds, inputds, orderedalgdslist);
						moveneeded = true;
					}
				}
			}
		}
		
		
		DataStructure[] ODEdsarray = ODEdslist.toArray(new DataStructure[]{});
		
		// Get units for plotting a variable
		String firstcdwdname = ODEdsarray[0].getName();
		
		// Get the units name if present
		String firstcdwdunits = ODEdsarray[0].hasUnits() ? ODEdsarray[0].getUnit().getName() : "dimensionless";
		
		outstring = outstring + ("\ttic\n");
		outstring = outstring + ("\tdisp('Running " + modelname + "...');\n");
		outstring = outstring + "\tICs = [";
		
		// Write out the ODEs
		// NEED TO FIX INSTANCES WHERE AN IC REFERS TO SOME OTHER VARIABLE (VLV(t.min), for example)
		for(int y=0; y<ODEdslist.size(); y++){
			outstring = outstring + ODEdsarray[y].getStartValue() + "; ";
		}
		outstring = outstring + "];\n";
		outstring = outstring + "\tODEvariableNames = [";
		for(int y=0; y<ODEdslist.size(); y++){
			outstring = outstring + "{'" + ODEdsarray[y].getName() + "'}; ";
		}
		
		DataStructure soldomds = semsimmodel.getDataStructure(solutiondomain);
		String solutiondomainunits = soldomds.hasUnits() ? soldomds.getUnit().getName() : "dimensionless";
		
		outstring = outstring + ("];\n");
		
		outstring = outstring + ("\t[T,Y] = ode45(@solveTerms, [0 100], ICs);\n");
		outstring = outstring + ("\tplot(T,Y(:,1));\n");
		outstring = outstring + ("\ttitle('" + firstcdwdname + "','Interpreter', 'none');\n");
		outstring = outstring + ("\txlabel('" + solutiondomainunits + "');\n");
		outstring = outstring + ("\tylabel('" + firstcdwdunits + "');\n");
		outstring = outstring + ("\tdisp('...Finished.');\n");
		outstring = outstring + ("\ttoc\n");
		
		outstring = outstring + "\n";
		outstring = outstring + ("function dy = solveTerms("+ timevectorname + "," + statevarvectorname + ")\n");
		outstring = outstring + "\n";
		outstring = outstring + ("\tdy = zeros(" + ODEdslist.size() + ", 1);\n");
		outstring = outstring + ("\t" + solutiondomain + " = " + timevectorname + ";\n");
		
		// write out the algebraic formulas in the necessary order, ignore MML-specific solution domain codewords
		for(DataStructure algds : orderedalgdslist){
			
			String dsname = algds.getName();
			if(!ODEdslist.contains(algds) && dsnamesandformulas.get(dsname)!=null 
					&& !dsname.equals(solutiondomain + ".min")
					&& !dsname.equals(solutiondomain + ".max")
					&& !dsname.equals(solutiondomain + ".delta")){
				
				String neweq = replaceCodewordsWithODEvarPointers(dsnamesandformulas.get(dsname), ODEdslist, false);
				
				// If the ds is solved piecewise, format if statement
				if (neweq.startsWith("if ")){
					String spacer = outstring.endsWith("\n\n") ? "" : "\n";
					outstring = outstring + (spacer + "\t% Solving " + dsname + "\n\t" + neweq + "\n");
				}
				else 
					outstring = outstring + ("\t" + neweq);
				
				outstring = outstring + "\n";
			}
		}
		
		// Write out the ODEs
		outstring = outstring + "\n\n";
		for(int y=0; y<ODEdsarray.length; y++){
			
			DataStructure ODEds = ODEdsarray[y];
			outstring = outstring + ("\t% " + ODEds.getName());
			
			// Add comment indicating biophysical meaning of codeword, if present
			if(ODEds.getDescription()!=null && ! ODEds.getDescription().equals(""))  
				outstring = outstring + ": " + ODEds.getDescription();
			
			String formula = dsnamesandformulas.get(ODEds.getName());
			if (formula.startsWith("if ")){
				formula = replaceCodewordsWithODEvarPointers(formula, ODEdslist, true);
				outstring = outstring + ("\n\t" + formula + ";\n"); 
			}
			else {
				formula = replaceCodewordsWithODEvarPointers(formula, ODEdslist, false);
				outstring = outstring + ("\n\tdy(" + (ODEdslist.indexOf(ODEds)+1) + ") = " + formula + ";\n"); 
			}
			outstring = outstring + "\n";
		}
		outstring = outstring + "\n";
		
		outstring = writeNeededFunctions(outstring);
		
		return outstring;
	}
	
	
	private String replaceCodewordsWithODEvarPointers(String oldeq, ArrayList<DataStructure> ODEdslist, boolean usederivativename) {
		String name = usederivativename ? "d" + statevarvectorname : statevarvectorname;
		String neweq = oldeq;
		for(DataStructure ODEds : ODEdslist){
			neweq = SemSimUtil.replaceCodewordsInString(neweq, name + "("+ (ODEdslist.indexOf(ODEds)+1) + ")", ODEds.getName());
		}
		return neweq;
	}

	
	public ArrayList<DataStructure> swap(DataStructure arg0, DataStructure arg1, ArrayList<DataStructure> list){
		int newindexforcdwd = list.indexOf(arg1);
		for(int j=list.indexOf(arg0); j<list.indexOf(arg1); j++){
			list.set(j, list.get(j+1));
		}
		list.set(newindexforcdwd, arg0);
		return list;
	}
	
	
	public String createUniqueNameForMatlabVariable(String startname, Set<String> usednames){
		String newname = startname;
		int g = 0;
		while(usednames.contains(newname)){
			newname = newname + g;
			g++;
		}
		return newname;
	}
	
	
	
	
	// Create MATLAB-friendly conditional statement to replace piecewise formulations
	private String makePiecewiseExpression(String mathmlstring, DataStructure solvedvar){
		
		String solvedvarname = solvedvar.getName();
		
		if(solvedvarname.contains("."))
			solvedvarname = solvedvarname.substring(solvedvarname.lastIndexOf(".")+1, solvedvarname.length());
				
		String formula = "if ";
		Document doc;
		try {
			doc = saxbuilder.build(new StringReader(mathmlstring));
			
			Namespace mathns = SemSimConstants.MATHML_NAMESPACE_OBJ;
			if(doc.getRootElement().getChild("piecewise", mathns)!=null){
				Iterator<?> piecesit = doc.getRootElement().getChild("piecewise",mathns).getChildren("piece", mathns).iterator();
				
				while(piecesit.hasNext()){
					Element pieceel = (Element)piecesit.next();
					Iterator<?> piecechildrenit = pieceel.getChildren().iterator();
					Element value = (Element)piecechildrenit.next();
					Element condition = (Element)piecechildrenit.next();
										
					ASTNode value_ast   = libsbml.readMathMLFromString(mathMLhead + outputter.outputString(value) + mathMLtail);
					String value_ast_string = libsbml.formulaToString(value_ast);
					
					ASTNode condition_ast   = libsbml.readMathMLFromString(mathMLhead + outputter.outputString(condition) + mathMLtail);
					String condition_ast_string = libsbml.formulaToString(condition_ast);
										
					formula = formula + condition_ast_string + "\n";
					formula = formula + "\t\t" + solvedvarname + " = " + value_ast_string + ";\n";
					
					if(piecesit.hasNext())
						formula = formula + "\telseif ";
				}
				
				// Get the <otherwise> statement
				String otherwise_ast_string = "???";
				if(doc.getRootElement().getChild("piecewise",mathns).getChild("otherwise", mathns)!=null){
					Element otherwiseel = doc.getRootElement().getChild("piecewise",mathns).getChild("otherwise", mathns);
					ASTNode otherwise_ast   = libsbml.readMathMLFromString(mathMLhead + outputter.outputString(otherwiseel.getChildren()) + mathMLtail);
					otherwise_ast_string = libsbml.formulaToString(otherwise_ast);
				}
				formula = formula + "\telse " + solvedvarname + " = " + otherwise_ast_string + ";\n\tend";
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		return formula;
	}
	
	// Write out standalone functions that are needed for matlab to understand formulas 
	// that are output from libsbml formula writer
	public String writeNeededFunctions(String outstring){
		
		// Exponent function
		String thestring = outstring + "\n";
		thestring = thestring + "function val = pow(arg0,arg1)\n";
		thestring = thestring + "\tval = arg0^arg1;\n";
		
		// Variable-argument 'and' function
		thestring = thestring + "\n";
		thestring = thestring + "function val = and(varargin)\n";
		thestring = thestring + "\tval = all([varargin{:}]);\n";
		
		return thestring;
	}
	

	@Override
	public void writeToFile(File destination) {
		SemSimUtil.writeStringToFile(writeToString(), destination);
	}

	@Override
	public void writeToFile(URI destination) {
		SemSimUtil.writeStringToFile(writeToString(), new File(destination));
	}
}
