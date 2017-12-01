package semsim.reading;

import java.io.ByteArrayInputStream;
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
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import semsim.SemSimLibrary;
import semsim.SemSimObject;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.reading.ModelClassifier.ModelType;


public abstract class AbstractRDFreader {

	public Model rdf = ModelFactory.createDefaultModel();
	public static Property dcterms_description = ResourceFactory.createProperty(RDFNamespace.DCTERMS.getNamespaceasString(), "description");
	protected Map<String, PhysicalModelComponent> ResourceURIandPMCmap = new HashMap<String, PhysicalModelComponent>();
	protected ModelType modeltype;
	public static String TEMP_NAMESPACE = "http://tempns.net/temp";
	protected String unnamedstring = "[unnamed!]";
	protected boolean modelNamespaceIsSet = true;
	protected SemSimLibrary sslib;
	protected SemSimModel semsimmodel;
	protected ModelAccessor modelaccessor;
	
	
	AbstractRDFreader(ModelAccessor accessor,  SemSimModel model, SemSimLibrary sslibrary){
		semsimmodel = model;
		modelaccessor = accessor;
		sslib = sslibrary;
	}

	// Abstract methods
	abstract protected void getModelLevelAnnotations();
	abstract protected void collectSingularBiologicalAnnotation(DataStructure ds, Resource resource);
	abstract protected SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource);
	abstract protected void getAllSemSimSubmodelAnnotations();
	
	// Read a string into an RDF model
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
			 
			 // If an SBML model, use the model namespace (included in serialization) and metadata ID
			case SBML_MODEL: resource = rdf.getResource(semsimmodel.getNamespace() + ds.getMetadataID());
				 break;
			
			default: resource = rdf.getResource(semsimmodel.getNamespace() + ds.getName());
				break;
		}
				
		collectFreeTextAnnotation(ds, resource);
		collectSingularBiologicalAnnotation(ds, resource);
		collectCompositeAnnotation(ds, resource);
	}

	
	public void collectFreeTextAnnotation(SemSimObject sso, Resource resource){
		Statement st = resource.getProperty(dcterms_description);		
		if(st != null)
			sso.setDescription(st.getObject().toString());
		
	}

		
	// Assuming that this will work the same for SemSimRDFreader and CASA reader
	public PhysicalProperty getSingularPhysicalProperty(URI uri){
		PhysicalModelComponent term = ResourceURIandPMCmap.get(uri.toString());
		
		if (term==null) {
			term = new PhysicalProperty("", uri);
			ResourceURIandPMCmap.put(uri.toString(), term);
			semsimmodel.addPhysicalProperty((PhysicalProperty) term);
		}
		return (PhysicalProperty)term;
	}
	
	
	public boolean hasPropertyAnnotationForDataStructure(DataStructure ds){
		//Only used for reading RDF in SBML models currently, so we look up by metadata ID
		return rdf.contains(rdf.getResource(semsimmodel.getNamespace() + ds.getMetadataID()), 
				SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty());
	}
	
	
	protected PhysicalPropertyinComposite getPhysicalPropertyInComposite(String key) {
		PhysicalModelComponent term = ResourceURIandPMCmap.get(key);
		if (term==null) {
			String description = "";
			term = new PhysicalPropertyinComposite(description, URI.create(key));
			ResourceURIandPMCmap.put(key, term);
			semsimmodel.addAssociatePhysicalProperty((PhysicalPropertyinComposite) term);
		}
		return (PhysicalPropertyinComposite)term;
	}
	
	
	protected PhysicalModelComponent getPMCfromRDFresourceAndAnnotate(Resource res){
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
						res.getPropertyResourceValue(StructuralRelation.PART_OF.getRDFproperty())!=null ||
						res.getPropertyResourceValue(StructuralRelation.BQB_IS_PART_OF.getRDFproperty())!=null
						)
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
	
	
	protected CompositePhysicalEntity buildCompositePhysicalEntityfromRDFresource(Resource propertyofres){
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
			if(entityres==null){
				entityres = curres.getPropertyResourceValue(StructuralRelation.BQB_IS_PART_OF.getRDFproperty());
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
	
	
	
	protected CustomPhysicalEntity addCustomPhysicalEntityToModel(Resource res){
		
		StmtIterator isversionofann = res.listProperties(SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty());
		//StmtIterator partofann = res.listProperties(StructuralRelation.PART_OF.getRDFproperty());
		StmtIterator haspartann = res.listProperties(StructuralRelation.HAS_PART.getRDFproperty());
		
		// Collect all annotations on custom term
		Set<Statement> allannstatements = new HashSet<Statement>();
		allannstatements.addAll(isversionofann.toSet());
		//allannstatements.addAll(partofann.toSet());
		allannstatements.addAll(haspartann.toSet());
		
		// Collect name
		String name = unnamedstring;
		
		if(res.hasProperty(SemSimRelation.HAS_NAME.getRDFproperty()))
			name = res.getProperty(SemSimRelation.HAS_NAME.getRDFproperty()).getObject().toString();
		
		// Collect description
		String description = null;
		
		if(res.hasProperty(dcterms_description))
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


	
	// Replace the namespace of a URI with the OPB's preferred namespace
	public URI swapInOPBnamespace(URI uri){
		String frag = SemSimOWLFactory.getIRIfragment(uri.toString());
		String uristring = RDFNamespace.OPB.getNamespaceasString() + frag;
		
		return URI.create(uristring);
	}
	
}
