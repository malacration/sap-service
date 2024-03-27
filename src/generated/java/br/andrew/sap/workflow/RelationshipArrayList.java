/**
 * RelationshipArrayList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class RelationshipArrayList  implements java.io.Serializable {
    private java.lang.String relationshipID;

    private br.andrew.sap.workflow.RelationshipAttributeData[][] relationshipAttributeList;

    public RelationshipArrayList() {
    }

    public RelationshipArrayList(
           java.lang.String relationshipID,
           br.andrew.sap.workflow.RelationshipAttributeData[][] relationshipAttributeList) {
           this.relationshipID = relationshipID;
           this.relationshipAttributeList = relationshipAttributeList;
    }


    /**
     * Gets the relationshipID value for this RelationshipArrayList.
     * 
     * @return relationshipID
     */
    public java.lang.String getRelationshipID() {
        return relationshipID;
    }


    /**
     * Sets the relationshipID value for this RelationshipArrayList.
     * 
     * @param relationshipID
     */
    public void setRelationshipID(java.lang.String relationshipID) {
        this.relationshipID = relationshipID;
    }


    /**
     * Gets the relationshipAttributeList value for this RelationshipArrayList.
     * 
     * @return relationshipAttributeList
     */
    public br.andrew.sap.workflow.RelationshipAttributeData[][] getRelationshipAttributeList() {
        return relationshipAttributeList;
    }


    /**
     * Sets the relationshipAttributeList value for this RelationshipArrayList.
     * 
     * @param relationshipAttributeList
     */
    public void setRelationshipAttributeList(br.andrew.sap.workflow.RelationshipAttributeData[][] relationshipAttributeList) {
        this.relationshipAttributeList = relationshipAttributeList;
    }

    public br.andrew.sap.workflow.RelationshipAttributeData[] getRelationshipAttributeList(int i) {
        return this.relationshipAttributeList[i];
    }

    public void setRelationshipAttributeList(int i, br.andrew.sap.workflow.RelationshipAttributeData[] _value) {
        this.relationshipAttributeList[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RelationshipArrayList)) return false;
        RelationshipArrayList other = (RelationshipArrayList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.relationshipID==null && other.getRelationshipID()==null) || 
             (this.relationshipID!=null &&
              this.relationshipID.equals(other.getRelationshipID()))) &&
            ((this.relationshipAttributeList==null && other.getRelationshipAttributeList()==null) || 
             (this.relationshipAttributeList!=null &&
              java.util.Arrays.equals(this.relationshipAttributeList, other.getRelationshipAttributeList())));
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
        if (getRelationshipID() != null) {
            _hashCode += getRelationshipID().hashCode();
        }
        if (getRelationshipAttributeList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRelationshipAttributeList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRelationshipAttributeList(), i);
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
        new org.apache.axis.description.TypeDesc(RelationshipArrayList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "RelationshipArrayList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipAttributeList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipAttributeList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "RelationshipListAttributeData"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
