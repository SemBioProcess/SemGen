package semgen.encoding;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
<<<<<<< HEAD
=======
import java.util.Arrays;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;
<<<<<<< HEAD
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGen;
import semgen.resource.GenericThread;
import semgen.resource.uicomponents.ProgressBar;
import semsim.SemSimConstants;
import semsim.SemSimUtil;
import semsim.owl.SemSimOWLFactory;

public class MatlabCoder extends Coder{
	public PrintWriter writer;

	private String statevarvectorname, solutiondomain;
=======
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import semgen.GenericThread;
import semgen.ProgressFrame;
import semgen.SemGenGUI;
import semgen.merging.Merger;
import semsim.SemSimConstants;
import semsim.SemSimUtil;
import semsim.owl.SemSimOWLFactory;
import semsim.writing.CaseInsensitiveComparator;


public class MatlabCoder extends Coder{

	public String timevectorname;
	public String statevarvectorname;
	public String solutiondomain;
	public String modelname;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
	
	public MatlabCoder(File afile, String extension, File outputfile, Boolean autoencode) throws OWLException {
		super(afile, extension, outputfile, autoencode);
	}
	
	public void encode(Boolean suppressfinish) throws IOException, OWLException {
		if (outputfile != null) {
			// Need to make this a task
<<<<<<< HEAD
			
			GenericThread task = new GenericThread(this, "writefile");
			task.start();
		}
		else {System.out.println("Cancelled coding operation");}
	}
	
	public void writefile() throws OWLException, IOException{		
		// Find the solution domain
		ProgressBar progframe = new ProgressBar("Creating Matlab file", false);
		for(String ds : SemSimOWLFactory.getIndividualsInTreeAsStrings(ontology, SemSimConstants.SEMSIM_NAMESPACE + "Data_structure")){
=======
			progframe = new ProgressFrame("Creating Matlab file", false, null);
			GenericThread task = new GenericThread(this, "writefile");
			task.start();
		} else {System.out.println("Cancelled coding operation");}
	}
	
	
	public void writefile() throws OWLException, IOException{
		
		int cnt = 0;
		float frac = 0;
		float cntfloat = 0;
		
		// Find the solution domain
		for(String ds : SemSimOWLFactory.getIndividualsInTreeAsStrings(ontology, SemSimConstants.SEMSIM_NAMESPACE + "Data_structure")){
			//System.out.println(ds + " " + OWLMethods.getFunctionalIndDatatypeProperty(ontology, ds, SemSimConstants.SEMSIM_NAMESPACE + "isSolutionDomain"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			if(SemSimOWLFactory.getFunctionalIndDatatypeProperty(ontology, ds, SemSimConstants.SEMSIM_NAMESPACE + "isSolutionDomain").equals("true")){
				solutiondomain = ds;
			}
		}
		
		// Get all the computations in the model
		Set<String> computations = SemSimOWLFactory.getIndividualsAsStrings(ontology, SemSimConstants.SEMSIM_NAMESPACE + "Computation");
		ArrayList<String> cdwduris = new ArrayList<String>();
		ArrayList<String> orderedcdwduris = new ArrayList<String>();
		ArrayList<String> ODEcdwduris = new ArrayList<String>();
		
		Hashtable<String,Set<String>> cdwdurisandinputs = new Hashtable<String,Set<String>>();
		Hashtable<String,String> cdwdurisandformulas = new Hashtable<String,String>();
		Hashtable<String,String> cdwdurisandstartvals = new Hashtable<String,String>();
		
		Set<String> cdwdnames = new HashSet<String>();

<<<<<<< HEAD
		for(String computation : computations){
			// Use libSBML to convert MathML to infix notation
			String mathmlstring = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ontology, computation, SemSimConstants.SEMSIM_NAMESPACE + "hasMathML");
			String ast_as_string = libsbml.formulaToString(libsbml.readMathMLFromString(mathmlstring));
=======
		
		for(String computation : computations){
			
			// Use libSBML to convert MathML to infix notation
			String mathmlstring = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ontology, computation, SemSimConstants.SEMSIM_NAMESPACE + "hasMathML");
			ASTNode ast_result   = libsbml.readMathMLFromString(mathmlstring);
			String ast_as_string = libsbml.formulaToString(ast_result);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			
			String cdwduri = SemSimOWLFactory.getFunctionalIndObjectProperty(ontology, computation, SemSimConstants.SEMSIM_NAMESPACE + "hasOutput");
			String cdwdname = SemSimOWLFactory.getIRIfragment(cdwduri);
			cdwdnames.add(cdwdname);
			
			cdwdurisandinputs.put(cdwduri, SemSimOWLFactory.getIndObjectProperty(ontology, computation, SemSimConstants.SEMSIM_NAMESPACE + "hasInput"));
			if(ast_as_string!=null){
<<<<<<< HEAD
				cdwdurisandformulas.put(cdwduri, ast_as_string + ";");
=======
				String formula = ast_as_string + ";";
				cdwdurisandformulas.put(cdwduri, formula);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			}
			
			String startval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ontology, cdwduri, SemSimConstants.SEMSIM_NAMESPACE + "hasStartValue");
			if(startval.startsWith("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">")){
				ASTNode ast_startexp   = libsbml.readMathMLFromString(startval);
				startval = libsbml.formulaToString(ast_startexp);
			}
			cdwdurisandstartvals.put(cdwduri, startval);

			System.out.println(SemSimOWLFactory.getIRIfragment(cdwduri) + " has start condition " + startval);
			
			// If the codeword is solved with an ODE
			if(!startval.equals("")){
				ODEcdwduris.add(cdwduri);
			}
			// If not solved with an ODE, add to cdwd list
			else{
				cdwduris.add(cdwduri);
			}
     
		     // update progress for user
<<<<<<< HEAD
			if(progframe!=null){
				float frac = 1.0f / computations.size();
=======
		    cnt++;
			cntfloat = cnt;
			frac = cntfloat / computations.size();
			if(progframe!=null){
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
				progframe.bar.setValue(Math.round(99 * frac));
			}
		}
		
<<<<<<< HEAD
		String modelname = outputfile.getName().replace(".m", "");
		String timevectorname = createUniqueNameForMatlabVariable("t", cdwdnames);
=======
		modelname = outputfile.getName().replace(".m", "");
		timevectorname = createUniqueNameForMatlabVariable("t", cdwdnames);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		statevarvectorname = createUniqueNameForMatlabVariable("y", cdwdnames);
		
		// write out header and rest of file
		writer = new PrintWriter(new FileWriter(outputfile));
<<<<<<< HEAD
		writer.println("% Autogenerated by SemGen version " + SemGen.version);
		writer.println("function [ T,Y ] = " + modelname + "( input_args )");
		writer.println();
=======
		writer.println("% Autogenerated by SemGen version " + SemGenGUI.version);
		writer.println("function [ T,Y ] = " + modelname + "( input_args )");
		writer.println();
		//writer.println("options - odeset('RelTol',1e-4,'AbsTol',[1e-4 1e-4 1e-5]);");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		
		// sort codewords so they are in the order needed for procedural execution
		orderedcdwduris = (ArrayList<String>) cdwduris.clone();
		Boolean moveneeded = true;
		while(moveneeded){
			moveneeded = false;
			for(int x=0; x<cdwduris.size(); x++){
				
				String cdwduri = orderedcdwduris.get(x);
<<<<<<< HEAD
=======
				String cdwdname = SemSimOWLFactory.getIRIfragment(cdwduri);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
				Set<String> inputuris = cdwdurisandinputs.get(cdwduri);
				for(String inputuri : inputuris){
					if(orderedcdwduris.indexOf(inputuri) > orderedcdwduris.indexOf(cdwduri) 
							&& !cdwduri.equals(inputuri)){
						orderedcdwduris = swap(cdwduri, inputuri, orderedcdwduris);
						moveneeded = true;
					}
				}
			}
		}
		
		String[] ODEcdwduriarray = ODEcdwduris.toArray(new String[]{});
		String firstcdwdname = SemSimOWLFactory.getIRIfragment(ODEcdwduriarray[0]);
		String unitsuri = SemSimOWLFactory.getFunctionalIndObjectProperty(ontology, ODEcdwduriarray[0], SemSimConstants.SEMSIM_NAMESPACE + "hasUnit");
		String firstcdwdunits = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ontology, unitsuri, SemSimConstants.SEMSIM_NAMESPACE + "hasComputationalCode");
		writer.println("\tdisp('Running model...');");
		writer.print("\tICs = [");
		// NEED TO FIX INSTANCES WHERE AN IC REFERS TO SOME OTHER VARIABLE (VLV(t.min), for example)
		for(int y=0; y<ODEcdwduris.size(); y++){
			writer.print(cdwdurisandstartvals.get(ODEcdwduriarray[y]) + "; ");
		}
		writer.println("];");
		writer.print("\tStateVarNames = [");
		for(int y=0; y<ODEcdwduris.size(); y++){
			writer.print("{'" + SemSimOWLFactory.getIRIfragment(ODEcdwduriarray[y]) + "'}; ");
		}
		writer.println("];");
		
		writer.println("\t[T,Y] = ode45(@solveTerms, [0 100], ICs);");
		writer.println("\tplot(T,Y(:,1));");
		writer.println("\ttitle('" + firstcdwdname + "');");
		writer.println("\txlabel('" + SemSimOWLFactory.getIRIfragment(solutiondomain) + "');");
		writer.println("\tylabel('" + firstcdwdunits + "');");
		writer.println("\tdisp('...Finished.');");
		
		writer.println();
		writer.println("function dy = solveTerms("+ timevectorname + "," + statevarvectorname + ")");
		writer.println();
		writer.println("\tdy = zeros(" + ODEcdwduris.size() + ", 1);");
		writer.println("\t" + SemSimOWLFactory.getIRIfragment(solutiondomain) + " = " + timevectorname + ";");
<<<<<<< HEAD

		// write out the algebraic formulas in the necessary order, ignore MML-specific solution domain codewords
		for(String cdwduri : orderedcdwduris){
=======
		
		
		// write out the algebraic formulas in the necessary order, ignore MML-specific solution domain codewords
		for(String cdwduri : orderedcdwduris){
			//System.out.println("looking at: " + OWLMethods.getOWLEntityNameFromIRI(cdwduri) + " " + cdwdurisandformulas.get(cdwduri)); 
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			String cdwdname = SemSimOWLFactory.getIRIfragment(cdwduri);
			if(!ODEcdwduris.contains(cdwduri) && cdwdurisandformulas.get(cdwduri)!=null 
					&& !cdwdname.equals(SemSimOWLFactory.getIRIfragment(solutiondomain) + ".min")
					&& !cdwdname.equals(SemSimOWLFactory.getIRIfragment(solutiondomain) + ".max")
					&& !cdwdname.equals(SemSimOWLFactory.getIRIfragment(solutiondomain) + ".delta")){
				String neweq = replaceCodewordsWithMatlabPointers(cdwdurisandformulas.get(cdwduri), cdwduri, ODEcdwduris);
				writer.println("\t" + SemSimOWLFactory.getIRIfragment(cdwduri) + " = " + neweq);
			}
		}
		// write out the ODEs
		writer.println();
<<<<<<< HEAD
		OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(int y=0; y<ODEcdwduriarray.length; y++){
			writer.println("\t% " + SemSimOWLFactory.getIRIfragment(ODEcdwduriarray[y]) + ": " + 
					SemSimOWLFactory.getRDFcomment(ontology, factory.getOWLNamedIndividual(IRI.create(ODEcdwduriarray[y]))));
=======
		for(int y=0; y<ODEcdwduriarray.length; y++){
			writer.println("\t% " + SemSimOWLFactory.getIRIfragment(ODEcdwduriarray[y]) + ": " + 
					SemSimOWLFactory.getRDFcomment(ontology, SemGenGUI.factory.getOWLNamedIndividual(IRI.create(ODEcdwduriarray[y]))));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			String neweq = replaceCodewordsWithMatlabPointers(cdwdurisandformulas.get(ODEcdwduriarray[y]), ODEcdwduriarray[y], ODEcdwduris);
			writer.println("\t\tdy(" + (ODEcdwduris.indexOf(ODEcdwduriarray[y])+1) + ") = " + neweq); 
			writer.println();
		}
		writer.println();
		writeNeededFunctions();
		writer.close();
		
		if(progframe!=null){
<<<<<<< HEAD
			progframe.dispose();
=======
			progframe.setVisible(false);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		}
	}
	
	private String replaceCodewordsWithMatlabPointers(String oldeq, String cdwduri, ArrayList<String> ODEcdwduris) {
		String neweq = oldeq;
		for(String ODEcdwduri : ODEcdwduris){
			neweq = SemSimUtil.replaceCodewordsInString(neweq, statevarvectorname + "("+ (ODEcdwduris.indexOf(ODEcdwduri)+1) + ")", SemSimOWLFactory.getIRIfragment(ODEcdwduri));
		}
		return neweq;
	}

	
	public ArrayList<String> swap(String arg0, String arg1, ArrayList<String> list){
		int newindexforcdwd = list.indexOf(arg1);
		for(int j=list.indexOf(arg0); j<list.indexOf(arg1); j++){
			list.set(j, list.get(j+1));
		}
		list.set(newindexforcdwd, arg0);
<<<<<<< HEAD
=======
		//System.out.println("Swapped " + OWLMethods.getOWLEntityNameFromIRI(arg0) + " at pos " + list.indexOf(arg0) + " with " + OWLMethods.getOWLEntityNameFromIRI(arg1) + " at pos " + list.indexOf(arg1));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
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
	
	
	public void writeNeededFunctions(){
		writer.println();
		writer.println("function val = pow(arg0,arg1)");
		writer.println("\tval = arg0^arg1;");
		
		writer.println();
		writer.println("function val = piecewise(arg0,arg1,arg2)");
		writer.println("\tif arg1\n\t\t val = arg0;\n\telse\n\t\tval = arg2;\n\tend;");
	}
}
