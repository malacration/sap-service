/**
 * WordProperties.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class WordProperties  implements java.io.Serializable {
    private java.lang.String IDKEYWORD;

    private java.lang.String NMKEYWORD;

    private java.lang.String[] SYNONYMS;

    public WordProperties() {
    }

    public WordProperties(
           java.lang.String IDKEYWORD,
           java.lang.String NMKEYWORD,
           java.lang.String[] SYNONYMS) {
           this.IDKEYWORD = IDKEYWORD;
           this.NMKEYWORD = NMKEYWORD;
           this.SYNONYMS = SYNONYMS;
    }


    /**
     * Gets the IDKEYWORD value for this WordProperties.
     * 
     * @return IDKEYWORD
     */
    public java.lang.String getIDKEYWORD() {
        return IDKEYWORD;
    }


    /**
     * Sets the IDKEYWORD value for this WordProperties.
     * 
     * @param IDKEYWORD
     */
    public void setIDKEYWORD(java.lang.String IDKEYWORD) {
        this.IDKEYWORD = IDKEYWORD;
    }


    /**
     * Gets the NMKEYWORD value for this WordProperties.
     * 
     * @return NMKEYWORD
     */
    public java.lang.String getNMKEYWORD() {
        return NMKEYWORD;
    }


    /**
     * Sets the NMKEYWORD value for this WordProperties.
     * 
     * @param NMKEYWORD
     */
    public void setNMKEYWORD(java.lang.String NMKEYWORD) {
        this.NMKEYWORD = NMKEYWORD;
    }


    /**
     * Gets the SYNONYMS value for this WordProperties.
     * 
     * @return SYNONYMS
     */
    public java.lang.String[] getSYNONYMS() {
        return SYNONYMS;
    }


    /**
     * Sets the SYNONYMS value for this WordProperties.
     * 
     * @param SYNONYMS
     */
    public void setSYNONYMS(java.lang.String[] SYNONYMS) {
        this.SYNONYMS = SYNONYMS;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WordProperties)) return false;
        WordProperties other = (WordProperties) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.IDKEYWORD==null && other.getIDKEYWORD()==null) || 
             (this.IDKEYWORD!=null &&
              this.IDKEYWORD.equals(other.getIDKEYWORD()))) &&
            ((this.NMKEYWORD==null && other.getNMKEYWORD()==null) || 
             (this.NMKEYWORD!=null &&
              this.NMKEYWORD.equals(other.getNMKEYWORD()))) &&
            ((this.SYNONYMS==null && other.getSYNONYMS()==null) || 
             (this.SYNONYMS!=null &&
              java.util.Arrays.equals(this.SYNONYMS, other.getSYNONYMS())));
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
        if (getIDKEYWORD() != null) {
            _hashCode += getIDKEYWORD().hashCode();
        }
        if (getNMKEYWORD() != null) {
            _hashCode += getNMKEYWORD().hashCode();
        }
        if (getSYNONYMS() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSYNONYMS());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSYNONYMS(), i);
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
        new org.apache.axis.description.TypeDesc(WordProperties.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "wordProperties"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDKEYWORD");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDKEYWORD"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMKEYWORD");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMKEYWORD"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SYNONYMS");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "SYNONYMS"));
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
