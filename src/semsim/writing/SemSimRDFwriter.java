package semsim.writing;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.definitions.SemSimTypes;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalEnergyDifferential;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.reading.SemSimRDFreader;
import semsim.reading.AbstractRDFreader;
import semsim.reading.ModelClassifier.ModelType;

/**
 * Class for writing SemSim-related RDF content in standalone
 * SBML, CellML and JSim project files.
 * @author mneal
 *
 */
public class SemSimRDFwriter extends AbstractRDFwriter{
	
	// For CompositePhysicalEntities, this relates a CPE with it's index entity Resource
	private Map<String, String> submodelNameAndURImap = new HashMap<String, String>(); // includes CellML and SemSim-style submodels
	private ModelType modeltype;
	
	// Constructor without existing RDF block
	public SemSimRDFwriter(SemSimModel semsimmodel, ModelType modeltype){
		super(null);
		
		this.modeltype = modeltype;
		initialize(semsimmodel);
	}
	
	/**
	 * Constructor with an existing RDF block
	 * @param semsimmodel The SemSim model whose annotations will be written out as RDF
	 * @param rdfasstring Existing RDF content to add to SemSim-related RDF content
	 * @param modeltype The type of model being written out
	 */
	public SemSimRDFwriter(SemSimModel semsimmodel, String rdfasstring, ModelType modeltype){	
		super(null);
		
		this.modeltype = modeltype;
		initialize(semsimmodel);
		
		if(rdfasstring!=null){
			SemSimRDFreader.readStringToRDFmodel(rdf, rdfasstring, "");
			
			if(modeltype.equals(ModelType.CELLML_MODEL))
				rdf.removeNsPrefix("model"); // In case old RDF with a declared namespace is present (older CellML models)
		}
	}
	
	
	/**
	 * Set namespace prefixes, etc.
	 * @param semsimmodel The SemSim model associated with the writer's RDF content
	 */
	private void initialize(SemSimModel semsimmodel){
		this.semsimmodel = semsimmodel;
		
		// Don't set model prefix for cellml models. They use relative IDs and ad-hoc namespaces.
		if( ! modeltype.equals(ModelType.CELLML_MODEL)){
			rdf.setNsPrefix("model", semsimmodel.getNamespace());
			modelnamespaceinRDF = semsimmodel.getNamespace();
		}
				
		createSubmodelURIandNameMap();
		
		localids.addAll(semsimmodel.getMetadataIDcomponentMap().keySet());

		rdf.setNsPrefix("semsim", RDFNamespace.SEMSIM.getNamespaceAsString());
		rdf.setNsPrefix("bqbiol", RDFNamespace.BQB.getNamespaceAsString());
		rdf.setNsPrefix("bqmodel", RDFNamespace.BQM.getNamespaceAsString());
		rdf.setNsPrefix("opb", RDFNamespace.OPB.getNamespaceAsString());
		rdf.setNsPrefix("ro", RDFNamespace.RO.getNamespaceAsString());
		rdf.setNsPrefix("dcterms", RDFNamespace.DCTERMS.getNamespaceAsString());
	}	

	
	/** Create a map that links submodels and the URIs representing them */
	private void createSubmodelURIandNameMap(){
		for(Submodel sub : semsimmodel.getSubmodels()){
			
			Resource subres = null;
			
			if(sub.hasMetadataID()) subres = rdf.createResource("#" + sub.getMetadataID());
			else subres = createNewResourceForSemSimObject("submodel");
			
			submodelNameAndURImap.put(sub.getName(), subres.getURI());
		}
	}
	
	// Add model-level annotations 
	@Override
	public void setRDFforModelLevelAnnotations(){
		
		String modelmetaid = semsimmodel.hasMetadataID() ?  semsimmodel.getMetadataID() : semsimmodel.assignValidMetadataIDtoSemSimObject(semsimmodel.getName(), semsimmodel);
		
		String modeluri = modelnamespaceinRDF + "#" + modelmetaid;
		modeluri = modeluri.replaceAll("##", "#"); // in case model namespace ends with #
		Resource modelres = rdf.createResource(modeluri);
				
		// Add model description to RDF if we're writing to a CellML model or a JSim project file
		// In SemSim OWL files, descriptions are stored as RDF:comments on the ontology
		// In SBML files (non-OMEX), the description is stored in the model element's <notes> block
		// In CellML and SBML files within OMEX archives, the description is stored in the OMEX metadata file
		if(semsimmodel.hasDescription() && (modeltype==ModelType.CELLML_MODEL || modeltype==ModelType.MML_MODEL_IN_PROJ)){
			Property prop = rdf.createProperty(AbstractRDFreader.dcterms_description.getURI());
			Statement st = rdf.createStatement(modelres, prop, semsimmodel.getDescription());
			addStatement(st);
		}
		
		if(modeltype==ModelType.CELLML_MODEL || modeltype==ModelType.MML_MODEL_IN_PROJ){
			for(Annotation ann : semsimmodel.getAnnotations()){
				
				//TODO: Need to work on preserving all annotations in CellML RDF block.
				if(ann.getRelation()==SemSimRelation.CELLML_RDF_MARKUP 
						|| ann.getRelation()==SemSimRelation.CELLML_DOCUMENTATION) continue;
				
				Property prop = ann.getRelation().getRDFproperty();
				Statement st = rdf.createStatement(modelres, prop, ann.getValue().toString());
				
				addStatement(st);
			}
		}
	}
	
	
	
	/** Add annotations for all submodels */
	protected void setRDFforSemSimSubmodelAnnotations(){
		
		for(Submodel sub : semsimmodel.getSubmodels()){
			setRDFforSubmodelAnnotations(sub);
		}
	}
	
	
	@Override
	protected void setRDFforSubmodelAnnotations(Submodel sub){

		String subname = sub.getName();
		Resource subres = rdf.getResource(submodelNameAndURImap.get(subname));
		
		setFreeTextAnnotationForObject(sub, subres);

		if(sub.isFunctional()) return; // Only collect free-text annotation if functional submodel
		
		// Write out name
		Statement st = rdf.createStatement(subres, SemSimRelation.HAS_NAME.getRDFproperty(), subname);
		addStatement(st);
						
		// Write out which data structures are associated with the submodel 
		for(DataStructure dsinsub : sub.getAssociatedDataStructures()){
			
			// Assign metaid to data structure if there isn't one already
			String dsmetaid = dsinsub.hasMetadataID() ? dsinsub.getMetadataID() : semsimmodel.assignValidMetadataIDtoSemSimObject(dsinsub.getName(), dsinsub);
			if(modeltype==ModelType.MML_MODEL_IN_PROJ) dsmetaid = dsinsub.getName();
			
			Resource dsres = rdf.getResource(modelnamespaceinRDF + dsmetaid);
			Statement stds = rdf.createStatement(subres, 
					SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE.getRDFproperty(), 
					dsres);
			
			addStatement(stds);

		}
		
		// Write out which submodels are associated with the submodel
		for(Submodel subsub : sub.getSubmodels()){
			String subsubname = subsub.getName();
			Resource subsubres = rdf.getResource(submodelNameAndURImap.get(subsubname)); // map includes CellML and SemSim-style submodels
			String subsuburi = subsubres.getURI();
			String candidatemetaid = subsuburi.substring(subsuburi.indexOf("#")+1);
			
			// Make sure the subsub model has a metaid that we can use for reference in the output model
			if( ! subsub.hasMetadataID()) semsimmodel.assignValidMetadataIDtoSemSimObject(candidatemetaid, subsub);

			Statement stsub = rdf.createStatement(
					subres, 
					SemSimRelation.INCLUDES_SUBMODEL.getRDFproperty(), 
					subsubres);
			
			addStatement(stsub);
		}
	}
	
		
	@Override
	protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares) {
		if(ds.hasPhysicalProperty()){
			Property iccfprop = SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty();
			Resource propres = getResourceForDataStructurePropertyAndAnnotate(rdf, (DataStructure)ds);
			Statement st = rdf.createStatement(ares, iccfprop, propres);
			
			addStatement(st);
			
			setDataStructurePropertyOfAnnotation((DataStructure)ds);
		}		
	}
	
	
	@Override
	protected void setDataStructurePropertyOfAnnotation(DataStructure ds){		
		// Collect physical model components with properties
		if( ! ds.isImportedViaSubmodel()){
			
			if(ds.hasPhysicalProperty()){
				Resource propres = getResourceForDataStructurePropertyAndAnnotate(rdf, ds);

				if(ds.hasAssociatedPhysicalComponent()){
					PhysicalModelComponent propof = ds.getAssociatedPhysicalModelComponent();
					
					// If the variable is a property of an entity
					if(propof instanceof PhysicalEntity){
						CompositePhysicalEntity cpe = (CompositePhysicalEntity)propof;
						
						if (cpe.getArrayListOfEntities().size()>1) {
							// Get the Resource corresponding to the index entity of the composite entity
							URI indexuri = setCompositePhysicalEntityMetadata(cpe);
							Resource indexresource = rdf.getResource(indexuri.toString());
							Statement propofst = rdf.createStatement(
									propres, 
									SemSimRelation.PHYSICAL_PROPERTY_OF.getRDFproperty(), 
									indexresource);
							
							addStatement(propofst);
						}
						// else it's a singular physical entity
						else{
							Resource entity = getResourceForPMCandAnnotate(cpe.getArrayListOfEntities().get(0));
							Statement st = rdf.createStatement(
									propres, 
									SemSimRelation.PHYSICAL_PROPERTY_OF.getRDFproperty(), 
									entity);
							
							addStatement(st);
						}
					}
					// If it's a property of a process
					else if(propof instanceof PhysicalProcess){
						PhysicalProcess process = (PhysicalProcess)propof;

						Resource processres = getResourceForPMCandAnnotate(ds.getAssociatedPhysicalModelComponent());
						Statement st = rdf.createStatement(
								propres, 
								SemSimRelation.PHYSICAL_PROPERTY_OF.getRDFproperty(), 
								processres);
						
						addStatement(st);
						
						// If the participants for the process have already been set, do not duplicate
						// statements (in CellML models mapped codewords may be annotated against the
						// same process, and because each process participant is created anew here, duplicate
						// participant statements would appear in CellML RDF block).
						
						if(rdf.contains(processres, SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty())
								|| rdf.contains(processres, SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty())
								|| rdf.contains(processres, SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty()))
							return;
						
						// If we're here, the process hasn't been assigned its participants yet
						
						// Set the sources
						for(PhysicalEntity source : process.getSourcePhysicalEntities()){
							setRDFstatementsForEntityParticipation(process, source, 
									SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty(), process.getSourceStoichiometry(source));
						}
						// Set the sinks
						for(PhysicalEntity sink : process.getSinkPhysicalEntities()){
							setRDFstatementsForEntityParticipation(process, sink,
									SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty(), process.getSinkStoichiometry(sink));
						}
						// Set the mediators
						for(PhysicalEntity mediator : process.getMediatorPhysicalEntities()){
							setRDFstatementsForEntityParticipation(process, mediator,
									SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty(), null);
						}
					}
					// Otherwise we assume it's a property of a force
					else{
						PhysicalEnergyDifferential force = (PhysicalEnergyDifferential)propof;

						Resource forceres = getResourceForPMCandAnnotate(ds.getAssociatedPhysicalModelComponent());
						Statement st = rdf.createStatement(
								propres, 
								SemSimRelation.PHYSICAL_PROPERTY_OF.getRDFproperty(), 
								forceres);
						
						addStatement(st);
						
						// If the participants for the process have already been set, do not duplicate
						// statements (in CellML models mapped codewords may be annotated against the
						// same process, and because each process participant is created anew here, duplicate
						// participant statements would appear in CellML RDF block).
						
						if(rdf.contains(forceres, SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty())
								|| rdf.contains(forceres, SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty())
								|| rdf.contains(forceres, SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty()))
							return;
						
						// If we're here, the process hasn't been assigned its participants yet
						
						// Set the sources
						for(PhysicalEntity source : force.getSources()){
							setRDFstatementsForEntityParticipation(force, source, 
									SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty(), null);
						}
						// Set the sinks
						for(PhysicalEntity sink : force.getSinks()){
							setRDFstatementsForEntityParticipation(force, sink,
									SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty(), null);
						}
					}
				}
			}
		}
	}
	
	
	
	@Override
	protected void setReferenceOrCustomResourceAnnotations(PhysicalModelComponent pmc, Resource res){
		Resource refres = null;
		
		// If it's a reference resource
		if(pmc instanceof ReferenceTerm){
			
			URI uri = ((ReferenceTerm)pmc).getPhysicalDefinitionURI();
			refres = findReferenceResourceFromURI(uri);
			
			Statement annagainstst = rdf.createStatement(
					res, 
					SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty(), 
					refres);
				
			// If we have a reference resource and the annotation statement hasn't already 
			// been added to the RDF block, add it
			if(refres!=null) addStatement(annagainstst);
		}
		
		// If it's a custom resource
		else{

			for(Annotation ann : pmc.getAnnotations()){
				// If the physical model component has either an "is" or "is version of" annotation, 
				// add the annotation statement to the RDF block
								
				if(ann instanceof ReferenceOntologyAnnotation){	
					
					ReferenceOntologyAnnotation roa = (ReferenceOntologyAnnotation)ann;
					refres = findReferenceResourceFromURI(roa.getReferenceURI());
					Relation relation = roa.getRelation();
					
					// Add the annotations on the custom term					
					if(relation.equals(SemSimRelation.BQB_IS_VERSION_OF)
							|| relation.equals(StructuralRelation.HAS_PART)){
//							|| relation.equals(StructuralRelation.PART_OF)){ // can't distinguish between part_of in composite statements and annotations on custom term

						Property refprop = roa.getRelation().getRDFproperty();
						Statement annagainstst = rdf.createStatement(res, refprop, refres);
						
						// If we have a reference resource and the annotation statement hasn't already 
						// been added to the RDF block, add it
						if(refres!=null) addStatement(annagainstst);
					}
				}
			}
			
			// If it is a custom entity or process, store the name and description
			if((pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_PROCESS)) || (pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_ENTITY))){
				
				if(pmc.getName()!=null){
					Statement namest = rdf.createStatement(res, 
							SemSimRelation.HAS_NAME.getRDFproperty(), pmc.getName());
					
					addStatement(namest);
				}
				
				if(pmc.hasDescription()){
					Statement descst = rdf.createStatement(res, 
							AbstractRDFreader.dcterms_description, pmc.getDescription());
					
					addStatement(descst);
				}
			}
		}
	}

	@Override
	protected Property getPartOfPropertyForComposites(){
		return StructuralRelation.PART_OF.getRDFproperty();
	}
	

}