/**
 * MembersData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class MembersData  implements java.io.Serializable {
    private int memberType;

    private java.lang.String memberID;

    private java.lang.String step;

    private int sequence;

    private int deadline;

    public MembersData() {
    }

    public MembersData(
           int memberType,
           java.lang.String memberID,
           java.lang.String step,
           int sequence,
           int deadline) {
           this.memberType = memberType;
           this.memberID = memberID;
           this.step = step;
           this.sequence = sequence;
           this.deadline = deadline;
    }


    /**
     * Gets the memberType value for this MembersData.
     * 
     * @return memberType
     */
    public int getMemberType() {
        return memberType;
    }


    /**
     * Sets the memberType value for this MembersData.
     * 
     * @param memberType
     */
    public void setMemberType(int memberType) {
        this.memberType = memberType;
    }


    /**
     * Gets the memberID value for this MembersData.
     * 
     * @return memberID
     */
    public java.lang.String getMemberID() {
        return memberID;
    }


    /**
     * Sets the memberID value for this MembersData.
     * 
     * @param memberID
     */
    public void setMemberID(java.lang.String memberID) {
        this.memberID = memberID;
    }


    /**
     * Gets the step value for this MembersData.
     * 
     * @return step
     */
    public java.lang.String getStep() {
        return step;
    }


    /**
     * Sets the step value for this MembersData.
     * 
     * @param step
     */
    public void setStep(java.lang.String step) {
        this.step = step;
    }


    /**
     * Gets the sequence value for this MembersData.
     * 
     * @return sequence
     */
    public int getSequence() {
        return sequence;
    }


    /**
     * Sets the sequence value for this MembersData.
     * 
     * @param sequence
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }


    /**
     * Gets the deadline value for this MembersData.
     * 
     * @return deadline
     */
    public int getDeadline() {
        return deadline;
    }


    /**
     * Sets the deadline value for this MembersData.
     * 
     * @param deadline
     */
    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MembersData)) return false;
        MembersData other = (MembersData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.memberType == other.getMemberType() &&
            ((this.memberID==null && other.getMemberID()==null) || 
             (this.memberID!=null &&
              this.memberID.equals(other.getMemberID()))) &&
            ((this.step==null && other.getStep()==null) || 
             (this.step!=null &&
              this.step.equals(other.getStep()))) &&
            this.sequence == other.getSequence() &&
            this.deadline == other.getDeadline();
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
        _hashCode += getMemberType();
        if (getMemberID() != null) {
            _hashCode += getMemberID().hashCode();
        }
        if (getStep() != null) {
            _hashCode += getStep().hashCode();
        }
        _hashCode += getSequence();
        _hashCode += getDeadline();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MembersData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "MembersData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memberType");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "MemberType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memberID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "MemberID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("step");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Step"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sequence");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Sequence"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deadline");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Deadline"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
