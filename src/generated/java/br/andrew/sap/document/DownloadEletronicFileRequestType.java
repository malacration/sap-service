/**
 * DownloadEletronicFileRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DownloadEletronicFileRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String idrevision;

    private java.lang.String iduser;

    private java.lang.String fgconverttopdf;

    private java.lang.String idcategory;

    private java.lang.String fgwatermark;

    private java.lang.String nmfile;

    private java.lang.String fgfilelink;

    public DownloadEletronicFileRequestType() {
    }

    public DownloadEletronicFileRequestType(
           java.lang.String iddocument,
           java.lang.String idrevision,
           java.lang.String iduser,
           java.lang.String fgconverttopdf,
           java.lang.String idcategory,
           java.lang.String fgwatermark,
           java.lang.String nmfile,
           java.lang.String fgfilelink) {
           this.iddocument = iddocument;
           this.idrevision = idrevision;
           this.iduser = iduser;
           this.fgconverttopdf = fgconverttopdf;
           this.idcategory = idcategory;
           this.fgwatermark = fgwatermark;
           this.nmfile = nmfile;
           this.fgfilelink = fgfilelink;
    }


    /**
     * Gets the iddocument value for this DownloadEletronicFileRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this DownloadEletronicFileRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the idrevision value for this DownloadEletronicFileRequestType.
     * 
     * @return idrevision
     */
    public java.lang.String getIdrevision() {
        return idrevision;
    }


    /**
     * Sets the idrevision value for this DownloadEletronicFileRequestType.
     * 
     * @param idrevision
     */
    public void setIdrevision(java.lang.String idrevision) {
        this.idrevision = idrevision;
    }


    /**
     * Gets the iduser value for this DownloadEletronicFileRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this DownloadEletronicFileRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the fgconverttopdf value for this DownloadEletronicFileRequestType.
     * 
     * @return fgconverttopdf
     */
    public java.lang.String getFgconverttopdf() {
        return fgconverttopdf;
    }


    /**
     * Sets the fgconverttopdf value for this DownloadEletronicFileRequestType.
     * 
     * @param fgconverttopdf
     */
    public void setFgconverttopdf(java.lang.String fgconverttopdf) {
        this.fgconverttopdf = fgconverttopdf;
    }


    /**
     * Gets the idcategory value for this DownloadEletronicFileRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this DownloadEletronicFileRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }


    /**
     * Gets the fgwatermark value for this DownloadEletronicFileRequestType.
     * 
     * @return fgwatermark
     */
    public java.lang.String getFgwatermark() {
        return fgwatermark;
    }


    /**
     * Sets the fgwatermark value for this DownloadEletronicFileRequestType.
     * 
     * @param fgwatermark
     */
    public void setFgwatermark(java.lang.String fgwatermark) {
        this.fgwatermark = fgwatermark;
    }


    /**
     * Gets the nmfile value for this DownloadEletronicFileRequestType.
     * 
     * @return nmfile
     */
    public java.lang.String getNmfile() {
        return nmfile;
    }


    /**
     * Sets the nmfile value for this DownloadEletronicFileRequestType.
     * 
     * @param nmfile
     */
    public void setNmfile(java.lang.String nmfile) {
        this.nmfile = nmfile;
    }


    /**
     * Gets the fgfilelink value for this DownloadEletronicFileRequestType.
     * 
     * @return fgfilelink
     */
    public java.lang.String getFgfilelink() {
        return fgfilelink;
    }


    /**
     * Sets the fgfilelink value for this DownloadEletronicFileRequestType.
     * 
     * @param fgfilelink
     */
    public void setFgfilelink(java.lang.String fgfilelink) {
        this.fgfilelink = fgfilelink;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DownloadEletronicFileRequestType)) return false;
        DownloadEletronicFileRequestType other = (DownloadEletronicFileRequestType) obj;
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
            ((this.fgconverttopdf==null && other.getFgconverttopdf()==null) || 
             (this.fgconverttopdf!=null &&
              this.fgconverttopdf.equals(other.getFgconverttopdf()))) &&
            ((this.idcategory==null && other.getIdcategory()==null) || 
             (this.idcategory!=null &&
              this.idcategory.equals(other.getIdcategory()))) &&
            ((this.fgwatermark==null && other.getFgwatermark()==null) || 
             (this.fgwatermark!=null &&
              this.fgwatermark.equals(other.getFgwatermark()))) &&
            ((this.nmfile==null && other.getNmfile()==null) || 
             (this.nmfile!=null &&
              this.nmfile.equals(other.getNmfile()))) &&
            ((this.fgfilelink==null && other.getFgfilelink()==null) || 
             (this.fgfilelink!=null &&
              this.fgfilelink.equals(other.getFgfilelink())));
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
        if (getFgconverttopdf() != null) {
            _hashCode += getFgconverttopdf().hashCode();
        }
        if (getIdcategory() != null) {
            _hashCode += getIdcategory().hashCode();
        }
        if (getFgwatermark() != null) {
            _hashCode += getFgwatermark().hashCode();
        }
        if (getNmfile() != null) {
            _hashCode += getNmfile().hashCode();
        }
        if (getFgfilelink() != null) {
            _hashCode += getFgfilelink().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DownloadEletronicFileRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "downloadEletronicFileRequestType"));
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
        elemField.setFieldName("fgconverttopdf");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "fgconverttopdf"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("idcategory");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idcategory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fgwatermark");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "fgwatermark"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nmfile");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "nmfile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fgfilelink");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "fgfilelink"));
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
