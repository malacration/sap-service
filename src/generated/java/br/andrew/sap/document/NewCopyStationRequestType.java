/**
 * NewCopyStationRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewCopyStationRequestType  implements java.io.Serializable {
    private java.lang.String copyStationID;

    private java.lang.String copyStationName;

    private java.lang.String copyStationOwnerID;

    private java.lang.String teamID;

    private java.lang.String userID;

    public NewCopyStationRequestType() {
    }

    public NewCopyStationRequestType(
           java.lang.String copyStationID,
           java.lang.String copyStationName,
           java.lang.String copyStationOwnerID,
           java.lang.String teamID,
           java.lang.String userID) {
           this.copyStationID = copyStationID;
           this.copyStationName = copyStationName;
           this.copyStationOwnerID = copyStationOwnerID;
           this.teamID = teamID;
           this.userID = userID;
    }


    /**
     * Gets the copyStationID value for this NewCopyStationRequestType.
     * 
     * @return copyStationID
     */
    public java.lang.String getCopyStationID() {
        return copyStationID;
    }


    /**
     * Sets the copyStationID value for this NewCopyStationRequestType.
     * 
     * @param copyStationID
     */
    public void setCopyStationID(java.lang.String copyStationID) {
        this.copyStationID = copyStationID;
    }


    /**
     * Gets the copyStationName value for this NewCopyStationRequestType.
     * 
     * @return copyStationName
     */
    public java.lang.String getCopyStationName() {
        return copyStationName;
    }


    /**
     * Sets the copyStationName value for this NewCopyStationRequestType.
     * 
     * @param copyStationName
     */
    public void setCopyStationName(java.lang.String copyStationName) {
        this.copyStationName = copyStationName;
    }


    /**
     * Gets the copyStationOwnerID value for this NewCopyStationRequestType.
     * 
     * @return copyStationOwnerID
     */
    public java.lang.String getCopyStationOwnerID() {
        return copyStationOwnerID;
    }


    /**
     * Sets the copyStationOwnerID value for this NewCopyStationRequestType.
     * 
     * @param copyStationOwnerID
     */
    public void setCopyStationOwnerID(java.lang.String copyStationOwnerID) {
        this.copyStationOwnerID = copyStationOwnerID;
    }


    /**
     * Gets the teamID value for this NewCopyStationRequestType.
     * 
     * @return teamID
     */
    public java.lang.String getTeamID() {
        return teamID;
    }


    /**
     * Sets the teamID value for this NewCopyStationRequestType.
     * 
     * @param teamID
     */
    public void setTeamID(java.lang.String teamID) {
        this.teamID = teamID;
    }


    /**
     * Gets the userID value for this NewCopyStationRequestType.
     * 
     * @return userID
     */
    public java.lang.String getUserID() {
        return userID;
    }


    /**
     * Sets the userID value for this NewCopyStationRequestType.
     * 
     * @param userID
     */
    public void setUserID(java.lang.String userID) {
        this.userID = userID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewCopyStationRequestType)) return false;
        NewCopyStationRequestType other = (NewCopyStationRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.copyStationID==null && other.getCopyStationID()==null) || 
             (this.copyStationID!=null &&
              this.copyStationID.equals(other.getCopyStationID()))) &&
            ((this.copyStationName==null && other.getCopyStationName()==null) || 
             (this.copyStationName!=null &&
              this.copyStationName.equals(other.getCopyStationName()))) &&
            ((this.copyStationOwnerID==null && other.getCopyStationOwnerID()==null) || 
             (this.copyStationOwnerID!=null &&
              this.copyStationOwnerID.equals(other.getCopyStationOwnerID()))) &&
            ((this.teamID==null && other.getTeamID()==null) || 
             (this.teamID!=null &&
              this.teamID.equals(other.getTeamID()))) &&
            ((this.userID==null && other.getUserID()==null) || 
             (this.userID!=null &&
              this.userID.equals(other.getUserID())));
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
        if (getCopyStationID() != null) {
            _hashCode += getCopyStationID().hashCode();
        }
        if (getCopyStationName() != null) {
            _hashCode += getCopyStationName().hashCode();
        }
        if (getCopyStationOwnerID() != null) {
            _hashCode += getCopyStationOwnerID().hashCode();
        }
        if (getTeamID() != null) {
            _hashCode += getTeamID().hashCode();
        }
        if (getUserID() != null) {
            _hashCode += getUserID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewCopyStationRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newCopyStationRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copyStationID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CopyStationID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copyStationName");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CopyStationName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copyStationOwnerID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CopyStationOwnerID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("teamID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "TeamID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "UserID"));
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
