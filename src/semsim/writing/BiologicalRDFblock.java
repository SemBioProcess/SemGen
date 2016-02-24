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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import semsim.SemSimConstants;
import semsim.SemSimObject;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
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
import semsim.utilities.SemSimUtil;

public class BiologicalRDFblock {
	// For CompositePhysicalEntities, this relates a CPE with it's index entity Resource
	private Map<PhysicalModelComponent, URI> PMCandResourceURImap = new HashMap<PhysicalModelComponent,URI>(); 
	private Map<String, PhysicalModelComponent> ResourceURIandPMCmap = new HashMap<String, PhysicalModelComponent>();

	private Map<DataStructure, URI> variablesAndPropertyResourceURIs = new HashMap<DataStructure, URI>();
	private Map<URI, Resource> refURIsandresources = new HashMap<URI,Resource>();
	private Set<String> localids = new HashSet<String>();
	public SemSimModel semsimmodel;
	private String unnamedstring = "[unnamed!]";
	
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
	
	// Constructor
	public BiologicalRDFblock(SemSimModel semsimmodel, String rdfasstring, String baseNamespace){	
		this.semsimmodel = semsimmodel;
		
		if(rdfasstring != null){
			try {
				InputStream stream = new ByteArrayInputStream(rdfasstring.getBytes("UTF-8"));
					rdf.read(stream, baseNamespace, null);
					semsimmodel.setNamespace(rdf.getNsPrefixURI("model"));
			} 
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		localids.addAll(semsimmodel.getMetadataIDcomponentMap().keySet());

		rdf.setNsPrefix("semsim", SemSimConstants.SEMSIM_NAMESPACE);
		rdf.setNsPrefix("bqbiol", SemSimConstants.BQB_NAMESPACE);
		rdf.setNsPrefix("opb", SemSimConstants.OPB_NAMESPACE);
		rdf.setNsPrefix("ro", SemSimConstants.RO_NAMESPACE);
		rdf.setNsPrefix("model", semsimmodel.getNamespace());
	}
	
	//--------------------------------------------------------------------------------------------
	// Methods for collecting RDF-formatted annotation data and reading it into a SemSim model
	//--------------------------------------------------------------------------------------------
	
	public void getRDFforAnnotatedSemSimObject(SemSimObject sso){
		
		if(sso instanceof DataStructure){
			
			String uristart = semsimmodel.getNamespace();
			String dsidentifier = sso.getName();
			
			Resource resource = rdf.getResource(uristart + dsidentifier);
			
			collectFreeTextAnnotation(sso, resource);
//			collectSingularBiologicalAnnotation(sso);
			collectCompositeAnnotation((DataStructure)sso, resource);
		}
	}
	
	
	private SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource){	
		
		Resource physpropres = null;
		
		if(resource != null)
			physpropres = resource.getPropertyResourceValue(compcomponentfor);
		
		// If a physical property is specified for the data structure
		if(physpropres != null){
						
			Resource isannres = physpropres.getPropertyResourceValue(is);
			
			if(isannres == null)
				isannres = physpropres.getPropertyResourceValue(hasphysicaldefinition);
			
			// If the property is annotated against a reference ontology term
			if(isannres != null){
				
				URI uri = URI.create(isannres.getURI());

				// If an identifiers.org OPB namespace was used, replace it with the OPB's
				if(! uri.toString().startsWith(SemSimConstants.OPB_NAMESPACE))
					uri = swapInOPBnamespace(uri);
				
				PhysicalPropertyinComposite pp = getPhysicalPropertyInComposite(uri.toString());
				ds.setAssociatedPhysicalProperty(pp);
			}

			getPMCfromRDFresourceAndAnnotate(physpropres);
			
			Resource propertyofres = physpropres.getPropertyResourceValue(BiologicalRDFblock.physicalpropertyof);
			
			// If the physical property is a property of something...
			if(propertyofres!=null){
								
				PhysicalModelComponent pmc = getPMCfromRDFresourceAndAnnotate(propertyofres);
				ds.setAssociatedPhysicalModelComponent(pmc);
				
				// If it is a process
				if(pmc instanceof PhysicalProcess){
					PhysicalProcess process = (PhysicalProcess)pmc;
					NodeIterator sourceit = rdf.listObjectsOfProperty(propertyofres, BiologicalRDFblock.hassourceparticipant);
					
					// Read in the source participants
					while(sourceit.hasNext()){
						Resource sourceres = (Resource) sourceit.next();
						Resource physentres = sourceres.getPropertyResourceValue(BiologicalRDFblock.hasphysicalentityreference);
						PhysicalModelComponent sourcepmc = getPMCfromRDFresourceAndAnnotate(physentres);
						
						if(sourceres.getProperty(BiologicalRDFblock.hasmultiplier)!=null){
							Literal multiplier = sourceres.getProperty(BiologicalRDFblock.hasmultiplier).getObject().asLiteral();
							process.addSource((PhysicalEntity) sourcepmc, multiplier.getDouble());
						}
						else process.addSource((PhysicalEntity) sourcepmc, 1.0);
					}
					// Read in the sink participants
					NodeIterator sinkit = rdf.listObjectsOfProperty(propertyofres, BiologicalRDFblock.hassinkparticipant);
					while(sinkit.hasNext()){
						Resource sinkres = (Resource) sinkit.next();
						Resource physentres = sinkres.getPropertyResourceValue(BiologicalRDFblock.hasphysicalentityreference);
						PhysicalModelComponent sinkpmc = getPMCfromRDFresourceAndAnnotate(physentres);
						
						if(sinkres.getProperty(BiologicalRDFblock.hasmultiplier)!=null){
							Literal multiplier = sinkres.getProperty(BiologicalRDFblock.hasmultiplier).getObject().asLiteral();
							process.addSource((PhysicalEntity) sinkpmc, multiplier.getDouble());
						}
						else process.addSource((PhysicalEntity) sinkpmc, 1.0);
					}
					// Read in the mediator participants
					NodeIterator mediatorit = rdf.listObjectsOfProperty(propertyofres, BiologicalRDFblock.hasmediatorparticipant);
					while(mediatorit.hasNext()){
						Resource mediatorres = (Resource) mediatorit.next();
						Resource physentres = mediatorres.getPropertyResourceValue(BiologicalRDFblock.hasphysicalentityreference);
						PhysicalModelComponent mediatorpmc = getPMCfromRDFresourceAndAnnotate(physentres);
						process.addMediator((PhysicalEntity) mediatorpmc);
					}
				}
			}
		}
		
		return semsimmodel;
	}
	
	
	private PhysicalPropertyinComposite getPhysicalPropertyInComposite(String key) {
		PhysicalModelComponent term = ResourceURIandPMCmap.get(key);
		if (term==null) {
			String description = "";
			term = new PhysicalPropertyinComposite(description, URI.create(key));
			ResourceURIandPMCmap.put(key, term);
			semsimmodel.addAssociatePhysicalProperty((PhysicalPropertyinComposite) term);
		}
		return (PhysicalPropertyinComposite)term;
	}
	
	
	private PhysicalModelComponent getPMCfromRDFresourceAndAnnotate(Resource res){
		// Find the Physical Model Component corresponding to the resource's URI
		// Instantiate, if not present
		
		PhysicalModelComponent pmc = null;
		if(ResourceURIandPMCmap.containsKey(res.getURI()))
			pmc = ResourceURIandPMCmap.get(res.getURI());
		else{
			Resource isannres = res.getPropertyResourceValue(BiologicalRDFblock.is);
			if(isannres==null) isannres = res.getPropertyResourceValue(BiologicalRDFblock.hasphysicaldefinition);
			
			boolean isentity = res.getLocalName().startsWith("entity_");
			boolean isprocess = res.getLocalName().startsWith("process_");
			
			// If a physical entity
			if(isentity){
				
				// If a composite entity
				if(res.getPropertyResourceValue(BiologicalRDFblock.containedin)!=null || 
						res.getPropertyResourceValue(BiologicalRDFblock.partof)!=null)
					pmc = semsimmodel.addCompositePhysicalEntity(buildCompositePhysicalEntityfromRDFresource(res));
				
				// If a singular entity
				else {
					ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
					entlist.add(getCompositeEntityComponentFromResourceAndAnnotate(res));
					pmc = semsimmodel.addCompositePhysicalEntity(entlist, new ArrayList<StructuralRelation>());
				}
			}
			else if(isprocess){
				
				// If a reference process
				if(isannres!=null){
					pmc = semsimmodel.addReferencePhysicalProcess(
							new ReferencePhysicalProcess(URI.create(isannres.getURI()), isannres.getURI()));
				}
				// If a custom process
				else{
					String name = res.getProperty(BiologicalRDFblock.hasname).getString();
					if(name==null) name = unnamedstring;
					
					String description = null;
					
					if(res.getProperty(BiologicalRDFblock.description)!=null)
						description = res.getProperty(BiologicalRDFblock.description).getString();
					
					pmc = semsimmodel.addCustomPhysicalProcess(new CustomPhysicalProcess(name, description));
				}
			}
			
			Resource isversionofann = res.getPropertyResourceValue(BiologicalRDFblock.isversionof);
			if(isversionofann!=null){
				URI isversionofannURI = URI.create(isversionofann.getURI());
				pmc.addAnnotation(new ReferenceOntologyAnnotation(SemSimConstants.BQB_IS_VERSION_OF_RELATION, 
						isversionofannURI, isversionofannURI.toString()));
				if(isentity)
					semsimmodel.addReferencePhysicalEntity(
							new ReferencePhysicalEntity(isversionofannURI, isversionofannURI.toString()));
				else if(isprocess)
					semsimmodel.addReferencePhysicalProcess(
							new ReferencePhysicalProcess(isversionofannURI, isversionofannURI.toString()));
			}
			
			ResourceURIandPMCmap.put(res.getURI(), pmc);
		}
		return pmc;
	}
	
	
	private CompositePhysicalEntity buildCompositePhysicalEntityfromRDFresource(Resource propertyofres){
		Resource curres = propertyofres;
		
		ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
		PhysicalEntity startent = getCompositeEntityComponentFromResourceAndAnnotate(propertyofres);
		entlist.add(startent); // index physical entity
		
		while(true){
			Resource entityres = curres.getPropertyResourceValue(BiologicalRDFblock.containedin);
			
			boolean containedinlink = true;
			if(entityres==null){
				entityres = curres.getPropertyResourceValue(BiologicalRDFblock.partof);
				containedinlink = false;
			}
			
			// If the physical entity is linked to another as part of a composite physical entity
			if(entityres!=null){
				PhysicalEntity nextent = getCompositeEntityComponentFromResourceAndAnnotate(entityres);
				entlist.add(nextent);
				if(containedinlink) rellist.add(SemSimConstants.CONTAINED_IN_RELATION);
				else rellist.add(SemSimConstants.PART_OF_RELATION);
				
				curres = entityres;
			}
			else break;
		}
		if(entlist.size()>0 && rellist.size()>0){
			return new CompositePhysicalEntity(entlist, rellist);
		}
		return null;
	}
	
	
	private PhysicalEntity getCompositeEntityComponentFromResourceAndAnnotate(Resource res){	
		Resource isannres = res.getPropertyResourceValue(BiologicalRDFblock.is);
		
		if(isannres==null) isannres = res.getPropertyResourceValue(BiologicalRDFblock.hasphysicaldefinition);
		
		// If a reference entity
		// Create a singular physical entity from a component in a composite physical entity
		PhysicalEntity returnent = null;
		if(isannres!=null)
			 returnent = semsimmodel.addReferencePhysicalEntity(new ReferencePhysicalEntity(URI.create(isannres.getURI()), isannres.getURI()));
		
		// If a custom entity
		else returnent = addCustomPhysicalEntityToModel(res);
		
		return returnent;
	}
	
	
	// Collect the reference ontology term used to describe the model component
	public URI collectSingularBiologicalAnnotation(SemSimObject toann){
		
		URI singularannURI = null;

		
		/*
		Element mainrdfel = el.getChild("RDF", CellMLconstants.rdfNS);
		// If not present, look for it in main RDF block
		if(mainrdfel==null)
			mainrdfel = rdfblockelement;
		
		URI singularannURI = null;
		if(mainrdfel!=null){
			Iterator<?> descit = mainrdfel.getChildren("Description", CellMLconstants.rdfNS).iterator();
			
			while(descit.hasNext()){
				Element rdfdesc = (Element) descit.next();
				String about = rdfdesc.getAttributeValue("about", CellMLconstants.rdfNS);
				String ID = rdfdesc.getAttributeValue("ID", CellMLconstants.rdfNS);
				String ref = null;
				
				if(about!=null) ref = about.replace("#", "");
				else if(ID!=null) ref = ID;
				
				if(ref!=null){				
					if(ref.equals(toann.getMetadataID())){
						Element relel = rdfdesc.getChild("is", CellMLconstants.bqbNS);
						Element freeel = rdfdesc.getChild("description", CellMLconstants.dctermsNS);
						
						// If there is a singular annotation
						if(relel!=null){
							String term = relel.getAttributeValue("resource", CellMLconstants.rdfNS);
							if(term==null){
								Element objectdescel = relel.getChild("Description", CellMLconstants.rdfNS);
								if(objectdescel!=null){
									term = objectdescel.getAttributeValue("about", CellMLconstants.rdfNS);
									singularannURI = URI.create(term);
								}
							}
							else singularannURI = URI.create(term);
						}
						
						// If there is a free-text description
						if(freeel!=null){
							String freetext = freeel.getText();
							
							if(freetext!=null) toann.setDescription(freetext);
						}
					}
				}
			}
		}
		*/
		return singularannURI;
	}
	
	
	private void collectFreeTextAnnotation(SemSimObject sso, Resource resource){
		Statement st = resource.getProperty(description);
		
		if(st != null) sso.setDescription(st.getObject().toString());

	}
		
		
	private CustomPhysicalEntity addCustomPhysicalEntityToModel(Resource res){
		
		StmtIterator isversionofann = res.listProperties(BiologicalRDFblock.isversionof);
//		StmtIterator partofann = res.listProperties(BiologicalRDFblock.partof);
//		StmtIterator haspartann = res.listProperties(BiologicalRDFblock.haspart);
		
		// Collect all annotations on custom term
		Set<Statement> allannstatements = new HashSet<Statement>();
		allannstatements.addAll(isversionofann.toSet());
//		allannstatements.addAll(partofann.toSet());
//		allannstatements.addAll(haspartann.toSet());
		
		// Collect name		
		String name = res.getProperty(BiologicalRDFblock.hasname).getString();
		if(name==null) name = unnamedstring;
		
		// Collect description
		String description = null;
		if(res.getProperty(BiologicalRDFblock.description)!=null)
			description = res.getProperty(BiologicalRDFblock.description).getString();
		
		// Add custom entity to SemSim model
		CustomPhysicalEntity returnent = new CustomPhysicalEntity(name, description);
		semsimmodel.addCustomPhysicalEntity(returnent);
		
		// Iterate through annotations against reference ontology terms and add them to SemSim model
		for(Statement st : allannstatements){
			URI propuri = URI.create(st.getPredicate().getURI());
			SemSimRelation relation = SemSimConstants.URIS_AND_SEMSIM_RELATIONS.get(propuri);
			String objectURI = st.getObject().asResource().getURI();
		
			semsimmodel.addReferencePhysicalEntity(new ReferencePhysicalEntity(URI.create(objectURI), objectURI));
			returnent.addAnnotation(new ReferenceOntologyAnnotation(relation, URI.create(objectURI), objectURI));	
		}
		
		return returnent;
	}
	
	
	public PhysicalProperty getSingularPhysicalProperty(URI uri){
		PhysicalModelComponent term = ResourceURIandPMCmap.get(uri.toString());
		if (term==null) {
			term = new PhysicalProperty("", uri);
			ResourceURIandPMCmap.put(uri.toString(), term);
			semsimmodel.addPhysicalProperty((PhysicalProperty) term);
		}
		return (PhysicalProperty)term;
	}
	
	
	// Replace the namespace of a URI with the OPB's preferred namespace
	public URI swapInOPBnamespace(URI uri){
		String frag = SemSimOWLFactory.getIRIfragment(uri.toString());
		String uristring = SemSimConstants.OPB_NAMESPACE + frag;
		return URI.create(uristring);
	}
	
	
	
	//--------------------------------------------------------------------------------------------
	// Methods for translating SemSim model data into RDF-formatted annotations 
	//--------------------------------------------------------------------------------------------
	
	// Add RDF-formatted semantic metadata for an annotated data structure or submodel 
	public void setRDFforAnnotatedSemSimObject(SemSimObject sso){
		
		Boolean hasphysprop = false;
		
		if(sso instanceof DataStructure){
			hasphysprop = ((DataStructure)sso).hasPhysicalProperty();
		}
		
		// If we actually need to write out annotations
		if(sso.hasPhysicalDefinitionAnnotation() || ! sso.getDescription().equals("") || hasphysprop){
			
			String resuri = semsimmodel.getNamespace() + sso.getName();
			Resource ares = rdf.createResource(resuri);
			
			if(sso instanceof PhysicalModelComponent){
				String type = ((PhysicalModelComponent)sso).getComponentTypeasString();
				ares = createResourceForPhysicalModelComponent(type);
			}
			
			// Set the free-text annotation
			if( ! sso.getDescription().equals("")){
				Property ftprop = ResourceFactory.createProperty(CurationalMetadata.DCTERMS_NAMESPACE + "description");
				Statement st = rdf.createStatement(ares, ftprop, sso.getDescription());
				addRDFstatement(st, "dcterms", CurationalMetadata.DCTERMS_NAMESPACE, rdf);
			}
							
			// Add singular annotation
			if(sso.hasPhysicalDefinitionAnnotation()){
				URI uri = ((DataStructure)sso).getPhysicalDefinitionReferenceOntologyAnnotation().getReferenceURI();
				Property isprop = ResourceFactory.createProperty(SemSimConstants.BQB_IS_URI.toString());
				URI furi = convertURItoIdentifiersDotOrgFormat(uri);
				Resource refres = rdf.createResource(furi.toString());
				Statement st = rdf.createStatement(ares, isprop, refres);
				addRDFstatement(st, "bqbiol", SemSimConstants.BQB_NAMESPACE, rdf);
			}
			
			// If annotated thing is a variable, include the necessary composite annotation info
			if(hasphysprop){
				setDataStructurePropertyAndPropertyOfAnnotations((DataStructure)sso, ares);
			}
		}
		
	}
		
	
	protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure a, Resource ares){
		
		Property iccfprop = ResourceFactory.createProperty(SemSimConstants.IS_COMPUTATIONAL_COMPONENT_FOR_URI.toString());
		Resource propres = getResourceForDataStructurePropertyAndAnnotate(rdf, (DataStructure)a);
		Statement st = rdf.createStatement(ares, iccfprop, propres);
		
		if( ! rdf.contains(st)) rdf.add(st);
		
		setDataStructurePropertyOfAnnotation((DataStructure)a);
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
						if( ! rdf.contains(st)) rdf.add(st);
						
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
			URI physentrefuri = setCompositePhysicalEntityMetadata((CompositePhysicalEntity)participant);
			physentrefres = rdf.getResource(physentrefuri.toString());
		}
		else physentrefres = getResourceForPMCandAnnotate(rdf, participant);
		
		if(physentrefres!=null){
			Statement st = rdf.createStatement(participantres, hasphysicalentityreference, physentrefres);
			if(!rdf.contains(st)) rdf.add(st);
		}
		else System.err.println("Error in setting participants for process: null value for Resource corresponding to " + participant.getName());

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
			
		Property structprop = partof;
		StructuralRelation rel = cpe.getArrayListOfStructuralRelations().get(0);
		
		if(rel==SemSimConstants.CONTAINED_IN_RELATION) structprop = containedin;
		
		Statement structst = rdf.createStatement(indexresource, structprop, rdf.getResource(nexturi.toString()));
		
		if(!rdf.contains(structst)) rdf.add(structst);
		
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
	
	
	private void addRDFstatement(Statement st, String abrev, String namespace, Model therdf) {
		if( ! therdf.contains(st)){
			therdf.add(st);
			therdf.setNsPrefix(abrev, namespace);
		}
	}
	
	
	public static String getRDFmodelAsString(Model rdf){
		String syntax = "RDF/XML-ABBREV"; 
		StringWriter out = new StringWriter();
		rdf.write(out, syntax);
		return out.toString();
	}
	
	// Get the RDF resource for a physical model component (entity or process)
	protected Resource getResourceForPMCandAnnotate(Model rdf, PhysicalModelComponent pmc){
		
		String typeprefix = pmc.getComponentTypeasString();
		boolean isphysproperty = typeprefix.matches("property");
		
		if(PMCandResourceURImap.containsKey(pmc) && ! isphysproperty){
			return rdf.getResource(PMCandResourceURImap.get(pmc).toString());
		}
		
		if (typeprefix.matches("submodel") || typeprefix.matches("dependency"))
			typeprefix = "unknown";
		
		Resource res = createResourceForPhysicalModelComponent(typeprefix);
		
		if(! isphysproperty) PMCandResourceURImap.put(pmc, URI.create(res.getURI()));
		
		setReferenceOrCustomResourceAnnotations(pmc, res);
		
		return res;
	}
	
	// Get the RDF resource for a data structure's associated physical property
	protected Resource getResourceForDataStructurePropertyAndAnnotate(Model rdf, DataStructure ds){
		
		if(variablesAndPropertyResourceURIs.containsKey(ds)){
			return rdf.getResource(variablesAndPropertyResourceURIs.get(ds).toString());
		}
		
		Resource res = createResourceForPhysicalModelComponent("property");
		variablesAndPropertyResourceURIs.put(ds, URI.create(res.getURI()));
		setReferenceOrCustomResourceAnnotations(ds.getPhysicalProperty(), res);
		return res;
	}
	
	// Generate an RDF resource for a physical component
	private Resource createResourceForPhysicalModelComponent(String typeprefix){
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
					refres = findReferenceResourceFromURI(roa.getReferenceURI());
					
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
			
			// If it is a custom entity or process, store the name and description
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
		if(!uri.toString().startsWith("http://identifiers.org") 
				&& SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(namespace)){
			
			String kbname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(namespace);
			String fragment = SemSimOWLFactory.getIRIfragment(uri.toString());
			String newnamespace = null;
			
			// Look up new namespace
			for(String nskey : SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.keySet()){
				if(nskey.startsWith("http://identifiers.org") && 
						SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(nskey)==kbname){
					newnamespace = nskey;
				}
			}

			// Replacement rules for specific knowledge bases
			if(kbname==SemSimConstants.UNIPROT_FULLNAME){
				newuri = URI.create(newnamespace + fragment);
			}
			if(kbname==SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME){
				newuri = URI.create(newnamespace + fragment);
			}
			if(kbname==SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(kbname==SemSimConstants.GENE_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(kbname==SemSimConstants.CELL_TYPE_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(kbname==SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME){
				// Need to figure out how to get FMAIDs!!!!
			}
			if(kbname==SemSimConstants.MOUSE_ADULT_GROSS_ANATOMY_ONTOLOGY_FULLNAME){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
		}
		return newuri;
	}
}