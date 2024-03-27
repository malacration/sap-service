/**
 * AttributeData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class AttributeData  implements java.io.Serializable {
    private java.lang.String entityAttributeID;

    private java.lang.String entityAttributeValue;

    public AttributeData() {
    }

    public AttributeData(
           java.lang.String entityAttributeID,
           java.lang.String entityAttributeValue) {
           this.entityAttributeID = entityAttributeID;
           this.entityAttributeValue = entityAttributeValue;
    }


    /**
     * Gets the entityAttributeID value for this AttributeData.
     * 
     * @return entityAttributeID
     */
    public java.lang.String getEntityAttributeID() {
        return entityAttributeID;
    }


    /**
     * Sets the entityAttributeID value for this AttributeData.
     * 
     * @param entityAttributeID
     */
    public void setEntityAttributeID(java.lang.String entityAttributeID) {
        this.entityAttributeID = entityAttributeID;
    }


    /**
     * Gets the entityAttributeValue value for this AttributeData.
     * 
     * @return entityAttributeValue
     */
    public java.lang.String getEntityAttributeValue() {
        return entityAttributeValue;
    }


    /**
     * Sets the entityAttributeValue value for this AttributeData.
     * 
     * @param entityAttributeValue
     */
    public void setEntityAttributeValue(java.lang.String entityAttributeValue) {
        this.entityAttributeValue = entityAttributeValue;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AttributeData)) return false;
        AttributeData other = (AttributeData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.entityAttributeID==null && other.getEntityAttributeID()==null) || 
             (this.entityAttributeID!=null &&
              this.entityAttributeID.equals(other.getEntityAttributeID()))) &&
            ((this.entityAttributeValue==null && other.getEntityAttributeValue()==null) || 
             (this.entityAttributeValue!=null &&
              this.entityAttributeValue.equals(other.getEntityAttributeValue())));
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
        if (getEntityAttributeID() != null) {
            _hashCode += getEntityAttributeID().hashCode();
        }
        if (getEntityAttributeValue() != null) {
            _hashCode += getEntityAttributeValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AttributeData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityAttributeID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityAttributeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityAttributeValue");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityAttributeValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
