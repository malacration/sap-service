/**
 * Requester.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class Requester  implements java.io.Serializable {
    private br.andrew.sap.workflow.RequesterAsUser user;

    private br.andrew.sap.workflow.RequesterAsCustomer customer;

    public Requester() {
    }

    public Requester(
           br.andrew.sap.workflow.RequesterAsUser user,
           br.andrew.sap.workflow.RequesterAsCustomer customer) {
           this.user = user;
           this.customer = customer;
    }


    /**
     * Gets the user value for this Requester.
     * 
     * @return user
     */
    public br.andrew.sap.workflow.RequesterAsUser getUser() {
        return user;
    }


    /**
     * Sets the user value for this Requester.
     * 
     * @param user
     */
    public void setUser(br.andrew.sap.workflow.RequesterAsUser user) {
        this.user = user;
    }


    /**
     * Gets the customer value for this Requester.
     * 
     * @return customer
     */
    public br.andrew.sap.workflow.RequesterAsCustomer getCustomer() {
        return customer;
    }


    /**
     * Sets the customer value for this Requester.
     * 
     * @param customer
     */
    public void setCustomer(br.andrew.sap.workflow.RequesterAsCustomer customer) {
        this.customer = customer;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Requester)) return false;
        Requester other = (Requester) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.user==null && other.getUser()==null) || 
             (this.user!=null &&
              this.user.equals(other.getUser()))) &&
            ((this.customer==null && other.getCustomer()==null) || 
             (this.customer!=null &&
              this.customer.equals(other.getCustomer())));
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
        if (getUser() != null) {
            _hashCode += getUser().hashCode();
        }
        if (getCustomer() != null) {
            _hashCode += getCustomer().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Requester.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "Requester"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("user");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "User"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "RequesterAsUser"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customer");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "Customer"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "RequesterAsCustomer"));
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
