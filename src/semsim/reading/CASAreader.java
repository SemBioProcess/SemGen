package semsim.reading;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Model;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimLibrary;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.writing.AbstractRDFwriter;
import semsim.writing.SemSimRDFwriter;

public class CASAreader extends AbstractRDFreader{

	public CASAreader(ModelAccessor ma, SemSimModel semsimmodel, SemSimLibrary lib, String rdfstring) {
		super(ma, semsimmodel, lib);
		
		if(rdfstring!=null)
			readStringToRDFmodel(rdf, rdfstring, "");
		
		this.semsimmodel = semsimmodel;
		this.modelNamespaceIsSet = false;
	}

	public void getAnnotationsForPhysicalComponents(Model sbmlmodel) {
		List<AbstractNamedSBase> annotablestuff = new ArrayList<AbstractNamedSBase>();
		annotablestuff.addAll(sbmlmodel.getListOfCompartments());
		annotablestuff.addAll(sbmlmodel.getListOfSpecies());
		annotablestuff.addAll(sbmlmodel.getListOfReactions());
		annotablestuff.addAll(sbmlmodel.getListOfParameters());
		
		for(AbstractNamedSBase ansbase : annotablestuff){
			for(int cv=0; cv<ansbase.getCVTermCount(); cv++) ansbase.removeCVTerm(cv); // Strip existing CV terms that were in SBML code 
			
			getAnnotationsForPhysicalComponent(ansbase);
		}
	}

	
	protected void getAnnotationsForPhysicalComponent(AbstractNamedSBase sbaseobj){
		
		// For reading in CASA-formatted annotations on SBML compartments, species, and reactions
		String metaid = sbaseobj.getMetaId(); // TODO: what if no metaid assigned? Just do nothing?
		String ns = semsimmodel.getLegacyCodeLocation().getFileName();
		Resource res = rdf.getResource(ns + "#" + metaid);
		Qualifier[] qualifiers = Qualifier.values();
				
		for(int i=0;i<qualifiers.length;i++){
			
			Qualifier q = qualifiers[i];
			
			if(q.isBiologicalQualifier()){ // Only collect biological qualifiers
				Relation relation = SemSimRelations.getRelationFromBiologicalQualifier(q);
								
				if(relation != null){
					NodeIterator nodeit = rdf.listObjectsOfProperty(res, relation.getRDFproperty());
					
					while(nodeit.hasNext()){

						RDFNode nextnode = nodeit.next();
						Resource objres = nextnode.asResource();
						CVTerm cvterm = new CVTerm();
						cvterm.setQualifier(q);
						
						if(objres.getURI().contains(ns + "#")) break; // If the annotation is the start of a composite physical entity, break while loop

						String uriasstring = AbstractRDFwriter.convertURItoIdentifiersDotOrgFormat(URI.create(objres.getURI())).toString();
						cvterm.addResourceURI(uriasstring);
						sbaseobj.addCVTerm(cvterm);
					}
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
			
			// If we're reading an annotated CellML model and there is an output variable 
			// annotated against OPB:Time, set it as a solution domain
			if(ds instanceof MappableVariable){
								
				if( ( ((MappableVariable)ds).getPublicInterfaceValue().equals("out") 
						|| ((MappableVariable)ds).getPublicInterfaceValue().equals("") )
						&& prop.getPhysicalDefinitionURI().equals(SemSimRDFwriter.convertURItoIdentifiersDotOrgFormat(SemSimLibrary.OPB_TIME_URI))){
					
					ds.setIsSolutionDomain(true);
				}
			}
		}
	}
	
	
	@Override
	public void getDataStructureAnnotations(DataStructure ds){
		
		String metaid = ds.getMetadataID(); // TODO: what if no metaid assigned? Just do nothing?
		String ns = semsimmodel.getLegacyCodeLocation().getFileName();
		Resource resource = rdf.getResource(ns + "#" + metaid);
						
		collectFreeTextAnnotation(ds, resource);
		collectSingularBiologicalAnnotation(ds, resource);
		collectCompositeAnnotation(ds, resource);
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
