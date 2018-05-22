package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalModelComponent;
import semsim.owl.SemSimOWLFactory;

/**
 * Class for representing the physical property component of a composite
 * annotation. The SemSim group recommends only using OPB physical property
 * terms when annotating instances of this class.
 * @author mneal
 */
public class PhysicalPropertyInComposite extends PhysicalModelComponent implements ReferenceTerm{
		
	public PhysicalPropertyInComposite(String label, URI uri) {
		super(SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE);
		referenceuri = uri;
		setName(label);
	}

	@Override
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary lib){
		if(hasPhysicalDefinitionAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimRelation.HAS_PHYSICAL_DEFINITION, referenceuri, getDescription(), lib);
		}
		return null;
	}
	
	@Override
	public URI getPhysicalDefinitionURI() {
		return URI.create(referenceuri.toString());
	}
	
	@Override
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib) {
		return getName() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
	}
	
	@Override
	public String getComponentTypeAsString() {
		return "property";
	}

	@Override
	protected boolean isEquivalent(Object obj) {
		URI physdefuri = ((PhysicalPropertyInComposite)obj).getPhysicalDefinitionURI();
		
		String physdefID = getTermID().replace(":", "_");
		String thisID = SemSimOWLFactory.getIRIfragment(physdefuri.toString()).replace(":", "_");
		
		return (ReferenceOntologies.URIsAreFromSameReferenceOntology(physdefuri, referenceuri) 
				&& physdefID.equals(thisID));
	}
	
	@Override
	public String getOntologyName(SemSimLibrary semsimlib) {
		return semsimlib.getReferenceOntologyName(referenceuri);
	}

	@Override
	public String getTermID() {
		return SemSimOWLFactory.getIRIfragment(referenceuri.toString());
	}


	@Override
	public PhysicalPropertyInComposite addToModel(SemSimModel model) {
		return model.addPhysicalPropertyForComposite(this);
	}
	
	@Override
	public void removeFromModel(SemSimModel model) {
		model.removeAssociatePhysicalProperty(this);
	}
}
