/**
 * ReactivateWorkflowRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class ReactivateWorkflowRequestType  implements java.io.Serializable {
    private java.lang.String[] reactivateWorkflowList;

    private java.lang.String explanation;

    private java.lang.String userID;

    public ReactivateWorkflowRequestType() {
    }

    public ReactivateWorkflowRequestType(
           java.lang.String[] reactivateWorkflowList,
           java.lang.String explanation,
           java.lang.String userID) {
           this.reactivateWorkflowList = reactivateWorkflowList;
           this.explanation = explanation;
           this.userID = userID;
    }


    /**
     * Gets the reactivateWorkflowList value for this ReactivateWorkflowRequestType.
     * 
     * @return reactivateWorkflowList
     */
    public java.lang.String[] getReactivateWorkflowList() {
        return reactivateWorkflowList;
    }


    /**
     * Sets the reactivateWorkflowList value for this ReactivateWorkflowRequestType.
     * 
     * @param reactivateWorkflowList
     */
    public void setReactivateWorkflowList(java.lang.String[] reactivateWorkflowList) {
        this.reactivateWorkflowList = reactivateWorkflowList;
    }


    /**
     * Gets the explanation value for this ReactivateWorkflowRequestType.
     * 
     * @return explanation
     */
    public java.lang.String getExplanation() {
        return explanation;
    }


    /**
     * Sets the explanation value for this ReactivateWorkflowRequestType.
     * 
     * @param explanation
     */
    public void setExplanation(java.lang.String explanation) {
        this.explanation = explanation;
    }


    /**
     * Gets the userID value for this ReactivateWorkflowRequestType.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this ReactivateWorkflowRequestType.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReactivateWorkflowRequestType)) return false;
        ReactivateWorkflowRequestType other = (ReactivateWorkflowRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.reactivateWorkflowList==null && other.getReactivateWorkflowList()==null) || 
             (this.reactivateWorkflowList!=null &&
              java.util.Arrays.equals(this.reactivateWorkflowList, other.getReactivateWorkflowList()))) &&
            ((this.explanation==null && other.getExplanation()==null) || 
             (this.explanation!=null &&
              this.explanation.equals(other.getExplanation()))) &&
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
        if (getReactivateWorkflowList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getReactivateWorkflowList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getReactivateWorkflowList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getExplanation() != null) {
            _hashCode += getExplanation().hashCode();
        }
        if (getUserID() != null) {
            _hashCode += getUserID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReactivateWorkflowRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reactivateWorkflowList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "ReactivateWorkflowList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "WorkflowID"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("explanation");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Explanation"));
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
