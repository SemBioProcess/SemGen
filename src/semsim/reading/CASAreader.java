package semsim.reading;

import java.io.IOException;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.semanticweb.owlapi.model.OWLException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimLibrary;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.AbstractRDFwriter;

public class CASAreader extends AbstractRDFreader{

	CASAreader(ModelAccessor accessor,  SemSimModel model, String rdfstring, ModelType type, SemSimLibrary sslibrary) {
		super(accessor, model, sslibrary);
		
		if(rdfstring!=null)
			readStringToRDFmodel(rdf, rdfstring);
		
		this.modeltype = type;
		this.modelNamespaceIsSet = false;
	}


	protected void getAnnotationsForPhysicalComponent(AbstractNamedSBase sbaseobj){
		// For reading in CASA-formatted annotations on SBML compartments, species, and reactions
		
		String metaid = sbaseobj.getMetaId(); // TODO: what if no metaid assigned? Just do nothing?
		Resource res = rdf.getResource(TEMP_NAMESPACE + "#" + metaid);
		
		Qualifier[] qualifiers = Qualifier.values();
		
		for(int i=0;i<qualifiers.length;i++){
			
			Qualifier q = qualifiers[i];
			
			if(q.isBiologicalQualifier()){ // Only collect biological qualifiers
				NodeIterator nodeit = rdf.listObjectsOfProperty(res, SemSimRelations.getRelationFromBiologicalQualifier(q).getRDFproperty());
				
				while(nodeit.hasNext()){
					RDFNode nextnode = nodeit.next();
					Resource objres = nextnode.asResource();
					CVTerm cvterm = new CVTerm();
					cvterm.setQualifier(q);
					String uriasstring = AbstractRDFwriter.convertURItoIdentifiersDotOrgFormat(URI.create(objres.getURI())).toString();
					cvterm.addResourceURI(uriasstring);
					sbaseobj.addCVTerm(cvterm);
				}
			}
		}
	}


	@Override
	public void getModelLevelAnnotations() {
		// TODO Auto-generated method stub
		
	}
	
	
	// Collect the reference ontology term used to describe the model component
	public void collectSingularBiologicalAnnotation(DataStructure ds, Resource resource){
		
		Statement singannst = resource.getProperty(SemSimRelation.HAS_PHYSICAL_DEFINITION.getRDFproperty());
		singannst = (singannst==null) ? resource.getProperty(SemSimRelation.BQB_IS.getRDFproperty()) : singannst; // Check for BQB_IS relation, too

		if(singannst != null){
			URI singularannURI = URI.create(singannst.getObject().asResource().getURI());
			PhysicalProperty prop = getSingularPhysicalProperty(singularannURI);
			ds.setSingularAnnotation(prop);
		}
	}
		
	


	@Override
	protected SemSimModel collectCompositeAnnotation(DataStructure ds, Resource resource) {

		Resource physpropres = null;
		
		if(resource != null)
			physpropres = resource.getPropertyResourceValue(SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty());
		
		// If a physical property is specified for the data structure
		if(physpropres != null){		
						
			URI uri = URI.create(physpropres.getURI());

			// If an identifiers.org OPB namespace was used, replace it with the OPB's
			if(! uri.toString().startsWith(RDFNamespace.OPB.getNamespaceasString()))
				uri = swapInOPBnamespace(uri);
			
			PhysicalPropertyinComposite pp = getPhysicalPropertyInComposite(uri.toString());
			ds.setAssociatedPhysicalProperty(pp);
			
			getPMCfromRDFresourceAndAnnotate(physpropres);
			
			Resource propertyofres = resource.getPropertyResourceValue(SemSimRelation.BQB_IS_PROPERTY_OF.getRDFproperty());
			
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
	
	
	@Override
	protected void getAllSemSimSubmodelAnnotations() {		
	}

}
