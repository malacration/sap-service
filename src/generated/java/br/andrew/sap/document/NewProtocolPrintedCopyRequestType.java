/**
 * NewProtocolPrintedCopyRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewProtocolPrintedCopyRequestType  implements java.io.Serializable {
    private java.lang.String protocolID;

    private java.lang.String copytype;

    private java.lang.String date;

    private java.lang.String reasonID;

    private java.lang.String responsible;

    private java.lang.String copyStationID;

    private java.lang.String copystationresp;

    private java.lang.String company;

    private java.lang.String companyresp;

    private java.lang.String observation;

    private java.lang.String status;

    private java.lang.String receivingresp;

    private java.lang.String receivingdate;

    private br.andrew.sap.document.DocumentsData[] documentsList;

    private java.lang.String destType;

    private java.lang.String ctrlTeam;

    private java.lang.String departmentId;

    private java.lang.String userId;

    public NewProtocolPrintedCopyRequestType() {
    }

    public NewProtocolPrintedCopyRequestType(
           java.lang.String protocolID,
           java.lang.String copytype,
           java.lang.String date,
           java.lang.String reasonID,
           java.lang.String responsible,
           java.lang.String copyStationID,
           java.lang.String copystationresp,
           java.lang.String company,
           java.lang.String companyresp,
           java.lang.String observation,
           java.lang.String status,
           java.lang.String receivingresp,
           java.lang.String receivingdate,
           br.andrew.sap.document.DocumentsData[] documentsList,
           java.lang.String destType,
           java.lang.String ctrlTeam,
           java.lang.String departmentId,
           java.lang.String userId) {
           this.protocolID = protocolID;
           this.copytype = copytype;
           this.date = date;
           this.reasonID = reasonID;
           this.responsible = responsible;
           this.copyStationID = copyStationID;
           this.copystationresp = copystationresp;
           this.company = company;
           this.companyresp = companyresp;
           this.observation = observation;
           this.status = status;
           this.receivingresp = receivingresp;
           this.receivingdate = receivingdate;
           this.documentsList = documentsList;
           this.destType = destType;
           this.ctrlTeam = ctrlTeam;
           this.departmentId = departmentId;
           this.userId = userId;
    }


    /**
     * Gets the protocolID value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return protocolID
     */
    public java.lang.String getProtocolID() {
        return protocolID;
    }


    /**
     * Sets the protocolID value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param protocolID
     */
    public void setProtocolID(java.lang.String protocolID) {
        this.protocolID = protocolID;
    }


    /**
     * Gets the copytype value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return copytype
     */
    public java.lang.String getCopytype() {
        return copytype;
    }


    /**
     * Sets the copytype value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param copytype
     */
    public void setCopytype(java.lang.String copytype) {
        this.copytype = copytype;
    }


    /**
     * Gets the date value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return date
     */
    public java.lang.String getDate() {
        return date;
    }


    /**
     * Sets the date value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param date
     */
    public void setDate(java.lang.String date) {
        this.date = date;
    }


    /**
     * Gets the reasonID value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return reasonID
     */
    public java.lang.String getReasonID() {
        return reasonID;
    }


    /**
     * Sets the reasonID value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param reasonID
     */
    public void setReasonID(java.lang.String reasonID) {
        this.reasonID = reasonID;
    }


    /**
     * Gets the responsible value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return responsible
     */
    public java.lang.String getResponsible() {
        return responsible;
    }


    /**
     * Sets the responsible value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param responsible
     */
    public void setResponsible(java.lang.String responsible) {
        this.responsible = responsible;
    }


    /**
     * Gets the copyStationID value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return copyStationID
     */
    public java.lang.String getCopyStationID() {
        return copyStationID;
    }


    /**
     * Sets the copyStationID value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param copyStationID
     */
    public void setCopyStationID(java.lang.String copyStationID) {
        this.copyStationID = copyStationID;
    }


    /**
     * Gets the copystationresp value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return copystationresp
     */
    public java.lang.String getCopystationresp() {
        return copystationresp;
    }


    /**
     * Sets the copystationresp value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param copystationresp
     */
    public void setCopystationresp(java.lang.String copystationresp) {
        this.copystationresp = copystationresp;
    }


    /**
     * Gets the company value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return company
     */
    public java.lang.String getCompany() {
        return company;
    }


    /**
     * Sets the company value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param company
     */
    public void setCompany(java.lang.String company) {
        this.company = company;
    }


    /**
     * Gets the companyresp value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return companyresp
     */
    public java.lang.String getCompanyresp() {
        return companyresp;
    }


    /**
     * Sets the companyresp value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param companyresp
     */
    public void setCompanyresp(java.lang.String companyresp) {
        this.companyresp = companyresp;
    }


    /**
     * Gets the observation value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return observation
     */
    public java.lang.String getObservation() {
        return observation;
    }


    /**
     * Sets the observation value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param observation
     */
    public void setObservation(java.lang.String observation) {
        this.observation = observation;
    }


    /**
     * Gets the status value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the receivingresp value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return receivingresp
     */
    public java.lang.String getReceivingresp() {
        return receivingresp;
    }


    /**
     * Sets the receivingresp value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param receivingresp
     */
    public void setReceivingresp(java.lang.String receivingresp) {
        this.receivingresp = receivingresp;
    }


    /**
     * Gets the receivingdate value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return receivingdate
     */
    public java.lang.String getReceivingdate() {
        return receivingdate;
    }


    /**
     * Sets the receivingdate value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param receivingdate
     */
    public void setReceivingdate(java.lang.String receivingdate) {
        this.receivingdate = receivingdate;
    }


    /**
     * Gets the documentsList value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return documentsList
     */
    public br.andrew.sap.document.DocumentsData[] getDocumentsList() {
        return documentsList;
    }


    /**
     * Sets the documentsList value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param documentsList
     */
    public void setDocumentsList(br.andrew.sap.document.DocumentsData[] documentsList) {
        this.documentsList = documentsList;
    }


    /**
     * Gets the destType value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return destType
     */
    public java.lang.String getDestType() {
        return destType;
    }


    /**
     * Sets the destType value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param destType
     */
    public void setDestType(java.lang.String destType) {
        this.destType = destType;
    }


    /**
     * Gets the ctrlTeam value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return ctrlTeam
     */
    public java.lang.String getCtrlTeam() {
        return ctrlTeam;
    }


    /**
     * Sets the ctrlTeam value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param ctrlTeam
     */
    public void setCtrlTeam(java.lang.String ctrlTeam) {
        this.ctrlTeam = ctrlTeam;
    }


    /**
     * Gets the departmentId value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return departmentId
     */
    public java.lang.String getDepartmentId() {
        return departmentId;
    }


    /**
     * Sets the departmentId value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param departmentId
     */
    public void setDepartmentId(java.lang.String departmentId) {
        this.departmentId = departmentId;
    }


    /**
     * Gets the userId value for this NewProtocolPrintedCopyRequestType.
     * 
     * @return userId
     */
    public java.lang.String getUserId() {
        return userId;
    }


    /**
     * Sets the userId value for this NewProtocolPrintedCopyRequestType.
     * 
     * @param userId
     */
    public void setUserId(java.lang.String userId) {
        this.userId = userId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewProtocolPrintedCopyRequestType)) return false;
        NewProtocolPrintedCopyRequestType other = (NewProtocolPrintedCopyRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.protocolID==null && other.getProtocolID()==null) || 
             (this.protocolID!=null &&
              this.protocolID.equals(other.getProtocolID()))) &&
            ((this.copytype==null && other.getCopytype()==null) || 
             (this.copytype!=null &&
              this.copytype.equals(other.getCopytype()))) &&
            ((this.date==null && other.getDate()==null) || 
             (this.date!=null &&
              this.date.equals(other.getDate()))) &&
            ((this.reasonID==null && other.getReasonID()==null) || 
             (this.reasonID!=null &&
              this.reasonID.equals(other.getReasonID()))) &&
            ((this.responsible==null && other.getResponsible()==null) || 
             (this.responsible!=null &&
              this.responsible.equals(other.getResponsible()))) &&
            ((this.copyStationID==null && other.getCopyStationID()==null) || 
             (this.copyStationID!=null &&
              this.copyStationID.equals(other.getCopyStationID()))) &&
            ((this.copystationresp==null && other.getCopystationresp()==null) || 
             (this.copystationresp!=null &&
              this.copystationresp.equals(other.getCopystationresp()))) &&
            ((this.company==null && other.getCompany()==null) || 
             (this.company!=null &&
              this.company.equals(other.getCompany()))) &&
            ((this.companyresp==null && other.getCompanyresp()==null) || 
             (this.companyresp!=null &&
              this.companyresp.equals(other.getCompanyresp()))) &&
            ((this.observation==null && other.getObservation()==null) || 
             (this.observation!=null &&
              this.observation.equals(other.getObservation()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.receivingresp==null && other.getReceivingresp()==null) || 
             (this.receivingresp!=null &&
              this.receivingresp.equals(other.getReceivingresp()))) &&
            ((this.receivingdate==null && other.getReceivingdate()==null) || 
             (this.receivingdate!=null &&
              this.receivingdate.equals(other.getReceivingdate()))) &&
            ((this.documentsList==null && other.getDocumentsList()==null) || 
             (this.documentsList!=null &&
              java.util.Arrays.equals(this.documentsList, other.getDocumentsList()))) &&
            ((this.destType==null && other.getDestType()==null) || 
             (this.destType!=null &&
              this.destType.equals(other.getDestType()))) &&
            ((this.ctrlTeam==null && other.getCtrlTeam()==null) || 
             (this.ctrlTeam!=null &&
              this.ctrlTeam.equals(other.getCtrlTeam()))) &&
            ((this.departmentId==null && other.getDepartmentId()==null) || 
             (this.departmentId!=null &&
              this.departmentId.equals(other.getDepartmentId()))) &&
            ((this.userId==null && other.getUserId()==null) || 
             (this.userId!=null &&
              this.userId.equals(other.getUserId())));
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
        if (getProtocolID() != null) {
            _hashCode += getProtocolID().hashCode();
        }
        if (getCopytype() != null) {
            _hashCode += getCopytype().hashCode();
        }
        if (getDate() != null) {
            _hashCode += getDate().hashCode();
        }
        if (getReasonID() != null) {
            _hashCode += getReasonID().hashCode();
        }
        if (getResponsible() != null) {
            _hashCode += getResponsible().hashCode();
        }
        if (getCopyStationID() != null) {
            _hashCode += getCopyStationID().hashCode();
        }
        if (getCopystationresp() != null) {
            _hashCode += getCopystationresp().hashCode();
        }
        if (getCompany() != null) {
            _hashCode += getCompany().hashCode();
        }
        if (getCompanyresp() != null) {
            _hashCode += getCompanyresp().hashCode();
        }
        if (getObservation() != null) {
            _hashCode += getObservation().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getReceivingresp() != null) {
            _hashCode += getReceivingresp().hashCode();
        }
        if (getReceivingdate() != null) {
            _hashCode += getReceivingdate().hashCode();
        }
        if (getDocumentsList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDocumentsList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDocumentsList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDestType() != null) {
            _hashCode += getDestType().hashCode();
        }
        if (getCtrlTeam() != null) {
            _hashCode += getCtrlTeam().hashCode();
        }
        if (getDepartmentId() != null) {
            _hashCode += getDepartmentId().hashCode();
        }
        if (getUserId() != null) {
            _hashCode += getUserId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewProtocolPrintedCopyRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopyRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("protocolID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ProtocolID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copytype");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Copytype"));
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
        elemField.setFieldName("reasonID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ReasonID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responsible");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Responsible"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copyStationID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CopyStationID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copystationresp");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Copystationresp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("company");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Company"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("companyresp");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Companyresp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("observation");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Observation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receivingresp");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Receivingresp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receivingdate");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "Receivingdate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentsList");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DocumentsList"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "documentsData"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "Documents"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("destType");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DestType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ctrlTeam");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CtrlTeam"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("departmentId");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DepartmentId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userId");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "UserId"));
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
