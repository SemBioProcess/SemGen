package semsim;

import java.net.URI;

public abstract class SemSimObject {
	private String name = new String("");
	private String description = new String("");
	private String metadataID = new String("");
	
	/**
	 * Get the component's free-text description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the component's name
	 */
	public String getName(){
		return name;
	}

	/**
	 * Set the object's name
	 * 
	 * @param name The name to apply
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * Set the component's free-text description
	 * 
	 * @param description The free-text description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public void copyDescription(SemSimObject srcds){
		// Copy free-text description
		setDescription(new String(srcds.getDescription()));
	}
	
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
	
	public abstract URI getSemSimClassURI();
}

