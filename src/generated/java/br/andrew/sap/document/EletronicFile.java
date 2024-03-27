/**
 * EletronicFile.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class EletronicFile  implements java.io.Serializable {
    private java.lang.String NMFILE;

    private byte[] BINFILE;

    private java.lang.String CONTAINER;

    private java.lang.String ERROR;

    public EletronicFile() {
    }

    public EletronicFile(
           java.lang.String NMFILE,
           byte[] BINFILE,
           java.lang.String CONTAINER,
           java.lang.String ERROR) {
           this.NMFILE = NMFILE;
           this.BINFILE = BINFILE;
           this.CONTAINER = CONTAINER;
           this.ERROR = ERROR;
    }


    /**
     * Gets the NMFILE value for this EletronicFile.
     * 
     * @return NMFILE
     */
    public java.lang.String getNMFILE() {
        return NMFILE;
    }


    /**
     * Sets the NMFILE value for this EletronicFile.
     * 
     * @param NMFILE
     */
    public void setNMFILE(java.lang.String NMFILE) {
        this.NMFILE = NMFILE;
    }


    /**
     * Gets the BINFILE value for this EletronicFile.
     * 
     * @return BINFILE
     */
    public byte[] getBINFILE() {
        return BINFILE;
    }


    /**
     * Sets the BINFILE value for this EletronicFile.
     * 
     * @param BINFILE
     */
    public void setBINFILE(byte[] BINFILE) {
        this.BINFILE = BINFILE;
    }


    /**
     * Gets the CONTAINER value for this EletronicFile.
     * 
     * @return CONTAINER
     */
    public java.lang.String getCONTAINER() {
        return CONTAINER;
    }


    /**
     * Sets the CONTAINER value for this EletronicFile.
     * 
     * @param CONTAINER
     */
    public void setCONTAINER(java.lang.String CONTAINER) {
        this.CONTAINER = CONTAINER;
    }


    /**
     * Gets the ERROR value for this EletronicFile.
     * 
     * @return ERROR
     */
    public java.lang.String getERROR() {
        return ERROR;
    }


    /**
     * Sets the ERROR value for this EletronicFile.
     * 
     * @param ERROR
     */
    public void setERROR(java.lang.String ERROR) {
        this.ERROR = ERROR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EletronicFile)) return false;
        EletronicFile other = (EletronicFile) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.NMFILE==null && other.getNMFILE()==null) || 
             (this.NMFILE!=null &&
              this.NMFILE.equals(other.getNMFILE()))) &&
            ((this.BINFILE==null && other.getBINFILE()==null) || 
             (this.BINFILE!=null &&
              java.util.Arrays.equals(this.BINFILE, other.getBINFILE()))) &&
            ((this.CONTAINER==null && other.getCONTAINER()==null) || 
             (this.CONTAINER!=null &&
              this.CONTAINER.equals(other.getCONTAINER()))) &&
            ((this.ERROR==null && other.getERROR()==null) || 
             (this.ERROR!=null &&
              this.ERROR.equals(other.getERROR())));
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
        if (getNMFILE() != null) {
            _hashCode += getNMFILE().hashCode();
        }
        if (getBINFILE() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBINFILE());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBINFILE(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCONTAINER() != null) {
            _hashCode += getCONTAINER().hashCode();
        }
        if (getERROR() != null) {
            _hashCode += getERROR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(EletronicFile.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "eletronicFile"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMFILE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMFILE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("BINFILE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "BINFILE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CONTAINER");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CONTAINER"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ERROR");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ERROR"));
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
