package semsim.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import semsim.SemSimObject;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
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

public class SemSimRDFreader extends ModelReader{

	private Map<String, PhysicalModelComponent> ResourceURIandPMCmap = new HashMap<String, PhysicalModelComponent>();
	public Model rdf = ModelFactory.createDefaultModel();
	private String unnamedstring = "[unnamed!]";
	public static Property dcterms_description = ResourceFactory.createProperty(RDFNamespace.DCTERMS + "description");



	public SemSimRDFreader(ModelAccessor accessor, SemSimModel semsimmodel, String rdfasstring, String baseNamespace) {
		super(accessor);
		
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
	}

	@Override
	public SemSimModel read() throws IOException, InterruptedException,
			OWLException, CloneNotSupportedException, XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	
	public void getRDFforAnnotatedSemSimObject(SemSimObject sso){
		
		String uristart = semsimmodel.getNamespace();
		String dsidentifier = sso.getName();
		Resource resource = rdf.getResource(uristart + dsidentifier);

		
		if(sso instanceof DataStructure){
			
			collectFreeTextAnnotation(sso, resource);
			collectSingularBiologicalAnnotation((DataStructure)sso, resource);
			collectCompositeAnnotation((DataStructure)sso, resource);
		}
//		else if(sso instanceof Submodel){
////			dsidentifier = 
//			semsimmodel
//			Resource resource = rdf.getResource(uristart + dsidentifier);
//			collectFreeTextAnnotation(sso, resource);
//		}
	}
	
	
	private SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource){	
		
		Resource physpropres = null;
		
		if(resource != null)
			physpropres = resource.getPropertyResourceValue(SemSimRelation.IS_COMPUTATIONAL_COMPONENT_FOR.getRDFproperty());
		
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
							process.addSource((PhysicalEntity) sinkpmc, multiplier.getDouble());
						}
						else process.addSource((PhysicalEntity) sinkpmc, 1.0);
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
		
		Statement singannst = resource.getProperty(SemSimRelation.BQB_IS.getRDFproperty());

		if(singannst != null){
			URI singularannURI = URI.create(singannst.getObject().asResource().getURI());
			ds.addReferenceOntologyAnnotation(SemSimRelation.HAS_PHYSICAL_DEFINITION, singularannURI, null, sslib);singannst.getObject().asResource();
		}
	}
	
	
	private void collectFreeTextAnnotation(SemSimObject sso, Resource resource){
		Statement st = resource.getProperty(dcterms_description);
		
		if(st != null) sso.setDescription(st.getObject().toString());

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
	
	
	// Replace the namespace of a URI with the OPB's preferred namespace
	public URI swapInOPBnamespace(URI uri){
		String frag = SemSimOWLFactory.getIRIfragment(uri.toString());
		String uristring = RDFNamespace.OPB.getNamespaceasString() + frag;
		return URI.create(uristring);
	}
	
	
	public static String getRDFmodelAsString(Model rdf){
		String syntax = "RDF/XML-ABBREV"; 
		StringWriter out = new StringWriter();
		rdf.write(out, syntax);
		return out.toString();
	}

}
