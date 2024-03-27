/**
 * AssocWorkflowData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class AssocWorkflowData  implements java.io.Serializable {
    private java.lang.String assocWorkflowID;

    private java.math.BigInteger assocWorkflowType;

    public AssocWorkflowData() {
    }

    public AssocWorkflowData(
           java.lang.String assocWorkflowID,
           java.math.BigInteger assocWorkflowType) {
           this.assocWorkflowID = assocWorkflowID;
           this.assocWorkflowType = assocWorkflowType;
    }


    /**
     * Gets the assocWorkflowID value for this AssocWorkflowData.
     * 
     * @return assocWorkflowID
     */
    public java.lang.String getAssocWorkflowID() {
        return assocWorkflowID;
    }


    /**
     * Sets the assocWorkflowID value for this AssocWorkflowData.
     * 
     * @param assocWorkflowID
     */
    public void setAssocWorkflowID(java.lang.String assocWorkflowID) {
        this.assocWorkflowID = assocWorkflowID;
    }


    /**
     * Gets the assocWorkflowType value for this AssocWorkflowData.
     * 
     * @return assocWorkflowType
     */
    public java.math.BigInteger getAssocWorkflowType() {
        return assocWorkflowType;
    }


    /**
     * Sets the assocWorkflowType value for this AssocWorkflowData.
     * 
     * @param assocWorkflowType
     */
    public void setAssocWorkflowType(java.math.BigInteger assocWorkflowType) {
        this.assocWorkflowType = assocWorkflowType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AssocWorkflowData)) return false;
        AssocWorkflowData other = (AssocWorkflowData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.assocWorkflowID==null && other.getAssocWorkflowID()==null) || 
             (this.assocWorkflowID!=null &&
              this.assocWorkflowID.equals(other.getAssocWorkflowID()))) &&
            ((this.assocWorkflowType==null && other.getAssocWorkflowType()==null) || 
             (this.assocWorkflowType!=null &&
              this.assocWorkflowType.equals(other.getAssocWorkflowType())));
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
        if (getAssocWorkflowID() != null) {
            _hashCode += getAssocWorkflowID().hashCode();
        }
        if (getAssocWorkflowType() != null) {
            _hashCode += getAssocWorkflowType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AssocWorkflowData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "assocWorkflowData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assocWorkflowID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "AssocWorkflowID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assocWorkflowType");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "AssocWorkflowType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
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
