/**
 * NewWorkflowResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class NewWorkflowResponseType  implements java.io.Serializable {
    private java.lang.String status;

    private java.math.BigInteger code;

    private java.lang.String detail;

    private java.lang.String recordKey;

    private java.lang.String recordID;

    public NewWorkflowResponseType() {
    }

    public NewWorkflowResponseType(
           java.lang.String status,
           java.math.BigInteger code,
           java.lang.String detail,
           java.lang.String recordKey,
           java.lang.String recordID) {
           this.status = status;
           this.code = code;
           this.detail = detail;
           this.recordKey = recordKey;
           this.recordID = recordID;
    }


    /**
     * Gets the status value for this NewWorkflowResponseType.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this NewWorkflowResponseType.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the code value for this NewWorkflowResponseType.
     * 
     * @return code
     */
    public java.math.BigInteger getCode() {
        return code;
    }


    /**
     * Sets the code value for this NewWorkflowResponseType.
     * 
     * @param code
     */
    public void setCode(java.math.BigInteger code) {
        this.code = code;
    }


    /**
     * Gets the detail value for this NewWorkflowResponseType.
     * 
     * @return detail
     */
    public java.lang.String getDetail() {
        return detail;
    }


    /**
     * Sets the detail value for this NewWorkflowResponseType.
     * 
     * @param detail
     */
    public void setDetail(java.lang.String detail) {
        this.detail = detail;
    }


    /**
     * Gets the recordKey value for this NewWorkflowResponseType.
     * 
     * @return recordKey
     */
    public java.lang.String getRecordKey() {
        return recordKey;
    }


    /**
     * Sets the recordKey value for this NewWorkflowResponseType.
     * 
     * @param recordKey
     */
    public void setRecordKey(java.lang.String recordKey) {
        this.recordKey = recordKey;
    }


    /**
     * Gets the recordID value for this NewWorkflowResponseType.
     * 
     * @return recordID
     */
    public java.lang.String getRecordID() {
        return recordID;
    }


    /**
     * Sets the recordID value for this NewWorkflowResponseType.
     * 
     * @param recordID
     */
    public void setRecordID(java.lang.String recordID) {
        this.recordID = recordID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewWorkflowResponseType)) return false;
        NewWorkflowResponseType other = (NewWorkflowResponseType) obj;
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
            ((this.recordKey==null && other.getRecordKey()==null) || 
             (this.recordKey!=null &&
              this.recordKey.equals(other.getRecordKey()))) &&
            ((this.recordID==null && other.getRecordID()==null) || 
             (this.recordID!=null &&
              this.recordID.equals(other.getRecordID())));
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
        if (getRecordKey() != null) {
            _hashCode += getRecordKey().hashCode();
        }
        if (getRecordID() != null) {
            _hashCode += getRecordID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewWorkflowResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "newWorkflowResponseType"));
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
        elemField.setFieldName("recordKey");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RecordKey"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recordID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RecordID"));
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
