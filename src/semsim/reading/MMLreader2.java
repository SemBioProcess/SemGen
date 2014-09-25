package semsim.reading;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;

import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.computational.Computation;
import semsim.model.computational.DataStructure;
import semsim.model.computational.Decimal;
import semsim.model.computational.MMLchoice;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.SemSimInteger;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalProperty;

public class MMLreader2 extends MMLreader {
	
	private Hashtable<String,String> discretevarsandconstraints = new Hashtable<String,String>();
	private Hashtable<String,String> discretevarsandmathml = new Hashtable<String,String>();
	private Hashtable<String,String[]> discretevarsandeventtriggerinputs = new Hashtable<String,String[]>();
	private Set<String> realStatenames = new HashSet<String>();
	private Namespace mathmlns = Namespace.getNamespace("", "http://www.w3.org/1998/Math/MathML");
	private XMLOutputter xmloutputter = new XMLOutputter();
	private Set<Element> toolset = new HashSet<Element>(); 
	private Document doc;

	protected MMLreader2(File filetoparse, Document doc, String jsimBuildDir) throws Xcept {
		super(jsimBuildDir);
		this.doc = doc;
		this.semsimmodel = new SemSimModel();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SemSimModel readFromFile(File file) throws FileNotFoundException, IOException, InterruptedException, OWLException {

		int numdomains = 0;
		// Collect all tools into a set
		Iterator toolit = doc.getRootElement().getChild("toolList").getChildren().iterator();
		while(toolit.hasNext()) toolset.add((Element)toolit.next());
			
		Element varroot = doc.getRootElement().getChild("model").getChild("variableList");
		List varchildren = varroot.getChildren();
		Iterator variterator = varchildren.iterator();

		// Collect the units
		Iterator unitit = doc.getRootElement().getChild("model").getChild("unitList").getChildren().iterator();
		while(unitit.hasNext()){
			
			Element unitel = (Element) unitit.next();
			if(unitel.getName().equals("fundamentalUnit") || unitel.getName().equals("derivedUnit")){
				String unitname = unitel.getAttributeValue("id");
				String unittype = unitel.getAttributeValue("unitType");
				
				UnitOfMeasurement uom = new UnitOfMeasurement(unitname);
				uom.setUnitType(unittype);
				semsimmodel.addUnit(uom);
				
				if(unitel.getName().equals("fundamentalUnit")){
					uom.setFundamental(true);
				}
				else if(unitel.getName().equals("derivedUnit")){
					uom.setFundamental(false);
					// ... need methods here to collect the unitFactors, etc.
				}
			}
		}

		// Get the eventConstraints
		Iterator eventiterator = doc.getRootElement().getChild("model").getChild("eventList").getChildren("event").iterator();
		while (eventiterator.hasNext()) {
			Element oneevent = (Element) eventiterator.next();
			String triggertext = "event("+ oneevent.getChild("trigger").getChild("debug").getText() + ")";

			// Get the inputs for the trigger equations
			Element trigger = oneevent.getChild("trigger");
			List<Element> triggerchildren = trigger.getChildren();
			Iterator triggerchildreniterator = triggerchildren.iterator();
			String[] inputsfortrigger = new String[] {};

			while (triggerchildreniterator.hasNext()) {
				Element triggerchild = (Element) triggerchildreniterator.next();
				if (triggerchild.getName().equals("math")) {
					inputsfortrigger = getIdentifiersInMathMLEquation(trigger.getChild("math", triggerchild.getNamespace()));
				}
			}

			List stateactions = oneevent.getChild("actionList").getChildren("action");
			Iterator stateactionsiterator = stateactions.iterator();
			while (stateactionsiterator.hasNext()) {
				Element action = (Element) stateactionsiterator.next();
				String varstring = action.getAttributeValue("variableID");
				String actioneq = varstring + " = " + action.getChild("expression").getChild("debug").getText();
				String fulltext = triggertext + "{ " + actioneq + "; }";
				discretevarsandconstraints.put(varstring, fulltext);
				discretevarsandmathml.put(varstring,xmloutputter.outputString(action.getChild("expression").getChild("math",mathmlns)));
				discretevarsandeventtriggerinputs.put(varstring, inputsfortrigger);
				realStatenames.add(varstring);
			}
		}

		// Get the JSim "relations" constraints
		Iterator relit = doc.getRootElement().getChild("model").getChild("relationList").getChildren("relation").iterator();
		while(relit.hasNext()){
			Element rel = (Element) relit.next();
			String mmleq = rel.getChild("expression").getChildText("debug");
			String mathmleq = xmloutputter.outputString(rel.getChild("expression").getChild("math",mathmlns));
			semsimmodel.addRelationalConstraint(new RelationalConstraint(mmleq, mathmleq));
		}

		variterator = varchildren.iterator();
			
		// collect the codeword names and units
		while (variterator.hasNext()) {
			Element varchild = (Element) variterator.next();
			// if variable should go into SemSim
			if (!varchild.getAttributeValue("id").endsWith(".ct")) {
				String codeword =  varchild.getAttributeValue("id");
				
				DataStructure ds = null;

				// Store the data type attribute
				String vardatatype = varchild.getAttributeValue("dataType");
				if (vardatatype.equals("real"))  ds = semsimmodel.addDataStructure(new Decimal(codeword));
				else if (vardatatype.equals("int")) ds = semsimmodel.addDataStructure(new SemSimInteger(codeword));
				else if(vardatatype.equals("choice")) ds = semsimmodel.addDataStructure(new MMLchoice(codeword));
				
				if(codeword.contains(":") || codeword.endsWith("__init")) ds.setDeclared(false);
				else ds.setDeclared(true);
				
				ds.setPhysicalProperty(new PhysicalProperty()); // If units, instantiate property
			
				// Store the units attribute value
				String unitname = "";
				if (varchild.getAttributeValue("unitID") != null) {
					unitname = varchild.getAttributeValue("unitID");
					if(semsimmodel.containsUnit(unitname)) ds.setUnit(semsimmodel.getUnit(unitname));
					else{
						UnitOfMeasurement uom = new UnitOfMeasurement(unitname);
						semsimmodel.addUnit(uom);
						ds.setUnit(uom);
					}
				} 
				
				// Store the solution domain attribute in the array
				if(varchild.getChild("domainList")!=null){
					Iterator domit = varchild.getChild("domainList").getChildren("domain").iterator();
					while(domit.hasNext()){
						Element dom = (Element) domit.next();
						String domname = dom.getAttributeValue("domainID");
						DataStructure soldom;
						if(!domname.equals(codeword)){
							if(semsimmodel.hasSolutionDomainName(domname)){
								soldom = semsimmodel.getSolutionDomainByName(domname);
								ds.setSolutionDomain(soldom);
							}
							else{
								if(varchild.getAttributeValue("dataType").equals("real")){
									Decimal newsoldomain = new Decimal(domname);
									newsoldomain.setIsSolutionDomain(true);
									semsimmodel.addDataStructure(newsoldomain);
								}
								else if(varchild.getAttributeValue("dataType").equals("int")){
									SemSimInteger newsoldomain = new SemSimInteger(domname);
									newsoldomain.setIsSolutionDomain(true);
									semsimmodel.addDataStructure(newsoldomain);
								}
							}
						break;
						// When change to allow for multiple solution domains, need to edit this part
						}
					}
				}

				// Store the isDomain value in the array
				if (varchild.getAttributeValue("isDomain").equals("true")){
					ds.setIsSolutionDomain(true);
					numdomains++;
					
					// Catch multiple domains
					if(numdomains>1){
						semsimmodel.addError("The model contains multiple solution domains.\n" +
								"SemGen currently only supports single-domain models." );
						return semsimmodel;
					}
				}
				else ds.setIsSolutionDomain(false);

				ds.setDiscrete(false);
				
				// Store whether the variable is discrete 
				if (varchild.getAttributeValue("isState") != null) {
					if(varchild.getAttributeValue("isState").equals("true")) ds.setDiscrete(true);
				} 
				
				// find the assignment constraint for the variable & start with implicit, then event constraints
				boolean stop = false;
				Computation computation = ds.getComputation();
				
				if (realStatenames.contains(codeword)) {
					computation.setComputationalCode(discretevarsandconstraints.get(codeword));
					computation.setMathML(discretevarsandmathml.get(codeword));

					// get the initial condition for the discrete realState variable (in some stateTool)
					ds.setStartValue(getIC(getToolToSolveCodeword(codeword), codeword));
				} 
				
				// Get the math assignments for all other types of variables
				else {
					List<Element> tools = doc.getRootElement().getChild("toolList").getChildren("exprTool");
					Iterator exprit = tools.iterator();
					while (exprit.hasNext() && !stop) {
						Element expr = (Element) exprit.next();
						Iterator solvedvarsit = expr.getChild("solvedVariableList").getChildren("variableUsage").iterator();
						while(solvedvarsit.hasNext() && !stop){
							Element solvedvar = (Element) solvedvarsit.next();
							if(solvedvar.getAttributeValue("id").equals(codeword)){ //&& ds.isDeclared()){
								
								String mmlcode = expr.getChild("expression").getChildText("debug");
								String mathmlassignment = xmloutputter.outputString(expr.getChild("expression").getChild("math",mathmlns));
								
								computation.setComputationalCode(codeword + " = " + mmlcode);
								computation.setMathML(mathmlassignment);
								stop = true;
							}
						}
					}
				}

				// If the codeword is solved with an ODE tool, get the state equation and initial condition from the ODE tool
				Iterator ODEtoolit = doc.getRootElement().getChild("toolList").getChildren("ODETool").iterator();
				stop = false;
				Element ODEtool = null;
				while (ODEtoolit.hasNext() && !stop) {
					ODEtool = (Element) ODEtoolit.next();
					Iterator varit = ODEtool.getChild("solvedVariableList").getChildren("variableUsage").iterator();
					while(varit.hasNext()){
						Element var = (Element) varit.next();
						if (var.getAttributeValue("id").equals(codeword) 
								&& !var.getAttributeValue("id").endsWith(".max)")){
								//&& ds.isDeclared()){
							String varid = codeword + ":" + ODEtool.getAttributeValue("timeDomainID");
							String steqid = ODEtool.getChild("stateEquation").getAttributeValue("toolID");
							Element steqtool = getToolByID(steqid);
							// Set the state equation
							if(steqtool.getName().equals("exprTool")){
								computation.setComputationalCode(codeword + ":" + ODEtool.getAttributeValue("timeDomainID") + " = " + steqtool.getChild("expression").getChild("debug").getText());
								computation.setMathML(xmloutputter.outputString(steqtool.getChild("expression").getChild("math",mathmlns)));
							}
							// if the state equation is part of an implicit tool, find the appropriate equation
							else if(steqtool.getName().equals("implicitTool")){
								Iterator zeroexit = steqtool.getChild("zeroExpressionList").getChildren("expression").iterator();
								while(zeroexit.hasNext()){
									Element ex = (Element) zeroexit.next();
									if(ex.getChild("debug").getText().startsWith(varid)){
										computation.setComputationalCode("0 = " + ex.getChild("debug").getText());
										computation.setMathML(xmloutputter.outputString(ex.getChild("math",mathmlns)));
									}
								}
							}
							ds.setStartValue(getIC(ODEtool, codeword));
						}
					}
				}
				
				// Get the implicit constraints
				Iterator imptools = doc.getRootElement().getChild("toolList").getChildren("implicitTool").iterator();
				Element imptool = null;
				while(imptools.hasNext()){
					imptool = (Element) imptools.next();
					Iterator<Element> solvedvars = imptool.getChild("solvedVariableList").getChildren("variableUsage").iterator();
					Iterator<Element> zeroeqs = imptool.getChild("zeroExpressionList").getChildren("expression").iterator();
					
					while(solvedvars.hasNext()){
						String solvedvar = solvedvars.next().getAttributeValue("id");
						Element zeroeq = zeroeqs.next();
						// If the variable is solved by the implicit tool
						if(solvedvar.equals(codeword)){
							// Get the zero expressions, find the one for the variable in question
							String mmleq = zeroeq.getChild("debug").getText();
							String mathmleq = xmloutputter.outputString(zeroeq.getChild("math",mathmlns));
							// Store in semsim model
							computation.setComputationalCode("0 = " + mmleq);
							computation.setMathML(mathmleq);
						}
					}
				}
			} // End of if statement that leaves out .ct vars
		} // End of variable iterator
		
		// Set the custom units
		setCustomUnits(file);
		
		// find the hasInput properties for the computations and hasRolePlayer properties for the dependencies
		for (Element tool : toolset) {
			Iterator solvedvarit = tool.getChild("solvedVariableList").getChildren("variableUsage").iterator();
			List invarchildren = tool.getChild("requiredVariableList").getChildren("variableUsage");
			while(solvedvarit.hasNext()){
				
				Iterator invariterator = tool.getChild("requiredVariableList").getChildren("variableUsage").iterator();
				Element solvedvar = (Element) solvedvarit.next();
				String name = solvedvar.getAttributeValue("variableID");
				
				if(semsimmodel.containsDataStructure(name) && solvedvar.getAttributeValue("status").equals("CURR")){
					DataStructure solvedds = semsimmodel.getDataStructure(name);
					if(!solvedds.isSolutionDomain()){
						invariterator = invarchildren.iterator();
	
						// Get all the input variables
						while (invariterator.hasNext()) {
							Element invarchild = (Element) invariterator.next();
							String inputname = invarchild.getAttributeValue("variableID");
								
							// All data structures, including the undeclared ones, should have been entered by now
								
							// Establish input relationships
							if(semsimmodel.containsDataStructure(inputname)){
								// Is ok if data structures are dependent on themselves
								// Do not include the input relationship unless the input variable is actually in the MathML
								// for computing the output variable
								DataStructure inputds = semsimmodel.getDataStructure(inputname);
								
								// As long as we're not looking at an ODE tool and the input isn't the derivative of the solved var, include input
								if(tool.getName().equals("ODETool")){
									if(inputds.getName().startsWith(solvedds.getName() + ":")){
										solvedds.getComputation().addInput(inputds);
									}
								}
								else{
									solvedds.getComputation().addInput(inputds);
								}
							}
							else System.out.println("Cannot set input: model doesn't have " + inputname);
						}	
					}
				}
			}
		} 
		
		// Set hasInput/inputFor relationships for discrete variables and the data structures required for triggering them
		for(String dsx : discretevarsandeventtriggerinputs.keySet()){
			for(String inputx : discretevarsandeventtriggerinputs.get(dsx))
				semsimmodel.getDataStructure(dsx).getComputation().addInput(semsimmodel.getDataStructure(inputx));
		}
				
		// Add the model-level annotations
		semsimmodel.addAnnotation(new Annotation(SemSimConstants.LEGACY_CODE_LOCATION_RELATION, file.getAbsolutePath()));
		semsimmodel.addAnnotation(new Annotation(SemSimConstants.SEMSIM_VERSION_RELATION, Double.toString(SemSimConstants.SEMSIM_VERSION)));

		// If jsbatch couldn't parse the model code into an xmml file, log the error
		if(semsimmodel.getDataStructures().isEmpty() && semsimmodel.getPhysicalModelComponents().isEmpty()){
			semsimmodel.addError(file.getName() + " model appears to be empty.");
		}
		return semsimmodel;
	}

	
	private void setCustomUnits(File file) throws FileNotFoundException, OWLException {
		Map<String,String> unitnamesandcustomdeclarations = new HashMap<String,String>();
		Scanner scnr = new Scanner(file);

		// This next part that attempts to account for custom unit declarations
		// is a bit of a hack
		// May need to account for multiple code lines in original .mod file
		// (blah...; blah....; blah.....;)
		// Can this be rewritten using XMML2?
		while (scnr.hasNextLine()) {
			String nextline = scnr.nextLine();
			String nextlinemod = "";
			nextlinemod = nextline.replace(" ", "");
			nextlinemod = nextlinemod.replace("\t", "");
			if (nextlinemod.startsWith("unit") && nextlinemod.contains("=")) {
				String unitname = nextlinemod.substring(nextlinemod.indexOf("unit") + 4,nextlinemod.indexOf("="));
				unitnamesandcustomdeclarations.put(unitname, nextline);
			}
		}
		scnr.close();
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(ds.hasUnits()){
				if(unitnamesandcustomdeclarations.containsKey(ds.getUnit().getName())){
					String customname = ds.getUnit().getName();
					semsimmodel.getUnit(customname).setCustomDeclaration(unitnamesandcustomdeclarations.get(customname));
				}
			}
		}
	}
	
	private Element getToolByID(String ID){
		for(Element tool : toolset){
			if(tool.getAttributeValue("id").equals(ID)){
				return tool;
			} 
		}
		System.out.println("Couldn't find tool that matches ID " + ID);
		return null;
	}
	
	private Element getToolToSolveCodeword(String cdwd){
		for(Element tool : toolset){
			List<?> varlist = tool.getChild("solvedVariableList").getChildren("variableUsage");
			Iterator<?> varit = varlist.iterator();
			if(varlist.size() == 1){
				while(varit.hasNext()){
					Element var = (Element) varit.next();
					if(var.getAttributeValue("id").equals(cdwd)){
						return tool;
					}
				}
			}
		}
		System.out.println("Couldn't find tool to solve " + cdwd);
		return null;
	}
	
	
	private Set<String> getRequiredVariablesForTool(String toolid){
		Element tool = getToolByID(toolid);
		Set<String> reqvars = new HashSet<String>();
		Iterator<?> reqvarit = tool.getChild("requiredVariableList").getChildren("variableUsage").iterator();
		while(reqvarit.hasNext()){
			Element reqvar = (Element) reqvarit.next();
			reqvars.add(reqvar.getAttributeValue("id"));
		}
		return reqvars;
	}
	
	
	private String getIC(Element tool, String cdwd){
		// get the tool that sets the IC
		Iterator<?> ICit = tool.getChild("initialConditionList").getChildren("initialCondition").iterator();
		if(tool.getChild("initialConditionList").getChildren("initialCondition").size()>1){
			System.out.println("WARNING: Multiple initial conditions for variable " + cdwd);
			return null;
		}
		while(ICit.hasNext()){
			Element IC = (Element) ICit.next();
			String ICtoolid = IC.getAttributeValue("toolID");
			
			// if there are required variables, get them recursively
			if(getRequiredVariablesForTool(ICtoolid).contains(cdwd + "__init")){
				return getToolToSolveCodeword(cdwd + "__init").getChild("expression").getChild("debug").getText();
			}
			else{
				String eq = getToolByID(ICtoolid).getChild("expression").getChild("debug").getText();
				if(eq.contains("when (")){
					eq = eq.substring(eq.indexOf(")")+1,eq.length());
				}
				return eq;
			}
		}
		return null;
	}
	
	private String[] getIdentifiersInMathMLEquation(Element rootelement) {
		Set<String> inputs = new HashSet<String>();
		
		@SuppressWarnings("rawtypes")
		Iterator descendants = rootelement.getDescendants();

		while (descendants.hasNext()) {
			Object descendant = descendants.next();
			if (descendant instanceof Element) {
				Element el = (Element) descendant;
				if (el.getName().equals("ci") && !inputs.contains(el.getValue())) {
					inputs.add(el.getValue());
				}
			}
		}
		String[] inputsarray = (String[]) inputs.toArray(new String[] {});
		return inputsarray;
	}
}