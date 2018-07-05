package semsim;

import java.net.URI;

import semsim.definitions.SemSimTypes;

/**
 * Class representing elements within a SemSim model
 * @author mneal
 *
 */
public abstract class SemSimObject {
	private String name = new String("");
	private String description = new String("");
	private String metadataID = new String("");
	private SemSimTypes semsimtype;
	
	public SemSimObject(SemSimTypes type) {
		semsimtype = type;
	}
	
	/**
	 * Copy constructor
	 * @param objtocopy The object to copy
	 */
	public SemSimObject(SemSimObject objtocopy) {
		semsimtype = objtocopy.semsimtype;
		name = new String(objtocopy.name);
		
		if (objtocopy.description != null)
			description = new String(objtocopy.description);
		
		if(objtocopy.metadataID != null)
			metadataID = new String(objtocopy.metadataID);
	}
	
	/** @return The object's free-text description */
	public String getDescription() {
		return description;
	}

	/** @return The object's name */
	public String getName(){
		return name;
	}
	
	/** @return Whether the object has an associated name*/
	public boolean hasName(){
		return ( ! name.equals("") && name != null);
	}
	
	/** @return Whether the object has an associated textual description */
	public boolean hasDescription(){
		if( description != null && ! description.equals("")) return true;
		else return false;
	}
	
	/** @return Whether the object has an associated metadata ID */
	public boolean hasMetadataID(){
		return ( ! metadataID.equals("") && metadataID != null);
	}
	
	/**
	 * Set the object's name
	 * @param name The name to apply
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * Set the component's free-text description
	 * @param description The free-text description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Copy this object's description to another SemSimObject
	 * @param srcds The object with the description we want to copy
	 */
	public void copyDescription(SemSimObject srcds){
		// Copy free-text description
		setDescription(new String(srcds.getDescription()));
	}
	
	/**
	 * Set the component's metadata ID. These ID's are often used
	 * by XML-based modeling languages such as SBML and CellML
	 * to link XML elements to RDF statements that describe
	 * the elements.
	 * 
	 * @param metadataID The ID to apply
	 */
	public void setMetadataID(String metadataID) {
		if (metadataID != null) {
			this.metadataID = metadataID;
		}
	}

	/**
	 * @return The component's metadata ID. These ID's are used
	 * by XML-based modeling languages such as SBML and CellML
	 * to link XML elements to RDF statements that describe
	 * the elements.
	 */
	public String getMetadataID() {
		return metadataID;
	}
	

	/** @return This object's SemSimType */
	public SemSimTypes getSemSimType() {
		return semsimtype;
	}
	
	/** @return This object's SemSim class URI */
	public URI getSemSimClassURI() {
		return semsimtype.getURI();
	}
	
	/**
	 * @param type A specified SemSimType
	 * @return Whether this object is a specified SemSimType
	 */
	public boolean isType(SemSimTypes type) {
		return type == semsimtype;
	}
	
}

