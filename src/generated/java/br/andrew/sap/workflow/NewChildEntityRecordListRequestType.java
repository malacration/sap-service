/**
 * NewChildEntityRecordListRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class NewChildEntityRecordListRequestType  implements java.io.Serializable {
    private java.lang.String workflowID;

    private java.lang.String mainEntityID;

    private java.lang.String childRelationshipID;

    private br.andrew.sap.workflow.EntityRecordArray[] entityRecordList;

    public NewChildEntityRecordListRequestType() {
    }

    public NewChildEntityRecordListRequestType(
           java.lang.String workflowID,
           java.lang.String mainEntityID,
           java.lang.String childRelationshipID,
           br.andrew.sap.workflow.EntityRecordArray[] entityRecordList) {
           this.workflowID = workflowID;
           this.mainEntityID = mainEntityID;
           this.childRelationshipID = childRelationshipID;
           this.entityRecordList = entityRecordList;
    }


    /**
     * Gets the workflowID value for this NewChildEntityRecordListRequestType.
     * 
     * @return workflowID
     */
    public java.lang.String getWorkflowID() {
        return workflowID;
    }


    /**
     * Sets the workflowID value for this NewChildEntityRecordListRequestType.
     * 
     * @param workflowID
     */
    public void setWorkflowID(java.lang.String workflowID) {
        this.workflowID = workflowID;
    }


    /**
     * Gets the mainEntityID value for this NewChildEntityRecordListRequestType.
     * 
     * @return mainEntityID
     */
    public java.lang.String getMainEntityID() {
        return mainEntityID;
    }


    /**
     * Sets the mainEntityID value for this NewChildEntityRecordListRequestType.
     * 
     * @param mainEntityID
     */
    public void setMainEntityID(java.lang.String mainEntityID) {
        this.mainEntityID = mainEntityID;
    }


    /**
     * Gets the childRelationshipID value for this NewChildEntityRecordListRequestType.
     * 
     * @return childRelationshipID
     */
    public java.lang.String getChildRelationshipID() {
        return childRelationshipID;
    }


    /**
     * Sets the childRelationshipID value for this NewChildEntityRecordListRequestType.
     * 
     * @param childRelationshipID
     */
    public void setChildRelationshipID(java.lang.String childRelationshipID) {
        this.childRelationshipID = childRelationshipID;
    }


    /**
     * Gets the entityRecordList value for this NewChildEntityRecordListRequestType.
     * 
     * @return entityRecordList
     */
    public br.andrew.sap.workflow.EntityRecordArray[] getEntityRecordList() {
        return entityRecordList;
    }


    /**
     * Sets the entityRecordList value for this NewChildEntityRecordListRequestType.
     * 
     * @param entityRecordList
     */
    public void setEntityRecordList(br.andrew.sap.workflow.EntityRecordArray[] entityRecordList) {
        this.entityRecordList = entityRecordList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewChildEntityRecordListRequestType)) return false;
        NewChildEntityRecordListRequestType other = (NewChildEntityRecordListRequestType) obj;
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
            ((this.entityRecordList==null && other.getEntityRecordList()==null) || 
             (this.entityRecordList!=null &&
              java.util.Arrays.equals(this.entityRecordList, other.getEntityRecordList())));
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
        if (getEntityRecordList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEntityRecordList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEntityRecordList(), i);
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
        new org.apache.axis.description.TypeDesc(NewChildEntityRecordListRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordListRequestType"));
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
        elemField.setFieldName("entityRecordList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityRecordList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "EntityRecordArray"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "EntityRecord"));
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
