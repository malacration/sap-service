/**
 * ReleasePendencyDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class ReleasePendencyDocumentRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String iduser;

    private int approv;

    private java.lang.String justify;

    private java.lang.String idcategory;

    public ReleasePendencyDocumentRequestType() {
    }

    public ReleasePendencyDocumentRequestType(
           java.lang.String iddocument,
           java.lang.String iduser,
           int approv,
           java.lang.String justify,
           java.lang.String idcategory) {
           this.iddocument = iddocument;
           this.iduser = iduser;
           this.approv = approv;
           this.justify = justify;
           this.idcategory = idcategory;
    }


    /**
     * Gets the iddocument value for this ReleasePendencyDocumentRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this ReleasePendencyDocumentRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the iduser value for this ReleasePendencyDocumentRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this ReleasePendencyDocumentRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the approv value for this ReleasePendencyDocumentRequestType.
     * 
     * @return approv
     */
    public int getApprov() {
        return approv;
    }


    /**
     * Sets the approv value for this ReleasePendencyDocumentRequestType.
     * 
     * @param approv
     */
    public void setApprov(int approv) {
        this.approv = approv;
    }


    /**
     * Gets the justify value for this ReleasePendencyDocumentRequestType.
     * 
     * @return justify
     */
    public java.lang.String getJustify() {
        return justify;
    }


    /**
     * Sets the justify value for this ReleasePendencyDocumentRequestType.
     * 
     * @param justify
     */
    public void setJustify(java.lang.String justify) {
        this.justify = justify;
    }


    /**
     * Gets the idcategory value for this ReleasePendencyDocumentRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this ReleasePendencyDocumentRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReleasePendencyDocumentRequestType)) return false;
        ReleasePendencyDocumentRequestType other = (ReleasePendencyDocumentRequestType) obj;
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
            ((this.iduser==null && other.getIduser()==null) || 
             (this.iduser!=null &&
              this.iduser.equals(other.getIduser()))) &&
            this.approv == other.getApprov() &&
            ((this.justify==null && other.getJustify()==null) || 
             (this.justify!=null &&
              this.justify.equals(other.getJustify()))) &&
            ((this.idcategory==null && other.getIdcategory()==null) || 
             (this.idcategory!=null &&
              this.idcategory.equals(other.getIdcategory())));
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
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        _hashCode += getApprov();
        if (getJustify() != null) {
            _hashCode += getJustify().hashCode();
        }
        if (getIdcategory() != null) {
            _hashCode += getIdcategory().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReleasePendencyDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iddocument");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iddocument"));
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
        elemField.setFieldName("approv");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "approv"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("justify");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "justify"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("idcategory");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idcategory"));
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
