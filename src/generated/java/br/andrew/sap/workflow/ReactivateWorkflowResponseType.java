/**
 * ReactivateWorkflowResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class ReactivateWorkflowResponseType  implements java.io.Serializable {
    private java.lang.String status;

    private java.math.BigInteger code;

    private java.lang.String detail;

    private java.lang.String[] recordListResponse;

    public ReactivateWorkflowResponseType() {
    }

    public ReactivateWorkflowResponseType(
           java.lang.String status,
           java.math.BigInteger code,
           java.lang.String detail,
           java.lang.String[] recordListResponse) {
           this.status = status;
           this.code = code;
           this.detail = detail;
           this.recordListResponse = recordListResponse;
    }


    /**
     * Gets the status value for this ReactivateWorkflowResponseType.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this ReactivateWorkflowResponseType.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the code value for this ReactivateWorkflowResponseType.
     * 
     * @return code
     */
    public java.math.BigInteger getCode() {
        return code;
    }


    /**
     * Sets the code value for this ReactivateWorkflowResponseType.
     * 
     * @param code
     */
    public void setCode(java.math.BigInteger code) {
        this.code = code;
    }


    /**
     * Gets the detail value for this ReactivateWorkflowResponseType.
     * 
     * @return detail
     */
    public java.lang.String getDetail() {
        return detail;
    }


    /**
     * Sets the detail value for this ReactivateWorkflowResponseType.
     * 
     * @param detail
     */
    public void setDetail(java.lang.String detail) {
        this.detail = detail;
    }


    /**
     * Gets the recordListResponse value for this ReactivateWorkflowResponseType.
     * 
     * @return recordListResponse
     */
    public java.lang.String[] getRecordListResponse() {
        return recordListResponse;
    }


    /**
     * Sets the recordListResponse value for this ReactivateWorkflowResponseType.
     * 
     * @param recordListResponse
     */
    public void setRecordListResponse(java.lang.String[] recordListResponse) {
        this.recordListResponse = recordListResponse;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReactivateWorkflowResponseType)) return false;
        ReactivateWorkflowResponseType other = (ReactivateWorkflowResponseType) obj;
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
            ((this.recordListResponse==null && other.getRecordListResponse()==null) || 
             (this.recordListResponse!=null &&
              java.util.Arrays.equals(this.recordListResponse, other.getRecordListResponse())));
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
        if (getRecordListResponse() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRecordListResponse());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRecordListResponse(), i);
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
        new org.apache.axis.description.TypeDesc(ReactivateWorkflowResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowResponseType"));
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
        elemField.setFieldName("recordListResponse");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RecordListResponse"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "WorkflowID"));
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
