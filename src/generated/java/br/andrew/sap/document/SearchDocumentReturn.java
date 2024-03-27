/**
 * SearchDocumentReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class SearchDocumentReturn  implements java.io.Serializable {
    private br.andrew.sap.document.DocumentReturn[] RESULTS;

    private int ADHESION;

    private java.lang.String ERROR;

    public SearchDocumentReturn() {
    }

    public SearchDocumentReturn(
           br.andrew.sap.document.DocumentReturn[] RESULTS,
           int ADHESION,
           java.lang.String ERROR) {
           this.RESULTS = RESULTS;
           this.ADHESION = ADHESION;
           this.ERROR = ERROR;
    }


    /**
     * Gets the RESULTS value for this SearchDocumentReturn.
     * 
     * @return RESULTS
     */
    public br.andrew.sap.document.DocumentReturn[] getRESULTS() {
        return RESULTS;
    }


    /**
     * Sets the RESULTS value for this SearchDocumentReturn.
     * 
     * @param RESULTS
     */
    public void setRESULTS(br.andrew.sap.document.DocumentReturn[] RESULTS) {
        this.RESULTS = RESULTS;
    }


    /**
     * Gets the ADHESION value for this SearchDocumentReturn.
     * 
     * @return ADHESION
     */
    public int getADHESION() {
        return ADHESION;
    }


    /**
     * Sets the ADHESION value for this SearchDocumentReturn.
     * 
     * @param ADHESION
     */
    public void setADHESION(int ADHESION) {
        this.ADHESION = ADHESION;
    }


    /**
     * Gets the ERROR value for this SearchDocumentReturn.
     * 
     * @return ERROR
     */
    public java.lang.String getERROR() {
        return ERROR;
    }


    /**
     * Sets the ERROR value for this SearchDocumentReturn.
     * 
     * @param ERROR
     */
    public void setERROR(java.lang.String ERROR) {
        this.ERROR = ERROR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SearchDocumentReturn)) return false;
        SearchDocumentReturn other = (SearchDocumentReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.RESULTS==null && other.getRESULTS()==null) || 
             (this.RESULTS!=null &&
              java.util.Arrays.equals(this.RESULTS, other.getRESULTS()))) &&
            this.ADHESION == other.getADHESION() &&
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
        if (getRESULTS() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRESULTS());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRESULTS(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getADHESION();
        if (getERROR() != null) {
            _hashCode += getERROR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SearchDocumentReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "searchDocumentReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RESULTS");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "RESULTS"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "documentReturn"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ADHESION");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ADHESION"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
