/**
 * AttributeFileData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class AttributeFileData  implements java.io.Serializable {
    private java.lang.String entityAttributeID;

    private java.lang.String fileName;

    private byte[] fileContent;

    public AttributeFileData() {
    }

    public AttributeFileData(
           java.lang.String entityAttributeID,
           java.lang.String fileName,
           byte[] fileContent) {
           this.entityAttributeID = entityAttributeID;
           this.fileName = fileName;
           this.fileContent = fileContent;
    }


    /**
     * Gets the entityAttributeID value for this AttributeFileData.
     * 
     * @return entityAttributeID
     */
    public java.lang.String getEntityAttributeID() {
        return entityAttributeID;
    }


    /**
     * Sets the entityAttributeID value for this AttributeFileData.
     * 
     * @param entityAttributeID
     */
    public void setEntityAttributeID(java.lang.String entityAttributeID) {
        this.entityAttributeID = entityAttributeID;
    }


    /**
     * Gets the fileName value for this AttributeFileData.
     * 
     * @return fileName
     */
    public java.lang.String getFileName() {
        return fileName;
    }


    /**
     * Sets the fileName value for this AttributeFileData.
     * 
     * @param fileName
     */
    public void setFileName(java.lang.String fileName) {
        this.fileName = fileName;
    }


    /**
     * Gets the fileContent value for this AttributeFileData.
     * 
     * @return fileContent
     */
    public byte[] getFileContent() {
        return fileContent;
    }


    /**
     * Sets the fileContent value for this AttributeFileData.
     * 
     * @param fileContent
     */
    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AttributeFileData)) return false;
        AttributeFileData other = (AttributeFileData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.entityAttributeID==null && other.getEntityAttributeID()==null) || 
             (this.entityAttributeID!=null &&
              this.entityAttributeID.equals(other.getEntityAttributeID()))) &&
            ((this.fileName==null && other.getFileName()==null) || 
             (this.fileName!=null &&
              this.fileName.equals(other.getFileName()))) &&
            ((this.fileContent==null && other.getFileContent()==null) || 
             (this.fileContent!=null &&
              java.util.Arrays.equals(this.fileContent, other.getFileContent())));
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
        if (getEntityAttributeID() != null) {
            _hashCode += getEntityAttributeID().hashCode();
        }
        if (getFileName() != null) {
            _hashCode += getFileName().hashCode();
        }
        if (getFileContent() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFileContent());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFileContent(), i);
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
        new org.apache.axis.description.TypeDesc(AttributeFileData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeFileData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityAttributeID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityAttributeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileName");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "FileName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileContent");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "FileContent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
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
