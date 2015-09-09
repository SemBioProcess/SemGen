package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.CellMLconstants;
import semsim.SemSimConstants;
import semsim.SemSimObject;
import semsim.annotation.Annotatable;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.model.Importable;
import semsim.model.SemSimComponent;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;

public class CellMLwriter extends ModelWriter {
	private Namespace mainNS;
	private Set<String> metadataids = new HashSet<String>();
	private CellMLbioRDFblock rdfblock;
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
			mainNS = CellMLconstants.cellml1_1NS;
			metadataids.addAll(semsimmodel.getMetadataIDcomponentMap().keySet());
			
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
				if(ann.getRelation()==SemSimConstants.CELLML_DOCUMENTATION_RELATION){
					root.addContent(makeXMLContentFromString((String)ann.getValue()));
				}
			}
			
			declareUnits();
			declareComponentsandVariables();
			declareGroupings();
			declareConnections();
			
			// Declare the RDF metadata
			if(!rdfblock.rdf.isEmpty()){
				String rawrdf = CellMLbioRDFblock.getRDFAsString(rdfblock.rdf);
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
			if(ann.getRelation()==SemSimConstants.CELLML_RDF_MARKUP_RELATION){
				rdfstring = (String) ann.getValue();
				break;
			}
		}
		
		rdfblock = new CellMLbioRDFblock(semsimmodel.getNamespace(), rdfstring, mainNS.getURI().toString());
	}
	
	private void createRootElement() {		
		root = new Element("model",mainNS);
		root.addNamespaceDeclaration(CellMLconstants.cmetaNS);
		root.addNamespaceDeclaration(CellMLconstants.xlinkNS);
		root.addNamespaceDeclaration(CellMLconstants.rdfNS);
		root.addNamespaceDeclaration(CellMLconstants.bqbNS);
		root.addNamespaceDeclaration(CellMLconstants.semsimNS);
		root.addNamespaceDeclaration(CellMLconstants.dctermsNS);
		
		root.setAttribute("name", semsimmodel.getName());
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
				importel.setAttribute("href", hrefVal, CellMLconstants.xlinkNS);
				importelements.add(importel); // make a set of elements
				
				String metaidprefix = null;
				String importedpiecetagname = null;
				String importedpiecerefattr = null;
				
				if(ssc instanceof FunctionalSubmodel){
					importedpiecetagname = "component";
					importedpiecerefattr = "component_ref";
					metaidprefix = "c";
				}
				else if(ssc instanceof UnitOfMeasurement){
					importedpiecetagname = "units";
					importedpiecerefattr = "units_ref";
					metaidprefix = "u";
				}
				importedpiece = new Element(importedpiecetagname, mainNS);
				importedpiece.setAttribute("name", ssc.getLocalName());
				importedpiece.setAttribute(importedpiecerefattr, ssc.getReferencedName());
				
				// Add the RDF block for any singular reference ontology annotations and free-text descriptions
				createRDFforAnnotatedThing((SemSimComponent)ssc, metaidprefix, importedpiece, ((SemSimComponent)ssc).getDescription());
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
			FunctionalSubmodel maincomponent = new FunctionalSubmodel("component_0", semsimmodel.getAssociatedDataStructures());
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
		
		if(!looseDataStructures.isEmpty()){
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
		if(!((FunctionalSubmodel)submodel).isImported()){
			Element comp = new Element("component", mainNS);
			
			// Add the RDF block for any singular annotation on the submodel
			createRDFforAnnotatedThing(submodel, "c", comp, submodel.getDescription());
			
			comp.setAttribute("name", submodel.getName());  // Add name
			
			if(submodel.getMetadataID()!=null) 
				comp.setAttribute("id", submodel.getMetadataID(), CellMLconstants.cmetaNS);  // Add ID, if present
			
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
				createRDFforAnnotatedThing(ds, "v", variable, ds.getDescription());
				
				String metadataid = ds.getMetadataID();
				// Add other attributes
				if(metadataid!=null && !metadataid.equals(""))
					variable.setAttribute("id", metadataid, CellMLconstants.cmetaNS);
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
			if(((FunctionalSubmodel)submodel).getComputation().getMathML()!=null)
				comp.addContent(makeXMLContentFromStringForMathML(((FunctionalSubmodel)submodel).getComputation().getMathML()));
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
	
	public void writeToFile(File destination){
		SemSimUtil.writeStringToFile(writeToString(), destination);
	}
	
	public void writeToFile(URI destination){
		SemSimUtil.writeStringToFile(writeToString(), new File(destination));
	}
	
	public static Content makeXMLContentFromString(String xml){
		try {
			InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Document aDoc = new SAXBuilder().build(stream);
			return aDoc.getRootElement().detach();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public static List<Content> makeXMLContentFromStringForMathML(String xml){
		
		xml = "<temp>\n" + xml + "\n</temp>";
		Content c = makeXMLContentFromString(xml);
		
		List<Content> listofmathmlels = new ArrayList<Content>();
		Iterator<?> it = ((Element) c).getChildren("math", CellMLconstants.mathmlNS).iterator();
		while(it.hasNext()){
			Element el = (Element) it.next();
			Element clone = (Element) el.clone();
			listofmathmlels.add(clone.detach());
		}
		return listofmathmlels;
	}
	
	// Add RDF-formatted semantic metadata for an annotated data structure or submodel 
	public void createRDFforAnnotatedThing(SemSimObject annotated, String idprefix, Element el, String freetext){
		if(annotated instanceof Annotatable){
			Annotatable a = (Annotatable) annotated;
			
			Boolean hasphysprop = false;
			if(a instanceof DataStructure){
				hasphysprop = ((DataStructure)a).hasPhysicalProperty();
			}
			
			if(a.hasRefersToAnnotation() || !freetext.equals("") || hasphysprop){
				String metaid = createMetadataIDandSetNSPrefixes(annotated, idprefix, el);
						
				Resource ares = rdfblock.rdf.createResource("#" + metaid);
				Model localrdf = ModelFactory.createDefaultModel();
				
				// Add free-text description, if present
				if(!freetext.equals("")){
					Property ftprop = ResourceFactory.createProperty(CurationalMetadata.DCTERMS_NAMESPACE + "description");
					Statement st = localrdf.createStatement(ares, ftprop, freetext);
					collectRDFStatement(st, "dcterms", CurationalMetadata.DCTERMS_NAMESPACE, localrdf);
				}
								
				// Add singular annotation
				if(a.hasRefersToAnnotation()){
					URI uri = ((DataStructure)a).getRefersToReferenceOntologyAnnotation().getReferenceURI();
					Property isprop = ResourceFactory.createProperty(SemSimConstants.BQB_IS_URI.toString());
					URI furi = formatAsIdentifiersDotOrgURI(uri);
					Resource refres = localrdf.createResource(furi.toString());
					Statement st = localrdf.createStatement(ares, isprop, refres);
					collectRDFStatement(st, "bqbiol", SemSimConstants.BQB_NAMESPACE, localrdf);
				}
				
				// Add the local RDF within the annotated CellML element
				if(!localrdf.isEmpty()){
					String rawrdf = CellMLbioRDFblock.getRDFAsString(localrdf);
					Content newrdf = makeXMLContentFromString(rawrdf);
					if(newrdf!=null) el.addContent(newrdf);
				}
				
				// If annotated thing is a variable, include any necessary composite annotation info
				if(hasphysprop){
					rdfblock.rdf.setNsPrefix("semsim", SemSimConstants.SEMSIM_NAMESPACE);
					rdfblock.rdf.setNsPrefix("bqbiol", SemSimConstants.BQB_NAMESPACE);
					rdfblock.rdf.setNsPrefix("opb", SemSimConstants.OPB_NAMESPACE);
					rdfblock.rdf.setNsPrefix("ro", SemSimConstants.RO_NAMESPACE);
					rdfblock.rdf.setNsPrefix("model", semsimmodel.getNamespace());
						
					Property iccfprop = ResourceFactory.createProperty(SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString());
					Resource propres = rdfblock.getResourceForDataStructurePropertyAndAnnotate(rdfblock.rdf, (DataStructure)a);
					Statement st = rdfblock.rdf.createStatement(ares, iccfprop, propres);
					
					if(!rdfblock.rdf.contains(st)) rdfblock.rdf.add(st);
					rdfblock.addCompositeAnnotationMetadataForVariable((DataStructure)a);
				}
			}
		}
	}
	
	private void collectRDFStatement(Statement st, String abrev, String namespace, Model localrdf) {
		if(!localrdf.contains(st)){
			localrdf.add(st);
			localrdf.setNsPrefix(abrev, namespace);
		}
	}
	
	private String createMetadataIDandSetNSPrefixes(SemSimObject annotated, String idprefix, Element el) {
		String metaid = annotated.getMetadataID();
		// Create metadata ID for the model element, cache locally
		if(metaid.isEmpty()){
			metaid = idprefix + 0;
			int n = 0;
			while(metadataids.contains(metaid)){
				n++;
				metaid = idprefix + n;
			}
			metadataids.add(metaid);
			el.setAttribute("id", metaid, CellMLconstants.cmetaNS);
		}
		
		rdfblock.rdf.setNsPrefix("semsim", SemSimConstants.SEMSIM_NAMESPACE);
		rdfblock.rdf.setNsPrefix("bqbiol", SemSimConstants.BQB_NAMESPACE);
		rdfblock.rdf.setNsPrefix("dcterms", CurationalMetadata.DCTERMS_NAMESPACE);
		return metaid;
	}
	
	protected static URI formatAsIdentifiersDotOrgURI(URI uri){
		URI newuri = uri;
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());

		// If we are looking at a URI that is NOT formatted according to identifiers.org
		if(!uri.toString().startsWith("http://identifiers.org") 
				&& SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(namespace)){
			
			String kbname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(namespace);
			String fragment = SemSimOWLFactory.getIRIfragment(uri.toString());
			String newnamespace = null;
			
			// Look up new namespace
			for(String nskey : SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.keySet()){
				if(nskey.startsWith("http://identifiers.org") && 
						SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(nskey)==kbname){
					newnamespace = nskey;
				}
			}

			// Replacement rules for specific knowledge bases
			if(kbname==SemSimConstants.UNIPROT_FULLNAME){
				newuri = URI.create(newnamespace + fragment);
			}
			if(kbname==SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME){
				newuri = URI.create(newnamespace + fragment);
			}
			if(kbname==SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(kbname==SemSimConstants.GENE_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(kbname==SemSimConstants.CELL_TYPE_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(kbname==SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME){
				// Need to figure out how to get FMAIDs!!!!
			}
			if(kbname==SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
		}
		return newuri;
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
