/**
 * Subtype.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package semgen.webservices.KEGG;

<<<<<<< HEAD
import javax.xml.namespace.QName;



public class Subtype  implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private String relation;

    private int element_id;

    private String type;

    public Subtype() {}

    public Subtype(
           String relation,
           int element_id,
           String type) {
=======
public class Subtype  implements java.io.Serializable {
    private java.lang.String relation;

    private int element_id;

    private java.lang.String type;

    public Subtype() {
    }

    public Subtype(
           java.lang.String relation,
           int element_id,
           java.lang.String type) {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
           this.relation = relation;
           this.element_id = element_id;
           this.type = type;
    }


    /**
     * Gets the relation value for this Subtype.
     * 
     * @return relation
     */
<<<<<<< HEAD
    public String getRelation() {
=======
    public java.lang.String getRelation() {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        return relation;
    }


    /**
     * Sets the relation value for this Subtype.
     * 
     * @param relation
     */
<<<<<<< HEAD
    public void setRelation(String relation) {
=======
    public void setRelation(java.lang.String relation) {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        this.relation = relation;
    }


    /**
     * Gets the element_id value for this Subtype.
     * 
     * @return element_id
     */
    public int getElement_id() {
        return element_id;
    }


    /**
     * Sets the element_id value for this Subtype.
     * 
     * @param element_id
     */
    public void setElement_id(int element_id) {
        this.element_id = element_id;
    }


    /**
     * Gets the type value for this Subtype.
     * 
     * @return type
     */
<<<<<<< HEAD
    public String getType() {
=======
    public java.lang.String getType() {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        return type;
    }


    /**
     * Sets the type value for this Subtype.
     * 
     * @param type
     */
<<<<<<< HEAD
    public void setType(String type) {
        this.type = type;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof Subtype)) return false;
        Subtype other = (Subtype) obj;
=======
    public void setType(java.lang.String type) {
        this.type = type;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Subtype)) return false;
        Subtype other = (Subtype) obj;
        if (obj == null) return false;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.relation==null && other.getRelation()==null) || 
             (this.relation!=null &&
              this.relation.equals(other.getRelation()))) &&
            this.element_id == other.getElement_id() &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getRelation() != null) {
            _hashCode += getRelation().hashCode();
        }
        _hashCode += getElement_id();
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Subtype.class, true);

    static {
<<<<<<< HEAD
        typeDesc.setXmlType(new QName("SOAP/KEGG", "Subtype"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relation");
        elemField.setXmlName(new QName("", "relation"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
=======
        typeDesc.setXmlType(new javax.xml.namespace.QName("SOAP/KEGG", "Subtype"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "relation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("element_id");
<<<<<<< HEAD
        elemField.setXmlName(new QName("", "element_id"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
=======
        elemField.setXmlName(new javax.xml.namespace.QName("", "element_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
<<<<<<< HEAD
        elemField.setXmlName(new QName("", "type"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
=======
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
<<<<<<< HEAD
           Class<?> _javaType,  
           QName _xmlType) {
=======
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
<<<<<<< HEAD
           Class<?> _javaType,  
           QName _xmlType) {
=======
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
