/**
 * SetAttributeValueRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class SetAttributeValueRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String idrevision;

    private java.lang.String idattribute;

    private java.lang.String vlattribute;

    private java.lang.String separator;

    public SetAttributeValueRequestType() {
    }

    public SetAttributeValueRequestType(
           java.lang.String iddocument,
           java.lang.String idrevision,
           java.lang.String idattribute,
           java.lang.String vlattribute,
           java.lang.String separator) {
           this.iddocument = iddocument;
           this.idrevision = idrevision;
           this.idattribute = idattribute;
           this.vlattribute = vlattribute;
           this.separator = separator;
    }


    /**
     * Gets the iddocument value for this SetAttributeValueRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this SetAttributeValueRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the idrevision value for this SetAttributeValueRequestType.
     * 
     * @return idrevision
     */
    public java.lang.String getIdrevision() {
        return idrevision;
    }


    /**
     * Sets the idrevision value for this SetAttributeValueRequestType.
     * 
     * @param idrevision
     */
    public void setIdrevision(java.lang.String idrevision) {
        this.idrevision = idrevision;
    }


    /**
     * Gets the idattribute value for this SetAttributeValueRequestType.
     * 
     * @return idattribute
     */
    public java.lang.String getIdattribute() {
        return idattribute;
    }


    /**
     * Sets the idattribute value for this SetAttributeValueRequestType.
     * 
     * @param idattribute
     */
    public void setIdattribute(java.lang.String idattribute) {
        this.idattribute = idattribute;
    }


    /**
     * Gets the vlattribute value for this SetAttributeValueRequestType.
     * 
     * @return vlattribute
     */
    public java.lang.String getVlattribute() {
        return vlattribute;
    }


    /**
     * Sets the vlattribute value for this SetAttributeValueRequestType.
     * 
     * @param vlattribute
     */
    public void setVlattribute(java.lang.String vlattribute) {
        this.vlattribute = vlattribute;
    }


    /**
     * Gets the separator value for this SetAttributeValueRequestType.
     * 
     * @return separator
     */
    public java.lang.String getSeparator() {
        return separator;
    }


    /**
     * Sets the separator value for this SetAttributeValueRequestType.
     * 
     * @param separator
     */
    public void setSeparator(java.lang.String separator) {
        this.separator = separator;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SetAttributeValueRequestType)) return false;
        SetAttributeValueRequestType other = (SetAttributeValueRequestType) obj;
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
            ((this.idattribute==null && other.getIdattribute()==null) || 
             (this.idattribute!=null &&
              this.idattribute.equals(other.getIdattribute()))) &&
            ((this.vlattribute==null && other.getVlattribute()==null) || 
             (this.vlattribute!=null &&
              this.vlattribute.equals(other.getVlattribute()))) &&
            ((this.separator==null && other.getSeparator()==null) || 
             (this.separator!=null &&
              this.separator.equals(other.getSeparator())));
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
        if (getIdattribute() != null) {
            _hashCode += getIdattribute().hashCode();
        }
        if (getVlattribute() != null) {
            _hashCode += getVlattribute().hashCode();
        }
        if (getSeparator() != null) {
            _hashCode += getSeparator().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SetAttributeValueRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "setAttributeValueRequestType"));
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
        elemField.setFieldName("idattribute");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idattribute"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("vlattribute");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "vlattribute"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("separator");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "separator"));
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
