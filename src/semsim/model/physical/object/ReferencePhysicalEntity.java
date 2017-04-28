package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.owl.SemSimOWLFactory;

public class ReferencePhysicalEntity extends PhysicalEntity implements ReferenceTerm{
	
	public ReferencePhysicalEntity(URI uri, String description){
		super(SemSimTypes.REFERENCE_PHYSICAL_ENTITY);
		referenceuri = uri;
		setName(description);
	}
	
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary lib){
		if(hasPhysicalDefinitionAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimRelation.HAS_PHYSICAL_DEFINITION, referenceuri, getDescription(), lib);
		}
		return null;
	}
	
	public URI getPhysicalDefinitionURI() {
		return URI.create(referenceuri.toString());
	}
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib) {
		return getName() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		URI physdefuri = ((ReferencePhysicalEntity)obj).getPhysicalDefinitionURI();
		
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
	public ReferencePhysicalEntity addToModel(SemSimModel model) {
		return model.addReferencePhysicalEntity(this);
		
	}
}
