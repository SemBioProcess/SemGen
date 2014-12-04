package semsim;

import java.net.URI;

public abstract class SemSimObject {
	private String name = new String("");
	private String description = new String("");
	
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
	
	public abstract URI getSemSimClassURI();
}

