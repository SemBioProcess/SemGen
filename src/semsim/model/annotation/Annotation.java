package semsim.model.annotation;

import org.apache.commons.lang3.builder.*;

/**
 * A SemSim annotation provides additional information about
 * a SemSim model or one of its components. Annotations can be used
 * to define the physical meaning of a SemSim model or model component, 
 * identify model authors, link a model to its PubMed ID, etc.
 */
public class Annotation implements Cloneable{
	
	protected String valueDescription;
	protected SemSimRelation relation;
	protected Object value;
	
	
	/**
	 * Constructor without a free-text description of the annotation.
	 * @param relation Indicates the relationship between the object being annotated
	 * and the annotation value
	 * @param value The annotation value
	 */
	public Annotation(SemSimRelation relation, Object value){
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
	public Annotation(SemSimRelation relation, Object value, String valueDescription){
		this.relation = relation;
		setValue(value);
		setValueDescription(valueDescription);
	}
	
	/**
	 * Set the free-text description for the annotation value
	 * @param description
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
	 * @param relation
	 */
	public void setRelation(SemSimRelation relation) {
		this.relation = relation;
	}

	/**
	 * @return The relationship between the object being annotated and the annotation value
	 */
	public SemSimRelation getRelation() {
		return relation;
	}
	
	/**
	 * Set the annotation value
	 * @param value An Object that is the annotation value
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
