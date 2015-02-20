package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.CellMLconstants;
import semsim.SemSimConstants;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.model.physical.object.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;
import semsim.writing.CellMLbioRDFblock;

public class CellMLreader extends ModelReader {
	private Namespace mainNS;
	private CellMLbioRDFblock rdfblock;
	private String rdfstring;
	private Element rdfblockelement;
	private Map<String, PhysicalModelComponent> URIandPMCmap = new HashMap<String, PhysicalModelComponent>();
	private String unnamedstring = "[unnamed!]";
	private XMLOutputter xmloutputter = new XMLOutputter();
	
	public CellMLreader(File file) {
		super(file);
	}
	
	public SemSimModel readFromFile() {
		String modelname = srcfile.getName().substring(0, srcfile.getName().indexOf("."));
		semsimmodel.setName(modelname);
		
		xmloutputter.setFormat(Format.getPrettyFormat());
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(srcfile);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			semsimmodel.addError(e.getLocalizedMessage());
			return semsimmodel;
		}
		if(doc.getRootElement()==null){
			semsimmodel.addError("Could not parse original model: Root element of XML document was null");
			return semsimmodel;
		}

		// Add model-level metadata
		semsimmodel.setSourcefilelocation(srcfile.getAbsolutePath());
		semsimmodel.setSemsimversion(sslib.getSemSimVersion());
		
		// Get the namespace that indicates if it is a CellML 1.0 or 1.1 model
		mainNS = doc.getRootElement().getNamespace();
		
		// Get curatorial information
		getModelNameAndIDAndPubMedId(doc, semsimmodel);
		getDocumentation(doc, semsimmodel);

		// Get the main RDF block for the CellML model
		rdfblockelement = getRDFmarkupForElement(doc.getRootElement(), semsimmodel);
		if(rdfblockelement!=null){
			rdfstring = getUTFformattedString(xmloutputter.outputString(rdfblockelement));
		}
		
		rdfblock = new CellMLbioRDFblock(semsimmodel.getNamespace(), rdfstring, mainNS.getURI().toString());
		
		// Get the semsim namespace of the model, if present, according to the rdf block
		String modelnamespacefromrdfblock = rdfblock.rdf.getNsPrefixURI("model");
		if(modelnamespacefromrdfblock !=null ) semsimmodel.setNamespace(modelnamespacefromrdfblock);

		
		// Get imported components
		Iterator<?> importsit = doc.getRootElement().getChildren("import", mainNS).iterator();
		while(importsit.hasNext()){
			Element importel = (Element) importsit.next();
			String hrefValue = importel.getAttributeValue("href", CellMLconstants.xlinkNS);
			
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
						SemSimComponentImporter.importFunctionalSubmodel(srcfile, semsimmodel, localcompname, origcompname, hrefValue, sslib);
				String metadataid = importedcompel.getAttributeValue("id", CellMLconstants.cmetaNS);
				instantiatedsubmodel.setMetadataID(metadataid);

				collectSingularBiologicalAnnotationForSubmodel(doc, instantiatedsubmodel, importedcompel);
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
			if(isbaseunitval!=null || sslib.isCellMLBaseUnit(unitname)){
				uom.setFundamental(true);
			}
			else{
				uom.setFundamental(false);
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
				double exp = (exponent==null) ? 1.0 : Double.parseDouble(exponent);
				UnitOfMeasurement baseuom = semsimmodel.getUnit(baseunits);
				if(baseuom==null){
					baseuom = new UnitOfMeasurement(baseunits);
					baseuom.setFundamental(true);
					semsimmodel.addUnit(baseuom);
				}
				uom.addUnitFactor(new UnitFactor(baseuom, exp, prefix));
			}
		}
		
		// Iterate through all the components, create new members of the SemSim "Submodel" class as we go
		Iterator<?> componentit = doc.getRootElement().getChildren("component", mainNS).iterator();
		while(componentit.hasNext()){
			Element comp = (Element) componentit.next();
			String compname = comp.getAttributeValue("name");
			String metadataid = comp.getAttributeValue("id", CellMLconstants.cmetaNS);
			
			String submodelname = compname;

			// Need to make sure the sub-model name is unique and not taken by a data structure
			while(semsimmodel.getDataStructure(submodelname)!=null || semsimmodel.getCustomPhysicalEntityByName(submodelname)!=null ||
				semsimmodel.getCustomPhysicalProcessByName(submodelname)!=null){
				submodelname = submodelname + "_";
			}
			
			String mathmltext = null;
			List<?> compMathMLelements = comp.getChildren("math", CellMLconstants.mathmlNS);
			if(compMathMLelements!=null){
				mathmltext = xmloutputter.outputString(comp.getChildren("math", CellMLconstants.mathmlNS));
			}
			
			
			// Iterate through variables to find the outputs
			Set<DataStructure> outputs = new HashSet<DataStructure>();
			Set<DataStructure> inputs = new HashSet<DataStructure>();
			Set<DataStructure> unknowns = new HashSet<DataStructure>();
			
			Iterator<?> varit = comp.getChildren("variable", mainNS).iterator();
			
			// Iterate through the variables that are included in the component
			String publicinterface = null;
			String privateinterface = null;
			
			while(varit.hasNext()){
				
				Element var = (Element) varit.next();
				String varname = var.getAttributeValue("name");
				String uniquevarname = compname + "." + varname;
				publicinterface = var.getAttributeValue("public_interface");
				privateinterface = var.getAttributeValue("private_interface");
				String unitstext = var.getAttributeValue("units");
				String initval = var.getAttributeValue("initial_value");
				String varmetaID = var.getAttributeValue("id", CellMLconstants.cmetaNS);
				
				MappableVariable cvar = new MappableVariable(uniquevarname);
				
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
				
				// CHANGE THIS?  What is scope of initial value?
				if(initval!=null) cvar.setCellMLinitialValue(initval);

				cvar.setPhysicalProperty(new PhysicalProperty());
				
				// Set units
				if(unitstext!=null) cvar.setUnit(semsimmodel.getUnit(unitstext));
				
				cvar.setDeclared(true);
				if(varmetaID!=null) cvar.setMetadataID(varmetaID);

				// Collect the singular biological annotation, if present
				if(cvar.getMetadataID()!=null){
					URI termURI = collectSingularBiologicalAnnotation(doc, cvar, var);
					if(termURI!=null){
						String label = null;
						cvar.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, termURI, label);
					}
					
					collectCompositeAnnotation(doc, cvar, var);
					
				}
				semsimmodel.addDataStructure(cvar);
			}
			
			// Add MathML and computational code for each component variable
			varit = comp.getChildren("variable", mainNS).iterator();
			
			while(varit.hasNext()){
				Element var = (Element) varit.next();
				String varname = var.getAttributeValue("name");
				String uniquevarname = compname + "." + varname;

				DataStructure cvar = semsimmodel.getDataStructure(uniquevarname);

				if(compMathMLelements!=null){
					Element varmathmlel = getMathMLforOutputVariable(varname, compMathMLelements);
					if(varmathmlel!=null){
						String varmathml = xmloutputter.outputString(varmathmlel);
						ASTNode ast_result = libsbml.readMathMLFromString(varmathml);
						String ast_as_string = libsbml.formulaToString(ast_result);
						// formulaToString doesn't parse equal signs and differentials 
						if(ast_as_string != null) {
							ast_as_string = ast_as_string.substring(3, ast_as_string.length()-1);
							ast_as_string = ast_as_string.replace(",", " =");
						}
						cvar.getComputation().setMathML(varmathml);
						cvar.getComputation().setComputationalCode(ast_as_string);
						
						// Create the computational dependency network among the component variables
						whiteBoxFunctionalSubmodelEquations(varmathmlel, compname, semsimmodel, cvar);
					}
				}
			}
			
			FunctionalSubmodel submodel = new FunctionalSubmodel(submodelname, outputs);
			
			// Set inputs, outputs, etc. and the computational elements of the submodel component
			for(DataStructure output : outputs) submodel.addDataStructure(output);
			for(DataStructure input : inputs) submodel.addDataStructure(input);
			for(DataStructure unknown : unknowns) submodel.addDataStructure(unknown);
			
			submodel.getComputation().setOutputs(outputs);
			submodel.getComputation().setInputs(inputs);
			
			// For now associate mathml with variables and with components - figure out what to do later (maybe just 
			// keep it with component but get individual variable math as needed?)
			if(mathmltext!=null) submodel.getComputation().setMathML(mathmltext);

			if(metadataid!=null) submodel.setMetadataID(metadataid);
			
			// Collect the biological annotation, if present
			collectSingularBiologicalAnnotationForSubmodel(doc, submodel, comp);
			
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
				
				MappableVariable var1 = (MappableVariable) semsimmodel.getDataStructure(var1name);
				MappableVariable var2 = (MappableVariable) semsimmodel.getDataStructure(var2name);
				
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
					if(var1.getPublicInterfaceValue().equals("out") && var2.getPublicInterfaceValue().equals("in")){
						inputvar = var1;
						outputvar = var2;
					}
					else if(var1.getPublicInterfaceValue().equals("in") && var2.getPublicInterfaceValue().equals("out")){
						inputvar = var2;
						outputvar = var1;
					}
				}
				
				if((inputvar == null || outputvar == null) && encapsulatedsubmodel!=null){
					if(encapsulatedvariable.getPublicInterfaceValue().equals("out") && encapsulatingvariable.getPrivateInterfaceValue().equals("in")){
						inputvar = encapsulatedvariable;
						outputvar = encapsulatingvariable;
					}
					else if(encapsulatedvariable.getPublicInterfaceValue().equals("in") && encapsulatingvariable.getPrivateInterfaceValue().equals("out")){
						inputvar = encapsulatingvariable;
						outputvar = encapsulatedvariable;
					}
				}
				
				if(inputvar==null || outputvar==null){
					semsimmodel.addError("Error mapping " + var1.getName() + " to " + var2.getName() + ": could not arrange an interface based on the variables' input/output designations");
				}
				
				if(inputvar!=null && outputvar!=null){
					inputvar.addVariableMappingTo(outputvar);
					if(outputvar.getComputation()!=null)
						outputvar.getComputation().addInput(inputvar);
				}
			}
		}
		
		// Strip the semsim-related content from the main RDF block
		stripSemSimRelatedContentFromRDFblock(rdfblock.rdf);
		semsimmodel.addAnnotation(new Annotation(SemSimConstants.CELLML_RDF_MARKUP_RELATION, CellMLbioRDFblock.getRDFAsString(rdfblock.rdf)));
		
		return semsimmodel;
	}
	
	
	// Collect singular annotation for model components
	private void collectSingularBiologicalAnnotationForSubmodel(Document doc, FunctionalSubmodel submodel, Element comp){
		if(submodel.getMetadataID()!=null){
			URI termURI = collectSingularBiologicalAnnotation(doc, submodel, comp);
			if(termURI!=null){
				String label = null;
				submodel.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, termURI, label);
			}
		}
	}
	
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
			String id = doc.getRootElement().getAttributeValue("id", CellMLconstants.cmetaNS);
			if(name!=null && !name.equals("")) semsimmodel.setModelAnnotation(Metadata.fullname, id);
			if(id!=null && !id.equals("")) semsimmodel.setModelAnnotation(Metadata.sourcemodelid, id);
			
			// Try to get pubmed ID from RDF tags
			if(doc.getRootElement().getChild("RDF", CellMLconstants.rdfNS)!=null){
				if(doc.getRootElement().getChild("RDF", CellMLconstants.rdfNS).getChildren("Description", CellMLconstants.rdfNS)!=null){
					Iterator<?> descit = doc.getRootElement().getChild("RDF",CellMLconstants.rdfNS).getChildren("Description", CellMLconstants.rdfNS).iterator();
					while(descit.hasNext()){
						Element nextdesc = (Element) descit.next();
						if(nextdesc.getChildren("reference", CellMLconstants.bqsNS)!=null){
							Iterator<?> refit = nextdesc.getChildren("reference", CellMLconstants.bqsNS).iterator();
							while(refit.hasNext()){
								Element nextref = (Element) refit.next();
								if(nextref.getChild("Pubmed_id", CellMLconstants.bqsNS)!=null){
									String pubmedid = nextref.getChild("Pubmed_id", CellMLconstants.bqsNS).getText();
									if(!pubmedid.equals("") && pubmedid!=null){
										ab = pubmedid;
									}
								}
							}
						}
					}
				}
			}
			Boolean process = true;
			try{
				doc.getRootElement().getChild("documentation",CellMLconstants.docNS).getChild("article",CellMLconstants.docNS).getChildren("sect1",CellMLconstants.docNS);
			}
			catch(NullPointerException e){
				System.err.println("Warning: in trying to parse metadata, failed to find sect1 tag in model");
				process = false;
			}
			// Try to get ID from documentation tags if not in RDF
			if(process){
				Iterator<?> sect1it = doc.getRootElement().getChild("documentation",CellMLconstants.docNS).getChild("article",CellMLconstants.docNS).getChildren("sect1",CellMLconstants.docNS).iterator();
				while(sect1it.hasNext()){
					Element nextsect1 = (Element) sect1it.next();
					if(nextsect1.getAttributeValue("id").equals("sec_structure")){
						if(nextsect1.getChildren("para",CellMLconstants.docNS)!=null){
							Iterator<?> it = nextsect1.getChildren("para",CellMLconstants.docNS).iterator();
							while(it.hasNext()){
								Element nextel = (Element) it.next();
								if(nextel.getChildren("ulink",CellMLconstants.docNS).size()!=0){
									Iterator<?> ulinkit = nextel.getChildren("ulink",CellMLconstants.docNS).iterator();
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
			semsimmodel.addAnnotation(new Annotation(CurationalMetadata.REFERENCE_PUBLICATION_PUBMED_ID_RELATION, ab));
	}
	
	
	private void getDocumentation(Document doc, SemSimModel semsimmodel){
		Element docel = doc.getRootElement().getChild("documentation", CellMLconstants.docNS);
		if(docel!=null){
			String text = getUTFformattedString(xmloutputter.outputString(docel));
			semsimmodel.addAnnotation(new Annotation(SemSimConstants.CELLML_DOCUMENTATION_RELATION, text));
		}
	}
	
	
	private Element getRDFmarkupForElement(Element el, SemSimModel semsimmodel){
		Element rdfel = el.getChild("RDF", CellMLconstants.rdfNS);
		return rdfel;
	}
	
	
	// Remove all semsim-related content from the main RDF block
	// It gets replaced, if needed, on write out
	private void stripSemSimRelatedContentFromRDFblock(Model rdf){
		Iterator<Statement> stit = rdf.listStatements();
		List<Statement> listofremovedstatements = new ArrayList<Statement>();
		String modelns = semsimmodel.getNamespace();
		while(stit.hasNext()){
			Statement st = (Statement) stit.next();
			RDFNode obnode = st.getObject();
			if(obnode instanceof Resource){
				Resource obres = (Resource)obnode;
				if(obres.getNameSpace()!=null){
					if(obres.getNameSpace().equals(modelns)){
						listofremovedstatements.add(st);
					}
				}
			}
			Resource subres = st.getSubject();
				if(subres.getNameSpace()!=null){
					if(subres.getNameSpace().equals(modelns)){
						listofremovedstatements.add(st);
					}
				}
			Property pred = st.getPredicate();
			if(pred.getNameSpace()!=null){
				if(pred.getNameSpace().equals(SemSimConstants.SEMSIM_NAMESPACE)){
					listofremovedstatements.add(st);
				}
			}
		}
		rdf.remove(listofremovedstatements);
	}
	
	
	// Collect the reference ontology term used to describe the model component
	private URI collectSingularBiologicalAnnotation(Document doc, SemSimComponent toann, Element el){
		// Look for rdf markup as child of element
		Element mainrdfel = el.getChild("RDF", CellMLconstants.rdfNS);
		// If not present, look for it in main RDF block
		if(mainrdfel==null)
			mainrdfel = rdfblockelement;
		
		URI singularannURI = null;
		if(mainrdfel!=null){
			Iterator<?> descit = mainrdfel.getChildren("Description", CellMLconstants.rdfNS).iterator();
			
			while(descit.hasNext()){
				Element rdfdesc = (Element) descit.next();
				String about = rdfdesc.getAttributeValue("about", CellMLconstants.rdfNS);
				String ID = rdfdesc.getAttributeValue("ID", CellMLconstants.rdfNS);
				String ref = null;
				
				if(about!=null) ref = about.replace("#", "");
				else if(ID!=null) ref = ID;
				
				if(ref!=null){				
					if(ref.equals(toann.getMetadataID())){
						Element relel = rdfdesc.getChild("is", CellMLconstants.bqbNS);
						Element freeel = rdfdesc.getChild("description", CellMLconstants.dctermsNS);
						
						// If there is a singular annotation
						if(relel!=null){
							String term = relel.getAttributeValue("resource", CellMLconstants.rdfNS);
							if(term==null){
								Element objectdescel = relel.getChild("Description", CellMLconstants.rdfNS);
								if(objectdescel!=null){
									term = objectdescel.getAttributeValue("about", CellMLconstants.rdfNS);
									singularannURI = URI.create(term);
								}
							}
							else singularannURI = URI.create(term);
						}
						
						// If there is a free-text description
						if(freeel!=null){
							String freetext = freeel.getText();
							if(freetext!=null) toann.setDescription(freetext);
						}
					}
				}
			}
		}
		return singularannURI;
	}
	
	
	private void collectCompositeAnnotation(Document doc, SemSimComponent toann, Element el){
		
		MappableVariable cvar = (MappableVariable)toann;
		Resource cvarResource = rdfblock.rdf.getResource(mainNS.getURI().toString() + cvar.getMetadataID());
		
		Resource physpropres = null;
		if(cvarResource!=null)
			physpropres = cvarResource.getPropertyResourceValue(CellMLbioRDFblock.compcomponentfor);
		
		// If a physical property is specified for the variable
		if(physpropres!=null && cvarResource!=null){
			URIandPMCmap.put(physpropres.getURI(), cvar.getPhysicalProperty());
			
			Resource isannres = physpropres.getPropertyResourceValue(CellMLbioRDFblock.is);
			if(isannres==null)
				isannres = physpropres.getPropertyResourceValue(CellMLbioRDFblock.refersto);
			
			// If the property is annotated against a reference ontology term
			if(isannres!=null){
				
				// If an identifiers.org OPB namespace was used, replace it with the OPB's
				String tempuri = isannres.getURI();
				if(!isannres.getURI().startsWith(SemSimConstants.OPB_NAMESPACE)){
					tempuri = isannres.getURI();
					String frag = SemSimOWLFactory.getIRIfragment(tempuri);
					tempuri = SemSimConstants.OPB_NAMESPACE + frag;
				}
				cvar.getPhysicalProperty().addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, 
					URI.create(tempuri), tempuri);
			}

			getPMCfromRDFresourceAndAnnotate(physpropres);
			
			Resource propertyofres = physpropres.getPropertyResourceValue(CellMLbioRDFblock.physicalpropertyof);
			
			// If the physical property is a property of something...
			if(propertyofres!=null){
				
				PhysicalModelComponent pmc = getPMCfromRDFresourceAndAnnotate(propertyofres);
				cvar.getPhysicalProperty().setPhysicalPropertyOf(pmc);
				
				// If it is a process
				if(pmc instanceof PhysicalProcess){
					PhysicalProcess process = (PhysicalProcess)pmc;
					cvar.getPhysicalProperty().setPhysicalPropertyOf(pmc);
					NodeIterator sourceit = rdfblock.rdf.listObjectsOfProperty(propertyofres, CellMLbioRDFblock.hassourceparticipant);
					
					// Read in the source participants
					while(sourceit.hasNext()){
						Resource sourceres = (Resource) sourceit.next();
						Resource physentres = sourceres.getPropertyResourceValue(CellMLbioRDFblock.hasphysicalentityreference);
						PhysicalModelComponent sourcepmc = getPMCfromRDFresourceAndAnnotate(physentres);
						Literal multiplier = sourceres.getProperty(CellMLbioRDFblock.hasmultiplier).getObject().asLiteral();
						process.addSource((PhysicalEntity) sourcepmc, multiplier.getDouble());
					}
					// Read in the sink participants
					NodeIterator sinkit = rdfblock.rdf.listObjectsOfProperty(propertyofres, CellMLbioRDFblock.hassinkparticipant);
					while(sinkit.hasNext()){
						Resource sinkres = (Resource) sinkit.next();
						Resource physentres = sinkres.getPropertyResourceValue(CellMLbioRDFblock.hasphysicalentityreference);
						PhysicalModelComponent sinkpmc = getPMCfromRDFresourceAndAnnotate(physentres);
						Literal multiplier = sinkres.getProperty(CellMLbioRDFblock.hasmultiplier).getObject().asLiteral();
						process.addSink((PhysicalEntity) sinkpmc, multiplier.getDouble());
					}
					// Read in the mediator participants
					NodeIterator mediatorit = rdfblock.rdf.listObjectsOfProperty(propertyofres, CellMLbioRDFblock.hasmediatorparticipant);
					while(mediatorit.hasNext()){
						Resource mediatorres = (Resource) mediatorit.next();
						Resource physentres = mediatorres.getPropertyResourceValue(CellMLbioRDFblock.hasphysicalentityreference);
						PhysicalModelComponent mediatorpmc = getPMCfromRDFresourceAndAnnotate(physentres);
						Literal multiplier = mediatorres.getProperty(CellMLbioRDFblock.hasmultiplier).getObject().asLiteral();
						process.addMediator((PhysicalEntity) mediatorpmc, multiplier.getDouble());
					}
				}
			}
		}
	}
	
	private CompositePhysicalEntity buildCompositePhysicalEntityfromRDFresource(Resource propertyofres){
		Boolean cont = true;
		Resource curres = propertyofres;
		
		ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
		PhysicalEntity startent = getCompositeEntityComponentFromResourceAndAnnotate(propertyofres);
		entlist.add(startent); // index physical entity
		
		while(cont){
			Resource entityres = curres.getPropertyResourceValue(CellMLbioRDFblock.containedin);
			
			boolean containedinlink = false;
			if(entityres==null){
				entityres = curres.getPropertyResourceValue(CellMLbioRDFblock.partof);
			}
			else containedinlink = true;
			
			// If the physical entity is linked to another as part of a composite physical entity
			if(entityres!=null){
				PhysicalEntity nextent = getCompositeEntityComponentFromResourceAndAnnotate(entityres);
				entlist.add(nextent);
				if(containedinlink) rellist.add(SemSimConstants.CONTAINED_IN_RELATION);
				else rellist.add(SemSimConstants.PART_OF_RELATION);
				
				curres = entityres;
			}
			else cont=false;
		}
		if(entlist.size()>0 && rellist.size()>0){
			CompositePhysicalEntity cpe = new CompositePhysicalEntity(entlist, rellist);
			return cpe;
		}
		else return null;
	}
	
	
	private PhysicalEntity getCompositeEntityComponentFromResourceAndAnnotate(Resource res){

		// Create a singular physical entity from a component in a composite physical entity
		PhysicalEntity returnent = null;
		
		Resource isannres = res.getPropertyResourceValue(CellMLbioRDFblock.is);
		if(isannres==null) isannres = res.getPropertyResourceValue(CellMLbioRDFblock.refersto);
		Resource isversionofann = res.getPropertyResourceValue(CellMLbioRDFblock.isversionof);
		
		// If a reference entity
		if(isannres!=null)
			 returnent = semsimmodel.addReferencePhysicalEntity(URI.create(isannres.getURI()), isannres.getURI());
		
		// If a custom entity
		else
			returnent = addCustomPhysicalEntityToModel(res);
		
		if(isversionofann!=null)
			returnent.addAnnotation(new ReferenceOntologyAnnotation(SemSimConstants.BQB_IS_VERSION_OF_RELATION, 
					URI.create(isversionofann.getURI()), isversionofann.getURI()));	
		
		return returnent;
	}
		
	
	private PhysicalModelComponent getPMCfromRDFresourceAndAnnotate(Resource res){
		
		// Find the Physical Model Component corresponding to the resource's URI
		// Instantiate, if not present
		Resource isannres = res.getPropertyResourceValue(CellMLbioRDFblock.is);
		if(isannres==null) isannres = res.getPropertyResourceValue(CellMLbioRDFblock.refersto);
		Resource isversionofann = res.getPropertyResourceValue(CellMLbioRDFblock.isversionof);
		
		PhysicalModelComponent pmc = null;
		if(URIandPMCmap.containsKey(res.getURI()))
			pmc = URIandPMCmap.get(res.getURI());
		else{
			// If a physical entity
			if(res.getLocalName().startsWith("entity_")){
				
				// If a composite entity
				if(res.getPropertyResourceValue(CellMLbioRDFblock.containedin)!=null || 
						res.getPropertyResourceValue(CellMLbioRDFblock.partof)!=null)
					pmc = semsimmodel.addCompositePhysicalEntity(buildCompositePhysicalEntityfromRDFresource(res));
				
				// If a reference entity
				else if(isannres!=null)
					pmc = semsimmodel.addReferencePhysicalEntity(URI.create(isannres.getURI()), isannres.getURI());
				
				// If a custom entity
				else{
					pmc = addCustomPhysicalEntityToModel(res);
				}
			}
			else if(res.getLocalName().startsWith("process_")){
				
				// If a reference process
				if(isannres!=null){
					pmc = semsimmodel.addReferencePhysicalProcess(URI.create(isannres.getURI()), isannres.getURI());
				}
				// If a custom process
				else{
					System.out.println(res.getURI());
					String name = res.getProperty(CellMLbioRDFblock.hasname).getString();
					if(name==null) name = unnamedstring;
					String description = res.getProperty(CellMLbioRDFblock.description).getString();
					pmc = semsimmodel.addCustomPhysicalProcess(name, description);
				}
			}
			
			if(isversionofann!=null)
				pmc.addAnnotation(new ReferenceOntologyAnnotation(SemSimConstants.BQB_IS_VERSION_OF_RELATION, 
						URI.create(isversionofann.getURI()), isversionofann.getURI()));				
			
			URIandPMCmap.put(res.getURI(), pmc);
		}
		return pmc;
	}
	
	
	private CustomPhysicalEntity addCustomPhysicalEntityToModel(Resource res){
		String name = res.getProperty(CellMLbioRDFblock.hasname).getString();
		if(name==null) name = unnamedstring;
		String description = res.getProperty(CellMLbioRDFblock.description).getString();
		return semsimmodel.addCustomPhysicalEntity(name, description);
	}
		
	
	protected static Element getMathMLforOutputVariable(String cvarname, List<?> componentMathMLlist){
		Element mathmlheadel = new Element("math", CellMLconstants.mathmlNS);
		Iterator<?> compmathmlit = componentMathMLlist.iterator();
		Boolean foundeq = false;
		Element childel = null;

		while(compmathmlit.hasNext()){
			Element MathMLelement = (Element) compmathmlit.next();
			Iterator<?> applyit = MathMLelement.getChildren("apply", CellMLconstants.mathmlNS).iterator();
			
			while(applyit.hasNext() && !foundeq){
				childel = (Element) applyit.next();
				Element subappel = (Element) childel.getChildren().get(1);
				
				if(subappel.getName().equals("apply")){
					Element ciel = subappel.getChild("ci", CellMLconstants.mathmlNS);
					if(ciel.getText().trim().equals(cvarname)){
						foundeq = true;
					}
				}
				else if(subappel.getName().equals("ci")){
					if(subappel.getText().trim().equals(cvarname))
						foundeq = true;
				}
			}
		}	
			
		if(foundeq){
			mathmlheadel.addContent((Element)childel.clone());
			return mathmlheadel;
		}
		return null;
	}
	
	protected static void whiteBoxFunctionalSubmodelEquations(Element varmathmlel, String compname, SemSimModel semsimmodel, DataStructure cvar){
		Iterator<?> conit = varmathmlel.getDescendants();
		while(conit.hasNext()){
			Content con = (Content) conit.next();
			if(con instanceof Element){
				Element conel = (Element)con;
				if(conel.getName().equals("ci")){
					String inputname = compname + "." + conel.getText().trim();
					DataStructure inputds = null;
					if(semsimmodel.containsDataStructure(inputname)){
						inputds = semsimmodel.getDataStructure(inputname);
					}
					else{
						System.err.println("Equation for " + cvar.getName() + " uses " + inputname + " but that data structure not in model");
					}
					if(inputds!=null){
						cvar.getComputation().addInput(inputds);
					}
				}
			}
		}
	}
	
	private String getUTFformattedString(String str){
		try {
			return new String(str.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}

