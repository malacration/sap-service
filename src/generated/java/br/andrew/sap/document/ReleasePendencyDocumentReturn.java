/**
 * ReleasePendencyDocumentReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class ReleasePendencyDocumentReturn  implements java.io.Serializable {
    private java.lang.String IDUSER;

    private int USERTYPE;

    public ReleasePendencyDocumentReturn() {
    }

    public ReleasePendencyDocumentReturn(
           java.lang.String IDUSER,
           int USERTYPE) {
           this.IDUSER = IDUSER;
           this.USERTYPE = USERTYPE;
    }


    /**
     * Gets the IDUSER value for this ReleasePendencyDocumentReturn.
     * 
     * @return IDUSER
     */
    public java.lang.String getIDUSER() {
        return IDUSER;
    }


    /**
     * Sets the IDUSER value for this ReleasePendencyDocumentReturn.
     * 
     * @param IDUSER
     */
    public void setIDUSER(java.lang.String IDUSER) {
        this.IDUSER = IDUSER;
    }


    /**
     * Gets the USERTYPE value for this ReleasePendencyDocumentReturn.
     * 
     * @return USERTYPE
     */
    public int getUSERTYPE() {
        return USERTYPE;
    }


    /**
     * Sets the USERTYPE value for this ReleasePendencyDocumentReturn.
     * 
     * @param USERTYPE
     */
    public void setUSERTYPE(int USERTYPE) {
        this.USERTYPE = USERTYPE;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReleasePendencyDocumentReturn)) return false;
        ReleasePendencyDocumentReturn other = (ReleasePendencyDocumentReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.IDUSER==null && other.getIDUSER()==null) || 
             (this.IDUSER!=null &&
              this.IDUSER.equals(other.getIDUSER()))) &&
            this.USERTYPE == other.getUSERTYPE();
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
        if (getIDUSER() != null) {
            _hashCode += getIDUSER().hashCode();
        }
        _hashCode += getUSERTYPE();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReleasePendencyDocumentReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDUSER");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDUSER"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("USERTYPE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "USERTYPE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
