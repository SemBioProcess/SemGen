package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimConstants;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.StructuralRelation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.utilities.SemSimUtil;

public class CellMLbioRDFblock {
	// For CompositePhysicalEntities, this relates a CPE with it's index entity Resource
	private Map<PhysicalModelComponent, URI> pmcsandresourceURIs = new HashMap<PhysicalModelComponent,URI>(); 
	private Map<DataStructure, URI> variablesAndPropertyResourceURIs = new HashMap<DataStructure, URI>();
	private Map<URI, Resource> refURIsandresources = new HashMap<URI,Resource>();
	private Set<String> localids = new HashSet<String>();
	public String modelns;
	
	public static Property hassourceparticipant = ResourceFactory.createProperty(SemSimConstants.HAS_SOURCE_PARTICIPANT_URI.toString());
	public static Property hassinkparticipant = ResourceFactory.createProperty(SemSimConstants.HAS_SINK_PARTICIPANT_URI.toString());
	public static Property hasmediatorparticipant = ResourceFactory.createProperty(SemSimConstants.HAS_MEDIATOR_PARTICIPANT_URI.toString());
	public static Property hasphysicalentityreference = ResourceFactory.createProperty(SemSimConstants.HAS_PHYSICAL_ENTITY_REFERENCE_URI.toString());
	public static Property hasmultiplier = ResourceFactory.createProperty(SemSimConstants.HAS_MULTIPLIER_URI.toString());
	public static Property physicalpropertyof = ResourceFactory.createProperty(SemSimConstants.PHYSICAL_PROPERTY_OF_URI.toString());
	public static Property hasphysicaldefinition = ResourceFactory.createProperty(SemSimConstants.HAS_PHYSICAL_DEFINITION_URI.toString());
	public static Property is = ResourceFactory.createProperty(SemSimConstants.BQB_IS_URI.toString());
	public static Property isversionof = ResourceFactory.createProperty(SemSimConstants.BQB_IS_VERSION_OF_URI.toString());
	public static Property partof = ResourceFactory.createProperty(SemSimConstants.PART_OF_URI.toString());
	public static Property haspart = ResourceFactory.createProperty(SemSimConstants.HAS_PART_URI.toString());
	public static Property containedin = ResourceFactory.createProperty(SemSimConstants.CONTAINED_IN_URI.toString());
	public static Property compcomponentfor = ResourceFactory.createProperty(SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString());
	public static Property hasname = ResourceFactory.createProperty(SemSimConstants.HAS_NAME_URI.toString());
	public static Property description = ResourceFactory.createProperty(CurationalMetadata.DCTERMS_NAMESPACE + "description");

	public Model rdf = ModelFactory.createDefaultModel();
	
	public CellMLbioRDFblock(String namespace, String rdfasstring, String baseNamespace){	
		this.modelns = namespace;
		
		if(rdfasstring!=null){
			try {
				InputStream stream = new ByteArrayInputStream(rdfasstring.getBytes("UTF-8"));
					rdf.read(stream, baseNamespace, null);
			} 
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void addCompositeAnnotationMetadataForVariable(DataStructure ds){		
		// Collect physical model components with properties
		if(!ds.isImportedViaSubmodel()){
			
			if(ds.hasPhysicalProperty()){
				Resource propres = getResourceForDataStructurePropertyAndAnnotate(rdf, ds);

				if(ds.hasAssociatedPhysicalComponent()){
					PhysicalModelComponent propof = ds.getAssociatedPhysicalModelComponent();
					
					// If the variable is a property of an entity
					if(propof instanceof PhysicalEntity){
						CompositePhysicalEntity cpe = (CompositePhysicalEntity)propof;
						
						if (cpe.getArrayListOfEntities().size()>1) {
							// Get the Resource corresponding to the index entity of the composite entity
							URI indexuri = addCompositePhysicalEntityMetadata(cpe);
							Resource indexresource = rdf.getResource(indexuri.toString());
							Statement propofst = rdf.createStatement(propres, physicalpropertyof, indexresource);
							if(!rdf.contains(propofst)) rdf.add(propofst);
						}
						// else it's a singular physical entity
						else{
							Resource entity = getResourceForPMCandAnnotate(rdf, cpe.getArrayListOfEntities().get(0));
							Statement st = rdf.createStatement(propres, physicalpropertyof, entity);
							if(!rdf.contains(st)) rdf.add(st);
						}
					}
					// Otherwise it's a property of a process
					else{
						PhysicalProcess process = (PhysicalProcess)ds.getAssociatedPhysicalModelComponent();

						Resource processres = getResourceForPMCandAnnotate(rdf, ds.getAssociatedPhysicalModelComponent());
						Statement st = rdf.createStatement(propres, physicalpropertyof, processres);
						if(!rdf.contains(st)) rdf.add(st);
						
						// Set the sources
						for(PhysicalEntity source : process.getSourcePhysicalEntities()){
							setProcessParticipationRDFstatements(processres, source, hassourceparticipant);
						}
						// Set the sinks
						for(PhysicalEntity sink : process.getSinkPhysicalEntities()){
							setProcessParticipationRDFstatements(processres, sink, hassinkparticipant);
						}
						// Set the mediators
						for(PhysicalEntity mediator : process.getMediatorPhysicalEntities()){
							setProcessParticipationRDFstatements(processres, mediator, hasmediatorparticipant);
						}
					}
				}
			}
		}
	}

	// For creating the statements that specify which physical entities participate in which processes
	private void setProcessParticipationRDFstatements(Resource processres, PhysicalEntity participant, Property relationship){
		Resource participantres = getResourceForPMCandAnnotate(rdf, participant);
		Statement partst = rdf.createStatement(processres, relationship, participantres);
		if(!rdf.contains(partst)) rdf.add(partst);
		
		Resource physentrefres = null;
		
		// Create link between process participant and the physical entity it references
		if(participant instanceof CompositePhysicalEntity){
			URI physentrefuri = addCompositePhysicalEntityMetadata((CompositePhysicalEntity)participant);
			physentrefres = rdf.getResource(physentrefuri.toString());
		}
		else{
			physentrefres = getResourceForPMCandAnnotate(rdf, participant);
		}
		if(physentrefres!=null){
			Statement st = rdf.createStatement(participantres, hasphysicalentityreference, physentrefres);
			if(!rdf.contains(st)) rdf.add(st);
		}
		else{
			System.err.println("Error in setting participants for process: null value for Resource corresponding to " + participant.getName());
		}
	}
	
	// Add statements that describe a composite physical entity in the model
	// Uses recursion to store all composite physical entities that make it up, too.
	private URI addCompositePhysicalEntityMetadata(CompositePhysicalEntity cpe){
		
		// Get the Resource corresponding to the index entity of the composite entity
		// If we haven't added this composite entity before, log it
		if(cpe.equals(SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, pmcsandresourceURIs))){
			pmcsandresourceURIs.put(cpe, URI.create(getResourceForPMCandAnnotate(rdf, cpe).getURI()));
		}
		// Otherwise use the CPE already stored
		else cpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, pmcsandresourceURIs);
		
		URI indexuri = pmcsandresourceURIs.get(cpe);
		Resource indexresource = null;
		
		if(indexuri == null){
			indexresource = getResourceForPMCandAnnotate(rdf, cpe);
			indexuri = URI.create(indexresource.getURI());
		}
		else indexresource = rdf.getResource(indexuri.toString());
		
		PhysicalEntity indexent = cpe.getArrayListOfEntities().get(0);
		
		annotateReferenceOrCustomResource(indexent, indexresource);

		if (cpe.getArrayListOfEntities().size()==1) {
			return indexuri;
		}
		
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
			if(nextcpe == SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, pmcsandresourceURIs)){
				pmcsandresourceURIs.put(nextcpe, URI.create(getResourceForPMCandAnnotate(rdf, nextcpe).getURI()));
			}
			// Otherwise use the CPE already stored
			else nextcpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, pmcsandresourceURIs);
			
			nexturi = addCompositePhysicalEntityMetadata(nextcpe);
		}
		// If we're at the end of the composite
		else {
			PhysicalEntity lastent = nextcpe.getArrayListOfEntities().get(0);
			
			// If it's an entity we haven't processed yet
			if(!pmcsandresourceURIs.containsKey(nextcpe.getArrayListOfEntities().get(0))){
				nexturi = URI.create(getResourceForPMCandAnnotate(rdf, lastent).getURI());
				pmcsandresourceURIs.put(lastent, nexturi);
			}
			// Otherwise get the terminal entity that we logged previously
			else{
				nexturi = pmcsandresourceURIs.get(lastent);
			}
		}
			
		Property structprop = partof;
			
		StructuralRelation rel = cpe.getArrayListOfStructuralRelations().get(0);
		if(rel==SemSimConstants.CONTAINED_IN_RELATION) structprop = containedin;
		
		Statement structst = rdf.createStatement(indexresource, structprop, rdf.getResource(nexturi.toString()));
		if(!rdf.contains(structst)) rdf.add(structst);
		
		return indexuri;
	}
	
	public static String getRDFAsString(Model rdf){
		String syntax = "RDF/XML-ABBREV"; 
		StringWriter out = new StringWriter();
		rdf.write(out, syntax);
		return out.toString();
	}
	
	// Get the RDF resource for a physical model component (entity or process)
	protected Resource getResourceForPMCandAnnotate(Model rdf, PhysicalModelComponent pmc){
		
		String typeprefix = pmc.getComponentTypeasString();
		boolean isphysproperty = typeprefix.matches("property");
		
		if(pmcsandresourceURIs.containsKey(pmc) && ! isphysproperty){
			return rdf.getResource(pmcsandresourceURIs.get(pmc).toString());
		}
		
		if (typeprefix.matches("submodel") || typeprefix.matches("dependency"))
			typeprefix = "unknown";
		
		Resource res = createResourceForPhysicalModelComponent(typeprefix);
		
		if(! isphysproperty) pmcsandresourceURIs.put(pmc, URI.create(res.getURI()));
		
		annotateReferenceOrCustomResource(pmc, res);
		
		return res;
	}
	
	// Get the RDF resource for a data structure's associated physical property
	protected Resource getResourceForDataStructurePropertyAndAnnotate(Model rdf, DataStructure ds){
		
		if(variablesAndPropertyResourceURIs.containsKey(ds)){
			return rdf.getResource(variablesAndPropertyResourceURIs.get(ds).toString());
		}
		
		Resource res = createResourceForPhysicalModelComponent("property");
		variablesAndPropertyResourceURIs.put(ds, URI.create(res.getURI()));
		annotateReferenceOrCustomResource(ds.getPhysicalProperty(), res);
		return res;
	}
	
	// Generate an RDF resource for a physical component
	private Resource createResourceForPhysicalModelComponent(String typeprefix){
		String resname = modelns;	
		int idnum = 0;
		while(localids.contains(resname + typeprefix + "_" + idnum)){
			idnum++;
		}
		resname = resname + typeprefix + "_" + idnum;

		localids.add(resname);
		
		Resource res = rdf.createResource(resname);
		return res;
	}

	private Resource getReferenceResourceFromURI(URI uri){
		Resource refres = null;
		if(refURIsandresources.containsKey(uri))
			refres = refURIsandresources.get(uri);
		else{
			URI furi = CellMLwriter.formatAsIdentifiersDotOrgURI(uri);
			refres = rdf.createResource(furi.toString());
			refURIsandresources.put(furi, refres);
		}
		return refres;
	}

	private void annotateReferenceOrCustomResource(PhysicalModelComponent pmc, Resource res){
		Resource refres = null;
		
		// If it's a reference resource
		if(pmc instanceof ReferenceTerm){
			
			URI uri = ((ReferenceTerm)pmc).getPhysicalDefinitionURI();
			refres = getReferenceResourceFromURI(uri);
			
			Statement annagainstst = rdf.createStatement(res, hasphysicaldefinition, refres);
				
			// If we have a reference resource and the annotation statement hasn't already 
			// been added to the RDF block, add it
			if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
		}
		
		// If it's a custom resource
		else{

			Property refprop = null;

			for(Annotation ann : pmc.getAnnotations()){
				// If the physical model component has either an "is" or "is version of" annotation, 
				// add the annotation statement to the RDF block
				
				if(ann instanceof ReferenceOntologyAnnotation){	
					
					ReferenceOntologyAnnotation roa = (ReferenceOntologyAnnotation)ann;
					refres = getReferenceResourceFromURI(roa.getReferenceURI());
					
					refprop = ResourceFactory.createProperty(roa.getRelation().getURI().toString());
					
					// Here we actually add the RDF statement on the resource
					// but for now, we only do hasPhysicalDefinition or isVersionOf.
					// When we figure out how to add part_of and has_part annotations, 
					// edit the following "if" statement here.					
					if(refprop.getURI().equals(isversionof.getURI())){

						Statement annagainstst = rdf.createStatement(res, refprop, refres);
						
						// If we have a reference resource and the annotation statement hasn't already 
						// been added to the RDF block, add it
						if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
					}
				}
			}
			
			// If it is a custom entity or process. Store the name and description
			if((pmc instanceof CustomPhysicalProcess) || (pmc instanceof CustomPhysicalEntity)){
				
				if(pmc.getName()!=null){
					Statement namest = rdf.createStatement(res, hasname, pmc.getName());
					
					if(!rdf.contains(namest)) rdf.add(namest);
				}
				
				if(pmc.getDescription()!=null){
					Statement descst = rdf.createStatement(res, description, pmc.getDescription());
					
					if(!rdf.contains(descst)) rdf.add(descst);
				}
			}
		}
	}
}