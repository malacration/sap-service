/**
 * NewDocument2RequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewDocument2RequestType  implements java.io.Serializable {
    private java.lang.String categoryID;

    private java.lang.String documentID;

    private java.lang.String title;

    private java.lang.String summary;

    private java.lang.String date;

    private br.andrew.sap.document.AttribData[] attributes;

    private java.lang.String responsibleUserID;

    private br.andrew.sap.document.MembersData[] revisionMembers;

    private java.lang.String templateID;

    private br.andrew.sap.document.File[] files;

    private br.andrew.sap.document.KeyWordData[] keywords;

    private java.lang.String languageID;

    private java.lang.String revisionID;

    public NewDocument2RequestType() {
    }

    public NewDocument2RequestType(
           java.lang.String categoryID,
           java.lang.String documentID,
           java.lang.String title,
           java.lang.String summary,
           java.lang.String date,
           br.andrew.sap.document.AttribData[] attributes,
           java.lang.String responsibleUserID,
           br.andrew.sap.document.MembersData[] revisionMembers,
           java.lang.String templateID,
           br.andrew.sap.document.File[] files,
           br.andrew.sap.document.KeyWordData[] keywords,
           java.lang.String languageID,
           java.lang.String revisionID) {
           this.categoryID = categoryID;
           this.documentID = documentID;
           this.title = title;
           this.summary = summary;
           this.date = date;
           this.attributes = attributes;
           this.responsibleUserID = responsibleUserID;
           this.revisionMembers = revisionMembers;
           this.templateID = templateID;
           this.files = files;
           this.keywords = keywords;
           this.languageID = languageID;
           this.revisionID = revisionID;
    }


    /**
     * Gets the categoryID value for this NewDocument2RequestType.
     * 
     * @return categoryID
     */
    public java.lang.String getCategoryID() {
        return categoryID;
    }


    /**
     * Sets the categoryID value for this NewDocument2RequestType.
     * 
     * @param categoryID
     */
    public void setCategoryID(java.lang.String categoryID) {
        this.categoryID = categoryID;
    }


    /**
     * Gets the documentID value for this NewDocument2RequestType.
     * 
     * @return documentID
     */
    public java.lang.String getDocumentID() {
        return documentID;
    }


    /**
     * Sets the documentID value for this NewDocument2RequestType.
     * 
     * @param documentID
     */
    public void setDocumentID(java.lang.String documentID) {
        this.documentID = documentID;
    }


    /**
     * Gets the title value for this NewDocument2RequestType.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this NewDocument2RequestType.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the summary value for this NewDocument2RequestType.
     * 
     * @return summary
     */
    public java.lang.String getSummary() {
        return summary;
    }


    /**
     * Sets the summary value for this NewDocument2RequestType.
     * 
     * @param summary
     */
    public void setSummary(java.lang.String summary) {
        this.summary = summary;
    }


    /**
     * Gets the date value for this NewDocument2RequestType.
     * 
     * @return date
     */
    public java.lang.String getDate() {
        return date;
    }


    /**
     * Sets the date value for this NewDocument2RequestType.
     * 
     * @param date
     */
    public void setDate(java.lang.String date) {
        this.date = date;
    }


    /**
     * Gets the attributes value for this NewDocument2RequestType.
     * 
     * @return attributes
     */
    public br.andrew.sap.document.AttribData[] getAttributes() {
        return attributes;
    }


    /**
     * Sets the attributes value for this NewDocument2RequestType.
     * 
     * @param attributes
     */
    public void setAttributes(br.andrew.sap.document.AttribData[] attributes) {
        this.attributes = attributes;
    }


    /**
     * Gets the responsibleUserID value for this NewDocument2RequestType.
     * 
     * @return responsibleUserID
     */
    public java.lang.String getResponsibleUserID() {
        return responsibleUserID;
    }


    /**
     * Sets the responsibleUserID value for this NewDocument2RequestType.
     * 
     * @param responsibleUserID
     */
    public void setResponsibleUserID(java.lang.String responsibleUserID) {
        this.responsibleUserID = responsibleUserID;
    }


    /**
     * Gets the revisionMembers value for this NewDocument2RequestType.
     * 
     * @return revisionMembers
     */
    public br.andrew.sap.document.MembersData[] getRevisionMembers() {
        return revisionMembers;
    }


    /**
     * Sets the revisionMembers value for this NewDocument2RequestType.
     * 
     * @param revisionMembers
     */
    public void setRevisionMembers(br.andrew.sap.document.MembersData[] revisionMembers) {
        this.revisionMembers = revisionMembers;
    }


    /**
     * Gets the templateID value for this NewDocument2RequestType.
     * 
     * @return templateID
     */
    public java.lang.String getTemplateID() {
        return templateID;
    }


    /**
     * Sets the templateID value for this NewDocument2RequestType.
     * 
     * @param templateID
     */
    public void setTemplateID(java.lang.String templateID) {
        this.templateID = templateID;
    }


    /**
     * Gets the files value for this NewDocument2RequestType.
     * 
     * @return files
     */
    public br.andrew.sap.document.File[] getFiles() {
        return files;
    }


    /**
     * Sets the files value for this NewDocument2RequestType.
     * 
     * @param files
     */
    public void setFiles(br.andrew.sap.document.File[] files) {
        this.files = files;
    }


    /**
     * Gets the keywords value for this NewDocument2RequestType.
     * 
     * @return keywords
     */
    public br.andrew.sap.document.KeyWordData[] getKeywords() {
        return keywords;
    }


    /**
     * Sets the keywords value for this NewDocument2RequestType.
     * 
     * @param keywords
     */
    public void setKeywords(br.andrew.sap.document.KeyWordData[] keywords) {
        this.keywords = keywords;
    }


    /**
     * Gets the languageID value for this NewDocument2RequestType.
     * 
     * @return languageID
     */
    public java.lang.String getLanguageID() {
        return languageID;
    }


    /**
     * Sets the languageID value for this NewDocument2RequestType.
     * 
     * @param languageID
     */
    public void setLanguageID(java.lang.String languageID) {
        this.languageID = languageID;
    }


    /**
     * Gets the revisionID value for this NewDocument2RequestType.
     * 
     * @return revisionID
     */
    public java.lang.String getRevisionID() {
        return revisionID;
    }


    /**
     * Sets the revisionID value for this NewDocument2RequestType.
     * 
     * @param revisionID
     */
    public void setRevisionID(java.lang.String revisionID) {
        this.revisionID = revisionID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewDocument2RequestType)) return false;
        NewDocument2RequestType other = (NewDocument2RequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.categoryID==null && other.getCategoryID()==null) || 
             (this.categoryID!=null &&
              this.categoryID.equals(other.getCategoryID()))) &&
            ((this.documentID==null && other.getDocumentID()==null) || 
             (this.documentID!=null &&
              this.documentID.equals(other.getDocumentID()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.summary==null && other.getSummary()==null) || 
             (this.summary!=null &&
              this.summary.equals(other.getSummary()))) &&
            ((this.date==null && other.getDate()==null) || 
             (this.date!=null &&
              this.date.equals(other.getDate()))) &&
            ((this.attributes==null && other.getAttributes()==null) || 
             (this.attributes!=null &&
              java.util.Arrays.equals(this.attributes, other.getAttributes()))) &&
            ((this.responsibleUserID==null && other.getResponsibleUserID()==null) || 
             (this.responsibleUserID!=null &&
              this.responsibleUserID.equals(other.getResponsibleUserID()))) &&
            ((this.revisionMembers==null && other.getRevisionMembers()==null) || 
             (this.revisionMembers!=null &&
              java.util.Arrays.equals(this.revisionMembers, other.getRevisionMembers()))) &&
            ((this.templateID==null && other.getTemplateID()==null) || 
             (this.templateID!=null &&
              this.templateID.equals(other.getTemplateID()))) &&
            ((this.files==null && other.getFiles()==null) || 
             (this.files!=null &&
              java.util.Arrays.equals(this.files, other.getFiles()))) &&
            ((this.keywords==null && other.getKeywords()==null) || 
             (this.keywords!=null &&
              java.util.Arrays.equals(this.keywords, other.getKeywords()))) &&
            ((this.languageID==null && other.getLanguageID()==null) || 
             (this.languageID!=null &&
              this.languageID.equals(other.getLanguageID()))) &&
            ((this.revisionID==null && other.getRevisionID()==null) || 
             (this.revisionID!=null &&
              this.revisionID.equals(other.getRevisionID())));
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
        if (getCategoryID() != null) {
            _hashCode += getCategoryID().hashCode();
        }
        if (getDocumentID() != null) {
            _hashCode += getDocumentID().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getSummary() != null) {
            _hashCode += getSummary().hashCode();
        }
        if (getDate() != null) {
            _hashCode += getDate().hashCode();
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
        if (getResponsibleUserID() != null) {
            _hashCode += getResponsibleUserID().hashCode();
        }
        if (getRevisionMembers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRevisionMembers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRevisionMembers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTemplateID() != null) {
            _hashCode += getTemplateID().hashCode();
        }
        if (getFiles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFiles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFiles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getKeywords() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getKeywords());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getKeywords(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLanguageID() != null) {
            _hashCode += getLanguageID().hashCode();
        }
        if (getRevisionID() != null) {
            _hashCode += getRevisionID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewDocument2RequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newDocument2RequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoryID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CategoryID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DocumentID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("summary");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Summary"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("date");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Date"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributes");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Attributes"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "attribData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responsibleUserID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ResponsibleUserID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("revisionMembers");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "RevisionMembers"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "MembersData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("templateID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "TemplateID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("files");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Files"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "File"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("keywords");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Keywords"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "keyWordData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("languageID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "LanguageID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("revisionID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "RevisionID"));
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
