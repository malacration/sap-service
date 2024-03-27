/**
 * RelationshipAttributeData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class RelationshipAttributeData  implements java.io.Serializable {
    private java.lang.String relationshipAttributeID;

    private java.lang.String relationshipAttributeValue;

    public RelationshipAttributeData() {
    }

    public RelationshipAttributeData(
           java.lang.String relationshipAttributeID,
           java.lang.String relationshipAttributeValue) {
           this.relationshipAttributeID = relationshipAttributeID;
           this.relationshipAttributeValue = relationshipAttributeValue;
    }


    /**
     * Gets the relationshipAttributeID value for this RelationshipAttributeData.
     * 
     * @return relationshipAttributeID
     */
    public java.lang.String getRelationshipAttributeID() {
        return relationshipAttributeID;
    }


    /**
     * Sets the relationshipAttributeID value for this RelationshipAttributeData.
     * 
     * @param relationshipAttributeID
     */
    public void setRelationshipAttributeID(java.lang.String relationshipAttributeID) {
        this.relationshipAttributeID = relationshipAttributeID;
    }


    /**
     * Gets the relationshipAttributeValue value for this RelationshipAttributeData.
     * 
     * @return relationshipAttributeValue
     */
    public java.lang.String getRelationshipAttributeValue() {
        return relationshipAttributeValue;
    }


    /**
     * Sets the relationshipAttributeValue value for this RelationshipAttributeData.
     * 
     * @param relationshipAttributeValue
     */
    public void setRelationshipAttributeValue(java.lang.String relationshipAttributeValue) {
        this.relationshipAttributeValue = relationshipAttributeValue;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RelationshipAttributeData)) return false;
        RelationshipAttributeData other = (RelationshipAttributeData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.relationshipAttributeID==null && other.getRelationshipAttributeID()==null) || 
             (this.relationshipAttributeID!=null &&
              this.relationshipAttributeID.equals(other.getRelationshipAttributeID()))) &&
            ((this.relationshipAttributeValue==null && other.getRelationshipAttributeValue()==null) || 
             (this.relationshipAttributeValue!=null &&
              this.relationshipAttributeValue.equals(other.getRelationshipAttributeValue())));
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
        if (getRelationshipAttributeID() != null) {
            _hashCode += getRelationshipAttributeID().hashCode();
        }
        if (getRelationshipAttributeValue() != null) {
            _hashCode += getRelationshipAttributeValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RelationshipAttributeData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "relationshipAttributeData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipAttributeID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipAttributeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipAttributeValue");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipAttributeValue"));
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
