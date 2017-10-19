package semsim.writing;

import java.io.File;
import java.net.URI;

import org.semanticweb.owlapi.model.OWLException;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;

public class CASAwriter extends AbstractRDFwriter{

	CASAwriter(SemSimModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setRDFforModelLevelAnnotations() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void setRDFforDataStructureAnnotations(DataStructure ds) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void setSingularAnnotationForDataStructure(DataStructure ds, Resource ares) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void setDataStructurePropertyAndPropertyOfAnnotations(DataStructure ds, Resource ares) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setDataStructurePropertyOfAnnotation(DataStructure ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setProcessParticipationRDFstatements(PhysicalProcess process, PhysicalEntity physent,
			Property relationship, Double multiplier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected URI setCompositePhysicalEntityMetadata(CompositePhysicalEntity cpe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setReferenceOrCustomResourceAnnotations(PhysicalModelComponent pmc, Resource res) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void writeToFile(File destination) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToFile(URI uri) throws OWLException {
		// TODO Auto-generated method stub
		
	}

}
