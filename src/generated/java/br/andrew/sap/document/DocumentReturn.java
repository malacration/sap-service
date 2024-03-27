/**
 * DocumentReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DocumentReturn  implements java.io.Serializable {
    private java.lang.String IDDOCUMENT;

    private java.lang.String NMTITLE;

    private java.lang.String IDREVISION;

    private java.lang.String DTDOCUMENT;

    private java.lang.String IDCATEGORY;

    private java.lang.String NMCATEGORY;

    public DocumentReturn() {
    }

    public DocumentReturn(
           java.lang.String IDDOCUMENT,
           java.lang.String NMTITLE,
           java.lang.String IDREVISION,
           java.lang.String DTDOCUMENT,
           java.lang.String IDCATEGORY,
           java.lang.String NMCATEGORY) {
           this.IDDOCUMENT = IDDOCUMENT;
           this.NMTITLE = NMTITLE;
           this.IDREVISION = IDREVISION;
           this.DTDOCUMENT = DTDOCUMENT;
           this.IDCATEGORY = IDCATEGORY;
           this.NMCATEGORY = NMCATEGORY;
    }


    /**
     * Gets the IDDOCUMENT value for this DocumentReturn.
     * 
     * @return IDDOCUMENT
     */
    public java.lang.String getIDDOCUMENT() {
        return IDDOCUMENT;
    }


    /**
     * Sets the IDDOCUMENT value for this DocumentReturn.
     * 
     * @param IDDOCUMENT
     */
    public void setIDDOCUMENT(java.lang.String IDDOCUMENT) {
        this.IDDOCUMENT = IDDOCUMENT;
    }


    /**
     * Gets the NMTITLE value for this DocumentReturn.
     * 
     * @return NMTITLE
     */
    public java.lang.String getNMTITLE() {
        return NMTITLE;
    }


    /**
     * Sets the NMTITLE value for this DocumentReturn.
     * 
     * @param NMTITLE
     */
    public void setNMTITLE(java.lang.String NMTITLE) {
        this.NMTITLE = NMTITLE;
    }


    /**
     * Gets the IDREVISION value for this DocumentReturn.
     * 
     * @return IDREVISION
     */
    public java.lang.String getIDREVISION() {
        return IDREVISION;
    }


    /**
     * Sets the IDREVISION value for this DocumentReturn.
     * 
     * @param IDREVISION
     */
    public void setIDREVISION(java.lang.String IDREVISION) {
        this.IDREVISION = IDREVISION;
    }


    /**
     * Gets the DTDOCUMENT value for this DocumentReturn.
     * 
     * @return DTDOCUMENT
     */
    public java.lang.String getDTDOCUMENT() {
        return DTDOCUMENT;
    }


    /**
     * Sets the DTDOCUMENT value for this DocumentReturn.
     * 
     * @param DTDOCUMENT
     */
    public void setDTDOCUMENT(java.lang.String DTDOCUMENT) {
        this.DTDOCUMENT = DTDOCUMENT;
    }


    /**
     * Gets the IDCATEGORY value for this DocumentReturn.
     * 
     * @return IDCATEGORY
     */
    public java.lang.String getIDCATEGORY() {
        return IDCATEGORY;
    }


    /**
     * Sets the IDCATEGORY value for this DocumentReturn.
     * 
     * @param IDCATEGORY
     */
    public void setIDCATEGORY(java.lang.String IDCATEGORY) {
        this.IDCATEGORY = IDCATEGORY;
    }


    /**
     * Gets the NMCATEGORY value for this DocumentReturn.
     * 
     * @return NMCATEGORY
     */
    public java.lang.String getNMCATEGORY() {
        return NMCATEGORY;
    }


    /**
     * Sets the NMCATEGORY value for this DocumentReturn.
     * 
     * @param NMCATEGORY
     */
    public void setNMCATEGORY(java.lang.String NMCATEGORY) {
        this.NMCATEGORY = NMCATEGORY;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentReturn)) return false;
        DocumentReturn other = (DocumentReturn) obj;
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
            ((this.NMTITLE==null && other.getNMTITLE()==null) || 
             (this.NMTITLE!=null &&
              this.NMTITLE.equals(other.getNMTITLE()))) &&
            ((this.IDREVISION==null && other.getIDREVISION()==null) || 
             (this.IDREVISION!=null &&
              this.IDREVISION.equals(other.getIDREVISION()))) &&
            ((this.DTDOCUMENT==null && other.getDTDOCUMENT()==null) || 
             (this.DTDOCUMENT!=null &&
              this.DTDOCUMENT.equals(other.getDTDOCUMENT()))) &&
            ((this.IDCATEGORY==null && other.getIDCATEGORY()==null) || 
             (this.IDCATEGORY!=null &&
              this.IDCATEGORY.equals(other.getIDCATEGORY()))) &&
            ((this.NMCATEGORY==null && other.getNMCATEGORY()==null) || 
             (this.NMCATEGORY!=null &&
              this.NMCATEGORY.equals(other.getNMCATEGORY())));
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
        if (getNMTITLE() != null) {
            _hashCode += getNMTITLE().hashCode();
        }
        if (getIDREVISION() != null) {
            _hashCode += getIDREVISION().hashCode();
        }
        if (getDTDOCUMENT() != null) {
            _hashCode += getDTDOCUMENT().hashCode();
        }
        if (getIDCATEGORY() != null) {
            _hashCode += getIDCATEGORY().hashCode();
        }
        if (getNMCATEGORY() != null) {
            _hashCode += getNMCATEGORY().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DocumentReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "documentReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDDOCUMENT");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDDOCUMENT"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMTITLE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMTITLE"));
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
        elemField.setFieldName("DTDOCUMENT");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DTDOCUMENT"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDCATEGORY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMCATEGORY"));
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
