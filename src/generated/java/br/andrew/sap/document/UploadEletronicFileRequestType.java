/**
 * UploadEletronicFileRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class UploadEletronicFileRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String idrevision;

    private java.lang.String iduser;

    private br.andrew.sap.document.EletronicFile[] file;

    private java.lang.String container;

    private java.lang.String idcategory;

    public UploadEletronicFileRequestType() {
    }

    public UploadEletronicFileRequestType(
           java.lang.String iddocument,
           java.lang.String idrevision,
           java.lang.String iduser,
           br.andrew.sap.document.EletronicFile[] file,
           java.lang.String container,
           java.lang.String idcategory) {
           this.iddocument = iddocument;
           this.idrevision = idrevision;
           this.iduser = iduser;
           this.file = file;
           this.container = container;
           this.idcategory = idcategory;
    }


    /**
     * Gets the iddocument value for this UploadEletronicFileRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this UploadEletronicFileRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the idrevision value for this UploadEletronicFileRequestType.
     * 
     * @return idrevision
     */
    public java.lang.String getIdrevision() {
        return idrevision;
    }


    /**
     * Sets the idrevision value for this UploadEletronicFileRequestType.
     * 
     * @param idrevision
     */
    public void setIdrevision(java.lang.String idrevision) {
        this.idrevision = idrevision;
    }


    /**
     * Gets the iduser value for this UploadEletronicFileRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this UploadEletronicFileRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the file value for this UploadEletronicFileRequestType.
     * 
     * @return file
     */
    public br.andrew.sap.document.EletronicFile[] getFile() {
        return file;
    }


    /**
     * Sets the file value for this UploadEletronicFileRequestType.
     * 
     * @param file
     */
    public void setFile(br.andrew.sap.document.EletronicFile[] file) {
        this.file = file;
    }


    /**
     * Gets the container value for this UploadEletronicFileRequestType.
     * 
     * @return container
     */
    public java.lang.String getContainer() {
        return container;
    }


    /**
     * Sets the container value for this UploadEletronicFileRequestType.
     * 
     * @param container
     */
    public void setContainer(java.lang.String container) {
        this.container = container;
    }


    /**
     * Gets the idcategory value for this UploadEletronicFileRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this UploadEletronicFileRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UploadEletronicFileRequestType)) return false;
        UploadEletronicFileRequestType other = (UploadEletronicFileRequestType) obj;
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
            ((this.file==null && other.getFile()==null) || 
             (this.file!=null &&
              java.util.Arrays.equals(this.file, other.getFile()))) &&
            ((this.container==null && other.getContainer()==null) || 
             (this.container!=null &&
              this.container.equals(other.getContainer()))) &&
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
        if (getIdrevision() != null) {
            _hashCode += getIdrevision().hashCode();
        }
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        if (getFile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFile(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getContainer() != null) {
            _hashCode += getContainer().hashCode();
        }
        if (getIdcategory() != null) {
            _hashCode += getIdcategory().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UploadEletronicFileRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "uploadEletronicFileRequestType"));
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
        elemField.setFieldName("file");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "file"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "eletronicFile"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("container");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "container"));
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
