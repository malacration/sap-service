/**
 * CategoryReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class CategoryReturn  implements java.io.Serializable {
    private int CDCATEGORY;

    private int CDCATEGORYOWNER;

    private java.lang.String IDCATEGORY;

    private java.lang.String NMCATEGORY;

    public CategoryReturn() {
    }

    public CategoryReturn(
           int CDCATEGORY,
           int CDCATEGORYOWNER,
           java.lang.String IDCATEGORY,
           java.lang.String NMCATEGORY) {
           this.CDCATEGORY = CDCATEGORY;
           this.CDCATEGORYOWNER = CDCATEGORYOWNER;
           this.IDCATEGORY = IDCATEGORY;
           this.NMCATEGORY = NMCATEGORY;
    }


    /**
     * Gets the CDCATEGORY value for this CategoryReturn.
     * 
     * @return CDCATEGORY
     */
    public int getCDCATEGORY() {
        return CDCATEGORY;
    }


    /**
     * Sets the CDCATEGORY value for this CategoryReturn.
     * 
     * @param CDCATEGORY
     */
    public void setCDCATEGORY(int CDCATEGORY) {
        this.CDCATEGORY = CDCATEGORY;
    }


    /**
     * Gets the CDCATEGORYOWNER value for this CategoryReturn.
     * 
     * @return CDCATEGORYOWNER
     */
    public int getCDCATEGORYOWNER() {
        return CDCATEGORYOWNER;
    }


    /**
     * Sets the CDCATEGORYOWNER value for this CategoryReturn.
     * 
     * @param CDCATEGORYOWNER
     */
    public void setCDCATEGORYOWNER(int CDCATEGORYOWNER) {
        this.CDCATEGORYOWNER = CDCATEGORYOWNER;
    }


    /**
     * Gets the IDCATEGORY value for this CategoryReturn.
     * 
     * @return IDCATEGORY
     */
    public java.lang.String getIDCATEGORY() {
        return IDCATEGORY;
    }


    /**
     * Sets the IDCATEGORY value for this CategoryReturn.
     * 
     * @param IDCATEGORY
     */
    public void setIDCATEGORY(java.lang.String IDCATEGORY) {
        this.IDCATEGORY = IDCATEGORY;
    }


    /**
     * Gets the NMCATEGORY value for this CategoryReturn.
     * 
     * @return NMCATEGORY
     */
    public java.lang.String getNMCATEGORY() {
        return NMCATEGORY;
    }


    /**
     * Sets the NMCATEGORY value for this CategoryReturn.
     * 
     * @param NMCATEGORY
     */
    public void setNMCATEGORY(java.lang.String NMCATEGORY) {
        this.NMCATEGORY = NMCATEGORY;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CategoryReturn)) return false;
        CategoryReturn other = (CategoryReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.CDCATEGORY == other.getCDCATEGORY() &&
            this.CDCATEGORYOWNER == other.getCDCATEGORYOWNER() &&
            ((this.IDCATEGORY==null && other.getIDCATEGORY()==null) || 
             (this.IDCATEGORY!=null &&
              this.IDCATEGORY.equals(other.getIDCATEGORY()))) &&
            ((this.NMCATEGORY==null && other.getNMCATEGORY()==null) || 
             (this.NMCATEGORY!=null &&
              this.NMCATEGORY.equals(other.getNMCATEGORY())));
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
        _hashCode += getCDCATEGORY();
        _hashCode += getCDCATEGORYOWNER();
        if (getIDCATEGORY() != null) {
            _hashCode += getIDCATEGORY().hashCode();
        }
        if (getNMCATEGORY() != null) {
            _hashCode += getNMCATEGORY().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CategoryReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "categoryReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CDCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CDCATEGORY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CDCATEGORYOWNER");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CDCATEGORYOWNER"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDCATEGORY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMCATEGORY"));
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
