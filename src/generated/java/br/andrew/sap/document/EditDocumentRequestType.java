/**
 * EditDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class EditDocumentRequestType  implements java.io.Serializable {
    private java.lang.String idcategory;

    private java.lang.String iddocument;

    private java.lang.String iduser;

    private java.lang.String idrevision;

    private java.lang.String title;

    private java.lang.String summary;

    private java.lang.String iduserresp;

    public EditDocumentRequestType() {
    }

    public EditDocumentRequestType(
           java.lang.String idcategory,
           java.lang.String iddocument,
           java.lang.String iduser,
           java.lang.String idrevision,
           java.lang.String title,
           java.lang.String summary,
           java.lang.String iduserresp) {
           this.idcategory = idcategory;
           this.iddocument = iddocument;
           this.iduser = iduser;
           this.idrevision = idrevision;
           this.title = title;
           this.summary = summary;
           this.iduserresp = iduserresp;
    }


    /**
     * Gets the idcategory value for this EditDocumentRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this EditDocumentRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }


    /**
     * Gets the iddocument value for this EditDocumentRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this EditDocumentRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the iduser value for this EditDocumentRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this EditDocumentRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the idrevision value for this EditDocumentRequestType.
     * 
     * @return idrevision
     */
    public java.lang.String getIdrevision() {
        return idrevision;
    }


    /**
     * Sets the idrevision value for this EditDocumentRequestType.
     * 
     * @param idrevision
     */
    public void setIdrevision(java.lang.String idrevision) {
        this.idrevision = idrevision;
    }


    /**
     * Gets the title value for this EditDocumentRequestType.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this EditDocumentRequestType.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the summary value for this EditDocumentRequestType.
     * 
     * @return summary
     */
    public java.lang.String getSummary() {
        return summary;
    }


    /**
     * Sets the summary value for this EditDocumentRequestType.
     * 
     * @param summary
     */
    public void setSummary(java.lang.String summary) {
        this.summary = summary;
    }


    /**
     * Gets the iduserresp value for this EditDocumentRequestType.
     * 
     * @return iduserresp
     */
    public java.lang.String getIduserresp() {
        return iduserresp;
    }


    /**
     * Sets the iduserresp value for this EditDocumentRequestType.
     * 
     * @param iduserresp
     */
    public void setIduserresp(java.lang.String iduserresp) {
        this.iduserresp = iduserresp;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EditDocumentRequestType)) return false;
        EditDocumentRequestType other = (EditDocumentRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.idcategory==null && other.getIdcategory()==null) || 
             (this.idcategory!=null &&
              this.idcategory.equals(other.getIdcategory()))) &&
            ((this.iddocument==null && other.getIddocument()==null) || 
             (this.iddocument!=null &&
              this.iddocument.equals(other.getIddocument()))) &&
            ((this.iduser==null && other.getIduser()==null) || 
             (this.iduser!=null &&
              this.iduser.equals(other.getIduser()))) &&
            ((this.idrevision==null && other.getIdrevision()==null) || 
             (this.idrevision!=null &&
              this.idrevision.equals(other.getIdrevision()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.summary==null && other.getSummary()==null) || 
             (this.summary!=null &&
              this.summary.equals(other.getSummary()))) &&
            ((this.iduserresp==null && other.getIduserresp()==null) || 
             (this.iduserresp!=null &&
              this.iduserresp.equals(other.getIduserresp())));
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
        if (getIdcategory() != null) {
            _hashCode += getIdcategory().hashCode();
        }
        if (getIddocument() != null) {
            _hashCode += getIddocument().hashCode();
        }
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        if (getIdrevision() != null) {
            _hashCode += getIdrevision().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getSummary() != null) {
            _hashCode += getSummary().hashCode();
        }
        if (getIduserresp() != null) {
            _hashCode += getIduserresp().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(EditDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "editDocumentRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("idcategory");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idcategory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("idrevision");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idrevision"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("summary");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "summary"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iduserresp");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "iduserresp"));
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
