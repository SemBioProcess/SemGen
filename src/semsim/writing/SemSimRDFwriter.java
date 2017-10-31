package semsim.writing;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

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
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.reading.SemSimRDFreader;
import semsim.reading.AbstractRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.SemSimUtil;

public class SemSimRDFwriter extends AbstractRDFwriter{
	
	// For CompositePhysicalEntities, this relates a CPE with it's index entity Resource
	private Map<DataStructure, URI> variablesAndPropertyResourceURIs = new HashMap<DataStructure, URI>();
	private Map<String, String> submodelNameAndURImap = new HashMap<String, String>(); // includes CellML and SemSim-style submodels
	private ModelType modeltype;
	
	// Constructor without existing RDF block
	public SemSimRDFwriter(SemSimModel semsimmodel, ModelType modeltype){
		super(null);
		
		this.modeltype = modeltype;
		initialize(semsimmodel);
	}
	
	// Constructor with existing RDF block
	public SemSimRDFwriter(SemSimModel semsimmodel, String rdfasstring, ModelType modeltype){	
		super(null);
		
		this.modeltype = modeltype;
		initialize(semsimmodel);
		
		if(rdfasstring!=null){
			SemSimRDFreader.readStringToRDFmodel(rdf, rdfasstring);
			
			if(modeltype.equals(ModelType.CELLML_MODEL))
				rdf.removeNsPrefix("model"); // In case old RDF with a declared namespace is present (older CellML models)
		}
	}
	
	
	private void initialize(SemSimModel semsimmodel){
		this.semsimmodel = semsimmodel;
		
		// Don't set model prefix for cellml models. They use relative IDs and ad-hoc namespaces.
		if( ! modeltype.equals(ModelType.CELLML_MODEL)){
			rdf.setNsPrefix("model", semsimmodel.getNamespace());
			xmlbase = semsimmodel.getNamespace();
		}
		else xmlbase = "#";
				
		createSubmodelURIandNameMap();
		
		localids.addAll(semsimmodel.getMetadataIDcomponentMap().keySet());

		rdf.setNsPrefix("semsim", RDFNamespace.SEMSIM.getNamespaceasString());
		rdf.setNsPrefix("bqbiol", RDFNamespace.BQB.getNamespaceasString());
		rdf.setNsPrefix("opb", RDFNamespace.OPB.getNamespaceasString());
		rdf.setNsPrefix("ro", RDFNamespace.RO.getNamespaceasString());
		rdf.setNsPrefix("dcterms", RDFNamespace.DCTERMS.getNamespaceasString());
	}

	// Empty functions so that we can pass in sslib
	@Override
	public void writeToFile(File file){}
	
	@Override 
	public void writeToFile(URI uri){}
	
	
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
	protected void setRDFforModelLevelAnnotations(){
		
		String modelmetaid = semsimmodel.hasMetadataID() ? semsimmodel.getMetadataID() : semsimmodel.assignValidMetadataIDtoSemSimObject(semsimmodel.getName(), semsimmodel);
		
		Resource modelres = rdf.createResource(xmlbase + modelmetaid);
		
		for(Annotation ann : semsimmodel.getCurationalMetadata().getAnnotationList()){
						
			Property prop = ann.getRelation().getRDFproperty();
			Statement st = rdf.createStatement(modelres, prop, ann.getValue().toString());
			
			addStatement(st);
		}
		
	}
	
	@Override
	protected void setRDFforDataStructureAnnotations(DataStructure ds){

		String metaid = (ds.hasMetadataID()) ? ds.getMetadataID() : semsimmodel.assignValidMetadataIDtoSemSimObject(ds.getName(), ds);
		String resuri = xmlbase + metaid;
		Resource ares = rdf.createResource(resuri);
		
		// Set free-text annotation
		setFreeTextAnnotationForObject(ds, ares);
		
		// If a singular reference annotation is present, write it out
		setSingularAnnotationForDataStructure(ds, ares);
		
		// Include the necessary composite annotation info
		setDataStructurePropertyAndPropertyOfAnnotations(ds, ares);
	}
	
	
	// Add annotation for submodel
	protected void setRDFforSemSimSubmodelAnnotations(){
		
		for(Submodel sub : semsimmodel.getSubmodels()){
			setRDFforSubmodelAnnotations(sub);
		}
	}
	
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
			Resource dsres = rdf.getResource(xmlbase + dsmetaid);
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
	
	
	// Add singular annotation
	protected void setSingularAnnotationForDataStructure(DataStructure ds, Resource ares){
		
		if(ds.hasPhysicalDefinitionAnnotation()){
			URI uri = ds.getPhysicalDefinitionURI();
			Property isprop = ResourceFactory.createProperty(SemSimRelation.HAS_PHYSICAL_DEFINITION.getURIasString());
			URI furi = convertURItoIdentifiersDotOrgFormat(uri);
			Resource refres = rdf.createResource(furi.toString());
			Statement st = rdf.createStatement(ares, isprop, refres);
			
			addStatement(st);
		}
		
	}
		
	
	protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares){
		
		if(ds.hasPhysicalProperty()){
			Property iccfprop = SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty();
			Resource propres = getResourceForDataStructurePropertyAndAnnotate(rdf, (DataStructure)ds);
			Statement st = rdf.createStatement(ares, iccfprop, propres);
			
			addStatement(st);
			
			setDataStructurePropertyOfAnnotation((DataStructure)ds);
		}
	}
	
	
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
							Resource entity = getResourceForPMCandAnnotate(rdf, cpe.getArrayListOfEntities().get(0));
							Statement st = rdf.createStatement(
									propres, 
									SemSimRelation.PHYSICAL_PROPERTY_OF.getRDFproperty(), 
									entity);
							
							addStatement(st);
						}
					}
					// Otherwise it's a property of a process
					else{
						PhysicalProcess process = (PhysicalProcess)ds.getAssociatedPhysicalModelComponent();

						Resource processres = getResourceForPMCandAnnotate(rdf, ds.getAssociatedPhysicalModelComponent());
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
							setProcessParticipationRDFstatements(process, source, 
									SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty(), process.getSourceStoichiometry(source));
						}
						// Set the sinks
						for(PhysicalEntity sink : process.getSinkPhysicalEntities()){
							setProcessParticipationRDFstatements(process, sink,
									SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty(), process.getSinkStoichiometry(sink));
						}
						// Set the mediators
						for(PhysicalEntity mediator : process.getMediatorPhysicalEntities()){
							setProcessParticipationRDFstatements(process, mediator,
									SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty(), null);
						}
					}
				}
			}
		}
	}

	// For creating the statements that specify which physical entities participate in which processes
	@Override
	protected void setProcessParticipationRDFstatements(
			PhysicalProcess process, PhysicalEntity physent, Property relationship, Double multiplier){
				
		Resource processres = getResourceForPMCandAnnotate(rdf, process);
		
		// Create a new participant resource
		String type = null;
		
		if(relationship.getLocalName().equals(SemSimRelation.HAS_SOURCE_PARTICIPANT.getName())) type = "source";
		else if(relationship.getLocalName().equals(SemSimRelation.HAS_SINK_PARTICIPANT.getName())) type = "sink";
		else if(relationship.getLocalName().equals(SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getName())) type = "mediator";
		else return;
		
		Resource participantres = createNewResourceForSemSimObject(type);
		Statement partst = rdf.createStatement(processres, relationship, participantres);
		addStatement(partst);
		
		Resource physentrefres = null;
		
		// Create link between process participant and the physical entity it references
		if(physent.isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)){
			URI physentrefuri = setCompositePhysicalEntityMetadata((CompositePhysicalEntity)physent);
			physentrefres = rdf.getResource(physentrefuri.toString());
		}
		else physentrefres = getResourceForPMCandAnnotate(rdf, physent);
		
		if(physentrefres!=null){
			Statement st = rdf.createStatement(participantres, 
					SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty(), 
					physentrefres);
			addStatement(st);
		}
		else System.err.println("Error in setting participants for process: null value for Resource corresponding to " + physent.getName());

		// Add multiplier info
		if( multiplier != null && ! relationship.getLocalName().equals(SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getName())){
			Statement st = rdf.createStatement(participantres, 
					SemSimRelation.HAS_MULTIPLIER.getRDFproperty(), 
					multiplier.toString());
			
			addStatement(st);
		}
	}
	
	// Add statements that describe a composite physical entity in the model
	// Uses recursion to store all composite physical entities that make it up, too.
	@Override
	protected URI setCompositePhysicalEntityMetadata(CompositePhysicalEntity cpe){
		
		// Get the Resource corresponding to the index entity of the composite entity
		// If we haven't added this composite entity before, log it
		if(cpe.equals(SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, PMCandResourceURImap))){
			PMCandResourceURImap.put(cpe, URI.create(getResourceForPMCandAnnotate(rdf, cpe).getURI()));
		}
		// Otherwise use the CPE already stored
		else cpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, PMCandResourceURImap);
		
		URI indexuri = PMCandResourceURImap.get(cpe);
		Resource indexresource = null;
		
		if(indexuri == null){
			indexresource = getResourceForPMCandAnnotate(rdf, cpe);
			indexuri = URI.create(indexresource.getURI());
		}
		else indexresource = rdf.getResource(indexuri.toString());
		
		PhysicalEntity indexent = cpe.getArrayListOfEntities().get(0);
		
		setReferenceOrCustomResourceAnnotations(indexent, indexresource);

		if (cpe.getArrayListOfEntities().size()==1) return indexuri;
		
		// Truncate the composite by one entity
		ArrayList<PhysicalEntity> nextents = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> nextrels = new ArrayList<StructuralRelation>();
		
		for(int u = 1; u<cpe.getArrayListOfEntities().size(); u++){
			nextents.add(cpe.getArrayListOfEntities().get(u));
		}
		for(int u = 1; u<cpe.getArrayListOfStructuralRelations().size(); u++){
			nextrels.add(cpe.getArrayListOfStructuralRelations().get(u));
		}
		
		CompositePhysicalEntity nextcpe = new CompositePhysicalEntity(nextents, nextrels);
		URI nexturi = null;
		
		// Add sub-composites recursively
		if(nextcpe.getArrayListOfEntities().size()>1){
			
			// If we haven't added this composite entity before, log it
			if(nextcpe == SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, PMCandResourceURImap)){
				PMCandResourceURImap.put(nextcpe, URI.create(getResourceForPMCandAnnotate(rdf, nextcpe).getURI()));
			}
			// Otherwise use the CPE already stored
			else nextcpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, PMCandResourceURImap);
			
			nexturi = setCompositePhysicalEntityMetadata(nextcpe);
		}
		// If we're at the end of the composite
		else {
			PhysicalEntity lastent = nextcpe.getArrayListOfEntities().get(0);
			
			// If it's an entity we haven't processed yet
			if(!PMCandResourceURImap.containsKey(nextcpe.getArrayListOfEntities().get(0))){
				nexturi = URI.create(getResourceForPMCandAnnotate(rdf, lastent).getURI());
				PMCandResourceURImap.put(lastent, nexturi);
			}
			// Otherwise get the terminal entity that we logged previously
			else nexturi = PMCandResourceURImap.get(lastent);
		}
			
		Property structprop = StructuralRelation.PART_OF.getRDFproperty();
		StructuralRelation rel = cpe.getArrayListOfStructuralRelations().get(0);
		
		if(rel==StructuralRelation.CONTAINED_IN) structprop = StructuralRelation.CONTAINED_IN.getRDFproperty();
		
		Statement structst = rdf.createStatement(indexresource, structprop, rdf.getResource(nexturi.toString()));
		
		addStatement(structst);
		
		return indexuri;
	}
	
	
	
	// Get the RDF resource for a data structure's associated physical property
	protected Resource getResourceForDataStructurePropertyAndAnnotate(Model rdf, DataStructure ds){
		
		if(variablesAndPropertyResourceURIs.containsKey(ds)){
			return rdf.getResource(variablesAndPropertyResourceURIs.get(ds).toString());
		}
		
		Resource res = createNewResourceForSemSimObject("property");
		variablesAndPropertyResourceURIs.put(ds, URI.create(res.getURI()));
		setReferenceOrCustomResourceAnnotations(ds.getPhysicalProperty(), res);
		return res;
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
			if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
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
						if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
					}
				}
			}
			
			// If it is a custom entity or process, store the name and description
			if((pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_PROCESS)) || (pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_ENTITY))){
				
				if(pmc.getName()!=null){
					Statement namest = rdf.createStatement(res, 
							SemSimRelation.HAS_NAME.getRDFproperty(), pmc.getName());
					
					if(!rdf.contains(namest)) rdf.add(namest);
				}
				
				if(pmc.getDescription()!=null){
					Statement descst = rdf.createStatement(res, 
							AbstractRDFreader.dcterms_description, pmc.getDescription());
					
					addStatement(descst);
				}
			}
		}
	}

}