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

import semsim.Annotatable;
import semsim.CellMLconstants;
import semsim.SemSimConstants;
import semsim.SemSimUtil;
import semsim.model.Importable;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.computational.MappableVariable;
import semsim.model.computational.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.FunctionalSubmodel;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.owl.SemSimOWLFactory;

public class CellMLwriter implements Writer{
	
	private SemSimModel semsimmodel;
	private Namespace mainNS;
	private Set<String> metadataids = new HashSet<String>();
	private CellMLbioRDFblock rdfblock;
	private Set<DataStructure> looseDataStructures = new HashSet<DataStructure>();
	private Element root;
	
	public String writeToString(SemSimModel model){
		Document doc = null;
		try{
			this.semsimmodel = model;
			
			mainNS = CellMLconstants.cellml1_1NS;
			metadataids.addAll(semsimmodel.getMetadataIDcomponentMap().keySet());
			
			String rdfstring = null;
			for(Annotation ann : semsimmodel.getAnnotations()){
				if(ann.getRelation()==SemSimConstants.CELLML_RDF_MARKUP_RELATION){
					rdfstring = (String) ann.getValue();
					break;
				}
			}
			
			rdfblock = new CellMLbioRDFblock(semsimmodel, rdfstring, mainNS.getURI().toString());
			
			// Create root element
			root = new Element("model",mainNS);
			root.addNamespaceDeclaration(CellMLconstants.cmetaNS);
			root.addNamespaceDeclaration(CellMLconstants.xlinkNS);
			root.addNamespaceDeclaration(CellMLconstants.rdfNS);
			root.addNamespaceDeclaration(CellMLconstants.bqbNS);
			root.addNamespaceDeclaration(CellMLconstants.semsimNS);
			root.addNamespaceDeclaration(CellMLconstants.dctermsNS);
			
			root.setAttribute("name", semsimmodel.getName());
			
			doc = new Document(root);
			
			// Add the documentation element
			for(Annotation ann : semsimmodel.getAnnotations()){
				if(ann.getRelation()==SemSimConstants.CELLML_DOCUMENTATION_RELATION){
					root.addContent(makeXMLContentFromString((String)ann.getValue()));
				}
			}
			
			// Declare the imports
			Set<Element> importelements = new HashSet<Element>();
			Set<Importable> importablecomponents = new HashSet<Importable>();
			for(Submodel sub : semsimmodel.getSubmodels()){ 
				if(sub instanceof FunctionalSubmodel) importablecomponents.add((FunctionalSubmodel)sub);
			}
			importablecomponents.addAll(semsimmodel.getUnits());
			
			for(Importable ssc : importablecomponents){
				Element importel = null;
				Element importedpiece = null;
				String importedpiecetagname = null;
				String importedpiecerefattr = null;
				String hrefVal = null;
				String origname = null;
				
				if(ssc.isImported() && ssc.getParentImport()==null){
					
					hrefVal = ssc.getHrefValue();
					origname = ssc.getReferencedName();
					String metaidprefix = null;
					
					if(ssc instanceof FunctionalSubmodel){
						if(ssc.isImported()){
							importedpiecetagname = "component";
							importedpiecerefattr = "component_ref";
							metaidprefix = "c";
						}
					}
					else if(ssc instanceof UnitOfMeasurement){
						if(ssc.isImported()){
							importedpiecetagname = "units";
							importedpiecerefattr = "units_ref";
							metaidprefix = "u";
						}
					}
					// Collecting all imports from a common model does not work, 
					// need to create a new import element for each imported piece
					importel = new Element("import", mainNS);
					importel.setAttribute("href", hrefVal, CellMLconstants.xlinkNS);
					importelements.add(importel); // make a set of elements
					
					importedpiece = new Element(importedpiecetagname, mainNS);
					importedpiece.setAttribute("name", ssc.getLocalName());
					importedpiece.setAttribute(importedpiecerefattr, origname);
					
					// Add the RDF block for any singular reference ontology annotations and free-text descriptions
					createRDFforAnnotatedThing((SemSimComponent)ssc, metaidprefix, importedpiece, ((SemSimComponent)ssc).getDescription());
				}
				if(importel!=null && importedpiece!=null){
					importel.addContent(importedpiece);
				}
			}
			
			root.addContent(importelements);
	
			// Declare the units
			for(UnitOfMeasurement uom : semsimmodel.getUnits()){
				if(!CellMLconstants.CellML_UNIT_DICTIONARY.contains(uom.getName()) && !uom.isImported()){
					Element unitel = new Element("units", mainNS);
					unitel.setAttribute("name", uom.getName());
					if(uom.isFundamental()) unitel.setAttribute("base_units", "yes");
					else{
						for(UnitFactor factor : uom.getUnitFactors()){
							Element factorel = new Element("unit", mainNS);
							if(factor.getExponent()!=1.0)
								factorel.setAttribute("exponent", Double.toString(factor.getExponent()));
							if(factor.getPrefix()!=null)
								factorel.setAttribute("prefix", factor.getPrefix());
							factorel.setAttribute("units", factor.getBaseUnit().getName());
							unitel.addContent(factorel);
						}
					}
					root.addContent(unitel);
				}
			}
			
			// Declare the components and their variables
			looseDataStructures.addAll(semsimmodel.getDataStructures());

			// If there are no functional submodels, then create a new one that houses all the data structures
			if(semsimmodel.getFunctionalSubmodels().size()==0){
				FunctionalSubmodel maincomponent = new FunctionalSubmodel("component_0", semsimmodel.getDataStructures());
				maincomponent.setAssociatedDataStructures(semsimmodel.getDataStructures());
				String mathml = "";
				for(DataStructure ds : maincomponent.getAssociatedDataStructures()){
					mathml = mathml + ds.getComputation().getMathML() + "\n";
				}
				maincomponent.getComputation().setMathML(mathml);
				processFunctionalSubmodel(maincomponent);
				// PROBLEM HERE IS THAT THE MATHML CONTENT ISN"T CELLML-FORMATTED
			}
			else{
				for(Submodel submodel : semsimmodel.getSubmodels()){
					if(submodel instanceof FunctionalSubmodel){
						processFunctionalSubmodel((FunctionalSubmodel) submodel);
					}
				}
			}
			
			if(!looseDataStructures.isEmpty()){
				System.err.println("There were data structures left over");
				for(DataStructure ds : looseDataStructures) System.err.println(ds.getName());
			}
			
			// Declare the groupings
			Set<CellMLGrouping> groupings = new HashSet<CellMLGrouping>();
			for(Submodel parentsub : semsimmodel.getSubmodels()){
				if(parentsub instanceof FunctionalSubmodel){
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
		
			// Declare the connections
			Set<CellMLConnection> connections = new HashSet<CellMLConnection>();
			for(DataStructure ds : semsimmodel.getDataStructures()){
				if(ds instanceof MappableVariable){
					MappableVariable var1 = (MappableVariable)ds;
					for(MappableVariable mappedvar : var1.getMappedTo()){
	
						FunctionalSubmodel sub1 = getSubmodelForAssociatedMappableVariable(var1);
						FunctionalSubmodel sub2 = getSubmodelForAssociatedMappableVariable(mappedvar);
						
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
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		return outputter.outputString(doc);
	}
	// End of writeToString function
	
	
	private void processFunctionalSubmodel(FunctionalSubmodel submodel){
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
				
				String initialval = null;
				String nameval = null;
				String publicintval = null;
				String privateintval = null;
				String metadataid = ds.getMetadataID();

				if(ds.getName().contains("."))
					nameval = ds.getName().substring(ds.getName().indexOf(".")+1);
				else nameval = ds.getName();
				
				if(ds instanceof MappableVariable){
					MappableVariable cellmlvar = (MappableVariable)ds;
					if(cellmlvar.getCellMLinitialValue()!=null)
						initialval = cellmlvar.getCellMLinitialValue();
					if(cellmlvar.getPublicInterfaceValue()!=null)
						publicintval = cellmlvar.getPublicInterfaceValue();
					if(cellmlvar.getPrivateInterfaceValue()!=null)
						privateintval = cellmlvar.getPrivateInterfaceValue();
					
					if(ds.getComputation()!=null){
						if(ds.getComputation().getInputs().isEmpty()){
							String code = ds.getComputation().getComputationalCode();
							if(code!=null)
								initialval = code.substring(code.indexOf("=")+1).trim();
						}
					}
				}
				
				// Add the RDF block for any singular annotation
				createRDFforAnnotatedThing(ds, "v", variable, ds.getDescription());

				// Add other attributes
				if(metadataid!=null)
					variable.setAttribute("id", metadataid, CellMLconstants.cmetaNS);
				if(initialval!=null)
					variable.setAttribute("initial_value", initialval);
				if(nameval!=null)
					variable.setAttribute("name", nameval);
				if(publicintval!=null)
					variable.setAttribute("public_interface", publicintval);
				if(privateintval!=null)
					variable.setAttribute("private_interface", privateintval);
				
				if(ds.hasUnits()) variable.setAttribute("units", ds.getUnit().getName());
				else variable.setAttribute("units", "dimensionless");
				
				comp.addContent(variable);
				looseDataStructures.remove(ds);
			}
		
			// Add the mathml
			if(((FunctionalSubmodel)submodel).getComputation().getMathML()!=null)
				comp.addContent(makeXMLContentFromStringForMathML(((FunctionalSubmodel)submodel).getComputation().getMathML()));
			
			root.addContent(comp);
		}
	}
	
	public void writeToFile(SemSimModel model, File destination){
		SemSimUtil.writeStringToFile(writeToString(model), destination);
	}
	
	public void writeToFile(SemSimModel model, URI destination){
		SemSimUtil.writeStringToFile(writeToString(model), new File(destination));
	}
	
	public Content makeXMLContentFromString(String xml){
		try {
			InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Document aDoc;
			aDoc = new SAXBuilder().build(stream);
			return aDoc.getRootElement().detach();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public List<Content> makeXMLContentFromStringForMathML(String xml){
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
	
	public FunctionalSubmodel getSubmodelForAssociatedMappableVariable(MappableVariable var){
		String compname = var.getName().substring(0, var.getName().lastIndexOf("."));
		return (FunctionalSubmodel) semsimmodel.getSubmodel(compname);
	}
	
	
	// Add RDF-formatted semantic metadata for an annotated data structure or submodel 
	public void createRDFforAnnotatedThing(SemSimComponent annotated, String idprefix, Element el, String freetext){
		if(annotated instanceof Annotatable){
			Annotatable a = (Annotatable) annotated;
			String metaid = annotated.getMetadataID();
			
			Boolean hasphysprop = false;
			if(a instanceof DataStructure){
				PhysicalProperty prop = ((DataStructure)a).getPhysicalProperty();
				if(prop!=null){
					if(prop.hasRefersToAnnotation()) hasphysprop = true;
				}
			}
			
			if(a.hasRefersToAnnotation() || (freetext!=null && !freetext.isEmpty()) || hasphysprop){
				// Create metadata ID for the model element, cache locally
				if(metaid==null){
					metaid = idprefix + 0;
					int n = 0;
					while(metadataids.contains(metaid)){
						n++;
						metaid = idprefix + n;
					}
					metadataids.add(metaid);
					el.setAttribute("id", metaid, CellMLconstants.cmetaNS);
				}
				
				Model localrdf = ModelFactory.createDefaultModel();
				
				rdfblock.rdf.setNsPrefix("semsim", SemSimConstants.SEMSIM_NAMESPACE);
				rdfblock.rdf.setNsPrefix("bqbiol", SemSimConstants.BQB_NAMESPACE);
				rdfblock.rdf.setNsPrefix("dcterms", SemSimConstants.DCTERMS_NAMESPACE);
				
				Resource ares = rdfblock.rdf.createResource("#" + metaid);
				
				// Add singular annotation
				if(a.hasRefersToAnnotation()){
					URI uri = a.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI();
					Property isprop = ResourceFactory.createProperty(SemSimConstants.BQB_IS_URI.toString());
					URI furi = formatAsIdentifiersDotOrgURI(uri);
					Resource refres = localrdf.createResource(furi.toString());
					Statement st = localrdf.createStatement(ares, isprop, refres);
					if(!localrdf.contains(st)){
						localrdf.add(st);
						localrdf.setNsPrefix("bqbiol", SemSimConstants.BQB_NAMESPACE);
					}
				}
				
				// Add free-text description, if present
				if(freetext!=null){
					Property ftprop = ResourceFactory.createProperty(SemSimConstants.DCTERMS_NAMESPACE + "description");
					Statement st = localrdf.createStatement(ares, ftprop, freetext);
					if(!localrdf.contains(st)){
						localrdf.add(st);
						localrdf.setNsPrefix("dcterms", SemSimConstants.DCTERMS_NAMESPACE);
					}
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
					Resource propres = rdfblock.getResourceForPMCandAnnotate(rdfblock.rdf, ((DataStructure)a).getPhysicalProperty());
					Statement st = rdfblock.rdf.createStatement(ares, iccfprop, propres);
					if(!rdfblock.rdf.contains(st)) rdfblock.rdf.add(st);
					rdfblock.addCompositeAnnotationMetadataForVariable((DataStructure)a);
				}
			}
		}
	}
	
	
	protected static URI formatAsIdentifiersDotOrgURI(URI uri){
		URI newuri = null;
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());
		String fragment = SemSimOWLFactory.getIRIfragment(uri.toString());
		String newnamespace = null;
		
		// If we are looking at a URI that is NOT formatted according to identifiers.org
		if(!uri.toString().startsWith("http://identifiers.org") 
				&& SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(namespace)){
			
			String kbname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(namespace);
			
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
				return uri;
			}
			if(kbname==SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
		}
		if(newuri!=null) return newuri;
		else return uri;
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
