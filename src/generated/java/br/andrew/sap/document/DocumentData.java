/**
 * DocumentData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DocumentData  implements java.io.Serializable {
    private java.lang.String NMTITLE;

    private java.lang.String ATTRIBUTTES;

    public DocumentData() {
    }

    public DocumentData(
           java.lang.String NMTITLE,
           java.lang.String ATTRIBUTTES) {
           this.NMTITLE = NMTITLE;
           this.ATTRIBUTTES = ATTRIBUTTES;
    }


    /**
     * Gets the NMTITLE value for this DocumentData.
     * 
     * @return NMTITLE
     */
    public java.lang.String getNMTITLE() {
        return NMTITLE;
    }


    /**
     * Sets the NMTITLE value for this DocumentData.
     * 
     * @param NMTITLE
     */
    public void setNMTITLE(java.lang.String NMTITLE) {
        this.NMTITLE = NMTITLE;
    }


    /**
     * Gets the ATTRIBUTTES value for this DocumentData.
     * 
     * @return ATTRIBUTTES
     */
    public java.lang.String getATTRIBUTTES() {
        return ATTRIBUTTES;
    }


    /**
     * Sets the ATTRIBUTTES value for this DocumentData.
     * 
     * @param ATTRIBUTTES
     */
    public void setATTRIBUTTES(java.lang.String ATTRIBUTTES) {
        this.ATTRIBUTTES = ATTRIBUTTES;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentData)) return false;
        DocumentData other = (DocumentData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.NMTITLE==null && other.getNMTITLE()==null) || 
             (this.NMTITLE!=null &&
              this.NMTITLE.equals(other.getNMTITLE()))) &&
            ((this.ATTRIBUTTES==null && other.getATTRIBUTTES()==null) || 
             (this.ATTRIBUTTES!=null &&
              this.ATTRIBUTTES.equals(other.getATTRIBUTTES())));
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
        if (getNMTITLE() != null) {
            _hashCode += getNMTITLE().hashCode();
        }
        if (getATTRIBUTTES() != null) {
            _hashCode += getATTRIBUTTES().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DocumentData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "documentData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMTITLE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMTITLE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ATTRIBUTTES");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ATTRIBUTTES"));
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
