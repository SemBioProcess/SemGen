package semsim.reading;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Model;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import semsim.SemSimLibrary;
import semsim.annotation.Annotation;
import semsim.annotation.Person;
import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalEnergyDifferential;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.writing.AbstractRDFwriter;
import semsim.writing.SemSimRDFwriter;

/**
 * Class for working with annotations stored in Combine Archive
 * annotation (OMEX metadata) files. OMEX metadata files are standalone RDF
 * files in COMBINE archives that are used to house annotations on models 
 * within the archive.
 * @author mneal
 */

//TODO: Make this backwards-compatible with 1.0 spec. Could use myOMEX prefix as 1.1 indicator.

public class OMEXmetadataReader extends AbstractRDFreader{
	
	/**
	 * Constructor
	 * @param ma A {@link ModelAccessor} indicating the location of the OMEX metadata file
	 * @param semsimmodel The {@link SemSimModel} to annotate using the contents of
	 * the OMEX metadata file
	 * @param lib A {@link SemSimLibrary} instance
	 * @param rdfstring Additional RDF content to include in the model's metadata
	 * that is not contained in the OMEX metadata file (e.g. curatorial info in a CellML model.
	 */
	public OMEXmetadataReader(OMEXAccessor ma, SemSimModel semsimmodel, SemSimLibrary lib, String rdfstring) {
		super(ma, semsimmodel, lib);
		
		if(rdfstring!=null)
			readStringToRDFmodel(rdf, rdfstring, "");
		
		this.semsimmodel = semsimmodel;
		this.modelNamespaceIsSet = false;
		
		String archivefn = ma.getArchiveFileName();
		String modfn = ma.getFileName();
		String rdffn = ma.getOMEXmetadataFileName();

		setModelNamespaceInRDF(RDFNamespace.OMEX_LIBRARY.getNamespaceAsString() + archivefn + "/" + modfn);
		setLocalNamespaceInRDF(RDFNamespace.OMEX_LIBRARY.getNamespaceAsString() + archivefn + "/" + rdffn);	
	}

	/**
	 * Apply annotations on the physical components of an input
	 * SBML model (compartments, species, reactions) using 
	 * info in the OMEX metadata file.
	 * @param sbmlmodel The SBML model to annotate using the OMEX metadata file.
	 */
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

	
	/**
	 * Apply annotations for a physical component from an input SBML model
	 * (compartment, species, or reaction) using info in the OMEX metadata file. 
	 * @param sbaseobj The SBML object to annotate
	 */
	protected void getAnnotationsForPhysicalComponent(AbstractNamedSBase sbaseobj){
		
		// For reading in OMEX metadata-formatted annotations on SBML compartments, species, and reactions
		String metaid = sbaseobj.getMetaId(); // TODO: what if no metaid assigned? Just do nothing?
		String ns = getModelNamespaceInRDF();
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
		String modeluri = getModelNamespaceInRDF() + "#" + semsimmodel.getMetadataID();
		
		Resource modelresource = rdf.getResource(modeluri);
		
		// If there's no resource with the model URI in OMEX metadata spec 1.2 format,
		// see if there is one with 1.0 formatting (just model namespace) and use that instead.
		if ( ! rdf.contains( modelresource, null, (RDFNode) null )) {
			  modelresource = rdf.getResource(getModelNamespaceInRDF());
			}
		
		StmtIterator stmts = modelresource.listProperties();
		
		while(stmts.hasNext()){
			Statement stmt = stmts.next();
			Property prop = stmt.getPredicate();
			
			// If it's the model description, store it and move on to next annotation
			if(prop.getURI().equals(AbstractRDFreader.dcterms_description.getURI())){
				semsimmodel.setDescription(stmt.getObject().asLiteral().getString());
				continue;
			}
			// If it's a statement about a model creator or contributor, collect the info
			else if(prop.getURI().equals(AbstractRDFreader.dcterms_creator.getURI())) {
				Resource personres = stmt.getObject().asResource();
				Person creator = collectPersonInfo(personres);
				semsimmodel.addCreator(creator);
			}
			else if(prop.getURI().equals(AbstractRDFreader.dcterms_contributor.getURI())) {
				Resource personres = stmt.getObject().asResource();
				Person contributor = collectPersonInfo(personres);
				semsimmodel.addContributor(contributor);
			}
			else {
				// Collect model-level annotation that is not the description or a creator
				Relation rel = SemSimRelations.getRelationFromURI(URI.create(stmt.getPredicate().getURI()));
				RDFNode obj = stmt.getObject();
				
				if(obj.isResource() && rel!=null)
					semsimmodel.addReferenceOntologyAnnotation(rel, URI.create(obj.asResource().getURI()), "", sslib);
				else if(rel!=null)
					semsimmodel.addAnnotation(new Annotation(rel,obj.asLiteral().getString()));
			}
		}
	}
	
	/**
	 * Collect name, email, etc. for an RDF resource representing a person
	 * @param personres The RDF resource representing a person
	 * @return A {@link Person} object containing the person's info
	 */
	private Person collectPersonInfo(Resource personres) {
		Person creator = new Person();
		
		// If the object in the <creator> triple is a local resource
		if(personres.getURI().startsWith(localnamespaceinRDF)) {
			
			// Set name
			NodeIterator nameit = rdf.listObjectsOfProperty(personres, SemSimRelation.FOAF_NAME.getRDFproperty());
			
			if( nameit.hasNext() ) creator.setName(nameit.next().asLiteral().toString());
			
			// Set email
			NodeIterator emailit = rdf.listObjectsOfProperty(personres, SemSimRelation.FOAF_MBOX.getRDFproperty());
			
			if( emailit.hasNext() )	creator.setEmail(emailit.next().asResource().getURI().replace("mailto:", ""));
			
			// Set account name
			NodeIterator acctnameit = rdf.listObjectsOfProperty(personres, SemSimRelation.FOAF_ACCOUNT_NAME.getRDFproperty());
			
			if( acctnameit.hasNext() ) creator.setAccountName(URI.create(acctnameit.next().asResource().getURI()));
			
			// Set account service home page
			NodeIterator accthpit = rdf.listObjectsOfProperty(personres, SemSimRelation.FOAF_ACCOUNT_SERVICE_HOMEPAGE.getRDFproperty());
			
			if( accthpit.hasNext() ) creator.setAccountServiceHomepage(URI.create(accthpit.next().asResource().getURI()));
		}
		
		// Otherwise we assume that the object in the statement is an account name
		else {
			creator.setAccountName(URI.create(personres.getURI()));
		}
		
		return creator;
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
	/**
	 * Only called when reading in SBML models in OMEX archives
	 */
	public void getDataStructureAnnotations(DataStructure ds) {
		getDataStructureAnnotations(ds, getModelNamespaceInRDF());
	}
	
	@Override
	public void getDataStructureAnnotations(DataStructure ds, String ns){
		
		String metaid = ds.getMetadataID();
		//String ns = TEMP_BASE + semsimmodel.getLegacyCodeLocation().getFileName();
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
			if(! uri.toString().startsWith(RDFNamespace.OPB.getNamespaceAsString()))
				uri = swapInOPBnamespace(uri);
			
			PhysicalPropertyInComposite pp = getPhysicalPropertyInComposite(uri.toString());
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
				else if(pmc instanceof PhysicalEnergyDifferential){
					PhysicalEnergyDifferential force = (PhysicalEnergyDifferential)pmc;
					NodeIterator sourceit = rdf.listObjectsOfProperty(propertyofres, SemSimRelation.HAS_SOURCE_PARTICIPANT.getRDFproperty());
					
					// Read in the source participants
					while(sourceit.hasNext()){
						Resource sourceres = (Resource) sourceit.next();
						Resource physentres = sourceres.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty());
						PhysicalModelComponent sourcepmc = getPMCfromRDFresourceAndAnnotate(physentres);
						
						force.addSource((PhysicalEntity) sourcepmc);
					}
					// Read in the sink participants
					NodeIterator sinkit = rdf.listObjectsOfProperty(propertyofres, SemSimRelation.HAS_SINK_PARTICIPANT.getRDFproperty());
					while(sinkit.hasNext()){
						Resource sinkres = (Resource) sinkit.next();
						Resource physentres = sinkres.getPropertyResourceValue(SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty());
						PhysicalModelComponent sinkpmc = getPMCfromRDFresourceAndAnnotate(physentres);
						
						force.addSink((PhysicalEntity) sinkpmc);
					}
				}
			}
		}
		
		return semsimmodel;
	}
	
	
	@Override
	protected void getAllSemSimSubmodelAnnotations() {	
		//TODO:
	}
}
