package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.semanticweb.owlapi.model.OWLException;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import semsim.SemSimObject;
import semsim.definitions.SemSimRelations;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.object.PhysicalProperty;
import semsim.writing.AbstractRDFwriter;

public class CASAreader extends AbstractRDFreader{

	CASAreader(File file, String rdfstring) {
		super(file);
		
		if(rdfstring!=null){ readStringToRDFmodel(rdf, rdfstring);}
	}


	protected void getAnnotationsForSBMLphysicalComponents(AbstractNamedSBase sbaseobj){
		// For reading in CASA-formatted annotations on SBML compartments, species, and reactions
		
		String metaid = sbaseobj.getMetaId(); // TODO: what if no metaid assigned? Just do nothing?
		Resource res = rdf.getResource(TEMP_NAMESPACE + "#" + metaid);
		
		Qualifier[] qualifiers = Qualifier.values();
		
		for(int i=0;i<qualifiers.length;i++){
			
			Qualifier q = qualifiers[i];
			
			if(q.isBiologicalQualifier()){ // Only collect biological qualifiers
				NodeIterator nodeit = rdf.listObjectsOfProperty(res, SemSimRelations.getBiologicalQualifierRelation(q).getRDFproperty());
				
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
	public void getDataStructureAnnotations(DataStructure ds) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void getModelLevelAnnotations() {
		// TODO Auto-generated method stub
		
	}

	
	public void collectFreeTextAnnotation(SemSimObject sso, Resource resource){
		Statement st = resource.getProperty(dcterms_description);		
		if(st != null)
			sso.setDescription(st.getObject().toString());
		
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
		// For reading in composite annotations on CellML variables and SBML parameters
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	public SemSimModel read()
			throws IOException, InterruptedException, OWLException, CloneNotSupportedException, XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
