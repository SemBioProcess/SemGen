package semsim.reading;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;

/**
 * Class for reading RDF-formatted SemSim annotations as stored within XML-formatted 
 * modeling documents (CellML, SBML, or JSim project files).
 * @author mneal
 *
 */
public class SemSimRDFreader extends AbstractRDFreader{

	private Map<String, Submodel> submodelURIandObjectMap = new HashMap<String, Submodel>();
	protected ModelAccessor modelaccessor;

	
	/**
	 * Constructor
	 * @param accessor Location of file containing RDF-formatted annotations
	 * @param semsimmodel The SemSimModel in which to store the annotations
	 * @param rdfasstring Non-SemSim related metadata also stored in the RDF element
	 * (e.g. curatorial metadata stored in a CellML model)
	 * @param sslibrary A SemSimLibrary instance
	 */
	public SemSimRDFreader(ModelAccessor accessor, SemSimModel semsimmodel, String rdfasstring, SemSimLibrary sslibrary) {
		super(accessor, semsimmodel, sslibrary);
				
		if(rdfasstring!=null){
			readStringToRDFmodel(rdf, rdfasstring, TEMP_NAMESPACE);
			createSemSimSubmodelURIandObjectMap();
		}
		
		//If an explicit namespace is specified with the "model" prefix, use it
		semsimmodel.setNamespace(getModelNamespaceFromRDF());
	}

	
	/**
	 * Create a map that links the RDF URIs representing SemSim submodels to their
	 * corresponding {@link Submodel} objects in the SemSim model
	 */
	private void createSemSimSubmodelURIandObjectMap(){
		
		ResIterator subit = rdf.listSubjectsWithProperty(SemSimRelation.HAS_NAME.getRDFproperty());
		
		while(subit.hasNext()){
			Resource submodelres = subit.next();
						
			if(submodelres.getURI() != null){
				
				String metaid = submodelres.getLocalName();
				if(metaid.contains("submodel_")){
					Statement subnamest = submodelres.getProperty(SemSimRelation.HAS_NAME.getRDFproperty());
					String name = subnamest.getLiteral().toString();
					
					Submodel newsub = new Submodel(name);
					newsub.setMetadataID(metaid);
					submodelURIandObjectMap.put(submodelres.getURI(), newsub);
					semsimmodel.addSubmodel(newsub);					
				}
			}
		}
	}
	
	
	/** @return The namespace of the model used in the RDF-formatted SemSim model annotations */
	public String getModelNamespaceFromRDF(){
		String ns = rdf.getNsPrefixURI("model");
		
		if(ns == null ){
			modelNamespaceIsSet = false;
			ns = semsimmodel.generateNamespaceFromDateAndTime();
		}

		return ns;
	}
	
	// Get model-level annotations
	@Override
	public void getModelLevelAnnotations(){
		
		Resource modelres = null;
		
		switch(modeltype){
		
		// If a CellML model and namespace is set, use namespace, otherwise use temp namespace and metadata ID.
		// The former is to accommodate CellML models annotated using previous SG versions.
		case CELLML_MODEL: modelres = modelNamespaceIsSet ? rdf.getResource(semsimmodel.getNamespace() + semsimmodel.getMetadataID()) : 
			rdf.getResource(TEMP_NAMESPACE + "#" + semsimmodel.getMetadataID());
			 break;
		 
		// If an MML model in a JSim project file, use the model namespace (included in serialization) and 
		// data structure name b/c we don't use metadata ID's in MML files
		case MML_MODEL_IN_PROJ: modelres = rdf.getResource(semsimmodel.getNamespace() + semsimmodel.getName());
			 break;
		 
		 // If an SBML model, use the model namespace (included in serialization) and metadata ID
		case SBML_MODEL: modelres = rdf.getResource(semsimmodel.getNamespace() + semsimmodel.getMetadataID());
			 break;
		
		default: modelres = rdf.getResource(semsimmodel.getNamespace() + semsimmodel.getName());
			break;
				
		}
				
		StmtIterator stit = modelres.listProperties();
				
		while(stit.hasNext()){
			
			Statement st = stit.next();
			URI predicateURI = URI.create(st.getPredicate().getURI());
			
			if (predicateURI.equals(SemSimLibrary.SEMSIM_VERSION_IRI.toURI())) {
				semsimmodel.setSemSimVersion(st.getObject().asLiteral().toString());
			}
			
			else if (predicateURI.equals(SemSimModel.LEGACY_CODE_LOCATION_IRI.toURI())) {
				ModelAccessor ma = FileAccessorFactory.getModelAccessor(st.getObject().asLiteral().toString());
				semsimmodel.setSourceFileLocation(ma);
			}
			
			else{
				Metadata m = getMetadataByURI(predicateURI);
				String value = st.getObject().toString();
				semsimmodel.getCurationalMetadata().setAnnotationValue(m, value);
			}
		}
	}
	
	
	// Get the annotations on a submodel
	@Override
	protected void getAllSemSimSubmodelAnnotations(){
		
		for(String suburi : submodelURIandObjectMap.keySet()){
			
			Resource subres = rdf.getResource(suburi); // collect name
			
			Submodel sub = submodelURIandObjectMap.get(suburi);
			collectFreeTextAnnotation(sub, subres); // collect description
						
			// Collect associated data structures
			StmtIterator dsstit = subres.listProperties(SemSimRelation.HAS_ASSOCIATED_DATA_STRUCTURE.getRDFproperty());
			while(dsstit.hasNext()){
				Statement dsst = dsstit.next();
				Resource dsresinsub = dsst.getResource();
				String dsresinsubURI = dsresinsub.getURI();
				
				String metaid = dsresinsubURI.substring(dsresinsubURI.indexOf("#") + 1);
				
				SemSimObject sso = semsimmodel.getMetadataIDcomponentMap().get(metaid);
								
				if(sso instanceof DataStructure) sub.addDataStructure((DataStructure)sso);
			}
			
			// Collect submodels of submodels
			StmtIterator substit = subres.listProperties(SemSimRelation.INCLUDES_SUBMODEL.getRDFproperty());
			while(substit.hasNext()){
				Statement subst = substit.next();
				Resource subsubres = subst.getResource();
				
				String subsuburi = subsubres.getURI();
				
				if(submodelURIandObjectMap.containsKey(subsuburi))
					sub.addSubmodel(submodelURIandObjectMap.get(subsuburi));
				else{
					String metaid = subsuburi.substring(subsuburi.indexOf("#") + 1);
					SemSimObject ssc = semsimmodel.getModelComponentByMetadataID(metaid);
					
					if(ssc instanceof FunctionalSubmodel) sub.addSubmodel((FunctionalSubmodel)ssc);
				}
			}			
		}		
	}
	
	@Override
	protected SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource){	
		
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
				
				PhysicalPropertyInComposite pp = getPhysicalPropertyInComposite(uri.toString());
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
	
	
	
	// Collect the reference ontology term used to describe the model component
	@Override
	public void collectSingularBiologicalAnnotation(DataStructure ds, Resource resource){
		
		Statement singannst = resource.getProperty(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
		singannst = (singannst==null) ? resource.getProperty(SemSimRelation.BQB_IS.getRDFproperty()) : singannst; // Check for BQB_IS relation, too

		if(singannst != null){
			URI singularannURI = URI.create(singannst.getObject().asResource().getURI());
			PhysicalProperty prop = getSingularPhysicalProperty(singularannURI);
			ds.setSingularAnnotation(prop);
		}
	}
	
	
	/**
	 * Look up {@link Metadata} items in {@link CurationalMetadata} that use
	 * an input URI for their relation
	 * @param uri An input URI 
	 * @return The {@link Metadata} object that uses the input URI as its relation
	 */
	private Metadata getMetadataByURI(URI uri){
		
		for(Metadata m : Metadata.values()){
			if(m.getURI().equals(uri)){
				return m;
			}
		}
		
		return null;
	}
}
