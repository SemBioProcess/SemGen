package semsim.writing;

import java.net.URI;
import java.util.Set;

import org.sbml.jsbml.CVTerm.Qualifier;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

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
import semsim.model.physical.PhysicalEnergyDifferential;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.reading.AbstractRDFreader;

/**
 * Class for serializing RDF-formatted SemSim annotations in 
 * COMBINE archive OMEX metadata files.
 * @author mneal
 *
 */
public class OMEXmetadataWriter extends AbstractRDFwriter{

	public OMEXmetadataWriter(SemSimModel model) {
		super(model);	
		initialize(model);
	}

	
	/**
	 * Sets some namespace prefixes for the serialized RDF
	 * @param model An annotated SemSim model
	 */
	protected void initialize(SemSimModel model) {
		// Add namespaces here
		rdf.setNsPrefix("bqbiol", RDFNamespace.BQB.getNamespaceAsString());
		rdf.setNsPrefix("bqmodel", RDFNamespace.BQM.getNamespaceAsString());
		rdf.setNsPrefix("dcterms", RDFNamespace.DCTERMS.getNamespaceAsString());
		rdf.setNsPrefix("semsim", RDFNamespace.SEMSIM.getNamespaceAsString());
	}
	
	@Override
	public void setRDFforModelLevelAnnotations() {
		
//		String metaid = semsimmodel.hasMetadataID() ? semsimmodel.getMetadataID() : 
//			semsimmodel.assignValidMetadataIDtoSemSimObject("metaid0", semsimmodel);
		Resource modelresource = rdf.createResource(modelnamespaceinRDF);

		// Save model description
		if(semsimmodel.hasDescription()){
			Property prop = rdf.createProperty(AbstractRDFreader.dcterms_description.getURI());
			Statement st = rdf.createStatement(modelresource, prop, semsimmodel.getDescription());
			addStatement(st);
		}
		
		
		// Add the other annotations
		for(Annotation ann : semsimmodel.getAnnotations()){
			
			Statement st;
			
			Relation annrel = ann.getRelation();
			
			// Convert to BioModels.net qualifiers for use in OMEX metadata
			if(annrel==StructuralRelation.HAS_PART)
				annrel = StructuralRelation.BQB_HAS_PART;
			else if(annrel==StructuralRelation.PART_OF)
				annrel = StructuralRelation.BQB_IS_PART_OF;
			
			if(ann instanceof ReferenceOntologyAnnotation){
				ReferenceOntologyAnnotation refann = (ReferenceOntologyAnnotation)ann;
				st = rdf.createStatement(modelresource, annrel.getRDFproperty(), 
						rdf.createResource(refann.getReferenceURI().toString()));
			}
			//TODO: Need to decide how to handle annotations already in RDF block. 
			// Skip them all for now (some are read into SemSim object model and will
			// be preserved in OMEX metadata file)
			else if(annrel==SemSimRelation.CELLML_RDF_MARKUP) continue;
			else st = rdf.createStatement(modelresource, 
					annrel.getRDFproperty(), 
					ann.getValue().toString());
			
			if(st!=null) addStatement(st);
		}
	}
	
	//TODO: getting duplicate has_part statements for process Synthesis2 in BIOMD51
	/**
	 * Used to write out annotations for SBML elements that represent physical 
	 * components (compartments, species, and reactions).
	 * @param pmc An annotated physical model component
	 */
	protected void setAnnotationsForSBMLphysicalComponent(PhysicalModelComponent pmc){
		setAnnotationsForSBMLphysicalComponent(pmc.getMetadataID(), pmc);
	}
	
	/**
	 * Used to write out annotations for SBML elements that represent physical 
	 * components (compartments, species, and reactions).
	 * @param metaid Metadata ID to use in the URI for the resource in RDF
	 * @param pmc An annotated physical model component
	 */
	protected void setAnnotationsForSBMLphysicalComponent(String metaid, PhysicalModelComponent pmc){
		setAnnotationsForSBMLphysicalComponent(modelnamespaceinRDF, metaid, pmc);
	}
	
	/**
	 * Used to write out annotations for SBML elements that represent physical 
	 * components (compartments, species, and reactions).
	 * @param namespace The RDF namespace to use in the URI for the resource
	 * representing the physical component
	 * @param metaid Metadata ID to use in the URI for the resource in RDF
	 * @param pmc An annotated physical model component
	 */
	protected void setAnnotationsForSBMLphysicalComponent(String namespace, String metaid, PhysicalModelComponent pmc) {
		// TODO: what if no metaid assigned?
		Resource res = rdf.createResource(namespace + "#" + metaid);
		
		Set<Annotation> anns = pmc.getAnnotations();
		
		// If it's a composite physical entity, write out the composite
		if(pmc instanceof CompositePhysicalEntity){
			URI enturi = setCompositePhysicalEntityMetadata((CompositePhysicalEntity)pmc);
			Statement st = rdf.createStatement(res, SemSimRelation.BQB_IS.getRDFproperty(), 
					rdf.createResource(enturi.toString()));
			addStatement(st);
		}
		
		// If it's a singular physical component, write out the singular annotation(s)
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
						if(roa.getRelation()==StructuralRelation.HAS_PART)
							rdfprop = StructuralRelation.BQB_HAS_PART.getRDFproperty();
						else if(roa.getRelation()==StructuralRelation.PART_OF)
							rdfprop = StructuralRelation.BQB_IS_PART_OF.getRDFproperty();
						
						Qualifier q = SemSimRelations.getBiologicalQualifierFromRelation(roa.getRelation());
						
						if (q!=null) {
							
							if(q.isBiologicalQualifier()){ // Only collect biological qualifiers
								
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
			if(refres!=null) addStatement(annagainstst);
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
						if(refres!=null) addStatement(annagainstst);
					}
				}
			}
			
			// If it is a custom entity or process, store the name and description
			if((pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_PROCESS)) || (pmc.isType(SemSimTypes.CUSTOM_PHYSICAL_ENTITY))){
				
				if(pmc.getName()!=null){
					Statement namest = rdf.createStatement(res, 
							SemSimRelation.HAS_NAME.getRDFproperty(), pmc.getName());
					
					addStatement(namest);
				}
				
				if(pmc.hasDescription()){
					Statement descst = rdf.createStatement(res, 
							AbstractRDFreader.dcterms_description, pmc.getDescription());
					
					addStatement(descst);
				}
			}
		}
	}
	

	
	@Override
	protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares) {
		
		if(ds.hasPhysicalProperty()){
			Property iccfprop = SemSimRelation.BQB_IS_VERSION_OF.getRDFproperty();
			URI physpropuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
			physpropuri = convertURItoIdentifiersDotOrgFormat(physpropuri);
			Resource propres = rdf.getResource(physpropuri.toString());
			Statement st = rdf.createStatement(ares, iccfprop, propres);
			
			addStatement(st);
			
			setDataStructurePropertyOfAnnotation((DataStructure)ds, ares);
		}		
	}
	

	/**
	 * Write out RDF statements about a data structure's associated physical component.
	 * That is, the physical component that bears the physical property used in the 
	 * data structure's composite annotation.
	 * @param ds The annotated data structure
	 * @param ares RDF Resource representing the data structure
	 */
	protected void setDataStructurePropertyOfAnnotation(DataStructure ds, Resource ares){		
		
		// Collect physical model components with properties
		if( ! ds.isImportedViaSubmodel()){
			
			if(ds.hasPhysicalProperty() && ds.hasAssociatedPhysicalComponent()){

				PhysicalModelComponent propof = ds.getAssociatedPhysicalModelComponent();
				
				// If the variable is a property of an entity
				if(propof instanceof PhysicalEntity){
					CompositePhysicalEntity cpe = (CompositePhysicalEntity)propof;
					
					// If there is more than one physical entity in the composite physical entity...
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
						PhysicalEntity pe = cpe.getArrayListOfEntities().get(0);
						Resource entity = getResourceForPMCandAnnotate(pe);
						
						Statement st = rdf.createStatement(ares, 
								SemSimRelation.BQB_IS_PROPERTY_OF.getRDFproperty(), 
								entity);
						
						addStatement(st);
					}
				}
				// Of it's a property of a process
				else if(propof instanceof PhysicalProcess){
					PhysicalProcess process = (PhysicalProcess)ds.getAssociatedPhysicalModelComponent();

					Resource processres = getResourceForPMCandAnnotate(ds.getAssociatedPhysicalModelComponent());
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
						setRDFstatementsForEntityParticipation(process, source, 
								SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty(), process.getSourceStoichiometry(source));
					}
					// Set the sinks
					for(PhysicalEntity sink : process.getSinkPhysicalEntities()){
						setRDFstatementsForEntityParticipation(process, sink,
								SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty(), process.getSinkStoichiometry(sink));
					}
					// Set the mediators
					for(PhysicalEntity mediator : process.getMediatorPhysicalEntities()){
						setRDFstatementsForEntityParticipation(process, mediator,
								SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty(), null);
					}
				}
				else{  // Otherwise we assume it's a property of a physical force
					PhysicalEnergyDifferential force = (PhysicalEnergyDifferential)ds.getAssociatedPhysicalModelComponent();

					Resource forcres = getResourceForPMCandAnnotate(ds.getAssociatedPhysicalModelComponent());
					Statement st = rdf.createStatement(ares, SemSimRelation.BQB_IS_PROPERTY_OF.getRDFproperty(), forcres);
					
					addStatement(st);
					
					// If the participants for the process have already been set, do not duplicate
					// statements (in CellML models mapped codewords may be annotated against the
					// same process, and because each process participant is created anew here, duplicate
					// participant statements would appear in CellML RDF block).
					
					if(rdf.contains(forcres, SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty())
							|| rdf.contains(forcres, SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty())
							|| rdf.contains(forcres, SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getRDFproperty()))
						return;
					
					// If we're here, the process hasn't been assigned its participants yet
					
					// Set the sources
					for(PhysicalEntity source : force.getSources()){
						setRDFstatementsForEntityParticipation(force, source, 
								SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty(), null);
					}
					// Set the sinks
					for(PhysicalEntity sink : force.getSinks()){
						setRDFstatementsForEntityParticipation(force, sink,
								SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty(), null);
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
