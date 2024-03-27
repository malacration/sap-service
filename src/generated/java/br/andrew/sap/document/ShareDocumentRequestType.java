/**
 * ShareDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class ShareDocumentRequestType  implements java.io.Serializable {
    private java.lang.String documentID;

    private java.lang.String categoryID;

    private int recreate;

    private int _public;

    private int download;

    private int original;

    private java.lang.String validityDate;

    public ShareDocumentRequestType() {
    }

    public ShareDocumentRequestType(
           java.lang.String documentID,
           java.lang.String categoryID,
           int recreate,
           int _public,
           int download,
           int original,
           java.lang.String validityDate) {
           this.documentID = documentID;
           this.categoryID = categoryID;
           this.recreate = recreate;
           this._public = _public;
           this.download = download;
           this.original = original;
           this.validityDate = validityDate;
    }


    /**
     * Gets the documentID value for this ShareDocumentRequestType.
     * 
     * @return documentID
     */
    public java.lang.String getDocumentID() {
        return documentID;
    }


    /**
     * Sets the documentID value for this ShareDocumentRequestType.
     * 
     * @param documentID
     */
    public void setDocumentID(java.lang.String documentID) {
        this.documentID = documentID;
    }


    /**
     * Gets the categoryID value for this ShareDocumentRequestType.
     * 
     * @return categoryID
     */
    public java.lang.String getCategoryID() {
        return categoryID;
    }


    /**
     * Sets the categoryID value for this ShareDocumentRequestType.
     * 
     * @param categoryID
     */
    public void setCategoryID(java.lang.String categoryID) {
        this.categoryID = categoryID;
    }


    /**
     * Gets the recreate value for this ShareDocumentRequestType.
     * 
     * @return recreate
     */
    public int getRecreate() {
        return recreate;
    }


    /**
     * Sets the recreate value for this ShareDocumentRequestType.
     * 
     * @param recreate
     */
    public void setRecreate(int recreate) {
        this.recreate = recreate;
    }


    /**
     * Gets the _public value for this ShareDocumentRequestType.
     * 
     * @return _public
     */
    public int get_public() {
        return _public;
    }


    /**
     * Sets the _public value for this ShareDocumentRequestType.
     * 
     * @param _public
     */
    public void set_public(int _public) {
        this._public = _public;
    }


    /**
     * Gets the download value for this ShareDocumentRequestType.
     * 
     * @return download
     */
    public int getDownload() {
        return download;
    }


    /**
     * Sets the download value for this ShareDocumentRequestType.
     * 
     * @param download
     */
    public void setDownload(int download) {
        this.download = download;
    }


    /**
     * Gets the original value for this ShareDocumentRequestType.
     * 
     * @return original
     */
    public int getOriginal() {
        return original;
    }


    /**
     * Sets the original value for this ShareDocumentRequestType.
     * 
     * @param original
     */
    public void setOriginal(int original) {
        this.original = original;
    }


    /**
     * Gets the validityDate value for this ShareDocumentRequestType.
     * 
     * @return validityDate
     */
    public java.lang.String getValidityDate() {
        return validityDate;
    }


    /**
     * Sets the validityDate value for this ShareDocumentRequestType.
     * 
     * @param validityDate
     */
    public void setValidityDate(java.lang.String validityDate) {
        this.validityDate = validityDate;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ShareDocumentRequestType)) return false;
        ShareDocumentRequestType other = (ShareDocumentRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.documentID==null && other.getDocumentID()==null) || 
             (this.documentID!=null &&
              this.documentID.equals(other.getDocumentID()))) &&
            ((this.categoryID==null && other.getCategoryID()==null) || 
             (this.categoryID!=null &&
              this.categoryID.equals(other.getCategoryID()))) &&
            this.recreate == other.getRecreate() &&
            this._public == other.get_public() &&
            this.download == other.getDownload() &&
            this.original == other.getOriginal() &&
            ((this.validityDate==null && other.getValidityDate()==null) || 
             (this.validityDate!=null &&
              this.validityDate.equals(other.getValidityDate())));
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
        if (getDocumentID() != null) {
            _hashCode += getDocumentID().hashCode();
        }
        if (getCategoryID() != null) {
            _hashCode += getCategoryID().hashCode();
        }
        _hashCode += getRecreate();
        _hashCode += get_public();
        _hashCode += getDownload();
        _hashCode += getOriginal();
        if (getValidityDate() != null) {
            _hashCode += getValidityDate().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ShareDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "shareDocumentRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DocumentID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoryID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CategoryID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recreate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Recreate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_public");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Public"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("download");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Download"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("original");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Original"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validityDate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ValidityDate"));
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
