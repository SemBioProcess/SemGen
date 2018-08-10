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
 * Class for representing the measurable or derivable characteristics that
 * physical entities, processes and dependencies can bear.
 * 
 * Analogous to 'OPB:Physics property':
 * A physics attribute that is a scalar, vector or tensor descriptor
 * of a physics continuant, physics processural entity or physics 
 * dependency that is observable and measureable by physical means or
 * derived by computations on such measures.
 * @author mneal
 *
 */
public class PhysicalProperty extends PhysicalModelComponent implements ReferenceTerm{
		
	public PhysicalProperty(String label, URI uri) {
		super(SemSimTypes.PHYSICAL_PROPERTY);
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
	
	/** @return The name of the knowledge base that contains the URI used 
	 * as the PhysicalProperty's annotation value */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib) {
		
		if( ! hasName()) 
			return getTermFragment() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
		
		return getName() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
	}
	
	@Override
	public String getComponentTypeAsString() {
		return "property";
	}


	@Override
	protected boolean isEquivalent(Object obj) {
		URI physdefuri = ((PhysicalProperty)obj).getPhysicalDefinitionURI();
		
		String physdefID = getTermFragment().replace(":", "_");
		String thisID = SemSimOWLFactory.getIRIfragment(physdefuri.toString()).replace(":", "_");
		
		return (ReferenceOntologies.URIsAreFromSameReferenceOntology(physdefuri, referenceuri) 
				&& physdefID.equals(thisID));
	}
	
	@Override
	public String getOntologyName(SemSimLibrary semsimlib) {
		return semsimlib.getReferenceOntologyName(referenceuri);
	}

	@Override
	public String getTermFragment() {
		return SemSimOWLFactory.getIRIfragment(referenceuri.toString());
	}

	@Override
	public PhysicalProperty addToModel(SemSimModel model) {
		return model.addPhysicalProperty(this);
		
	}
	
	@Override
	public void removeFromModel(SemSimModel model) {
		model.removePhysicalProperty(this);
	}
}
