package semsim.annotation;

import org.apache.commons.lang3.builder.*;


/**
 * A SemSim annotation provides additional information about
 * a SemSim model or one of its elements. Annotations can be used
 * to define the physical meaning of a SemSim model or model element, 
 * identify model authors, link a model to its PubMed ID, etc.
 */
public class Annotation {
	
	protected String valueDescription;
	protected Relation relation;
	protected Object value;
	
	
	/**
	 * Constructor without a free-text description of the annotation.
	 * @param relation Indicates the relationship between the object being annotated
	 * and the annotation value
	 * @param value The annotation value
	 */
	public Annotation(Relation relation, Object value){
		this.relation = relation;
		this.setValue(value);
	}
	
	/**
	 * Constructor that includes a free-text description of the annotation.
	 * @param relation Indicates the relationship between the object being annotated
	 * and the annotation value
	 * @param value The annotation value
	 * @param valueDescription A free-text description of the annotation value
	 */
	public Annotation(Relation relation, Object value, String valueDescription){
		this.relation = relation;
		setValue(value);
		setValueDescription(valueDescription);
	}
	/**
	 * Constructor for copying an Annotation
	 * @param tocopy The Annotation to copy
	 */
	public Annotation(Annotation tocopy) {
		if (tocopy.valueDescription!=null) {
			valueDescription = new String(tocopy.valueDescription);
		}
		relation = tocopy.relation;
		value = tocopy.value;
	}
	
	/**
	 * Set the free-text description for the annotation value
	 * @param valueDescription The free-text description
	 */
	public void setValueDescription(String valueDescription) {
		this.valueDescription = valueDescription;
	}

	/**
	 * @return The free-text description of the annotation value
	 */
	public String getValueDescription() {
		return valueDescription;
	}

	/**
	 * Set the relationship between the object being annotated and the annotation value
	 * @param relation The relation (AKA predicate, qualifier) between the object being annotated and the annotation value
	 */
	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	/** @return The relationship between the object being annotated and the annotation value */
	public Relation getRelation() {
		return relation;
	}
	
	/**
	 * Set the annotation value
	 * @param value The annotation value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * @return The annotation value
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Create a copy of the annotation
	 */
	public Annotation clone() throws CloneNotSupportedException {
        return (Annotation) super.clone();
	}
	
	@Override
	public int hashCode() {
	    return value.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Annotation))
			return false;

		if (obj == this)
			return true;

		Annotation rhs = (Annotation) obj;
        return new EqualsBuilder().
            append(relation, rhs.relation).
            append(valueDescription, rhs.valueDescription).
            append(value, rhs.value).
            isEquals();
    }
}
