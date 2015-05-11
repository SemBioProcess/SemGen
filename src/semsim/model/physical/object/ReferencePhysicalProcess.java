package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.physical.PhysicalProcess;

public class ReferencePhysicalProcess extends PhysicalProcess{
	
	public ReferencePhysicalProcess(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
		setName(description);
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.REFERENCE_PHYSICAL_PROCESS_CLASS_URI;
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((ReferencePhysicalProcess)obj).getReferstoURI().compareTo(referenceuri)==0;
	}
}
