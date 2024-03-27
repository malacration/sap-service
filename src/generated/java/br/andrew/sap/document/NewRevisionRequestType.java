/**
 * NewRevisionRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewRevisionRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String iduser;

    private br.andrew.sap.document.ParticipantsData[] participants;

    private br.andrew.sap.document.DocumentData documentdata;

    private br.andrew.sap.document.EletronicFile[] file;

    private int fgstatus;

    private java.lang.String dsjustify;

    private java.lang.String idcategory;

    private java.lang.String idrevision;

    public NewRevisionRequestType() {
    }

    public NewRevisionRequestType(
           java.lang.String iddocument,
           java.lang.String iduser,
           br.andrew.sap.document.ParticipantsData[] participants,
           br.andrew.sap.document.DocumentData documentdata,
           br.andrew.sap.document.EletronicFile[] file,
           int fgstatus,
           java.lang.String dsjustify,
           java.lang.String idcategory,
           java.lang.String idrevision) {
           this.iddocument = iddocument;
           this.iduser = iduser;
           this.participants = participants;
           this.documentdata = documentdata;
           this.file = file;
           this.fgstatus = fgstatus;
           this.dsjustify = dsjustify;
           this.idcategory = idcategory;
           this.idrevision = idrevision;
    }


    /**
     * Gets the iddocument value for this NewRevisionRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this NewRevisionRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the iduser value for this NewRevisionRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this NewRevisionRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the participants value for this NewRevisionRequestType.
     * 
     * @return participants
     */
    public br.andrew.sap.document.ParticipantsData[] getParticipants() {
        return participants;
    }


    /**
     * Sets the participants value for this NewRevisionRequestType.
     * 
     * @param participants
     */
    public void setParticipants(br.andrew.sap.document.ParticipantsData[] participants) {
        this.participants = participants;
    }


    /**
     * Gets the documentdata value for this NewRevisionRequestType.
     * 
     * @return documentdata
     */
    public br.andrew.sap.document.DocumentData getDocumentdata() {
        return documentdata;
    }


    /**
     * Sets the documentdata value for this NewRevisionRequestType.
     * 
     * @param documentdata
     */
    public void setDocumentdata(br.andrew.sap.document.DocumentData documentdata) {
        this.documentdata = documentdata;
    }


    /**
     * Gets the file value for this NewRevisionRequestType.
     * 
     * @return file
     */
    public br.andrew.sap.document.EletronicFile[] getFile() {
        return file;
    }


    /**
     * Sets the file value for this NewRevisionRequestType.
     * 
     * @param file
     */
    public void setFile(br.andrew.sap.document.EletronicFile[] file) {
        this.file = file;
    }


    /**
     * Gets the fgstatus value for this NewRevisionRequestType.
     * 
     * @return fgstatus
     */
    public int getFgstatus() {
        return fgstatus;
    }


    /**
     * Sets the fgstatus value for this NewRevisionRequestType.
     * 
     * @param fgstatus
     */
    public void setFgstatus(int fgstatus) {
        this.fgstatus = fgstatus;
    }


    /**
     * Gets the dsjustify value for this NewRevisionRequestType.
     * 
     * @return dsjustify
     */
    public java.lang.String getDsjustify() {
        return dsjustify;
    }


    /**
     * Sets the dsjustify value for this NewRevisionRequestType.
     * 
     * @param dsjustify
     */
    public void setDsjustify(java.lang.String dsjustify) {
        this.dsjustify = dsjustify;
    }


    /**
     * Gets the idcategory value for this NewRevisionRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this NewRevisionRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }


    /**
     * Gets the idrevision value for this NewRevisionRequestType.
     * 
     * @return idrevision
     */
    public java.lang.String getIdrevision() {
        return idrevision;
    }


    /**
     * Sets the idrevision value for this NewRevisionRequestType.
     * 
     * @param idrevision
     */
    public void setIdrevision(java.lang.String idrevision) {
        this.idrevision = idrevision;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewRevisionRequestType)) return false;
        NewRevisionRequestType other = (NewRevisionRequestType) obj;
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
            ((this.participants==null && other.getParticipants()==null) || 
             (this.participants!=null &&
              java.util.Arrays.equals(this.participants, other.getParticipants()))) &&
            ((this.documentdata==null && other.getDocumentdata()==null) || 
             (this.documentdata!=null &&
              this.documentdata.equals(other.getDocumentdata()))) &&
            ((this.file==null && other.getFile()==null) || 
             (this.file!=null &&
              java.util.Arrays.equals(this.file, other.getFile()))) &&
            this.fgstatus == other.getFgstatus() &&
            ((this.dsjustify==null && other.getDsjustify()==null) || 
             (this.dsjustify!=null &&
              this.dsjustify.equals(other.getDsjustify()))) &&
            ((this.idcategory==null && other.getIdcategory()==null) || 
             (this.idcategory!=null &&
              this.idcategory.equals(other.getIdcategory()))) &&
            ((this.idrevision==null && other.getIdrevision()==null) || 
             (this.idrevision!=null &&
              this.idrevision.equals(other.getIdrevision())));
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
        if (getDocumentdata() != null) {
            _hashCode += getDocumentdata().hashCode();
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
        _hashCode += getFgstatus();
        if (getDsjustify() != null) {
            _hashCode += getDsjustify().hashCode();
        }
        if (getIdcategory() != null) {
            _hashCode += getIdcategory().hashCode();
        }
        if (getIdrevision() != null) {
            _hashCode += getIdrevision().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewRevisionRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newRevisionRequestType"));
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
        elemField.setFieldName("participants");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "participants"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "participantsData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentdata");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "documentdata"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "documentData"));
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
        elemField.setFieldName("fgstatus");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "fgstatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dsjustify");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "dsjustify"));
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
        elemField.setFieldName("idrevision");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idrevision"));
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
