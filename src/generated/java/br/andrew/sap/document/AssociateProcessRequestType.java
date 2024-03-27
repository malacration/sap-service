/**
 * AssociateProcessRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class AssociateProcessRequestType  implements java.io.Serializable {
    private java.lang.String documentID;

    private java.lang.String categoryID;

    private java.lang.String processID;

    public AssociateProcessRequestType() {
    }

    public AssociateProcessRequestType(
           java.lang.String documentID,
           java.lang.String categoryID,
           java.lang.String processID) {
           this.documentID = documentID;
           this.categoryID = categoryID;
           this.processID = processID;
    }


    /**
     * Gets the documentID value for this AssociateProcessRequestType.
     * 
     * @return documentID
     */
    public java.lang.String getDocumentID() {
        return documentID;
    }


    /**
     * Sets the documentID value for this AssociateProcessRequestType.
     * 
     * @param documentID
     */
    public void setDocumentID(java.lang.String documentID) {
        this.documentID = documentID;
    }


    /**
     * Gets the categoryID value for this AssociateProcessRequestType.
     * 
     * @return categoryID
     */
    public java.lang.String getCategoryID() {
        return categoryID;
    }


    /**
     * Sets the categoryID value for this AssociateProcessRequestType.
     * 
     * @param categoryID
     */
    public void setCategoryID(java.lang.String categoryID) {
        this.categoryID = categoryID;
    }


    /**
     * Gets the processID value for this AssociateProcessRequestType.
     * 
     * @return processID
     */
    public java.lang.String getProcessID() {
        return processID;
    }


    /**
     * Sets the processID value for this AssociateProcessRequestType.
     * 
     * @param processID
     */
    public void setProcessID(java.lang.String processID) {
        this.processID = processID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AssociateProcessRequestType)) return false;
        AssociateProcessRequestType other = (AssociateProcessRequestType) obj;
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
            ((this.processID==null && other.getProcessID()==null) || 
             (this.processID!=null &&
              this.processID.equals(other.getProcessID())));
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
        if (getProcessID() != null) {
            _hashCode += getProcessID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AssociateProcessRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "associateProcessRequestType"));
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
        elemField.setFieldName("processID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ProcessID"));
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
