package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semsim.CellMLconstants;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.StructuralRelation;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.MMLchoice;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.model.physical.object.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;

public class SemSimOWLreader extends BioModelReader {
	private SemSimModel semsimmodel;
	private OWLDataFactory factory;
	private Map<URI, PhysicalModelComponent> URIandPMCmap = new HashMap<URI, PhysicalModelComponent>();
	private OWLOntology ont;
	private File srcfile;
	
	public SemSimOWLreader(File file) {
		srcfile = file;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		semsimmodel = new SemSimModel();
		semsimmodel.setName(srcfile.getName().substring(0, srcfile.getName().lastIndexOf(".")));
		
		try {
			ont = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	//*****************************READ METHODS*************************//
		
	public SemSimModel readFromFile() throws OWLException, CloneNotSupportedException{	
		if (verifyModel()) return semsimmodel;
		
		collectModelAnnotations();
		collectDataStructures();
		mapCellMLTypeVariables();
		collectUnits();
		establishIsInputRelationships();
		collectRelationalConstraints();
		createProcesses();		
		collectCustomAnnotations();		
		collectSubModels();
				
		return semsimmodel;
	}
	
	/**
	 * Verify the model is a valid SemSimModel
	 */
	private boolean verifyModel() throws OWLException {
		OWLClass topclass = factory.getOWLClass(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "SemSim_component"));
		if(!ont.getClassesInSignature().contains(topclass)){
			semsimmodel.addError("Source file does not appear to be a valid SemSim model");
		}
		
		// Test if the model actually has data structures
		if(SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.DATA_STRUCTURE_CLASS_URI.toString()).isEmpty()
				&& SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_PROPERTY_CLASS_URI.toString()).isEmpty()){
			semsimmodel.addError("No data structures or physical properties in model");
		}
		return (semsimmodel.getErrors().size() > 0);
	}
	
	private void collectModelAnnotations() {
		// Get model-level annotations
		for(OWLAnnotation ann : ont.getAnnotations()){
			URI propertyuri = ann.getProperty().getIRI().toURI();
			if(SemSimConstants.URIS_AND_SEMSIM_RELATIONS.containsKey(propertyuri)){
				if(ann.getValue() instanceof OWLLiteral){
					OWLLiteral val = (OWLLiteral) ann.getValue();
					
					semsimmodel.addAnnotation(new Annotation(SemSimConstants.getRelationFromURI(propertyuri), val.getLiteral()));
				}
			}
		}
	}

	private void collectDataStructures() throws OWLException {
		// Get data structures and add them to model - Decimals, Integers, MMLchoice
				for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.DATA_STRUCTURE_CLASS_URI.toString())){
					String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
					String computationind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimConstants.IS_OUTPUT_FOR_URI.toString());
					String compcode = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, computationind, SemSimConstants.HAS_COMPUTATIONAL_CODE_URI.toString());
					String mathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, computationind, SemSimConstants.HAS_MATHML_URI.toString());
					String description = SemSimOWLFactory.getRDFcomment(ont, factory.getOWLNamedIndividual(IRI.create(dsind)));
					
					DataStructure ds = null;
					
					// If the data structure is a decimal
					if(SemSimOWLFactory.indExistsInClass(dsind, SemSimConstants.DECIMAL_CLASS_URI.toString(), ont)){
						// If it's not a CellML-type variable
						if(SemSimOWLFactory.getIndDatatypeProperty(ont, dsind, SemSimConstants.CELLML_COMPONENT_PUBLIC_INTERFACE_URI.toString()).isEmpty()
								&& SemSimOWLFactory.getIndDatatypeProperty(ont, dsind, SemSimConstants.CELLML_COMPONENT_PRIVATE_INTERFACE_URI.toString()).isEmpty()
								&& SemSimOWLFactory.getIndObjectProperty(ont, dsind, SemSimConstants.MAPPED_TO_URI.toString()).isEmpty()
								&& !SemSimOWLFactory.getIndObjectProperty(ont, dsind, SemSimConstants.IS_OUTPUT_FOR_URI.toString()).isEmpty()){
							ds = semsimmodel.addDataStructure(new Decimal(name));
						}
						else
							ds = semsimmodel.addDataStructure(new MappableVariable(name));
					}
					// If an integer
					if(SemSimOWLFactory.indExistsInClass(dsind, SemSimConstants.SEMSIM_INTEGER_CLASS_URI.toString(), ont))
						ds = semsimmodel.addDataStructure(new SemSimInteger(name));
					// If an MML choice variable
					if(SemSimOWLFactory.indExistsInClass(dsind, SemSimConstants.MML_CHOICE_CLASS_URI.toString(), ont))
						ds = semsimmodel.addDataStructure(new MMLchoice(name));
					if(!compcode.equals("")) ds.getComputation().setComputationalCode(compcode);
					if(!mathml.equals("")) ds.getComputation().setMathML(mathml);
					if(!description.equals("")) ds.setDescription(description);
					
					// Set the data property values: startValue, isDeclared, isDiscrete, isSolutionDomain
					String startval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.HAS_START_VALUE_URI.toString());
					if(!startval.equals("")) ds.setStartValue(startval);
					
					String isdeclared = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.IS_DECLARED_URI.toString());
					ds.setDeclared(Boolean.parseBoolean(isdeclared));
					
					String isdiscrete = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.IS_DISCRETE_URI.toString());
					ds.setDiscrete(Boolean.parseBoolean(isdiscrete));
					
					String issoldom = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.IS_SOLUTION_DOMAIN_URI.toString());
					ds.setIsSolutionDomain(Boolean.parseBoolean(issoldom));
					
					String metadataid = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.METADATA_ID_URI.toString());
					if(!metadataid.equals("")) ds.setMetadataID(metadataid);
					
					// Collect singular refersTo annotation, if present (use nonCompositeAnnotationRefersTo to accommodate older models)
					String referstovalds = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.REFERS_TO_URI.toString());
					if(referstovalds.equals("")){
						referstovalds = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.SEMSIM_NAMESPACE + "nonCompositeAnnotationRefersTo");
					}
					
					// If the data structure is annotated, store annotation
					if(!referstovalds.equals("")){
						String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(dsind)))[0];
						ds.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(referstovalds), label);
					}
					
					// If a CellML-type variable, get interface values
					if(ds instanceof MappableVariable){
						String pubint = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.CELLML_COMPONENT_PUBLIC_INTERFACE_URI.toString());
						String privint = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.CELLML_COMPONENT_PRIVATE_INTERFACE_URI.toString());
						
						if(!pubint.equals("")) ((MappableVariable)ds).setPublicInterfaceValue(pubint);
						if(!privint.equals("")) ((MappableVariable)ds).setPrivateInterfaceValue(privint);
						
						String initval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimConstants.HAS_START_VALUE_URI.toString());
						if(initval!=null && !initval.equals("")) ((MappableVariable)ds).setCellMLinitialValue(initval);
					}
					
					// Set object properties: hasUnit and isComputationalComponentFor
					// OWL versions of semsim models have the units linked to the data structure, not the property
					
					String units = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimConstants.HAS_UNIT_URI.toString());
					String propind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString());
					
					if(!units.equals("") || !propind.equals("")){
						PhysicalProperty pp = new PhysicalProperty();
						ds.setPhysicalProperty(pp);
						String referstoval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, propind, SemSimConstants.REFERS_TO_URI.toString());
						
						// If the property is annotated, store annotation
						if(!referstoval.equals("")){
							String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(propind)))[0];
							pp.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(referstoval), label);
						}
						
						// Set the connection between the physical property and what it's a property of
						String propofind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, propind, SemSimConstants.PHYSICAL_PROPERTY_OF_URI.toString());

						// If it's a property of a physical entity, get the entity. Follow composite, if present
						if(SemSimOWLFactory.indExistsInTree(propofind, SemSimConstants.PHYSICAL_ENTITY_CLASS_URI.toString(), ont)){
							PhysicalEntity ent = getPhysicalEntityFromURI(ont, URI.create(propofind));
							ds.getPhysicalProperty().setPhysicalPropertyOf(ent);
						}
					}
				}
	}
	
	private void mapCellMLTypeVariables() throws OWLException {
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.DECIMAL_CLASS_URI.toString())){
			DataStructure ds = semsimmodel.getDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind));
			if(ds instanceof MappableVariable){
				for(String mappedvaruri : SemSimOWLFactory.getIndObjectProperty(ont, dsind, SemSimConstants.MAPPED_TO_URI.toString())){
					DataStructure mappedvar = semsimmodel.getDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(mappedvaruri));
					if(mappedvar!=null && (mappedvar instanceof MappableVariable)){
						((MappableVariable)ds).addVariableMappingTo((MappableVariable)mappedvar);
						// Use mapping info in input/output network
						((MappableVariable)mappedvar).getComputation().addInput((MappableVariable)ds);
					}
				}
			}
		}
	}
	
	private void collectUnits() throws OWLException {
		// Add units to model, assign to data structures, store unit factoring
		for(String unitind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimConstants.UNITS_CLASS_URI.toString())){
			String unitcode = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, unitind, SemSimConstants.HAS_COMPUTATIONAL_CODE_URI.toString());
			
			UnitOfMeasurement uom = semsimmodel.getUnit(unitcode);
			
			if(uom==null){
				String importedfromval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, unitind, SemSimConstants.IMPORTED_FROM_URI.toString());
				// If the unit is imported, collect import info
				if(importedfromval.equals("") || importedfromval==null){
					uom = new UnitOfMeasurement(unitcode);
					semsimmodel.addUnit(uom);
				}
				else{
					String referencename = getStringValueFromAnnotatedDataPropertyAxiom(ont, unitind, SemSimConstants.IMPORTED_FROM_URI,
							importedfromval, SemSimConstants.REFERENCE_NAME_OF_IMPORT_URI);
					uom = SemSimComponentImporter.importUnits(semsimmodel, unitcode, referencename, importedfromval);
				}
			}
			
			// Set the units for the data structures
			for(String dsuri : SemSimOWLFactory.getIndObjectProperty(ont, unitind, SemSimConstants.UNIT_FOR_URI.toString())){
				DataStructure ds = semsimmodel.getDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsuri));
				ds.setUnit(uom);
			}
			
			String isfundamental = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, unitind, SemSimConstants.IS_FUNDAMENTAL_UNIT_URI.toString());
			uom.setFundamental(isfundamental.equals("true"));

			// Store the unit's factoring info
			for(String baseunitind : SemSimOWLFactory.getIndObjectProperty(ont, unitind, SemSimConstants.HAS_UNIT_FACTOR_URI.toString())){
				String factorunitcode = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, baseunitind, SemSimConstants.HAS_COMPUTATIONAL_CODE_URI.toString());
				if(!factorunitcode.equals("") && factorunitcode!=null){
					UnitOfMeasurement baseunit = semsimmodel.getUnit(factorunitcode);
					if(baseunit==null){
						baseunit = new UnitOfMeasurement(factorunitcode);
						semsimmodel.addUnit(baseunit);
					}
					double exponent = getUnitFactorExponent(ont, unitind, SemSimConstants.HAS_UNIT_FACTOR_URI.toString(), baseunitind);
					String prefix = getStringValueFromAnnotatedObjectPropertyAxiom(ont, unitind, SemSimConstants.HAS_UNIT_FACTOR_URI, baseunitind, SemSimConstants.UNIT_FACTOR_PREFIX_URI);
					uom.addUnitFactor(new UnitFactor(baseunit, exponent, prefix));
				}
			}
		}
	}
		
	/** Go through existing data structures and establish the hasInput relationships */
	private void establishIsInputRelationships() throws OWLException {
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.DATA_STRUCTURE_CLASS_URI.toString())){
			String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
			String computationind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimConstants.IS_OUTPUT_FOR_URI.toString());
			Set<String> compinputs = SemSimOWLFactory.getIndObjectProperty(ont, computationind, SemSimConstants.HAS_INPUT_URI.toString());
			DataStructure ds = semsimmodel.getDataStructure(name);

			for(String in : compinputs){
				ds.getComputation().addInput(semsimmodel.getDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(in)));
			}
			// set the data structure's solution domain
			String soldom = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimConstants.HAS_SOLUTION_DOMAIN_URI.toString());
			semsimmodel.getDataStructure(name).setSolutionDomain(semsimmodel.getDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(soldom)));
		}
	}
	
	private void collectRelationalConstraints() throws OWLException {
		for(String relind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimConstants.RELATIONAL_CONSTRAINT_CLASS_URI.toString())){
			String mmleq = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, relind, SemSimConstants.HAS_COMPUTATIONAL_CODE_URI.toString());
			String mathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, relind, SemSimConstants.HAS_MATHML_URI.toString());
			semsimmodel.addRelationalConstraint(new RelationalConstraint(mmleq, mathml));
		}
	}

	// Deal with physical processes
	private void createProcesses() throws OWLException {
				for(String processind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_PROCESS_CLASS_URI.toString())){
					Set<String> propinds = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimConstants.HAS_PHYSICAL_PROPERTY_URI.toString());
					for(String propind : propinds){
						String dsind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, propind, SemSimConstants.HAS_COMPUTATATIONAL_COMPONENT_URI.toString());
						String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
						
						PhysicalProcess pproc = null;
						String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(processind)))[0];
						String description = null;
						if(SemSimOWLFactory.getRDFComments(ont, processind)!=null)
							description = SemSimOWLFactory.getRDFComments(ont, processind)[0];
						
			
						if(isReferencedIndividual(ont, URI.create(processind))){
							String refersto = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, processind, SemSimConstants.REFERS_TO_URI.toString());
							pproc = semsimmodel.addReferencePhysicalProcess(URI.create(refersto), label); // if equivalent process is available, will return it
						}
						else pproc = semsimmodel.addCustomPhysicalProcess(label, description);  // if equivalent process is available, will return it
			
						// Create a new physical property associated with the process and data structure
						PhysicalProperty pp = new PhysicalProperty();
						semsimmodel.getDataStructure(name).setPhysicalProperty(pp);
			
						if(isReferencedIndividual(ont, URI.create(propind))){
							String referstoval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, propind, SemSimConstants.REFERS_TO_URI.toString());
							String propertylabel = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(propind)))[0];
							pp.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(referstoval), propertylabel);
						}
						
						semsimmodel.getDataStructure(name).getPhysicalProperty().setPhysicalPropertyOf(pproc);
			
						// If needed, create new process object in model and store in uri-physmodelcomp map
					    if(!URIandPMCmap.containsKey(URI.create(processind))){
							Set<String> srcs = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimConstants.HAS_SOURCE_URI.toString());
							Set<String> sinks = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimConstants.HAS_SINK_URI.toString());
							Set<String> mediators = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimConstants.HAS_MEDIATOR_URI.toString());
				
							// Enter source information
							for(String src : srcs){
								PhysicalEntity srcent = getPhysicalEntityFromURI(ont, URI.create(src));
								pproc.addSource(srcent);
							}
							// Enter sink info
							for(String sink : sinks){
								PhysicalEntity sinkent = getPhysicalEntityFromURI(ont, URI.create(sink));
								pproc.addSink(sinkent);
							}
							// Enter mediator info
							for(String med : mediators){
								PhysicalEntity medent = getPhysicalEntityFromURI(ont, URI.create(med));
								pproc.addMediator(medent);
							}
							URIandPMCmap.put(URI.create(processind), pproc);
						}
					}
				}
	}
	
	private void collectCustomAnnotations() {
		// Get additional annotations on custom terms
				// Make sure to get reference classes that are there to define custom classes
				URI[] customclasses = new URI[]{SemSimConstants.CUSTOM_PHYSICAL_ENTITY_CLASS_URI, SemSimConstants.CUSTOM_PHYSICAL_PROCESS_CLASS_URI};
				
				// For the two custom physical model classes...
				for(URI customclassuri : customclasses){			
					// For each custom term in them...
					for(String custstring : SemSimOWLFactory.getIndividualsAsStrings(ont, customclassuri.toString())){
						OWLNamedIndividual custind = factory.getOWLNamedIndividual(IRI.create(custstring));
						
						// For each super class that is not the custom physical component class itself...
						for(OWLClassExpression supercls : custind.asOWLNamedIndividual().getTypes(ont)){
							URI superclsuri = supercls.asOWLClass().getIRI().toURI();
							if(!superclsuri.toString().equals(customclassuri.toString())){
								String label = SemSimOWLFactory.getRDFLabels(ont, supercls.asOWLClass())[0];
								
								// If the reference term hasn't been added yet, add it
								if(!URIandPMCmap.containsKey(supercls.asOWLClass().getIRI().toURI())){
									PhysicalModelComponent refpmc = null;
									if(customclassuri==SemSimConstants.CUSTOM_PHYSICAL_PROCESS_CLASS_URI)
										refpmc = semsimmodel.addReferencePhysicalProcess(superclsuri, label);
									if(customclassuri==SemSimConstants.CUSTOM_PHYSICAL_ENTITY_CLASS_URI)
										refpmc = semsimmodel.addReferencePhysicalEntity(superclsuri, label);
									URIandPMCmap.put(superclsuri, refpmc);
								}
								
								// Add isVersionOf annotation
								PhysicalModelComponent pmc = null;
								if(customclassuri==SemSimConstants.CUSTOM_PHYSICAL_PROCESS_CLASS_URI)
									pmc = semsimmodel.getCustomPhysicalProcessByName(SemSimOWLFactory.getRDFLabels(ont, custind)[0]);
								if(customclassuri==SemSimConstants.CUSTOM_PHYSICAL_ENTITY_CLASS_URI)
									pmc = semsimmodel.getCustomPhysicalEntityByName(SemSimOWLFactory.getRDFLabels(ont, custind)[0]);
								if(pmc!=null){
									pmc.addReferenceOntologyAnnotation(SemSimConstants.BQB_IS_VERSION_OF_RELATION, superclsuri, label);
								}
								else semsimmodel.addError("Attempt to apply reference ontology annotation (BQB:isVersionOf) to " + custstring + " failed. Could not find individual in set of processed physical model components");
							}
						}
					}
				}
	}
	
	
	private void collectSubModels() throws OWLException {
		// Collect the submodels
		Set<String> subset = SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimConstants.SUBMODEL_CLASS_URI.toString());
		
		for(String sub : subset){
			String subname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimConstants.HAS_NAME_URI.toString());
			
			boolean hascomputation = !SemSimOWLFactory.getIndObjectProperty(ont, sub, SemSimConstants.HAS_COMPUTATATIONAL_COMPONENT_URI.toString()).isEmpty();
			
			// Get all associated data structures
			Set<String> dss = SemSimOWLFactory.getIndObjectProperty(ont, sub, SemSimConstants.HAS_ASSOCIATED_DATA_STRUCTURE_URI.toString());
			
			// Get importedFrom value
			String importval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimConstants.IMPORTED_FROM_URI.toString());
			Submodel sssubmodel = null;
			
			// If submodel IS NOT imported
			if(importval.equals("") || importval==null){
				sssubmodel = (hascomputation) ? new FunctionalSubmodel(subname, subname, null, null) : new Submodel(subname);
				String componentmathml = null;
				// If computation associated with submodel, store mathml
				if(sssubmodel instanceof FunctionalSubmodel){
					String comp = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, sub, SemSimConstants.HAS_COMPUTATATIONAL_COMPONENT_URI.toString());
					if(comp!=null && !comp.equals("")){
						componentmathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, comp, SemSimConstants.HAS_MATHML_URI.toString());
						if(componentmathml!=null && !componentmathml.equals(""))
							((FunctionalSubmodel)sssubmodel).getComputation().setMathML(componentmathml);
					}
				}
				
				// Associate data structures with the model
				for(String ds : dss){
					DataStructure theds = semsimmodel.getDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(ds));
					sssubmodel.addDataStructure(theds);

					// white box the equations
					if(componentmathml!=null && !componentmathml.equals("")){
						String localvarname = theds.getName().substring(theds.getName().lastIndexOf(".")+1,theds.getName().length());
						SAXBuilder builder = new SAXBuilder();
						componentmathml = "<temp>\n" + componentmathml + "\n</temp>";
						try {
							Document doc = builder.build(new StringReader(componentmathml));
							
							if(doc.getRootElement() instanceof Element){
								List<?> mathmllist = doc.getRootElement().getChildren("math", CellMLconstants.mathmlNS);
								
								Element varmathml = CellMLreader.getMathMLforOutputVariable(localvarname, mathmllist);
								if(varmathml!=null){
									XMLOutputter xmloutputter = new XMLOutputter();
									theds.getComputation().setMathML(xmloutputter.outputString(varmathml));
									CellMLreader.whiteBoxFunctionalSubmodelEquations(varmathml, subname, semsimmodel, theds);
								}
							}
						} catch (JDOMException | IOException e) {
							e.printStackTrace();
						}
					}
				}
				
				
				// Set the description
				semsimmodel.addSubmodel(sssubmodel);
			}
			// If submodel IS imported
			else{
				String referencename = getStringValueFromAnnotatedDataPropertyAxiom(ont, sub, SemSimConstants.IMPORTED_FROM_URI,
						importval, SemSimConstants.REFERENCE_NAME_OF_IMPORT_URI);
				sssubmodel = 
						SemSimComponentImporter.importFunctionalSubmodel(srcfile, semsimmodel, subname, referencename, importval, sslib);
			}
			
			// Store refersTo annotations, if present (accommodate older models that used nonCompositeAnnotationRefersTo)
			String referstovalsub = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimConstants.REFERS_TO_URI.toString());
			if(referstovalsub.equals(""))
				referstovalsub = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimConstants.SEMSIM_NAMESPACE + "nonCompositeAnnotationRefersTo");
			if(!referstovalsub.equals("")){
				String sublabel = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(sub)))[0];
				if(sublabel.equals("")) sublabel = null;
				sssubmodel.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(referstovalsub), sublabel);
				sssubmodel.setDescription(sublabel);
			}
		}
		
		// If a sub-model has sub-models, add that info to the model, store subsumption types as annotations
		for(String sub : subset){
			String subname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimConstants.HAS_NAME_URI.toString());
			Set<String> subsubset = SemSimOWLFactory.getIndObjectProperty(ont, sub, SemSimConstants.INCLUDES_SUBMODEL_URI.toString());
			for(String subsub : subsubset){
				String subsubname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, subsub, SemSimConstants.HAS_NAME_URI.toString());
				Submodel subsubmodel = semsimmodel.getSubmodel(subsubname);
				
				// Assign the subsumption type (for CellML-type submodels)
				Set<String> reltypes = getSubmodelSubsumptionRelationship(ont, sub, SemSimConstants.INCLUDES_SUBMODEL_URI.toString(), subsub);
				for(String reltype : reltypes){
					if(reltype!=null){
						FunctionalSubmodel fxnalsub = (FunctionalSubmodel)semsimmodel.getSubmodel(subname);
						Set<FunctionalSubmodel> valueset = fxnalsub.getRelationshipSubmodelMap().get(reltype);
						if(valueset==null){
							valueset = new HashSet<FunctionalSubmodel>();
						}
						valueset.add((FunctionalSubmodel) semsimmodel.getSubmodel(subsubname));
						fxnalsub.getRelationshipSubmodelMap().put(reltype, valueset);
					}
				}
				semsimmodel.getSubmodel(subname).addSubmodel(subsubmodel);
			}
		}
	}
	
	//********************************************************************//
	//*****************************HELPER METHODS*************************//
	
	// Get the URI of the object of a triple that uses a structural relation as its predicate
	private CompositePhysicalEntity getURIofObjectofPhysicalEntityStructuralRelation(OWLOntology ont, CompositePhysicalEntity cpe, 
			URI startind) throws OWLException{
		String partof = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, startind.toString(), SemSimConstants.PART_OF_URI.toString());
		String containedin = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, startind.toString(), SemSimConstants.CONTAINED_IN_URI.toString());

		if(!partof.equals("") || !containedin.equals("")){
			String nextind = null;
			if(!partof.equals("")){
				nextind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, startind.toString(), SemSimConstants.PART_OF_URI.toString());
				cpe.getArrayListOfStructuralRelations().add(SemSimConstants.PART_OF_RELATION);
			}
			else{
				nextind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, startind.toString(), SemSimConstants.CONTAINED_IN_URI.toString());
				cpe.getArrayListOfStructuralRelations().add(SemSimConstants.CONTAINED_IN_RELATION);
			}
			String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(nextind)))[0];
			String description = null;
			if(SemSimOWLFactory.getRDFComments(ont, nextind)!=null)
				description = SemSimOWLFactory.getRDFComments(ont, nextind)[0];
			
			PhysicalModelComponent pmc = null;
			
			if(isReferencedIndividual(ont, URI.create(nextind))){
				URI refuri = URI.create(SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, nextind.toString(), SemSimConstants.REFERS_TO_URI.toString()));
				if(!refuri.toString().startsWith("http://www.bhi.washington.edu/SemSim/")){  // Account for older models that used refersTo pointers to custom annotations
					pmc = semsimmodel.addReferencePhysicalEntity(refuri, label);
					cpe.getArrayListOfEntities().add((PhysicalEntity) pmc);
				}
				else{
					pmc = semsimmodel.addCustomPhysicalEntity(label, description);
					cpe.getArrayListOfEntities().add((PhysicalEntity) pmc);
				}
			}
			else{
				pmc = semsimmodel.addCustomPhysicalEntity(label, description);
				cpe.getArrayListOfEntities().add((PhysicalEntity) pmc);
			}
			cpe = getURIofObjectofPhysicalEntityStructuralRelation(ont, cpe, URI.create(nextind));
			if(!URIandPMCmap.containsKey(URI.create(nextind))){
				URIandPMCmap.put(URI.create(nextind), pmc);
			}
		}
		return cpe;
	}
	
	
	// Determine if the individual is annotated against a reference ontology term
	private Boolean isReferencedIndividual(OWLOntology ont, URI induri) throws OWLException{
		return !SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, induri.toString(), SemSimConstants.REFERS_TO_URI.toString()).equals("");
	}
	
	// Retrieve or generate a physical entity from its URI in the OWL-encoded SemSim model 
	
	private PhysicalEntity getPhysicalEntityFromURI(OWLOntology ont, URI uri) throws OWLException{
		PhysicalEntity ent = null;
		String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(uri)))[0];
		String description = null;
		if(SemSimOWLFactory.getRDFComments(ont, uri.toString())!=null)
			description = SemSimOWLFactory.getRDFComments(ont, uri.toString())[0];
		
		if(URIandPMCmap.containsKey(uri))
			ent = (PhysicalEntity) URIandPMCmap.get(uri);
		else{
			if(isReferencedIndividual(ont, uri)){
				String refersto = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, uri.toString(), SemSimConstants.REFERS_TO_URI.toString());
				ent = semsimmodel.addReferencePhysicalEntity(URI.create(refersto), label);
			}
			else
				ent = semsimmodel.addCustomPhysicalEntity(label, description);
			
			// If it's a part of a composite physical entity
			if(!SemSimOWLFactory.getFunctionalIndObjectProperty(ont, uri.toString(), SemSimConstants.PART_OF_URI.toString()).equals("")
					|| !SemSimOWLFactory.getFunctionalIndObjectProperty(ont, uri.toString(), SemSimConstants.CONTAINED_IN_URI.toString()).equals("")){
				ArrayList<PhysicalEntity> ents = new ArrayList<PhysicalEntity>();
				ents.add(ent);
				ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
				CompositePhysicalEntity cent = new CompositePhysicalEntity(ents, rels);
				cent = getURIofObjectofPhysicalEntityStructuralRelation(ont, cent, uri);
				ent = semsimmodel.addCompositePhysicalEntity(cent.getArrayListOfEntities(), cent.getArrayListOfStructuralRelations());
			}
			URIandPMCmap.put(uri, ent);
		}
		return ent;
	}
	
	// Get the relationship for a submodel subsumption

	private Set<String> getSubmodelSubsumptionRelationship(OWLOntology ont, String submodel, String prop, String subsubmodel){
		Set<String> vals = new HashSet<String>();
		OWLIndividual procind = factory.getOWLNamedIndividual(IRI.create(submodel));
		OWLIndividual entind = factory.getOWLNamedIndividual(IRI.create(subsubmodel));
		OWLObjectProperty owlprop = factory.getOWLObjectProperty(IRI.create(prop));
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(owlprop, procind, entind);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(IRI.create(SemSimConstants.CELLML_COMPONENT_SUBSUMPTION_TYPE_URI));
		for(OWLAxiom ax : ont.getAxioms(procind)){
			if(ax.equalsIgnoreAnnotations(axiom)){
				if(!ax.getAnnotations(annprop).isEmpty()){
					for(OWLAnnotation ann : ax.getAnnotations(annprop)){
						OWLLiteral litval = (OWLLiteral) ann.getValue();
						vals.add(litval.getLiteral());
					}
				}
			}
		}
		return vals;
	}
	
	// Get the exponent for a unit factor
	private double getUnitFactorExponent(OWLOntology ont, String derivunit, String prop, String baseunit){
		double val = 1.0;
		OWLIndividual derivind = factory.getOWLNamedIndividual(IRI.create(derivunit));
		OWLIndividual baseind = factory.getOWLNamedIndividual(IRI.create(baseunit));
		OWLObjectProperty owlprop = factory.getOWLObjectProperty(IRI.create(prop));
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(owlprop, derivind, baseind);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(IRI.create(SemSimConstants.UNIT_FACTOR_EXPONENT_URI));
		for(OWLAxiom ax : ont.getAxioms(derivind)){
			if(ax.equalsIgnoreAnnotations(axiom)){
				if(!ax.getAnnotations(annprop).isEmpty()){
					OWLLiteral litval = (OWLLiteral) ax.getAnnotations(annprop).toArray(new OWLAnnotation[]{})[0].getValue();
					val = litval.parseDouble();
				}
			}
		}
		return val;
	}
	
	// Get a string value from an annotation on an object property axiom (individual > prop > individual)

	private String getStringValueFromAnnotatedObjectPropertyAxiom(OWLOntology ont, String subject, URI pred, String object, URI annpropuri){
		String val = null;
		OWLIndividual subjectind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLIndividual objectind = factory.getOWLNamedIndividual(IRI.create(object));
		OWLObjectProperty owlprop = factory.getOWLObjectProperty(IRI.create(pred));
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(owlprop, subjectind, objectind);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(IRI.create(annpropuri));
		for(OWLAxiom ax : ont.getAxioms(subjectind)){
			if(ax.equalsIgnoreAnnotations(axiom)){
				if(!ax.getAnnotations(annprop).isEmpty()){
					OWLLiteral litval = (OWLLiteral) ax.getAnnotations(annprop).toArray(new OWLAnnotation[]{})[0].getValue();
					val = litval.getLiteral();
				}
			}
		}
		return val;
	}
	
	// Get a string value from an annotation on an datatype property axiom (individual > prop > datatype)

	private String getStringValueFromAnnotatedDataPropertyAxiom(OWLOntology ont, String subject, URI pred, String data, URI annpropuri){
		String val = null;
		OWLIndividual subjectind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLDataProperty owlprop = factory.getOWLDataProperty(IRI.create(pred));
		OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(owlprop, subjectind, data);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(IRI.create(annpropuri));
		for(OWLAxiom ax : ont.getAxioms(subjectind)){
			if(ax.equalsIgnoreAnnotations(axiom)){
				if(!ax.getAnnotations(annprop).isEmpty()){
					OWLLiteral litval = (OWLLiteral) ax.getAnnotations(annprop).toArray(new OWLAnnotation[]{})[0].getValue();
					val = litval.getLiteral();
				}
			}
		}
		return val;
	}

}
