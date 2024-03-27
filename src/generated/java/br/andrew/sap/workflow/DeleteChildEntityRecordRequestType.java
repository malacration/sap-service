/**
 * DeleteChildEntityRecordRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class DeleteChildEntityRecordRequestType  implements java.io.Serializable {
    private java.lang.String workflowID;

    private java.lang.String mainEntityID;

    private java.lang.String childRelationshipID;

    private java.lang.String childRecordOID;

    public DeleteChildEntityRecordRequestType() {
    }

    public DeleteChildEntityRecordRequestType(
           java.lang.String workflowID,
           java.lang.String mainEntityID,
           java.lang.String childRelationshipID,
           java.lang.String childRecordOID) {
           this.workflowID = workflowID;
           this.mainEntityID = mainEntityID;
           this.childRelationshipID = childRelationshipID;
           this.childRecordOID = childRecordOID;
    }


    /**
     * Gets the workflowID value for this DeleteChildEntityRecordRequestType.
     * 
     * @return workflowID
     */
    public java.lang.String getWorkflowID() {
        return workflowID;
    }


    /**
     * Sets the workflowID value for this DeleteChildEntityRecordRequestType.
     * 
     * @param workflowID
     */
    public void setWorkflowID(java.lang.String workflowID) {
        this.workflowID = workflowID;
    }


    /**
     * Gets the mainEntityID value for this DeleteChildEntityRecordRequestType.
     * 
     * @return mainEntityID
     */
    public java.lang.String getMainEntityID() {
        return mainEntityID;
    }


    /**
     * Sets the mainEntityID value for this DeleteChildEntityRecordRequestType.
     * 
     * @param mainEntityID
     */
    public void setMainEntityID(java.lang.String mainEntityID) {
        this.mainEntityID = mainEntityID;
    }


    /**
     * Gets the childRelationshipID value for this DeleteChildEntityRecordRequestType.
     * 
     * @return childRelationshipID
     */
    public java.lang.String getChildRelationshipID() {
        return childRelationshipID;
    }


    /**
     * Sets the childRelationshipID value for this DeleteChildEntityRecordRequestType.
     * 
     * @param childRelationshipID
     */
    public void setChildRelationshipID(java.lang.String childRelationshipID) {
        this.childRelationshipID = childRelationshipID;
    }


    /**
     * Gets the childRecordOID value for this DeleteChildEntityRecordRequestType.
     * 
     * @return childRecordOID
     */
    public java.lang.String getChildRecordOID() {
        return childRecordOID;
    }


    /**
     * Sets the childRecordOID value for this DeleteChildEntityRecordRequestType.
     * 
     * @param childRecordOID
     */
    public void setChildRecordOID(java.lang.String childRecordOID) {
        this.childRecordOID = childRecordOID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeleteChildEntityRecordRequestType)) return false;
        DeleteChildEntityRecordRequestType other = (DeleteChildEntityRecordRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.workflowID==null && other.getWorkflowID()==null) || 
             (this.workflowID!=null &&
              this.workflowID.equals(other.getWorkflowID()))) &&
            ((this.mainEntityID==null && other.getMainEntityID()==null) || 
             (this.mainEntityID!=null &&
              this.mainEntityID.equals(other.getMainEntityID()))) &&
            ((this.childRelationshipID==null && other.getChildRelationshipID()==null) || 
             (this.childRelationshipID!=null &&
              this.childRelationshipID.equals(other.getChildRelationshipID()))) &&
            ((this.childRecordOID==null && other.getChildRecordOID()==null) || 
             (this.childRecordOID!=null &&
              this.childRecordOID.equals(other.getChildRecordOID())));
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
        if (getWorkflowID() != null) {
            _hashCode += getWorkflowID().hashCode();
        }
        if (getMainEntityID() != null) {
            _hashCode += getMainEntityID().hashCode();
        }
        if (getChildRelationshipID() != null) {
            _hashCode += getChildRelationshipID().hashCode();
        }
        if (getChildRecordOID() != null) {
            _hashCode += getChildRecordOID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DeleteChildEntityRecordRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecordRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "WorkflowID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mainEntityID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "MainEntityID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("childRelationshipID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ChildRelationshipID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("childRecordOID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ChildRecordOID"));
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
