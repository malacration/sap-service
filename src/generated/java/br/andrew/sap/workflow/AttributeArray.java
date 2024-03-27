/**
 * AttributeArray.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class AttributeArray  implements java.io.Serializable {
    private java.lang.String attributeID;

    private br.andrew.sap.workflow.AttributeValueData[] attributeValueList;

    public AttributeArray() {
    }

    public AttributeArray(
           java.lang.String attributeID,
           br.andrew.sap.workflow.AttributeValueData[] attributeValueList) {
           this.attributeID = attributeID;
           this.attributeValueList = attributeValueList;
    }


    /**
     * Gets the attributeID value for this AttributeArray.
     * 
     * @return attributeID
     */
    public java.lang.String getAttributeID() {
        return attributeID;
    }


    /**
     * Sets the attributeID value for this AttributeArray.
     * 
     * @param attributeID
     */
    public void setAttributeID(java.lang.String attributeID) {
        this.attributeID = attributeID;
    }


    /**
     * Gets the attributeValueList value for this AttributeArray.
     * 
     * @return attributeValueList
     */
    public br.andrew.sap.workflow.AttributeValueData[] getAttributeValueList() {
        return attributeValueList;
    }


    /**
     * Sets the attributeValueList value for this AttributeArray.
     * 
     * @param attributeValueList
     */
    public void setAttributeValueList(br.andrew.sap.workflow.AttributeValueData[] attributeValueList) {
        this.attributeValueList = attributeValueList;
    }

    public br.andrew.sap.workflow.AttributeValueData getAttributeValueList(int i) {
        return this.attributeValueList[i];
    }

    public void setAttributeValueList(int i, br.andrew.sap.workflow.AttributeValueData _value) {
        this.attributeValueList[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AttributeArray)) return false;
        AttributeArray other = (AttributeArray) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attributeID==null && other.getAttributeID()==null) || 
             (this.attributeID!=null &&
              this.attributeID.equals(other.getAttributeID()))) &&
            ((this.attributeValueList==null && other.getAttributeValueList()==null) || 
             (this.attributeValueList!=null &&
              java.util.Arrays.equals(this.attributeValueList, other.getAttributeValueList())));
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
        if (getAttributeID() != null) {
            _hashCode += getAttributeID().hashCode();
        }
        if (getAttributeValueList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttributeValueList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttributeValueList(), i);
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
        new org.apache.axis.description.TypeDesc(AttributeArray.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeArray"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributeID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "AttributeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributeValueList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "AttributeValueList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeValueData"));
        elemField.setMinOccurs(0);
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
