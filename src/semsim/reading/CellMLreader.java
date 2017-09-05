package semsim.reading;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.JSBML;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.SemSimUtil;
import semsim.writing.SemSimRDFwriter;

public class CellMLreader extends ModelReader {
	private Namespace mainNS;
	public SemSimRDFreader rdfblock;
	private Element rdfblockelement;
	private static XMLOutputter xmloutputter = new XMLOutputter();
	
	public CellMLreader(File file) {
		super(file);
	}
	
	public CellMLreader(ModelAccessor modelaccessor){
		super(modelaccessor);
	}
	
	public SemSimModel read() {
		
		String srccode = modelaccessor.getLocalModelTextAsString();
		
		xmloutputter.setFormat(Format.getPrettyFormat());
		
		Document doc = getJDOMdocumentFromString(semsimmodel, srccode);
		
		if(doc.getRootElement()==null){
			semsimmodel.addError("Could not parse original model: Root element of XML document was null");
			return semsimmodel;
		}

		// Add model-level metadata
		semsimmodel.setSourceFileLocation(modelaccessor);
		semsimmodel.setSemSimVersion(sslib.getSemSimVersion());
		
		// Get the namespace that indicates if it is a CellML 1.0 or 1.1 model
		mainNS = doc.getRootElement().getNamespace();
		
		// Get curatorial information
		getModelNameAndIDAndPubMedId(doc, semsimmodel);
		getDocumentation(doc, semsimmodel);

		// Get the main RDF block for the CellML model
		rdfblockelement = getRDFmarkupForElement(doc.getRootElement());
		
		String rdfstring = null;
		
		if(rdfblockelement != null)
			rdfstring = getUTFformattedString(xmloutputter.outputString(rdfblockelement));
		
		rdfblock = new SemSimRDFreader(modelaccessor, semsimmodel, rdfstring, ModelType.CELLML_MODEL);
		
		rdfblock.getModelLevelAnnotations();
		
		// Get imported components
		Iterator<?> importsit = doc.getRootElement().getChildren("import", mainNS).iterator();
		
		while(importsit.hasNext()){
			Element importel = (Element) importsit.next();
			String hrefValue = importel.getAttributeValue("href", RDFNamespace.XLINK.createJdomNamespace());
			
			// Load in the referenced units
			Iterator<?> importedunitsit = importel.getChildren("units", mainNS).iterator();
			
			while(importedunitsit.hasNext()){
				Element importedunitel = (Element) importedunitsit.next();
				String localunitname = importedunitel.getAttributeValue("name");
				String origunitname = importedunitel.getAttributeValue("units_ref");
				SemSimComponentImporter.importUnits(semsimmodel, localunitname, origunitname, hrefValue);
			}
			
			Iterator<?> importedcompsit = importel.getChildren("component", mainNS).iterator();
			
			while(importedcompsit.hasNext()){
				Element importedcompel = (Element) importedcompsit.next();
				String localcompname = importedcompel.getAttributeValue("name");
				String origcompname = importedcompel.getAttributeValue("component_ref");
				
				FunctionalSubmodel instantiatedsubmodel = 
						SemSimComponentImporter.importFunctionalSubmodel(
								modelaccessor.getFileThatContainsModel(), 
								semsimmodel, localcompname, origcompname, hrefValue, sslib);
				
				String metadataid = importedcompel.getAttributeValue("id", RDFNamespace.CMETA.createJdomNamespace());
				semsimmodel.assignValidMetadataIDtoSemSimObject(metadataid, instantiatedsubmodel);

			}
		}
		
		// Process units
		Iterator<?> unitit = doc.getRootElement().getChildren("units", mainNS).iterator();
		
		// Collect all units, set whether they are fundamental
		while(unitit.hasNext()){
			Element unit = (Element) unitit.next();
			String unitname = unit.getAttributeValue("name");
			UnitOfMeasurement uom = new UnitOfMeasurement(unitname);
			semsimmodel.addUnit(uom);
			String isbaseunitval = unit.getAttributeValue("base_units");
			
			uom.setFundamental(false);
			if(isbaseunitval!=null || sslib.isCellMLBaseUnit(unitname)){
				uom.setFundamental(true);
			}
		}
		
		unitit = doc.getRootElement().getChildren("units", mainNS).iterator();

		// Process the unit factors
		while(unitit.hasNext()){
			Element unit = (Element) unitit.next();
			String unitname = unit.getAttributeValue("name");
			UnitOfMeasurement uom = semsimmodel.getUnit(unitname);
			Iterator<?> unitfactorit = unit.getChildren("unit",mainNS).iterator();
			
			// Set whether unit is fundamental (is false by default), include unit factors
			// Get the factors for the unit
			while(unitfactorit.hasNext()){
				Element unitfactor = (Element) unitfactorit.next();
				String baseunits = unitfactor.getAttributeValue("units");
				String prefix = unitfactor.getAttributeValue("prefix");
				String exponent = unitfactor.getAttributeValue("exponent");
				
				UnitOfMeasurement baseuom = semsimmodel.getUnit(baseunits);
				if(baseuom==null){
					baseuom = new UnitOfMeasurement(baseunits);
					baseuom.setFundamental(true);
					semsimmodel.addUnit(baseuom);
				}
				double exp = (exponent==null) ? 1.0 : Double.parseDouble(exponent);
				uom.addUnitFactor(new UnitFactor(baseuom, exp, prefix));
			}
		}

		// Iterate through all the components, create new members of the SemSim "Submodel" class as we go
		Iterator<?> componentit = doc.getRootElement().getChildren("component", mainNS).iterator();
		while(componentit.hasNext()){
			Element comp = (Element) componentit.next();
			String compname = comp.getAttributeValue("name");
			String metadataid = comp.getAttributeValue("id", RDFNamespace.CMETA.createJdomNamespace());
			
			String submodelname = compname;

			// Need to make sure the sub-model name is unique and not taken by a data structure
			while(semsimmodel.getAssociatedDataStructure(submodelname)!=null || semsimmodel.getCustomPhysicalEntityByName(submodelname)!=null ||
				semsimmodel.getCustomPhysicalProcessByName(submodelname)!=null){
				submodelname = submodelname + "_";
			}
			
			String mathmltext = null;
			List<?> compMathMLelements = comp.getChildren("math", RDFNamespace.MATHML.createJdomNamespace());
			
			if(compMathMLelements!=null)
				mathmltext = xmloutputter.outputString(comp.getChildren("math", RDFNamespace.MATHML.createJdomNamespace()));
			

			// Iterate through variables to find the outputs
			ArrayList<DataStructure> allvars = new ArrayList<DataStructure>();
			Set<DataStructure> outputs = new HashSet<DataStructure>();
			Set<DataStructure> inputs = new HashSet<DataStructure>();
			Set<DataStructure> unknowns = new HashSet<DataStructure>();
			
			Iterator<?> varit = comp.getChildren("variable", mainNS).iterator();
			
			// Iterate through the variables that are included in the component
			String publicinterface = null;
			String privateinterface = null;
			
			while(varit.hasNext()){
				Element var = (Element) varit.next();
				String uniquevarname = compname + "." + var.getAttributeValue("name");
				publicinterface = var.getAttributeValue("public_interface");
				privateinterface = var.getAttributeValue("private_interface");
				
				
				MappableVariable cvar = new MappableVariable(uniquevarname);
				allvars.add(cvar);
				
				if(publicinterface!=null){
					if(publicinterface.equals("out")){
						outputs.add(cvar);
					}
					else if(publicinterface.equals("in"))
						inputs.add(cvar);
				}
				else{
					unknowns.add(cvar);
					publicinterface = null;
				}
				
				// Set interface values
				if(publicinterface!=null) cvar.setPublicInterfaceValue(publicinterface);
				if(privateinterface!=null) cvar.setPrivateInterfaceValue(privateinterface);
				
				String initval = var.getAttributeValue("initial_value");
				
				// Set the CellML initial value (in CellML this can be used to set an
				// initial condition for an ODE or the value of a static constant. The 
				// SemSim DataStructure.startValue is only for initial conditions.)
				if(initval!=null) cvar.setCellMLinitialValue(initval);
				
				// Set units
				String unitstext = var.getAttributeValue("units");
				
				// If a unit is specified...
				if(unitstext!=null){
					
					// If the specified unit for the variable wasn't already added to the semsim model, 
					// and it's a CellML base unit, assign the new unit
					if(semsimmodel.getUnit(unitstext)==null && sslib.isCellMLBaseUnit(unitstext)){
						UnitOfMeasurement newunit = new UnitOfMeasurement(unitstext);
						newunit.setFundamental(true);
						semsimmodel.addUnit(newunit);
						cvar.setUnit(newunit);
					}
					else cvar.setUnit(semsimmodel.getUnit(unitstext));
				}
				
				cvar.setDeclared(true);
				String varmetaID = var.getAttributeValue("id", RDFNamespace.CMETA.createJdomNamespace());
				
				semsimmodel.assignValidMetadataIDtoSemSimObject(varmetaID, cvar);

				// Collect the biological annotations, if present
				if(cvar.getMetadataID() != null) rdfblock.getDataStructureAnnotations(cvar);

				semsimmodel.addDataStructure(cvar);
			}
			
			// Add MathML and computational code for each component variable
			varit = comp.getChildren("variable", mainNS).iterator();
			
			while(varit.hasNext()){
				Element var = (Element) varit.next();
				String varname = var.getAttributeValue("name");
				String uniquevarname = compname + "." + varname;
				String initval = var.getAttributeValue("initial_value");
				
				DataStructure cvar = semsimmodel.getAssociatedDataStructure(uniquevarname);

				if(compMathMLelements!=null){
					Element varmathmlel = getMathMLforOutputVariable(varname, compMathMLelements);
					
					if(varmathmlel!=null){
						
						// Check if variable is solved using ODE, and set initial_value as start value.
						Boolean ode = isSolvedbyODE(varname, compMathMLelements);
						if(ode) cvar.setStartValue(initval);
						
						String varmathml = xmloutputter.outputString(varmathmlel);
												
						cvar.getComputation().setMathML(varmathml);

						String RHS = getRHSofDataStructureEquation(varmathml, varname);
						
						// formulaToString doesn't parse equal signs and differentials.
						// Not the prettiest fix, but at least it'll make the equations look prettier.
						if(RHS != null) {
							String LHS = ode ? "d(" + varname + ")/dt = " : varname + " = ";
							cvar.getComputation().setComputationalCode(LHS + RHS);
						}
						
						// Create the computational dependency network among the component variables
						whiteBoxFunctionalSubmodelEquations(varmathmlel, compname, semsimmodel, cvar);
					}
				}
				
				if(cvar.getComputation().getComputationalCode().isEmpty() && initval!=null)
					cvar.getComputation().setComputationalCode(varname + " = " + initval);
			}
			
			FunctionalSubmodel submodel = new FunctionalSubmodel(submodelname, outputs);
			
			// Set inputs, outputs, etc. and the computational elements of the submodel component
			submodel.setAssociatedDataStructures(allvars);;
			
			submodel.getComputation().setOutputs(outputs);
			submodel.getComputation().setInputs(inputs);
			
			// For now associate mathml with variables and with components - figure out what to do later (maybe just 
			// keep it with component but get individual variable math as needed?)
			if(mathmltext!=null) submodel.getComputation().setMathML(mathmltext);

			semsimmodel.assignValidMetadataIDtoSemSimObject(metadataid, submodel);
			
			// Collect the free text annotation for the component
			rdfblock.collectFreeTextAnnotation(submodel, 
					rdfblock.rdf.getResource(SemSimRDFreader.TEMP_NAMESPACE + "#" + submodel.getMetadataID()));		
			semsimmodel.addSubmodel(submodel);
		}
		
		// Process the CellML groupings
		Iterator<?> groupit = doc.getRootElement().getChildren("group", mainNS).iterator();
		
		while(groupit.hasNext()){
			Element group = (Element) groupit.next();
			String rel = group.getChild("relationship_ref", mainNS).getAttributeValue("relationship");
			Iterator<?> compit = group.getChildren("component_ref", mainNS).iterator();
			
			while(compit.hasNext()){
				Element topcomp = (Element) compit.next();
				processComponentRelationships(rel, topcomp);
			}
		}
		
		// Process the CellML connections
		Iterator<?> conit = doc.getRootElement().getChildren("connection", mainNS).iterator();
		
		while(conit.hasNext()){
			Element con = (Element) conit.next();
			Element compmap = con.getChild("map_components", mainNS);
			FunctionalSubmodel sub1 = (FunctionalSubmodel) semsimmodel.getSubmodel(compmap.getAttributeValue("component_1"));
			FunctionalSubmodel sub2 = (FunctionalSubmodel) semsimmodel.getSubmodel(compmap.getAttributeValue("component_2"));
			
			Iterator<?> varconit = con.getChildren("map_variables", mainNS).iterator();
			
			while(varconit.hasNext()){
				
				Element varcon = (Element) varconit.next();
				
				String var1name = sub1.getName() + "." + varcon.getAttributeValue("variable_1");
				String var2name = sub2.getName() + "." + varcon.getAttributeValue("variable_2");
				
				MappableVariable var1 = (MappableVariable) semsimmodel.getAssociatedDataStructure(var1name);
				MappableVariable var2 = (MappableVariable) semsimmodel.getAssociatedDataStructure(var2name);
				
				Submodel encapsulatedsubmodel = null;
				MappableVariable encapsulatedvariable = null;
				MappableVariable encapsulatingvariable = null;
				
				if(sub1.getRelationshipSubmodelMap().containsKey("encapsulation")){
					
					for(Submodel sub : sub1.getRelationshipSubmodelMap().get("encapsulation")){
						
						if(sub==sub2){
							encapsulatedsubmodel = sub2;
							encapsulatedvariable = var2;
							encapsulatingvariable = var1;
						}
					}
				}
				if(sub2.getRelationshipSubmodelMap().containsKey("encapsulation")){
					
					for(Submodel sub : sub2.getRelationshipSubmodelMap().get("encapsulation")){
						
						if(sub==sub1){
							encapsulatedsubmodel = sub1;
							encapsulatedvariable = var1;
							encapsulatingvariable = var2;
						}
					}
				}
				
				MappableVariable inputvar = null;
				MappableVariable outputvar = null;
				
				if(var1.getPublicInterfaceValue()!=null && var2.getPublicInterfaceValue()!=null){
					
					if( ! var1.getPublicInterfaceValue().equals("in") && var2.getPublicInterfaceValue().equals("in")){
						inputvar = var1;
						outputvar = var2;
					}
					
					else if(var1.getPublicInterfaceValue().equals("in") && ! var2.getPublicInterfaceValue().equals("in")){
						inputvar = var2;
						outputvar = var1;
					}
				}
				
				if((inputvar == null || outputvar == null) && encapsulatedsubmodel!=null){
					if( ! encapsulatedvariable.getPublicInterfaceValue().equals("in") && encapsulatingvariable.getPrivateInterfaceValue().equals("in")){
						inputvar = encapsulatedvariable;
						outputvar = encapsulatingvariable;
					}
					else if(encapsulatedvariable.getPublicInterfaceValue().equals("in") && ! encapsulatingvariable.getPrivateInterfaceValue().equals("in")){
						inputvar = encapsulatingvariable;
						outputvar = encapsulatedvariable;
					}
				}
				
				if(inputvar==null || outputvar==null){
					semsimmodel.addError("Error mapping " + var1.getName() + " to " + var2.getName() + ": could not arrange an interface based on the variables' input/output designations");
				}
				else {
					inputvar.addVariableMappingTo(outputvar);
					if(outputvar.getComputation()!=null)
					outputvar.getComputation().addInput(inputvar);
				}
			}
		}
		
		// If there's a variable called "time" in a component called "environment"
		// set it as a solution domain for the other variables in the model
		
		String soldomname = "environment.time";
		if(semsimmodel.containsDataStructure(soldomname)){
			DataStructure soldomds = semsimmodel.getAssociatedDataStructure(soldomname);
			soldomds.setIsSolutionDomain(true);
			
			for(DataStructure dstruct : semsimmodel.getAssociatedDataStructures()){
				
				if(dstruct != soldomds) dstruct.setSolutionDomain(soldomds);
			}
		}
		
		// Collect info about SemSim style submodels
		rdfblock.getAllSemSimSubmodelAnnotations();
		
		// Strip the semsim-related content from the main RDF block
		stripSemSimRelatedContentFromRDFblock(rdfblock.rdf);
		String remainingrdf = SemSimRDFwriter.getRDFmodelAsString(rdfblock.rdf);
		semsimmodel.addAnnotation(new Annotation(SemSimRelation.CELLML_RDF_MARKUP, remainingrdf));
				
		return semsimmodel;
	}
	
	
	// Collect singular annotation for model components
//	private void collectSingularBiologicalAnnotationForSubmodel(FunctionalSubmodel submodel){
//		if(submodel.getMetadataID()!=null){
//			URI termURI = rdfblock.collectSingularBiologicalAnnotation(submodel);
//			if(termURI!=null){
//				ReferencePhysicalEntity rpe = new ReferencePhysicalEntity(termURI, termURI.toString());
//				semsimmodel.addReferencePhysicalEntity(rpe);
//				submodel.setSingularAnnotation(rpe);
//			}
//		}
//	}
	
	private void processComponentRelationships(String rel, Element comp){
		Submodel parentsubmodel = semsimmodel.getSubmodel(comp.getAttributeValue("component"));
		Iterator<?> subcompit = comp.getChildren("component_ref", mainNS).iterator();
		while(subcompit.hasNext()){
			Element subcomp = (Element) subcompit.next();
			Submodel childsubmodel = semsimmodel.getSubmodel(subcomp.getAttributeValue("component"));
			
			// If both components have corresponding FunctionalSubmodels in the SemSimModel
			if(parentsubmodel instanceof FunctionalSubmodel && childsubmodel instanceof FunctionalSubmodel){
				Set<FunctionalSubmodel> valueset = ((FunctionalSubmodel)parentsubmodel).getRelationshipSubmodelMap().get(rel);
				
				// If we haven't associated a FunctionalSubmodel with this relationship type yet, create new value set, add value
				if(valueset==null) valueset = new HashSet<FunctionalSubmodel>();
				valueset.add((FunctionalSubmodel)childsubmodel);
				
				// Connect the parent and child submodels
				((FunctionalSubmodel)parentsubmodel).getRelationshipSubmodelMap().put(rel, valueset);
				
				// Iterate recursively
				processComponentRelationships(rel, subcomp);
			}
		}
	}
	
	private void getModelNameAndIDAndPubMedId(Document doc, SemSimModel semsimmodel){
		String ab = "";
		if(doc.getRootElement()!=null){
			String name = doc.getRootElement().getAttributeValue("name");
			String id = doc.getRootElement().getAttributeValue("id", RDFNamespace.CMETA.createJdomNamespace());
			if(name!=null && !name.equals("")){
				semsimmodel.setModelAnnotation(Metadata.fullname, name);
				semsimmodel.setName(name);
			}
			if(id!=null && !id.equals("")){
				
				//TODO: this is duplicating information, maybe cut one?
				semsimmodel.setMetadataID(id);
				semsimmodel.setModelAnnotation(Metadata.sourcemodelid, id);
			}
			
			// Try to get pubmed ID from RDF tags
			if(doc.getRootElement().getChild("RDF", RDFNamespace.RDF.createJdomNamespace())!=null){
				if(doc.getRootElement().getChild("RDF", RDFNamespace.RDF.createJdomNamespace()).getChildren("Description", RDFNamespace.RDF.createJdomNamespace())!=null){
					Iterator<?> descit = doc.getRootElement().getChild("RDF",RDFNamespace.RDF.createJdomNamespace()).getChildren("Description", RDFNamespace.RDF.createJdomNamespace()).iterator();
					while(descit.hasNext()){
						Element nextdesc = (Element) descit.next();
						if(nextdesc.getChildren("reference", RDFNamespace.BQS.createJdomNamespace())!=null){
							Iterator<?> refit = nextdesc.getChildren("reference", RDFNamespace.BQS.createJdomNamespace()).iterator();
							while(refit.hasNext()){
								Element nextref = (Element) refit.next();
								if(nextref.getChild("Pubmed_id", RDFNamespace.BQS.createJdomNamespace())!=null){
									String pubmedid = nextref.getChild("Pubmed_id", RDFNamespace.BQS.createJdomNamespace()).getText();
									if(!pubmedid.equals("") && pubmedid!=null){
										ab = pubmedid;
									}
								}
							}
						}
					}
				}
			}
			Boolean process = false;
			try{
				doc.getRootElement().getChild("documentation",RDFNamespace.DOC.createJdomNamespace()).getChild("article",RDFNamespace.DOC.createJdomNamespace()).getChildren("sect1",RDFNamespace.DOC.createJdomNamespace());
				process = true;
			}
			catch(NullPointerException e){
				System.err.println("Warning: in trying to parse metadata, failed to find sect1 tag in model");
			}
			// Try to get ID from documentation tags if not in RDF
			if(process){
				Iterator<?> sect1it = doc.getRootElement().getChild("documentation",RDFNamespace.DOC.createJdomNamespace()).getChild("article",RDFNamespace.DOC.createJdomNamespace()).getChildren("sect1",RDFNamespace.DOC.createJdomNamespace()).iterator();
				while(sect1it.hasNext()){
					Element nextsect1 = (Element) sect1it.next();
					if(nextsect1.getAttributeValue("id").equals("sec_structure")){
						if(nextsect1.getChildren("para",RDFNamespace.DOC.createJdomNamespace())!=null){
							Iterator<?> it = nextsect1.getChildren("para",RDFNamespace.DOC.createJdomNamespace()).iterator();
							while(it.hasNext()){
								Element nextel = (Element) it.next();
								if(nextel.getChildren("ulink",RDFNamespace.DOC.createJdomNamespace()).size()!=0){
									Iterator<?> ulinkit = nextel.getChildren("ulink",RDFNamespace.DOC.createJdomNamespace()).iterator();
									while(ulinkit.hasNext()){
										Element nextulink = (Element) ulinkit.next();
										if(nextulink.getText().toLowerCase().contains("pubmed id") || nextulink.getText().toLowerCase().contains("pubmedid")){
											ab = nextulink.getText();
											ab = ab.substring(ab.indexOf(":") + 1, ab.length()).trim();
										}
									}
								}
							}
						}
					}
				}
			}
			// end documentation processing
		}
		if(ab!=null && !ab.equals(""))
			semsimmodel.setModelAnnotation(Metadata.pubmedid, ab);
	}
	
	
	private void getDocumentation(Document doc, SemSimModel semsimmodel){
		Element docel = doc.getRootElement().getChild("documentation", RDFNamespace.DOC.createJdomNamespace());
		if(docel!=null){
			String text = getUTFformattedString(xmloutputter.outputString(docel));
			semsimmodel.addAnnotation(new Annotation(SemSimRelations.SemSimRelation.CELLML_DOCUMENTATION, text));
		}
	}
	
	
	public static Element getRDFmarkupForElement(Element el){
		return el.getChild("RDF", RDFNamespace.RDF.createJdomNamespace());
	}
	
	
	// Remove all semsim-related content from the main RDF block
	// It gets replaced, if needed, on write out
	private void stripSemSimRelatedContentFromRDFblock(Model rdf){
		
		// Currently getting rid of anything with semsim predicates
		// and descriptions on variables and components
		// AND
		// * part-of statements for physical entities (prbly need to assign metaids to physical entities)
		// * isVersionOf statements on custom terms
		// * has-part statements on custom terms
		// Test to make sure we're not preserving extraneous stuff in CellMLRDFmarkup block
		// within SemSim models (test with all kinds of anns)
		// MAYBE THE RIGHT WAY TO DO THIS IS TO USE THE METAIDS/??
		
		Iterator<Statement> stit = rdf.listStatements();
		List<Statement> listofremovedstatements = new ArrayList<Statement>();
		
		// Go through all statements in RDF
		while(stit.hasNext()){
			Statement st = (Statement) stit.next();
			String rdfprop = st.getPredicate().getURI();
			
			// Flag any statement that uses a predicate with a semsim namespace for removal
			if(rdfprop.startsWith(RDFNamespace.SEMSIM.getNamespaceasString())
					|| rdfprop.equals(StructuralRelation.PART_OF.getURIasString())
					|| rdfprop.equals(StructuralRelation.HAS_PART.getURIasString())
					|| rdfprop.equals(SemSimRelation.BQB_IS_VERSION_OF.getURIasString())
					|| rdfprop.equals(SemSimRelation.BQB_HAS_PART.getURIasString())  // Adding in the BQB structural relations here for good measure, even though we're not currently using them
					|| rdfprop.equals(SemSimRelation.BQB_IS_PART_OF.getURIasString())){
				listofremovedstatements.add(st);
				continue;
			}
			
			Resource subject = st.getSubject();
			
			if(subject.getURI() != null){
				
				if(subject.getURI().contains(SemSimRDFreader.TEMP_NAMESPACE + "#")){
					
					// Look up the SemSimObject associated with the URI fragment (should be the metaid of the RDF Subject)
					SemSimObject sso = semsimmodel.getModelComponentByMetadataID(subject.getLocalName());
					
					if (sso!=null){
						
						// Remove dc:description statements (do not need to preserve these)
						if((sso instanceof DataStructure || sso instanceof Submodel) && rdfprop.equals(SemSimRDFwriter.dcterms_description.getURI()))
							listofremovedstatements.add(st);
					}
				}
			}
			
		}
		
		rdf.remove(listofremovedstatements);
	}
	
	
	// Wraps a cloned version of the mathML element that solves a component variable inside a parent mathML element
	public static Element getMathMLforOutputVariable(String cvarname, List<?> componentMathMLlist){
		
		Element mathmlheadel = new Element("math", RDFNamespace.MATHML.getNamespaceasString());
		Iterator<?> compmathmlit = componentMathMLlist.iterator();
		Element childel = getElementForOutputVariableFromComponentMathML(cvarname, compmathmlit);
				
		if(childel!=null){
			
			Element childelclone = (Element)childel.clone();
			childelclone.removeNamespaceDeclaration(Namespace.getNamespace(RDFNamespace.MATHML.getNamespaceasString()));
			mathmlheadel.addContent(childelclone);
			
			return mathmlheadel;
		}
		return null;
	}
	
	
	// Returns the mathML Element representing the equation for the specified variable
	public static Element getElementForOutputVariableFromComponentMathML(String cvarname, Iterator<?> compmathmlit){
		
		Element childel = null;
		
		while(compmathmlit.hasNext()){
			Element MathMLelement = (Element) compmathmlit.next();
			Iterator<?> applyit = MathMLelement.getChildren("apply", RDFNamespace.MATHML.createJdomNamespace()).iterator();
			
			while(applyit.hasNext()){
				childel = (Element) applyit.next();
				Element subappel = (Element) childel.getChildren().get(1);
				
				if(subappel.getName().equals("apply")){
					Element ciel = subappel.getChild("ci", RDFNamespace.MATHML.createJdomNamespace());
					if(ciel.getText().trim().equals(cvarname)){
						return childel;
					}
				}
				else if(subappel.getName().equals("ci")){
					
					if(subappel.getText().trim().equals(cvarname))
						return childel;
				}
			}
		}	
		return null; // If we are here we didn't find the MathML element for the output variable
	}
	
	
	protected static Boolean isSolvedbyODE(String cvarname, List<?> componentMathMLlist){
		Iterator<?> compmathmlit = componentMathMLlist.iterator();
		Element childel = null;

		while(compmathmlit.hasNext()){
			Element MathMLelement = (Element) compmathmlit.next();
			Iterator<?> applyit = MathMLelement.getChildren("apply", RDFNamespace.MATHML.createJdomNamespace()).iterator();
			
			while(applyit.hasNext()){
				childel = (Element) applyit.next();
				Element subappel = (Element) childel.getChildren().get(1);
				
				if(subappel.getName().equals("apply")){
					Element ciel = subappel.getChild("ci", RDFNamespace.MATHML.createJdomNamespace());
					Element diffeq = subappel.getChild("diff", RDFNamespace.MATHML.createJdomNamespace());
					if(ciel.getText().trim().equals(cvarname) && diffeq != null){
						return true;
					}
				}
			}
		}	
		return false;
	}
	
	protected static void whiteBoxFunctionalSubmodelEquations(Element varmathmlel, String compname, SemSimModel semsimmodel, DataStructure cvar){
		Iterator<?> conit = varmathmlel.getDescendants();
		while(conit.hasNext()){
			Content con = (Content) conit.next();
			if(con instanceof Element){
				Element conel = (Element)con;
				if(conel.getName().equals("ci")){
					String inputname = compname + "." + conel.getText().trim();
					
					// If the input and output are not the same DataStructure...
					if(! inputname.equals(cvar.getName())){
						DataStructure inputds = null;
						
						// If the input is actually in the model...
						if(semsimmodel.containsDataStructure(inputname) && !inputname.equals(cvar.getName())){
							inputds = semsimmodel.getAssociatedDataStructure(inputname);
						}
						else{
							System.err.println("Equation for " + cvar.getName() + " uses " + inputname + " but that data structure not in model");
						}
						
						// Add the input DataStructure
						if(inputds!=null){
							cvar.getComputation().addInput(inputds);
						}
					}
				}
			}
		}
	}
	
	
	public static String getRHSofDataStructureEquation(String varmathml, String varname){
		
		String varmathmlRHS = SemSimUtil.getRHSofMathML(varmathml, varname);
		ASTNode ast_result = JSBML.readMathMLFromString(varmathmlRHS);
		
		return JSBML.formulaToString(ast_result);
	}
	
	
	protected static String getUTFformattedString(String str){
		try {
			return new String(str.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}

