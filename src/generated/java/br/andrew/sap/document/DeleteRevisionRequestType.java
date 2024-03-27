/**
 * DeleteRevisionRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DeleteRevisionRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String idrevision;

    private java.lang.String iduser;

    private java.lang.String justify;

    public DeleteRevisionRequestType() {
    }

    public DeleteRevisionRequestType(
           java.lang.String iddocument,
           java.lang.String idrevision,
           java.lang.String iduser,
           java.lang.String justify) {
           this.iddocument = iddocument;
           this.idrevision = idrevision;
           this.iduser = iduser;
           this.justify = justify;
    }


    /**
     * Gets the iddocument value for this DeleteRevisionRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this DeleteRevisionRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the idrevision value for this DeleteRevisionRequestType.
     * 
     * @return idrevision
     */
    public java.lang.String getIdrevision() {
        return idrevision;
    }


    /**
     * Sets the idrevision value for this DeleteRevisionRequestType.
     * 
     * @param idrevision
     */
    public void setIdrevision(java.lang.String idrevision) {
        this.idrevision = idrevision;
    }


    /**
     * Gets the iduser value for this DeleteRevisionRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this DeleteRevisionRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the justify value for this DeleteRevisionRequestType.
     * 
     * @return justify
     */
    public java.lang.String getJustify() {
        return justify;
    }


    /**
     * Sets the justify value for this DeleteRevisionRequestType.
     * 
     * @param justify
     */
    public void setJustify(java.lang.String justify) {
        this.justify = justify;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeleteRevisionRequestType)) return false;
        DeleteRevisionRequestType other = (DeleteRevisionRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.iddocument==null && other.getIddocument()==null) || 
             (this.iddocument!=null &&
              this.iddocument.equals(other.getIddocument()))) &&
            ((this.idrevision==null && other.getIdrevision()==null) || 
             (this.idrevision!=null &&
              this.idrevision.equals(other.getIdrevision()))) &&
            ((this.iduser==null && other.getIduser()==null) || 
             (this.iduser!=null &&
              this.iduser.equals(other.getIduser()))) &&
            ((this.justify==null && other.getJustify()==null) || 
             (this.justify!=null &&
              this.justify.equals(other.getJustify())));
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
        if (getIddocument() != null) {
            _hashCode += getIddocument().hashCode();
        }
        if (getIdrevision() != null) {
            _hashCode += getIdrevision().hashCode();
        }
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        if (getJustify() != null) {
            _hashCode += getJustify().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DeleteRevisionRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "deleteRevisionRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iddocument");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iddocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("idrevision");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idrevision"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iduser");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iduser"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("justify");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "justify"));
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
