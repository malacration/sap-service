/**
 * ElectronicFile.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class ElectronicFile  implements java.io.Serializable {
    private java.lang.String FILENAME;

    public ElectronicFile() {
    }

    public ElectronicFile(
           java.lang.String FILENAME) {
           this.FILENAME = FILENAME;
    }


    /**
     * Gets the FILENAME value for this ElectronicFile.
     * 
     * @return FILENAME
     */
    public java.lang.String getFILENAME() {
        return FILENAME;
    }


    /**
     * Sets the FILENAME value for this ElectronicFile.
     * 
     * @param FILENAME
     */
    public void setFILENAME(java.lang.String FILENAME) {
        this.FILENAME = FILENAME;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ElectronicFile)) return false;
        ElectronicFile other = (ElectronicFile) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.FILENAME==null && other.getFILENAME()==null) || 
             (this.FILENAME!=null &&
              this.FILENAME.equals(other.getFILENAME())));
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
        if (getFILENAME() != null) {
            _hashCode += getFILENAME().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ElectronicFile.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "electronicFile"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("FILENAME");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "FILENAME"));
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
