package semsim.writing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import semsim.annotation.Annotation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.fileaccessors.OMEXAccessor;
import semsim.model.Importable;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.ErrorLog;
import semsim.utilities.SemSimUtil;

/**
 * Class for writing out CellML model files from {@link SemSimModel} objects
 * @author mneal
 *
 */
public class CellMLwriter extends ModelWriter {
	private Namespace mainNS;
	private AbstractRDFwriter rdfwriter;
	private Element root;
	private static String timedomainname = "time";
	private Map<String,String> oldAndNewUnitNameMap = new HashMap<String,String>();
	private XMLOutputter outputter = new XMLOutputter();

	
	public CellMLwriter(SemSimModel model) {
		super(model);
	}
	
	
	@Override
	public String encodeModel() {
		
		Document doc = null;
		outputter.setFormat(Format.getPrettyFormat());
		
		mainNS = Namespace.getNamespace(RDFNamespace.CELLML1_1.getNamespaceAsString());
		
		createRDFBlock();
		createRootElement();
		
		SemSimUtil.createUnitNameMap(semsimmodel,oldAndNewUnitNameMap);
		
		doc = new Document(root);
		
		rdfwriter.setRDFforModelLevelAnnotations();
		
		declareImports();
		
		// Add the documentation element
		for(Annotation ann : semsimmodel.getAnnotations()){
			if(ann.getRelation()==SemSimRelation.CELLML_DOCUMENTATION){
				root.addContent(makeXMLContentFromString((String)ann.getValue()));
			}
		}
		
		declareUnits();
		declareSemSimSubmodels(); // this needs to go before we add variables to output b/c we may need to assign new metadata ID's to variables and components
		declareComponentsandVariables();
		declareGroupings();
		declareConnections();
		
		// Add the RDF metadata, if we are writing to a standalone CellML file
		if( ! rdfwriter.rdf.isEmpty() && ! (getWriteLocation() instanceof OMEXAccessor)){
			
			String rawrdf = AbstractRDFwriter.getRDFmodelAsString(rdfwriter.rdf, "RDF/XML-ABBREV");
			Content newrdf = makeXMLContentFromString(rawrdf);
			
			if(newrdf!=null) root.addContent(newrdf);
		}

        return outputter.outputString(doc);
	}
	
	
	//*************WRITE PROCEDURE********************************************//
	
	/**
	 * Assign either a CASAwriter or SemSimRDFwriter for serializing the 
	 * RDF content associated with the CellML model. The CASAwriter is used
	 * if the CellML model is being written out as part of an OMEX archive,
	 * and a SemSimRDFwriter is used if the model is being written out in a
	 * standalone CellML file.
	 */
	private void createRDFBlock() {
		
		// If we're writing to an OMEX file, make the RDF writer a CASAwriter that follows COMBINE conventions
		if(getWriteLocation() instanceof OMEXAccessor){
			rdfwriter = new CASAwriter(semsimmodel);
			rdfwriter.setXMLbase("./" + getWriteLocation().getFileName() + "#");
		}
		// Otherwise use a SemSimRDFwriter
		else{
			String rdfstring = null;
			for(Annotation ann : semsimmodel.getAnnotations()){
				if(ann.getRelation()==SemSimRelation.CELLML_RDF_MARKUP){
					rdfstring = (String) ann.getValue();
					break;
				}
			}
			
			rdfwriter = new SemSimRDFwriter(semsimmodel, rdfstring, ModelType.CELLML_MODEL);
		}
	}
	
	
	/** Add the root "model" element to the CellML JDOM object, declare namespaces
	 * and set attributes such as model name and ID */
	private void createRootElement() {		
		root = new Element("model",mainNS);
		root.addNamespaceDeclaration(RDFNamespace.CMETA.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.XLINK.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.RDF.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.BQS.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.SEMSIM.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.DCTERMS.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.VCARD.createJdomNamespace());
		
		String namestring = semsimmodel.hasName() ? semsimmodel.getName() : "model0";
		
		root.setAttribute("name", namestring);
		
		String metaid = semsimmodel.hasMetadataID() ? semsimmodel.getMetadataID() : 
			semsimmodel.assignValidMetadataIDtoSemSimObject("metaid0", semsimmodel);
		
		root.setAttribute("id", metaid, RDFNamespace.CMETA.createJdomNamespace());
	}
	
	
	/** Declare all imported content in the CellML model */
	private void declareImports() {
		// Declare the imports
		Set<Element> importelements = new HashSet<Element>();
		Set<Importable> importablecomponents = new HashSet<Importable>();
		importablecomponents.addAll(semsimmodel.getFunctionalSubmodels());
		importablecomponents.addAll(semsimmodel.getUnits());
		
		for(Importable ssc : importablecomponents){
			Element importel = null;
			Element importedpiece = null;

			if(ssc.isImported() && ssc.getParentImport()==null){
				// Collecting all imports from a common model does not work, 
				// need to create a new import element for each imported piece
				String hrefVal = ssc.getHrefValue();
				importel = new Element("import", mainNS);
				importel.setAttribute("href", hrefVal, RDFNamespace.XLINK.createJdomNamespace());
				importelements.add(importel); // make a set of elements
				
				String importedpiecetagname = null;
				String importedpiecerefattr = null;
				
				if(ssc instanceof FunctionalSubmodel){
					importedpiecetagname = "component";
					importedpiecerefattr = "component_ref";
				}
				else if(ssc instanceof UnitOfMeasurement){
					importedpiecetagname = "units";
					importedpiecerefattr = "units_ref";
				}
				importedpiece = new Element(importedpiecetagname, mainNS);
				importedpiece.setAttribute("name", ssc.getLocalName());
				importedpiece.setAttribute(importedpiecerefattr, ssc.getReferencedName());
				
				// Add the RDF block for any singular reference ontology annotations and free-text descriptions
				if(ssc instanceof DataStructure) rdfwriter.setRDFforDataStructureAnnotations((DataStructure)ssc);
				
				//TODO: might need to rethink the data that gets written out for submodels
				else if(ssc instanceof Submodel) rdfwriter.setRDFforSubmodelAnnotations((Submodel)ssc);
			}
			if(importel!=null && importedpiece!=null){
				importel.addContent(importedpiece);
			}
		}
		
		root.addContent(importelements);
	}
	
	
	/** Declare all units in the model */
	private void declareUnits() {
		for(UnitOfMeasurement uom : semsimmodel.getUnits()){
			
			if(!sslib.isCellMLBaseUnit(uom.getName()) && !uom.isImported()){
				
				Element unitel = new Element("units", mainNS);
				
				// Convert name into CellML-friendly version
				
				String unitname = oldAndNewUnitNameMap.get(uom.getName());
				
				unitel.setAttribute("name", unitname);
				
				if(uom.isFundamental()) unitel.setAttribute("base_units", "yes");
				
				else{
					
					for(UnitFactor factor : uom.getUnitFactors()){
						Element factorel = new Element("unit", mainNS);
						
						if(factor.getExponent()!=1.0 && factor.getExponent()!=0.0)
							factorel.setAttribute("exponent", Double.toString(factor.getExponent()));
						
						if(factor.getPrefix()!=null)
							factorel.setAttribute("prefix", factor.getPrefix());
						
						if(factor.getMultiplier()!=1.0 && factor.getMultiplier()!=0.0)
							factorel.setAttribute("multiplier", Double.toString(factor.getMultiplier()));
						
						String baseunitname = oldAndNewUnitNameMap.get(factor.getBaseUnit().getName());
						factorel.setAttribute("units", baseunitname);
						unitel.addContent(factorel);
					}
				}
				root.addContent(unitel);
			}
		}
	}
	
	
	/** Declare all components and their variables in the model */
	private void declareComponentsandVariables() {

		// If there are no functional submodels, then create a new one that houses all the data structures
		if(semsimmodel.getFunctionalSubmodels().size()==0){
			
			Set<DataStructure> outputset = new HashSet<DataStructure>();
			outputset.addAll(semsimmodel.getAssociatedDataStructures());
			
			String modelname = semsimmodel.getName();
			modelname.replaceAll(".", "_");
			
			FunctionalSubmodel maincomponent = new FunctionalSubmodel(modelname, outputset);
			
			maincomponent.setAssociatedDataStructures(semsimmodel.getDeclaredDataStructures()); // leave out undeclared data structures that can show up in JSim models
			
			
			// Make sure that no variables have "." in their names (need a full flattening of the model)
			for(DataStructure ds : maincomponent.getAssociatedDataStructures()){

				if(ds.getName().contains(".")){
					
					String oldname = ds.getName();
					String newname = oldname.replace(".", "_");
					
					// if model already contains a data structure with the new name, use prefix name without "."
					Integer x = 0;
					while(semsimmodel.containsDataStructure(newname)){
						newname = newname + x.toString();
						x++;
					}
					ds.setName(newname);
					SemSimUtil.replaceCodewordInAllEquations(ds, ds, semsimmodel, oldname, newname, Pair.of(1.0, "*"));					
				}
			}
			
			// Collect all the mathml for the single component
			String mathml = "";

			for(DataStructure ds : maincomponent.getAssociatedDataStructures()){
				
				if(ds.getComputation().hasMathML()){
					
					// Make sure the MathML contains a left-hand side
					String dsmathml = ds.getComputation().getMathML();
				
					if( ! SemSimUtil.mathmlHasLHS(dsmathml))
						dsmathml = SemSimUtil.addLHStoMathML(dsmathml, ds.getName(), ds.hasStartValue(), timedomainname);
					
					// Make sure that any in-equation unit declarations are CellML-compliant
					int index = dsmathml.indexOf("units=\"");
					while(index >= 0) {
						// Get the unit name in the first two quotes
					   String submathml = dsmathml.substring(index+7, dsmathml.length()-1);
					   int quoteindex = submathml.indexOf("\"");
					   String oldunitname = submathml.substring(0, quoteindex);
					   
					   if(oldAndNewUnitNameMap.containsKey(oldunitname)){
						   String newunitname = oldAndNewUnitNameMap.get(oldunitname);
						   dsmathml = dsmathml.replace("units=\"" + oldunitname + "\"", "units=\"" + newunitname + "\"");
					   }
					   
					   index = submathml.indexOf(dsmathml, index+1);
					}
					
					// OpenCOR doesn't like "integer" type declarations in MathML so get rid of them
					dsmathml = dsmathml.replace("<cn type=\"integer\">","<cn>");
					
					mathml = mathml + dsmathml + "\n";
				}
			}
			maincomponent.getComputation().setMathML(mathml);
			processFunctionalSubmodel(maincomponent, false);
		}
		
		// Otherwise process each CellML-style submodel
		else{
			for(Submodel submodel : semsimmodel.getSubmodels()){
				if(submodel.isFunctional()){
					processFunctionalSubmodel((FunctionalSubmodel) submodel, true);
				}
			}
		}
	}
	
	
	/** Declare groupings between components in the model */
	private void declareGroupings() {
		Set<CellMLGrouping> groupings = new HashSet<CellMLGrouping>();
		
		for(Submodel parentsub : semsimmodel.getSubmodels()){
			
			if(parentsub.isFunctional()){
				
				if( ! ((FunctionalSubmodel)parentsub).isImported()){
					
					for(String rel : ((FunctionalSubmodel)parentsub).getRelationshipSubmodelMap().keySet()){
						
						// Find the grouping
						CellMLGrouping group = null;
						
						for(CellMLGrouping g : groupings){
							if(g.rel.equals(rel)) group = g;
						}
						
						if(group==null){
							group = new CellMLGrouping(rel);
							groupings.add(group);
						}
						
						// Create the parent element
						Element parentel = group.submodelelementmap.get(parentsub);
						if(parentel == null){
							parentel = new Element("component_ref", mainNS);
							parentel.setAttribute("component", parentsub.getName());
							group.submodelelementmap.put((FunctionalSubmodel) parentsub, parentel);
						}
						
						try{
							// Link child elements to the parent
							for(FunctionalSubmodel childsub : ((FunctionalSubmodel)parentsub).getRelationshipSubmodelMap().get(rel)){
								
								if(childsub != null){ // child submodel may have been set to null during extraction process
									Element childelement = group.submodelelementmap.get(childsub);
									if(childelement==null) childelement = new Element("component_ref", mainNS);
									childelement.setAttribute("component", childsub.getName());
									parentel.addContent(childelement);
									group.submodelelementmap.put((FunctionalSubmodel) childsub, childelement);
								}
							}
						}
						catch(Exception ex){
							ex.printStackTrace();
						}
						
						
						
					}
				}
			}
		}
		
		// Go through all the groupings we created and put them in the XML doc
		for(CellMLGrouping group : groupings){
			Element groupel = new Element("group", mainNS);
			Element relationel = new Element("relationship_ref", mainNS);
			relationel.setAttribute("relationship", group.rel);
			groupel.addContent(relationel);
			
			for(FunctionalSubmodel sub : group.submodelelementmap.keySet()){
				Element el = group.submodelelementmap.get(sub);
				if(el.getParentElement()==null){
					groupel.addContent(el);
				}
			}
			root.addContent(groupel);
		}
	}
	
	
	/** Declare all variable mappings in the model */
	private void declareConnections() {
		Set<CellMLConnection> connections = new HashSet<CellMLConnection>();
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			if(ds.isMapped()){
				MappableVariable var1 = (MappableVariable)ds;
				for(MappableVariable mappedvar : var1.getMappedTo()){
					FunctionalSubmodel sub1 = semsimmodel.getParentFunctionalSubmodelForMappableVariable(var1);
					FunctionalSubmodel sub2 = semsimmodel.getParentFunctionalSubmodelForMappableVariable(mappedvar);
					
					if(sub1!=null && sub2!=null){
						if(sub1.getParentImport()==null && sub2.getParentImport()==null){
							CellMLConnection con = null;
							for(CellMLConnection testcon : connections){
								if(testcon.sub1==sub1 && testcon.sub2==sub2) con = testcon;
							}
							if(con==null){  
								con = new CellMLConnection(sub1, sub2);
								connections.add(con);
							}
							con.varmap.put(var1, mappedvar);
						}
					}
					else ErrorLog.addError("Problem generating CellML: Couldn't retrieve submodels from variable mapping " + var1.getName() + " > " + mappedvar.getName(), true, false);
					
				}
			}
		}
		for(CellMLConnection con : connections){
			Element conel = new Element("connection", mainNS);
			Element mapcompel = new Element("map_components", mainNS);
			mapcompel.setAttribute("component_1", con.sub1.getLocalName());
			mapcompel.setAttribute("component_2", con.sub2.getLocalName());
			conel.addContent(mapcompel);
			
			for(MappableVariable var : con.varmap.keySet()){
				Element mapvarel = new Element("map_variables", mainNS);
				mapvarel.setAttribute("variable_1", var.getName().substring(var.getName().lastIndexOf(".") +1));
				String var2name = con.varmap.get(var).getName();
				mapvarel.setAttribute("variable_2", var2name.substring(var2name.lastIndexOf(".")+1));
				conel.addContent(mapvarel);
			}
			root.addContent(conel);
		}
	}
	
	
	/** Declare any SemSim-style Submodels that are part of the model */
	private void declareSemSimSubmodels(){
		
		for(Submodel submodel : semsimmodel.getSubmodels()){
			
			if( ! submodel.isFunctional()){
				rdfwriter.setRDFforSubmodelAnnotations(submodel);
			}
		}
	}
	
	//*************END WRITE PROCEDURE********************************************//
	
	/**
	 * Add a CellML component (AKA {@link FunctionalSubmodel}) to the CellML JDOM object
	 * @param submodel The CellML component to add
	 * @param truncatenames Whether to shorten the name of the submodel by only taking
	 * the part of its name that follows the first ".".
	 */
	private void processFunctionalSubmodel(FunctionalSubmodel submodel, boolean truncatenames){
		if( ! submodel.isImported()){
			Element comp = new Element("component", mainNS);
			
			// Add the RDF block for any annotations on the submodel
			rdfwriter.setRDFforSubmodelAnnotations(submodel);
			
			comp.setAttribute("name", submodel.getName());  // Add name
			
			if(submodel.hasMetadataID()) 
				comp.setAttribute("id", submodel.getMetadataID(), RDFNamespace.CMETA.createJdomNamespace());  // Add ID, if present
			
			// Add the variables
			for(DataStructure ds : submodel.getAssociatedDataStructures()){
				
				Element variable = new Element("variable", mainNS);
				String initialval = ds.getStartValue();  // Overwritten later by getCellMLintialValue if ds is CellML-type variable
				String nameval = ds.getName();
				
				if(truncatenames && nameval.contains("."))
					nameval = nameval.substring(nameval.indexOf(".")+1);
				
				String publicintval = null;
				String privateintval = null;
				
				// If the Data Structure is a CellML-type variable
				if(ds instanceof MappableVariable){
					MappableVariable cellmlvar = (MappableVariable)ds;
					initialval = cellmlvar.getCellMLinitialValue();
					publicintval = cellmlvar.getPublicInterfaceValue();
					privateintval = cellmlvar.getPrivateInterfaceValue();
				}
				
				// Add the RDF block for any annotations
				rdfwriter.setRDFforDataStructureAnnotations(ds);
				
				String metadataid = ds.getMetadataID();
				// Add other attributes
				if(!metadataid.equals(""))
					variable.setAttribute("id", metadataid, RDFNamespace.CMETA.createJdomNamespace());
				if(initialval!=null && !initialval.equals(""))
					variable.setAttribute("initial_value", initialval);
				if(nameval!=null && !nameval.equals(""))
					variable.setAttribute("name", nameval);
				if(publicintval!=null && !publicintval.equals(""))
					variable.setAttribute("public_interface", publicintval);
				if(privateintval!=null && !privateintval.equals(""))
					variable.setAttribute("private_interface", privateintval);
				
				if(ds.hasUnits()){
					String unitsname = oldAndNewUnitNameMap.get( ds.getUnit().getName());

					variable.setAttribute("units",unitsname);
				}
				else variable.setAttribute("units", "dimensionless");
				
				comp.addContent(variable);
			}
		
			// Add the mathml
			Computation cmptn = submodel.getComputation();
			
			if(cmptn.getMathML() != null && ! cmptn.getMathML().isEmpty()){
				comp.addContent(makeXMLContentFromStringForMathML(cmptn.getMathML()));				
			}
			else{
				String allmathml = "";
				
				for(DataStructure ds : submodel.getAssociatedDataStructures()){					
					allmathml = allmathml + "\n" + ds.getComputation().getMathML();
				}
				comp.addContent(makeXMLContentFromStringForMathML(allmathml));
			}
			root.addContent(comp);
		}
	}
	
	
	/**
	 * Takes a String of XML-formatted content and collects each MathML XML element
	 * @param xml XML text
	 * @return A list of all MathML elements in the XML text
	 */
	public static List<Content> makeXMLContentFromStringForMathML(String xml){
		
		xml = "<temp>\n" + xml + "\n</temp>";
		Content c = makeXMLContentFromString(xml);
		
		List<Content> listofmathmlels = new ArrayList<Content>();
		Iterator<?> it = ((Element) c).getChildren("math", RDFNamespace.MATHML.createJdomNamespace()).iterator();
		while(it.hasNext()){
			Element el = (Element) it.next();
			Element clone = (Element) el.clone();
			listofmathmlels.add(clone.detach());
		}
		return listofmathmlels;
	}
	
	
	/**
	 * Class for representing CellML-style variable mappings across CelLML components
	 * @author mneal
	 */
	public class CellMLConnection{
		public FunctionalSubmodel sub1;
		public FunctionalSubmodel sub2;
		public Map<MappableVariable, MappableVariable> varmap = new HashMap<MappableVariable, MappableVariable>();
		
		public CellMLConnection(FunctionalSubmodel sub1, FunctionalSubmodel sub2){
			this.sub1 = sub1;
			this.sub2 = sub2;
		}
	}
	
	
	/**
	 * Class for representing groupings of CellML components
	 * @author mneal
	 */
	public class CellMLGrouping{
		public String rel;
		public Map<FunctionalSubmodel, Element> submodelelementmap = new HashMap<FunctionalSubmodel, Element>();
		
		public CellMLGrouping(String rel){
			this.rel = rel;
		}
	}
	
	@Override
	public AbstractRDFwriter getRDFwriter(){
		return rdfwriter;
	}

}
