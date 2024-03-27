/**
 * SearchDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class SearchDocumentRequestType  implements java.io.Serializable {
    private br.andrew.sap.document.SearchDocumentFilter filters;

    private java.lang.String iduser;

    private br.andrew.sap.document.AttributeData[] attributes;

    public SearchDocumentRequestType() {
    }

    public SearchDocumentRequestType(
           br.andrew.sap.document.SearchDocumentFilter filters,
           java.lang.String iduser,
           br.andrew.sap.document.AttributeData[] attributes) {
           this.filters = filters;
           this.iduser = iduser;
           this.attributes = attributes;
    }


    /**
     * Gets the filters value for this SearchDocumentRequestType.
     * 
     * @return filters
     */
    public br.andrew.sap.document.SearchDocumentFilter getFilters() {
        return filters;
    }


    /**
     * Sets the filters value for this SearchDocumentRequestType.
     * 
     * @param filters
     */
    public void setFilters(br.andrew.sap.document.SearchDocumentFilter filters) {
        this.filters = filters;
    }


    /**
     * Gets the iduser value for this SearchDocumentRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this SearchDocumentRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the attributes value for this SearchDocumentRequestType.
     * 
     * @return attributes
     */
    public br.andrew.sap.document.AttributeData[] getAttributes() {
        return attributes;
    }


    /**
     * Sets the attributes value for this SearchDocumentRequestType.
     * 
     * @param attributes
     */
    public void setAttributes(br.andrew.sap.document.AttributeData[] attributes) {
        this.attributes = attributes;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SearchDocumentRequestType)) return false;
        SearchDocumentRequestType other = (SearchDocumentRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.filters==null && other.getFilters()==null) || 
             (this.filters!=null &&
              this.filters.equals(other.getFilters()))) &&
            ((this.iduser==null && other.getIduser()==null) || 
             (this.iduser!=null &&
              this.iduser.equals(other.getIduser()))) &&
            ((this.attributes==null && other.getAttributes()==null) || 
             (this.attributes!=null &&
              java.util.Arrays.equals(this.attributes, other.getAttributes())));
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
        if (getFilters() != null) {
            _hashCode += getFilters().hashCode();
        }
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        if (getAttributes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttributes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttributes(), i);
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
        new org.apache.axis.description.TypeDesc(SearchDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "searchDocumentRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filters");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "filters"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "searchDocumentFilter"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iduser");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iduser"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "attributes"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "attributeData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
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
