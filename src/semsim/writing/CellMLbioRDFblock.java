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
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.annotation.StructuralRelation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.CustomPhysicalEntity;
import semsim.model.physical.CustomPhysicalProcess;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;

public class CellMLbioRDFblock {
		
	public Set<PhysicalModelComponent> pmcswithproperties;
	public Map<PhysicalModelComponent, URI> pmcsandresourceURIs; // For CompositePhysicalEntities, this relates a CPE with it's index entity Resource
	public Map<URI, Resource> refURIsandresources;
	public Set<String> localids = new HashSet<String>();
	public String modelns;
	
	public static Property hassourceparticipant = ResourceFactory.createProperty(SemSimConstants.HAS_SOURCE_PARTICIPANT_URI.toString());
	public static Property hassinkparticipant = ResourceFactory.createProperty(SemSimConstants.HAS_SINK_PARTICIPANT_URI.toString());
	public static Property hasmediatorparticipant = ResourceFactory.createProperty(SemSimConstants.HAS_MEDIATOR_PARTICIPANT_URI.toString());
	public static Property hasphysicalentityreference = ResourceFactory.createProperty(SemSimConstants.HAS_PHYSICAL_ENTITY_REFERENCE_URI.toString());
	public static Property hasmultiplier = ResourceFactory.createProperty(SemSimConstants.HAS_MULTIPLIER_URI.toString());
	public static Property physicalpropertyof = ResourceFactory.createProperty(SemSimConstants.PHYSICAL_PROPERTY_OF_URI.toString());
	public static Property refersto = ResourceFactory.createProperty(SemSimConstants.REFERS_TO_URI.toString());
	public static Property is = ResourceFactory.createProperty(SemSimConstants.BQB_IS_URI.toString());
	public static Property isversionof = ResourceFactory.createProperty(SemSimConstants.BQB_IS_VERSION_OF_URI.toString());
	public static Property partof = ResourceFactory.createProperty(SemSimConstants.PART_OF_URI.toString());
	public static Property containedin = ResourceFactory.createProperty(SemSimConstants.CONTAINED_IN_URI.toString());
	public static Property compcomponentfor = ResourceFactory.createProperty(SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString());
	public static Property hasname = ResourceFactory.createProperty(SemSimConstants.HAS_NAME_URI.toString());
	public static Property description = ResourceFactory.createProperty(SemSimConstants.DCTERMS_NAMESPACE + "description");
	
	public SemSimModel semsimmodel;
	public Model rdf;
	
	public CellMLbioRDFblock(SemSimModel model, String rdfasstring, String baseNamespace){
		
		pmcsandresourceURIs = new HashMap<PhysicalModelComponent,URI>();
		refURIsandresources = new HashMap<URI,Resource>();
		pmcswithproperties = new HashSet<PhysicalModelComponent>();
		rdf = ModelFactory.createDefaultModel();
		
		this.modelns = model.getNamespace();
		semsimmodel = model;
		
		if(rdfasstring!=null){
			String otherrdf = rdfasstring;

			try {
				InputStream stream = new ByteArrayInputStream(otherrdf.getBytes("UTF-8"));
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
			
			if(ds.getPhysicalProperty()!=null){
				PhysicalProperty prop = ds.getPhysicalProperty();
				
				if(prop.hasRefersToAnnotation()){
					Resource propres = getResourceForPMCandAnnotate(rdf, prop);

					// Only include physical properties that have reference annotations (exclude unannotated physical properties)
					if(prop.getPhysicalPropertyOf()!=null){
						PhysicalModelComponent propof = prop.getPhysicalPropertyOf();
						
						// If the variable is a property of an entity
						if(propof instanceof PhysicalEntity){
							
							// If the entity is a composite entity
							if(propof instanceof CompositePhysicalEntity){
								CompositePhysicalEntity cpe = (CompositePhysicalEntity)propof;
								
								// Get the Resource corresponding to the index entity of the composite entity
								URI indexuri = addCompositePhysicalEntityMetadata(cpe);
								Resource indexresource = rdf.getResource(indexuri.toString());
								Statement propofst = rdf.createStatement(propres, physicalpropertyof, indexresource);
								if(!rdf.contains(propofst)) rdf.add(propofst);
							}
							// else it's a singular physical entity
							else{
								Resource entity = getResourceForPMCandAnnotate(rdf, prop.getPhysicalPropertyOf());
								Statement st = rdf.createStatement(propres, physicalpropertyof, entity);
								if(!rdf.contains(st)) rdf.add(st);
							}
						}
						// Otherwise it's a property of a process
						else{
							PhysicalProcess process = (PhysicalProcess)prop.getPhysicalPropertyOf();

							Resource processres = getResourceForPMCandAnnotate(rdf, prop.getPhysicalPropertyOf());
							Statement st = rdf.createStatement(propres, physicalpropertyof, processres);
							if(!rdf.contains(st)) rdf.add(st);
							
							// Set the sources
							for(PhysicalEntity source : process.getSources()){
								setProcessParticipationRDFstatements(processres, source, hassourceparticipant);
							}
							// Set the sinks
							for(PhysicalEntity sink : process.getSinks()){
								setProcessParticipationRDFstatements(processres, sink, hassinkparticipant);
							}
							// Set the mediators
							for(PhysicalEntity mediator : process.getMediators()){
								setProcessParticipationRDFstatements(processres, mediator, hasmediatorparticipant);
							}
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
		if(cpe == SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, pmcsandresourceURIs)){
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
			
			// If we haven't added this composite entity before, log it
			if(nextcpe == SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, pmcsandresourceURIs)){
				pmcsandresourceURIs.put(nextcpe, URI.create(getResourceForPMCandAnnotate(rdf, nextcpe).getURI()));
			}
			// Otherwise use the CPE already stored
			else nextcpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, pmcsandresourceURIs);
			
			nexturi = addCompositePhysicalEntityMetadata(nextcpe);
		}
		// If we're at the end of the composite
		else{
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
	
	protected Resource getResourceForPMCandAnnotate(Model rdf, PhysicalModelComponent pmc){
		if(pmcsandresourceURIs.containsKey(pmc)){
			return rdf.getResource(pmcsandresourceURIs.get(pmc).toString());
		}
		else{
			System.out.println(pmc.getName());
	
			String resname = modelns;
			
			String typeprefix = "unknown";
			if(pmc instanceof PhysicalProperty)
				typeprefix = "property";
			else if(pmc instanceof PhysicalProcess)
				typeprefix = "process";
			else if(pmc instanceof PhysicalEntity)
				typeprefix = "entity";
			
			int idnum = 0;
			while(localids.contains(resname + typeprefix + "_" + idnum)){
				idnum++;
			}
			resname = resname + typeprefix + "_" + idnum;

			localids.add(resname);
			
			Resource res = rdf.createResource(resname);
			pmcsandresourceURIs.put(pmc, URI.create(res.getURI()));
			
			annotateReferenceOrCustomResource(pmc, res);
			
			return res;
		}
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
		ReferenceOntologyAnnotation ann = null;
		Resource refres = null;
		
		// If there is an "is" annotation (aka "refersTo")
		if(pmc.hasRefersToAnnotation()){
			ann = pmc.getFirstRefersToReferenceOntologyAnnotation();
		}
		// Else use the first "isVersionOf" annotation found
		else if(pmc.getReferenceOntologyAnnotations(SemSimConstants.BQB_IS_VERSION_OF_RELATION).size()>0){
			for(ReferenceOntologyAnnotation ann0 : pmc.getReferenceOntologyAnnotations(SemSimConstants.BQB_IS_VERSION_OF_RELATION)){
				ann = ann0;
				break;
			}
		}
		
		// If the physical model component has either an "is" or "is version of" annotation, 
		// add the annotation statement to the RDF block
		if(ann!=null){
			
			refres = getReferenceResourceFromURI(ann.getReferenceURI());
			
			Property refprop = 
				(ann.getRelation()==SemSimConstants.BQB_IS_RELATION || ann.getRelation()==SemSimConstants.REFERS_TO_RELATION) ? is : isversionof; 
			
			// Use SemSim:refersTo for annotated Physical model components
			if(refprop == is){
				if(pmc instanceof PhysicalProperty || pmc instanceof PhysicalEntity
						|| pmc instanceof PhysicalProcess) refprop = refersto;
			}
			// If we have a reference resource and the annotation statement hasn't already 
			// been added to the RDF block, add it
			Statement annagainstst = rdf.createStatement(res, refprop, refres);
			if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
			
			System.out.println(ann.getReferenceURI());
		}
		
		// If it is a custom entity or process. Store the name and description
		if((pmc instanceof CustomPhysicalProcess) || (pmc instanceof CustomPhysicalEntity)){
			System.out.println("CUSTOM");
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
