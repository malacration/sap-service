/**
 * NewWorkflowRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class NewWorkflowRequestType  implements java.io.Serializable {
    private java.lang.String processID;

    private java.lang.String workflowTitle;

    private java.lang.String userID;

    public NewWorkflowRequestType() {
    }

    public NewWorkflowRequestType(
           java.lang.String processID,
           java.lang.String workflowTitle,
           java.lang.String userID) {
           this.processID = processID;
           this.workflowTitle = workflowTitle;
           this.userID = userID;
    }


    /**
     * Gets the processID value for this NewWorkflowRequestType.
     * 
     * @return processID
     */
    public java.lang.String getProcessID() {
        return processID;
    }


    /**
     * Sets the processID value for this NewWorkflowRequestType.
     * 
     * @param processID
     */
    public void setProcessID(java.lang.String processID) {
        this.processID = processID;
    }


    /**
     * Gets the workflowTitle value for this NewWorkflowRequestType.
     * 
     * @return workflowTitle
     */
    public java.lang.String getWorkflowTitle() {
        return workflowTitle;
    }


    /**
     * Sets the workflowTitle value for this NewWorkflowRequestType.
     * 
     * @param workflowTitle
     */
    public void setWorkflowTitle(java.lang.String workflowTitle) {
        this.workflowTitle = workflowTitle;
    }


    /**
     * Gets the userID value for this NewWorkflowRequestType.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this NewWorkflowRequestType.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewWorkflowRequestType)) return false;
        NewWorkflowRequestType other = (NewWorkflowRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.processID==null && other.getProcessID()==null) || 
             (this.processID!=null &&
              this.processID.equals(other.getProcessID()))) &&
            ((this.workflowTitle==null && other.getWorkflowTitle()==null) || 
             (this.workflowTitle!=null &&
              this.workflowTitle.equals(other.getWorkflowTitle()))) &&
            ((this.userID==null && other.getUserID()==null) || 
             (this.userID!=null &&
              this.userID.equals(other.getUserID())));
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
        if (getProcessID() != null) {
            _hashCode += getProcessID().hashCode();
        }
        if (getWorkflowTitle() != null) {
            _hashCode += getWorkflowTitle().hashCode();
        }
        if (getUserID() != null) {
            _hashCode += getUserID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewWorkflowRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "newWorkflowRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ProcessID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
