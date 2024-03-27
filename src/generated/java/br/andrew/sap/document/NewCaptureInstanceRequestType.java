/**
 * NewCaptureInstanceRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewCaptureInstanceRequestType  implements java.io.Serializable {
    private java.lang.String processid;

    private java.lang.String workflowtitle;

    private java.lang.String documentbatchid;

    private java.lang.String userid;

    private java.lang.String captbatchid;

    public NewCaptureInstanceRequestType() {
    }

    public NewCaptureInstanceRequestType(
           java.lang.String processid,
           java.lang.String workflowtitle,
           java.lang.String documentbatchid,
           java.lang.String userid,
           java.lang.String captbatchid) {
           this.processid = processid;
           this.workflowtitle = workflowtitle;
           this.documentbatchid = documentbatchid;
           this.userid = userid;
           this.captbatchid = captbatchid;
    }


    /**
     * Gets the processid value for this NewCaptureInstanceRequestType.
     * 
     * @return processid
     */
    public java.lang.String getProcessid() {
        return processid;
    }


    /**
     * Sets the processid value for this NewCaptureInstanceRequestType.
     * 
     * @param processid
     */
    public void setProcessid(java.lang.String processid) {
        this.processid = processid;
    }


    /**
     * Gets the workflowtitle value for this NewCaptureInstanceRequestType.
     * 
     * @return workflowtitle
     */
    public java.lang.String getWorkflowtitle() {
        return workflowtitle;
    }


    /**
     * Sets the workflowtitle value for this NewCaptureInstanceRequestType.
     * 
     * @param workflowtitle
     */
    public void setWorkflowtitle(java.lang.String workflowtitle) {
        this.workflowtitle = workflowtitle;
    }


    /**
     * Gets the documentbatchid value for this NewCaptureInstanceRequestType.
     * 
     * @return documentbatchid
     */
    public java.lang.String getDocumentbatchid() {
        return documentbatchid;
    }


    /**
     * Sets the documentbatchid value for this NewCaptureInstanceRequestType.
     * 
     * @param documentbatchid
     */
    public void setDocumentbatchid(java.lang.String documentbatchid) {
        this.documentbatchid = documentbatchid;
    }


    /**
     * Gets the userid value for this NewCaptureInstanceRequestType.
     * 
     * @return userid
     */
    public java.lang.String getUserid() {
        return userid;
    }


    /**
     * Sets the userid value for this NewCaptureInstanceRequestType.
     * 
     * @param userid
     */
    public void setUserid(java.lang.String userid) {
        this.userid = userid;
    }


    /**
     * Gets the captbatchid value for this NewCaptureInstanceRequestType.
     * 
     * @return captbatchid
     */
    public java.lang.String getCaptbatchid() {
        return captbatchid;
    }


    /**
     * Sets the captbatchid value for this NewCaptureInstanceRequestType.
     * 
     * @param captbatchid
     */
    public void setCaptbatchid(java.lang.String captbatchid) {
        this.captbatchid = captbatchid;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewCaptureInstanceRequestType)) return false;
        NewCaptureInstanceRequestType other = (NewCaptureInstanceRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.processid==null && other.getProcessid()==null) || 
             (this.processid!=null &&
              this.processid.equals(other.getProcessid()))) &&
            ((this.workflowtitle==null && other.getWorkflowtitle()==null) || 
             (this.workflowtitle!=null &&
              this.workflowtitle.equals(other.getWorkflowtitle()))) &&
            ((this.documentbatchid==null && other.getDocumentbatchid()==null) || 
             (this.documentbatchid!=null &&
              this.documentbatchid.equals(other.getDocumentbatchid()))) &&
            ((this.userid==null && other.getUserid()==null) || 
             (this.userid!=null &&
              this.userid.equals(other.getUserid()))) &&
            ((this.captbatchid==null && other.getCaptbatchid()==null) || 
             (this.captbatchid!=null &&
              this.captbatchid.equals(other.getCaptbatchid())));
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
        if (getProcessid() != null) {
            _hashCode += getProcessid().hashCode();
        }
        if (getWorkflowtitle() != null) {
            _hashCode += getWorkflowtitle().hashCode();
        }
        if (getDocumentbatchid() != null) {
            _hashCode += getDocumentbatchid().hashCode();
        }
        if (getUserid() != null) {
            _hashCode += getUserid().hashCode();
        }
        if (getCaptbatchid() != null) {
            _hashCode += getCaptbatchid().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewCaptureInstanceRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newCaptureInstanceRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processid");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "processid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowtitle");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "workflowtitle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentbatchid");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "documentbatchid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userid");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "userid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("captbatchid");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "captbatchid"));
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
