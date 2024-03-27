/**
 * NewAccessPermissionRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class NewAccessPermissionRequestType  implements java.io.Serializable {
    private java.lang.String iddocument;

    private java.lang.String iduser;

    private int usertype;

    private java.lang.String permission;

    private int permissionstype;

    private java.lang.String fgaddlowerlevel;

    private java.lang.String idcategory;

    public NewAccessPermissionRequestType() {
    }

    public NewAccessPermissionRequestType(
           java.lang.String iddocument,
           java.lang.String iduser,
           int usertype,
           java.lang.String permission,
           int permissionstype,
           java.lang.String fgaddlowerlevel,
           java.lang.String idcategory) {
           this.iddocument = iddocument;
           this.iduser = iduser;
           this.usertype = usertype;
           this.permission = permission;
           this.permissionstype = permissionstype;
           this.fgaddlowerlevel = fgaddlowerlevel;
           this.idcategory = idcategory;
    }


    /**
     * Gets the iddocument value for this NewAccessPermissionRequestType.
     * 
     * @return iddocument
     */
    public java.lang.String getIddocument() {
        return iddocument;
    }


    /**
     * Sets the iddocument value for this NewAccessPermissionRequestType.
     * 
     * @param iddocument
     */
    public void setIddocument(java.lang.String iddocument) {
        this.iddocument = iddocument;
    }


    /**
     * Gets the iduser value for this NewAccessPermissionRequestType.
     * 
     * @return iduser
     */
    public java.lang.String getIduser() {
        return iduser;
    }


    /**
     * Sets the iduser value for this NewAccessPermissionRequestType.
     * 
     * @param iduser
     */
    public void setIduser(java.lang.String iduser) {
        this.iduser = iduser;
    }


    /**
     * Gets the usertype value for this NewAccessPermissionRequestType.
     * 
     * @return usertype
     */
    public int getUsertype() {
        return usertype;
    }


    /**
     * Sets the usertype value for this NewAccessPermissionRequestType.
     * 
     * @param usertype
     */
    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }


    /**
     * Gets the permission value for this NewAccessPermissionRequestType.
     * 
     * @return permission
     */
    public java.lang.String getPermission() {
        return permission;
    }


    /**
     * Sets the permission value for this NewAccessPermissionRequestType.
     * 
     * @param permission
     */
    public void setPermission(java.lang.String permission) {
        this.permission = permission;
    }


    /**
     * Gets the permissionstype value for this NewAccessPermissionRequestType.
     * 
     * @return permissionstype
     */
    public int getPermissionstype() {
        return permissionstype;
    }


    /**
     * Sets the permissionstype value for this NewAccessPermissionRequestType.
     * 
     * @param permissionstype
     */
    public void setPermissionstype(int permissionstype) {
        this.permissionstype = permissionstype;
    }


    /**
     * Gets the fgaddlowerlevel value for this NewAccessPermissionRequestType.
     * 
     * @return fgaddlowerlevel
     */
    public java.lang.String getFgaddlowerlevel() {
        return fgaddlowerlevel;
    }


    /**
     * Sets the fgaddlowerlevel value for this NewAccessPermissionRequestType.
     * 
     * @param fgaddlowerlevel
     */
    public void setFgaddlowerlevel(java.lang.String fgaddlowerlevel) {
        this.fgaddlowerlevel = fgaddlowerlevel;
    }


    /**
     * Gets the idcategory value for this NewAccessPermissionRequestType.
     * 
     * @return idcategory
     */
    public java.lang.String getIdcategory() {
        return idcategory;
    }


    /**
     * Sets the idcategory value for this NewAccessPermissionRequestType.
     * 
     * @param idcategory
     */
    public void setIdcategory(java.lang.String idcategory) {
        this.idcategory = idcategory;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NewAccessPermissionRequestType)) return false;
        NewAccessPermissionRequestType other = (NewAccessPermissionRequestType) obj;
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
            this.usertype == other.getUsertype() &&
            ((this.permission==null && other.getPermission()==null) || 
             (this.permission!=null &&
              this.permission.equals(other.getPermission()))) &&
            this.permissionstype == other.getPermissionstype() &&
            ((this.fgaddlowerlevel==null && other.getFgaddlowerlevel()==null) || 
             (this.fgaddlowerlevel!=null &&
              this.fgaddlowerlevel.equals(other.getFgaddlowerlevel()))) &&
            ((this.idcategory==null && other.getIdcategory()==null) || 
             (this.idcategory!=null &&
              this.idcategory.equals(other.getIdcategory())));
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
        _hashCode += getUsertype();
        if (getPermission() != null) {
            _hashCode += getPermission().hashCode();
        }
        _hashCode += getPermissionstype();
        if (getFgaddlowerlevel() != null) {
            _hashCode += getFgaddlowerlevel().hashCode();
        }
        if (getIdcategory() != null) {
            _hashCode += getIdcategory().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NewAccessPermissionRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "newAccessPermissionRequestType"));
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
        elemField.setFieldName("usertype");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "usertype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("permission");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "permission"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("permissionstype");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "permissionstype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fgaddlowerlevel");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "fgaddlowerlevel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("idcategory");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "idcategory"));
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
