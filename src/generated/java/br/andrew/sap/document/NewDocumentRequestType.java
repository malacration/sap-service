/**
 * NewDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewDocumentRequestType  implements java.io.Serializable {
    private java.lang.String idcategory;

    private java.lang.String iddocument;

    private java.lang.String title;

    private java.lang.String dsresume;

    private java.lang.String dtdocument;

    private java.lang.String attributes;

    private java.lang.String iduser;

    private br.andrew.sap.document.ParticipantsData[] participants;

    private java.lang.String fgmodel;

    private br.andrew.sap.document.EletronicFile[] file;

    private br.andrew.sap.document.KeyWordData[] keyword;

    public NewDocumentRequestType() {
    }

    public NewDocumentRequestType(
           java.lang.String idcategory,
           java.lang.String iddocument,
           java.lang.String title,
           java.lang.String dsresume,
           java.lang.String dtdocument,
           java.lang.String attributes,
           java.lang.String iduser,
           br.andrew.sap.document.ParticipantsData[] participants,
           java.lang.String fgmodel,
           br.andrew.sap.document.EletronicFile[] file,
           br.andrew.sap.document.KeyWordData[] keyword) {
           this.idcategory = idcategory;
           this.iddocument = iddocument;
           this.title = title;
           this.dsresume = dsresume;
           this.dtdocument = dtdocument;
           this.attributes = attributes;
           this.iduser = iduser;
           this.participants = participants;
           this.fgmodel = fgmodel;
           this.file = file;
           this.keyword = keyword;
    }


    /**
     * Gets the idcategory value for this NewDocumentRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this NewDocumentRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }


    /**
     * Gets the iddocument value for this NewDocumentRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this NewDocumentRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the title value for this NewDocumentRequestType.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this NewDocumentRequestType.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the dsresume value for this NewDocumentRequestType.
     * 
     * @return dsresume
     */
    public java.lang.String getDsresume() {
        return dsresume;
    }


    /**
     * Sets the dsresume value for this NewDocumentRequestType.
     * 
     * @param dsresume
     */
    public void setDsresume(java.lang.String dsresume) {
        this.dsresume = dsresume;
    }


    /**
     * Gets the dtdocument value for this NewDocumentRequestType.
     * 
     * @return dtdocument
     */
    public java.lang.String getDtdocument() {
        return dtdocument;
    }


    /**
     * Sets the dtdocument value for this NewDocumentRequestType.
     * 
     * @param dtdocument
     */
    public void setDtdocument(java.lang.String dtdocument) {
        this.dtdocument = dtdocument;
    }


    /**
     * Gets the attributes value for this NewDocumentRequestType.
     * 
     * @return attributes
     */
    public java.lang.String getAttributes() {
        return attributes;
    }


    /**
     * Sets the attributes value for this NewDocumentRequestType.
     * 
     * @param attributes
     */
    public void setAttributes(java.lang.String attributes) {
        this.attributes = attributes;
    }


    /**
     * Gets the iduser value for this NewDocumentRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this NewDocumentRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the participants value for this NewDocumentRequestType.
     * 
     * @return participants
     */
    public br.andrew.sap.document.ParticipantsData[] getParticipants() {
        return participants;
    }


    /**
     * Sets the participants value for this NewDocumentRequestType.
     * 
     * @param participants
     */
    public void setParticipants(br.andrew.sap.document.ParticipantsData[] participants) {
        this.participants = participants;
    }


    /**
     * Gets the fgmodel value for this NewDocumentRequestType.
     * 
     * @return fgmodel
     */
    public java.lang.String getFgmodel() {
        return fgmodel;
    }


    /**
     * Sets the fgmodel value for this NewDocumentRequestType.
     * 
     * @param fgmodel
     */
    public void setFgmodel(java.lang.String fgmodel) {
        this.fgmodel = fgmodel;
    }


    /**
     * Gets the file value for this NewDocumentRequestType.
     * 
     * @return file
     */
    public br.andrew.sap.document.EletronicFile[] getFile() {
        return file;
    }


    /**
     * Sets the file value for this NewDocumentRequestType.
     * 
     * @param file
     */
    public void setFile(br.andrew.sap.document.EletronicFile[] file) {
        this.file = file;
    }


    /**
     * Gets the keyword value for this NewDocumentRequestType.
     * 
     * @return keyword
     */
    public br.andrew.sap.document.KeyWordData[] getKeyword() {
        return keyword;
    }


    /**
     * Sets the keyword value for this NewDocumentRequestType.
     * 
     * @param keyword
     */
    public void setKeyword(br.andrew.sap.document.KeyWordData[] keyword) {
        this.keyword = keyword;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewDocumentRequestType)) return false;
        NewDocumentRequestType other = (NewDocumentRequestType) obj;
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
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.dsresume==null && other.getDsresume()==null) || 
             (this.dsresume!=null &&
              this.dsresume.equals(other.getDsresume()))) &&
            ((this.dtdocument==null && other.getDtdocument()==null) || 
             (this.dtdocument!=null &&
              this.dtdocument.equals(other.getDtdocument()))) &&
            ((this.attributes==null && other.getAttributes()==null) || 
             (this.attributes!=null &&
              this.attributes.equals(other.getAttributes()))) &&
            ((this.iduser==null && other.getIduser()==null) || 
             (this.iduser!=null &&
              this.iduser.equals(other.getIduser()))) &&
            ((this.participants==null && other.getParticipants()==null) || 
             (this.participants!=null &&
              java.util.Arrays.equals(this.participants, other.getParticipants()))) &&
            ((this.fgmodel==null && other.getFgmodel()==null) || 
             (this.fgmodel!=null &&
              this.fgmodel.equals(other.getFgmodel()))) &&
            ((this.file==null && other.getFile()==null) || 
             (this.file!=null &&
              java.util.Arrays.equals(this.file, other.getFile()))) &&
            ((this.keyword==null && other.getKeyword()==null) || 
             (this.keyword!=null &&
              java.util.Arrays.equals(this.keyword, other.getKeyword())));
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
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDsresume() != null) {
            _hashCode += getDsresume().hashCode();
        }
        if (getDtdocument() != null) {
            _hashCode += getDtdocument().hashCode();
        }
        if (getAttributes() != null) {
            _hashCode += getAttributes().hashCode();
        }
        if (getIduser() != null) {
            _hashCode += getIduser().hashCode();
        }
        if (getParticipants() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParticipants());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParticipants(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFgmodel() != null) {
            _hashCode += getFgmodel().hashCode();
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
        if (getKeyword() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getKeyword());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getKeyword(), i);
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
        new org.apache.axis.description.TypeDesc(NewDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newDocumentRequestType"));
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
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dsresume");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "dsresume"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dtdocument");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "dtdocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "attributes"));
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
        elemField.setFieldName("participants");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "participants"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "participantsData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fgmodel");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "fgmodel"));
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
        elemField.setFieldName("keyword");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "keyword"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "keyWordData"));
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
