package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;

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
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEnergyDifferential;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.owl.SemSimOWLFactory;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.AbstractRDFwriter;

/**
 * Class for working with SemSim annotations that are serialized
 * in RDF format
 * @author mneal
 *
 */
public abstract class AbstractRDFreader {

	public Model rdf = ModelFactory.createDefaultModel();
	public static Property dcterms_description = ResourceFactory.createProperty(RDFNamespace.DCTERMS.getNamespaceAsString(), "description");
	public static Property dcterms_creator = ResourceFactory.createProperty(RDFNamespace.DCTERMS.getNamespaceAsString(), "creator");

	protected Map<String, PhysicalModelComponent> ResourceURIandPMCmap = new HashMap<String, PhysicalModelComponent>();
	protected ModelType modeltype;
	public static String TEMP_BASE = "http://tempns.net/";
	protected String unnamedstring = "[unnamed!]";
	protected boolean modelNamespaceIsSet = true;
	protected SemSimLibrary sslib;
	protected SemSimModel semsimmodel;
	protected ModelAccessor modelaccessor;
	protected String modelnamespaceinRDF = "";
	protected String localnamespaceinRDF = "";
	
	
	public AbstractRDFreader(ModelAccessor accessor,  SemSimModel model, SemSimLibrary sslibrary){
		semsimmodel = model;
		modelaccessor = accessor;
		sslib = sslibrary;
		modeltype = accessor.getModelType();
	}

	//Abstract methods
	abstract protected void getModelLevelAnnotations();
	abstract protected void collectSingularBiologicalAnnotation(DataStructure ds, Resource resource);
	abstract protected SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource);
	abstract protected void getAllSemSimSubmodelAnnotations();
	
	/**
	 * Read a String into an RDF model
	 * @param rdf The RDF model object that will contain any statements in the input string
	 * @param rdfasstring RDF-formatted text to read into the model
	 * @param namespace Namespace of the RDF model
	 */
	public static void readStringToRDFmodel(Model rdf, String rdfasstring, String namespace){
		
		try {
			
			try (InputStream stream = new ByteArrayInputStream(rdfasstring.getBytes("UTF-8"))) {
		        RDFParser.create()
		            .source(stream)
		            //.resolveURIs(false) // should not be doing this. Need a temp namespace.
		            .lang(RDFLanguages.RDFXML)
		            //.errorHandler(ErrorHandlerFactory.errorHandlerWarning(null)) // change this
		            .base(TEMP_BASE) // Used if relative URIs encountered
		            .parse(rdf);
				stream.close();
		    }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Collect all annotations, use default namespace
	 */
	public void getAllDataStructureAnnotations() {
		String ns = AbstractRDFreader.TEMP_BASE + semsimmodel.getLegacyCodeLocation().getFileName();
		getAllDataStructureAnnotations(ns);
	}
	
	/** Collect all annotations on {@link DataStructure}s */
	public void getAllDataStructureAnnotations(String ns){
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			getDataStructureAnnotations(ds, ns);
		}
	}
	
	/** Collect annotations on {@link DataStructure}s, use default namespace*/
	public void getDataStructureAnnotations(DataStructure ds) {
		String ns = AbstractRDFreader.TEMP_BASE + semsimmodel.getLegacyCodeLocation().getFileName();
		getDataStructureAnnotations(ds, ns);
	}
	
	
	/**
	 * Get the annotations on an input {@link DataStructure}
	 * @param ds The input {@link DataStructure}
	 */
	public void getDataStructureAnnotations(DataStructure ds, String ns){
		
		// If we're reading a CellML model use a temp namespace
		// otherwise use the namespace that was specified and assigned to the SemSim model
		Resource resource = null;
				
		switch(modeltype){
		
			// If a CellML model and namespace is set, use namespace, otherwise use temp namespace and metadata ID.
			// The former is to accommodate CellML models annotated using previous SG versions.
			case CELLML_MODEL: resource = modelNamespaceIsSet ? rdf.getResource(semsimmodel.getNamespace() + ds.getName()) : 
				rdf.getResource(TEMP_BASE + "#" + ds.getMetadataID());
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

	
	/**
	 * Collect the free text annotation (stored using the 
	 * Dublin Core Terms:description property) on a SemSimObject
	 * @param sso The SemSimObject 
	 * @param resource The resource in the RDF model corresponding to the
	 * SemSimObject
	 */
	public void collectFreeTextAnnotation(SemSimObject sso, Resource resource){
		Statement st = resource.getProperty(dcterms_description);		
		if(st != null)
			sso.setDescription(st.getObject().toString());
		
	}

		
	/**
	 * Add a physical property reference term to the {@link SemSimModel}. If the
	 * property was already added, retrieve it.
	 * @param uri Reference URI of the physical property 
	 * @return The {@link PhysicalProperty} instance added to the model
	 */
	public PhysicalProperty getSingularPhysicalProperty(URI uri){
		PhysicalModelComponent term = ResourceURIandPMCmap.get(uri.toString());
		
		if (term==null) {
			term = new PhysicalProperty("", uri);
			ResourceURIandPMCmap.put(uri.toString(), term);
			semsimmodel.addPhysicalProperty((PhysicalProperty) term);
		}
		return (PhysicalProperty)term;
	}
	
	
	/**
	 * Determine whether a {@link DataStructure} has an associated
	 * physical property annotation. This is only used when reading 
	 * RDF-formatted SemSim annotations contained within fSBML models,
	 * currently.
	 * @param ds A {@link DataStructure}
	 * @return Whether  {@link DataStructure} has an associated
	 * physical property annotation.
	 */
	protected boolean hasPropertyAnnotationForDataStructure(DataStructure ds){
		//Only used for reading RDF in SBML models currently, so we look up by metadata ID
		return rdf.contains(rdf.getResource(semsimmodel.getNamespace() + ds.getMetadataID()), 
				SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty());
	}
	
	
	/**
	 * Add a {@link PhysicalPropertyInComposite} object to a {@link SemSimModel}, or
	 * retrieve it if already added. Uses the input URI as the reference URI for the
	 * property. 
	 * @param resourceuri The URI of the reference physical property
	 * @return The {@link PhysicalPropertyInComposite} object corresponding to the
	 * input URI
	 */
	protected PhysicalPropertyInComposite getPhysicalPropertyInComposite(String resourceuri) {
		PhysicalModelComponent term = ResourceURIandPMCmap.get(resourceuri);
		if (term==null) {
			term = new PhysicalPropertyInComposite("", URI.create(resourceuri));
			ResourceURIandPMCmap.put(resourceuri, term);
			semsimmodel.addPhysicalPropertyForComposite((PhysicalPropertyInComposite) term);
		}
		return (PhysicalPropertyInComposite)term;
	}
	
	
	/**
	 * Get the SemSim physical model component corresponding to an input RDF
	 * resource. If a mapping exists between the resource and the component,
	 * the mapped component is returned. Otherwise, a new physical model component
	 * is created in the SemSim model and added to the resource-component map.
	 * @param res An RDF resource in the SemSim-formatted RDF content associated
	 * with the model.
	 * @return The {@link PhysicalModelComponent} mapped to the resource
	 */
	protected PhysicalModelComponent getPMCfromRDFresourceAndAnnotate(Resource res){
		// Find the Physical Model Component corresponding to the resource's URI
		// Instantiate, if not present
		
		PhysicalModelComponent pmc = null;
		if(ResourceURIandPMCmap.containsKey(res.getURI()))
			pmc = ResourceURIandPMCmap.get(res.getURI());
		
		else{
			Resource isannres = res.getPropertyResourceValue(SemSimRelation.BQB_IS.getRDFproperty());
			if(isannres==null) isannres = res.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
			
			boolean isproperty = res.getLocalName().startsWith("OPB_");
			boolean isprocess = res.getLocalName().startsWith("process_");
			boolean isforce = res.getLocalName().startsWith("force_");
			boolean isentity = res.getLocalName().startsWith("entity_") || (isannres==null && ! isproperty && ! isprocess && ! isforce); // Assume that if there's no identity annotation, it's a reference URI to a physical entity

			
			// If a physical entity
			if(isentity){
				
				// If a composite entity
				if(res.getPropertyResourceValue(StructuralRelation.CONTAINED_IN.getRDFproperty())!=null || 
						res.getPropertyResourceValue(StructuralRelation.PART_OF.getRDFproperty())!=null ||
						res.getPropertyResourceValue(StructuralRelation.BQB_IS_PART_OF.getRDFproperty())!=null
						) {
					pmc = semsimmodel.addCompositePhysicalEntity(buildCompositePhysicalEntityfromRDFresource(res));
				}
				// If a singular entity
				else {
					ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
					entlist.add(createCompositeEntityComponentFromResourceAndAnnotate(res));
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
			else if(isforce){
				pmc = semsimmodel.addCustomPhysicalForce(new CustomPhysicalEnergyDifferential());
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
	
	
	/**
	 * Create a {@link CompositePhysicalEntity} object by traversing the structural relations
	 * statements on the first physical entity of the composite.
	 * @param firstentity RDF resource representing the first physical entity in the 
	 * composite physical entity (AKA the "index" entity).
	 * @return A {@link CompositePhysicalEntity} object that incorporates the mereotopological
	 * graph associated with the input RDF resource
	 */
	protected CompositePhysicalEntity buildCompositePhysicalEntityfromRDFresource(Resource firstentity){
		Resource curres = firstentity;
		
		ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
		PhysicalEntity startent = createCompositeEntityComponentFromResourceAndAnnotate(firstentity);
		entlist.add(startent); // index physical entity
		
		//TODO: maybe problem is that we need to read in BQB:PARTOF?

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
				PhysicalEntity nextent = createCompositeEntityComponentFromResourceAndAnnotate(entityres);
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
	
	
	/**
	 * Create a physical entity component of a {@link CompositePhysicalEntity}, add
	 * it to the model, and if it is a custom entity, add its annotations.
	 * @param res An RDF resource corresponding to a physical entity
	 * @return A {@link PhysicalEntity} object created within the SemSim model
	 * that corresponds to the input RDF resource
	 */
	private PhysicalEntity createCompositeEntityComponentFromResourceAndAnnotate(Resource res){	
		Resource isannres = res.getPropertyResourceValue(SemSimRelation.BQB_IS.getRDFproperty());
		
		if(isannres==null) 
			isannres = res.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
		
		// If a reference entity
		// Create a singular physical entity from a component in a composite physical entity
		PhysicalEntity returnent = null;
		
		// If it's a physical entity with an identity statement on it...
		if(isannres!=null)
			 returnent = semsimmodel.addReferencePhysicalEntity(
					 new ReferencePhysicalEntity(
							 URI.create(isannres.getURI()), isannres.getURI()));
		
		// Else if it's a custom physical entity...
		else if( res.getNameSpace().startsWith(getModelNamespaceInRDF())
				|| res.getNameSpace().startsWith(getLocalNamespaceInRDF()))
			returnent = addCustomPhysicalEntityToModel(res);
		
		// Else it's a URI for a reference knowledge resource (concise format used in OMEX metadata)
		else
			 returnent = semsimmodel.addReferencePhysicalEntity(
					 new ReferencePhysicalEntity(
							 URI.create(res.getURI()), res.getURI()));
		
		return returnent;
	}
	
	
	/**
	 * Add a {@link CustomPhysicalEntity} to the SemSim model that corresponds 
	 * to an input RDF resource. Also includes any semantic annotations on the
	 * entity.
	 * @param res An RDF resource
	 * @return The {@link CustomPhysicalEntity} added to the model
	 */
	private CustomPhysicalEntity addCustomPhysicalEntityToModel(Resource res){
		
		StmtIterator isversionofann = res.listProperties(SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty());
		//StmtIterator partofann = res.listProperties(StructuralRelation.PART_OF.getRDFproperty());
		StmtIterator haspartann = res.listProperties(StructuralRelation.HAS_PART.getRDFproperty());
		StmtIterator bqbhaspartann = res.listProperties(StructuralRelation.BQB_HAS_PART.getRDFproperty());

		
		// Collect all annotations on custom term
		Set<Statement> allannstatements = new HashSet<Statement>();
		allannstatements.addAll(isversionofann.toSet());
		//allannstatements.addAll(partofann.toSet());
		allannstatements.addAll(haspartann.toSet());
		allannstatements.addAll(bqbhaspartann.toSet());
		
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
		
		returnent.setMetadataID(res.getLocalName());
		
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

	
	/**
	 * Remove all SemSim annotation statements from an RDF block. Used to separate 
	 * CellML-specific RDF annotations (curational metadata, e.g.) from SemSim
	 * annotations.
	 * @param rdf The RDF model containing the SemSim annotations
	 * @param thesemsimmodel The SemSim model associated with the RDF model
	 * @return The RDF model stripped of SemSim statements
	 */
	public static Model stripSemSimRelatedContentFromRDFblock(Model rdf, SemSimModel thesemsimmodel){
		
		// Currently getting rid of anything with semsim predicates
		// and descriptions on variables and components
		// AND
		// * part-of statements for physical entities (prbly need to assign metaids to physical entities)
		// * isVersionOf statements on custom terms
		// * has-part statements on custom terms
		// Test to make sure we're not preserving extraneous stuff in CellMLRDFmarkup block
		// within SemSim models (test with all kinds of anns)
		// MAYBE THE RIGHT WAY TO DO THIS IS TO USE THE METAIDS/??
		
		Iterator<Statement> stit = rdf.listStatements();
		List<Statement> listofremovedstatements = new ArrayList<Statement>();
		
		// Go through all statements in RDF
		while(stit.hasNext()){
			Statement st = (Statement) stit.next();
			String rdfprop = st.getPredicate().getURI();
			
			// Flag any statement that uses a predicate with a semsim namespace for removal
			if(rdfprop.startsWith(RDFNamespace.SEMSIM.getNamespaceAsString())
					|| rdfprop.startsWith("http://www.bhi.washington.edu/SemSim#") // accommodate models with older SemSim namespace 
					|| rdfprop.equals(StructuralRelation.PART_OF.getURIasString())
					|| rdfprop.equals(StructuralRelation.HAS_PART.getURIasString())
					|| rdfprop.equals(SemSimRelation.BQB_IS_VERSION_OF.getURIasString())
					|| rdfprop.equals(StructuralRelation.BQB_HAS_PART.getURIasString())  // Adding in the BQB structural relations here for good measure, even though we're not currently using them
					|| rdfprop.equals(StructuralRelation.BQB_IS_PART_OF.getURIasString())){
				listofremovedstatements.add(st);
				continue;
			}
			
			Resource subject = st.getSubject();
			
			if(subject.getURI() != null){
				
				if(subject.getURI().contains(SemSimRDFreader.TEMP_BASE + "#")){
					
					// Look up the SemSimObject associated with the URI fragment (should be the metaid of the RDF Subject)
					SemSimObject sso = thesemsimmodel.getModelComponentByMetadataID(subject.getLocalName());
					
					if (sso!=null){
						
						// Remove dc:description statements (do not need to preserve these)
						if((sso instanceof DataStructure || sso instanceof Submodel || sso instanceof PhysicalEntity || sso instanceof PhysicalProcess)
								&& rdfprop.equals(AbstractRDFreader.dcterms_description.getURI()))
							listofremovedstatements.add(st);
					}
				}
			}
			
		}
		
		rdf.remove(listofremovedstatements);
		
		return rdf;
	}
	
	
	/**
	 * Replace the namespace of a URI with the OPB's namespace
	 * @param uri An input URI
	 * @return The URI with the OPB's namespace in place of the
	 * input URI's namespace
	 */
	public URI swapInOPBnamespace(URI uri){
		String frag = SemSimOWLFactory.getIRIfragment(uri.toString());
		String uristring = RDFNamespace.OPB.getNamespaceAsString() + frag;
		
		return URI.create(uristring);
	}
	
	
	/**
	 * Get a String representation of an RDF model
	 * @param rdf An input RDF model
	 * @param rdfxmlformat The format to use when writing out the String.
	 * For example, "RDF/XML-ABBREV".
	 * @return The String representation of the RDF model
	 */
	public static String getRDFmodelAsString(Model rdf, String rdfxmlformat){
		return AbstractRDFwriter.getRDFmodelAsString(rdf, rdfxmlformat);
	}
	

	/**
	 * @return The namespace for RDF resources that do not refer to explicit
	 * elements in the annotated model
	 */
	public String getLocalNamespaceInRDF() {
		return this.localnamespaceinRDF;
	}
	
	
	/**
	 * @return The namespace for RDF resources that refer to explicit
	 * elements in the annotated model
	 */
	public String getModelNamespaceInRDF() {
		return this.modelnamespaceinRDF;
	}
	
	
	/**
	 * Set the namespace for resources that do not refer to explicit 
	 * elements in the annotated model
	 * @param ns
	 */
	public void setLocalNamespaceInRDF(String ns) {
		this.localnamespaceinRDF = ns;
	}
	
	
	/**
	 * Set the namespace for resources that refer to explicit 
	 * elements in the annotated model
	 * @param ns
	 */
	public void setModelNamespaceInRDF(String ns) {
		this.modelnamespaceinRDF = ns;
	}
	
	
	
}
