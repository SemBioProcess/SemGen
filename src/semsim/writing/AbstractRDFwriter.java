package semsim.writing;

import java.io.StringWriter;
import java.net.URI;
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
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.owl.SemSimOWLFactory;
import semsim.reading.AbstractRDFreader;
import semsim.reading.SemSimRDFreader;

public abstract class AbstractRDFwriter {
	protected SemSimModel semsimmodel;
	protected Map<PhysicalModelComponent, URI> PMCandResourceURImap = new HashMap<PhysicalModelComponent,URI>();
	protected Model rdf = ModelFactory.createDefaultModel();
	protected String xmlbase;
	protected Set<String> localids = new HashSet<String>();
	protected Map<URI, Resource> refURIsandresources = new HashMap<URI,Resource>();


	AbstractRDFwriter(SemSimModel model) {
		semsimmodel = model;
		// TODO Auto-generated constructor stub
	}

	// Abstract methods
	abstract protected void setRDFforModelLevelAnnotations();
	abstract protected void setRDFforDataStructureAnnotations(DataStructure ds);
	abstract protected void setSingularAnnotationForDataStructure(DataStructure ds, Resource ares);
	abstract protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares);
	abstract protected void setDataStructurePropertyOfAnnotation(DataStructure ds);		
	abstract protected void setProcessParticipationRDFstatements(PhysicalProcess process, PhysicalEntity physent, Property relationship, Double multiplier);
	abstract protected URI setCompositePhysicalEntityMetadata(CompositePhysicalEntity cpe);
	abstract protected void setReferenceOrCustomResourceAnnotations(PhysicalModelComponent pmc, Resource res);

	
	// Add annotations for data structures
	protected void setRDFforDataStructureAnnotations(){
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			setRDFforDataStructureAnnotations(ds);
		}
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
			if(refont==ReferenceOntology.OPB){
				newuri = URI.create(newnamespace + fragment);
			}
			if(refont==ReferenceOntology.CHEBI){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.GO){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.CL){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.FMA){
				// assumes that FMA IDs are formatted
				// like http://purl.org/sig/ont/fma/fma70586
				String newfragment = fragment.replace("fma","FMA:");
				newuri = URI.create(newnamespace + newfragment);
			}
			if(refont==ReferenceOntology.MA){
				String newfragment = fragment.replace("_", ":");
				newuri = URI.create(newnamespace + newfragment);
			}
		}
		return newuri;
	}
	
	
	public static String getRDFmodelAsString(Model rdf){
		
		// TODO: if just use RDF/XML then get node refs and not pretty
		// If use -ABBREV, get rdf:IDs
		RDFWriter writer = rdf.getWriter("RDF/XML-ABBREV");
		writer.setProperty("blockRules", "idAttr");
		writer.setProperty("relativeURIs","same-document,relative"); // this allows relative URIs
		StringWriter out = new StringWriter();
		writer.write(rdf, out, SemSimRDFreader.TEMP_NAMESPACE);
		
		return out.toString();
	}
	
	protected void addStatement(Statement st){
		if( ! rdf.contains(st)) rdf.add(st);
	}
}
