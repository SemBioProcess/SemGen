package semsim.writing;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;

public class PhysioMapWriter{
	
	public static Set<PhysicalModelComponent> pmcswithproperties;
	public static Hashtable<PhysicalModelComponent, Resource> pmcsandresources;
	
	public String write(SemSimModel model, String modelns){
		
		pmcsandresources = new Hashtable<PhysicalModelComponent,Resource>();
		pmcswithproperties = new HashSet<PhysicalModelComponent>();
		
		Model rdf = ModelFactory.createDefaultModel();
		
		modelns = model.getNamespace();
		
		// Collect physical model components with properties
		for(DataStructure ds : model.getDataStructures()){
			if(!ds.isImportedViaSubmodel()){
				if(!pmcswithproperties.contains(ds.getPhysicalProperty().getPhysicalPropertyOf())){
					if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null){
						pmcswithproperties.add(ds.getPhysicalProperty().getPhysicalPropertyOf());
					}
				}
			}
		}
		
		// Assign physical model components to unique RDF resources
		for(PhysicalModelComponent pmc : pmcswithproperties){
			Resource pmcres = null;
			URI rscuri = null;
			if(pmcsandresources.containsKey(pmc))
				pmcres = pmcsandresources.get(pmc);
			else{
				if(pmc.hasRefersToAnnotation())
					rscuri = pmc.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI();
				else
					try {
						rscuri = URI.create(modelns + URLEncoder.encode(pmc.getName(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				
				pmcres = rdf.createResource(rscuri.toString());
				pmcsandresources.put(pmc, pmcres);
			}
		}
		Property hassource = ResourceFactory.createProperty(SemSimConstants.HAS_SOURCE_URI.toString());
		Property hassink = ResourceFactory.createProperty(SemSimConstants.HAS_SINK_URI.toString());
		Property hasmediator = ResourceFactory.createProperty(SemSimConstants.HAS_MEDIATOR_URI.toString());
		
		Property physicalpropertyof = ResourceFactory.createProperty(SemSimConstants.PHYSICAL_PROPERTY_OF_URI.toString());
		
		// Set source, sink, mediator and composite entity info
		for(PhysicalModelComponent pmc : pmcsandresources.keySet()){
			Resource pmcrsc = pmcsandresources.get(pmc);
			
			if(pmc instanceof PhysicalProperty){
				PhysicalProperty prop = (PhysicalProperty)pmc;
				if(pmcsandresources.containsKey(prop.getPhysicalPropertyOf())){
					System.out.println("here2");
					Resource propof = pmcsandresources.get(prop.getPhysicalPropertyOf());
					rdf.createStatement(pmcrsc, physicalpropertyof, propof);
				}
			}
			
			if(pmc instanceof PhysicalProcess){
				for(PhysicalEntity ent : ((PhysicalProcess) pmc).getSourcePhysicalEntities()){
					Resource sourcersc = pmcsandresources.get(ent);
					pmcrsc.addProperty(hassource, sourcersc);
				}
				for(PhysicalEntity ent : ((PhysicalProcess) pmc).getSinkPhysicalEntities()){
					Resource sinkrsc = pmcsandresources.get(ent);
					pmcrsc.addProperty(hassink, sinkrsc);
				}
				for(PhysicalEntity ent : ((PhysicalProcess) pmc).getMediatorPhysicalEntities()){
					Resource medrsc = pmcsandresources.get(ent);
					pmcrsc.addProperty(hasmediator, medrsc);
				}
			}
		}
		
		String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE" and "TURTLE"
		StringWriter out = new StringWriter();
		rdf.write(out, syntax);
		return out.toString();
	}
}
