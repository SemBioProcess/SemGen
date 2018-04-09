package semsim.writing;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimObject;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.SemSimTypes;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.owl.SemSimOWLFactory;
import semsim.reading.AbstractRDFreader;
import semsim.reading.SemSimRDFreader;
import semsim.utilities.SemSimUtil;

public abstract class AbstractRDFwriter {
	protected SemSimModel semsimmodel;
	protected Map<PhysicalModelComponent, URI> PMCandResourceURImap = new HashMap<PhysicalModelComponent,URI>();
	protected Model rdf = ModelFactory.createDefaultModel();
	protected String xmlbase;
	protected Set<String> localids = new HashSet<String>();
	protected Map<URI, Resource> refURIsandresources = new HashMap<URI,Resource>();
	private Map<DataStructure, URI> variablesAndPropertyResourceURIs = new HashMap<DataStructure, URI>();



	AbstractRDFwriter(SemSimModel model) {
		semsimmodel = model;
	}

	// Abstract methods
	abstract protected void setRDFforModelLevelAnnotations();
	abstract protected void setSingularAnnotationForDataStructure(DataStructure ds, Resource ares);
	abstract protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares);
	abstract protected void setDataStructurePropertyOfAnnotation(DataStructure ds);		
	abstract protected void setReferenceOrCustomResourceAnnotations(PhysicalModelComponent pmc, Resource res);
	abstract protected void setRDFforSubmodelAnnotations(Submodel sub);
	abstract protected Property getPartOfPropertyForComposites();
	
	
	
	public void setXMLbase(String base){
		xmlbase = base;
	}

	
	// Set free text annotation
	public void setFreeTextAnnotationForObject(SemSimObject sso, Resource ares){

		// Set the free-text annotation
		if( sso.hasDescription()){
			Statement st = rdf.createStatement(ares, AbstractRDFreader.dcterms_description, sso.getDescription());
			addStatement(st);
			
			// If we're assigning free text to a FunctionalSubmodel that doesn't have a metadata ID,
			// make sure we add the metadata ID when we write out
			if( ! sso.hasMetadataID() && (sso instanceof FunctionalSubmodel)) 
				sso.setMetadataID(ares.getURI().replace("#", ""));
		}
	}
		
	
	// Add annotations for data structures
	protected void setRDFforDataStructureAnnotations(){
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			setRDFforDataStructureAnnotations(ds);
		}
	}
	
	
	
	protected void setRDFforDataStructureAnnotations(DataStructure ds){
		
		String metaid = (ds.hasMetadataID()) ? ds.getMetadataID() : semsimmodel.assignValidMetadataIDtoSemSimObject(ds.getName(), ds);
		String resuri = xmlbase + metaid;
		Resource ares = rdf.createResource(resuri);
		
		// Set free-text annotation
		setFreeTextAnnotationForObject(ds, ares);
		
		// If a singular reference annotation is present, write it out
		setSingularAnnotationForDataStructure(ds, ares);
		
		// Include the necessary composite annotation info
		setDataStructurePropertyAndPropertyOfAnnotations(ds, ares);
	}
	
	
	
	// Get the RDF resource for a data structure's associated physical property
	protected Resource getResourceForDataStructurePropertyAndAnnotate(Model rdf, DataStructure ds){
		
		if(variablesAndPropertyResourceURIs.containsKey(ds)){
			return rdf.getResource(variablesAndPropertyResourceURIs.get(ds).toString());
		}
		
		Resource res = createNewResourceForSemSimObject("property");
		variablesAndPropertyResourceURIs.put(ds, URI.create(res.getURI()));
		setReferenceOrCustomResourceAnnotations(ds.getPhysicalProperty(), res);
		return res;
	}
	
	
	// Add statements that assert the sources, sinks and mediators participating in a process as
	// well as their stoichiometry
	protected void setProcessParticipationRDFstatements(PhysicalProcess process, PhysicalEntity physent, Property relationship, Double multiplier){
		Resource processres = getResourceForPMCandAnnotate(rdf, process);
		
		// Create a new participant resource
		String type = null;
		
		if(relationship.getLocalName().equals(SemSimRelation.HAS_SOURCE_PARTICIPANT.getName())) type = "source";
		else if(relationship.getLocalName().equals(SemSimRelation.HAS_SINK_PARTICIPANT.getName())) type = "sink";
		else if(relationship.getLocalName().equals(SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getName())) type = "mediator";
		else return;
		
		Resource participantres = createNewResourceForSemSimObject(type);
		Statement partst = rdf.createStatement(processres, relationship, participantres);
		addStatement(partst);
		
		Resource physentrefres = null;
		
		// Create link between process participant and the physical entity it references
		if(physent.isType(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)){
			URI physentrefuri = setCompositePhysicalEntityMetadata((CompositePhysicalEntity)physent);
			physentrefres = rdf.getResource(physentrefuri.toString());
		}
		else physentrefres = getResourceForPMCandAnnotate(rdf, physent);
		
		if(physentrefres!=null){
			Statement st = rdf.createStatement(participantres, 
					SemSimRelation.HAS_PHYSICAL_ENTITY_REFERENCE.getRDFproperty(), 
					physentrefres);
			addStatement(st);
		}
		else System.err.println("Error in setting participants for process: null value for Resource corresponding to " + physent.getName());

		// Add multiplier info
		if( multiplier != null && ! relationship.getLocalName().equals(SemSimRelation.HAS_MEDIATOR_PARTICIPANT.getName())){
			Statement st = rdf.createStatement(participantres, 
					SemSimRelation.HAS_MULTIPLIER.getRDFproperty(), 
					multiplier.toString());
			
			addStatement(st);
		}
	}
	
	
	// Add statements that describe a composite physical entity in the model
	// Uses recursion to store all composite physical entities that make it up, too.
	protected URI setCompositePhysicalEntityMetadata(CompositePhysicalEntity cpe){
		
		// Get the Resource corresponding to the index entity of the composite entity
		// If we haven't added this composite entity before, log it
		if(cpe.equals(SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, PMCandResourceURImap))){
			PMCandResourceURImap.put(cpe, URI.create(getResourceForPMCandAnnotate(rdf, cpe).getURI()));
		}
		// Otherwise use the CPE already stored
		else cpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(cpe, PMCandResourceURImap);
		
		URI indexuri = PMCandResourceURImap.get(cpe);
		Resource indexresource = null;
		
		if(indexuri == null){
			indexresource = getResourceForPMCandAnnotate(rdf, cpe);
			indexuri = URI.create(indexresource.getURI());
		}
		else indexresource = rdf.getResource(indexuri.toString());
		
		PhysicalEntity indexent = cpe.getArrayListOfEntities().get(0);
		
		setReferenceOrCustomResourceAnnotations(indexent, indexresource);

		if (cpe.getArrayListOfEntities().size()==1) return indexuri;
		
		// Truncate the composite by one entity
		ArrayList<PhysicalEntity> nextents = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> nextrels = new ArrayList<StructuralRelation>();
		
		for(int u = 1; u<cpe.getArrayListOfEntities().size(); u++){
			nextents.add(cpe.getArrayListOfEntities().get(u));
		}
		for(int u = 1; u<cpe.getArrayListOfStructuralRelations().size(); u++){
			nextrels.add(cpe.getArrayListOfStructuralRelations().get(u));
		}
		
		CompositePhysicalEntity nextcpe = new CompositePhysicalEntity(nextents, nextrels);
		URI nexturi = null;
		
		// Add sub-composites recursively
		if(nextcpe.getArrayListOfEntities().size()>1){
			
			// If we haven't added this composite entity before, log it
			if(nextcpe == SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, PMCandResourceURImap)){
				PMCandResourceURImap.put(nextcpe, URI.create(getResourceForPMCandAnnotate(rdf, nextcpe).getURI()));
			}
			// Otherwise use the CPE already stored
			else nextcpe = SemSimUtil.getEquivalentCompositeEntityIfAlreadyInMap(nextcpe, PMCandResourceURImap);
			
			nexturi = setCompositePhysicalEntityMetadata(nextcpe);
		}
		// If we're at the end of the composite
		else {
			PhysicalEntity lastent = nextcpe.getArrayListOfEntities().get(0);
			
			// If it's an entity we haven't processed yet
			if(!PMCandResourceURImap.containsKey(nextcpe.getArrayListOfEntities().get(0))){
				nexturi = URI.create(getResourceForPMCandAnnotate(rdf, lastent).getURI());
				PMCandResourceURImap.put(lastent, nexturi);
			}
			// Otherwise get the terminal entity that we logged previously
			else nexturi = PMCandResourceURImap.get(lastent);
		}
		
		Property structprop = getPartOfPropertyForComposites();
		
		StructuralRelation rel = cpe.getArrayListOfStructuralRelations().get(0);
		if(rel==StructuralRelation.CONTAINED_IN) structprop = StructuralRelation.CONTAINED_IN.getRDFproperty();
		
		Statement structst = rdf.createStatement(indexresource, structprop, rdf.getResource(nexturi.toString()));
		
		addStatement(structst);
		
		return indexuri;
	}
		
		
		
	
	// Get the RDF resource for a physical model component (entity or process)
	protected Resource getResourceForPMCandAnnotate(Model rdf, PhysicalModelComponent pmc){
		
		String typeprefix = pmc.getComponentTypeasString();
		boolean isphysproperty = typeprefix.matches("property");
		
		if(PMCandResourceURImap.containsKey(pmc) && ! isphysproperty)
			return rdf.getResource(PMCandResourceURImap.get(pmc).toString());
		
		if (typeprefix.matches("submodel") || typeprefix.matches("dependency"))
			typeprefix = "unknown";
		
		Resource res = createNewResourceForSemSimObject(typeprefix);
				
		if(! isphysproperty) PMCandResourceURImap.put(pmc, URI.create(res.getURI()));
		
		setReferenceOrCustomResourceAnnotations(pmc, res);
		
		return res;
	}
		
	// Generate an RDF resource for a physical component
	protected Resource createNewResourceForSemSimObject(String typeprefix){
		
		//Use relative URIs
		String resname = xmlbase;	
		int idnum = 0;
		
		while(localids.contains(resname + typeprefix + "_" + idnum)){
			idnum++;
		}
		resname = resname + typeprefix + "_" + idnum;

		localids.add(resname);
		
		Resource res = rdf.createResource(resname);
		return res;
	}
	
	
	protected Resource findReferenceResourceFromURI(URI uri){
		Resource refres = null;
		
		if(refURIsandresources.containsKey(uri))
			refres = refURIsandresources.get(uri);
		else{
			URI furi = convertURItoIdentifiersDotOrgFormat(uri);
			refres = rdf.createResource(furi.toString());
			refURIsandresources.put(furi, refres);
		}
		return refres;
	}
	
	public static URI convertURItoIdentifiersDotOrgFormat(URI uri){
		URI newuri = uri;
		String namespace = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());

		// If we are looking at a URI that is NOT formatted according to identifiers.org
		if( ! uri.toString().startsWith("http://identifiers.org") 
				&& ReferenceOntologies.getReferenceOntologyByNamespace(namespace) != ReferenceOntology.UNKNOWN){
			
			ReferenceOntology refont = ReferenceOntologies.getReferenceOntologyByNamespace(namespace);
			String fragment = SemSimOWLFactory.getIRIfragment(uri.toString());
			String newnamespace = null;
			
			// Look up identifiers.org namespace
			for(String ns : refont.getNamespaces()){
				if(ns.startsWith("http://identifiers.org") && ! ns.startsWith("http://identifiers.org/obo.")){
					newnamespace = ns;
					break;
				}
			}

			// Replacement rules for specific knowledge bases
			if(refont==ReferenceOntology.UNIPROT){
				newuri = URI.create(newnamespace + fragment);
			}
			else if(refont==ReferenceOntology.OPB){
				newuri = URI.create(newnamespace + fragment);
			}
			else if(refont==ReferenceOntology.CHEBI){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			else if(refont==ReferenceOntology.GO){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			else if(refont==ReferenceOntology.CL){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			else if(refont==ReferenceOntology.FMA){
				// assumes that FMA IDs are formatted
				// like http://purl.org/sig/ont/fma/fma70586
				String newfragment = fragment.replace("fma","FMA:");
				newuri = URI.create(newnamespace + newfragment);
			}
			else if(refont==ReferenceOntology.MA){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			else if(refont==ReferenceOntology.PR){
				// how to replace correctly?
			}
		}
		return newuri;
	}
	
	
	
	public static String getRDFmodelAsString(Model rdf,String rdfxmlformat){
				// Use "RDF/XML-ABBREV" for rdfxmlformat parameter if writing a standalone model
				// Use "RDF/XML" for CASA files in archives
				RDFWriter writer = rdf.getWriter(rdfxmlformat);
				writer.setProperty("blockRules", "idAttr");
				writer.setProperty("relativeURIs","same-document,relative"); // this allows relative URIs
				StringWriter out = new StringWriter();
				writer.write(rdf, out, SemSimRDFreader.TEMP_NAMESPACE);
				String outstring = out.toString();
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return outstring;
	}
	
	
	protected void addStatement(Statement st){
		if( ! rdf.contains(st)) rdf.add(st);
	}
}
