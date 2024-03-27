/**
 * RelateCopyStationRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class RelateCopyStationRequestType  implements java.io.Serializable {
    private java.lang.String documentID;

    private java.lang.String categoryID;

    private java.lang.String copyStationID;

    private int quantity;

    public RelateCopyStationRequestType() {
    }

    public RelateCopyStationRequestType(
           java.lang.String documentID,
           java.lang.String categoryID,
           java.lang.String copyStationID,
           int quantity) {
           this.documentID = documentID;
           this.categoryID = categoryID;
           this.copyStationID = copyStationID;
           this.quantity = quantity;
    }


    /**
     * Gets the documentID value for this RelateCopyStationRequestType.
     * 
     * @return documentID
     */
    public java.lang.String getDocumentID() {
        return documentID;
    }


    /**
     * Sets the documentID value for this RelateCopyStationRequestType.
     * 
     * @param documentID
     */
    public void setDocumentID(java.lang.String documentID) {
        this.documentID = documentID;
    }


    /**
     * Gets the categoryID value for this RelateCopyStationRequestType.
     * 
     * @return categoryID
     */
    public java.lang.String getCategoryID() {
        return categoryID;
    }


    /**
     * Sets the categoryID value for this RelateCopyStationRequestType.
     * 
     * @param categoryID
     */
    public void setCategoryID(java.lang.String categoryID) {
        this.categoryID = categoryID;
    }


    /**
     * Gets the copyStationID value for this RelateCopyStationRequestType.
     * 
     * @return copyStationID
     */
    public java.lang.String getCopyStationID() {
        return copyStationID;
    }


    /**
     * Sets the copyStationID value for this RelateCopyStationRequestType.
     * 
     * @param copyStationID
     */
    public void setCopyStationID(java.lang.String copyStationID) {
        this.copyStationID = copyStationID;
    }


    /**
     * Gets the quantity value for this RelateCopyStationRequestType.
     * 
     * @return quantity
     */
    public int getQuantity() {
        return quantity;
    }


    /**
     * Sets the quantity value for this RelateCopyStationRequestType.
     * 
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RelateCopyStationRequestType)) return false;
        RelateCopyStationRequestType other = (RelateCopyStationRequestType) obj;
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
            ((this.copyStationID==null && other.getCopyStationID()==null) || 
             (this.copyStationID!=null &&
              this.copyStationID.equals(other.getCopyStationID()))) &&
            this.quantity == other.getQuantity();
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
        if (getCopyStationID() != null) {
            _hashCode += getCopyStationID().hashCode();
        }
        _hashCode += getQuantity();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RelateCopyStationRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "relateCopyStationRequestType"));
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
        elemField.setFieldName("copyStationID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CopyStationID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("quantity");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Quantity"));
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
