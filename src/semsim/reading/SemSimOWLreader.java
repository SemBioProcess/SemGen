package semsim.reading;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
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
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import semsim.SemSimLibrary;
import semsim.annotation.Annotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Event;
import semsim.model.computational.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.SBMLInitialAssignment;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.MMLchoice;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.datastructures.SBMLFunctionOutput;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEnergyDifferential;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.model.physical.object.ReferencePhysicalDependency;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.owl.SemSimOWLFactory.RestrictionVisitor;
import semsim.utilities.SemSimUtil;

/**
 * Class for reading a SemSim .owl file and creating a SemSimModel 
 * object containing the modeling information contained therein.
 * @author mneal
 */
public class SemSimOWLreader extends ModelReader {
	private OWLDataFactory factory;
	private Map<String, PhysicalModelComponent> identitymap = new HashMap<String, PhysicalModelComponent>();
	private Map<String, ReferencePhysicalDependency> iddependencymap = new HashMap<String, ReferencePhysicalDependency>();

	private Map<String, PhysicalPropertyInComposite> idpropertymap = new HashMap<String, PhysicalPropertyInComposite>();
	private OWLOntology ont;
	private URI physicaldefinitionURI = SemSimRelation.HAS_PHYSICAL_DEFINITION.getURI();
	
	public SemSimOWLreader(ModelAccessor accessor) {
		super(accessor);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		semsimmodel.setName(modelaccessor.getModelName());
		
		try {
			InputStream stream = accessor.modelInStream();
			ont = manager.loadOntologyFromOntologyDocument(stream);
			stream.close();
		} catch (OWLOntologyCreationException | IOException e) {
			e.printStackTrace();
		}
		
		// Look at top class. If it uses an old SemSim namespace, rename OWL entities so they use new namespace
		OWLClass topclassnew = factory.getOWLClass(IRI.create(SemSimTypes.SEMSIM_COMPONENT.getURIasString()));
		OWLClass topclassold = factory.getOWLClass(IRI.create("http://www.bhi.washington.edu/SemSim#SemSim_component"));
		
		if( ont.getClassesInSignature().contains(topclassold) ) {
			
			setPhysicalDefinitionURI(); // Only need to accomodate older refersTo identity relation if old semsim namespace is used. Deal with that here.

			// Set the SemSim namespace (to accommodate older models that use SemSim#, not semsim/)
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ont));

			// Rename object and data properties
			for(OWLEntity entity: ont.getSignature()) {
				IRI oldiri = entity.getIRI();
				
				if(oldiri.toString().startsWith("http://www.bhi.washington.edu/SemSim#"))
					manager.applyChanges(renamer.changeIRI(oldiri, IRI.create(RDFNamespace.SEMSIM.getNamespaceAsString() + oldiri.getFragment())));
			}
			
			//TODO:
			// Replace all occurrences of old SemSim namespace with new one
			// This ensures that SemSim prefix mapping is up-to-date
			OWLOntologyFormat oof = manager.getOntologyFormat(ont);
			oof.asPrefixOWLOntologyFormat().setPrefix("SemSim", RDFNamespace.SEMSIM.getNamespaceAsString());
			manager.setOntologyFormat(ont, oof);
			oof = manager.getOntologyFormat(ont);
			
		}
		else if( ! ont.getClassesInSignature().contains(topclassnew))
			semsimmodel.addError("ERROR: Could not determine SemSim namespace for model");
		
	}
	
	
	//*****************************READ METHODS*************************//
		
	@Override
	public SemSimModel read() throws OWLException, JDOMException, IOException{	
		if (verifyModel()) return semsimmodel;
		
		collectModelLevelAnnotations();
		collectReferenceClasses();
		collectCompositeEntities();
		collectProcesses();
		collectForces();
		collectDataStructures();
		mapCellMLTypeVariables();
		collectUnits();
		collectRelationalConstraints();	
		collectEvents();
		collectSBMLinitialAssignments();
		establishIsInputRelationships();
		collectCustomAnnotations();		
		collectSubModels();
		
		return semsimmodel;
	}
	
	/** Verify the model is a valid SemSimModel 
	 * @return Whether the SemSimModel is valid
	 * @throws OWLException*/
	private boolean verifyModel() throws OWLException {
		OWLClass topclass = factory.getOWLClass(IRI.create(SemSimTypes.SEMSIM_COMPONENT.getURIasString()));

		if( ! ont.getClassesInSignature().contains(topclass))
				semsimmodel.addError("Could not find root class 'SemSim_component'. Source file does not appear to be a valid SemSim model");
		
		// Test if the model actually has data structures
		if(SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString()).isEmpty()
				&& SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_PROPERTY.getURIasString()).isEmpty()) {
			
			semsimmodel.addError("Model contains no data structures or physical properties");
		}
		
		return (semsimmodel.getErrors().size() > 0);
	}
	
	/** Determine which qualifier should be used to read in physical definitions for physical model components.
	 * This is needed because older SemSim models used the "refersTo" relation to link a model component
	 * to its physical definition. More recent models use the "hasPhysicalDefinition" relation.
	 */
	private void setPhysicalDefinitionURI(){
		if(ont.containsDataPropertyInSignature(IRI.create("http://www.bhi.washington.edu/SemSim#refersTo"))) // toLowerCase() needed to accommodate previous use of SemSim namespace that used some uppercase letters
			physicaldefinitionURI = URI.create(RDFNamespace.SEMSIM.getNamespaceAsString() + "refersTo"); // toLowerCase() needed to accommodate previous use of SemSim namespace that used some uppercase letters
		
		else if(ont.containsDataPropertyInSignature(SemSimRelation.HAS_PHYSICAL_DEFINITION.getIRI()))
			physicaldefinitionURI = SemSimRelation.HAS_PHYSICAL_DEFINITION.getURI();
	}
	
	
	/**
	 * Collect all the annotations that are at the level of the whole model.
	 * @throws JDOMException
	 * @throws IOException
	 */
	private void collectModelLevelAnnotations() throws JDOMException, IOException {
		// Get model-level annotations
		Set<OWLAnnotation> anns = ont.getAnnotations();
		
		// Annotations with values that are read into object fields are removed from the 
		// OWLAnnotation set and not stored as Annotations on the model
		Set<OWLAnnotation> annstoremove = new HashSet<OWLAnnotation>();  
		
		for (OWLAnnotation named : anns) {
			
			if (named.getProperty().getIRI().equals(SemSimLibrary.SEMSIM_VERSION_IRI)) {
				semsimmodel.setSemSimVersion(((OWLLiteral)named.getValue()).getLiteral());
				annstoremove.add(named);
			}
			
			if (named.getProperty().getIRI().equals(SemSimModel.LEGACY_CODE_LOCATION_IRI)) {
				String location = ((OWLLiteral)named.getValue()).getLiteral();
				
				if( ! location.isEmpty()){
					ModelAccessor ma = FileAccessorFactory.getModelAccessor(location);
					semsimmodel.setSourceFileLocation(ma);
					annstoremove.add(named);
				}
			}
			
			if(named.getProperty().getIRI().equals(SemSimRelation.MODEL_METADATA_ID.getIRI())){
				semsimmodel.setMetadataID(((OWLLiteral)named.getValue()).getLiteral());
				annstoremove.add(named);
			}	
			
			if(named.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_COMMENT.getIRI())){
				semsimmodel.setDescription(((OWLLiteral)named.getValue()).getLiteral());
				annstoremove.add(named);
			}
			
			if(named.getProperty().getIRI().equals(SemSimRelation.MODEL_CREATOR.getIRI())) {
				// Collect info associated with the creator individual
				IRI creatoriri = (IRI)named.getValue();
				
//				System.out.println(named.getValue().getClass()); IRI
//				System.out.println(named.isDeprecatedIRIAnnotation()); false

			}
		}
		
		anns.removeAll(annstoremove);
		
		//Add remaining annotations
		for(OWLAnnotation ann : anns){
			URI propertyuri = ann.getProperty().getIRI().toURI();
			Relation rel = SemSimRelations.getRelationFromURI(propertyuri);
			
			if(rel != SemSimRelation.UNKNOWN ){
				
				rel = SemSimRelations.getSynonymousModelLevelRelationForSemSimOWLreading(rel);
				
				if(ann.getValue() instanceof OWLLiteral){
					OWLLiteral val = (OWLLiteral) ann.getValue();
					
					if(val.getLiteral().startsWith("http"))
						semsimmodel.addReferenceOntologyAnnotation(rel, URI.create(val.getLiteral()), "", sslib);
					else
						semsimmodel.addAnnotation(new Annotation(rel, val.getLiteral()));
				}
			}
		}
	}

	
	/**
	 * Collect all classes in the OWL file that are terms from reference ontologies. 
	 * @throws OWLException
	 */
	private void collectReferenceClasses() throws OWLException {
		
		// Collect physical properties
		for (String refuri : SemSimOWLFactory.getAllSubclasses(ont,  SemSimTypes.PHYSICAL_PROPERTY.getURIasString(), false)) {
			String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLClass(IRI.create(refuri)))[0];
			if (label.isEmpty()) continue;
			
			PhysicalPropertyInComposite pp = new PhysicalPropertyInComposite(label, URI.create(refuri));
			
			semsimmodel.addPhysicalPropertyForComposite(pp);
			idpropertymap.put(refuri, pp);
		}

		// Collect physical entities
		for (String rperef : SemSimOWLFactory.getAllSubclasses(ont,  SemSimTypes.REFERENCE_PHYSICAL_ENTITY.getURIasString(), false)) {
			String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLClass(IRI.create(rperef)))[0];
			PhysicalEntity pe;
			if( ! rperef.toString().toLowerCase().startsWith("http://www.bhi.washington.edu/semsim/")){  // Account for older models that used refersTo pointers to custom annotations
				
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
		
		// Collect reference physical dependencies
		for(String rpdcls : SemSimOWLFactory.getAllSubclasses(ont, SemSimTypes.REFERENCE_PHYSICAL_DEPENDENCY.getURIasString(), false)){
			String label = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLClass(IRI.create(rpdcls)))[0];
			ReferencePhysicalDependency rpd = new ReferencePhysicalDependency(URI.create(rpdcls), label);
			semsimmodel.addReferencePhysicalDependency(rpd);
			iddependencymap.put(rpdcls, rpd);
		}
	}
	
	
	/**
	 * Collect all the composite physical entities in the OWL file and instantiate 
	 * the corresponding {@link CompositePhysicalEntity} objects
	 * @throws OWLException
	 */
	private void collectCompositeEntities() throws OWLException {
		for (String cperef : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont,  SemSimTypes.COMPOSITE_PHYSICAL_ENTITY.getURIasString())) {		
			String ind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, cperef, StructuralRelation.HAS_INDEX_ENTITY.getURIasString());
			String index = ind;
			ArrayList<PhysicalEntity> rpes = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
			
			PhysicalEntity rpe = (PhysicalEntity)getClassofIndividual(ind);
			rpes.add(rpe);
			
			while (true) {
				String nextind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, ind.toString(), StructuralRelation.PART_OF.getURIasString());
				StructuralRelation rel = StructuralRelation.PART_OF;
				if (nextind=="") {
					nextind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, ind.toString(), StructuralRelation.CONTAINED_IN.getURIasString());
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
	
	/**
	 * Collect the physical processes represented in the SemSim OWL file
	 * @throws OWLException
	 */
	private void collectProcesses() throws OWLException {
		
		// For each process instance in the class SemSim:Physical_process
		for(String processind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_PROCESS.getURIasString())){
			
			OWLEntity processowlind = factory.getOWLNamedIndividual(IRI.create(processind));
			String processlabel = SemSimOWLFactory.getRDFLabel(ont, processowlind);
			String description = SemSimOWLFactory.getRDFcomment(ont, processowlind);
						
			PhysicalProcess pproc = null;
			// Create reference physical process, if there is an annotation
			String hasphysicaldef = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, processind, physicaldefinitionURI.toString());
			
			if ( ! hasphysicaldef.isEmpty())
				pproc = semsimmodel.addReferencePhysicalProcess(new ReferencePhysicalProcess(URI.create(hasphysicaldef), processlabel));
			
			// Otherwise create a custom physical process
			else pproc = semsimmodel.addCustomPhysicalProcess(new CustomPhysicalProcess(processlabel, description));
			
			
			// Capture the physical entity participants
			Set<String> srcs = SemSimOWLFactory.getIndObjectPropertyObjects(ont, processind, SemSimRelation.HAS_SOURCE.getURIasString());
			// Enter source information
			for(String src : srcs){
				CompositePhysicalEntity srcent = (CompositePhysicalEntity)identitymap.get(src);
				
				if (srcent==null) srcent = createSingularComposite(src);
				
				Double m = getMultiplierForProcessParticipant(ont, processind, SemSimRelation.HAS_SOURCE, src);
				pproc.addSource(srcent, m);
			}
			// Enter sink info
			Set<String> sinks = SemSimOWLFactory.getIndObjectPropertyObjects(ont, processind, SemSimRelation.HAS_SINK.getURIasString());
			for(String sink : sinks){
				CompositePhysicalEntity sinkent = (CompositePhysicalEntity)identitymap.get(sink);
				if (sinkent==null) { 
					sinkent = createSingularComposite(sink);
				}
				Double m = getMultiplierForProcessParticipant(ont, processind, SemSimRelation.HAS_SINK, sink);
				pproc.addSink(sinkent, m);
			}
			
			// Enter mediator info
			Set<String> mediators = SemSimOWLFactory.getIndObjectPropertyObjects(ont, processind, SemSimRelation.HAS_MEDIATOR.getURIasString());
			
			for(String med : mediators){
				
				CompositePhysicalEntity medent = (CompositePhysicalEntity)identitymap.get(med);
				
				if (medent==null) medent = createSingularComposite(med);
				
				pproc.addMediator(medent);
			}
			identitymap.put(processind, pproc);
		}
	}
	
	
	/**
	 * Collect the physical forces represented in the SemSim OWL file
	 * @throws OWLException
	 */
	private void collectForces() throws OWLException {
		
		// For each force instance in the class SemSim:Physical_force
		for(String forceind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_FORCE.getURIasString())){
			
			Set<String> srcs = SemSimOWLFactory.getIndObjectPropertyObjects(ont, forceind, SemSimRelation.HAS_SOURCE.getURIasString());
			Set<String> sinks = SemSimOWLFactory.getIndObjectPropertyObjects(ont, forceind, SemSimRelation.HAS_SINK.getURIasString());

			if(srcs.size()==0 && sinks.size()==0) continue;
			
			CustomPhysicalEnergyDifferential cpf = semsimmodel.addCustomPhysicalForce(new CustomPhysicalEnergyDifferential());
			
			// Capture the physical entity participants
			// Enter source information first
			for(String src : srcs){
				CompositePhysicalEntity srcent = (CompositePhysicalEntity)identitymap.get(src);
				
				if (srcent==null) srcent = createSingularComposite(src);
				
				cpf.addSource(srcent);
			}
			// Enter sink info
			for(String sink : sinks){
				CompositePhysicalEntity sinkent = (CompositePhysicalEntity)identitymap.get(sink);
				if (sinkent==null) sinkent = createSingularComposite(sink);
				
				cpf.addSink(sinkent);
			}
			
			identitymap.put(forceind, cpf);
		}
	}
	
	
	

	/**
	 * Collect information for the data structures in the SemSim OWL file
	 * @throws OWLException
	 */
	private void collectDataStructures() throws OWLException {
		
		// Get data structures and add them to model - Decimals, Integers, MMLchoice
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString())){
			String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
			String computationind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, dsind, SemSimRelation.IS_OUTPUT_FOR.getURIasString());
			String compcode = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, computationind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
			String mathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, computationind, SemSimRelation.HAS_MATHML.getURIasString());
			String depind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, computationind, SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getURIasString());
			String description = SemSimOWLFactory.getRDFcomment(ont, factory.getOWLNamedIndividual(IRI.create(dsind)));
			
			DataStructure ds = null;
						
			// If the data structure is a decimal
			if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.DECIMAL.getURIasString(), ont)){
				
				// If it's not a CellML-type variable
				if(SemSimOWLFactory.getIndDatatypePropertyValues(ont, dsind, SemSimRelation.CELLML_COMPONENT_PUBLIC_INTERFACE.getURIasString()).isEmpty()
						&& SemSimOWLFactory.getIndDatatypePropertyValues(ont, dsind, SemSimRelation.CELLML_COMPONENT_PRIVATE_INTERFACE.getURIasString()).isEmpty()
						&& SemSimOWLFactory.getIndDatatypePropertyValues(ont, dsind, SemSimRelation.CELLML_INITIAL_VALUE.getURIasString()).isEmpty()
						&& SemSimOWLFactory.getIndObjectPropertyObjects(ont, dsind, SemSimRelation.MAPPED_TO.getURIasString()).isEmpty()
						&& !SemSimOWLFactory.getIndObjectPropertyObjects(ont, dsind, SemSimRelation.IS_OUTPUT_FOR.getURIasString()).isEmpty()){
					
					ds = new Decimal(name);
				}
				else ds = new MappableVariable(name);
			}
			
			// If the data structure is an SBML function output
			else if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.SBML_FUNCTION_OUTPUT.getURIasString(), ont))
					ds = new SBMLFunctionOutput(name);
			
			// If an integer
			else if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.INTEGER.getURIasString(), ont))
				ds = new SemSimInteger(name);
			
			// If an MML choice variable
			else if(SemSimOWLFactory.indExistsInClass(dsind, SemSimTypes.MMLCHOICE.getURIasString(), ont))
				ds = new MMLchoice(name);
						
			semsimmodel.addDataStructure(ds);
			
			if( ! compcode.isEmpty()) ds.getComputation().setComputationalCode(compcode);
			
			if( ! mathml.isEmpty()) ds.getComputation().setMathML(mathml);
			
			if( ! description.isEmpty()) ds.setDescription(description);
			
			// Set the data property values: startValue, isDeclared, isSolutionDomain
			String startval = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.HAS_START_VALUE.getURIasString());
			if( ! startval.isEmpty()) ds.setStartValue(startval);
			
			String isdeclared = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.IS_DECLARED.getURIasString());
			if( ! isdeclared.isEmpty()) ds.setDeclared(Boolean.parseBoolean(isdeclared));
			else ds.setDeclared(true); // default value is TRUE if nothing explicitly stated in OWL file

			String issoldom = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.IS_SOLUTION_DOMAIN.getURIasString());
			if( ! issoldom.isEmpty()) {
				ds.setIsSolutionDomain(Boolean.parseBoolean(issoldom));
			}
			else ds.setIsSolutionDomain(false); // default value is FALSE if nothing explicitly stated in OWL file
			
			String metadataid = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.METADATA_ID.getURIasString());
			semsimmodel.assignValidMetadataIDtoSemSimObject(metadataid, ds);
			
			// Collect singular physical definition annotation, if present
			String physdefvalds = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, physicaldefinitionURI.toString());
			
			// If the data structure is annotated, store annotation
			if( ! physdefvalds.isEmpty()){	
				String reflabel = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(dsind)))[0];
				ds.setSingularAnnotation((PhysicalProperty)getReferenceProperty(physdefvalds, reflabel));
			}
			
			// If a CellML-type variable, get interface values and initial value
			if(ds instanceof MappableVariable){
				String pubint = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.CELLML_COMPONENT_PUBLIC_INTERFACE.getURIasString());
				String privint = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.CELLML_COMPONENT_PRIVATE_INTERFACE.getURIasString());
				
				if(! pubint.isEmpty()) ((MappableVariable)ds).setPublicInterfaceValue(pubint);
				
				if(! privint.isEmpty()) ((MappableVariable)ds).setPrivateInterfaceValue(privint);
				
				String initval = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, dsind, SemSimRelation.CELLML_INITIAL_VALUE.getURIasString());
				
				if(initval != null && ! initval.isEmpty()) ((MappableVariable)ds).setCellMLinitialValue(initval);
			}
			
			// Set object properties: hasUnit and isComputationalComponentFor
			// OWL versions of semsim models have the units linked to the data structure, not the property
			
			String units = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, dsind, SemSimRelation.HAS_UNIT.getURIasString());
			String propind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, dsind, SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getURIasString());
			
			if( ! units.isEmpty() || ! propind.isEmpty()){
				String physdefval = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, propind, physicaldefinitionURI.toString());	
				
				if (!physdefval.isEmpty())	ds.setAssociatedPhysicalProperty(idpropertymap.get(physdefval));
				
				// Set the connection between the physical property and what it's a property of
				String propofind = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, propind, SemSimRelation.PHYSICAL_PROPERTY_OF.getURIasString());
				
				if ( ! propofind.isEmpty()) {
					PhysicalModelComponent pmc = identitymap.get(propofind);
					
					if (pmc==null) pmc = createSingularComposite(propofind);
					
					ds.setAssociatedPhysicalModelComponent(pmc);
				}
			}
			
			if( ! depind.isEmpty()){
				// We assume that if there is an explicit dependency associated with the computation, 
				// then it's annotated against a reference dependency from the OPB.
				String reflabel = SemSimOWLFactory.getRDFLabels(ont, factory.getOWLNamedIndividual(IRI.create(depind)))[0];
				String physdefvaldep = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, depind, physicaldefinitionURI.toString());

				ReferencePhysicalDependency rpd = (ReferencePhysicalDependency) getReferenceDependency(physdefvaldep, reflabel);
				ds.getComputation().setPhysicalDependency(rpd);
			}
		}
	}
	
	
	/**
	 * Assert mappings between CellML-type variables
	 * @throws OWLException
	 */
	private void mapCellMLTypeVariables() throws OWLException {
		
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DECIMAL.getURIasString())){
			DataStructure ds = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind));
			
			if(ds instanceof MappableVariable){
				
				for(String mappedvaruri : SemSimOWLFactory.getIndObjectPropertyObjects(ont, dsind, SemSimRelation.MAPPED_TO.getURIasString())){
					DataStructure mappedvar = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(mappedvaruri));
					
					if(mappedvar != null && (mappedvar instanceof MappableVariable)){
						((MappableVariable)ds).addVariableMappingTo((MappableVariable)mappedvar);
						// Use mapping info in input/output network
						mappedvar.getComputation().addInput(ds);
					}
				}
			}
		}
	}
	
	
	/**
	 * Collect information about the units in the model
	 * @throws OWLException
	 */
	private void collectUnits() throws OWLException {
		// Add units to model, assign to data structures, store unit factoring
		for(String unitind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.UNIT_OF_MEASUREMENT.getURIasString())){
			String unitcode = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, unitind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
			
			UnitOfMeasurement uom = semsimmodel.getUnit(unitcode);
			
			if(uom == null){
				String importedfromval = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, unitind, SemSimRelation.IMPORTED_FROM.getURIasString());
				
				// If unit not imported
				if(importedfromval.isEmpty() || importedfromval == null){
					uom = new UnitOfMeasurement(unitcode);
					semsimmodel.addUnit(uom);
				}
				// If the unit is imported, collect import info
				else{
					String referencename = getStringValueFromAnnotatedDataPropertyAxiom(ont, unitind, SemSimRelation.IMPORTED_FROM.getURI(),
							importedfromval, SemSimRelation.REFERENCE_NAME_OF_IMPORT.getURI());
					uom = SemSimComponentImporter.importUnits(semsimmodel, unitcode, referencename, importedfromval);
				}
			}
			
			// Set the units for the data structures
			for(String dsuri : SemSimOWLFactory.getIndObjectPropertyObjects(ont, unitind, SemSimRelation.UNIT_FOR.getURIasString())){
				DataStructure ds = semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsuri));
				ds.setUnit(uom);
			}
			
			String isfundamental = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, unitind, SemSimRelation.IS_FUNDAMENTAL_UNIT.getURIasString());
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
					String factorunitcode = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, baseunitind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
					
					if(!factorunitcode.isEmpty() && factorunitcode!=null){
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
						
						
						if( ! oopaa.getAnnotations(unitfactorexpprop).isEmpty()){
							OWLLiteral litval = (OWLLiteral) oopaa.getAnnotations(unitfactorexpprop).toArray(new OWLAnnotation[]{})[0].getValue();
							exponent = litval.parseDouble();
						}
						
						if( ! oopaa.getAnnotations(unitfactorprefixprop).isEmpty()){
							OWLLiteral litval = (OWLLiteral) oopaa.getAnnotations(unitfactorprefixprop).toArray(new OWLAnnotation[]{})[0].getValue();
							prefix = litval.getLiteral();
						}
						
						if( ! oopaa.getAnnotations(unitfactormultprop).isEmpty()){
							OWLLiteral litval = (OWLLiteral) oopaa.getAnnotations(unitfactormultprop).toArray(new OWLAnnotation[]{})[0].getValue();
							multiplier = litval.parseDouble();
						}
						uom.addUnitFactor(new UnitFactor(baseunit, exponent, prefix, multiplier));
					}
				}
			}
		}
	}
		
	/** Go through existing data structures and establish the computational relationships between data structures
	 * @throws OWLException*/
	private void establishIsInputRelationships() throws OWLException {
		
		for(String dsind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString())){
			String name = SemSimOWLFactory.getURIdecodedFragmentFromIRI(dsind);
			
			DataStructure ds = semsimmodel.getAssociatedDataStructure(name);
			
			Pair<String,String> prefixanddelimiter = ds instanceof SBMLFunctionOutput ? Pair.of(SBMLreader.FUNCTION_PREFIX + ds.getName(),"_") : null;
			
			SemSimUtil.setComputationInputsForDataStructure(semsimmodel, ds, prefixanddelimiter);
			
			// set the data structure's solution domain
			String soldom = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, dsind, SemSimRelation.HAS_SOLUTION_DOMAIN.getURIasString());
			semsimmodel.getAssociatedDataStructure(name).setSolutionDomain(semsimmodel.getAssociatedDataStructure(SemSimOWLFactory.getURIdecodedFragmentFromIRI(soldom)));
		}
	}
	
	
	/**
	 * Collect info about the relational constraints in the model
	 * @throws OWLException
	 */
	private void collectRelationalConstraints() throws OWLException {
		for(String relind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.RELATIONAL_CONSTRAINT.getURIasString())){
			String mmleq = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, relind, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
			String mathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, relind, SemSimRelation.HAS_MATHML.getURIasString());
			semsimmodel.addRelationalConstraint(new RelationalConstraint(mmleq, mathml));
		}
	}
	
	
	/**
	 * Collect info about the discrete events in the model
	 * @throws OWLException
	 */
	private void collectEvents() throws OWLException{
		
		for(String eventind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.EVENT.getURIasString())){
			String eventname = SemSimOWLFactory.getIRIfragment(eventind);
			Event ssevent = new Event();
			ssevent.setName(eventname);
			String triggermathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, eventind,
					SemSimRelation.HAS_TRIGGER_MATHML.getURIasString());
			ssevent.setTriggerMathML(triggermathml);
			
			// Get priority mathml, delay mathml and time units
			String prioritymathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, eventind, SemSimRelation.HAS_PRIORITY_MATHML.getURIasString());
			String delaymathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, eventind, SemSimRelation.HAS_DELAY_MATHML.getURIasString());
			String timeunituri = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, eventind, SemSimRelation.HAS_TIME_UNIT.getURIasString());

			// Set priority
			if(! prioritymathml.isEmpty() && prioritymathml!=null) ssevent.setPriorityMathML(prioritymathml);
			
			// Set delay
			if(! delaymathml.isEmpty() && delaymathml!=null) ssevent.setDelayMathML(delaymathml);
			
			// Set time units
			if(! timeunituri.isEmpty() && timeunituri!=null){
				String unitname = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, 
						timeunituri, SemSimRelation.HAS_COMPUTATIONAL_CODE.getURIasString());
				UnitOfMeasurement timeunit = semsimmodel.getUnit(unitname);
				ssevent.setTimeUnit(timeunit);
			}
			
			// Process event assignments
			for(String eaind : SemSimOWLFactory.getIndObjectPropertyObjects(ont, eventind, SemSimRelation.HAS_EVENT_ASSIGNMENT.getURIasString())){
				EventAssignment ea = new EventAssignment();
				ssevent.addEventAssignment(ea);
				String eamathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, eaind, SemSimRelation.HAS_MATHML.getURIasString());
				ea.setMathML(eamathml);
				String outputuri = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, eaind, SemSimRelation.HAS_OUTPUT.getURIasString());
				String outputname = SemSimOWLFactory.getIRIfragment(outputuri);
				DataStructure outputds = semsimmodel.getAssociatedDataStructure(outputname);
				ea.setOutput(outputds);
				outputds.getComputation().addEvent(ssevent);
			}
			
			semsimmodel.addEvent(ssevent);
			
		}
	}

	
	/** Collect the SBML-style initial assignments
	 * @throws OWLException */
	private void collectSBMLinitialAssignments() throws OWLException{
		
		for(String iaind : SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.SBML_INITIAL_ASSIGNMENT.getURIasString())){
			SBMLInitialAssignment ssia = new SBMLInitialAssignment();
			String iamathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, iaind, SemSimRelation.HAS_MATHML.getURIasString());
			ssia.setMathML(iamathml);
			String outputuri = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, iaind, SemSimRelation.HAS_OUTPUT.getURIasString());
			String outputname = SemSimOWLFactory.getIRIfragment(outputuri);
			DataStructure outputds = semsimmodel.getAssociatedDataStructure(outputname);
			ssia.setOutput(outputds);
			
			outputds.getComputation().addSBMLinitialAssignment(ssia);
		}
	}
	
	
	/** Collect annotations on custom terms used in the model (e.g. custom physical entities) */
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
	
	
	/**
	 * Collect info about the Submodels represented in the model
	 * @throws OWLException
	 */
	private void collectSubModels() throws OWLException {
		// Collect the submodels
		Set<String> subset = SemSimOWLFactory.getIndividualsAsStrings(ont, SemSimTypes.SUBMODEL.getURIasString());
		
		for(String sub : subset){
			
			String subname = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, sub, SemSimRelation.HAS_NAME.getURIasString());
			String metadataid = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, sub, SemSimRelation.METADATA_ID.getURIasString());
			
			boolean hascomputation = ! SemSimOWLFactory.getIndObjectPropertyObjects(ont, sub, SemSimRelation.HAS_COMPUTATIONAL_COMPONENT.getURIasString()).isEmpty();
			
			// Get all associated data structures
			Set<String> dss = SemSimOWLFactory.getIndObjectPropertyObjects(ont, sub, SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE.getURIasString());
			
			// Get importedFrom value
			String importval = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, sub, SemSimRelation.IMPORTED_FROM.getURIasString());
			Submodel sssubmodel = null;
			
			// If submodel IS NOT imported
			if(importval.isEmpty() || importval==null){
				sssubmodel = (hascomputation) ? new FunctionalSubmodel(subname, subname, null, null) : new Submodel(subname);
				semsimmodel.assignValidMetadataIDtoSemSimObject(metadataid, sssubmodel);

				String componentmathml = null;
				String componentmathmlwithroot = null;
				String description = SemSimOWLFactory.getRDFcomment(ont, factory.getOWLNamedIndividual(IRI.create(sub)));
				
				if (!description.isEmpty()) sssubmodel.setDescription(description);
				
				// If computation associated with submodel, store mathml
				if(sssubmodel.isFunctional()){
					String comp = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(ont, sub, SemSimRelation.HAS_COMPUTATIONAL_COMPONENT.getURIasString());
					
					if(comp!=null && !comp.isEmpty()){
						componentmathml = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, comp, SemSimRelation.HAS_MATHML.getURIasString());
						
						if(componentmathml!=null && !componentmathml.isEmpty()){
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
					if(componentmathml!=null && !componentmathml.isEmpty()){
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
									if(theds.getComputation().getComputationalCode().isEmpty()){
										
										String RHS = CellMLreader.getRHSofDataStructureEquation(varmathml, localvarname);
										
										if(RHS != null) {
											String soldomname = theds.hasSolutionDomain() ? "(" + theds.getSolutionDomain().getName() + ")" : "t";
											String LHS = ode ? "d(" + localvarname + ")/d" + soldomname + " = " : localvarname + " = ";
											theds.getComputation().setComputationalCode(LHS + RHS);
										}
										else theds.getComputation().setComputationalCode("(error converting MathML to infix equation)");

									}
									
									CellMLreader.whiteBoxFunctionalSubmodelEquation(varmathmlel, subname, semsimmodel, theds);
									
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
						
						// Have to check for empty string b/c that's what getCellMLinitialValue is initialized to.
						if((mv.getComputation().getComputationalCode().isEmpty()) &&
								(mv.getCellMLinitialValue()!=null && !mv.getCellMLinitialValue().isEmpty())){ 
							
							theds.getComputation().setComputationalCode(localvarname + " = " + mv.getCellMLinitialValue());
						}
					}
				}
				
				semsimmodel.addSubmodel(sssubmodel);
			}
			// If submodel IS imported
			else{
				String referencename = getStringValueFromAnnotatedDataPropertyAxiom(ont, sub, SemSimRelation.IMPORTED_FROM.getURI(),
						importval, SemSimRelation.REFERENCE_NAME_OF_IMPORT.getURI());
				try {
					sssubmodel = 
							SemSimComponentImporter.importFunctionalSubmodel(
									modelaccessor,
									semsimmodel, subname, referencename, importval, sslib);
				} catch (JDOMException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// If a sub-model has sub-models, add that info to the model, store subsumption types as annotations
		for(String sub : subset){
			String subname = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, sub, SemSimRelation.HAS_NAME.getURIasString());
			Set<String> subsubset = SemSimOWLFactory.getIndObjectPropertyObjects(ont, sub, SemSimRelation.INCLUDES_SUBMODEL.getURIasString());
			
			for(String subsub : subsubset){
				String subsubname = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, subsub, SemSimRelation.HAS_NAME.getURIasString());
				Submodel subsubmodel = semsimmodel.getSubmodel(subsubname);
				
				// Assign the subsumption type (for CellML-type submodels)
				Set<String> reltypes = getSubmodelSubsumptionRelationship(ont, sub, SemSimRelation.INCLUDES_SUBMODEL, subsub);
				for(String reltype : reltypes){
					
					if(reltype!=null){
						FunctionalSubmodel fxnalsub = (FunctionalSubmodel)semsimmodel.getSubmodel(subname);
						Set<FunctionalSubmodel> valueset = fxnalsub.getRelationshipSubmodelMap().get(reltype);
						
						if(valueset==null) valueset = new HashSet<FunctionalSubmodel>();
						
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

	/**
	 * Get the multiplier for a process participant
	 * @param ont The ontology object associated with the SemSim OWL file
	 * @param process IRI of the physical process in the ontology
	 * @param prop IRI of a process participation relation (e.g. SemSimRelation.HAS_SOURCE or SemSimRelation.HAS_SINK)
	 * @param ent IRI of the process participant in the ontology
	 * @return The stoichiometry for the entity's participation in the process
	 */
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
					
					if (litval.isInteger()) val = (double)litval.parseInteger();
					else val = litval.parseDouble();
				}
			}
		}
		return val;
	}
	
	/**
	 * Get the relationship for a submodel subsumption
	 * @param ont Ontology corresponding to the SemSim OWL file
	 * @param submodel IRI of a submodel in the ontology
	 * @param prop IRI of a submodel subsumption relation
	 * @param subsubmodel IRI of a submodel subsumed by another submodel
	 * @return Set of relationship types associated with a submodel subsumption (e.g. "containment")
	 */
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
	
	
	/**
	 * Get a string value from an annotation on a datatype property axiom
	 * @param ont Source ontology containing the datatype property axiom
	 * @param subject IRI of the subject in the axiom
	 * @param pred URI of the predicate in the axiom
	 * @param data Object of the axiom as a String
	 * @param annpropuri URI of the property used in an annotation on the axiom
	 * @return The object of the annotation statement on the axiom (as an OWLLiteral String)
	 * If multiple annotations are present, returns the object used in the first annotation.
	 */
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
	
	// TODO: something is up here - looks in idpropertymap but if not found writes to identitymap
	/**
	 * Look up a PhysicalProperty object in the SemSimModel. If not added to the model already,
	 * create it and add.
	 * @param referencekey URI of a physical property class
	 * @param description Human-readable description of the property
	 * @return A PhysicalProperty object corresponding to the reference URI
	 */
	private PhysicalModelComponent getReferenceProperty(String referencekey, String description) {
		PhysicalModelComponent term = idpropertymap.get(referencekey);
		
		if (term == null) {
			term = new PhysicalProperty(description, URI.create(referencekey));
			identitymap.put(referencekey, term);
			semsimmodel.addPhysicalProperty((PhysicalProperty) term);
		}
		return term;
	}
	
	
	/**
	 * Look up a PhysicalDependency object in the SemSimModel. If not added to the model already,
	 * create it and add.
	 * @param refuri Reference URI of the physical dependency
	 * @param desc Human-readable description of the physical dependency
	 * @return A ReferencePhysicalDependency object corresponding to the input reference URI
	 */
	private ReferencePhysicalDependency getReferenceDependency(String refuri, String desc){
		ReferencePhysicalDependency rpd = iddependencymap.get(refuri);
		
		if(rpd == null){
			rpd = new ReferencePhysicalDependency(URI.create(refuri), desc);
			iddependencymap.put(refuri, rpd);
			rpd = semsimmodel.addReferencePhysicalDependency(rpd);
		}
		return rpd;
	}
	
	
	/**
	 * Get the physical model component class of a given individual in the model. 
	 * This looks at the individual's URI fragment and determines its class based 
	 * on the "_{class}_{uniqueNum}" naming convention for physical model components.
	 * @param ind IRI of the individual
	 * @return The PhysicalModelComponent class that the individual belongs to
	 * @throws OWLException
	 */
	private PhysicalModelComponent getClassofIndividual(String ind) throws OWLException {
		String indclass = SemSimOWLFactory.getFunctionalIndDatatypePropertyValues(ont, ind, physicaldefinitionURI.toString());
		
		if (indclass.isEmpty()) {
			String sub = ind.subSequence(ind.lastIndexOf("_"), ind.length()).toString();
			PhysicalModelComponent pmc = identitymap.get(ind.replace(sub, ""));
			//Catch unmarked custom entities
			if (pmc==null) pmc = makeCustomEntity(ind);
			return pmc; 
			
		}
		return identitymap.get(indclass);
	}
	
	/**
	 * Create a custom physical entity based on an individual in the SemSim OWL file
	 * @param cuperef IRI of the individual representing a custom entity
	 * @return CustomPhysicalEntity object corresponding to the input individual
	 */
	private CustomPhysicalEntity makeCustomEntity(String cuperef) {
		
		OWLEntity cupecls = factory.getOWLClass(IRI.create(cuperef));
		String label = SemSimOWLFactory.getRDFLabels(ont, cupecls)[0];
		
		String sub = cuperef.subSequence(cuperef.lastIndexOf("_"), cuperef.length()).toString();
		cuperef = cuperef.replace(sub, "");
		
		if (identitymap.containsKey(cuperef)) return (CustomPhysicalEntity) identitymap.get(cuperef);
		
		if (label.isEmpty()) { 
			label = cuperef.subSequence(cuperef.lastIndexOf("/"), cuperef.length()).toString();
			label = label.replace("_", " ");
		}
		CustomPhysicalEntity cupe = new CustomPhysicalEntity(label, null);
		
		String description = SemSimOWLFactory.getRDFcomment(ont, cupecls);
		
		if( ! description.isEmpty()) cupe.setDescription(description);
		
		label = cuperef.replace(sub, "");
		
		identitymap.put(cuperef.replace(sub, ""), cupe);
		return semsimmodel.addCustomPhysicalEntity(cupe);
	}
	
	/**
	 * Produces a composite entity object for a singular physical entity individual in the SemSim OWL file.
	 * This makes some of the programmatic tasks in the SemSimAPI easier.
	 * @param uri URI of the singular physical entity in the SemSim OWL file
	 * @return CompositePhysicalEntity representation of the input individual
	 * @throws OWLException
	 */
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
