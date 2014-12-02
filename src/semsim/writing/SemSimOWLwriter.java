package semsim.writing;

import java.io.File;
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

import semsim.SemSimConstants;
import semsim.SemSimUtil;
import semsim.model.Importable;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.annotation.StructuralRelation;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;

public class SemSimOWLwriter {
	protected SemSimModel model;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	protected OWLOntology ont;
	public OWLDataFactory factory = manager.getOWLDataFactory();
	private Map<PhysicalModelComponent, URI> singularPMCsAndUrisForDataStructures = new HashMap<PhysicalModelComponent,URI>();
	private String base = SemSimConstants.SEMSIM_NAMESPACE;
	private String namespace;
	private Set<DataStructure> localdss = new HashSet<DataStructure>();
	
	public SemSimOWLwriter(SemSimModel model) throws OWLOntologyCreationException {
		this.model = model;
		namespace = model.getNamespace();
		
		// Create a blank semsim ontology with just the base classes
		Set<OWLAxiom> allbaseaxioms = manager.loadOntologyFromOntologyDocument(new File("cfg/SemSimBase.owl")).getAxioms();
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
		addDataStructures();
		setRelations();
		addCompositeAnnotations();
		setProcessParticipants();
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
		localdss.addAll(model.getDataStructures());
		
		for(Submodel sub : model.getSubmodels()){
			if(sub instanceof FunctionalSubmodel){
				FunctionalSubmodel fsub = (FunctionalSubmodel)sub;
				if(fsub.isImported() && fsub.getParentImport()!=null){
					localdss.removeAll(((FunctionalSubmodel)sub).getAssociatedDataStructures());
				}
			}
		}
	}
	

	private void addUnits() throws OWLException {
		// First add the units. Only include those that are used in local data structures, or those they are mapped from
		Set<UnitOfMeasurement> unitstoadd = new HashSet<UnitOfMeasurement>();
		for(DataStructure ds : localdss){
			if(ds.hasUnits()){
				if(ds.getUnit().getParentImport()==null){
					unitstoadd.add(ds.getUnit());
					for(UnitFactor uf : ds.getUnit().getUnitFactors()){
						unitstoadd.add(uf.getBaseUnit());
					}
				}
			}
		}
		
		for(UnitOfMeasurement uom : unitstoadd){
			String unituri = model.getNamespace() + "UNIT_" + SemSimOWLFactory.URIencoding(uom.getName());
			SemSimOWLFactory.createSemSimIndividual(ont, unituri, factory.getOWLClass(IRI.create(SemSimConstants.UNITS_CLASS_URI.toString())), "", manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, unituri,
					SemSimConstants.HAS_COMPUTATIONAL_CODE_URI.toString(), uom.getComputationalCode(), manager);
			
			// IS CUSTOM DECLARATION INFO STILL NEEDED HERE?
			if(uom.isFundamental())
				SemSimOWLFactory.setIndDatatypeProperty(ont, unituri, SemSimConstants.IS_FUNDAMENTAL_UNIT_URI.toString(),
						uom.isFundamental(), manager);
			
			if(uom.isImported())
				SemSimOWLFactory.setIndDatatypePropertyWithAnnotations(ont, unituri, SemSimConstants.IMPORTED_FROM_URI.toString(), uom.getHrefValue(),
						makeAnnotationsForImport(uom), manager);
			
			for(UnitFactor factor : uom.getUnitFactors()){
				String factoruri = model.getNamespace() + "UNIT_" + SemSimOWLFactory.URIencoding(factor.getBaseUnit().getName());
				SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, unituri, factoruri,
						SemSimConstants.HAS_UNIT_FACTOR_URI.toString(), SemSimConstants.UNIT_FACTOR_FOR_URI.toString(), 
						makeUnitFactorAnnotations(factor), manager);
			}
		}
	}
	
	private void addDataStructures() throws OWLException {
		for(DataStructure ds : localdss){		
			String dsuri = namespace + SemSimOWLFactory.URIencoding(ds.getName());
			OWLNamedIndividual dsind = factory.getOWLNamedIndividual(IRI.create(dsuri));
			// Set the RDF:comment (the free text definition of the data structure)
			SemSimOWLFactory.setRDFComment(ont, dsind, ds.getDescription(), manager);
			
			String parent = SemSimConstants.SEMSIM_BASE_CLASSES_AND_URIS.get(ds.getClass()).toString();
			OWLClass parentclass = factory.getOWLClass(IRI.create(parent));
			SemSimOWLFactory.createSemSimIndividual(ont, dsuri, parentclass, "", manager);
			
			// If there is a singular annotation on the DataStructure, write it. Use reference annotation label as
			// the DataStructure's label
			if(ds.hasRefersToAnnotation()){
				String referenceuri = ds.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString();
				SemSimOWLFactory.setRDFLabel(ont, dsind, ds.getFirstRefersToReferenceOntologyAnnotation().getValueDescription(), manager);
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.REFERS_TO_URI.toString(), referenceuri, manager);
			}
			
			// Classify the physical property under the first (and should be ONLY) reference ontology class
			// that it is annotated against
			if(ds.getPhysicalProperty()!=null){
				URI propertyuri = SemSimOWLFactory.getURIforPhysicalProperty(model, ds.getPhysicalProperty());
				createPhysicalModelIndividual(ds.getPhysicalProperty(), propertyuri.toString());
				// Log the physical property and its URI
				singularPMCsAndUrisForDataStructures.put(ds.getPhysicalProperty(), propertyuri);
				SemSimOWLFactory.setIndObjectProperty(ont, propertyuri.toString(),
						dsuri, SemSimConstants.HAS_COMPUTATATIONAL_COMPONENT_URI.toString(),
						SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString(), manager);
				if(ds.getComputation()!=null && !(ds instanceof MappableVariable))
					SemSimOWLFactory.setIndObjectProperty(ont, propertyuri.toString(),
						dsuri + "_dependency", SemSimConstants.IS_DETERMINED_BY_URI.toString(), SemSimConstants.DETERMINES_URI.toString(), manager);
			}
			
			// If the data structure is solved with an explicit computation, store that info
			if(!(ds instanceof MappableVariable) && ds.getComputation()!=null){
				SemSimOWLFactory.createSemSimIndividual(ont, dsuri + "_computation",
						factory.getOWLClass(IRI.create(SemSimConstants.COMPUTATION_CLASS_URI)), "", manager);
			
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri + "_computation", base + "hasComputationalCode", ds.getComputation().getComputationalCode(), manager);
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri + "_computation", base + "hasMathML", ds.getComputation().getMathML(), manager);
				SemSimOWLFactory.createSemSimIndividual(ont, dsuri + "_dependency",
						factory.getOWLClass(IRI.create(SemSimConstants.PHYSICAL_DEPENDENCY_CLASS_URI)), "", manager);
				SemSimOWLFactory.setIndObjectProperty(ont, dsuri + "_computation",
						dsuri + "_dependency", SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString(),
						SemSimConstants.HAS_COMPUTATATIONAL_COMPONENT_URI.toString(), manager);
				SemSimOWLFactory.setIndObjectProperty(ont, dsuri, dsuri + "_computation",
						SemSimConstants.IS_OUTPUT_FOR_URI.toString(),
						SemSimConstants.HAS_OUTPUT_URI.toString(), manager);
				
				// Put the hasInput and hasRolePlayer data in the SemSim model
				for(DataStructure inputds : ds.getComputation().getInputs()){
					String inputuri = namespace + SemSimOWLFactory.URIencoding(inputds.getName());
					SemSimOWLFactory.setIndObjectProperty(ont, dsuri + "_computation", inputuri, base + "hasInput", base + "isInputFor", manager);
				}
			}
			
			if(ds.hasSolutionDomain()){
				SemSimOWLFactory.setIndObjectProperty(ont, dsuri, namespace + SemSimOWLFactory.URIencoding(ds.getSolutionDomain().getName()),
						base + "hasSolutionDomain", base + "solutionDomainFor", manager);
			}
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.IS_SOLUTION_DOMAIN_URI.toString(), ds.isSolutionDomain(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.IS_DISCRETE_URI.toString(), ds.isDiscrete(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.IS_DECLARED_URI.toString(), ds.isDeclared(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.HAS_START_VALUE_URI.toString(), ds.getStartValue(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.METADATA_ID_URI.toString(), ds.getMetadataID(), manager);
			
			// Assert CellML-type mappings between data structures
			if(ds instanceof MappableVariable){
				for(MappableVariable var : ((MappableVariable)ds).getMappedTo()){
					String varuri = namespace + SemSimOWLFactory.URIencoding(var.getName());
					if(localdss.contains(var))
						SemSimOWLFactory.setIndObjectProperty(ont, dsuri, varuri, SemSimConstants.MAPPED_TO_URI.toString(), "", manager);
				}
				// Set CellML initial value - IS THIS THE RIGHT THING TO DO?
				SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.HAS_START_VALUE_URI.toString(),
						((MappableVariable)ds).getCellMLinitialValue(), manager);
				
				// Set the interface values
				String pubint = null;
				String privint = null;
				
				pubint = ((MappableVariable)ds).getPublicInterfaceValue();
				privint = ((MappableVariable)ds).getPrivateInterfaceValue();
				
				if(pubint!=null) SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.CELLML_COMPONENT_PUBLIC_INTERFACE_URI.toString(),
						pubint, manager);
				if(privint!=null) SemSimOWLFactory.setIndDatatypeProperty(ont, dsuri, SemSimConstants.CELLML_COMPONENT_PRIVATE_INTERFACE_URI.toString(),
						privint, manager);
			}
			
			// Create physical entity and physical process individuals, link to properties
			if(ds.hasPhysicalProperty()){
				PhysicalProperty prop = ds.getPhysicalProperty();
				
				// If we have an annotated physical model component that the property points to
				if(prop.getPhysicalPropertyOf()!=null){
					
					// Create the new physical model individual and get what it's a physical property of
					PhysicalModelComponent pmc = prop.getPhysicalPropertyOf();
					
					// If it's not a composite physical entity 
					if(!(pmc instanceof CompositePhysicalEntity)){
						String uristring = null;
						
						// If it's a singular physical entity
						if(pmc instanceof PhysicalEntity){
							uristring = logSingularPhysicalComponentAndGetURIasString(pmc, namespace);
						}
						// Otherwise it's a physical process
						else{
							// Need to make sure that each process gets its own physical property individual, even
							// if some are annotated as being the exact same process
							if(singularPMCsAndUrisForDataStructures.containsKey(pmc)){
								uristring = makeURIforPhysicalModelComponent(model.getNamespace(), pmc, 
										SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_MODEL_COMPONENT_CLASS_URI.toString())).toString();
							}
							else{
								uristring = logSingularPhysicalComponentAndGetURIasString(pmc, namespace);
								SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_ENTITY_CLASS_URI.toString());

								// Make sure to log all the participating entities - some may not be directly associated
								// with a data structure but only used to define the process
								Set<PhysicalEntity> participants = new HashSet<PhysicalEntity>();
								participants.addAll(((PhysicalProcess)pmc).getSourcePhysicalEntities());
								participants.addAll(((PhysicalProcess)pmc).getSinkPhysicalEntities());
								participants.addAll(((PhysicalProcess)pmc).getMediatorPhysicalEntities());

								for(PhysicalEntity ent : participants){
									URI uriforent = null;
									if(ent instanceof CompositePhysicalEntity){
										uriforent = processCompositePhysicalEntity((CompositePhysicalEntity) ent, namespace);
									}
									else{
										String enturistring = logSingularPhysicalComponentAndGetURIasString(ent, namespace);
										uriforent = URI.create(enturistring);
									}
									singularPMCsAndUrisForDataStructures.put(ent, uriforent);
								}
							}
						}
						// Add the individual to the ontology if not already there, create it
						if(!SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_MODEL_COMPONENT_CLASS_URI.toString()).contains(uristring)){
							createPhysicalModelIndividual(pmc, uristring);
						}
						// Connect the new individual to its property
						SemSimOWLFactory.setIndObjectProperty(ont, SemSimOWLFactory.getURIforPhysicalProperty(model, ds.getPhysicalProperty()).toString(), uristring, 
								SemSimConstants.PHYSICAL_PROPERTY_OF_URI.toString(), SemSimConstants.HAS_PHYSICAL_PROPERTY_URI.toString(), manager);
					}
				}
			}
			
			// Get the units info
			if(ds.hasUnits()){
				if(ds.getUnit().getParentImport()==null){
					UnitOfMeasurement uom = ds.getUnit();
					String unituri = model.getNamespace() + "UNIT_" + SemSimOWLFactory.URIencoding(uom.getName());
					SemSimOWLFactory.setIndObjectProperty(ont, dsuri, unituri, SemSimConstants.HAS_UNIT_URI.toString(),
							SemSimConstants.UNIT_FOR_URI.toString(), manager);
				}
			}
		// If not declared and not used to compute anything, leave the data structure out of the model
	}
	}
	
	private void setRelations() throws OWLException {
		int r = 0;
		OWLClass relparent = factory.getOWLClass(IRI.create(SemSimConstants.RELATIONAL_CONSTRAINT_CLASS_URI));
		for(RelationalConstraint rel : model.getRelationalConstraints()){
			String relind = namespace + "relationalConstraint_" + r;
			SemSimOWLFactory.createSemSimIndividual(ont, relind, relparent, "", manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, relind, SemSimConstants.HAS_COMPUTATIONAL_CODE_URI.toString(), rel.getComputationalCode(), manager);
			SemSimOWLFactory.setIndDatatypeProperty(ont, relind, SemSimConstants.HAS_MATHML_URI.toString(), rel.getMathML(), manager);
			r++;
		}
	}
	
	private void addCompositeAnnotations() throws OWLException {
		for(PhysicalProperty prop : model.getPropertyAndCompositePhysicalEntityMap().keySet()){
			CompositePhysicalEntity cpe =  model.getPropertyAndCompositePhysicalEntityMap().get(prop);
			URI indexuri = processCompositePhysicalEntity(cpe, namespace);
			
			// Associate the CompositePhysicalEntity with its index uri in the ontology 
			if(!singularPMCsAndUrisForDataStructures.keySet().contains(cpe)){
				singularPMCsAndUrisForDataStructures.put(cpe, indexuri);
			}

			// Connect physical property to the index physical entity for the composite entity
			SemSimOWLFactory.setIndObjectProperty(ont, SemSimOWLFactory.getURIforPhysicalProperty(model, prop).toString(),
					indexuri.toString(), SemSimConstants.PHYSICAL_PROPERTY_OF_URI.toString(), SemSimConstants.HAS_PHYSICAL_PROPERTY_URI.toString(), manager);
		}
	}

	/** Add source, sink and mediator info to model */
	private void setProcessParticipants() throws OWLException {
		for(PhysicalProcess proc : model.getPhysicalProcesses()){
			if(singularPMCsAndUrisForDataStructures.containsKey(proc)){
				for(PhysicalEntity source : proc.getSources()){		
					SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, singularPMCsAndUrisForDataStructures.get(proc).toString(),
							singularPMCsAndUrisForDataStructures.get(source).toString(), SemSimConstants.HAS_SOURCE_URI.toString(),
							"", null, manager);
				}
				for(PhysicalEntity sink : proc.getSinks()){
					SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, singularPMCsAndUrisForDataStructures.get(proc).toString(),
							singularPMCsAndUrisForDataStructures.get(sink).toString(), SemSimConstants.HAS_SINK_URI.toString(),
							"", null, manager);
				}
				for(PhysicalEntity mediator : proc.getMediators()){
					SemSimOWLFactory.setIndObjectPropertyWithAnnotations(ont, singularPMCsAndUrisForDataStructures.get(proc).toString(),
							singularPMCsAndUrisForDataStructures.get(mediator).toString(), SemSimConstants.HAS_MEDIATOR_URI.toString(),
							"", null, manager);
				}
			}
		}
	}
	
	private void addSubModels() throws OWLException {
		// Process submodels
		Set<Subsumption> cellmlsubsumptions = new HashSet<Subsumption>();
		
		for(Submodel sub : model.getSubmodels()){
			Boolean toplevelimport = false;
			Boolean sublevelimport = false;
			if(sub instanceof FunctionalSubmodel){
				FunctionalSubmodel fsub = (FunctionalSubmodel)sub;
				if(fsub.isImported() && fsub.getParentImport()==null){
					toplevelimport = true;
				}
				else if(fsub.isImported() && fsub.getParentImport()!=null){
					sublevelimport = true;
				}
			}
			
			if(!sublevelimport){
				// Create the individual
				String indstr = namespace + SemSimOWLFactory.URIencoding(sub.getName());
				SemSimOWLFactory.createSemSimIndividual(ont, indstr, factory.getOWLClass(IRI.create(SemSimConstants.SUBMODEL_CLASS_URI)), "", manager);
				
				// Set the name
				SemSimOWLFactory.setIndDatatypeProperty(ont, indstr, SemSimConstants.HAS_NAME_URI.toString(), sub.getName(), manager);
				
				// Set the associated data structures
				for(DataStructure ds : sub.getAssociatedDataStructures()){
					SemSimOWLFactory.setIndObjectProperty(ont, indstr, namespace + SemSimOWLFactory.URIencoding(ds.getName()),
							SemSimConstants.HAS_ASSOCIATED_DATA_STRUCTURE_URI.toString(), "", manager);
				}
				
				// Set the annotation, if present
				if(sub.hasRefersToAnnotation()){
					ReferenceOntologyAnnotation ann = sub.getFirstRefersToReferenceOntologyAnnotation();
					SemSimOWLFactory.setIndDatatypeProperty(ont, indstr, SemSimConstants.REFERS_TO_URI.toString(),
							ann.getReferenceURI().toString(), manager);
					SemSimOWLFactory.setRDFLabel(ont, factory.getOWLNamedIndividual(IRI.create(indstr)), ann.getValueDescription(), manager);
					SemSimOWLFactory.setRDFComment(ont, factory.getOWLNamedIndividual(IRI.create(indstr)), ann.getValueDescription(), manager);
				}
				
				if(!toplevelimport){
					// If a functional sub-model, store computation.
					if(sub instanceof FunctionalSubmodel){
						SemSimOWLFactory.createSemSimIndividual(ont, indstr + "_computation", factory.getOWLClass(IRI.create(SemSimConstants.COMPUTATION_CLASS_URI)), "", manager);
						SemSimOWLFactory.setIndDatatypeProperty(ont, indstr + "_computation", SemSimConstants.HAS_MATHML_URI.toString(), 
								((FunctionalSubmodel)sub).getComputation().getMathML(), manager);
						SemSimOWLFactory.setIndObjectProperty(ont, indstr, indstr + "_computation", 
								SemSimConstants.HAS_COMPUTATATIONAL_COMPONENT_URI.toString(),
								SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString(), manager);
						
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
								SemSimConstants.INCLUDES_SUBMODEL_URI.toString(), "", manager);
					}
				}
				// Otherwise add the assertion that the submodel is imported, but leave out the rest of the info
				else{
					SemSimOWLFactory.setIndDatatypePropertyWithAnnotations(ont, indstr, 
							SemSimConstants.IMPORTED_FROM_URI.toString(), ((FunctionalSubmodel)sub).getHrefValue(),
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
					SemSimConstants.INCLUDES_SUBMODEL_URI.toString(),
					"", anns, manager);
		}
	}
	
	/** 
	 * Go through custom physical model components and assert their annotations, if present
	 */
	private void addPhysicalComponentAnnotations() throws OWLException {	
		Set<PhysicalModelComponent> custs = new HashSet<PhysicalModelComponent>();
		custs.addAll(model.getCustomPhysicalEntities());
		custs.addAll(model.getCustomPhysicalProcesses());
		for(PhysicalModelComponent pmc : custs){
			for(ReferenceOntologyAnnotation ref : pmc.getReferenceOntologyAnnotations(SemSimConstants.BQB_IS_VERSION_OF_RELATION)){
				OWLClass refclass = factory.getOWLClass(IRI.create(ref.getReferenceURI()));
				if(!ont.getClassesInSignature().contains(refclass)){
					String parent = "";
					if(pmc instanceof PhysicalEntity) parent = SemSimConstants.REFERENCE_PHYSICAL_ENTITY_CLASS_URI.toString();
					if(pmc instanceof PhysicalProcess) parent = SemSimConstants.REFERENCE_PHYSICAL_PROCESS_CLASS_URI.toString();
					SemSimOWLFactory.addClass(ont, ref.getReferenceURI().toString(), new String[]{parent}, manager);
					SemSimOWLFactory.setRDFLabel(ont, refclass, ref.getValueDescription(), manager);
				}
				if(singularPMCsAndUrisForDataStructures.containsKey(pmc)){
					SemSimOWLFactory.subclassIndividual(ont, singularPMCsAndUrisForDataStructures.get(pmc).toString(), ref.getReferenceURI().toString(), manager);
				}
				else{
					for(String custind : SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.SEMSIM_BASE_CLASSES_AND_URIS.get(pmc.getClass()).toString())){
						OWLNamedIndividual theind = factory.getOWLNamedIndividual(IRI.create(custind));
						if(SemSimOWLFactory.getRDFLabels(ont, theind)[0].equals(pmc.getName())){
							SemSimOWLFactory.subclassIndividual(ont, custind, ref.getReferenceURI().toString(), manager);
						}
					}
				}
			}
		}
	}
	
	private void addModelAnnotations() throws OWLException {
		for(Annotation ann : model.getAnnotations()){
			if(ann.getValue() instanceof String){
				String str = (String)ann.getValue();
				SemSimOWLFactory.addOntologyAnnotation(ont, ann.getRelation().getURI().toString(), str, "en", manager);
			}
		}
	}
	
	//********************************************************************//
	//*****************************HELPER METHODS*************************//
	
	private URI processCompositePhysicalEntity(CompositePhysicalEntity cpe, String namespace) throws OWLException{			
		URI indexuri = null;
		Map<CompositePhysicalEntity,URI> compositeEntitiesAndIndexes = new HashMap<CompositePhysicalEntity,URI>();
		
		// check if an equivalent nextcpe already exists
		cpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, compositeEntitiesAndIndexes);
		
		// If we haven't added this composite entity yet
		if(!compositeEntitiesAndIndexes.containsKey(cpe)){
			URI compuri = URI.create(logSingularPhysicalComponentAndGetURIasString(cpe, namespace));
			SemSimOWLFactory.createSemSimIndividual(ont, compuri.toString(), 
					factory.getOWLClass(IRI.create(SemSimConstants.COMPOSITE_PHYSICAL_ENTITY_CLASS_URI)), "", manager);
			PhysicalEntity indexent = cpe.getArrayListOfEntities().get(0);
			
			// Create a unique URI for the index physical entity
			String indexuristring = makeURIforPhysicalModelComponent(namespace, indexent, SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_MODEL_COMPONENT_CLASS_URI.toString())).toString();
			indexuri = URI.create(indexuristring);
			
			compositeEntitiesAndIndexes.put(cpe, indexuri);
			SemSimOWLFactory.setIndObjectProperty(ont, compuri.toString(), indexuristring, SemSimConstants.HAS_INDEX_ENTITY_URI.toString(), 
					SemSimConstants.INDEX_ENTITY_FOR_URI.toString(), manager);

			// create the index entity, put reference class in ontology if not there already
			createPhysicalModelIndividual(indexent, indexuristring);
		}
		else indexuri = compositeEntitiesAndIndexes.get(cpe);
		
		StructuralRelation rel = cpe.getArrayListOfStructuralRelations().get(0);
		
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
		SemSimOWLFactory.setIndObjectProperty(ont, indexuri.toString(), nexturi.toString(),
				rel.getURI().toString(), SemSimConstants.INVERSE_STRUCTURAL_RELATIONS_MAP.get(rel.getURI()).toString(), manager);
		return indexuri;
	}

	private static URI makeURIForCompositeEntity(CompositePhysicalEntity cpe, String namespace){
		String uristring = namespace;
		for(int y=0; y<cpe.getArrayListOfEntities().size();y++){
			PhysicalEntity pe = cpe.getArrayListOfEntities().get(y);
			// If a reference physical entity
			if(pe.hasRefersToAnnotation()){
				uristring = uristring + SemSimOWLFactory.getIRIfragment(pe.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString());
			}
			// If custom physical entity
			else{
				uristring = uristring + SemSimOWLFactory.URIencoding(pe.getName());
			}
			// Concatenate with the relation string, if needed
			if(y<cpe.getArrayListOfEntities().size()-1){
				uristring = uristring + "_" + 
					SemSimOWLFactory.getIRIfragment(cpe.getArrayListOfStructuralRelations().get(y).getURI().toString()) + "_";
			}
		}
		return URI.create(uristring);
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
				uri = makeURIForCompositeEntity(cpe, namespace);
				return uri.toString();
			}
			else{
				uri = makeURIforPhysicalModelComponent(namespace, pmc, SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.PHYSICAL_MODEL_COMPONENT_CLASS_URI.toString()));
				uristring = uri.toString();
				singularPMCsAndUrisForDataStructures.put(pmc, uri);
			}
		}
		return uristring;
	}
	
	
	private URI makeURIforPhysicalModelComponent(String namespace, PhysicalModelComponent pmc, Set<String> existinguris){
		String uritrunk = namespace;
		URI uri = null;

		if(!(pmc instanceof PhysicalProcess)){
			if(pmc.hasRefersToAnnotation()){
				uritrunk = uritrunk +
				SemSimOWLFactory.getIRIfragment(pmc.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString());
			}
			else uritrunk = uritrunk + SemSimOWLFactory.URIencoding(pmc.getName());
			uritrunk = uritrunk + "_";
			uri = URI.create(SemSimOWLFactory.generateUniqueIRIwithNumber(uritrunk.toString(), existinguris));
		}
		else uri = URI.create(uritrunk + SemSimOWLFactory.URIencoding(pmc.getName()));
		
		if(model.getDataStructure(pmc.getName())!=null){
			try {
				uri = URI.create(SemSimOWLFactory.generateUniqueIRIwithNumber(uri.toString() + "_",
						SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, SemSimConstants.DATA_STRUCTURE_CLASS_URI.toString())));
			} 
			catch (OWLException e) {e.printStackTrace();}
		}
		return uri;
	}
	
	
	private void createPhysicalModelIndividual(PhysicalModelComponent pmc, String uriforind) throws OWLException{
		String physicaltype = null;
		String parenturistring = null;
		if(pmc instanceof PhysicalProperty){
			physicaltype = "property";
			parenturistring = SemSimConstants.PHYSICAL_PROPERTY_CLASS_URI.toString();
		}
		else if(pmc instanceof ReferencePhysicalEntity || pmc instanceof CustomPhysicalEntity){
			physicaltype = "entity";
			parenturistring = SemSimConstants.PHYSICAL_ENTITY_CLASS_URI.toString();
		}
		else if(pmc instanceof ReferencePhysicalProcess || pmc instanceof CustomPhysicalProcess){
			physicaltype = "process";
			parenturistring = SemSimConstants.PHYSICAL_PROCESS_CLASS_URI.toString();
		}
		
		Set<String> allphysmodclasses = new HashSet<String>();
		allphysmodclasses.addAll(SemSimOWLFactory.getAllSubclasses(ont, SemSimConstants.PHYSICAL_MODEL_COMPONENT_CLASS_URI.toString(), false));
		
		String label = null;
		String description = null;
		
		// If there is a "refers-to" reference ontology annotation
		if(pmc.getFirstRefersToReferenceOntologyAnnotation()!=null){
			ReferenceOntologyAnnotation firstann = pmc.getFirstRefersToReferenceOntologyAnnotation();
			parenturistring = firstann.getReferenceURI().toString();
			label = firstann.getValueDescription();
			
			// Add the reference class to the semsim model if needed
			if(!allphysmodclasses.contains(parenturistring)){
				SemSimOWLFactory.addExternalReferenceClass(ont, parenturistring, physicaltype, firstann.getValueDescription(), manager);
			}
			// Put the individual physical component in the reference class
			SemSimOWLFactory.createSemSimIndividual(ont, uriforind, factory.getOWLClass(IRI.create(parenturistring)), "", manager);
			// Establish "refersTo" relationship
			SemSimOWLFactory.setIndDatatypeProperty(ont, uriforind, base + "refersTo", firstann.getReferenceURI().toString(), manager);
		}
		// Otherwise it's a custom entity, custom process or unspecified property
		else if(pmc instanceof PhysicalProperty || !(pmc instanceof CompositePhysicalEntity)){
			if(pmc instanceof PhysicalProperty) parenturistring = SemSimConstants.PHYSICAL_PROPERTY_CLASS_URI.toString();
			else{
				parenturistring = SemSimOWLFactory.getNamespaceFromIRI(uriforind) + SemSimOWLFactory.URIencoding(pmc.getName());
				if(!allphysmodclasses.contains(parenturistring)){
					if(pmc instanceof CustomPhysicalEntity) parenturistring = SemSimConstants.CUSTOM_PHYSICAL_ENTITY_CLASS_URI.toString();
					else if(pmc instanceof CustomPhysicalProcess) parenturistring = SemSimConstants.CUSTOM_PHYSICAL_PROCESS_CLASS_URI.toString();
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
	
	// Assert a public interface annotation (for CellML-derived models)
	private Set<OWLAnnotation> makeSubmodelSubsumptionAnnotation(String type){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		OWLLiteral lit = factory.getOWLLiteral(type);
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SemSimConstants.CELLML_COMPONENT_SUBSUMPTION_TYPE_URI)), lit);
		anns.add(anno);
		return anns;
	}
	
	// Assert a public interface annotation (for CellML-derived models)
	private Set<OWLAnnotation> makeUnitFactorAnnotations(UnitFactor factor){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		if(factor.getExponent()!=1.0){
			OWLLiteral explit = factory.getOWLLiteral(factor.getExponent());
			OWLAnnotation expanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SemSimConstants.UNIT_FACTOR_EXPONENT_URI)), explit);
			anns.add(expanno);
		}
		if(factor.getPrefix()!=null){
			OWLLiteral preflit = factory.getOWLLiteral(factor.getPrefix());
			OWLAnnotation prefanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SemSimConstants.UNIT_FACTOR_PREFIX_URI)), preflit);
			anns.add(prefanno);
		}
		return anns;
	}
	
	// Assert annotations needed to retrieve an imported unit or submodel
	private Set<OWLAnnotation> makeAnnotationsForImport(Importable imported){
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		if(imported.getReferencedName()!=null){
			OWLLiteral preflit = factory.getOWLLiteral(imported.getReferencedName());
			OWLAnnotation prefanno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(SemSimConstants.REFERENCE_NAME_OF_IMPORT_URI)), preflit);
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
