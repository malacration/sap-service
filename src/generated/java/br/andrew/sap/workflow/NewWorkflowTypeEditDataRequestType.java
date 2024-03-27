/**
 * NewWorkflowTypeEditDataRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class NewWorkflowTypeEditDataRequestType  implements java.io.Serializable {
    private boolean workflowTypeID;

    private java.lang.String workflowTitle;

    private java.lang.String userID;

    private br.andrew.sap.workflow.Requester requester;

    private br.andrew.sap.workflow.EntityArray[] entityList;

    private br.andrew.sap.workflow.AttributeArray[] attributeList;

    public NewWorkflowTypeEditDataRequestType() {
    }

    public NewWorkflowTypeEditDataRequestType(
           boolean workflowTypeID,
           java.lang.String workflowTitle,
           java.lang.String userID,
           br.andrew.sap.workflow.Requester requester,
           br.andrew.sap.workflow.EntityArray[] entityList,
           br.andrew.sap.workflow.AttributeArray[] attributeList) {
           this.workflowTypeID = workflowTypeID;
           this.workflowTitle = workflowTitle;
           this.userID = userID;
           this.requester = requester;
           this.entityList = entityList;
           this.attributeList = attributeList;
    }


    /**
     * Gets the workflowTypeID value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @return workflowTypeID
     */
    public boolean isWorkflowTypeID() {
        return workflowTypeID;
    }


    /**
     * Sets the workflowTypeID value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @param workflowTypeID
     */
    public void setWorkflowTypeID(boolean workflowTypeID) {
        this.workflowTypeID = workflowTypeID;
    }


    /**
     * Gets the workflowTitle value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @return workflowTitle
     */
    public java.lang.String getWorkflowTitle() {
        return workflowTitle;
    }


    /**
     * Sets the workflowTitle value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @param workflowTitle
     */
    public void setWorkflowTitle(java.lang.String workflowTitle) {
        this.workflowTitle = workflowTitle;
    }


    /**
     * Gets the userID value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }


    /**
     * Gets the requester value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @return requester
     */
    public br.andrew.sap.workflow.Requester getRequester() {
        return requester;
    }


    /**
     * Sets the requester value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @param requester
     */
    public void setRequester(br.andrew.sap.workflow.Requester requester) {
        this.requester = requester;
    }


    /**
     * Gets the entityList value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @return entityList
     */
    public br.andrew.sap.workflow.EntityArray[] getEntityList() {
        return entityList;
    }


    /**
     * Sets the entityList value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @param entityList
     */
    public void setEntityList(br.andrew.sap.workflow.EntityArray[] entityList) {
        this.entityList = entityList;
    }


    /**
     * Gets the attributeList value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @return attributeList
     */
    public br.andrew.sap.workflow.AttributeArray[] getAttributeList() {
        return attributeList;
    }


    /**
     * Sets the attributeList value for this NewWorkflowTypeEditDataRequestType.
     * 
     * @param attributeList
     */
    public void setAttributeList(br.andrew.sap.workflow.AttributeArray[] attributeList) {
        this.attributeList = attributeList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewWorkflowTypeEditDataRequestType)) return false;
        NewWorkflowTypeEditDataRequestType other = (NewWorkflowTypeEditDataRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.workflowTypeID == other.isWorkflowTypeID() &&
            ((this.workflowTitle==null && other.getWorkflowTitle()==null) || 
             (this.workflowTitle!=null &&
              this.workflowTitle.equals(other.getWorkflowTitle()))) &&
            ((this.userID==null && other.getUserID()==null) || 
             (this.userID!=null &&
              this.userID.equals(other.getUserID()))) &&
            ((this.requester==null && other.getRequester()==null) || 
             (this.requester!=null &&
              this.requester.equals(other.getRequester()))) &&
            ((this.entityList==null && other.getEntityList()==null) || 
             (this.entityList!=null &&
              java.util.Arrays.equals(this.entityList, other.getEntityList()))) &&
            ((this.attributeList==null && other.getAttributeList()==null) || 
             (this.attributeList!=null &&
              java.util.Arrays.equals(this.attributeList, other.getAttributeList())));
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
        _hashCode += (isWorkflowTypeID() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getWorkflowTitle() != null) {
            _hashCode += getWorkflowTitle().hashCode();
        }
        if (getUserID() != null) {
            _hashCode += getUserID().hashCode();
        }
        if (getRequester() != null) {
            _hashCode += getRequester().hashCode();
        }
        if (getEntityList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEntityList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEntityList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAttributeList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttributeList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttributeList(), i);
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
        new org.apache.axis.description.TypeDesc(NewWorkflowTypeEditDataRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditDataRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowTypeID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "WorkflowTypeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowTitle");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "WorkflowTitle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "UserID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requester");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Requester"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "Requester"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "EntityArray"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "Entity"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributeList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "AttributeList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeArray"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "Attribute"));
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
