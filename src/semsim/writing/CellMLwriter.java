package semsim.writing;

import java.io.File;
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
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.Importable;
import semsim.model.SemSimComponent;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.utilities.SemSimUtil;

public class CellMLwriter extends ModelWriter {
	private Namespace mainNS;
	private BiologicalRDFblock rdfblock;
	private Set<DataStructure> looseDataStructures = new HashSet<DataStructure>();
	private Element root;
	
	public CellMLwriter(SemSimModel model) {
		super(model);
	}
	
	//*************WRITE PROCEDURE********************************************//
	
	public String writeToString(){
		Document doc = null;
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		
		try{	
			mainNS = Namespace.getNamespace(RDFNamespace.CELLML1_1.getNamespaceasString());
			
			// Check for events, if present write out error msg
			if(semsimmodel.getEvents().size()>0){
				Element eventerror = new Element("error");
				eventerror.setAttribute("msg", "SemSim-to-CellML translation not supported for models with discrete events.");
				return outputter.outputString(new Document(eventerror));
			}
						
			createRDFBlock();
			createRootElement();
			
			doc = new Document(root);
			
			declareImports();
			
			// Add the documentation element
			for(Annotation ann : semsimmodel.getAnnotations()){
				if(ann.getRelation()==SemSimRelation.CELLML_DOCUMENTATION){
					root.addContent(makeXMLContentFromString((String)ann.getValue()));
				}
			}
			
			declareUnits();
			declareComponentsandVariables();
			declareGroupings();
			declareConnections();
			
			// Declare the RDF metadata
			if(!rdfblock.rdf.isEmpty()){
				String rawrdf = BiologicalRDFblock.getRDFmodelAsString(rdfblock.rdf);
				Content newrdf = makeXMLContentFromString(rawrdf);
				if(newrdf!=null) root.addContent(newrdf);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return outputter.outputString(doc);
	}

	private void createRDFBlock() {
		String rdfstring = null;
		for(Annotation ann : semsimmodel.getAnnotations()){
			if(ann.getRelation()==SemSimRelation.CELLML_RDF_MARKUP){
				rdfstring = (String) ann.getValue();
				break;
			}
		}
		
		rdfblock = new BiologicalRDFblock(semsimmodel, rdfstring, mainNS.getURI().toString());
	}
	
	private void createRootElement() {		
		root = new Element("model",mainNS);
		root.addNamespaceDeclaration(RDFNamespace.CMETA.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.XLINK.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.RDF.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.BQS.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.SEMSIM.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.DCTERMS.createJdomNamespace());
		root.addNamespaceDeclaration(RDFNamespace.VCARD.createJdomNamespace());
		
		String namestring = semsimmodel.getName();
		if(semsimmodel.getCurationalMetadata().hasAnnotationValue(Metadata.fullname))
			namestring = semsimmodel.getCurationalMetadata().getAnnotationValue(Metadata.fullname);
		
		root.setAttribute("name", namestring);
		
		if(semsimmodel.getCurationalMetadata().hasAnnotationValue(Metadata.sourcemodelid)) {
			namestring = semsimmodel.getCurationalMetadata().getAnnotationValue(Metadata.sourcemodelid);
			root.setAttribute("id", namestring, RDFNamespace.CMETA.createJdomNamespace());
		}
		
		
	}
	
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
				rdfblock.setRDFforAnnotatedSemSimObject((SemSimComponent)ssc);
			}
			if(importel!=null && importedpiece!=null){
				importel.addContent(importedpiece);
			}
		}
		
		root.addContent(importelements);
	}
	
	private void declareUnits() {
		for(UnitOfMeasurement uom : semsimmodel.getUnits()){
			
			if(!sslib.isCellMLBaseUnit(uom.getName()) && !uom.isImported()){
				
				Element unitel = new Element("units", mainNS);
				unitel.setAttribute("name", uom.getName().replace(" ", "_"));
				
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
						
						factorel.setAttribute("units", factor.getBaseUnit().getName().replace(" ", "_"));
						unitel.addContent(factorel);
					}
				}
				root.addContent(unitel);
			}
		}
	}
	
	private void declareComponentsandVariables() {
		looseDataStructures.addAll(semsimmodel.getAssociatedDataStructures());

		// If there are no functional submodels, then create a new one that houses all the data structures
		if(semsimmodel.getFunctionalSubmodels().size()==0){
			Set<DataStructure> outputset = new HashSet<DataStructure>();
			outputset.addAll(semsimmodel.getAssociatedDataStructures());
			FunctionalSubmodel maincomponent = new FunctionalSubmodel("component_0", outputset);
			maincomponent.setAssociatedDataStructures(semsimmodel.getAssociatedDataStructures());
			String mathml = "";
			
			for(DataStructure ds : maincomponent.getAssociatedDataStructures()){
				if(ds.getComputation().getEvents().size()>0){
					System.err.println("Error: Cannot convert models with discrete events into CellML");
					break;
				}
				else mathml = mathml + ds.getComputation().getMathML() + "\n";
			}
			maincomponent.getComputation().setMathML(mathml);
			processFunctionalSubmodel(maincomponent, false);
		}
		else{
			for(Submodel submodel : semsimmodel.getSubmodels()){
				if(submodel.isFunctional()){
					processFunctionalSubmodel((FunctionalSubmodel) submodel, true);
				}
			}
		}
		
		if( ! looseDataStructures.isEmpty()){
			System.err.println("There were data structures left over");
			for(DataStructure ds : looseDataStructures) System.err.println(ds.getName());
		}
	}
	
	private void declareGroupings() {
		Set<CellMLGrouping> groupings = new HashSet<CellMLGrouping>();
		for(Submodel parentsub : semsimmodel.getSubmodels()){
			if(parentsub.isFunctional()){
				if(!((FunctionalSubmodel)parentsub).isImported()){
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
						
						// Link child elements to the parent
						for(FunctionalSubmodel childsub : ((FunctionalSubmodel)parentsub).getRelationshipSubmodelMap().get(rel)){
							Element childelement = group.submodelelementmap.get(childsub);
							if(childelement==null) childelement = new Element("component_ref", mainNS);
							childelement.setAttribute("component", childsub.getName());
							parentel.addContent(childelement);
							group.submodelelementmap.put((FunctionalSubmodel) childsub, childelement);
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
					else{
						System.err.println("Couldn't retrieve submodels from variable mapping " + var1.getName() + " > " + mappedvar.getName());
					}
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
	
	//*************END WRITE PROCEDURE********************************************//
	
	private void processFunctionalSubmodel(FunctionalSubmodel submodel, boolean truncatenames){
		if( ! ((FunctionalSubmodel)submodel).isImported()){
			Element comp = new Element("component", mainNS);
			
			// Add the RDF block for any singular annotation on the submodel
			rdfblock.setRDFforAnnotatedSemSimObject(submodel);
			
			comp.setAttribute("name", submodel.getName());  // Add name
			
			if( ! submodel.getMetadataID().equalsIgnoreCase("")) 
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
				// Otherwise, if the variable has a start value store it as the initial_value
				else if(ds.hasStartValue())
					initialval = ds.getStartValue();
				
				// Add the RDF block for any singular annotation
				rdfblock.setRDFforAnnotatedSemSimObject(ds);
				
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
				
				if(ds.hasUnits()) variable.setAttribute("units", ds.getUnit().getName().replace(" ", "_"));
				else variable.setAttribute("units", "dimensionless");
				
				comp.addContent(variable);
				looseDataStructures.remove(ds);
			}
		
			// Add the mathml
			Computation cmptn = ((FunctionalSubmodel)submodel).getComputation();
			if(cmptn.getMathML() != null && ! cmptn.getMathML().isEmpty())
				comp.addContent(makeXMLContentFromStringForMathML(cmptn.getMathML()));
			else{
				String allmathml = "";
				for(DataStructure ds : submodel.getAssociatedDataStructures()){
					String mathml = ds.getComputation().getMathML();
					allmathml = allmathml + "\n" + mathml;
				}
				comp.addContent(makeXMLContentFromStringForMathML(allmathml));
			}
			root.addContent(comp);
		}
	}
	
	@Override
	public void writeToFile(File destination){
		SemSimUtil.writeStringToFile(writeToString(), destination);
	}
	
	@Override
	public void writeToFile(URI destination){
		SemSimUtil.writeStringToFile(writeToString(), new File(destination));
	}

	
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
	
	
	// Nested classes
	public class CellMLConnection{
		public FunctionalSubmodel sub1;
		public FunctionalSubmodel sub2;
		public Map<MappableVariable, MappableVariable> varmap = new HashMap<MappableVariable, MappableVariable>();
		
		public CellMLConnection(FunctionalSubmodel sub1, FunctionalSubmodel sub2){
			this.sub1 = sub1;
			this.sub2 = sub2;
		}
	}
	
	public class CellMLGrouping{
		public String rel;
		public Map<FunctionalSubmodel, Element> submodelelementmap = new HashMap<FunctionalSubmodel, Element>();
		
		public CellMLGrouping(String rel){
			this.rel = rel;
		}
	}
}
