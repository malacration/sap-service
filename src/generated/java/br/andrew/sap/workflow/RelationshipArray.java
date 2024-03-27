/**
 * RelationshipArray.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class RelationshipArray  implements java.io.Serializable {
    private java.lang.String relationshipID;

    private br.andrew.sap.workflow.RelationshipAttributeData[] relationshipAttribute;

    public RelationshipArray() {
    }

    public RelationshipArray(
           java.lang.String relationshipID,
           br.andrew.sap.workflow.RelationshipAttributeData[] relationshipAttribute) {
           this.relationshipID = relationshipID;
           this.relationshipAttribute = relationshipAttribute;
    }


    /**
     * Gets the relationshipID value for this RelationshipArray.
     * 
     * @return relationshipID
     */
    public java.lang.String getRelationshipID() {
        return relationshipID;
    }


    /**
     * Sets the relationshipID value for this RelationshipArray.
     * 
     * @param relationshipID
     */
    public void setRelationshipID(java.lang.String relationshipID) {
        this.relationshipID = relationshipID;
    }


    /**
     * Gets the relationshipAttribute value for this RelationshipArray.
     * 
     * @return relationshipAttribute
     */
    public br.andrew.sap.workflow.RelationshipAttributeData[] getRelationshipAttribute() {
        return relationshipAttribute;
    }


    /**
     * Sets the relationshipAttribute value for this RelationshipArray.
     * 
     * @param relationshipAttribute
     */
    public void setRelationshipAttribute(br.andrew.sap.workflow.RelationshipAttributeData[] relationshipAttribute) {
        this.relationshipAttribute = relationshipAttribute;
    }

    public br.andrew.sap.workflow.RelationshipAttributeData getRelationshipAttribute(int i) {
        return this.relationshipAttribute[i];
    }

    public void setRelationshipAttribute(int i, br.andrew.sap.workflow.RelationshipAttributeData _value) {
        this.relationshipAttribute[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RelationshipArray)) return false;
        RelationshipArray other = (RelationshipArray) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.relationshipID==null && other.getRelationshipID()==null) || 
             (this.relationshipID!=null &&
              this.relationshipID.equals(other.getRelationshipID()))) &&
            ((this.relationshipAttribute==null && other.getRelationshipAttribute()==null) || 
             (this.relationshipAttribute!=null &&
              java.util.Arrays.equals(this.relationshipAttribute, other.getRelationshipAttribute())));
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
        if (getRelationshipID() != null) {
            _hashCode += getRelationshipID().hashCode();
        }
        if (getRelationshipAttribute() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRelationshipAttribute());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRelationshipAttribute(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RelationshipArray.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "relationshipArray"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipAttribute");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipAttribute"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "relationshipAttributeData"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
