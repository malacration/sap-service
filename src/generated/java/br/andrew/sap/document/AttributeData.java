/**
 * AttributeData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class AttributeData  implements java.io.Serializable {
    private java.lang.String IDATTRIBUTE;

    private java.lang.String VLATTRIBUTE;

    public AttributeData() {
    }

    public AttributeData(
           java.lang.String IDATTRIBUTE,
           java.lang.String VLATTRIBUTE) {
           this.IDATTRIBUTE = IDATTRIBUTE;
           this.VLATTRIBUTE = VLATTRIBUTE;
    }


    /**
     * Gets the IDATTRIBUTE value for this AttributeData.
     * 
     * @return IDATTRIBUTE
     */
    public java.lang.String getIDATTRIBUTE() {
        return IDATTRIBUTE;
    }


    /**
     * Sets the IDATTRIBUTE value for this AttributeData.
     * 
     * @param IDATTRIBUTE
     */
    public void setIDATTRIBUTE(java.lang.String IDATTRIBUTE) {
        this.IDATTRIBUTE = IDATTRIBUTE;
    }


    /**
     * Gets the VLATTRIBUTE value for this AttributeData.
     * 
     * @return VLATTRIBUTE
     */
    public java.lang.String getVLATTRIBUTE() {
        return VLATTRIBUTE;
    }


    /**
     * Sets the VLATTRIBUTE value for this AttributeData.
     * 
     * @param VLATTRIBUTE
     */
    public void setVLATTRIBUTE(java.lang.String VLATTRIBUTE) {
        this.VLATTRIBUTE = VLATTRIBUTE;
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
            ((this.IDATTRIBUTE==null && other.getIDATTRIBUTE()==null) || 
             (this.IDATTRIBUTE!=null &&
              this.IDATTRIBUTE.equals(other.getIDATTRIBUTE()))) &&
            ((this.VLATTRIBUTE==null && other.getVLATTRIBUTE()==null) || 
             (this.VLATTRIBUTE!=null &&
              this.VLATTRIBUTE.equals(other.getVLATTRIBUTE())));
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
        if (getIDATTRIBUTE() != null) {
            _hashCode += getIDATTRIBUTE().hashCode();
        }
        if (getVLATTRIBUTE() != null) {
            _hashCode += getVLATTRIBUTE().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AttributeData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "attributeData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDATTRIBUTE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDATTRIBUTE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("VLATTRIBUTE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "VLATTRIBUTE"));
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
