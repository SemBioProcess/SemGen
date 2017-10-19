package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimObject;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.PhysicalProperty;
import semsim.reading.ModelClassifier.ModelType;


public abstract class AbstractRDFreader extends ModelReader{

	public Model rdf = ModelFactory.createDefaultModel();
	public static Property dcterms_description = ResourceFactory.createProperty(RDFNamespace.DCTERMS.getNamespaceasString(), "description");
	protected Map<String, PhysicalModelComponent> ResourceURIandPMCmap = new HashMap<String, PhysicalModelComponent>();
	protected ModelType modeltype;
	public static String TEMP_NAMESPACE = "http://tempns.net/temp";
	protected boolean modelNamespaceIsSet = true;
	
	
	AbstractRDFreader(File file) {
		super(file);
	}
	
	AbstractRDFreader(ModelAccessor accessor){
		super(accessor);
	}

	// Abstract methods
	abstract protected void getModelLevelAnnotations();
	abstract protected void collectSingularBiologicalAnnotation(DataStructure ds, Resource resource);
	abstract protected SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource);

	
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
	
}
