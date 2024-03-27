/**
 * Attributtes.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class Attributtes  implements java.io.Serializable {
    private java.lang.String ATTRIBUTTENAME;

    private java.lang.String[] ATTRIBUTTEVALUE;

    public Attributtes() {
    }

    public Attributtes(
           java.lang.String ATTRIBUTTENAME,
           java.lang.String[] ATTRIBUTTEVALUE) {
           this.ATTRIBUTTENAME = ATTRIBUTTENAME;
           this.ATTRIBUTTEVALUE = ATTRIBUTTEVALUE;
    }


    /**
     * Gets the ATTRIBUTTENAME value for this Attributtes.
     * 
     * @return ATTRIBUTTENAME
     */
    public java.lang.String getATTRIBUTTENAME() {
        return ATTRIBUTTENAME;
    }


    /**
     * Sets the ATTRIBUTTENAME value for this Attributtes.
     * 
     * @param ATTRIBUTTENAME
     */
    public void setATTRIBUTTENAME(java.lang.String ATTRIBUTTENAME) {
        this.ATTRIBUTTENAME = ATTRIBUTTENAME;
    }


    /**
     * Gets the ATTRIBUTTEVALUE value for this Attributtes.
     * 
     * @return ATTRIBUTTEVALUE
     */
    public java.lang.String[] getATTRIBUTTEVALUE() {
        return ATTRIBUTTEVALUE;
    }


    /**
     * Sets the ATTRIBUTTEVALUE value for this Attributtes.
     * 
     * @param ATTRIBUTTEVALUE
     */
    public void setATTRIBUTTEVALUE(java.lang.String[] ATTRIBUTTEVALUE) {
        this.ATTRIBUTTEVALUE = ATTRIBUTTEVALUE;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Attributtes)) return false;
        Attributtes other = (Attributtes) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.ATTRIBUTTENAME==null && other.getATTRIBUTTENAME()==null) || 
             (this.ATTRIBUTTENAME!=null &&
              this.ATTRIBUTTENAME.equals(other.getATTRIBUTTENAME()))) &&
            ((this.ATTRIBUTTEVALUE==null && other.getATTRIBUTTEVALUE()==null) || 
             (this.ATTRIBUTTEVALUE!=null &&
              java.util.Arrays.equals(this.ATTRIBUTTEVALUE, other.getATTRIBUTTEVALUE())));
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
        if (getATTRIBUTTENAME() != null) {
            _hashCode += getATTRIBUTTENAME().hashCode();
        }
        if (getATTRIBUTTEVALUE() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getATTRIBUTTEVALUE());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getATTRIBUTTEVALUE(), i);
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
        new org.apache.axis.description.TypeDesc(Attributtes.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "attributtes"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ATTRIBUTTENAME");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ATTRIBUTTENAME"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ATTRIBUTTEVALUE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ATTRIBUTTEVALUE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "document"));
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
