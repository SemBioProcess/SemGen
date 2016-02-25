package semsim.writing;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import semsim.SemSimLibrary;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.Importable;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.Event;
import semsim.model.computational.Event.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;

public class SemSimOWLwriter extends ModelWriter {
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	protected OWLOntology ont;
	public OWLDataFactory factory = manager.getOWLDataFactory();
	
	private Map<PhysicalModelComponent, URI> singularPMCsAndUrisForDataStructures = new HashMap<PhysicalModelComponent,URI>();
	private Map<CompositePhysicalEntity,URI> compositeEntitiesAndIndexes = new HashMap<CompositePhysicalEntity,URI>();
	private String namespace;
	private Set<DataStructure> localdss = new HashSet<DataStructure>();
	
	
	public SemSimOWLwriter(SemSimModel model) throws OWLOntologyCreationException {
		super(model);
		namespace = model.getNamespace();
		
		// Create a blank semsim ontology with just the base classes
		InputStream in = getClass().getResourceAsStream("/semsim/owl/SemSimBase.owl"); 
		Set<OWLAxiom> allbaseaxioms = manager.loadOntologyFromOntologyDocument(in).getAxioms();
		IRI ontiri = IRI.create(namespace.substring(0, namespace.length()-1));  // Gets rid of '#' at end of namespace
		ont = manager.createOntology(allbaseaxioms, ontiri);
	}
	
	public void writeToFile(File destination) throws OWLException{
		createOWLOntologyFromModel();
		manager.saveOntology(ont,new RDFXMLOntologyFormat(), IRI.create(destination));
	}
	
	public void writeToFile(URI uri) throws OWLException{
		createOWLOntologyFromModel();
		manager.saveOntology(ont, new RDFXMLOntologyFormat(), IRI.create(uri));
	}
	
	//*****************************OWL CREATION METHODS*************************//
	
	public OWLOntology createOWLOntologyFromModel() throws OWLException{	
		getLocalDataStuctures();
		addUnits();	
		addEvents();
		addDataStructures();
		setRelations();
		addSubModels();
		addPhysicalComponentAnnotations();
		addModelAnnotations();
		
		return ont;
	}
	
/** Add the computational model components: units of measurement, data structures, computations
 * 	Create a list of the data structures that need to be preserved in the model
 *  Exclude those that are imports of imports
*/	
	private void getLocalDataStuctures() throws OWLException {
		localdss.addAll(semsimmodel.getAssociatedDataStructures());
		
		for(FunctionalSubmodel sub : semsimmodel.getFunctionalSubmodels()){
			
			if(sub.isImported() && sub.getParentImport()!=null){
				localdss.removeAll(sub.getAssociatedDataStructures());
			}			
		}
	}
	
	private void addUnits() throws OWLException {
		// First add the units. 
		Set<UnitOfMeasurement> unitstoadd = new HashSet<UnitOfMeasurement>();
		for(UnitOfMeasurement uom : semsimmodel.getUnits()){
			if(uom.getParentImport()==null){
				unitstoadd.add(uom);
				for(UnitFactor uf : uom.getUnitFactors()){
					unitstoadd.add(uf.getBaseUnit());
				}
			}
		}
		
		for(UnitOfMeasurement uom : unitstoadd){
			String unituri = semsimmodel.getNamespace() + "UNIT_" + SemSimOWLFactory.URIencoding(uom.getName());
			SemSimOWLFactory.createSemSimIndividual(ont, unituri, factory.getOWLClass(SemSimTypes.UNIT_OF_MEASUREMENT.getIRI()), "", manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, unituri,
					SemSimRelation.HAS_COMPUTATIONAL_CODE, uom.getComputationalCode(), manager);
			
			// IS CUSTOM DECLARATION INFO STILL NEEDED HERE?
			if(uom.isFundamental())
				SemSimOWLFactory.setIndDatatypeProperty(ont, unituri, SemSimRelation.IS_FUNDAMENTAL_UNIT,
						uom.isFundamental(), manager);
			
			if(uom.isImported())
				SemSimOWLFactory.setIndDatatypePropertyWithAnnotations(ont, unituri, SemSimRelation.IMPORTED_FROM, uom.getHrefValue(),
						makeAnnotationsForImport(uom), manager);
			
			for(UnitFactor factor : uom.getUnitFactors()){
				String factoruri = semsimmodel.getNamespace() + "UNIT_" + SemSimOWLFactory.URIencoding(factor.getBaseUnit().getName());
				SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, unituri, factoruri,
						SemSimRelation.HAS_UNIT_FACTOR, SemSimRelation.UNIT_FACTOR_FOR, 
						makeUnitFactorAnnotations(factor), manager);
			}
		}
	}
	
	// Add Event info to model
	private void addEvents() throws OWLException{
		
		int suffix = 0;
		for(Event event : semsimmodel.getEvents()){
			OWLClass eventparentclass = factory.getOWLClass(IRI.create(event.getSemSimClassURI()));
			String eventuristring = namespace + SemSimOWLFactory.URIencoding(event.getName());
			String eventname = null;

			// Use suffix integer if no name available for event
			if(event.getName().equals("") || event.getName()==null){
				eventname = "event_" + suffix;
				suffix++;
			}
			else eventname = event.getName();
			
			eventuristring = namespace + eventname;
			
			SemSimOWLFactory.createSemSimIndividual(ont, eventuristring, eventparentclass, "", manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, eventuristring, 
					SemSimRelation.HAS_TRIGGER_MATHML, event.getTriggerMathML(), manager);
			
			// Write out priority
			if(event.getPriorityMathML()!=null)
				SemSimOWLFactory.setIndDatatypeProperty(ont, eventuristring, SemSimRelation.HAS_PRIORITY_MATHML,
					event.getPriorityMathML(), manager);
			
			// Write out delay
			if(event.getDelayMathML()!=null)
				SemSimOWLFactory.setIndDatatypeProperty(ont, eventuristring, SemSimRelation.HAS_DELAY_MATHML,
						event.getDelayMathML(), manager);
			
			// Write out time units
			if(event.getTimeUnit()!=null){
				String unitname = event.getTimeUnit().getName();
				String unituri = namespace + "UNIT_" + SemSimOWLFactory.URIencoding(unitname);
				SemSimOWLFactory.setIndObjectProperty(ont, eventuristring, unituri, 
						SemSimRelation.HAS_TIME_UNIT, null, manager);
			}
				
			
			// Write out the event assignments
			for(EventAssignment ssea : event.getEventAssignments()){
				String eaname = eventname + "_assignment_" + ssea.getOutput().getName();
				OWLClass eaparentclass = factory.getOWLClass(IRI.create(ssea.getSemSimClassURI()));
				String eauristring = namespace + eaname;
				String outputdsuristring = namespace + ssea.getOutput().getName();
				
				// Create event assignment individual and attach properties
				SemSimOWLFactory.createSemSimIndividual(ont, eauristring, eaparentclass, "", manager);
				SemSimOWLFactory.setIndObjectProperty(ont, eauristring, outputdsuristring,
						SemSimRelation.HAS_OUTPUT, null, manager);
				SemSimOWLFactory.setIndDatatypeProperty(ont, eauristring, SemSimRelation.HAS_MATHML,
						ssea.getMathML(), manager);
				
				// Associate the assignment with the event
				SemSimOWLFactory.setIndObjectProperty(ont, eventuristring, eauristring,
						SemSimRelation.HAS_EVENT_ASSIGNMENT,null, manager);
				
				// Associate the assignment with the data structure's computation that it effects
				SemSimOWLFactory.setIndObjectProperty(ont, outputdsuristring + "_computation", 
						eventuristring, SemSimRelation.HAS_EVENT, null, manager);
			}
		}
	}
	
	private void addDataStructures() throws OWLException {
		
		for(DataStructure ds : localdss){		
			String dsuri = namespace + SemSimOWLFactory.URIencoding(ds.getName());
			OWLNamedIndividual dsind = factory.getOWLNamedIndividual(IRI.create(dsuri));
			// Set the RDF:comment (the free text definition of the data structure)
			SemSimOWLFactory.setRDFComment(ont, dsind, ds.getDescription(), manager);

			OWLClass parentclass = factory.getOWLClass(IRI.create(ds.getSemSimClassURI()));
			SemSimOWLFactory.createSemSimIndividual(ont, dsuri, parentclass, "", manager);
			
			// If there is a singular annotation on the DataStructure, write it. Use reference annotation label as
			// the DataStructure's label
			if(ds.hasPhysicalDefinitionAnnotation()){
				ReferenceTerm refterm = ds.getSingularTerm();
				SemSimOWLFactory.setRDFLabel(ont, dsind, refterm.getName(), manager);
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.HAS_PHYSICAL_DEFINITION, refterm.getPhysicalDefinitionURI(), manager);
			}
			
			// Classify the physical property under its reference ontology class
			// that it is annotated against
			if(ds.hasPhysicalProperty() || ds.hasAssociatedPhysicalComponent()){
				PhysicalPropertyinComposite dspp = ds.getPhysicalProperty();
				
				//Create dummy property if one has not been assigned
				if (dspp==null)  dspp = new PhysicalPropertyinComposite("",URI.create(""));
				
				URI propertyuri = SemSimOWLFactory.getURIforPhysicalProperty(semsimmodel, ds);
				createPhysicalModelIndividual(dspp, propertyuri.toString());
				// Log the physical property and its URI
				singularPMCsAndUrisForDataStructures.put(dspp, propertyuri);
				SemSimOWLFactory.setIndObjectProperty(ont, propertyuri.toString(),
						dsuri, SemSimRelation.HAS_COMPUTATIONAL_COMPONENT,
						SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR, manager);
				if(ds.getComputation()!=null && !(ds instanceof MappableVariable))
					SemSimOWLFactory.setIndObjectProperty(ont, propertyuri.toString(),
						dsuri + "_dependency", SemSimRelation.IS_DETERMINED_BY, SemSimRelation.DETERMINES, manager);
				// Create physical entity and physical process individuals, link to properties
				processAssociatedCompositeofDataStructure(ds);
				
			}
			
			// If the data structure is solved with an explicit computation, store that info
			if(!(ds instanceof MappableVariable) && ds.getComputation()!=null){
				SemSimOWLFactory.createSemSimIndividual(ont, dsuri + "_computation",
						factory.getOWLClass(SemSimTypes.COMPUTATION.getIRI()), "", manager);
			
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri + "_computation", SemSimRelation.HAS_COMPUTATIONAL_CODE, ds.getComputation().getComputationalCode(), manager);
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri + "_computation", SemSimRelation.HAS_MATHML, ds.getComputation().getMathML(), manager);
				SemSimOWLFactory.createSemSimIndividual(ont, dsuri + "_dependency",
						factory.getOWLClass(SemSimTypes.PHYSICAL_DEPENDENCY.getIRI()), "", manager);
				SemSimOWLFactory.setIndObjectProperty(ont, dsuri + "_computation",
						dsuri + "_dependency", SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR,
						SemSimRelation.HAS_COMPUTATIONAL_COMPONENT, manager);
				SemSimOWLFactory.setIndObjectProperty(ont, dsuri, dsuri + "_computation",
						SemSimRelation.IS_OUTPUT_FOR,
						SemSimRelation.HAS_OUTPUT, manager);
				
				// Put the hasInput and hasRolePlayer data in the SemSim model
				for(DataStructure inputds : ds.getComputation().getInputs()){
					String inputuri = namespace + SemSimOWLFactory.URIencoding(inputds.getName());
					SemSimOWLFactory.setIndObjectProperty(ont, dsuri + "_computation", inputuri, 
							SemSimRelation.HAS_INPUT, SemSimRelation.IS_INPUT_FOR, manager);
				}				
			}
			
			if(ds.hasSolutionDomain()){
				SemSimOWLFactory.setIndObjectProperty(ont, dsuri, namespace + SemSimOWLFactory.URIencoding(ds.getSolutionDomain().getName()),
						SemSimRelation.HAS_SOLUTION_DOMAIN, SemSimRelation.IS_SOLUTION_DOMAIN, manager);
			}
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.IS_SOLUTION_DOMAIN, ds.isSolutionDomain(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.IS_DECLARED, ds.isDeclared(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.METADATA_ID, ds.getMetadataID(), manager);
			
			// Assert CellML-type mappings between data structures
			if(ds instanceof MappableVariable){
				for(MappableVariable var : ((MappableVariable)ds).getMappedTo()){
					String varuri = namespace + SemSimOWLFactory.URIencoding(var.getName());
					if(localdss.contains(var))
						SemSimOWLFactory.setIndObjectProperty(ont, dsuri, varuri, SemSimRelation.MAPPED_TO, null, manager);
				}
				
				// Set CellML initial value
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.CELLML_INITIAL_VALUE,
						((MappableVariable)ds).getCellMLinitialValue(), manager);
				
				// Set the interface values
				String pubint = ((MappableVariable)ds).getPublicInterfaceValue();
				String privint = ((MappableVariable)ds).getPrivateInterfaceValue();
				
				if(pubint!=null) SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.CELLML_COMPONENT_PUBLIC_INTERFACE,
						pubint, manager);
				if(privint!=null) SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.CELLML_COMPONENT_PRIVATE_INTERFACE,
						privint, manager);
			}
			
			// If not a CellML-type variable, store startValue info
			else 
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimRelation.HAS_START_VALUE, ds.getStartValue(), manager);
			
			// Get the units info
			if(ds.hasUnits()){
				if(ds.getUnit().getParentImport()==null){
					UnitOfMeasurement uom = ds.getUnit();
					String unituri = semsimmodel.getNamespace() + "UNIT_" + SemSimOWLFactory.URIencoding(uom.getName());
					SemSimOWLFactory.setIndObjectProperty(ont, dsuri, unituri, SemSimRelation.HAS_UNIT,
							SemSimRelation.UNIT_FOR, manager);
				}
			}
		// If not declared and not used to compute anything, leave the data structure out of the model
		}
	}
		
	private void setRelations() throws OWLException {
		int r = 0;
		OWLClass relparent = factory.getOWLClass(SemSimTypes.RELATIONAL_CONSTRAINT.getIRI());
		for(RelationalConstraint rel : semsimmodel.getRelationalConstraints()){
			String relind = namespace + "relationalConstraint_" + r;
			SemSimOWLFactory.createSemSimIndividual(ont, relind, relparent, "", manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, relind, SemSimRelation.HAS_COMPUTATIONAL_CODE, rel.getComputationalCode(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, relind, SemSimRelation.HAS_MATHML, rel.getMathML(), manager);
			r++;
		}
	}
	
	/** Add source, sink and mediator info to model */
	private void setProcessParticipants(PhysicalProcess proc) throws OWLException {
				for(PhysicalEntity source : proc.getSourcePhysicalEntities()){	
					Set<OWLAnnotation> anns = makeMultiplierAnnotation(source, proc.getSourceStoichiometry(source));
					SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, singularPMCsAndUrisForDataStructures.get(proc).toString(),
							compositeEntitiesAndIndexes.get(source).toString(), SemSimRelation.HAS_SOURCE,
							null, anns, manager);
				}
				for(PhysicalEntity sink : proc.getSinkPhysicalEntities()){
					Set<OWLAnnotation> anns = makeMultiplierAnnotation(sink, proc.getSinkStoichiometry(sink));
					SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, singularPMCsAndUrisForDataStructures.get(proc).toString(),
							compositeEntitiesAndIndexes.get(sink).toString(), SemSimRelation.HAS_SINK,
							null, anns, manager);
				}
				for(PhysicalEntity mediator : proc.getMediatorPhysicalEntities()){
					SemSimOWLFactory.setIndObjectProperty(ont, singularPMCsAndUrisForDataStructures.get(proc).toString(),
							compositeEntitiesAndIndexes.get(mediator).toString(), SemSimRelation.HAS_MEDIATOR,
							null, manager);
				}
	}
	
	private void addSubModels() throws OWLException {
		// Process submodels
		Set<Subsumption> cellmlsubsumptions = new HashSet<Subsumption>();
		
		for(Submodel sub : semsimmodel.getSubmodels()){
			Boolean toplevelimport = false;
			Boolean sublevelimport = false;
			
			if(sub.isFunctional()){
				FunctionalSubmodel fsub = (FunctionalSubmodel)sub;
				
				if(fsub.isImported() && fsub.getParentImport()==null){
					toplevelimport = true;
				}
				else if(fsub.isImported() && fsub.getParentImport()!=null){
					sublevelimport = true;
				}
			}
			
			if(! sublevelimport){
				
				// Create the individual
				String indstr = namespace + SemSimOWLFactory.URIencoding(sub.getName());
				SemSimOWLFactory.createSemSimIndividual(ont, indstr, factory.getOWLClass(SemSimTypes.SUBMODEL.getIRI()), "", manager);
				
				// Set the name
				SemSimOWLFactory.setIndDatatypeProperty(ont, indstr, SemSimRelation.HAS_NAME, sub.getName(), manager);
				SemSimOWLFactory.setRDFComment(ont, factory.getOWLNamedIndividual(IRI.create(indstr)), sub.getDescription(), manager);
				
				// Set the associated data structures
				for(DataStructure ds : sub.getAssociatedDataStructures()){
					SemSimOWLFactory.setIndObjectProperty(ont, indstr, namespace + SemSimOWLFactory.URIencoding(ds.getName()),
							SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE, null, manager);
				}
				
				if(!toplevelimport){
					
					// If a functional sub-model, store computation.
					if(sub instanceof FunctionalSubmodel){
						SemSimOWLFactory.createSemSimIndividual(ont, indstr + "_computation", factory.getOWLClass(SemSimTypes.COMPUTATION.getIRI()), "", manager);
						SemSimOWLFactory.setIndDatatypeProperty(ont, indstr + "_computation", SemSimRelation.HAS_MATHML, 
								((FunctionalSubmodel)sub).getComputation().getMathML(), manager);
						SemSimOWLFactory.setIndObjectProperty(ont, indstr, indstr + "_computation", 
								SemSimRelation.HAS_COMPUTATIONAL_COMPONENT,
								SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR, manager);
						
						// assert all CellML-type containment and encapsulation relationships 
						for(String rel : ((FunctionalSubmodel)sub).getRelationshipSubmodelMap().keySet()){
							for(FunctionalSubmodel subsub : ((FunctionalSubmodel)sub).getRelationshipSubmodelMap().get(rel)){
								Subsumption subsumption = null;
								for(Subsumption g : cellmlsubsumptions){
									if(g.parent==sub && g.child==subsub){
										subsumption = g;
										subsumption.rels.add(rel);
									}
								}
								if(subsumption==null){
									subsumption = new Subsumption((FunctionalSubmodel) sub, subsub, rel);
									cellmlsubsumptions.add(subsumption);
								}
							}
						}
					}
					
					// get all sub-submodels, too
					for(Submodel subsub : sub.getSubmodels()){
						SemSimOWLFactory.setIndObjectProperty(ont, indstr, namespace + SemSimOWLFactory.URIencoding(subsub.getName()), 
								SemSimRelation.INCLUDES_SUBMODEL, null, manager);
					}
				}
				// Otherwise add the assertion that the submodel is imported, but leave out the rest of the info
				else{
					SemSimOWLFactory.setIndDatatypePropertyWithAnnotations(ont, indstr, 
							SemSimRelation.IMPORTED_FROM, ((FunctionalSubmodel)sub).getHrefValue(),
							makeAnnotationsForImport(((FunctionalSubmodel)sub)), manager);
				}
			}
		}
		// Assert the annotations needed for CellML component groupings
		for(Subsumption subsump : cellmlsubsumptions){
			String indstr = namespace + SemSimOWLFactory.URIencoding(subsump.parent.getName());
			String subindstr = namespace + SemSimOWLFactory.URIencoding(subsump.child.getName());
			Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
			for(String rel : subsump.rels){
				anns.addAll(makeSubmodelSubsumptionAnnotation(rel));
			}
			SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, indstr, subindstr,
					SemSimRelation.INCLUDES_SUBMODEL,
					null, anns, manager);
		}
	}
	
	/** 
	 * Go through custom physical model components and assert their annotations, if present
	 */
	private void addPhysicalComponentAnnotations() throws OWLException {	
		
		Set<PhysicalModelComponent> custs = new HashSet<PhysicalModelComponent>();
		custs.addAll(semsimmodel.getCustomPhysicalEntities());
		custs.addAll(semsimmodel.getCustomPhysicalProcesses());
		
		// For each custom term in the model...
		for(PhysicalModelComponent pmc : custs){
			
			Set<ReferenceOntologyAnnotation> annstoprocess = new HashSet<ReferenceOntologyAnnotation>();
			annstoprocess.addAll(pmc.getReferenceOntologyAnnotations(SemSimRelation.BQB_IS_VERSION_OF));
			annstoprocess.addAll(pmc.getReferenceOntologyAnnotations(StructuralRelation.HAS_PART));
			annstoprocess.addAll(pmc.getReferenceOntologyAnnotations(StructuralRelation.PART_OF));
			
			for(ReferenceOntologyAnnotation ref : annstoprocess){
				
				String referenceURIstring = ref.getReferenceURI().toString();
				OWLClass refclass = factory.getOWLClass(IRI.create(referenceURIstring));
				
				// Store the reference term class if not already added
				if(!ont.getClassesInSignature().contains(refclass)){
					String parent = pmc.getSemSimClassURI().toString();
							
					SemSimOWLFactory.addClass(ont, referenceURIstring, new String[]{parent}, manager);
					SemSimOWLFactory.setRDFLabel(ont, refclass, ref.getValueDescription(), manager);
				}
				
				// Get the URI for the individual we're processing
				String indURIstring = null;
				
				if(singularPMCsAndUrisForDataStructures.containsKey(pmc))
					indURIstring = singularPMCsAndUrisForDataStructures.get(pmc).toString();
				
				else{
					for(String custind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, pmc.getSemSimClassURI().toString())){
						OWLNamedIndividual theind = factory.getOWLNamedIndividual(IRI.create(custind));
						
						if(SemSimOWLFactory.getRDFLabels(ont, theind)[0].equals(pmc.getName()))
							indURIstring = custind;
					}
				}
				
				// Add object property restrictions on the custom individual
				URI propertyURI = ref.getRelation().getURI();
				SemSimOWLFactory.addExistentialObjectPropertyRestrictionOnIndividual(ont,
						indURIstring, propertyURI.toString(), referenceURIstring, manager);
			}
		}
	}
	
	// Add the model's curational metadata
	private void addModelAnnotations() throws OWLException {
		SemSimOWLFactory.addOntologyAnnotation(ont, SemSimLibrary.SEMSIM_VERSION_IRI, Double.toString(SemSimLibrary.SEMSIM_VERSION), manager);
		
		if(semsimmodel.getLegacyCodeLocation()!=null)
			SemSimOWLFactory.addOntologyAnnotation(ont, SemSimModel.LEGACY_CODE_LOCATION_IRI, 
					semsimmodel.getLegacyCodeLocation().getFullLocation(), manager);
		
		ArrayList<Annotation> anns = semsimmodel.getCurationalMetadata().getAnnotationList();
		anns.addAll(semsimmodel.getAnnotations());
		for(Annotation ann : anns){
				String str = (String)ann.getValue();
				SemSimOWLFactory.addOntologyAnnotation(ont, ann.getRelation().getURI().toString(), str, "en", manager);
		}
	}
	
	//********************************************************************//
	//*****************************HELPER METHODS*************************//
	
	private void processAssociatedCompositeofDataStructure(DataStructure ds) throws OWLException {
		if(ds.hasAssociatedPhysicalComponent()){				
			// Create the new physical model individual and get what it's a physical property of
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			// If it's not a composite physical entity
			if(!pmc.isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)){
				String uristring = null;
				// Need to make sure that each process gets its own physical property individual, even
				// if some are annotated as being the exact same process

				uristring = logSingularPhysicalComponentAndGetURIasString(pmc, namespace);
				SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_ENTITY.getURIasString());

				// Make sure to log all the participating entities - some may not be directly associated
				// with a data structure but only used to define the process

				for(PhysicalEntity ent : ((PhysicalProcess)pmc).getParticipants()){
					URI uri = processCompositePhysicalEntity((CompositePhysicalEntity) ent, namespace);
					if (!compositeEntitiesAndIndexes.containsKey(ent)) {
						compositeEntitiesAndIndexes.put((CompositePhysicalEntity) ent, uri);
					}
				}
						
				// Add the individual to the ontology if not already there, create it
				if(!SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_MODEL_COMPONENT.getURIasString()).contains(uristring)){
					createPhysicalModelIndividual(pmc, uristring);
				}
					// Connect the new individual to its property
				SemSimOWLFactory.setIndObjectProperty(ont, SemSimOWLFactory.getURIforPhysicalProperty(semsimmodel, ds).toString(), uristring, 
						SemSimRelation.PHYSICAL_PROPERTY_OF, SemSimRelation.HAS_PHYSICAL_PROPERTY, manager);
				setProcessParticipants((PhysicalProcess)pmc);
			}
			else {

				URI indexuri = processCompositePhysicalEntity((CompositePhysicalEntity)pmc, namespace);
				// Connect physical property to the index physical entity for the composite entity
				SemSimOWLFactory.setIndObjectProperty(ont, SemSimOWLFactory.getURIforPhysicalProperty(semsimmodel, ds).toString(),
						indexuri.toString(), SemSimRelation.PHYSICAL_PROPERTY_OF, SemSimRelation.HAS_PHYSICAL_PROPERTY, manager);
			}
		}
		
	}
	
	private URI processCompositePhysicalEntity(CompositePhysicalEntity cpe, String namespace) throws OWLException{			
		// check if an equivalent nextcpe already exists
		cpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, compositeEntitiesAndIndexes);
		
		if(compositeEntitiesAndIndexes.containsKey(cpe)) {
			return compositeEntitiesAndIndexes.get(cpe);
		}
		// If we haven't added this composite entity yet
		URI compuri = cpe.makeURI(namespace);
		SemSimOWLFactory.createSemSimIndividual(ont, compuri.toString(), 
				factory.getOWLClass(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY.getIRI()), "", manager);

		PhysicalEntity indexent = cpe.getArrayListOfEntities().get(0);
		
		// Create a unique URI for the index physical entity
		String indexuristring = makeURIforPhysicalModelComponent(namespace, indexent, SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_MODEL_COMPONENT.getURIasString())).toString();
		URI indexuri = URI.create(indexuristring);
		
		compositeEntitiesAndIndexes.put(cpe, indexuri);
		SemSimOWLFactory.setIndObjectProperty(ont, compuri.toString(), indexuristring, StructuralRelation.HAS_INDEX_ENTITY, 
				StructuralRelation.INDEX_ENTITY_FOR, manager);

		// create the index entity, put reference class in ontology if not there already
		createPhysicalModelIndividual(indexent, indexuristring);

		if (cpe.getArrayListOfEntities().size()>1) {
			// Truncate the composite by one entity
			ArrayList<PhysicalEntity> nextents = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> nextrels = new ArrayList<StructuralRelation>();
			
			for(int u=1; u<cpe.getArrayListOfEntities().size(); u++){
				nextents.add(cpe.getArrayListOfEntities().get(u));
			}
			for(int u=1; u<cpe.getArrayListOfStructuralRelations().size(); u++){
				nextrels.add(cpe.getArrayListOfStructuralRelations().get(u));
			}
			CompositePhysicalEntity nextcpe = new CompositePhysicalEntity(nextents, nextrels);
			URI nexturi = null;
			
			// Add sub-composites recursively
			if(nextcpe.getArrayListOfEntities().size()>1){
				// check if an equivalent nextcpe already exists
				nextcpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, compositeEntitiesAndIndexes);
				nexturi = processCompositePhysicalEntity(nextcpe, namespace);
			}
			// If we're at the end of the composite
			else{
				// If it's an entity we haven't processed yet
				if(!singularPMCsAndUrisForDataStructures.containsKey(nextcpe.getArrayListOfEntities().get(0))){
					nexturi = URI.create(logSingularPhysicalComponentAndGetURIasString(nextcpe.getArrayListOfEntities().get(0),namespace));
					createPhysicalModelIndividual(nextcpe.getArrayListOfEntities().get(0), nexturi.toString());
					singularPMCsAndUrisForDataStructures.put(nextcpe.getArrayListOfEntities().get(0), nexturi);
				}
				// Otherwise get the terminal entity that we logged previously
				else{
					nexturi = singularPMCsAndUrisForDataStructures.get(nextcpe.getArrayListOfEntities().get(0));
				}
			}
			// Establish structural relationship between parts of composite annotation
			StructuralRelation rel = cpe.getArrayListOfStructuralRelations().get(0);
			SemSimOWLFactory.setIndObjectProperty(ont, indexuri.toString(), nexturi.toString(),
				rel, SemSimRelations.getInverseStructuralRelation(rel), manager);
		}
		return indexuri;
	}
	
	private String logSingularPhysicalComponentAndGetURIasString(PhysicalModelComponent pmc, String namespace) throws OWLException{
		String uristring = null;
		if(singularPMCsAndUrisForDataStructures.containsKey(pmc)){
			uristring = singularPMCsAndUrisForDataStructures.get(pmc).toString();
		}
		else{
			URI uri = null;
			if(pmc instanceof CompositePhysicalEntity){
				CompositePhysicalEntity cpe = (CompositePhysicalEntity)pmc;
				// This should really associate the URI of the index entity with the CPE. Or should it?
				uri = cpe.makeURI(namespace);
				return uri.toString();
			}
			else{
				uri = makeURIforPhysicalModelComponent(namespace, pmc, SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.PHYSICAL_MODEL_COMPONENT.getURIasString()));
				uristring = uri.toString();
				singularPMCsAndUrisForDataStructures.put(pmc, uri);
			}
		}
		return uristring;
	}
	
	private URI makeURIforPhysicalModelComponent(String namespace, PhysicalModelComponent pmc, Set<String> existinguris){
		String uritrunk = namespace;
		URI uri = null;

		if(! (pmc instanceof PhysicalProcess)){
			
			if(pmc.hasPhysicalDefinitionAnnotation()){
				uritrunk = uritrunk +
				SemSimOWLFactory.getIRIfragment(((ReferenceTerm) pmc).getPhysicalDefinitionURI().toString());
			}
			else uritrunk = uritrunk + SemSimOWLFactory.URIencoding(pmc.getName());
			uritrunk = uritrunk + "_";
			uri = URI.create(SemSimOWLFactory.generateUniqueIRIwithNumber(uritrunk.toString(), existinguris));
		}
		else uri = URI.create(uritrunk + SemSimOWLFactory.URIencoding(pmc.getName()));
		
		// If there is already a data structure with the same URI as the physical model component,
		// append with a numerical suffix until we have a unique ID
		try {
			Set<String> existingdsinds = SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimTypes.DATASTRUCTURE.getURIasString());
			
			if(existingdsinds.contains(uri.toString()) && semsimmodel.getAssociatedDataStructure(pmc.getName()) != null)
					uri = URI.create(SemSimOWLFactory.generateUniqueIRIwithNumber(uri.toString() + "_", existingdsinds)); 
			}
		catch (OWLException e) {e.printStackTrace();}

		return uri;
	}
	
	private void createPhysicalModelIndividual(PhysicalModelComponent pmc, String uriforind) throws OWLException{
		String physicaltype = pmc.getComponentTypeasString();
		String parenturistring = null;
		if(pmc.isType(SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE)) {
			parenturistring = SemSimTypes.PHYSICAL_PROPERTY.getURIasString();
		}
		else if(pmc.isType(SemSimTypes.REFERENCE_PHYSICAL_ENTITY) || pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_ENTITY)){
			parenturistring = SemSimTypes.PHYSICAL_ENTITY.getURIasString();
		}
		else if(pmc.isType(SemSimTypes.REFERENCE_PHYSICAL_PROCESS) || pmc.isType(SemSimTypes.REFERENCE_PHYSICAL_PROCESS)){
			parenturistring = SemSimTypes.PHYSICAL_PROCESS.getURIasString();
		}
		
		Set<String> allphysmodclasses = new HashSet<String>();
		allphysmodclasses.addAll(SemSimOWLFactory.getAllSubclasses(ont, SemSimTypes.PHYSICAL_MODEL_COMPONENT.getURIasString(), false));
		
		String label = null;
		String description = null;
		
		// If there is a "refers-to" reference ontology annotation
		if(pmc.hasPhysicalDefinitionAnnotation()){
			ReferenceTerm firstann = (ReferenceTerm)pmc;
			parenturistring = firstann.getPhysicalDefinitionURI().toString();
			label = firstann.getName();
			
			// Add the reference class to the semsim model if needed
			if(!allphysmodclasses.contains(parenturistring)){
				SemSimOWLFactory.addExternalReferenceClass(ont, parenturistring, physicaltype, label, manager);
			}
			
			// Put the individual physical component in the reference class
			SemSimOWLFactory.createSemSimIndividual(ont, uriforind, factory.getOWLClass(IRI.create(parenturistring)), "", manager);
			
			// Establish physical definition
			SemSimOWLFactory.setIndDatatypeProperty(ont, uriforind, 
					SemSimRelation.HAS_PHYSICAL_DEFINITION, firstann.getPhysicalDefinitionURI().toString(), manager);
		}
		// Otherwise it's a custom entity, custom process or unspecified property
		else if (!(pmc instanceof CompositePhysicalEntity)){
			if(pmc instanceof PhysicalPropertyinComposite) parenturistring = SemSimTypes.PHYSICAL_PROPERTY.getURIasString();
			else{
				parenturistring = SemSimOWLFactory.getNamespaceFromIRI(uriforind) + SemSimOWLFactory.URIencoding(pmc.getName());
				if(!allphysmodclasses.contains(parenturistring)){
					parenturistring = pmc.getSemSimClassURI().toString();
				}
				label = pmc.getName();
				description = pmc.getDescription();
			}
			SemSimOWLFactory.createSemSimIndividual(ont, uriforind, factory.getOWLClass(IRI.create(parenturistring)), "", manager);
		}
		// Set the RDF label for the individual (RDF label for reference classes are set in addExternalReferenceClass method)
		if(label!=null)
			SemSimOWLFactory.setRDFLabel(ont, factory.getOWLNamedIndividual(IRI.create(uriforind)), label, manager);
		if(description!=null){
			SemSimOWLFactory.setRDFComment(ont, factory.getOWLNamedIndividual(IRI.create(uriforind)), description, manager);
		}
	}
	
	// Assert the multiplier on process participants
	private Set<OWLAnnotation> makeMultiplierAnnotation(PhysicalEntity pp, Double stoich){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		OWLLiteral lit = factory.getOWLLiteral(stoich);
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(SemSimRelation.HAS_MULTIPLIER.getIRI()), lit);
		anns.add(anno);
		return anns;
	}
	
	// Assert a public interface annotation (for CellML-derived models)
	private Set<OWLAnnotation> makeSubmodelSubsumptionAnnotation(String type){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		OWLLiteral lit = factory.getOWLLiteral(type);
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(SemSimRelation.CELLML_COMPONENT_SUBSUMPTION_TYPE.getIRI()), lit);
		anns.add(anno);
		return anns;
	}
	
	// Assert a public interface annotation (for CellML-derived models)
	private Set<OWLAnnotation> makeUnitFactorAnnotations(UnitFactor factor){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		
		if(factor.getExponent()!=1.0 && factor.getExponent()!=0.0){
			OWLLiteral explit = factory.getOWLLiteral(factor.getExponent());
			OWLAnnotation expanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(SemSimRelation.UNIT_FACTOR_EXPONENT.getIRI()), explit);
			anns.add(expanno);
		}
		
		if(factor.getPrefix()!=null){
			OWLLiteral preflit = factory.getOWLLiteral(factor.getPrefix());
			OWLAnnotation prefanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(SemSimRelation.UNIT_FACTOR_PREFIX.getIRI()), preflit);
			anns.add(prefanno);
		}
		
		if(factor.getMultiplier()!=1.0 && factor.getMultiplier()!=0.0){
			OWLLiteral multlit = factory.getOWLLiteral(factor.getMultiplier());
			OWLAnnotation multanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(SemSimRelation.UNIT_FACTOR_MULTIPLIER.getIRI()), multlit);
			anns.add(multanno);
		}
		return anns;
	}
	
	// Assert annotations needed to retrieve an imported unit or submodel
	private Set<OWLAnnotation> makeAnnotationsForImport(Importable imported){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		if(imported.getReferencedName()!=null){
			OWLLiteral preflit = factory.getOWLLiteral(imported.getReferencedName());
			OWLAnnotation prefanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(SemSimRelation.REFERENCE_NAME_OF_IMPORT.getIRI()), preflit);
			anns.add(prefanno);
		}
		return anns;
	}
	
	private class Subsumption{
		public FunctionalSubmodel parent;
		public FunctionalSubmodel child;
		public Set<String> rels = new HashSet<String>();
		
		public Subsumption(FunctionalSubmodel parent, FunctionalSubmodel child, String rel){
			this.parent = parent;
			this.child = child;
			rels.add(rel);
		}
	}
}
