package semsim.writing;

import java.net.URI;
import java.util.Set;

import org.sbml.jsbml.CVTerm.Qualifier;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.reading.AbstractRDFreader;

public class CASAwriter extends AbstractRDFwriter{

	public CASAwriter(SemSimModel model) {
		super(model);	
		initialize(model);
	}

	protected void initialize(SemSimModel model) {
		// Add namespaces here
		rdf.setNsPrefix("bqbiol", RDFNamespace.BQB.getNamespaceasString());
		rdf.setNsPrefix("dcterms", RDFNamespace.DCTERMS.getNamespaceasString());
		rdf.setNsPrefix("semsim", RDFNamespace.SEMSIM.getNamespaceasString());
	}
	
	@Override
	protected void setRDFforModelLevelAnnotations() {
		//TODO:
	}
	
	
	public void setAnnotationsForPhysicalComponent(PhysicalModelComponent pmc){
		
		String metaid = pmc.getMetadataID(); // TODO: what if no metaid assigned?
		Resource res = rdf.createResource(xmlbase + metaid);
		
		Set<Annotation> anns = pmc.getAnnotations();
		
		// If it's a composite physical entity, write out the composite
		if(pmc instanceof CompositePhysicalEntity){
			URI enturi = setCompositePhysicalEntityMetadata((CompositePhysicalEntity)pmc);
			Statement st = rdf.createStatement(res, SemSimRelation.BQB_IS.getRDFproperty(), rdf.createResource(enturi.toString()));
			addStatement(st);
		}
		
		// If it's a singular physical entity, write out the singular annotation(s)
		else{

			// If the physical component has a physical identity annotation (is a ReferenceTerm), write the identity annotation
			if(pmc instanceof ReferenceTerm){
				Resource objres = rdf.createResource(((ReferenceTerm)pmc).getPhysicalDefinitionURI().toString());
				Statement st = rdf.createStatement(res, SemSimRelation.BQB_IS.getRDFproperty(), objres);
				addStatement(st);
			}
			
			// Otherwise it's a custom physical component. Store any non-identity annotations on it.
			else{
				for(Annotation ann : anns){
					
					if(ann instanceof ReferenceOntologyAnnotation){
						
						ReferenceOntologyAnnotation roa = (ReferenceOntologyAnnotation)ann;
						Property rdfprop = roa.getRelation().getRDFproperty();						
						Qualifier q = SemSimRelations.getBiologicalQualifierFromRelation(roa.getRelation());
						
						if (q!=null) {
							
							if(q.isBiologicalQualifier()){ // Only collect biological qualifiers
								
								ResourceFactory.createProperty(q.toString());
								Resource objres = rdf.createResource(roa.getReferenceURI().toString());
								Statement st = rdf.createStatement(res, rdfprop, objres);
								addStatement(st);
							}
						}
					}
				}
			}
		}
	}
	
	
	
	@Override
	protected void setReferenceOrCustomResourceAnnotations(PhysicalModelComponent pmc, Resource res){
		Resource refres = null;
		
		// If it's a reference resource
		if(pmc instanceof ReferenceTerm){
			
			URI uri = ((ReferenceTerm)pmc).getPhysicalDefinitionURI();
			refres = findReferenceResourceFromURI(uri);
			
			Statement annagainstst = rdf.createStatement(
					res, 
					SemSimRelation.BQB_IS.getRDFproperty(), 
					refres);
				
			// If we have a reference resource and the annotation statement hasn't already 
			// been added to the RDF block, add it
			if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
		}
		
		// If it's a custom resource
		else{

			for(Annotation ann : pmc.getAnnotations()){
				// If the physical model component has either an "is" or "is version of" annotation, 
				// add the annotation statement to the RDF block
								
				if(ann instanceof ReferenceOntologyAnnotation){	
					
					ReferenceOntologyAnnotation roa = (ReferenceOntologyAnnotation)ann;
					refres = findReferenceResourceFromURI(roa.getReferenceURI());
					Relation relation = roa.getRelation();
					
					// Add the annotations on the custom term					
					if(relation.equals(SemSimRelation.BQB_IS_VERSION_OF)
							|| relation.equals(StructuralRelation.HAS_PART)
							|| relation.equals(StructuralRelation.BQB_HAS_PART)){
//							|| relation.equals(StructuralRelation.PART_OF)){ // can't distinguish between part_of in composite statements and annotations on custom term

						if(relation.equals(StructuralRelation.HAS_PART)) relation = StructuralRelation.BQB_HAS_PART;
						
						Property refprop = relation.getRDFproperty();
						Statement annagainstst = rdf.createStatement(res, refprop, refres);
						
						// If we have a reference resource and the annotation statement hasn't already 
						// been added to the RDF block, add it
						if(refres!=null && !rdf.contains(annagainstst)) rdf.add(annagainstst);
					}
				}
			}
			
			// If it is a custom entity or process, store the name and description
			if((pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_PROCESS)) || (pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_ENTITY))){
				
				if(pmc.getName()!=null){
					Statement namest = rdf.createStatement(res, 
							SemSimRelation.HAS_NAME.getRDFproperty(), pmc.getName());
					
					if(!rdf.contains(namest)) rdf.add(namest);
				}
				
				if(pmc.hasDescription()){
					Statement descst = rdf.createStatement(res, 
							AbstractRDFreader.dcterms_description, pmc.getDescription());
					
					addStatement(descst);
				}
			}
		}
	}
	
	
	// Add singular annotation
	@Override
	protected void setSingularAnnotationForDataStructure(DataStructure ds, Resource ares){
		
		if(ds.hasPhysicalDefinitionAnnotation()){
			URI uri = ds.getPhysicalDefinitionURI();
			Property isprop = ResourceFactory.createProperty(SemSimRelation.BQB_IS.getURIasString());
			URI furi = convertURItoIdentifiersDotOrgFormat(uri);
			Resource refres = rdf.createResource(furi.toString());
			Statement st = rdf.createStatement(ares, isprop, refres);
			
			addStatement(st);
		}
		
	}
	
	
	@Override
	protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares) {
		if(ds.hasPhysicalProperty()){
			Property iccfprop = SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty();
			Resource propres = rdf.getResource(ds.getPhysicalProperty().getPhysicalDefinitionURI().toString());
			Statement st = rdf.createStatement(ares, iccfprop, propres);
			
			addStatement(st);
			
			setDataStructurePropertyOfAnnotation((DataStructure)ds, ares);
		}		
	}
	

	
	protected void setDataStructurePropertyOfAnnotation(DataStructure ds, Resource ares){		
		// Collect physical model components with properties
		if( ! ds.isImportedViaSubmodel()){
			
			if(ds.hasPhysicalProperty() && ds.hasAssociatedPhysicalComponent()){

				PhysicalModelComponent propof = ds.getAssociatedPhysicalModelComponent();
				
				// If the variable is a property of an entity
				if(propof instanceof PhysicalEntity){
					CompositePhysicalEntity cpe = (CompositePhysicalEntity)propof;
					
					if (cpe.getArrayListOfEntities().size()>1) {
						// Get the Resource corresponding to the index entity of the composite entity
						URI indexuri = setCompositePhysicalEntityMetadata(cpe);
						Resource indexresource = rdf.getResource(indexuri.toString());
						Statement propofst = rdf.createStatement(
								ares, 
								SemSimRelation.BQB_IS_PROPERTY_OF.getRDFproperty(), 
								indexresource);
						
						addStatement(propofst);
					}
					// else it's a singular physical entity
					else{
						Resource entity = getResourceForPMCandAnnotate(rdf, cpe.getArrayListOfEntities().get(0));
						Statement st = rdf.createStatement(
								ares, 
								SemSimRelation.BQB_IS_PROPERTY_OF.getRDFproperty(), 
								entity);
						
						addStatement(st);
					}
				}
				// Otherwise it's a property of a process
				else{
					PhysicalProcess process = (PhysicalProcess)ds.getAssociatedPhysicalModelComponent();

					Resource processres = getResourceForPMCandAnnotate(rdf, ds.getAssociatedPhysicalModelComponent());
					Statement st = rdf.createStatement(
							ares, 
							SemSimRelation.BQB_IS_PROPERTY_OF.getRDFproperty(), 
							processres);
					
					addStatement(st);
					
					// If the participants for the process have already been set, do not duplicate
					// statements (in CellML models mapped codewords may be annotated against the
					// same process, and because each process participant is created anew here, duplicate
					// participant statements would appear in CellML RDF block).
					
					if(rdf.contains(processres, SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty())
							|| rdf.contains(processres, SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty())
							|| rdf.contains(processres, SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty()))
						return;
					
					// If we're here, the process hasn't been assigned its participants yet
					
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


	@Override
	protected void setRDFforSubmodelAnnotations(Submodel sub) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setDataStructurePropertyOfAnnotation(DataStructure ds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Property getPartOfPropertyForComposites(){
		return StructuralRelation.BQB_IS_PART_OF.getRDFproperty();
	}
	
	
}
