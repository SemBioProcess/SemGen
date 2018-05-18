package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalDependency;
import semsim.owl.SemSimOWLFactory;

/**
 * Class for representing physical dependencies that are defined using
 * controlled knowledge resource terms.
 * @author mneal
 *
 */
public class ReferencePhysicalDependency extends PhysicalDependency implements ReferenceTerm {

	public ReferencePhysicalDependency(URI uri, String description){
		super(SemSimTypes.REFERENCE_PHYSICAL_DEPENDENCY);
		referenceuri = uri;
		setName(description);
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
	public String getOntologyName(SemSimLibrary semsimlib) {
		return semsimlib.getReferenceOntologyName(referenceuri);
	}

	@Override
	public String getTermID() {
		return SemSimOWLFactory.getIRIfragment(referenceuri.toString());
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		URI physdefuri = ((ReferencePhysicalDependency)obj).getPhysicalDefinitionURI();
		
		String physdefID = getTermID().replace(":", "_");
		String thisID = SemSimOWLFactory.getIRIfragment(physdefuri.toString()).replace(":", "_");
		
		return (ReferenceOntologies.URIsAreFromSameReferenceOntology(physdefuri, referenceuri) 
				&& physdefID.equals(thisID));	}

	@Override
	public ReferencePhysicalDependency addToModel(SemSimModel model) {
		return model.addReferencePhysicalDependency(this);
	}
	
	@Override
	public void removeFromModel(SemSimModel model) {
		model.removeReferencePhysicalDependency(this);
	}
}
