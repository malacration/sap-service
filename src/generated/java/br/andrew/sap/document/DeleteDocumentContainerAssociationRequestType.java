/**
 * DeleteDocumentContainerAssociationRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DeleteDocumentContainerAssociationRequestType  implements java.io.Serializable {
    private java.lang.String upperLevelCategoryID;

    private java.lang.String upperLevelDocumentID;

    private java.lang.String revisionID;

    private java.lang.String structID;

    private java.lang.String lowerLevelCategoryID;

    private java.lang.String lowerLevelDocumentID;

    public DeleteDocumentContainerAssociationRequestType() {
    }

    public DeleteDocumentContainerAssociationRequestType(
           java.lang.String upperLevelCategoryID,
           java.lang.String upperLevelDocumentID,
           java.lang.String revisionID,
           java.lang.String structID,
           java.lang.String lowerLevelCategoryID,
           java.lang.String lowerLevelDocumentID) {
           this.upperLevelCategoryID = upperLevelCategoryID;
           this.upperLevelDocumentID = upperLevelDocumentID;
           this.revisionID = revisionID;
           this.structID = structID;
           this.lowerLevelCategoryID = lowerLevelCategoryID;
           this.lowerLevelDocumentID = lowerLevelDocumentID;
    }


    /**
     * Gets the upperLevelCategoryID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @return upperLevelCategoryID
     */
    public java.lang.String getUpperLevelCategoryID() {
        return upperLevelCategoryID;
    }


    /**
     * Sets the upperLevelCategoryID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @param upperLevelCategoryID
     */
    public void setUpperLevelCategoryID(java.lang.String upperLevelCategoryID) {
        this.upperLevelCategoryID = upperLevelCategoryID;
    }


    /**
     * Gets the upperLevelDocumentID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @return upperLevelDocumentID
     */
    public java.lang.String getUpperLevelDocumentID() {
        return upperLevelDocumentID;
    }


    /**
     * Sets the upperLevelDocumentID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @param upperLevelDocumentID
     */
    public void setUpperLevelDocumentID(java.lang.String upperLevelDocumentID) {
        this.upperLevelDocumentID = upperLevelDocumentID;
    }


    /**
     * Gets the revisionID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @return revisionID
     */
    public java.lang.String getRevisionID() {
        return revisionID;
    }


    /**
     * Sets the revisionID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @param revisionID
     */
    public void setRevisionID(java.lang.String revisionID) {
        this.revisionID = revisionID;
    }


    /**
     * Gets the structID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @return structID
     */
    public java.lang.String getStructID() {
        return structID;
    }


    /**
     * Sets the structID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @param structID
     */
    public void setStructID(java.lang.String structID) {
        this.structID = structID;
    }


    /**
     * Gets the lowerLevelCategoryID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @return lowerLevelCategoryID
     */
    public java.lang.String getLowerLevelCategoryID() {
        return lowerLevelCategoryID;
    }


    /**
     * Sets the lowerLevelCategoryID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @param lowerLevelCategoryID
     */
    public void setLowerLevelCategoryID(java.lang.String lowerLevelCategoryID) {
        this.lowerLevelCategoryID = lowerLevelCategoryID;
    }


    /**
     * Gets the lowerLevelDocumentID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @return lowerLevelDocumentID
     */
    public java.lang.String getLowerLevelDocumentID() {
        return lowerLevelDocumentID;
    }


    /**
     * Sets the lowerLevelDocumentID value for this DeleteDocumentContainerAssociationRequestType.
     * 
     * @param lowerLevelDocumentID
     */
    public void setLowerLevelDocumentID(java.lang.String lowerLevelDocumentID) {
        this.lowerLevelDocumentID = lowerLevelDocumentID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeleteDocumentContainerAssociationRequestType)) return false;
        DeleteDocumentContainerAssociationRequestType other = (DeleteDocumentContainerAssociationRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.upperLevelCategoryID==null && other.getUpperLevelCategoryID()==null) || 
             (this.upperLevelCategoryID!=null &&
              this.upperLevelCategoryID.equals(other.getUpperLevelCategoryID()))) &&
            ((this.upperLevelDocumentID==null && other.getUpperLevelDocumentID()==null) || 
             (this.upperLevelDocumentID!=null &&
              this.upperLevelDocumentID.equals(other.getUpperLevelDocumentID()))) &&
            ((this.revisionID==null && other.getRevisionID()==null) || 
             (this.revisionID!=null &&
              this.revisionID.equals(other.getRevisionID()))) &&
            ((this.structID==null && other.getStructID()==null) || 
             (this.structID!=null &&
              this.structID.equals(other.getStructID()))) &&
            ((this.lowerLevelCategoryID==null && other.getLowerLevelCategoryID()==null) || 
             (this.lowerLevelCategoryID!=null &&
              this.lowerLevelCategoryID.equals(other.getLowerLevelCategoryID()))) &&
            ((this.lowerLevelDocumentID==null && other.getLowerLevelDocumentID()==null) || 
             (this.lowerLevelDocumentID!=null &&
              this.lowerLevelDocumentID.equals(other.getLowerLevelDocumentID())));
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
        if (getUpperLevelCategoryID() != null) {
            _hashCode += getUpperLevelCategoryID().hashCode();
        }
        if (getUpperLevelDocumentID() != null) {
            _hashCode += getUpperLevelDocumentID().hashCode();
        }
        if (getRevisionID() != null) {
            _hashCode += getRevisionID().hashCode();
        }
        if (getStructID() != null) {
            _hashCode += getStructID().hashCode();
        }
        if (getLowerLevelCategoryID() != null) {
            _hashCode += getLowerLevelCategoryID().hashCode();
        }
        if (getLowerLevelDocumentID() != null) {
            _hashCode += getLowerLevelDocumentID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DeleteDocumentContainerAssociationRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociationRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("upperLevelCategoryID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "UpperLevelCategoryID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("upperLevelDocumentID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "UpperLevelDocumentID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("revisionID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "RevisionID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("structID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "StructID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lowerLevelCategoryID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "LowerLevelCategoryID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lowerLevelDocumentID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "LowerLevelDocumentID"));
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
