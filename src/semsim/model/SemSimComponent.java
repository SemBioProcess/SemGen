package semsim.model;

import semsim.SemSimObject;

/**
 * A SemSimComponent is a representation of a mathematical or physical element
 */
public abstract class SemSimComponent extends SemSimObject {
	
	private String metadataID = new String("");
	
	/**
	 * Set the component's metadata ID. These ID's are often used
	 * by XML-based modeling languages such as SBML and CellML
	 * to link XML elements to RDF statements that further describe
	 * the elements.
	 * 
	 * @param metadataID The ID to apply
	 */
	public void setMetadataID(String metadataID) {
		this.metadataID = metadataID;
	}

	/**
	 * Get the component's metadata ID. These ID's are often used
	 * by XML-based modeling languages such as SBML and CellML
	 * to link XML elements to RDF statements that further describe
	 * the elements.
	 */
	public String getMetadataID() {
		return metadataID;
	}
}
