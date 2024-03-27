/**
 * GetKeyWordsDataRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class GetKeyWordsDataRequestType  implements java.io.Serializable {
    private java.lang.String[] words;

    public GetKeyWordsDataRequestType() {
    }

    public GetKeyWordsDataRequestType(
           java.lang.String[] words) {
           this.words = words;
    }


    /**
     * Gets the words value for this GetKeyWordsDataRequestType.
     * 
     * @return words
     */
    public java.lang.String[] getWords() {
        return words;
    }


    /**
     * Sets the words value for this GetKeyWordsDataRequestType.
     * 
     * @param words
     */
    public void setWords(java.lang.String[] words) {
        this.words = words;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetKeyWordsDataRequestType)) return false;
        GetKeyWordsDataRequestType other = (GetKeyWordsDataRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.words==null && other.getWords()==null) || 
             (this.words!=null &&
              java.util.Arrays.equals(this.words, other.getWords())));
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
        if (getWords() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getWords());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWords(), i);
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
        new org.apache.axis.description.TypeDesc(GetKeyWordsDataRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "getKeyWordsDataRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("words");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "words"));
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
