/**
 * EntityArray.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class EntityArray  implements java.io.Serializable {
    private java.lang.String entityID;

    private br.andrew.sap.workflow.AttributeData[] entityAttributeList;

    private br.andrew.sap.workflow.RelationshipArrayList[] relationshipList;

    private br.andrew.sap.workflow.AttributeFileData[] entityAttributeFileList;

    public EntityArray() {
    }

    public EntityArray(
           java.lang.String entityID,
           br.andrew.sap.workflow.AttributeData[] entityAttributeList,
           br.andrew.sap.workflow.RelationshipArrayList[] relationshipList,
           br.andrew.sap.workflow.AttributeFileData[] entityAttributeFileList) {
           this.entityID = entityID;
           this.entityAttributeList = entityAttributeList;
           this.relationshipList = relationshipList;
           this.entityAttributeFileList = entityAttributeFileList;
    }


    /**
     * Gets the entityID value for this EntityArray.
     * 
     * @return entityID
     */
    public java.lang.String getEntityID() {
        return entityID;
    }


    /**
     * Sets the entityID value for this EntityArray.
     * 
     * @param entityID
     */
    public void setEntityID(java.lang.String entityID) {
        this.entityID = entityID;
    }


    /**
     * Gets the entityAttributeList value for this EntityArray.
     * 
     * @return entityAttributeList
     */
    public br.andrew.sap.workflow.AttributeData[] getEntityAttributeList() {
        return entityAttributeList;
    }


    /**
     * Sets the entityAttributeList value for this EntityArray.
     * 
     * @param entityAttributeList
     */
    public void setEntityAttributeList(br.andrew.sap.workflow.AttributeData[] entityAttributeList) {
        this.entityAttributeList = entityAttributeList;
    }


    /**
     * Gets the relationshipList value for this EntityArray.
     * 
     * @return relationshipList
     */
    public br.andrew.sap.workflow.RelationshipArrayList[] getRelationshipList() {
        return relationshipList;
    }


    /**
     * Sets the relationshipList value for this EntityArray.
     * 
     * @param relationshipList
     */
    public void setRelationshipList(br.andrew.sap.workflow.RelationshipArrayList[] relationshipList) {
        this.relationshipList = relationshipList;
    }


    /**
     * Gets the entityAttributeFileList value for this EntityArray.
     * 
     * @return entityAttributeFileList
     */
    public br.andrew.sap.workflow.AttributeFileData[] getEntityAttributeFileList() {
        return entityAttributeFileList;
    }


    /**
     * Sets the entityAttributeFileList value for this EntityArray.
     * 
     * @param entityAttributeFileList
     */
    public void setEntityAttributeFileList(br.andrew.sap.workflow.AttributeFileData[] entityAttributeFileList) {
        this.entityAttributeFileList = entityAttributeFileList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EntityArray)) return false;
        EntityArray other = (EntityArray) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.entityID==null && other.getEntityID()==null) || 
             (this.entityID!=null &&
              this.entityID.equals(other.getEntityID()))) &&
            ((this.entityAttributeList==null && other.getEntityAttributeList()==null) || 
             (this.entityAttributeList!=null &&
              java.util.Arrays.equals(this.entityAttributeList, other.getEntityAttributeList()))) &&
            ((this.relationshipList==null && other.getRelationshipList()==null) || 
             (this.relationshipList!=null &&
              java.util.Arrays.equals(this.relationshipList, other.getRelationshipList()))) &&
            ((this.entityAttributeFileList==null && other.getEntityAttributeFileList()==null) || 
             (this.entityAttributeFileList!=null &&
              java.util.Arrays.equals(this.entityAttributeFileList, other.getEntityAttributeFileList())));
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
        if (getEntityID() != null) {
            _hashCode += getEntityID().hashCode();
        }
        if (getEntityAttributeList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEntityAttributeList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEntityAttributeList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRelationshipList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRelationshipList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRelationshipList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getEntityAttributeFileList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEntityAttributeFileList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEntityAttributeFileList(), i);
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
        new org.apache.axis.description.TypeDesc(EntityArray.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:workflow", "EntityArray"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityAttributeList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityAttributeList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "EntityAttribute"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("relationshipList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "RelationshipList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "RelationshipArrayList"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "Relationship"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityAttributeFileList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:workflow", "EntityAttributeFileList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:workflow", "attributeFileData"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:workflow", "EntityAttributeFile"));
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
