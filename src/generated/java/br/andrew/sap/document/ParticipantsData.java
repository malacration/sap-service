/**
 * ParticipantsData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class ParticipantsData  implements java.io.Serializable {
    private int CONTROL;

    private java.lang.String ENTCONTROL;

    private java.lang.String STEP;

    private int SEQUENCE;

    private int QTDEADLINE;

    public ParticipantsData() {
    }

    public ParticipantsData(
           int CONTROL,
           java.lang.String ENTCONTROL,
           java.lang.String STEP,
           int SEQUENCE,
           int QTDEADLINE) {
           this.CONTROL = CONTROL;
           this.ENTCONTROL = ENTCONTROL;
           this.STEP = STEP;
           this.SEQUENCE = SEQUENCE;
           this.QTDEADLINE = QTDEADLINE;
    }


    /**
     * Gets the CONTROL value for this ParticipantsData.
     * 
     * @return CONTROL
     */
    public int getCONTROL() {
        return CONTROL;
    }


    /**
     * Sets the CONTROL value for this ParticipantsData.
     * 
     * @param CONTROL
     */
    public void setCONTROL(int CONTROL) {
        this.CONTROL = CONTROL;
    }


    /**
     * Gets the ENTCONTROL value for this ParticipantsData.
     * 
     * @return ENTCONTROL
     */
    public java.lang.String getENTCONTROL() {
        return ENTCONTROL;
    }


    /**
     * Sets the ENTCONTROL value for this ParticipantsData.
     * 
     * @param ENTCONTROL
     */
    public void setENTCONTROL(java.lang.String ENTCONTROL) {
        this.ENTCONTROL = ENTCONTROL;
    }


    /**
     * Gets the STEP value for this ParticipantsData.
     * 
     * @return STEP
     */
    public java.lang.String getSTEP() {
        return STEP;
    }


    /**
     * Sets the STEP value for this ParticipantsData.
     * 
     * @param STEP
     */
    public void setSTEP(java.lang.String STEP) {
        this.STEP = STEP;
    }


    /**
     * Gets the SEQUENCE value for this ParticipantsData.
     * 
     * @return SEQUENCE
     */
    public int getSEQUENCE() {
        return SEQUENCE;
    }


    /**
     * Sets the SEQUENCE value for this ParticipantsData.
     * 
     * @param SEQUENCE
     */
    public void setSEQUENCE(int SEQUENCE) {
        this.SEQUENCE = SEQUENCE;
    }


    /**
     * Gets the QTDEADLINE value for this ParticipantsData.
     * 
     * @return QTDEADLINE
     */
    public int getQTDEADLINE() {
        return QTDEADLINE;
    }


    /**
     * Sets the QTDEADLINE value for this ParticipantsData.
     * 
     * @param QTDEADLINE
     */
    public void setQTDEADLINE(int QTDEADLINE) {
        this.QTDEADLINE = QTDEADLINE;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParticipantsData)) return false;
        ParticipantsData other = (ParticipantsData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.CONTROL == other.getCONTROL() &&
            ((this.ENTCONTROL==null && other.getENTCONTROL()==null) || 
             (this.ENTCONTROL!=null &&
              this.ENTCONTROL.equals(other.getENTCONTROL()))) &&
            ((this.STEP==null && other.getSTEP()==null) || 
             (this.STEP!=null &&
              this.STEP.equals(other.getSTEP()))) &&
            this.SEQUENCE == other.getSEQUENCE() &&
            this.QTDEADLINE == other.getQTDEADLINE();
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
        _hashCode += getCONTROL();
        if (getENTCONTROL() != null) {
            _hashCode += getENTCONTROL().hashCode();
        }
        if (getSTEP() != null) {
            _hashCode += getSTEP().hashCode();
        }
        _hashCode += getSEQUENCE();
        _hashCode += getQTDEADLINE();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ParticipantsData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "participantsData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CONTROL");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CONTROL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ENTCONTROL");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ENTCONTROL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("STEP");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "STEP"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SEQUENCE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "SEQUENCE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("QTDEADLINE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "QTDEADLINE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
