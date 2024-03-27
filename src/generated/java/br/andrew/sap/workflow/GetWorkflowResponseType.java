/**
 * GetWorkflowResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class GetWorkflowResponseType  implements java.io.Serializable {
    private java.lang.String status;

    private java.math.BigInteger code;

    private java.lang.String detail;

    private java.lang.String title;

    private java.lang.String processID;

    private java.lang.String instanceStatus;

    private java.lang.String workflowStatus;

    private java.lang.String starterUser;

    private java.lang.String starterExternalUser;

    private java.lang.String startDate;

    public GetWorkflowResponseType() {
    }

    public GetWorkflowResponseType(
           java.lang.String status,
           java.math.BigInteger code,
           java.lang.String detail,
           java.lang.String title,
           java.lang.String processID,
           java.lang.String instanceStatus,
           java.lang.String workflowStatus,
           java.lang.String starterUser,
           java.lang.String starterExternalUser,
           java.lang.String startDate) {
           this.status = status;
           this.code = code;
           this.detail = detail;
           this.title = title;
           this.processID = processID;
           this.instanceStatus = instanceStatus;
           this.workflowStatus = workflowStatus;
           this.starterUser = starterUser;
           this.starterExternalUser = starterExternalUser;
           this.startDate = startDate;
    }


    /**
     * Gets the status value for this GetWorkflowResponseType.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetWorkflowResponseType.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the code value for this GetWorkflowResponseType.
     * 
     * @return code
     */
    public java.math.BigInteger getCode() {
        return code;
    }


    /**
     * Sets the code value for this GetWorkflowResponseType.
     * 
     * @param code
     */
    public void setCode(java.math.BigInteger code) {
        this.code = code;
    }


    /**
     * Gets the detail value for this GetWorkflowResponseType.
     * 
     * @return detail
     */
    public java.lang.String getDetail() {
        return detail;
    }


    /**
     * Sets the detail value for this GetWorkflowResponseType.
     * 
     * @param detail
     */
    public void setDetail(java.lang.String detail) {
        this.detail = detail;
    }


    /**
     * Gets the title value for this GetWorkflowResponseType.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this GetWorkflowResponseType.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the processID value for this GetWorkflowResponseType.
     * 
     * @return processID
     */
    public java.lang.String getProcessID() {
        return processID;
    }


    /**
     * Sets the processID value for this GetWorkflowResponseType.
     * 
     * @param processID
     */
    public void setProcessID(java.lang.String processID) {
        this.processID = processID;
    }


    /**
     * Gets the instanceStatus value for this GetWorkflowResponseType.
     * 
     * @return instanceStatus
     */
    public java.lang.String getInstanceStatus() {
        return instanceStatus;
    }


    /**
     * Sets the instanceStatus value for this GetWorkflowResponseType.
     * 
     * @param instanceStatus
     */
    public void setInstanceStatus(java.lang.String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }


    /**
     * Gets the workflowStatus value for this GetWorkflowResponseType.
     * 
     * @return workflowStatus
     */
    public java.lang.String getWorkflowStatus() {
        return workflowStatus;
    }


    /**
     * Sets the workflowStatus value for this GetWorkflowResponseType.
     * 
     * @param workflowStatus
     */
    public void setWorkflowStatus(java.lang.String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }


    /**
     * Gets the starterUser value for this GetWorkflowResponseType.
     * 
     * @return starterUser
     */
    public java.lang.String getStarterUser() {
        return starterUser;
    }


    /**
     * Sets the starterUser value for this GetWorkflowResponseType.
     * 
     * @param starterUser
     */
    public void setStarterUser(java.lang.String starterUser) {
        this.starterUser = starterUser;
    }


    /**
     * Gets the starterExternalUser value for this GetWorkflowResponseType.
     * 
     * @return starterExternalUser
     */
    public java.lang.String getStarterExternalUser() {
        return starterExternalUser;
    }


    /**
     * Sets the starterExternalUser value for this GetWorkflowResponseType.
     * 
     * @param starterExternalUser
     */
    public void setStarterExternalUser(java.lang.String starterExternalUser) {
        this.starterExternalUser = starterExternalUser;
    }


    /**
     * Gets the startDate value for this GetWorkflowResponseType.
     * 
     * @return startDate
     */
    public java.lang.String getStartDate() {
        return startDate;
    }


    /**
     * Sets the startDate value for this GetWorkflowResponseType.
     * 
     * @param startDate
     */
    public void setStartDate(java.lang.String startDate) {
        this.startDate = startDate;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetWorkflowResponseType)) return false;
        GetWorkflowResponseType other = (GetWorkflowResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.code==null && other.getCode()==null) || 
             (this.code!=null &&
              this.code.equals(other.getCode()))) &&
            ((this.detail==null && other.getDetail()==null) || 
             (this.detail!=null &&
              this.detail.equals(other.getDetail()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.processID==null && other.getProcessID()==null) || 
             (this.processID!=null &&
              this.processID.equals(other.getProcessID()))) &&
            ((this.instanceStatus==null && other.getInstanceStatus()==null) || 
             (this.instanceStatus!=null &&
              this.instanceStatus.equals(other.getInstanceStatus()))) &&
            ((this.workflowStatus==null && other.getWorkflowStatus()==null) || 
             (this.workflowStatus!=null &&
              this.workflowStatus.equals(other.getWorkflowStatus()))) &&
            ((this.starterUser==null && other.getStarterUser()==null) || 
             (this.starterUser!=null &&
              this.starterUser.equals(other.getStarterUser()))) &&
            ((this.starterExternalUser==null && other.getStarterExternalUser()==null) || 
             (this.starterExternalUser!=null &&
              this.starterExternalUser.equals(other.getStarterExternalUser()))) &&
            ((this.startDate==null && other.getStartDate()==null) || 
             (this.startDate!=null &&
              this.startDate.equals(other.getStartDate())));
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
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getCode() != null) {
            _hashCode += getCode().hashCode();
        }
        if (getDetail() != null) {
            _hashCode += getDetail().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getProcessID() != null) {
            _hashCode += getProcessID().hashCode();
        }
        if (getInstanceStatus() != null) {
            _hashCode += getInstanceStatus().hashCode();
        }
        if (getWorkflowStatus() != null) {
            _hashCode += getWorkflowStatus().hashCode();
        }
        if (getStarterUser() != null) {
            _hashCode += getStarterUser().hashCode();
        }
        if (getStarterExternalUser() != null) {
            _hashCode += getStarterExternalUser().hashCode();
        }
        if (getStartDate() != null) {
            _hashCode += getStartDate().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetWorkflowResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "getWorkflowResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("code");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Code"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("detail");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Detail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ProcessID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("instanceStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "InstanceStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "WorkflowStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("starterUser");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "StarterUser"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("starterExternalUser");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "StarterExternalUser"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startDate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "StartDate"));
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
