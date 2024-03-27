/**
 * NewAccessExceptionRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class NewAccessExceptionRequestType  implements java.io.Serializable {
    private java.lang.String workflowID;

    private br.andrew.sap.workflow.AccessExceptionData[] accessExceptionList;

    public NewAccessExceptionRequestType() {
    }

    public NewAccessExceptionRequestType(
           java.lang.String workflowID,
           br.andrew.sap.workflow.AccessExceptionData[] accessExceptionList) {
           this.workflowID = workflowID;
           this.accessExceptionList = accessExceptionList;
    }


    /**
     * Gets the workflowID value for this NewAccessExceptionRequestType.
     * 
     * @return workflowID
     */
    public java.lang.String getWorkflowID() {
        return workflowID;
    }


    /**
     * Sets the workflowID value for this NewAccessExceptionRequestType.
     * 
     * @param workflowID
     */
    public void setWorkflowID(java.lang.String workflowID) {
        this.workflowID = workflowID;
    }


    /**
     * Gets the accessExceptionList value for this NewAccessExceptionRequestType.
     * 
     * @return accessExceptionList
     */
    public br.andrew.sap.workflow.AccessExceptionData[] getAccessExceptionList() {
        return accessExceptionList;
    }


    /**
     * Sets the accessExceptionList value for this NewAccessExceptionRequestType.
     * 
     * @param accessExceptionList
     */
    public void setAccessExceptionList(br.andrew.sap.workflow.AccessExceptionData[] accessExceptionList) {
        this.accessExceptionList = accessExceptionList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewAccessExceptionRequestType)) return false;
        NewAccessExceptionRequestType other = (NewAccessExceptionRequestType) obj;
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
            ((this.accessExceptionList==null && other.getAccessExceptionList()==null) || 
             (this.accessExceptionList!=null &&
              java.util.Arrays.equals(this.accessExceptionList, other.getAccessExceptionList())));
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
        if (getAccessExceptionList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAccessExceptionList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAccessExceptionList(), i);
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
        new org.apache.axis.description.TypeDesc(NewAccessExceptionRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "newAccessExceptionRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "WorkflowID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accessExceptionList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "AccessExceptionList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "accessExceptionData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "AccessException"));
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
