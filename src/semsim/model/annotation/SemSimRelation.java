package semsim.model.annotation;

import java.net.URI;

import semsim.model.SemSimComponent;

/** SemSimRelations describe the relationship that an annotated SemSimComponent
 * has with an annotation value. Examples include SemSimConstants.REFERS_TO_RELATION
 * and SemSimConstants.PART_OF_RELATION */
public class SemSimRelation extends SemSimComponent{	
	private URI uri;
	
	/** Class constructor (generally you'd want to use the relations in SemSimConstants,
	 * rather than construct a new SemSimRelation de novo)
	 * @param description A free-text description of the relation
	 * @param relationURI A URI for the relation */
	public SemSimRelation(String description, URI relationURI) {
		String name;
		if(relationURI.getFragment()==null){
			name = relationURI.toString();
			name = name.substring(name.lastIndexOf("/")+1, name.length());
			setName(name);
		}
		else name = relationURI.getFragment();
		setName(name);
		setDescription(description);
		setURI(relationURI);
	}
	
	/** Set the URI of the relation
	 * @param uri The URI of the relation */
	public void setURI(URI uri) {
		this.uri = uri;
	}
	
	/** @return The URI of the relation */
	public URI getURI() {
		return uri;
	}
	
	@Override
	public URI getSemSimClassURI() {
		return null;
	}
}
