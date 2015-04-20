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
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;

import semsim.SemSimLibrary;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;


public class MATLABwriter extends ModelWriter{
	
	public String timevectorname;
	public String statevarvectorname;
	public String solutiondomain;
	private SAXBuilder saxbuilder = new SAXBuilder();
	private static Namespace mathMLnamespace = Namespace.getNamespace("http://www.w3.org/1998/Math/MathML");
	private static String mathMLhead = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">";
	private static String mathMLtail = "</math>";

	public MATLABwriter(SemSimModel model) {
		super(model);
	}

	
	
	public String writeToString() {
				
		// HOW TO DEAL WITH CELLML COMPONENTS (RENAME DATA STRUCTURES?)
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
			
			if(mathmlstring!=null) mathmlstring = getRHSofMathML(mathmlstring);
			
			ASTNode ast_result   = libsbml.readMathMLFromString(mathmlstring);
			String ast_as_string = libsbml.formulaToString(ast_result);
			
			String dsname = ds.getName();
			String formula = null;
			
			if(ast_as_string!=null){

				formula = ast_as_string;
				
				// If data structure is mappable variable, change local variable names in equation
				// to global names, also include the data structure itself, in the case of ODEs
				if(ds instanceof MappableVariable){
					
					Set<DataStructure> reqinputs = new HashSet<DataStructure>();
					reqinputs.addAll(ds.getComputationInputs());
					reqinputs.add(ds);
					
					for(DataStructure inputds : reqinputs){
						String inglobalname = inputds.getName();
						
						String componentname = ds.getName().substring(0, ds.getName().lastIndexOf("."));
						String inlocalname = inglobalname.substring(inglobalname.lastIndexOf(".")+1, inglobalname.length());
						
						// If the input is from the same component, replace any occurences of its local name
						// with its global name
						if(inputds.getName().equals(componentname + "." + inlocalname))
							formula = SemSimUtil.replaceCodewordsInString(formula, inglobalname, inlocalname);
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
				else if(mv.getMappedFrom().size()>0){
					formula = mv.getMappedFrom().toArray(new MappableVariable[]{})[0].getName();
				}
			}
			
			// If we've got a formula, store it in lookup table, otherwise we assume 
			// the data structure is the temporal solution domain
			if(formula!=null)
				dsnamesandformulas.put(dsname, formula + ";");
			else{
				solutiondomain = ds.getName();
			}

			
			// Get initial condition values for ODE state variables, if they exist
			if(ds.hasStartValue()){
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
		timevectorname = createUniqueNameForMatlabVariable("t", dsnames);
		statevarvectorname = createUniqueNameForMatlabVariable("y", dsnames);
		
		// write out header and rest of file
		String outstring = "";
		outstring = outstring + "% Autogenerated by SemSim version " + SemSimLibrary.SEMSIM_VERSION + "\n";
		outstring = outstring +("function [ T,Y ] = " + modelname + "( input_args )\n");
		outstring = outstring + "\n";

		//writer.println("options - odeset('RelTol',1e-4,'AbsTol',[1e-4 1e-4 1e-5]);");
		
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
		
		// Get units for plotting variable
		String firstcdwdname = ODEdsarray[0].getName();
		
		// Get the units name if present
		String firstcdwdunits = ODEdsarray[0].hasUnits() ? ODEdsarray[0].getUnit().getName() : "";
		
		outstring = outstring + ("\tdisp('Running model...');\n");
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
		outstring = outstring + ("];\n");
		
		outstring = outstring + ("\t[T,Y] = ode45(@solveTerms, [0 100], ICs);\n");
		outstring = outstring + ("\tplot(T,Y(:,1));\n");
		outstring = outstring + ("\ttitle('" + firstcdwdname + "');\n");
		outstring = outstring + ("\txlabel('" + solutiondomain + "');\n");
		outstring = outstring + ("\tylabel('" + firstcdwdunits + "');\n");
		outstring = outstring + ("\tdisp('...Finished.');\n");
		
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
				
				String neweq = replaceCodewordsWithMatlabPointers(dsnamesandformulas.get(dsname), ODEdslist);
				
				outstring = outstring + ("\t" + dsname + " = " + neweq + "\n");
			}
		}
		

		// write out the ODEs
		outstring = outstring + "\n";
		for(int y=0; y<ODEdsarray.length; y++){
			
			// Add comment indicating biophysical meaning of codeword
			outstring = outstring + ("\t% " + ODEdsarray[y].getName()) + ": " + ODEdsarray[y].getDescription() + "\n";
	
			String neweq = replaceCodewordsWithMatlabPointers(dsnamesandformulas.get(ODEdsarray[y].getName()), ODEdslist);
			outstring = outstring + ("\tdy(" + (ODEdslist.indexOf(ODEdsarray[y])+1) + ") = " + neweq + "\n"); 
			outstring = outstring + "\n";
		}
		outstring = outstring + "\n";
		
		outstring = writeNeededFunctions(outstring);
		return outstring;
	}
	
	private String replaceCodewordsWithMatlabPointers(String oldeq, ArrayList<DataStructure> ODEdslist) {
		String neweq = oldeq;
		for(DataStructure ODEds : ODEdslist){
			neweq = SemSimUtil.replaceCodewordsInString(neweq, statevarvectorname + "("+ (ODEdslist.indexOf(ODEds)+1) + ")", ODEds.getName());
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
	
	
	public String writeNeededFunctions(String outstring){
		String thestring = outstring + "\n";
		thestring = thestring + "function val = pow(arg0,arg1)\n";
		thestring = thestring + "\tval = arg0^arg1;\n";
		
		thestring = thestring + "\n";
		thestring = thestring + "function val = piecewise(arg0,arg1,arg2)\n";
		thestring = thestring + "\tif arg1\n\t\t val = arg0;\n\telse\n\t\tval = arg2;\n\tend;\n";
		
		thestring = thestring + "\n";
		thestring = thestring + "function val = geq(arg0,arg1)\n";
		thestring = thestring + "\tval = (arg0>=arg1);\n";
		
		thestring = thestring + "\n";
		thestring = thestring + "function val = leq(arg0,arg1)\n";
		thestring = thestring + "\tval = (arg0<=arg1);\n";
		
		return thestring;
	}
	
	public String getRHSofMathML(String mathmlstring){
		
		try {
			Document doc = saxbuilder.build(new StringReader(mathmlstring));
			
			// Get the <eq> element if there is one...
			Boolean hastopeqel = false;
			if(doc.getRootElement().getChild("apply",mathMLnamespace)!=null){
				if(doc.getRootElement().getChild("apply",mathMLnamespace).getChild("eq", mathMLnamespace)!=null){
					hastopeqel = true;
				}
			}
			if(hastopeqel){
				Element eqel = doc.getRootElement().getChild("apply",mathMLnamespace).getChild("eq",mathMLnamespace); //(Element)alleqs.next();
				Element eqparentel = eqel.getParentElement();
				
				// Iterate over the <eq> element's siblings by getting its parent's children
				Iterator<?> eqparentelit = eqparentel.getChildren().iterator();
	
				while(eqparentelit.hasNext()){
					Element nextel = (Element)eqparentelit.next();
					Boolean isciel = nextel.getName().equals("ci");
					Boolean iseqel = nextel.getName().equals("eq");
					Iterator<?> nexteldescit = nextel.getDescendants(new ElementFilter("diff"));
					Boolean hasdiffel = nexteldescit.hasNext();
	
					// If the element isn't a <ci> variable element or an <eq> element, or doeesn't have a <diff> descendant,
					// then we've found our RHS
					if(! isciel && ! hasdiffel && ! iseqel){
						XMLOutputter outputter = new XMLOutputter();
						outputter.setFormat(Format.getPrettyFormat());
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

	@Override
	public void writeToFile(File destination) {
		SemSimUtil.writeStringToFile(writeToString(), destination);
	}

	@Override
	public void writeToFile(URI destination) {
		SemSimUtil.writeStringToFile(writeToString(), new File(destination));
	}
}
