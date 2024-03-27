/**
 * SearchAccessAuditRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class SearchAccessAuditRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String iduser;

    private java.lang.String periodbegin;

    private java.lang.String periodend;

    public SearchAccessAuditRequestType() {
    }

    public SearchAccessAuditRequestType(
           java.lang.String iddocument,
           java.lang.String iduser,
           java.lang.String periodbegin,
           java.lang.String periodend) {
           this.iddocument = iddocument;
           this.iduser = iduser;
           this.periodbegin = periodbegin;
           this.periodend = periodend;
    }


    /**
     * Gets the iddocument value for this SearchAccessAuditRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this SearchAccessAuditRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the iduser value for this SearchAccessAuditRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this SearchAccessAuditRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the periodbegin value for this SearchAccessAuditRequestType.
     * 
     * @return periodbegin
     */
    public java.lang.String getPeriodbegin() {
        return periodbegin;
    }


    /**
     * Sets the periodbegin value for this SearchAccessAuditRequestType.
     * 
     * @param periodbegin
     */
    public void setPeriodbegin(java.lang.String periodbegin) {
        this.periodbegin = periodbegin;
    }


    /**
     * Gets the periodend value for this SearchAccessAuditRequestType.
     * 
     * @return periodend
     */
    public java.lang.String getPeriodend() {
        return periodend;
    }


    /**
     * Sets the periodend value for this SearchAccessAuditRequestType.
     * 
     * @param periodend
     */
    public void setPeriodend(java.lang.String periodend) {
        this.periodend = periodend;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SearchAccessAuditRequestType)) return false;
        SearchAccessAuditRequestType other = (SearchAccessAuditRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.iddocument==null && other.getIddocument()==null) || 
             (this.iddocument!=null &&
              this.iddocument.equals(other.getIddocument()))) &&
            ((this.iduser==null && other.getIduser()==null) || 
             (this.iduser!=null &&
              this.iduser.equals(other.getIduser()))) &&
            ((this.periodbegin==null && other.getPeriodbegin()==null) || 
             (this.periodbegin!=null &&
              this.periodbegin.equals(other.getPeriodbegin()))) &&
            ((this.periodend==null && other.getPeriodend()==null) || 
             (this.periodend!=null &&
              this.periodend.equals(other.getPeriodend())));
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
        if (getIddocument() != null) {
            _hashCode += getIddocument().hashCode();
        }
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        if (getPeriodbegin() != null) {
            _hashCode += getPeriodbegin().hashCode();
        }
        if (getPeriodend() != null) {
            _hashCode += getPeriodend().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SearchAccessAuditRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "searchAccessAuditRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iddocument");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iddocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iduser");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iduser"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("periodbegin");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "periodbegin"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("periodend");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "periodend"));
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
