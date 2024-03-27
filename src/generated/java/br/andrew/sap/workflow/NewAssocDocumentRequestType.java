/**
 * NewAssocDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class NewAssocDocumentRequestType  implements java.io.Serializable {
    private java.lang.String workflowID;

    private java.lang.String activityID;

    private java.lang.String documentID;

    private java.math.BigInteger activityOrder;

    public NewAssocDocumentRequestType() {
    }

    public NewAssocDocumentRequestType(
           java.lang.String workflowID,
           java.lang.String activityID,
           java.lang.String documentID,
           java.math.BigInteger activityOrder) {
           this.workflowID = workflowID;
           this.activityID = activityID;
           this.documentID = documentID;
           this.activityOrder = activityOrder;
    }


    /**
     * Gets the workflowID value for this NewAssocDocumentRequestType.
     * 
     * @return workflowID
     */
    public java.lang.String getWorkflowID() {
        return workflowID;
    }


    /**
     * Sets the workflowID value for this NewAssocDocumentRequestType.
     * 
     * @param workflowID
     */
    public void setWorkflowID(java.lang.String workflowID) {
        this.workflowID = workflowID;
    }


    /**
     * Gets the activityID value for this NewAssocDocumentRequestType.
     * 
     * @return activityID
     */
    public java.lang.String getActivityID() {
        return activityID;
    }


    /**
     * Sets the activityID value for this NewAssocDocumentRequestType.
     * 
     * @param activityID
     */
    public void setActivityID(java.lang.String activityID) {
        this.activityID = activityID;
    }


    /**
     * Gets the documentID value for this NewAssocDocumentRequestType.
     * 
     * @return documentID
     */
    public java.lang.String getDocumentID() {
        return documentID;
    }


    /**
     * Sets the documentID value for this NewAssocDocumentRequestType.
     * 
     * @param documentID
     */
    public void setDocumentID(java.lang.String documentID) {
        this.documentID = documentID;
    }


    /**
     * Gets the activityOrder value for this NewAssocDocumentRequestType.
     * 
     * @return activityOrder
     */
    public java.math.BigInteger getActivityOrder() {
        return activityOrder;
    }


    /**
     * Sets the activityOrder value for this NewAssocDocumentRequestType.
     * 
     * @param activityOrder
     */
    public void setActivityOrder(java.math.BigInteger activityOrder) {
        this.activityOrder = activityOrder;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewAssocDocumentRequestType)) return false;
        NewAssocDocumentRequestType other = (NewAssocDocumentRequestType) obj;
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
            ((this.activityID==null && other.getActivityID()==null) || 
             (this.activityID!=null &&
              this.activityID.equals(other.getActivityID()))) &&
            ((this.documentID==null && other.getDocumentID()==null) || 
             (this.documentID!=null &&
              this.documentID.equals(other.getDocumentID()))) &&
            ((this.activityOrder==null && other.getActivityOrder()==null) || 
             (this.activityOrder!=null &&
              this.activityOrder.equals(other.getActivityOrder())));
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
        if (getActivityID() != null) {
            _hashCode += getActivityID().hashCode();
        }
        if (getDocumentID() != null) {
            _hashCode += getDocumentID().hashCode();
        }
        if (getActivityOrder() != null) {
            _hashCode += getActivityOrder().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewAssocDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "newAssocDocumentRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "WorkflowID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ActivityID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "DocumentID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityOrder");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ActivityOrder"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
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
