package semsim.writing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
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

import semsim.definitions.ReferenceOntologies;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.RDFNamespace;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.SemSimUtil;

public class SemSimRDFwriter extends ModelWriter{
	
	// For CompositePhysicalEntities, this relates a CPE with it's index entity Resource
	private Map<PhysicalModelComponent, URI> PMCandResourceURImap = new HashMap<PhysicalModelComponent,URI>();
	private Map<DataStructure, URI> variablesAndPropertyResourceURIs = new HashMap<DataStructure, URI>();
	private Map<URI, Resource> refURIsandresources = new HashMap<URI,Resource>();
	private Set<String> localids = new HashSet<String>();
	public SemSimModel semsimmodel;
	private Map<String, String> submodelNameAndURImap = new HashMap<String, String>();

	
	public static Property dcterms_description = ResourceFactory.createProperty(RDFNamespace.DCTERMS.getNamespaceAsString(), "description");
	public Model rdf = ModelFactory.createDefaultModel();
	
	// Constructor without existing RDF block
	public SemSimRDFwriter(SemSimModel semsimmodel){
		super(null);
		
		initialize(semsimmodel);
	}
	
	// Constructor with existing RDF block
	public SemSimRDFwriter(SemSimModel semsimmodel, String rdfasstring, String baseNamespace){	
		super(null);
		
		initialize(semsimmodel);
		intializeExistingRDF(rdfasstring, baseNamespace);
	}
	
	
	private void initialize(SemSimModel semsimmodel){
		this.semsimmodel = semsimmodel;
		
		createSubmodelURIandNameMap();
		
		localids.addAll(semsimmodel.getMetadataIDcomponentMap().keySet());

		rdf.setNsPrefix("semsim", RDFNamespace.SEMSIM.getNamespaceAsString());
		rdf.setNsPrefix("bqbiol", RDFNamespace.BQB.getNamespaceAsString());
		rdf.setNsPrefix("opb", RDFNamespace.OPB.getNamespaceAsString());
		rdf.setNsPrefix("ro", RDFNamespace.RO.getNamespaceAsString());
		rdf.setNsPrefix("dcterms", RDFNamespace.DCTERMS.getNamespaceAsString());
		rdf.setNsPrefix("model", semsimmodel.getNamespace());
	}
	
	
	private void intializeExistingRDF(String rdfasstring, String baseNamespace){
		
		// If rdfasstring is not null, add it as rdf model
		if(rdfasstring != null){
			try {
				InputStream stream = new ByteArrayInputStream(rdfasstring.getBytes("UTF-8"));
					rdf.read(stream, baseNamespace, null);
					
					if(rdf.getNsPrefixURI("model") != null)
						semsimmodel.setNamespace(rdf.getNsPrefixURI("model"));
					
			} 
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	// Empty functions so that we can pass in sslib
	@Override
	public void writeToFile(File file){}
	
	@Override 
	public void writeToFile(URI uri){}
	
	
	private void createSubmodelURIandNameMap(){
		for(Submodel sub : semsimmodel.getSubmodels()){
			Resource subres = createNewResourceForSemSimObject("submodel");
			submodelNameAndURImap.put(sub.getName(), subres.getURI());
		}
	}
	
	// Add model-level annotations 
	protected void setRDFforModelLevelAnnotations(){
		
		Resource modelres = rdf.createResource(semsimmodel.getNamespace().replace("#", ""));
		
		for(Annotation ann : semsimmodel.getCurationalMetadata().getAnnotationList()){
						
			Property prop = ann.getRelation().getRDFproperty();
			Statement st = rdf.createStatement(modelres, prop, ann.getValue().toString());
			
			addStatement(st);
		}
		
	}
	
	// Add annotations for data structure
	protected void setRDFforDataStructureAnnotations(){
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			setRDFforDataStructureAnnotations(ds);
		}
	}
	
	
	protected void setRDFforDataStructureAnnotations(DataStructure ds){

		String resuri = semsimmodel.getNamespace() + ds.getName();
		Resource ares = rdf.createResource(resuri);
		
		// Set free-text annotation
		setFreeTextAnnotationForObject(ds, ares);
		
		// If a singular reference annotation is present, write it out
		setSingularAnnotationForDataStructure(ds, ares);
		
		// Include the necessary composite annotation info
		setDataStructurePropertyAndPropertyOfAnnotations(ds, ares);
	}
	
	
	// Add annotation for submodel
	protected void setRDFforSubmodelAnnotations(){
		
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
			Resource dsres = rdf.getResource(semsimmodel.getNamespace() + dsinsub.getName());
			Statement stds = rdf.createStatement(
					subres, 
					SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE.getRDFproperty(), 
					dsres);
			
			addStatement(stds);

		}
		
		// Write out which submodels are associated with the model
		for(Submodel subsub : sub.getSubmodels()){
			String subsubname = subsub.getName();
			Resource subsubres = rdf.getResource(submodelNameAndURImap.get(subsubname));
			Statement stsub = rdf.createStatement(
					subres, 
					SemSimRelation.INCLUDES_SUBMODEL.getRDFproperty(), 
					subsubres);
			
			addStatement(stsub);
		}
	}
	
	
	// Set free text annotation
	public void setFreeTextAnnotationForObject(SemSimObject sso, Resource ares){

		// Set the free-text annotation
		if( ! sso.getDescription().equals("")){
			Statement st = rdf.createStatement(ares, dcterms_description, sso.getDescription());
			
			addStatement(st);
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
	private void setProcessParticipationRDFstatements(
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
		if(physent instanceof CompositePhysicalEntity){
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
	private URI setCompositePhysicalEntityMetadata(CompositePhysicalEntity cpe){
		
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
	
	
//	private String createMetadataIDandSetNSPrefixes(SemSimObject annotated, String idprefix, Element el) {
//		String metaid = annotated.getMetadataID();
//		
//		// Create metadata ID for the model element, cache locally
//		if(metaid.isEmpty()){
//			metaid = idprefix + 0;
//			int n = 0;
//			while(metadataids.contains(metaid)){
//				n++;
//				metaid = idprefix + n;
//			}
//			metadataids.add(metaid);
//			el.setAttribute("id", metaid, CellMLconstants.cmetaNS);
//		}
//		
//		rdf.setNsPrefix("semsim", SemSimConstants.SEMSIM_NAMESPACE);
//		rdf.setNsPrefix("bqbiol", SemSimConstants.BQB_NAMESPACE);
//		rdf.setNsPrefix("dcterms", CurationalMetadata.DCTERMS_NAMESPACE);
//		return metaid;
//	}
	
	
	// Get the RDF resource for a physical model component (entity or process)
	protected Resource getResourceForPMCandAnnotate(Model rdf, PhysicalModelComponent pmc){
		
		String typeprefix = pmc.getComponentTypeasString();
		boolean isphysproperty = typeprefix.matches("property");
		
		if(PMCandResourceURImap.containsKey(pmc) && ! isphysproperty){
			return rdf.getResource(PMCandResourceURImap.get(pmc).toString());
		}
		
		if (typeprefix.matches("submodel") || typeprefix.matches("dependency"))
			typeprefix = "unknown";
		
		Resource res = createNewResourceForSemSimObject(typeprefix);
		
		if(! isphysproperty) PMCandResourceURImap.put(pmc, URI.create(res.getURI()));
		
		setReferenceOrCustomResourceAnnotations(pmc, res);
		
		return res;
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
	
	// Generate an RDF resource for a physical component
	private Resource createNewResourceForSemSimObject(String typeprefix){
		String resname = semsimmodel.getNamespace();	
		int idnum = 0;
		while(localids.contains(resname + typeprefix + "_" + idnum)){
			idnum++;
		}
		resname = resname + typeprefix + "_" + idnum;

		localids.add(resname);
		
		Resource res = rdf.createResource(resname);
		return res;
	}

	

	private void setReferenceOrCustomResourceAnnotations(PhysicalModelComponent pmc, Resource res){
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

			Property refprop = null;

			for(Annotation ann : pmc.getAnnotations()){
				// If the physical model component has either an "is" or "is version of" annotation, 
				// add the annotation statement to the RDF block
				
				if(ann instanceof ReferenceOntologyAnnotation){	
					
					ReferenceOntologyAnnotation roa = (ReferenceOntologyAnnotation)ann;
					refres = findReferenceResourceFromURI(roa.getReferenceURI());
					
					refprop = ResourceFactory.createProperty(roa.getRelation().getURI().toString());
					
					// Here we actually add the RDF statement on the resource
					// but for now, we only do hasPhysicalDefinition or isVersionOf.
					// When we figure out how to add part_of and has_part annotations, 
					// edit the following "if" statement here.					
					if(refprop.getURI().equals(SemSimRelation.BQB_IS_VERSION_OF.getURI())){

						Statement annagainstst = rdf.createStatement(res, refprop, refres);
						
						// If we have a reference resource and the annotation statement hasn't already 
						// been added to the RDF block, add it
						if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
					}
				}
			}
			
			// If it is a custom entity or process, store the name and description
			if((pmc instanceof CustomPhysicalProcess) || (pmc instanceof CustomPhysicalEntity)){
				
				if(pmc.getName()!=null){
					Statement namest = rdf.createStatement(
							res, 
							SemSimRelation.HAS_NAME.getRDFproperty(),
							pmc.getName());
					
					if(!rdf.contains(namest)) rdf.add(namest);
				}
				
				if(pmc.getDescription()!=null){
					Statement descst = rdf.createStatement(
							res, 
							dcterms_description, 
							pmc.getDescription());
					
					addStatement(descst);
				}
			}
		}
	}
	
	
	private Resource findReferenceResourceFromURI(URI uri){
		Resource refres = null;
		
		if(refURIsandresources.containsKey(uri))
			refres = refURIsandresources.get(uri);
		else{
			URI furi = convertURItoIdentifiersDotOrgFormat(uri);
			refres = rdf.createResource(furi.toString());
			refURIsandresources.put(furi, refres);
		}
		return refres;
	}
	
	
	public URI convertURItoIdentifiersDotOrgFormat(URI uri){
		URI newuri = uri;
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());

		// If we are looking at a URI that is NOT formatted according to identifiers.org
		if( ! uri.toString().startsWith("http://identifiers.org") 
				&& ReferenceOntologies.getReferenceOntologyByNamespace(namespace) != ReferenceOntology.UNKNOWN){
			
			ReferenceOntology refont = ReferenceOntologies.getReferenceOntologyByNamespace(namespace);
			String fragment = SemSimOWLFactory.getIRIfragment(uri.toString());
			String newnamespace = null;
			
			// Look up identifiers.org namespace
			for(String ns : refont.getNamespaces()){
				if(ns.startsWith("http://identifiers.org")
						&& ! ns.startsWith("http://identifiers.org/obo.")) newnamespace = ns;
			}

			// Replacement rules for specific knowledge bases
			if(refont==ReferenceOntology.UNIPROT){
				newuri = URI.create(newnamespace + fragment);
			}
			if(refont==ReferenceOntology.OPB){
				newuri = URI.create(newnamespace + fragment);
			}
			if(refont==ReferenceOntology.CHEBI){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.GO){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.CL){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.FMA){
				// TODO: Need to figure out how to get FMAIDs!!!!
			}
			if(refont==ReferenceOntology.MA){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
		}
		return newuri;
	}
	
	private void addStatement(Statement st){
		if( ! rdf.contains(st)) rdf.add(st);
	}
}