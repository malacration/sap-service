/**
 * ListPendencyDocumentReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class ListPendencyDocumentReturn  implements java.io.Serializable {
    private java.lang.String IDDOCUMENT;

    private java.lang.String IDREVISION;

    private int PENDENCYTYPE;

    private java.lang.String ERROR;

    public ListPendencyDocumentReturn() {
    }

    public ListPendencyDocumentReturn(
           java.lang.String IDDOCUMENT,
           java.lang.String IDREVISION,
           int PENDENCYTYPE,
           java.lang.String ERROR) {
           this.IDDOCUMENT = IDDOCUMENT;
           this.IDREVISION = IDREVISION;
           this.PENDENCYTYPE = PENDENCYTYPE;
           this.ERROR = ERROR;
    }


    /**
     * Gets the IDDOCUMENT value for this ListPendencyDocumentReturn.
     * 
     * @return IDDOCUMENT
     */
    public java.lang.String getIDDOCUMENT() {
        return IDDOCUMENT;
    }


    /**
     * Sets the IDDOCUMENT value for this ListPendencyDocumentReturn.
     * 
     * @param IDDOCUMENT
     */
    public void setIDDOCUMENT(java.lang.String IDDOCUMENT) {
        this.IDDOCUMENT = IDDOCUMENT;
    }


    /**
     * Gets the IDREVISION value for this ListPendencyDocumentReturn.
     * 
     * @return IDREVISION
     */
    public java.lang.String getIDREVISION() {
        return IDREVISION;
    }


    /**
     * Sets the IDREVISION value for this ListPendencyDocumentReturn.
     * 
     * @param IDREVISION
     */
    public void setIDREVISION(java.lang.String IDREVISION) {
        this.IDREVISION = IDREVISION;
    }


    /**
     * Gets the PENDENCYTYPE value for this ListPendencyDocumentReturn.
     * 
     * @return PENDENCYTYPE
     */
    public int getPENDENCYTYPE() {
        return PENDENCYTYPE;
    }


    /**
     * Sets the PENDENCYTYPE value for this ListPendencyDocumentReturn.
     * 
     * @param PENDENCYTYPE
     */
    public void setPENDENCYTYPE(int PENDENCYTYPE) {
        this.PENDENCYTYPE = PENDENCYTYPE;
    }


    /**
     * Gets the ERROR value for this ListPendencyDocumentReturn.
     * 
     * @return ERROR
     */
    public java.lang.String getERROR() {
        return ERROR;
    }


    /**
     * Sets the ERROR value for this ListPendencyDocumentReturn.
     * 
     * @param ERROR
     */
    public void setERROR(java.lang.String ERROR) {
        this.ERROR = ERROR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ListPendencyDocumentReturn)) return false;
        ListPendencyDocumentReturn other = (ListPendencyDocumentReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.IDDOCUMENT==null && other.getIDDOCUMENT()==null) || 
             (this.IDDOCUMENT!=null &&
              this.IDDOCUMENT.equals(other.getIDDOCUMENT()))) &&
            ((this.IDREVISION==null && other.getIDREVISION()==null) || 
             (this.IDREVISION!=null &&
              this.IDREVISION.equals(other.getIDREVISION()))) &&
            this.PENDENCYTYPE == other.getPENDENCYTYPE() &&
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
        if (getIDDOCUMENT() != null) {
            _hashCode += getIDDOCUMENT().hashCode();
        }
        if (getIDREVISION() != null) {
            _hashCode += getIDREVISION().hashCode();
        }
        _hashCode += getPENDENCYTYPE();
        if (getERROR() != null) {
            _hashCode += getERROR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ListPendencyDocumentReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "listPendencyDocumentReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDDOCUMENT");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDDOCUMENT"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDREVISION");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDREVISION"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("PENDENCYTYPE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "PENDENCYTYPE"));
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
