package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.semanticweb.owlapi.model.OWLException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
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
import semsim.reading.ModelClassifier.ModelType;

public class SemSimRDFreader extends ModelReader{

	private Map<String, PhysicalModelComponent> ResourceURIandPMCmap = new HashMap<String, PhysicalModelComponent>();
	public Model rdf = ModelFactory.createDefaultModel();
	private String unnamedstring = "[unnamed!]";
	public static Property dcterms_description = ResourceFactory.createProperty(RDFNamespace.DCTERMS.getNamespaceasString(), "description");
	private Map<String, Submodel> submodelURIandObjectMap = new HashMap<String, Submodel>();
	public static String TEMP_NAMESPACE = "http://tempns.net/temp";
	private ModelType modeltype;
	private boolean modelNamespaceIsSet = true;

	public SemSimRDFreader(ModelAccessor accessor, SemSimModel semsimmodel, String rdfasstring, ModelType modeltype) {
		super(accessor);
		
		this.semsimmodel = semsimmodel;
		this.modeltype = modeltype;
		
		if(rdfasstring!=null){
			readStringToRDFmodel(rdf, rdfasstring);
			createSemSimSubmodelURIandObjectMap();
		}
		
		//If an explicit namespace is specified with the "model" prefix, use it
		semsimmodel.setNamespace(getModelNamespaceFromRDF());
	}
	
	public boolean hasPropertyAnnotationForDataStructure(DataStructure ds){
		//Only used for reading RDF in SBML models currently, so we look up by metadata ID
		return rdf.contains(rdf.getResource(semsimmodel.getNamespace() + ds.getMetadataID()), 
				SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty());
	}

	private void createSemSimSubmodelURIandObjectMap(){
		
		ResIterator subit = rdf.listSubjectsWithProperty(SemSimRelation.HAS_NAME.getRDFproperty());
		
		while(subit.hasNext()){
			Resource submodelres = subit.next();
						
			if(submodelres.getURI() != null){
				
				if(submodelres.getURI().contains("#submodel_")){
					Statement subnamest = submodelres.getProperty(SemSimRelation.HAS_NAME.getRDFproperty());
					String name = subnamest.getLiteral().toString();
					
					Submodel newsub = new Submodel(name);
					submodelURIandObjectMap.put(submodelres.getURI(), newsub);
					semsimmodel.addSubmodel(newsub);
				}
			}
		}
	}
	
	@Override
	public SemSimModel read() throws IOException, InterruptedException,
			OWLException, CloneNotSupportedException, XMLStreamException {
		return null;
	}
	
	public String getModelNamespaceFromRDF(){
		String ns = rdf.getNsPrefixURI("model");
		
		if(ns == null ){
			modelNamespaceIsSet = false;
			ns = semsimmodel.generateNamespaceFromDateAndTime();
		}

		return ns;
	}
	
	// Get model-level annotations
	public void getModelLevelAnnotations(){
		Resource modelres = rdf.createResource(semsimmodel.getNamespace().replace("#", ""));
		StmtIterator stit = modelres.listProperties();
				
		while(stit.hasNext()){
			
			Statement st = stit.next();
			URI predicateURI = URI.create(st.getPredicate().getURI());
			
			if (predicateURI.equals(SemSimLibrary.SEMSIM_VERSION_IRI.toURI())) {
				semsimmodel.setSemSimVersion(st.getObject().asLiteral().toString());
			}
			
			else if (predicateURI.equals(SemSimModel.LEGACY_CODE_LOCATION_IRI.toURI())) {
				ModelAccessor ma = new ModelAccessor(st.getObject().asLiteral().toString());
				semsimmodel.setSourceFileLocation(ma);
			}
			
			else{
				Metadata m = getMetadataByURI(predicateURI);
				String value = st.getObject().toString();
				semsimmodel.getCurationalMetadata().setAnnotationValue(m, value);
			}
		}

	}
	
	// Get all the data structure annotations
	public void getAllDataStructureAnnotations(){
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			getDataStructureAnnotations(ds);
		}
	}
	
	public void getDataStructureAnnotations(DataStructure ds){
		
		// If we're reading a CellML model use a temp namespace
		// otherwise use the namespace that was specified and assigned to the SemSim model
		Resource resource = null;
		
		switch(modeltype){
		
			// If a CellML model and namespace is set, use namespace, otherwise use temp namespace and metadata ID.
			// The former is to accommodate CellML models annotated using previous SG versions.
			case CELLML_MODEL: resource = modelNamespaceIsSet ? rdf.getResource(semsimmodel.getNamespace() + ds.getName()) : 
				rdf.getResource(TEMP_NAMESPACE + "#" + ds.getMetadataID());
				 break;
			 
			// If an MML model in a JSim project file, use the model namespace (included in serialization) and 
			// data structure name b/c we don't use metadata ID's in MML files
			case MML_MODEL_IN_PROJ: resource = rdf.getResource(semsimmodel.getNamespace() + ds.getName());
				 break;
			 
			 // If an SBML model, use the model namespace (included in serialization) and
			 // metadata ID
			case SBML_MODEL: resource = rdf.getResource(semsimmodel.getNamespace() + ds.getMetadataID());
				 break;
			
			default: resource = rdf.getResource(semsimmodel.getNamespace() + ds.getName());
				break;
		}
				
		collectFreeTextAnnotation(ds, resource);
		collectSingularBiologicalAnnotation(ds, resource);
		collectCompositeAnnotation(ds, resource);
	}
	
	// Get the annotations on a submodel
	protected void getAllSubmodelAnnotations(){
		
		for(String suburi : submodelURIandObjectMap.keySet()){
			
			Resource subres = rdf.getResource(suburi); // collect name
			
			Submodel sub = submodelURIandObjectMap.get(suburi);
			collectFreeTextAnnotation(sub, subres); // collect description
			
			// Collect associated data structures
			StmtIterator dsstit = subres.listProperties(SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE.getRDFproperty());
			while(dsstit.hasNext()){
				Statement dsst = dsstit.next();
				Resource dsresinsub = dsst.getResource();
				sub.addDataStructure(semsimmodel.getAssociatedDataStructure(dsresinsub.getLocalName()));
			}
			
			// Collect submodels of submodels
			StmtIterator substit = subres.listProperties(SemSimRelation.INCLUDES_SUBMODEL.getRDFproperty());
			while(substit.hasNext()){
				Statement subst = substit.next();
				Resource subsubres = subst.getResource();
				sub.addSubmodel(submodelURIandObjectMap.get(subsubres.getURI()));
			}			
		}		
	}
	
	
	private SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource){	
		
		Resource physpropres = null;
		
		if(resource != null){
			physpropres = resource.getPropertyResourceValue(SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty());
		}
		
		// If a physical property is specified for the data structure
		if(physpropres != null){
						
			Resource isannres = physpropres.getPropertyResourceValue(SemSimRelation.BQB_IS.getRDFproperty());
			
			if(isannres == null)
				isannres = physpropres.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
			
			// If the property is annotated against a reference ontology term
			if(isannres != null){
				
				URI uri = URI.create(isannres.getURI());

				// If an identifiers.org OPB namespace was used, replace it with the OPB's
				if(! uri.toString().startsWith(RDFNamespace.OPB.getNamespaceasString()))
					uri = swapInOPBnamespace(uri);
				
				PhysicalPropertyinComposite pp = getPhysicalPropertyInComposite(uri.toString());
				ds.setAssociatedPhysicalProperty(pp);
			}

			getPMCfromRDFresourceAndAnnotate(physpropres);
			
			Resource propertyofres = physpropres.getPropertyResourceValue(SemSimRelation.PHYSICAL_PROPERTY_OF.getRDFproperty());
			
			// If the physical property is a property of something...
			if(propertyofres!=null){
								
				PhysicalModelComponent pmc = getPMCfromRDFresourceAndAnnotate(propertyofres);
				ds.setAssociatedPhysicalModelComponent(pmc);
				
				// If it is a process
				if(pmc instanceof PhysicalProcess){
					PhysicalProcess process = (PhysicalProcess)pmc;
					NodeIterator sourceit = rdf.listObjectsOfProperty(propertyofres, SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty());
					
					// Read in the source participants
					while(sourceit.hasNext()){
						Resource sourceres = (Resource) sourceit.next();
						Resource physentres = sourceres.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty());
						PhysicalModelComponent sourcepmc = getPMCfromRDFresourceAndAnnotate(physentres);
						
						if(sourceres.getProperty(SemSimRelation.HAS_MULTIPLIER.getRDFproperty())!=null){
							Literal multiplier = sourceres.getProperty(SemSimRelation.HAS_MULTIPLIER.getRDFproperty()).getObject().asLiteral();
							process.addSource((PhysicalEntity) sourcepmc, multiplier.getDouble());
						}
						else process.addSource((PhysicalEntity) sourcepmc, 1.0);
					}
					// Read in the sink participants
					NodeIterator sinkit = rdf.listObjectsOfProperty(propertyofres, SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty());
					while(sinkit.hasNext()){
						Resource sinkres = (Resource) sinkit.next();
						Resource physentres = sinkres.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty());
						PhysicalModelComponent sinkpmc = getPMCfromRDFresourceAndAnnotate(physentres);
						
						if(sinkres.getProperty(SemSimRelation.HAS_MULTIPLIER.getRDFproperty())!=null){
							Literal multiplier = sinkres.getProperty(SemSimRelation.HAS_MULTIPLIER.getRDFproperty()).getObject().asLiteral();
							process.addSink((PhysicalEntity) sinkpmc, multiplier.getDouble());
						}
						else process.addSink((PhysicalEntity) sinkpmc, 1.0);
					}
					// Read in the mediator participants
					NodeIterator mediatorit = rdf.listObjectsOfProperty(propertyofres, SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty());
					while(mediatorit.hasNext()){
						Resource mediatorres = (Resource) mediatorit.next();
						Resource physentres = mediatorres.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty());
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
			Resource isannres = res.getPropertyResourceValue(SemSimRelation.BQB_IS.getRDFproperty());
			if(isannres==null) isannres = res.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
			
			boolean isentity = res.getLocalName().startsWith("entity_");
			boolean isprocess = res.getLocalName().startsWith("process_");
			
			// If a physical entity
			if(isentity){
				
				// If a composite entity
				if(res.getPropertyResourceValue(StructuralRelation.CONTAINED_IN.getRDFproperty())!=null || 
						res.getPropertyResourceValue(StructuralRelation.PART_OF.getRDFproperty())!=null)
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
					String name = res.getProperty(SemSimRelation.HAS_NAME.getRDFproperty()).getString();
					if(name==null) name = unnamedstring;
					
					String description = null;
					
					if(res.getProperty(dcterms_description)!=null)
						description = res.getProperty(dcterms_description).getString();
					
					pmc = semsimmodel.addCustomPhysicalProcess(new CustomPhysicalProcess(name, description));
				}
			}
			
			Resource isversionofann = res.getPropertyResourceValue(SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty());
			if(isversionofann!=null){
				URI isversionofannURI = URI.create(isversionofann.getURI());
				pmc.addAnnotation(new ReferenceOntologyAnnotation(SemSimRelation.BQB_IS_VERSION_OF, 
						isversionofannURI, isversionofannURI.toString(), sslib));
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
			Resource entityres = curres.getPropertyResourceValue(StructuralRelation.CONTAINED_IN.getRDFproperty());
			
			boolean containedinlink = true;
			if(entityres==null){
				entityres = curres.getPropertyResourceValue(StructuralRelation.PART_OF.getRDFproperty());
				containedinlink = false;
			}
			
			// If the physical entity is linked to another as part of a composite physical entity
			if(entityres!=null){
				PhysicalEntity nextent = getCompositeEntityComponentFromResourceAndAnnotate(entityres);
				entlist.add(nextent);
				if(containedinlink) rellist.add(StructuralRelation.CONTAINED_IN);
				else rellist.add(StructuralRelation.PART_OF);
				
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
		Resource isannres = res.getPropertyResourceValue(SemSimRelation.BQB_IS.getRDFproperty());
		
		if(isannres==null) isannres = res.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
		
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
	public void collectSingularBiologicalAnnotation(DataStructure ds, Resource resource){
		
		Statement singannst = resource.getProperty(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());

		if(singannst != null){
			URI singularannURI = URI.create(singannst.getObject().asResource().getURI());
			PhysicalProperty prop = getSingularPhysicalProperty(singularannURI);
			ds.setSingularAnnotation(prop);
		}
	}
	
	
	public void collectFreeTextAnnotation(SemSimObject sso, Resource resource){
		Statement st = resource.getProperty(dcterms_description);		
		if(st != null)
			sso.setDescription(st.getObject().toString());
		
	}
		
		
	private CustomPhysicalEntity addCustomPhysicalEntityToModel(Resource res){
		
		StmtIterator isversionofann = res.listProperties(SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty());
//		StmtIterator partofann = res.listProperties(BiologicalRDFblock.partof);
//		StmtIterator haspartann = res.listProperties(BiologicalRDFblock.haspart);
		
		// Collect all annotations on custom term
		Set<Statement> allannstatements = new HashSet<Statement>();
		allannstatements.addAll(isversionofann.toSet());
//		allannstatements.addAll(partofann.toSet());
//		allannstatements.addAll(haspartann.toSet());
		
		// Collect name		
		String name = res.getProperty(SemSimRelation.HAS_NAME.getRDFproperty()).getString();
		if(name==null) name = unnamedstring;
		
		// Collect description
		String description = null;
		if(res.getProperty(dcterms_description)!=null)
			description = res.getProperty(dcterms_description).getString();
		
		// Add custom entity to SemSim model
		CustomPhysicalEntity returnent = new CustomPhysicalEntity(name, description);
		semsimmodel.addCustomPhysicalEntity(returnent);
		
		// Iterate through annotations against reference ontology terms and add them to SemSim model
		for(Statement st : allannstatements){
			URI propuri = URI.create(st.getPredicate().getURI());
			Relation relation = SemSimRelations.getRelationFromURI(propuri);
			String objectURI = st.getObject().asResource().getURI();
		
			semsimmodel.addReferencePhysicalEntity(new ReferencePhysicalEntity(URI.create(objectURI), objectURI));
			returnent.addAnnotation(new ReferenceOntologyAnnotation(relation, URI.create(objectURI), objectURI, sslib));	
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
	
	// For looking up Metadata items in CurationalMetadata by URI
	private Metadata getMetadataByURI(URI uri){
		
		for(Metadata m : Metadata.values()){
			if(m.getURI().equals(uri)){
				return m;
			}
		}
		
		return null;
	}
	
	
	// Replace the namespace of a URI with the OPB's preferred namespace
	public URI swapInOPBnamespace(URI uri){
		String frag = SemSimOWLFactory.getIRIfragment(uri.toString());
		String uristring = RDFNamespace.OPB.getNamespaceasString() + frag;
		
		return URI.create(uristring);
	}
	
	public static void readStringToRDFmodel(Model rdf, String rdfasstring){
			
		try {
			InputStream stream = new ByteArrayInputStream(rdfasstring.getBytes("UTF-8"));
			RDFReader reader = rdf.getReader();
			reader.setProperty("relativeURIs","same-document,relative");
			reader.read(rdf, stream, TEMP_NAMESPACE);
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
