package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semsim.SemSimLibrary;
import semsim.annotation.Annotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Event;
import semsim.model.computational.Event.EventAssignment;
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
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.owl.SemSimOWLFactory.RestrictionVisitor;
import semsim.utilities.SemSimUtil;

public class SemSimOWLreader extends ModelReader {
	private OWLDataFactory factory;
	private Map<String, PhysicalModelComponent> identitymap = new HashMap<String, PhysicalModelComponent>();

	private Map<String, PhysicalPropertyinComposite> idpropertymap = new HashMap<String, PhysicalPropertyinComposite>();
	private OWLOntology ont;
	private URI physicaldefinitionURI;

	public SemSimOWLreader(File file) {
		super(file);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		semsimmodel.setName(modelaccessor.getModelName());
		
		try {
			ont = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
//	public SemSimOWLreader(ModelAccessor modelaccessor){
//		super(modelaccessor);
//		
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		factory = manager.getOWLDataFactory();
//		semsimmodel.setName(modelaccessor.getModelName());
//		
//		try {
//			String modelcode = modelaccessor.getModelTextAsString();
//			InputStream stream = new ByteArrayInputStream(modelcode.getBytes(StandardCharsets.UTF_8));
//			
//			System.out.println(modelcode);
//			
//			ont = manager.loadOntologyFromOntologyDocument(stream);
//		} catch (OWLOntologyCreationException e) {
//			e.printStackTrace();
//		}
//	}
	
	//*****************************READ METHODS*************************//
		
	public SemSimModel read() throws OWLException{	
		if (verifyModel()) return semsimmodel;
		
		setPhysicalDefinitionURI();
		collectModelAnnotations();
		collectReferenceClasses();
		collectCompositeEntities();
		createProcesses();
		collectDataStructures();
		mapCellMLTypeVariables();
		collectUnits();
		collectRelationalConstraints();	
		collectEvents();
		establishIsInputRelationships();
		collectCustomAnnotations();		
		collectSubModels();
				
		return semsimmodel;
	}
	
	/**
	 * Verify the model is a valid SemSimModel
	 */
	private boolean verifyModel() throws OWLException {
		OWLClass topclass = factory.getOWLClass(IRI.create(RDFNamespace.SEMSIM.getNamespaceasString() + "SemSim_component"));
		if(!ont.getClassesInSignature().contains(topclass)){
			semsimmodel.addError("Source file does not appear to be a valid SemSim model");
		}
		
		// Test if the model actually has data structures
		if(SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString()).isEmpty()
				&& SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_PROPERTY.getURIasString()).isEmpty()){
			semsimmodel.addError("No data structures or physical properties in model");
		}
		return (semsimmodel.getErrors().size() > 0);
	}
	
	private void setPhysicalDefinitionURI(){
		
		if(ont.containsDataPropertyInSignature(IRI.create(RDFNamespace.SEMSIM.getNamespaceasString() + "refersTo"))){
			physicaldefinitionURI = URI.create(RDFNamespace.SEMSIM.getNamespaceasString() + "refersTo");
		}
		else if(ont.containsDataPropertyInSignature(SemSimRelation.HAS_PHYSICAL_DEFINITION.getIRI())){
			physicaldefinitionURI = SemSimRelation.HAS_PHYSICAL_DEFINITION.getURI();
		}
	}
	
	private void collectModelAnnotations() {
		// Get model-level annotations
		Set<OWLAnnotation> anns = ont.getAnnotations();
		Set<OWLAnnotation> annstoremove = new HashSet<OWLAnnotation>();
		for (OWLAnnotation named : anns) {
			if (named.getProperty().getIRI().equals(SemSimLibrary.SEMSIM_VERSION_IRI)) {
				semsimmodel.setSemSimVersion(((OWLLiteral)named.getValue()).getLiteral());
				annstoremove.add(named);
			};
			if (named.getProperty().getIRI().equals(SemSimModel.LEGACY_CODE_LOCATION_IRI)) {
				ModelAccessor ma = new ModelAccessor(((OWLLiteral)named.getValue()).getLiteral());
				semsimmodel.setSourceFileLocation(ma);
				annstoremove.add(named);
			};
		}
		semsimmodel.getCurationalMetadata().setCurationalMetadata(anns, annstoremove);
		anns.removeAll(annstoremove);
		
		//Add remaining annotations
		for(OWLAnnotation ann : anns){
			URI propertyuri = ann.getProperty().getIRI().toURI();
			Relation rel = SemSimRelations.getRelationFromURI(propertyuri);
			if(rel != SemSimRelation.UNKNOWN){
				if(ann.getValue() instanceof OWLLiteral){
					OWLLiteral val = (OWLLiteral) ann.getValue();
					
					semsimmodel.addAnnotation(new Annotation(rel, val.getLiteral()));
				}
			}
		}
	}

	private void collectReferenceClasses() throws OWLException {
		for (String refuri : SemSimOWLFactory.getAllSubclasses(ont,  SemSimTypes.PHYSICAL_PROPERTY.getURIasString(),false)) {
			String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLClass(IRI.create(refuri)))[0];
			if (label.isEmpty()) continue;
			
			PhysicalPropertyinComposite pp = new PhysicalPropertyinComposite(label, URI.create(refuri));
			
			semsimmodel.addAssociatePhysicalProperty(pp);
			idpropertymap.put(refuri, pp);
		}

		for (String rperef : SemSimOWLFactory.getAllSubclasses(ont,  SemSimTypes.REFERENCE_PHYSICAL_ENTITY.getURIasString(), false)) {
			String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLClass(IRI.create(rperef)))[0];
			PhysicalEntity pe;
			if(!rperef.toString().startsWith("http://www.bhi.washington.edu/SemSim/")){  // Account for older models that used refersTo pointers to custom annotations
				
				// If an identical reference entity was already added to the model, this will return the original, 
				// otherwise it creates a new physical entity
				pe = new ReferencePhysicalEntity(URI.create(rperef), label);
				semsimmodel.addReferencePhysicalEntity((ReferencePhysicalEntity)pe);
			}
			else{
				pe = new CustomPhysicalEntity(label, label);
				semsimmodel.addCustomPhysicalEntity((CustomPhysicalEntity) pe);
			}
			identitymap.put(rperef, pe);
		}

		for (String cuperef : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont,  SemSimTypes.CUSTOM_PHYSICAL_ENTITY.getURIasString())) {
			makeCustomEntity(cuperef);
		}
	}
	
	private void collectCompositeEntities() throws OWLException {
		for (String cperef : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont,  SemSimTypes.COMPOSITE_PHYSICAL_ENTITY.getURIasString())) {		
			String ind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, cperef, StructuralRelation.HAS_INDEX_ENTITY.getURIasString());
			String index = ind;
			ArrayList<PhysicalEntity> rpes = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
			
			PhysicalEntity rpe = (PhysicalEntity)getClassofIndividual(ind);
			rpes.add(rpe);
			
			while (true) {
				String nextind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, ind.toString(), StructuralRelation.PART_OF.getURIasString());
				StructuralRelation rel = StructuralRelation.PART_OF;
				if (nextind=="") {
					nextind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, ind.toString(), StructuralRelation.CONTAINED_IN.getURIasString());
					if (nextind=="") break;
					rel = StructuralRelation.CONTAINED_IN;
				}
				rpe = (PhysicalEntity)getClassofIndividual(nextind);
				if (rpe == null) break;
				rpes.add(rpe);
				rels.add(rel);
				ind = nextind;
			}
			CompositePhysicalEntity cpe = semsimmodel.addCompositePhysicalEntity(rpes, rels);
			identitymap.put(index, cpe);
		}
	}
	
	// Deal with physical processes
	private void createProcesses() throws OWLException {
		// For each process instance in the class SemSim:Physical_process
		for(String processind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_PROCESS.getURIasString())){
			
			String processlabel = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(processind)))[0];
			String description = null;
			
			if(SemSimOWLFactory.getRDFComments(ont, processind)!=null)
				description = SemSimOWLFactory.getRDFComments(ont, processind)[0];
						
			PhysicalProcess pproc = null;
			// Create reference physical process, if there is an annotation
			String hasphysicaldef = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, processind, physicaldefinitionURI.toString());
			if (!hasphysicaldef.isEmpty()) {
				pproc = semsimmodel.addReferencePhysicalProcess(new ReferencePhysicalProcess(URI.create(hasphysicaldef), processlabel));
			}
			// Otherwise create a custom physical process
			else {
				pproc = semsimmodel.addCustomPhysicalProcess(new CustomPhysicalProcess(processlabel, description));
			}
			
			// Capture the physical entity participants
			Set<String> srcs = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimRelation.HAS_SOURCE.getURIasString());
			// Enter source information
			for(String src : srcs){
				CompositePhysicalEntity srcent = (CompositePhysicalEntity)identitymap.get(src);
				if (srcent==null) { 
					srcent = createSingularComposite(src);
				}
				Double m = getMultiplierForProcessParticipant(ont, processind, 
						SemSimRelation.HAS_SOURCE, src);
				pproc.addSource(srcent, m);
			}
			// Enter sink info
			Set<String> sinks = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimRelation.HAS_SINK.getURIasString());
			for(String sink : sinks){
				CompositePhysicalEntity sinkent = (CompositePhysicalEntity)identitymap.get(sink);
				if (sinkent==null) { 
					sinkent = createSingularComposite(sink);
				}
				Double m = getMultiplierForProcessParticipant(ont, processind, 
						SemSimRelation.HAS_SINK, sink);
				pproc.addSink(sinkent, m);
			}
			// Enter mediator info
			Set<String> mediators = SemSimOWLFactory.getIndObjectProperty(ont, processind, SemSimRelation.HAS_MEDIATOR.getURIasString());
			for(String med : mediators){
				
				CompositePhysicalEntity medent = (CompositePhysicalEntity)identitymap.get(med);
				if (medent==null) { 
					medent = createSingularComposite(med);
				}
				pproc.addMediator(medent);
			}
			identitymap.put(processind, pproc);
		}
	}
	

	
	private void collectDataStructures() throws OWLException {
		// Get data structures and add them to model - Decimals, Integers, MMLchoice
				for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString())){
					String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
					String computationind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimRelation.IS_OUTPUT_FOR.getURIasString());
					String compcode = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, computationind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
					String mathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, computationind, SemSimRelation.HAS_MATHML.getURIasString());
					String description = SemSimOWLFactory.getRDFcomment(ont, factory.getOWLNamedIndividual(IRI.create(dsind)));
					
					DataStructure ds = null;
					
					// If the data structure is a decimal
					if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.DECIMAL.getURIasString(), ont)){
						// If it's not a CellML-type variable
						if(SemSimOWLFactory.getIndDatatypeProperty(ont, dsind, SemSimRelation.CELLML_COMPONENT_PUBLIC_INTERFACE.getURIasString()).isEmpty()
								&& SemSimOWLFactory.getIndDatatypeProperty(ont, dsind, SemSimRelation.CELLML_COMPONENT_PRIVATE_INTERFACE.getURIasString()).isEmpty()
								&& SemSimOWLFactory.getIndDatatypeProperty(ont, dsind, SemSimRelation.CELLML_INITIAL_VALUE.getURIasString()).isEmpty()
								&& SemSimOWLFactory.getIndObjectProperty(ont, dsind, SemSimRelation.MAPPED_TO.getURIasString()).isEmpty()
								&& !SemSimOWLFactory.getIndObjectProperty(ont, dsind, SemSimRelation.IS_OUTPUT_FOR.getURIasString()).isEmpty()){
							ds = new Decimal(name);
						}
						else
							ds = new MappableVariable(name);
					}
					// If an integer
					if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.INTEGER.getURIasString(), ont))
						ds = new SemSimInteger(name);
					// If an MML choice variable
					if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.MMLCHOICE.getURIasString(), ont))
						ds = new MMLchoice(name);
					semsimmodel.addDataStructure(ds);
					if(!compcode.equals("")) ds.getComputation().setComputationalCode(compcode);
					if(!mathml.equals("")) ds.getComputation().setMathML(mathml);
					if(!description.equals("")) ds.setDescription(description);
					
					// Set the data property values: startValue, isDeclared, isDiscrete, isSolutionDomain
					String startval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.HAS_START_VALUE.getURIasString());
					if(!startval.equals("")) ds.setStartValue(startval);
					
					String isdeclared = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.IS_DECLARED.getURIasString());
					ds.setDeclared(Boolean.parseBoolean(isdeclared));

					String issoldom = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.IS_SOLUTION_DOMAIN.getURIasString());
					ds.setIsSolutionDomain(Boolean.parseBoolean(issoldom));
					
					String metadataid = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.METADATA_ID.getURIasString());
					if(!metadataid.equals("")) ds.setMetadataID(metadataid);
					
					// Collect singular physical definition annotation, if present
					String physdefvalds = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, physicaldefinitionURI.toString());
					
					// If the data structure is annotated, store annotation
					if(!physdefvalds.equals("")){	
						String reflabel = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(dsind)))[0];
						ds.setSingularAnnotation((PhysicalProperty)getReferenceTerm(physdefvalds, reflabel));
					}
					
					// If a CellML-type variable, get interface values and initial value
					if(ds instanceof MappableVariable){
						String pubint = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.CELLML_COMPONENT_PUBLIC_INTERFACE.getURIasString());
						String privint = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.CELLML_COMPONENT_PRIVATE_INTERFACE.getURIasString());
						
						if(!pubint.equals("")) ((MappableVariable)ds).setPublicInterfaceValue(pubint);
						if(!privint.equals("")) ((MappableVariable)ds).setPrivateInterfaceValue(privint);
						
						String initval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, dsind, SemSimRelation.CELLML_INITIAL_VALUE.getURIasString());
						if(initval!=null && !initval.equals("")) ((MappableVariable)ds).setCellMLinitialValue(initval);
					}
					
					// Set object properties: hasUnit and isComputationalComponentFor
					// OWL versions of semsim models have the units linked to the data structure, not the property
					
					String units = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimRelation.HAS_UNIT.getURIasString());
					String propind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getURIasString());
					
					if(!units.equals("") || !propind.equals("")){
						String physdefval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, propind, physicaldefinitionURI.toString());	
						if (!physdefval.isEmpty())	ds.setAssociatedPhysicalProperty(idpropertymap.get(physdefval));
						
						// Set the connection between the physical property and what it's a property of
						String propofind = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, propind, SemSimRelation.PHYSICAL_PROPERTY_OF.getURIasString());
						if (!propofind.isEmpty()) {
							PhysicalModelComponent pmc = identitymap.get(propofind);
							if (pmc==null) {
								pmc = createSingularComposite(propofind);
							
							}
							ds.setAssociatedPhysicalModelComponent(pmc);
						}
					}
				}
	}
	
	private void mapCellMLTypeVariables() throws OWLException {
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DECIMAL.getURIasString())){
			DataStructure ds = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind));
			if(ds instanceof MappableVariable){
				for(String mappedvaruri : SemSimOWLFactory.getIndObjectProperty(ont, dsind, SemSimRelation.MAPPED_TO.getURIasString())){
					DataStructure mappedvar = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(mappedvaruri));
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
		for(String unitind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.UNIT_OF_MEASUREMENT.getURIasString())){
			String unitcode = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, unitind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
			
			UnitOfMeasurement uom = semsimmodel.getUnit(unitcode);
			
			if(uom==null){
				String importedfromval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, unitind, SemSimRelation.IMPORTED_FROM.getURIasString());
				// If the unit is imported, collect import info
				if(importedfromval.equals("") || importedfromval==null){
					uom = new UnitOfMeasurement(unitcode);
					semsimmodel.addUnit(uom);
				}
				else{
					String referencename = getStringValueFromAnnotatedDataPropertyAxiom(ont, unitind, SemSimRelation.IMPORTED_FROM.getURI(),
							importedfromval, SemSimRelation.REFERENCE_NAME_OF_IMPORT.getURI());
					uom = SemSimComponentImporter.importUnits(semsimmodel, unitcode, referencename, importedfromval);
				}
			}
			
			// Set the units for the data structures
			for(String dsuri : SemSimOWLFactory.getIndObjectProperty(ont, unitind, SemSimRelation.UNIT_FOR.getURIasString())){
				DataStructure ds = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsuri));
				ds.setUnit(uom);
			}
			
			String isfundamental = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, unitind, SemSimRelation.IS_FUNDAMENTAL_UNIT.getURIasString());
			uom.setFundamental(isfundamental.equals("true"));

			// Store the unit's factoring info. Need to do this with raw OWL API b/c 
			// you can have the same hasUnitFactor axiom but with different annotations.
			// So we can't just use SemSimOWLFactory.getIndObjectProperty().
			OWLNamedIndividual unitOWLind = factory.getOWLNamedIndividual(IRI.create(unitind));
			OWLObjectProperty hasunitfactorprop = factory.getOWLObjectProperty(SemSimRelation.HAS_UNIT_FACTOR.getIRI());
			Set<OWLObjectPropertyAssertionAxiom> oopaas = ont.getObjectPropertyAssertionAxioms(unitOWLind);
			
			// Go through all the object assertion axioms on this individual
			for(OWLObjectPropertyAssertionAxiom oopaa : oopaas){
				
				OWLObjectPropertyExpression readprop = oopaa.getProperty();
				
				// If the assertion uses the "hasUnitFactor" property, collect the required info
				if(readprop.equals(hasunitfactorprop)){
					OWLIndividual baseunitOWLind = oopaa.getObject();
					String baseunitind = baseunitOWLind.asOWLNamedIndividual().getIRI().toString();
					String factorunitcode = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, baseunitind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
					
					if(!factorunitcode.equals("") && factorunitcode!=null){
						UnitOfMeasurement baseunit = semsimmodel.getUnit(factorunitcode);
						
						if(baseunit==null){
							baseunit = new UnitOfMeasurement(factorunitcode);
							semsimmodel.addUnit(baseunit);
						}
						
						double exponent = 1.0;
						String prefix = null;
						double multiplier = 1.0;
						
						OWLAnnotationProperty unitfactorexpprop = factory.getOWLAnnotationProperty(SemSimRelation.UNIT_FACTOR_EXPONENT.getIRI());
						OWLAnnotationProperty unitfactorprefixprop = factory.getOWLAnnotationProperty(SemSimRelation.UNIT_FACTOR_PREFIX.getIRI());
						OWLAnnotationProperty unitfactormultprop = factory.getOWLAnnotationProperty(SemSimRelation.UNIT_FACTOR_MULTIPLIER.getIRI());
						
						
						if(!oopaa.getAnnotations(unitfactorexpprop).isEmpty()){
							OWLLiteral litval = (OWLLiteral) oopaa.getAnnotations(unitfactorexpprop).toArray(new OWLAnnotation[]{})[0].getValue();
							exponent = litval.parseDouble();
						}
						
						if(!oopaa.getAnnotations(unitfactorprefixprop).isEmpty()){
							OWLLiteral litval = (OWLLiteral) oopaa.getAnnotations(unitfactorprefixprop).toArray(new OWLAnnotation[]{})[0].getValue();
							prefix = litval.getLiteral();
						}
						
						if(!oopaa.getAnnotations(unitfactormultprop).isEmpty()){
							OWLLiteral litval = (OWLLiteral) oopaa.getAnnotations(unitfactormultprop).toArray(new OWLAnnotation[]{})[0].getValue();
							multiplier = litval.parseDouble();
						}
						uom.addUnitFactor(new UnitFactor(baseunit, exponent, prefix, multiplier));
					}
				}
			}
		}
	}
		
	/** Go through existing data structures and establish the hasInput relationships */
	private void establishIsInputRelationships() throws OWLException {
		
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString())){
			String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
			
			DataStructure ds = semsimmodel.getAssociatedDataStructure(name);
			
			SemSimUtil.setComputationInputsForDataStructure(semsimmodel, ds, null);
			
			// set the data structure's solution domain
			String soldom = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, dsind, SemSimRelation.HAS_SOLUTION_DOMAIN.getURIasString());
			semsimmodel.getAssociatedDataStructure(name).setSolutionDomain(semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(soldom)));
		}
	}
	
	private void collectRelationalConstraints() throws OWLException {
		for(String relind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.RELATIONAL_CONSTRAINT.getURIasString())){
			String mmleq = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, relind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
			String mathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, relind, SemSimRelation.HAS_MATHML.getURIasString());
			semsimmodel.addRelationalConstraint(new RelationalConstraint(mmleq, mathml));
		}
	}
	
	private void collectEvents() throws OWLException{
		
		for(String eventind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.EVENT.getURIasString())){
			String eventname = SemSimOWLFactory.getIRIfragment(eventind);
			Event ssevent = new Event();
			ssevent.setName(eventname);
			String triggermathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, eventind,
					SemSimRelation.HAS_TRIGGER_MATHML.getURIasString());
			ssevent.setTriggerMathML(triggermathml);
			
			// Get priority mathml, delay mathml and time units
			String prioritymathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, eventind, SemSimRelation.HAS_PRIORITY_MATHML.getURIasString());
			String delaymathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, eventind, SemSimRelation.HAS_DELAY_MATHML.getURIasString());
			String timeunituri = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, eventind, SemSimRelation.HAS_TIME_UNIT.getURIasString());

			// Set priority
			if(! prioritymathml.equals("") && prioritymathml!=null) ssevent.setPriorityMathML(prioritymathml);
			
			// Set delay
			if(! delaymathml.equals("") && delaymathml!=null) ssevent.setDelayMathML(delaymathml);
			
			// Set time units
			if(! timeunituri.equals("") && timeunituri!=null){
				String unitname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, 
						timeunituri, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
				UnitOfMeasurement timeunit = semsimmodel.getUnit(unitname);
				ssevent.setTimeUnit(timeunit);
			}
			
			// Process event assignments
			for(String eaind : SemSimOWLFactory.getIndObjectProperty(ont, eventind, SemSimRelation.HAS_EVENT_ASSIGNMENT.getURIasString())){
				EventAssignment ea = ssevent.new EventAssignment();
				ssevent.addEventAssignment(ea);
				String eamathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, eaind, SemSimRelation.HAS_MATHML.getURIasString());
				ea.setMathML(eamathml);
				String outputuri = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, eaind, SemSimRelation.HAS_OUTPUT.getURIasString());
				String outputname = SemSimOWLFactory.getIRIfragment(outputuri);
				DataStructure outputds = semsimmodel.getAssociatedDataStructure(outputname);
				ea.setOutput(outputds);
				outputds.getComputation().addEvent(ssevent);
			}
			
			semsimmodel.addEvent(ssevent);
			
		}
	}

	
	private void collectCustomAnnotations() {
		
		// Get additional annotations on custom terms
		// Make sure to get reference classes that are there to define custom classes
		SemSimTypes[] customclasses = new SemSimTypes[]{SemSimTypes.CUSTOM_PHYSICAL_ENTITY, SemSimTypes.CUSTOM_PHYSICAL_PROCESS};
		
		// For the two custom physical model classes...
		for(SemSimTypes customclass : customclasses){	
			
			// For each custom term in them...
			for(String custstring : SemSimOWLFactory.getIndividualsAsStrings(ont, customclass.getURIasString())){
				
				OWLNamedIndividual custind = factory.getOWLNamedIndividual(IRI.create(custstring));
				
				// For each super class that is not the custom physical component class itself...
				for(OWLClassExpression supercls : custind.asOWLNamedIndividual().getTypes(ont)){
					
					// If the superclass is anonymous
					if(supercls.isAnonymous()){
						
						RestrictionVisitor restrictionVisitor = new RestrictionVisitor(Collections.singleton(ont));
				        supercls.accept(restrictionVisitor);
				        
				        for(OWLObjectPropertyExpression owlprop : restrictionVisitor.getPropertyFillerMap().keySet()){
				        	
				        	OWLClassExpression filler = restrictionVisitor.getPropertyFillerMap().get(owlprop);
				        	
				        	if(! filler.isAnonymous()){
				        		
				        		OWLClass reftermowlclass = filler.asOWLClass();
				        		URI reftermURI = reftermowlclass.getIRI().toURI();
				        		
								String label = SemSimOWLFactory.getRDFLabels(ont, reftermowlclass)[0];
								
								// Add reference terms to model and get the physical model component for
								// each custom object
								PhysicalModelComponent pmc = null;
								
								if(customclass==SemSimTypes.CUSTOM_PHYSICAL_PROCESS) {
									semsimmodel.addReferencePhysicalProcess(new ReferencePhysicalProcess(reftermURI, label));
									pmc = semsimmodel.getCustomPhysicalProcessByName(SemSimOWLFactory.getRDFLabels(ont, custind)[0]);
								}
								
								if(customclass==SemSimTypes.CUSTOM_PHYSICAL_ENTITY) {
									semsimmodel.addReferencePhysicalEntity(new ReferencePhysicalEntity(reftermURI, label));
									pmc = semsimmodel.getCustomPhysicalEntityByName(SemSimOWLFactory.getRDFLabels(ont, custind)[0]);
								}
								
								String propstring = owlprop.getNamedProperty().getIRI().toString();
								
								// If we've got the physical model component object, add the annotations
								if(pmc!=null){

									Relation rel = SemSimRelations.getRelationFromURI(URI.create(propstring));
									if (rel!=SemSimRelation.UNKNOWN) {
										pmc.addReferenceOntologyAnnotation(rel, reftermURI, label, sslib);
									}
								}
								else semsimmodel.addError("Attempt to apply reference ontology annotation " + propstring + " to " + custstring + " failed."
										+ "\nCould not find individual in set of processed physical model components");
							}
				        }
					}
				}
			}
		}
	}
	
	private void collectSubModels() throws OWLException {
		// Collect the submodels
		Set<String> subset = SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.SUBMODEL.getURIasString());
		
		for(String sub : subset){
			String subname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimRelation.HAS_NAME.getURIasString());
			
			boolean hascomputation = !SemSimOWLFactory.getIndObjectProperty(ont, sub, SemSimRelation.HAS_COMPUTATIONAL_COMPONENT.getURIasString()).isEmpty();
			
			// Get all associated data structures
			Set<String> dss = SemSimOWLFactory.getIndObjectProperty(ont, sub, SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE.getURIasString());
			
			// Get importedFrom value
			String importval = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimRelation.IMPORTED_FROM.getURIasString());
			Submodel sssubmodel = null;
			
			// If submodel IS NOT imported
			if(importval.equals("") || importval==null){
				sssubmodel = (hascomputation) ? new FunctionalSubmodel(subname, subname, null, null) : new Submodel(subname);
				String componentmathml = null;
				String componentmathmlwithroot = null;
				String description = SemSimOWLFactory.getRDFcomment(ont, factory.getOWLNamedIndividual(IRI.create(sub)));
				if (!description.isEmpty()) sssubmodel.setDescription(description);
				
				// If computation associated with submodel, store mathml
				if(sssubmodel.isFunctional()){
					String comp = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, sub, SemSimRelation.HAS_COMPUTATIONAL_COMPONENT.getURIasString());
					if(comp!=null && !comp.equals("")){
						componentmathml = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, comp, SemSimRelation.HAS_MATHML.getURIasString());
						if(componentmathml!=null && !componentmathml.equals("")){
							((FunctionalSubmodel)sssubmodel).getComputation().setMathML(componentmathml);
							componentmathmlwithroot = "<temp>\n" + componentmathml + "\n</temp>";
						}
					}
				}
				
				// Associate data structures with the model submodel
				for(String dsname : dss){
					DataStructure theds = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsname));
					String localvarname = theds.getName().substring(theds.getName().lastIndexOf(".")+1,theds.getName().length());
					sssubmodel.addDataStructure(theds);
					
					// white box the equations
					if(componentmathml!=null && !componentmathml.equals("")){
						SAXBuilder builder = new SAXBuilder();
						
						try {
							Document doc = builder.build(new StringReader(componentmathmlwithroot));
							
							if(doc.getRootElement() instanceof Element){
								List<?> mathmllist = doc.getRootElement().getChildren("math", RDFNamespace.MATHML.createJdomNamespace());
								
								// Check if variable is solved using ODE, and is a CellML-type variable, set startValue to CellML intial value.
								Boolean ode = CellMLreader.isSolvedbyODE(localvarname, mathmllist);
								
								Element varmathmlel = CellMLreader.getMathMLforOutputVariable(localvarname, mathmllist);
								
								if(varmathmlel!=null){
									XMLOutputter xmloutputter = new XMLOutputter();
									String varmathml = xmloutputter.outputString(varmathmlel);
									theds.getComputation().setMathML(varmathml);
									
									// Assign human-readable computational code, if not already present
									if(theds.getComputation().getComputationalCode()==null ||
											theds.getComputation().getComputationalCode().equals("")){
										
										String RHS = CellMLreader.getRHSofDataStructureEquation(varmathml, localvarname);
										
										if(RHS != null) {
											String soldomname = theds.hasSolutionDomain() ? "(" + theds.getSolutionDomain().getName() + ")" : "t";
											String LHS = ode ? "d(" + localvarname + ")/d" + soldomname + " = " : localvarname + " = ";
											theds.getComputation().setComputationalCode(LHS + RHS);
										}
									}
									
									CellMLreader.whiteBoxFunctionalSubmodelEquations(varmathmlel, subname, semsimmodel, theds);
									
									if(ode && (theds instanceof MappableVariable)){
										MappableVariable mv = (MappableVariable)theds;
										mv.setStartValue(mv.getCellMLinitialValue());
									}
								}
							}
						} catch (JDOMException | IOException e) {
							e.printStackTrace();
						}
					}
					
					// If the human-readable computation code hasn't been found, and theds is a mappable variable,
					// and there's a CellML initial value, use the initial value for the computational code
					if(theds instanceof MappableVariable){
						MappableVariable mv = (MappableVariable)theds;
						
						if((mv.getComputation().getComputationalCode()==null ||
								mv.getComputation().getComputationalCode().equals("")) &&
								(mv.getCellMLinitialValue()!=null && !mv.getCellMLinitialValue().equals(""))){ // Have to check for empty string
																											   // b/c that's what getCellMLinitialValue
																											   // is initialized to.
							
							theds.getComputation().setComputationalCode(localvarname + " = " + mv.getCellMLinitialValue());
						}
					}
				}
				
				// Set the description of the submodel
				semsimmodel.addSubmodel(sssubmodel);
			}
			// If submodel IS imported
			else{
				String referencename = getStringValueFromAnnotatedDataPropertyAxiom(ont, sub, SemSimRelation.IMPORTED_FROM.getURI(),
						importval, SemSimRelation.REFERENCE_NAME_OF_IMPORT.getURI());
				sssubmodel = 
						SemSimComponentImporter.importFunctionalSubmodel(
								modelaccessor.getFileThatContainsModel(),
								semsimmodel, subname, referencename, importval, sslib);
			}
		}
		
		// If a sub-model has sub-models, add that info to the model, store subsumption types as annotations
		for(String sub : subset){
			String subname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, sub, SemSimRelation.HAS_NAME.getURIasString());
			Set<String> subsubset = SemSimOWLFactory.getIndObjectProperty(ont, sub, SemSimRelation.INCLUDES_SUBMODEL.getURIasString());
			for(String subsub : subsubset){
				String subsubname = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, subsub, SemSimRelation.HAS_NAME.getURIasString());
				Submodel subsubmodel = semsimmodel.getSubmodel(subsubname);
				
				// Assign the subsumption type (for CellML-type submodels)
				Set<String> reltypes = getSubmodelSubsumptionRelationship(ont, sub, SemSimRelation.INCLUDES_SUBMODEL, subsub);
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

	// Get the multiplier for a process participant
	private Double getMultiplierForProcessParticipant(OWLOntology ont, String process, Relation prop, String ent){
		Double val = 1.0;
		OWLIndividual procind = factory.getOWLNamedIndividual(IRI.create(process));
		OWLIndividual entind = factory.getOWLNamedIndividual(IRI.create(ent));
		OWLObjectProperty owlprop = factory.getOWLObjectProperty(prop.getIRI());
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(owlprop, procind, entind);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(SemSimRelation.HAS_MULTIPLIER.getIRI());
		for(OWLAxiom ax : ont.getAxioms(procind)){
			if(ax.equalsIgnoreAnnotations(axiom)){
				if(!ax.getAnnotations(annprop).isEmpty()){
					OWLLiteral litval = (OWLLiteral) ax.getAnnotations(annprop).toArray(new OWLAnnotation[]{})[0].getValue();
					if (litval.isInteger()) {
						val = (double)litval.parseInteger();
					}
					else val = litval.parseDouble();
				}
			}
		}
		return val;
	}
	
	// Get the relationship for a submodel subsumption
	private Set<String> getSubmodelSubsumptionRelationship(OWLOntology ont, String submodel, Relation prop, String subsubmodel){
		Set<String> vals = new HashSet<String>();
		OWLIndividual procind = factory.getOWLNamedIndividual(IRI.create(submodel));
		OWLIndividual entind = factory.getOWLNamedIndividual(IRI.create(subsubmodel));
		OWLObjectProperty owlprop = factory.getOWLObjectProperty(prop.getIRI());
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(owlprop, procind, entind);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(SemSimRelation.CELLML_COMPONENT_SUBSUMPTION_TYPE.getIRI());
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
	
	
	// Get a string value from an annotation on an datatype property axiom (individual > prop > datatype)
	private String getStringValueFromAnnotatedDataPropertyAxiom(OWLOntology ont, String subject, URI pred, String data, URI annpropuri){
		OWLIndividual subjectind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLDataProperty owlprop = factory.getOWLDataProperty(IRI.create(pred));
		OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(owlprop, subjectind, data);
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(IRI.create(annpropuri));
		
		String val = null;
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
	
	private PhysicalModelComponent getReferenceTerm(String referencekey, String description) {
		PhysicalModelComponent term = idpropertymap.get(referencekey);
		if (term==null) {
			term = new PhysicalProperty(description, URI.create(referencekey));
			identitymap.put(referencekey, term);
			semsimmodel.addPhysicalProperty((PhysicalProperty) term);
		}
		return term;
	}
	private PhysicalModelComponent getClassofIndividual(String ind) throws OWLException {
		String indclass = SemSimOWLFactory.getFunctionalIndDatatypeProperty(ont, ind, physicaldefinitionURI.toString());
		if (indclass.isEmpty()) {
			String sub = ind.subSequence(ind.lastIndexOf("_"), ind.length()).toString();
			PhysicalModelComponent pmc = identitymap.get(ind.replace(sub, ""));
			//Catch unmarked custom entities
			if (pmc==null) pmc = makeCustomEntity(ind);
			return pmc; 
			
		}
		return identitymap.get(indclass);
	}
	
	/** Make Custom Entity **/
	private CustomPhysicalEntity makeCustomEntity(String cuperef) {
		String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLClass(IRI.create(cuperef)))[0];
		
		String sub = cuperef.subSequence(cuperef.lastIndexOf("_"), cuperef.length()).toString();
		cuperef = cuperef.replace(sub, "");
		if (identitymap.containsKey(cuperef)) return (CustomPhysicalEntity) identitymap.get(cuperef);
		
		if (label.isEmpty()) { 
			label = cuperef.subSequence(cuperef.lastIndexOf("/"), cuperef.length()).toString();
			label = label.replace("_", " ");
		}
		CustomPhysicalEntity cupe = new CustomPhysicalEntity(label, null);
		
		if(SemSimOWLFactory.getRDFComments(ont, cuperef)!=null)
			cupe.setDescription(SemSimOWLFactory.getRDFComments(ont, cuperef)[0]);
		label = cuperef.replace(sub, "");
		
		identitymap.put(cuperef.replace(sub, ""), cupe);
		return semsimmodel.addCustomPhysicalEntity(cupe);
	}
	
	/** 
	 * Produces a composite for a singular term.
	 * */
	private CompositePhysicalEntity createSingularComposite(String uri) throws OWLException {
		if (identitymap.containsKey(uri)) return (CompositePhysicalEntity) identitymap.get(uri);
		ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
		entlist.add((PhysicalEntity) getClassofIndividual(uri));
		CompositePhysicalEntity cpe = new CompositePhysicalEntity(entlist, new ArrayList<StructuralRelation>());
		
		semsimmodel.addCompositePhysicalEntity(cpe);
		identitymap.put(uri, cpe);
		return cpe;	
	}
}
